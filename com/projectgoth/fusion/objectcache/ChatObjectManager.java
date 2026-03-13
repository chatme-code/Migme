package com.projectgoth.fusion.objectcache;

import Ice.AlreadyRegisteredException;
import Ice.Identity;
import Ice.LocalException;
import Ice.NotRegisteredException;
import Ice.ObjectAdapter;
import Ice.ObjectImpl;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.chatsync.ChatDefinition;
import com.projectgoth.fusion.chatsync.ChatSyncStore;
import com.projectgoth.fusion.chatsync.RedisChatSyncStore;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.LRUCache;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.exception.ExceptionWithDiagnosticCode;
import com.projectgoth.fusion.exception.GroupChatNoLongerActiveException;
import com.projectgoth.fusion.exception.InternalServerErrorException;
import com.projectgoth.fusion.exception.UserNotOnlineException;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardI;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ChatRoomPrxHelper;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.GroupChatPrxHelper;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardPrxHelper;
import com.projectgoth.fusion.slice.ObjectCacheStats;
import com.projectgoth.fusion.slice.ObjectExistsException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionCachePrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.SessionPrxHelper;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice.UserPrxHelper;
import com.projectgoth.fusion.stats.FusionStatsIceDispatchInterceptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;

public class ChatObjectManager implements ChatObjectManagerUser, ChatObjectManagerSession, ChatObjectManagerRoom, ChatObjectManagerGroup {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatObjectManager.class));
   private final ScheduledThreadPoolExecutor distributionService;
   private final IcePrxFinder icePrxFinder;
   private final AuthenticationServicePrx authenticationServiceProxy;
   private final String iceID;
   private AtomicReference<MessageSwitchboardPrx> localMessageSwitchboardPrx = new AtomicReference((Object)null);
   private final ChatObjectManager.IdleSessionPurger idleSessionPurger;
   private final ChatObjectManager.IdleRoomPurger idleRoomPurger;
   private final ChatObjectManager.IdleGroupChatPurger idleGroupPurger;
   private ChatObjectManager.ObjectMetrics metrics;
   private String instanceID;
   ObjectCacheContext applicationContext;
   private ChatContentStore chatContentStore;
   final ConcurrentMap<String, UserRpcI> userObjects = new ConcurrentHashMap();
   final ConcurrentMap<String, SessionRpcI> sessionObjects = new ConcurrentHashMap();
   final ConcurrentMap<String, ChatRoomRpcI> chatRoomObjects = new ConcurrentHashMap();
   final ConcurrentMap<String, GroupChatRpcI> groupChatObjects = new ConcurrentHashMap();
   LRUCache<String, UserPrx> recentUserProxiesCache;
   LRUCache<String, SessionPrx> recentSessionProxiesCache;
   LRUCache<String, ChatRoomPrx> recentChatRoomProxiesCache;
   LRUCache<String, GroupChatPrx> recentGroupChatProxiesCache;
   private int loadWeightage;
   private Semaphore giftAllSemaphore;
   private static boolean logSessions;

   public ObjectCacheStats getStats(ObjectCacheStats stats) {
      if (stats == null) {
         stats = new ObjectCacheStats();
      }

      this.metrics.getStats(stats);
      return stats;
   }

   public String[] getUsernames() {
      return (String[])this.userObjects.keySet().toArray(new String[this.userObjects.size()]);
   }

   public int getUserCount() {
      return this.userObjects.size();
   }

   public ChatObjectManager(ObjectCacheContext applicationContext, String iceID) {
      this.applicationContext = applicationContext;
      this.iceID = iceID;
      this.idleSessionPurger = new ChatObjectManager.IdleSessionPurger(applicationContext.getProperties());
      this.idleRoomPurger = new ChatObjectManager.IdleRoomPurger(applicationContext.getProperties());
      this.idleGroupPurger = new ChatObjectManager.IdleGroupChatPurger(applicationContext.getProperties());
      this.icePrxFinder = new IcePrxFinder(applicationContext.getCommunicator(), applicationContext.getProperties());
      this.authenticationServiceProxy = this.icePrxFinder.waitForAuthenticationServiceProxy();
      int maxDistributionThreadPoolSize = applicationContext.getProperties().getPropertyAsIntWithDefault("MaxDistributionThreadPoolSize", 25);
      this.distributionService = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(maxDistributionThreadPoolSize);
      this.recentUserProxiesCache = new LRUCache(applicationContext.getProperties().getPropertyAsIntWithDefault("UserProxiesCacheSize", 5000), (long)applicationContext.getProperties().getPropertyAsIntWithDefault("UserProxiesCacheExpiryTime", 60));
      this.recentChatRoomProxiesCache = new LRUCache(applicationContext.getProperties().getPropertyAsIntWithDefault("ChatRoomProxiesCacheSize", 5000), (long)applicationContext.getProperties().getPropertyAsIntWithDefault("ChatRoomProxiesCacheExpiryTime", 60));
      this.recentGroupChatProxiesCache = new LRUCache(applicationContext.getProperties().getPropertyAsIntWithDefault("GroupChatProxiesCacheSize", 1000), (long)applicationContext.getProperties().getPropertyAsIntWithDefault("GroupChatProxiesCacheExpiryTime", 60));
      this.metrics = new ChatObjectManager.ObjectMetrics((long)applicationContext.getProperties().getPropertyAsIntWithDefault("RequestCounterInterval", 10));
      this.chatContentStore = new ChatContentStore(applicationContext);
      this.giftAllSemaphore = new Semaphore(applicationContext.getProperties().getPropertyAsIntWithDefault("OverallGiftAllPermits", 100), true);
      logSessions = applicationContext.getProperties().getPropertyAsIntWithDefault("LogSessions", 1) == 1;
      ChatUser.setLogSessions(logSessions);
      this.loadWeightage = applicationContext.getProperties().getPropertyAsIntWithDefault("LoadWeightage", 100);
   }

   public void setLoadWeightage(int weightage) {
      this.loadWeightage = weightage;
   }

   public int getLoadWeightage() {
      return this.loadWeightage;
   }

   public ScheduledExecutorService getDistributionService() {
      return this.distributionService;
   }

   public int getDistributionServiceQueueSize() {
      return this.distributionService.getQueue().size();
   }

   private ObjectPrx addToCacheAdapter(ObjectImpl object, Identity id) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.ICE_CONNECTION_STATS)) {
         if (log.isDebugEnabled()) {
            log.debug("Enabling FusionStatsIceDispatchInterceptor for " + object);
         }

         FusionStatsIceDispatchInterceptor interceptor = new FusionStatsIceDispatchInterceptor(object);
         return this.getCacheAdapter().add(interceptor, id);
      } else {
         return this.getCacheAdapter().add(object, id);
      }
   }

   private ObjectAdapter getCacheAdapter() {
      return this.applicationContext.getCacheAdapter();
   }

   public RegistryPrx getRegistryPrx() {
      return this.applicationContext.getRegistryPrx();
   }

   public SessionCachePrx getSessionCachePrx() {
      return this.applicationContext.getSessionCachePrx();
   }

   private String getUniqueID() {
      return this.applicationContext.getUniqueID();
   }

   private Identity sessionIdentity(String sessionID) {
      return Util.stringToIdentity("S" + sessionID);
   }

   private Identity userIdentity(String username) {
      return Util.stringToIdentity("U" + username);
   }

   public UserPrx createUser(String username) throws ObjectExistsException, FusionException {
      UserPrx userPrx = null;
      UserRpcI existingUser = (UserRpcI)this.userObjects.get(username);
      FusionException fe;
      if (existingUser != null) {
         log.warn("Request to create a User object for the user '" + username + "' when an object already exists");
         ObjectPrx basePrx = this.getCacheAdapter().createDirectProxy(this.userIdentity(username));
         userPrx = UserPrxHelper.uncheckedCast(basePrx);
         if (log.isDebugEnabled()) {
            log.debug("Registering the (existing) User object [" + username + "] with the Registry");
         }

         try {
            this.getRegistryPrx().registerUserObject(username, userPrx, this.getUniqueID());
         } catch (LocalException var10) {
            log.error("Object Cache " + ObjectCache.hostName + ": Unable to register the User object '" + username + "': Unable to communicate with the Registry", var10);
            fe = new FusionException();
            fe.message = "Unable to register the User object with the Registry";
            throw fe;
         }
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Creating the User object '" + username + "'");
         }

         ChatUser chatUser = new ChatUser(this, username);

         try {
            chatUser.loadFromDB();
            if (log.isDebugEnabled()) {
               log.debug("loaded user [" + username + "] from DB");
            }
         } catch (Exception var11) {
            log.error("Unable to load the user [" + username + "] from the database", var11);
            throw new FusionException(var11.getMessage());
         }

         UserRpcI user = new UserRpcI(this, chatUser);
         fe = null;

         ObjectPrx basePrx;
         try {
            basePrx = this.addToCacheAdapter(user, this.userIdentity(username));
         } catch (Exception var9) {
            log.error("Object Cache " + ObjectCache.hostName + ": Ice.AlreadyRegisteredException when adding a new User object '" + username + "' to the object adapter");
            throw new FusionException("Ice.AlreadyRegisteredException when adding a new User object '" + username + "' to the object adapter");
         }

         userPrx = UserPrxHelper.uncheckedCast(basePrx);
         if (log.isDebugEnabled()) {
            log.debug("Registering the User object [" + username + "] with the Registry");
         }

         try {
            this.getRegistryPrx().registerUserObject(username, userPrx, this.getUniqueID());
         } catch (LocalException var8) {
            log.error("Object Cache " + ObjectCache.hostName + ": Unable to register the User object '" + username + "': Unable to communicate with the Registry");
            throw new FusionException("Unable to communicate with the Registry");
         }

         this.userObjects.put(username, user);
         this.metrics.userAdded(this.userObjects.size());
      }

      this.metrics.request();
      return userPrx;
   }

   public SessionPrx createSession(String sessionID, ChatSession session) {
      SessionRpcI newSession = new SessionRpcI(this, session);
      ObjectPrx basePrx = this.addToCacheAdapter(newSession, this.sessionIdentity(sessionID));
      SessionPrx sessionPrx = SessionPrxHelper.uncheckedCast(basePrx);
      this.metrics.sessionAdded();
      return sessionPrx;
   }

   public ChatRoomPrx createRoom(String name) throws ObjectExistsException, FusionException {
      name = name.toLowerCase();
      if (this.chatRoomObjects.containsKey(name)) {
         throw new ObjectExistsException();
      } else {
         Identity identity = Util.stringToIdentity("C" + name);

         ChatRoomRpcI chatRoom;
         ChatRoomPrx chatRoomPrx;
         ChatRoomData chatRoomData;
         Message botData;
         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
               chatRoomData = DAOFactory.getInstance().getChatRoomDAO().getChatRoom(name);
            } else {
               botData = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               chatRoomData = botData.getChatRoom(name);
            }

            if (chatRoomData == null) {
               throw new FusionException("No such room [" + name + "]");
            }

            GroupData groupData = null;
            if (chatRoomData.groupID != null) {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_GROUP_DAO)) {
                  groupData = DAOFactory.getInstance().getGroupDAO().getGroup(chatRoomData.groupID);
               } else {
                  User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                  groupData = userEJB.getGroup(chatRoomData.groupID);
               }
            }

            Object room;
            if ((Boolean)SystemPropertyEntities.Temp.Cache.se454ChatroomSystemMessageSendMimeTypesEnabled.getValue()) {
               room = new ChatRoom(this, chatRoomData, groupData);
            } else {
               room = new ChatRoomPreSE454(this, chatRoomData, groupData);
            }

            chatRoom = new ChatRoomRpcI((ChatRoom)room);
            chatRoomPrx = ChatRoomPrxHelper.uncheckedCast(this.getCacheAdapter().add(chatRoom, identity));
         } catch (AlreadyRegisteredException var11) {
            throw new ObjectExistsException();
         } catch (FusionException var12) {
            throw var12;
         } catch (Exception var13) {
            log.error("Unable to create chat room object [" + name + "] - " + var13.getMessage());
            throw new ExceptionWithDiagnosticCode("Internal server error. Please try again later", var13, name);
         }

         try {
            this.getRegistryPrx().registerChatRoomObject(name, chatRoomPrx);
         } catch (ObjectExistsException var9) {
            this.getCacheAdapter().remove(identity);
            throw var9;
         } catch (LocalException var10) {
            log.error("Unable to register the Chat Room object [" + name + "]", var10);
            this.getCacheAdapter().remove(identity);
            throw new FusionException("Internal server error (Unable to communicate with Registry)");
         }

         if (chatRoomData.botID != null) {
            try {
               botData = null;
               BotData botData;
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_BOT_DAO)) {
                  botData = DAOFactory.getInstance().getBotDAO().getBot(chatRoomData.botID);
               } else {
                  Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
                  botData = messageEJB.getBot(chatRoomData.botID);
               }

               if (botData != null) {
                  chatRoom.startBot(chatRoomData.name, botData.getCommandName());
               }
            } catch (Exception var8) {
               log.warn("Unable to start bot in chat room [" + name + "]", var8);
               this.getRegistryPrx().deregisterChatRoomObject(name);
               this.getCacheAdapter().remove(identity);
               throw new FusionException("Internal server error (Unable to start attached bot)");
            }
         }

         this.chatRoomObjects.put(name, chatRoom);
         this.metrics.request();
         return chatRoomPrx;
      }
   }

   public GroupChatPrx createGroupChatObject(String id, String creator, String privateChatParticipantIce, String[] otherPartyList) throws FusionException, ObjectExistsException {
      String privateChatParticipant = privateChatParticipantIce.equals("\u0000") ? null : privateChatParticipantIce;
      String[] arr$ = otherPartyList;
      int len$ = otherPartyList.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String p = arr$[i$];
         if (creator.equals(p)) {
            throw new FusionException("You cannot invite yourself");
         }

         if (privateChatParticipant != null && privateChatParticipant.equals(p)) {
            throw new FusionException("You are already chatting with " + privateChatParticipant);
         }
      }

      ChatObjectManager.GroupChatPair groupChatPair = this.createGroupChatObjectCore(id, creator);

      try {
         groupChatPair.local.addInitialParticipants(creator, privateChatParticipant, otherPartyList);
      } catch (FusionException var10) {
         this.purgeGroupChat(id);
         throw var10;
      }

      MessageSwitchboardDispatcher.getInstance().onCreateGroupChat(this.applicationContext, groupChatPair.local.toChatDefinition(), creator, privateChatParticipant, groupChatPair.remote, groupChatPair.local.getCreatorUserID());
      return groupChatPair.remote;
   }

   private ChatObjectManager.GroupChatPair createGroupChatObjectCore(String id, String creator) throws FusionException, ObjectExistsException {
      GroupChatPrx groupChatPrx = null;
      GroupChatRpcI existingGroupChat = (GroupChatRpcI)this.groupChatObjects.get(id);
      GroupChatRpcI groupChat = null;
      if (existingGroupChat != null) {
         log.error("ObjectCache " + ObjectCache.hostName + ": Received request to create the GroupChat object '" + id + "' that already exists");
         throw new ObjectExistsException();
      } else {
         log.debug("Creating the GroupChat object '" + id + "'");
         ChatGroup group = new ChatGroup(this, id);
         groupChat = new GroupChatRpcI(group);
         ObjectPrx basePrx = this.addToCacheAdapter(groupChat, Util.stringToIdentity("G" + id));
         groupChatPrx = GroupChatPrxHelper.uncheckedCast(basePrx);
         groupChat.setGroupChatPrx(groupChatPrx);
         log.debug("Registering the GroupChat object '" + id + "' with the Registry");

         try {
            this.getRegistryPrx().registerGroupChatObject(id, groupChatPrx);
         } catch (LocalException var9) {
            log.error("Object Cache " + ObjectCache.hostName + ": Unable to register the GroupChat object '" + id + "': Unable to communicate with the Registry");
            throw new FusionException("Unable to communicate with the Registry");
         }

         this.groupChatObjects.put(id, groupChat);
         this.metrics.groupAdded(this.groupChatObjects.size());
         this.metrics.request();
         ChatObjectManager.GroupChatPair pair = new ChatObjectManager.GroupChatPair();
         pair.remote = groupChatPrx;
         pair.local = groupChat;
         return pair;
      }
   }

   public UserPrx findUserPrx(String username) throws FusionException {
      UserPrx userPrx = (UserPrx)this.recentUserProxiesCache.get(username);
      if (userPrx != null) {
         try {
            userPrx.ice_ping();
            return userPrx;
         } catch (Exception var6) {
            this.recentUserProxiesCache.remove(username);
         }
      }

      try {
         userPrx = this.getRegistryPrx().findUserObject(username);
         this.recentUserProxiesCache.put(username, userPrx);
         return userPrx;
      } catch (ObjectNotFoundException var4) {
         throw new UserNotOnlineException(username);
      } catch (Exception var5) {
         log.warn("Exception in findUserPrx()", var5);
         throw new InternalServerErrorException(var5, "findUserPrx:" + username);
      }
   }

   public UserPrx makeUserPrx(String username) {
      return UserPrxHelper.uncheckedCast(this.applicationContext.getCacheAdapter().createProxy(this.userIdentity(username)));
   }

   public UserPrx findUserPrxFromRegistry(String fusionUsername) throws ObjectNotFoundException {
      return this.getRegistryPrx().findUserObject(fusionUsername);
   }

   public ChatRoomPrx findChatRoomPrx(String name) throws FusionException {
      ChatRoomPrx chatRoomPrx = (ChatRoomPrx)this.recentChatRoomProxiesCache.get(name);
      if (chatRoomPrx != null) {
         try {
            chatRoomPrx.ice_ping();
            return chatRoomPrx;
         } catch (Exception var6) {
            this.recentChatRoomProxiesCache.remove(name);
         }
      }

      try {
         chatRoomPrx = this.getRegistryPrx().findChatRoomObject(name);
         this.recentChatRoomProxiesCache.put(name, chatRoomPrx);
         return chatRoomPrx;
      } catch (ObjectNotFoundException var4) {
         throw new FusionException("The chat room '" + name + "' could not be found");
      } catch (Exception var5) {
         log.warn("Exception in findChatRoomPrx()", var5);
         throw new InternalServerErrorException(var5, name);
      }
   }

   public GroupChatPrx findGroupChatPrx(String groupChatID) throws FusionException {
      GroupChatPrx groupChat;
      try {
         groupChat = this.findGroupChatPrxInner(groupChatID);
      } catch (Exception var10) {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PersistentGroupChatSettings.ENABLED)) {
            if (var10 instanceof FusionException) {
               throw (FusionException)var10;
            }

            throw new FusionException("In findGroupChatPrx: " + var10.getMessage());
         }

         try {
            ChatDefinition groupChatDef = new ChatDefinition(groupChatID);
            RedisChatSyncStore store = new RedisChatSyncStore(ChatSyncStore.StorePrimacy.SLAVE);
            ChatSyncStore[] stores = new ChatSyncStore[]{store};
            groupChatDef.retrieve(stores);
            groupChat = this.restoreGroupChatObject(groupChatID, groupChatDef.getGroupOwner(), groupChatDef.getParticipantUsernames());
         } catch (ChatDefinition.ChatDefinitionNotFoundException var7) {
            log.info("Group chat " + groupChatID + " unrestorable, has expired from storage");
            throw var7;
         } catch (FusionException var8) {
            log.error("Exception restoring group chat id=" + groupChatID + " e=" + var8, var8);
            throw var8;
         } catch (Exception var9) {
            log.error("Exception restoring group chat id=" + groupChatID + " e=" + var9, var9);
            throw new FusionException(var9.getMessage());
         }
      }

      return groupChat;
   }

   private GroupChatPrx findGroupChatPrxInner(String id) throws FusionException {
      GroupChatPrx groupChatPrx = (GroupChatPrx)this.recentGroupChatProxiesCache.get(id);
      if (groupChatPrx != null) {
         try {
            groupChatPrx.ice_ping();
            return groupChatPrx;
         } catch (Exception var6) {
            this.recentGroupChatProxiesCache.remove(id);
         }
      }

      try {
         groupChatPrx = this.getRegistryPrx().findGroupChatObject(id);
         this.recentGroupChatProxiesCache.put(id, groupChatPrx);
         return groupChatPrx;
      } catch (ObjectNotFoundException var4) {
         throw new GroupChatNoLongerActiveException(id);
      } catch (Exception var5) {
         log.warn("Exception in findGroupChatPrx()", var5);
         throw new InternalServerErrorException(var5, id);
      }
   }

   public void purgeUser(String username) {
      if (log.isDebugEnabled()) {
         log.debug("Purging the User object '" + username + "'");
      }

      UserRpcI user = (UserRpcI)this.userObjects.get(username);
      if (user == null) {
         log.warn("Unable to purge the User object '" + username + "': The user was not found in the cache");
      } else {
         user.prepareForPurge();
         this.userObjects.remove(username);

         try {
            this.getRegistryPrx().deregisterUserObject(username, this.getUniqueID());
         } catch (LocalException var5) {
            log.warn("Unable to deregister the User object '" + username + "': Unable to communicate with the Registry");
         }

         try {
            this.getCacheAdapter().remove(this.userIdentity(username));
         } catch (NotRegisteredException var4) {
            log.warn("Caught Ice.NotRegisteredException when attempting to remove the User object '" + username + "' from the cache adapter");
         }

      }
   }

   public void purgeRoom(String name) {
      if (log.isDebugEnabled()) {
         log.debug("Purging the chat room [" + name + "]");
      }

      log.info("Purging the chat room [" + name + "]");
      ChatRoomRpcI chatRoom = (ChatRoomRpcI)this.chatRoomObjects.remove(name);
      if (chatRoom == null) {
         log.warn("Unable to purge the chat room object '" + name + "': The chat room was not found in the cache");
      }

      try {
         this.getRegistryPrx().deregisterChatRoomObject(name);
      } catch (LocalException var5) {
         log.warn("Unable to deregister the chat room object '" + name + "': Unable to communicate with the Registry");
      }

      try {
         this.getCacheAdapter().remove(Util.stringToIdentity("C" + name));
      } catch (NotRegisteredException var4) {
         log.warn("Caught Ice.NotRegisteredException when attempting to remove the chat room object '" + name + "' from the cache adapter");
      }

   }

   public GroupChatPrx[] getGroupChats() throws FusionException {
      try {
         Collection<GroupChatRpcI> gcis = this.groupChatObjects.values();
         ArrayList<GroupChatPrx> prxs = new ArrayList();
         Iterator i$ = gcis.iterator();

         while(i$.hasNext()) {
            GroupChatRpcI gci = (GroupChatRpcI)i$.next();
            GroupChatPrx prx = this.findGroupChatPrxInner(gci.getId());
            prxs.add(prx);
         }

         return (GroupChatPrx[])prxs.toArray(new GroupChatPrx[prxs.size()]);
      } catch (FusionException var6) {
         log.error("Exception getting group chats: e=" + var6, var6);
         throw var6;
      } catch (Exception var7) {
         log.error("Exception getting group chats: e=" + var7, var7);
         throw new FusionException(var7.getMessage());
      }
   }

   public void purgeGroupChat(String id) {
      if (log.isDebugEnabled()) {
         log.debug("Purging the GroupChat object '" + id + "'");
      }

      GroupChatRpcI groupChat = (GroupChatRpcI)this.groupChatObjects.remove(id);
      if (groupChat == null) {
         log.warn("Unable to purge the GroupChat object '" + id + "': The group chat was not found in the cache");
      } else {
         try {
            this.getRegistryPrx().deregisterGroupChatObject(id);
         } catch (LocalException var5) {
            log.warn("Unable to deregister the GroupChat object '" + id + "': Unable to communicate with the Registry");
         }

         try {
            this.getCacheAdapter().remove(Util.stringToIdentity("G" + id));
         } catch (NotRegisteredException var4) {
            log.warn("Caught Ice.NotRegisteredException when attempting to remove the GroupChat object '" + id + "' from the cache adapter");
         }

         if (!MessageSwitchboardDispatcher.getInstance().isFeatureEnabled()) {
            groupChat.removeAllParticipants();
         }

      }
   }

   public final SessionPrx findSessionPrx(String sessionID) {
      return this.makeSessionPrx(sessionID);
   }

   public final SessionPrx makeSessionPrx(String sessionID) {
      return SessionPrxHelper.uncheckedCast(this.applicationContext.getCacheAdapter().createProxy(this.sessionIdentity(sessionID)));
   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout) {
      Iterator i$ = this.userObjects.values().iterator();

      while(i$.hasNext()) {
         UserRpcI user = (UserRpcI)i$.next();

         try {
            user.putAlertMessage(message, title, timeout);
         } catch (Exception var7) {
         }
      }

   }

   private GroupChatPrx restoreGroupChatObject(String id, String creator, String[] participants) throws FusionException, ObjectExistsException {
      if (log.isDebugEnabled()) {
         log.debug("Restoring group chat id=" + id + ", creator=" + creator);
      }

      ChatObjectManager.GroupChatPair groupChatPair = this.createGroupChatObjectCore(id, creator);
      String[] arr$ = participants;
      int len$ = participants.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String participant = arr$[i$];

         try {
            if (log.isDebugEnabled()) {
               log.debug("Restoring group chat id=" + id + ": adding participant=" + participant);
            }

            groupChatPair.local.addUserToGroupChat(participant, participant.equals(creator), false);
         } catch (Exception var10) {
            log.warn("restoreGroupChatObject: failed to add user=" + participant + " to restored group chat=" + id + ": ex=" + var10, var10);
         }
      }

      groupChatPair.local.setCreatorParticipant(creator);
      if (log.isDebugEnabled()) {
         log.debug("Restored groupchat with id=" + id + " prx=" + groupChatPair.remote);
      }

      return groupChatPair.remote;
   }

   public Properties getProperties() {
      return this.applicationContext.getProperties();
   }

   public RequestCounter getRequestCounter() {
      return this.metrics.getRequestCounter();
   }

   public ChatSession createSession(ChatUser chatUser, String sessionID, int presence, int deviceType, int connectionType, int imType, int port, int remotePort, String remoteAddress, String mobileDevice, String userAgent, short clientVersion, String language, ConnectionPrx connectionProxy) {
      return ChatSession.create(this, chatUser, sessionID, presence, deviceType, connectionType, imType, port, remotePort, remoteAddress, mobileDevice, userAgent, clientVersion, language, connectionProxy);
   }

   public AuthenticationServicePrx getAuthenticationServiceProxy() {
      return this.authenticationServiceProxy;
   }

   public IcePrxFinder getIcePrxFinder() {
      return this.icePrxFinder;
   }

   public void removeSession(String sessionID, boolean userIsBeingPurged) {
      SessionRpcI session = (SessionRpcI)this.sessionObjects.remove(sessionID);
      if (session == null) {
         log.warn("ChatSession " + sessionID + " is not held by this ObjectCache");
      } else {
         this.getCacheAdapter().remove(this.sessionIdentity(sessionID));
      }
   }

   public SessionPrx onSessionCreated(ChatSession session) {
      String sessionID = session.getSessionID();
      SessionRpcI newSession = new SessionRpcI(this, session);
      ObjectPrx basePrx = this.addToCacheAdapter(newSession, this.sessionIdentity(sessionID));
      SessionPrx sessionPrx = SessionPrxHelper.uncheckedCast(basePrx);
      this.sessionObjects.put(sessionID, newSession);
      this.metrics.sessionAdded();
      this.updateUserState(session.user);
      return sessionPrx;
   }

   public void onSessionRemoved(ChatUser user) {
      this.updateUserState(user);
      this.metrics.sessionRemoved();
   }

   public void removeUser(String username) {
      this.purgeUser(username);
   }

   public SessionPrx getSessionPrx(String sessionID) {
      return SessionPrxHelper.uncheckedCast(this.applicationContext.getCacheAdapter().createProxy(this.sessionIdentity(sessionID)));
   }

   public SessionPrx[] findSessionsPrx(String[] sessionIDs) {
      ArrayList<SessionPrx> sessionProxies = new ArrayList();
      String[] arr$ = sessionIDs;
      int len$ = sessionIDs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String sessionID = arr$[i$];
         SessionPrx sessionPrx = SessionPrxHelper.uncheckedCast(this.getCacheAdapter().createProxy(this.sessionIdentity(sessionID)));
         sessionProxies.add(sessionPrx);
      }

      return (SessionPrx[])sessionProxies.toArray(new SessionPrx[0]);
   }

   public Credential[] getUserCredentials(int userID, byte[] types) throws FusionException {
      return this.authenticationServiceProxy.getCredentialsForTypes(userID, types);
   }

   private void updateUserState(ChatUser user) {
   }

   public MessageSwitchboardPrx getMessageSwitchboardPrx() throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
         MessageSwitchboardPrx msp = this.applicationContext.getRegistryPrx().getMessageSwitchboard();
         return msp;
      } else {
         if (this.localMessageSwitchboardPrx.get() == null) {
            synchronized(this) {
               if (this.localMessageSwitchboardPrx.get() == null) {
                  MessageSwitchboardI msi = new MessageSwitchboardI();
                  ObjectPrx basePrx = this.addToCacheAdapter(msi, Util.stringToIdentity("msp_" + this.iceID));
                  MessageSwitchboardPrx msp = MessageSwitchboardPrxHelper.uncheckedCast(basePrx);
                  this.localMessageSwitchboardPrx.set(msp);
               }
            }
         }

         return (MessageSwitchboardPrx)this.localMessageSwitchboardPrx.get();
      }
   }

   public GroupChatPrx findGroupChatPrxFromRegistry(String groupChatID) throws ObjectNotFoundException {
      return this.applicationContext.getRegistryPrx().findGroupChatObject(groupChatID);
   }

   public ChatRoomPrx findChatRoomPrxFromRegistry(String destination) throws ObjectNotFoundException {
      return this.applicationContext.getRegistryPrx().findChatRoomObject(destination);
   }

   public void onGroupSessionAdded() {
      this.metrics.groupSessionAdded();
   }

   public void onGroupSessionRemoved() {
      this.metrics.groupSessionRemoved();
   }

   public void onRoomSessionAdded() {
      this.metrics.roomSessionAdded();
   }

   public void onRoomSessionRemoved() {
      this.metrics.roomSessionRemoved();
   }

   public void logMessage(MessageToLog.TypeEnum logType, int localUserCountryID, String source, String destination, int numRecipients, String messageText) {
      this.applicationContext.getMessageLoggerPrx().logMessage(logType.value(), localUserCountryID, source, destination, numRecipients, messageText);
   }

   public boolean isLogMessagesToFile() {
      return ObjectCache.logMessagesToFile;
   }

   public boolean isLogMessagesToDB() {
      return ObjectCache.logMessagesToDB;
   }

   public long getChatRoomIdleTimeout() {
      return (long)this.idleRoomPurger.getIdleTimeout();
   }

   public Semaphore getGiftAllSemaphore() {
      return this.giftAllSemaphore;
   }

   public ObjectCacheContext getApplicationContext() {
      return this.applicationContext;
   }

   public ChatContentStore getFileStore() {
      return this.chatContentStore;
   }

   private class IdleGroupChatPurger extends ChatObjectManager.ObjectPurger {
      public IdleGroupChatPurger(Properties properties) {
         super(null);
         int idleTimeout = properties.getPropertyAsIntWithDefault("GroupChatIdleTimeout", 1800) * 1000;
         int purgeInterval = properties.getPropertyAsIntWithDefault("GroupChatPurgerInterval", 60) * 1000;
         this.start(idleTimeout, purgeInterval);
      }

      public void run() {
         if (ChatObjectManager.log.isDebugEnabled()) {
            ChatObjectManager.log.debug("Running IdleAndRemovedGroupChatPurger, examining " + ChatObjectManager.this.groupChatObjects.size() + " group chat objects");
         }

         try {
            List<String> groupChatIdList = new LinkedList(ChatObjectManager.this.groupChatObjects.keySet());
            Iterator i$ = groupChatIdList.iterator();

            while(i$.hasNext()) {
               String groupChatId = (String)i$.next();
               GroupChatRpcI groupChat = (GroupChatRpcI)ChatObjectManager.this.groupChatObjects.get(groupChatId);
               if (groupChat != null) {
                  if (groupChat.isMarkedForRemoval()) {
                     if (ChatObjectManager.log.isDebugEnabled()) {
                        ChatObjectManager.log.debug("purging group chat [" + groupChatId + "] marked for removal");
                     }

                     ChatObjectManager.this.purgeGroupChat(groupChatId);
                  } else if (this.idleTimeout != 0 && groupChat.getTimeLastMessageSent() < System.currentTimeMillis() - (long)this.idleTimeout) {
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PersistentGroupChatSettings.GROUP_CHAT_IDLE_NOTIFICATION_ENABLED)) {
                        groupChat.sendAdminMessageToParticipants(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.PersistentGroupChatSettings.GROUP_CHAT_IDLE_TIMEOUT_MESSAGE), (String)null);
                     }

                     if (ChatObjectManager.log.isDebugEnabled()) {
                        ChatObjectManager.log.debug("purging timed out group chat [" + groupChatId + "]");
                     }

                     ChatObjectManager.this.purgeGroupChat(groupChatId);
                  }
               }
            }

            groupChatIdList.clear();
         } catch (Exception var5) {
            ChatObjectManager.log.error("Failed to purge group chat objects", var5);
         }

      }
   }

   private class IdleRoomPurger extends ChatObjectManager.ObjectPurger {
      public IdleRoomPurger(Properties properties) {
         super(null);
         int idleTimeout = properties.getPropertyAsIntWithDefault("ChatRoomIdleTimeout", 1800) * 1000;
         int purgeInterval = properties.getPropertyAsIntWithDefault("ChatRoomPurgerInterval", 60) * 1000;
         this.start(idleTimeout, purgeInterval);
      }

      public void run() {
         if (ChatObjectManager.log.isDebugEnabled()) {
            ChatObjectManager.log.debug("Running IdleChatRoomPurger, examining " + ChatObjectManager.this.chatRoomObjects.size() + " chat room objects");
         }

         try {
            Iterator i$ = ChatObjectManager.this.chatRoomObjects.keySet().iterator();

            while(i$.hasNext()) {
               String chatRoomName = (String)i$.next();
               ChatRoomRpcI chatRoom = (ChatRoomRpcI)ChatObjectManager.this.chatRoomObjects.get(chatRoomName);
               if (chatRoom != null && chatRoom.isIdle()) {
                  if (ChatObjectManager.log.isDebugEnabled()) {
                     ChatObjectManager.log.debug("Running IdleChatRoomPurger, purging:" + chatRoomName);
                  }

                  chatRoom.prepareForPurge();
                  ChatObjectManager.this.purgeRoom(chatRoomName);
               }
            }
         } catch (Exception var4) {
            ChatObjectManager.log.error("Failed to purge chat rooms", var4);
         }

      }
   }

   private class IdleSessionPurger extends ChatObjectManager.ObjectPurger {
      public IdleSessionPurger(Properties properties) {
         super(null);
         int idleTimeout = properties.getPropertyAsIntWithDefault("SessionIdleTimeout", 3600) * 1000;
         int purgeInterval = properties.getPropertyAsIntWithDefault("IdleSessionPurgeInterval", 600) * 1000;
         this.start(idleTimeout, purgeInterval);
      }

      public void run() {
         UserRpcI user;
         for(Iterator i$ = ChatObjectManager.this.userObjects.values().iterator(); i$.hasNext(); user.purgeExpiredSessions()) {
            user = (UserRpcI)i$.next();
            if (ChatObjectManager.log.isDebugEnabled()) {
               ChatObjectManager.log.debug("Purging expired sessions for the user '" + user.getUserData().username + "'");
            }
         }

      }
   }

   private abstract class ObjectPurger extends TimerTask {
      protected int idleTimeout;
      protected int purgeInterval;

      private ObjectPurger() {
      }

      public int getIdleTimeout() {
         return this.idleTimeout;
      }

      public int getPurgeInterval() {
         return this.purgeInterval;
      }

      protected void start(int idleTimeout, int purgeInterval) {
         this.idleTimeout = idleTimeout;
         this.purgeInterval = purgeInterval;
         Timer timer = new Timer(true);
         timer.schedule(this, (long)idleTimeout, (long)purgeInterval);
      }

      // $FF: synthetic method
      ObjectPurger(Object x1) {
         this();
      }
   }

   private class ObjectMetrics {
      private int maxUserObjects = 0;
      private int maxChatRoomObjects = 0;
      private int maxGroupChatObjects = 0;
      private int maxSessionObjects = 0;
      private AtomicInteger numSessionObjects = new AtomicInteger(0);
      private AtomicInteger numSessionsInChatrooms = new AtomicInteger(0);
      private AtomicInteger numSessionsInGroupChats = new AtomicInteger(0);
      private RequestCounter requestCounter;

      public ObjectMetrics(long requestCounterInterval) {
         this.requestCounter = new RequestCounter(requestCounterInterval);
      }

      public void userAdded(int size) {
         if (size > this.maxUserObjects) {
            this.maxUserObjects = size;
         }

      }

      public void groupAdded(int size) {
         if (size > this.maxGroupChatObjects) {
            this.maxGroupChatObjects = size;
         }

      }

      public void sessionAdded() {
         this.maxSessionObjects = Math.max(this.numSessionObjects.incrementAndGet(), this.maxSessionObjects);
      }

      public void sessionRemoved() {
         this.numSessionObjects.decrementAndGet();
      }

      public void request() {
         this.requestCounter.add();
      }

      public RequestCounter getRequestCounter() {
         return this.requestCounter;
      }

      public void groupSessionRemoved() {
         this.numSessionsInGroupChats.decrementAndGet();
      }

      public void groupSessionAdded() {
         this.numSessionsInGroupChats.incrementAndGet();
      }

      public void roomSessionRemoved() {
         this.numSessionsInChatrooms.decrementAndGet();
      }

      public void roomSessionAdded() {
         this.numSessionsInChatrooms.incrementAndGet();
      }

      public void getStats(ObjectCacheStats stats) {
         stats.numUserObjects = ChatObjectManager.this.userObjects.size();
         stats.maxUserObjects = this.maxUserObjects;
         stats.numOnlineUserObjects = stats.numUserObjects;
         stats.maxOnlineUserObjects = stats.maxUserObjects;
         stats.eldestUserObject = 0L;
         stats.numSessionObjects = this.numSessionObjects.get();
         stats.numSessionsInGroupChats = this.numSessionsInGroupChats.get();
         stats.numSessionsInChatrooms = this.numSessionsInChatrooms.get();
         stats.maxSessionObjects = this.maxSessionObjects;
         stats.numChatRoomObjects = ChatObjectManager.this.chatRoomObjects.size();
         if (stats.numChatRoomObjects > this.maxChatRoomObjects) {
            this.maxChatRoomObjects = stats.numChatRoomObjects;
         }

         stats.maxChatRoomObjects = this.maxChatRoomObjects;
         stats.numGroupChatObjects = ChatObjectManager.this.groupChatObjects.size();
         stats.maxGroupChatObjects = this.maxGroupChatObjects;
         stats.distributionServiceQueueSize = ChatObjectManager.this.getDistributionServiceQueueSize();
         stats.stadiumDistributionServiceQueueSize = 0;
         stats.requestsPerSecond = this.requestCounter.getRequestsPerSecond();
         stats.maxRequestsPerSecond = this.requestCounter.getMaxRequestsPerSecond();
         stats.weightage = ChatObjectManager.this.getLoadWeightage();
      }
   }

   private class GroupChatPair {
      public GroupChatPrx remote;
      public GroupChatRpcI local;

      private GroupChatPair() {
      }

      // $FF: synthetic method
      GroupChatPair(Object x1) {
         this();
      }
   }
}
