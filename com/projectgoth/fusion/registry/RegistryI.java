package com.projectgoth.fusion.registry;

import Ice.Current;
import Ice.LocalException;
import Ice.UserException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.AMD_Registry_getMessageSwitchboard;
import com.projectgoth.fusion.slice.AMI_ObjectCache_getMessageSwitchboard;
import com.projectgoth.fusion.slice.BotServiceAdminPrx;
import com.projectgoth.fusion.slice.BotServicePrx;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardAdminPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.ObjectCacheAdminPrx;
import com.projectgoth.fusion.slice.ObjectCachePrx;
import com.projectgoth.fusion.slice.ObjectCachePrxHelper;
import com.projectgoth.fusion.slice.ObjectCacheStats;
import com.projectgoth.fusion.slice.ObjectExistsException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryStats;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._RegistryDisp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class RegistryI extends _RegistryDisp {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RegistryI.class));
   private int iceInvalidProxyPurgerTimeout;
   private int iceObjectCachePingerTimeout;
   public ConcurrentHashMap<String, ObjectCacheRef> objectCacheRefs;
   public ConcurrentHashMap<String, BotServiceRef> botServiceRefs;
   public ConcurrentHashMap<String, MessageSwitchboardRef> messageSwitchboardRefs;
   private RegistryI.InvalidProxyPurger purger;
   public RequestCounter requestCounter;
   private AtomicInteger gatewayID;
   private RegistryContext applicationContext;
   private final RegistryI.UserMap userMap = new RegistryI.UserMap();
   private final RegistryI.ConnectionMap connectionMap = new RegistryI.ConnectionMap();
   private final RegistryI.RoomMap roomMap = new RegistryI.RoomMap();
   private final RegistryI.GroupMap groupMap = new RegistryI.GroupMap();

   public RegistryI(RegistryContext appContext) {
      this.applicationContext = appContext;
      this.objectCacheRefs = new ConcurrentHashMap();
      this.botServiceRefs = new ConcurrentHashMap();
      this.messageSwitchboardRefs = new ConcurrentHashMap();
      this.gatewayID = new AtomicInteger();
      this.purger = new RegistryI.InvalidProxyPurger();
      this.purger.start();
      Timer timer = new Timer(true);
      int objectCachePingInterval = this.applicationContext.getProperties().getPropertyAsIntWithDefault("ObjectCachePingInterval", 1) * 1000;
      this.iceInvalidProxyPurgerTimeout = this.applicationContext.getProperties().getPropertyAsIntWithDefault("IceInvalidProxyPurgerTimeout", 35000);
      this.iceObjectCachePingerTimeout = this.applicationContext.getProperties().getPropertyAsIntWithDefault("IceObjectCachePingerTimeout", 500);
      RegistryI.ObjectCachePinger pinger = new RegistryI.ObjectCachePinger();
      timer.schedule(pinger, (long)objectCachePingInterval, (long)objectCachePingInterval);
      int botServicePingInterval = this.applicationContext.getProperties().getPropertyAsIntWithDefault("BotServicePingInterval", 1) * 1000;
      RegistryI.BotServicePinger botServicePinger = new RegistryI.BotServicePinger();
      timer.schedule(botServicePinger, (long)botServicePingInterval, (long)botServicePingInterval);
      this.requestCounter = new RequestCounter((long)this.applicationContext.getProperties().getPropertyAsIntWithDefault("RequestCounterInterval", 10));
   }

   public UserPrx findUserObject(String username, Current __current) throws ObjectNotFoundException {
      this.requestCounter.add();
      UserPrx userProxy = (UserPrx)this.userMap.find(username);
      if (userProxy == null) {
         if (log.isDebugEnabled()) {
            log.debug("Unable to return a reference to the User object '" + username + "'");
         }

         throw new ObjectNotFoundException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Returning a reference to the User object '" + username + "'");
         }

         return userProxy;
      }
   }

   public void registerObjectCacheStats(String objectCacheUniqueID, ObjectCacheStats stats, Current __current) throws ObjectNotFoundException {
      this.requestCounter.add();
      if (this.objectCacheRefs.containsKey(objectCacheUniqueID)) {
         ((ObjectCacheRef)this.objectCacheRefs.get(objectCacheUniqueID)).setStats(stats);
         if (log.isDebugEnabled()) {
            log.debug("received statistics from " + objectCacheUniqueID);
         }

         if (__current != null) {
            this.applicationContext.getRegistryNode().registerObjectCacheStatsWithOtherRegistries(objectCacheUniqueID, stats);
         }

      } else {
         throw new ObjectNotFoundException(objectCacheUniqueID + " is not currently registered");
      }
   }

   public void registerUserObject(String username, UserPrx userProxy, String objectCacheUniqueID, Current __current) throws ObjectExistsException {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("Registering the User object '" + username + "'");
      }

      if (userProxy == null) {
         log.warn("Registering the User object '" + username + "' failed: null");
      } else {
         UserPrx existingProxy = (UserPrx)this.userMap.find(username);
         if (existingProxy != null) {
            log.warn("The User object '" + username + "' is already registered");
         }

         this.userMap.register(username, userProxy);
         if (__current != null) {
            this.applicationContext.getRegistryNode().registerUserObjectWithOtherRegistries(username, userProxy, objectCacheUniqueID);
         }

      }
   }

   public void deregisterUserObject(String username, String objectCacheUniqueID, Current __current) {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("Deregistering the User object '" + username + "'");
      }

      UserPrx oldValue = (UserPrx)this.userMap.deregister(username);
      if (oldValue == null && log.isDebugEnabled()) {
         log.debug("The User object '" + username + "' was not found to deregister");
      }

      if (__current != null) {
         this.applicationContext.getRegistryNode().deregisterUserObjectFromOtherRegistries(username, objectCacheUniqueID);
      }

   }

   public UserPrx[] findUserObjects(String[] usernames, Current __current) {
      this.requestCounter.add();
      LinkedList<UserPrx> userProxiesToReturn = new LinkedList();
      String[] arr$ = usernames;
      int len$ = usernames.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String username = arr$[i$];
         UserPrx userProxy = (UserPrx)this.userMap.find(username);
         if (userProxy != null) {
            userProxiesToReturn.add(userProxy);
         }
      }

      return (UserPrx[])userProxiesToReturn.toArray(new UserPrx[userProxiesToReturn.size()]);
   }

   public Map<String, UserPrx> findUserObjectsMap(String[] usernames, Current __current) {
      this.requestCounter.add();
      Map<String, UserPrx> result = new HashMap();
      String[] arr$ = usernames;
      int len$ = usernames.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String username = arr$[i$];
         UserPrx userProxy = (UserPrx)this.userMap.find(username);
         if (userProxy != null) {
            result.put(username, userProxy);
         }
      }

      return result;
   }

   private int findNumberOfOnlineRegisteredObjectCaches() {
      int count = 0;
      Iterator i$ = this.objectCacheRefs.values().iterator();

      while(i$.hasNext()) {
         ObjectCacheRef objectCacheRef = (ObjectCacheRef)i$.next();
         if (objectCacheRef.isOnline()) {
            ++count;
         }
      }

      return count;
   }

   public ObjectCachePrx getLowestLoadedObjectCache(Current __current) throws ObjectNotFoundException {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("number of online object caches: " + this.findNumberOfOnlineRegisteredObjectCaches());
      }

      ObjectCacheRef lowestLoadedObjectCacheRef = ObjectCacheLoadBalancer.getLowestLoaded(this.objectCacheRefs.values());
      if (lowestLoadedObjectCacheRef == null) {
         log.warn("Failed to find a lowest loaded object cache server");
         throw new ObjectNotFoundException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("returning lowest loaded object cache: " + lowestLoadedObjectCacheRef.getCacheProxy());
         }

         return lowestLoadedObjectCacheRef.getCacheProxy();
      }
   }

   public void registerObjectCache(String uniqueID, ObjectCachePrx cacheProxy, ObjectCacheAdminPrx adminProxy, Current __current) {
      this.requestCounter.add();
      if (this.objectCacheRefs.get(uniqueID) == null) {
         log.info("New ObjectCache [" + cacheProxy + "] registered");
      } else {
         log.info("Connection to [" + cacheProxy + "] restored");
      }

      try {
         cacheProxy.ice_ping();
      } catch (LocalException var6) {
         log.error("Failed to ping ObjectCache [" + cacheProxy + "] that's attempting to register, ignoring request");
         throw var6;
      }

      this.objectCacheRefs.put(uniqueID, new ObjectCacheRef(uniqueID, cacheProxy, adminProxy));
      if (__current != null) {
         this.applicationContext.getRegistryNode().registerObjectCacheWithOtherRegistries(uniqueID, cacheProxy, adminProxy);
      }

   }

   public void deregisterObjectCache(String uniqueID, Current __current) {
      this.requestCounter.add();
      this.objectCacheRefs.remove(uniqueID);
      log.info("Object Cache " + uniqueID + " was deregistered");
      if (__current != null) {
         this.applicationContext.getRegistryNode().deregisterObjectCacheFromOtherRegistries(uniqueID);
      }

   }

   public ConnectionPrx findConnectionObject(String sessionID, Current __current) throws ObjectNotFoundException {
      this.requestCounter.add();
      ConnectionPrx connectionProxy = (ConnectionPrx)this.connectionMap.find(sessionID);
      if (connectionProxy == null) {
         if (log.isDebugEnabled()) {
            log.debug("Unable to return a reference to the Connection object '" + sessionID + "'");
         }

         throw new ObjectNotFoundException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Returning a reference to the Connection object '" + sessionID + "'");
         }

         return connectionProxy;
      }
   }

   public void registerConnectionObject(String sessionID, ConnectionPrx connectionProxy, Current __current) throws ObjectExistsException {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("Registering the Connection object '" + sessionID + "'");
      }

      ConnectionPrx existingProxy = (ConnectionPrx)this.connectionMap.find(sessionID);
      if (existingProxy != null) {
         log.error("Registry " + this.applicationContext.getHostName() + ": Received request to register the Connection object '" + sessionID + "' that is already registered");
         throw new ObjectExistsException();
      } else {
         this.connectionMap.register(sessionID, connectionProxy);
         if (__current != null) {
            this.applicationContext.getRegistryNode().registerConnectionObjectWithOtherRegistries(sessionID, connectionProxy);
         }

      }
   }

   public void deregisterConnectionObject(String sessionID, Current __current) {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("Deregistering the Connection object '" + sessionID + "'");
      }

      ConnectionPrx oldValue = (ConnectionPrx)this.connectionMap.deregister(sessionID);
      if (oldValue == null && log.isDebugEnabled()) {
         log.debug("The Connection object '" + sessionID + "' was not found to deregister");
      }

      if (__current != null) {
         this.applicationContext.getRegistryNode().deregisterConnectionObjectFromOtherRegistries(sessionID);
      }

   }

   public ChatRoomPrx findChatRoomObject(String name, Current __current) throws ObjectNotFoundException {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("finding chatroom [" + name + "]");
      }

      if (!StringUtils.hasLength(name)) {
         throw new ObjectNotFoundException();
      } else {
         name = name.toLowerCase();
         ChatRoomPrx chatRoomProxy = (ChatRoomPrx)this.roomMap.find(name);
         if (chatRoomProxy == null) {
            throw new ObjectNotFoundException();
         } else {
            return chatRoomProxy;
         }
      }
   }

   public ChatRoomPrx[] findChatRoomObjects(String[] chatRoomNames, Current __current) {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("finding chatrooms [" + StringUtil.asString(chatRoomNames) + "]");
      }

      LinkedList<ChatRoomPrx> chatRoomProxiesToReturn = new LinkedList();
      String[] arr$ = chatRoomNames;
      int len$ = chatRoomNames.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String chatRoomName = arr$[i$];
         ChatRoomPrx chatRoomProxy = (ChatRoomPrx)this.roomMap.find(chatRoomName.toLowerCase());
         chatRoomProxiesToReturn.add(chatRoomProxy);
      }

      return (ChatRoomPrx[])chatRoomProxiesToReturn.toArray(new ChatRoomPrx[chatRoomProxiesToReturn.size()]);
   }

   public void registerChatRoomObject(String name, ChatRoomPrx chatRoomProxy, Current __current) throws ObjectExistsException {
      this.requestCounter.add();
      name = name.toLowerCase();
      if (log.isDebugEnabled()) {
         log.debug("Registering the ChatRoom object '" + name + "'");
      }

      ChatRoomPrx existingProxy = (ChatRoomPrx)this.roomMap.find(name);
      if (existingProxy != null) {
         log.warn("Registry " + this.applicationContext.getHostName() + ": Received request to register the Chat Room object '" + name + "' that is already registered");
         throw new ObjectExistsException();
      } else {
         this.roomMap.register(name, chatRoomProxy);
         if (__current != null) {
            this.applicationContext.getRegistryNode().registerChatRoomObjectWithOtherRegistries(name, chatRoomProxy);
         }

      }
   }

   public void deregisterChatRoomObject(String name, Current __current) {
      this.requestCounter.add();
      name = name.toLowerCase();
      if (log.isDebugEnabled()) {
         log.debug("Deregistering the ChatRoom object '" + name + "'");
      }

      ChatRoomPrx oldValue = (ChatRoomPrx)this.roomMap.deregister(name);
      if (oldValue == null && log.isDebugEnabled()) {
         log.debug("The ChatRoom object '" + name + "' was not found to deregister");
      }

      if (__current != null) {
         this.applicationContext.getRegistryNode().deregisterChatRoomObjectFromOtherRegistries(name);
      }

   }

   public GroupChatPrx findGroupChatObject(String id, Current __current) throws ObjectNotFoundException {
      this.requestCounter.add();
      GroupChatPrx groupChatProxy = (GroupChatPrx)this.groupMap.find(id);
      if (groupChatProxy == null) {
         if (log.isDebugEnabled()) {
            log.debug("Unable to return a reference to the GroupChat object '" + id + "'");
         }

         throw new ObjectNotFoundException();
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Returning a reference to the GroupChat object '" + id + "'");
         }

         return groupChatProxy;
      }
   }

   public void registerGroupChatObject(String id, GroupChatPrx groupChatProxy, Current __current) {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("Registering the GroupChat object '" + id + "'");
      }

      GroupChatPrx existingProxy = (GroupChatPrx)this.groupMap.find(id);
      if (existingProxy != null) {
         log.warn("The GroupChat object '" + id + "' is already registered. The old reference will be discarded.");
      }

      this.groupMap.register(id, groupChatProxy);
      if (__current != null) {
         this.applicationContext.getRegistryNode().registerGroupChatObjectWithOtherRegistries(id, groupChatProxy);
      }

   }

   public void deregisterGroupChatObject(String id, Current __current) {
      this.requestCounter.add();
      if (log.isDebugEnabled()) {
         log.debug("Deregistering the GroupChat object '" + id + "'");
      }

      GroupChatPrx oldValue = (GroupChatPrx)this.groupMap.deregister(id);
      if (oldValue == null && log.isDebugEnabled()) {
         log.debug("The GroupChat object '" + id + "' was not found to deregister");
      }

      if (__current != null) {
         this.applicationContext.getRegistryNode().deregisterGroupChatObjectFromOtherRegistries(id);
      }

   }

   public BotServicePrx getLowestLoadedBotService(Current __current) throws ObjectNotFoundException {
      this.requestCounter.add();
      BotServiceRef lowestLoadedBotServiceRef = null;
      Iterator i$ = this.botServiceRefs.values().iterator();

      while(true) {
         BotServiceRef botServiceRef;
         do {
            do {
               if (!i$.hasNext()) {
                  if (lowestLoadedBotServiceRef == null) {
                     log.warn("Failed to find a lowest loaded BotService server");
                     throw new ObjectNotFoundException();
                  }

                  return lowestLoadedBotServiceRef.getServiceProxy();
               }

               botServiceRef = (BotServiceRef)i$.next();
            } while(!botServiceRef.isOnline());
         } while(lowestLoadedBotServiceRef != null && botServiceRef.getLoad() >= lowestLoadedBotServiceRef.getLoad());

         lowestLoadedBotServiceRef = botServiceRef;
      }
   }

   public void registerBotService(String hostName, int load, BotServicePrx serviceProxy, BotServiceAdminPrx adminProxy, Current __current) {
      this.requestCounter.add();

      try {
         serviceProxy.ice_ping();
      } catch (LocalException var7) {
         log.error("Failed to ping BotService [" + serviceProxy + "] that's attempting to register, ignoring request");
         throw var7;
      }

      BotServiceRef oldValue = (BotServiceRef)this.botServiceRefs.put(hostName, new BotServiceRef(hostName, load, serviceProxy, adminProxy));
      if (oldValue == null) {
         log.info("New BotService [" + serviceProxy + "] registered");
      } else {
         log.info("Connection to BotService [" + serviceProxy + "] restored");
      }

      if (__current != null) {
         this.applicationContext.getRegistryNode().registerBotServiceWithOtherRegistries(hostName, load, serviceProxy, adminProxy);
      }

   }

   public void deregisterBotService(String hostName, Current __current) {
      this.requestCounter.add();
      this.objectCacheRefs.remove(hostName);
      log.info("Bot Service " + hostName + " was deregistered");
      if (__current != null) {
         this.applicationContext.getRegistryNode().deregisterBotServiceFromOtherRegistries(hostName);
      }

   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout, Current __current) throws FusionException {
      Iterator i$ = this.objectCacheRefs.values().iterator();

      while(i$.hasNext()) {
         ObjectCacheRef ref = (ObjectCacheRef)i$.next();

         try {
            ObjectCachePrx objectCacheProxy = ObjectCachePrxHelper.uncheckedCast(ref.getCacheProxy().ice_timeout(this.iceInvalidProxyPurgerTimeout));
            objectCacheProxy.sendAlertMessageToAllUsers(message, title, timeout);
         } catch (LocalException var8) {
         }
      }

   }

   public int newGatewayID(Current __current) {
      return this.gatewayID.incrementAndGet();
   }

   public int getUserCount(Current __current) {
      return this.userMap.size();
   }

   public RegistryStats getStats(RegistryStats stats) {
      if (stats == null) {
         stats = ServiceStatsFactory.getRegistryStats(this.applicationContext.getHostName(), Registry.startTime);
      }

      stats.numUserProxies = this.userMap.size();
      stats.maxUserProxies = this.userMap.getMaxProxies();
      stats.numConnectionProxies = this.connectionMap.size();
      stats.maxConnectionProxies = this.connectionMap.getMaxProxies();
      stats.numChatRoomProxies = this.roomMap.size();
      stats.maxChatRoomProxies = this.roomMap.getMaxProxies();
      stats.numGroupChatProxies = this.groupMap.size();
      stats.maxGroupChatProxies = this.groupMap.getMaxProxies();
      stats.objectCaches = "";
      Iterator i = this.objectCacheRefs.values().iterator();

      while(i.hasNext()) {
         ObjectCacheRef ref = (ObjectCacheRef)i.next();
         stats.objectCaches = stats.objectCaches + ref.getHostName();
         if (ref.isOnline()) {
            stats.objectCaches = stats.objectCaches + " (Load: " + ObjectCacheLoadBalancer.getLoad(ref.getStats()) + ")";
         } else {
            stats.objectCaches = stats.objectCaches + " (OFFLINE)";
         }

         if (i.hasNext()) {
            stats.objectCaches = stats.objectCaches + "; ";
         }
      }

      return stats;
   }

   public ConcurrentMap<String, UserPrx> getUserProxies() {
      return this.userMap.getProxies();
   }

   public ConcurrentMap<String, ConnectionPrx> getConnectionProxies() {
      return this.connectionMap.getProxies();
   }

   public ConcurrentMap<String, ChatRoomPrx> getChatRoomProxies() {
      return this.roomMap.getProxies();
   }

   public void registerMessageSwitchboard(String uniqueID, MessageSwitchboardPrx msbProxy, MessageSwitchboardAdminPrx adminProxy, Current __current) {
      this.requestCounter.add();
      if (this.messageSwitchboardRefs.get(uniqueID) == null) {
         log.info("New MessageSwitchboard [" + msbProxy + "] registered");
      } else {
         log.info("Connection to [" + msbProxy + "] restored");
      }

      try {
         msbProxy.ice_ping();
      } catch (LocalException var6) {
         log.error("Failed to ping MessageSwitchboard [" + msbProxy + "] that's attempting to register, ignoring request");
         throw var6;
      }

      this.messageSwitchboardRefs.put(uniqueID, new MessageSwitchboardRef(uniqueID, msbProxy, adminProxy));
      if (__current != null) {
         this.applicationContext.getRegistryNode().registerMessageSwitchboardWithOtherRegistries(uniqueID, msbProxy, adminProxy);
      }

   }

   public void deregisterMessageSwitchboard(String uniqueID, Current __current) {
      this.requestCounter.add();
      this.messageSwitchboardRefs.remove(uniqueID);
      log.info("MessageSwitchboard " + uniqueID + " was deregistered");
      if (__current != null) {
         this.applicationContext.getRegistryNode().deregisterMessageSwitchboardFromOtherRegistries(uniqueID);
      }

   }

   public void getMessageSwitchboard_async(final AMD_Registry_getMessageSwitchboard cb, Current __current) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
         int random = (new Random(System.currentTimeMillis())).nextInt(this.messageSwitchboardRefs.size());
         Collection<MessageSwitchboardRef> values = this.messageSwitchboardRefs.values();
         MessageSwitchboardRef[] arr = (MessageSwitchboardRef[])values.toArray(new MessageSwitchboardRef[values.size()]);
         cb.ice_response(arr[random].getProxy());
      } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RegistrySettings.USE_AMD_AMI_GET_MESSAGE_SWITCHBOARD)) {
         try {
            RegistryThreadPoolExecutor.schedule(new Runnable() {
               public void run() {
                  try {
                     RegistryI.this.getLowestLoadedObjectCache().getMessageSwitchboard_async(new AMI_ObjectCache_getMessageSwitchboard() {
                        public void ice_response(MessageSwitchboardPrx result) {
                           cb.ice_response(result);
                        }

                        public void ice_exception(UserException ex) {
                           cb.ice_exception(ex);
                        }

                        public void ice_exception(LocalException ex) {
                           cb.ice_exception(ex);
                        }
                     });
                  } catch (FusionException var2) {
                     cb.ice_exception(var2);
                  }

               }
            });
         } catch (Exception var6) {
            log.error("Exception while scheduling getMessageSwitchboard task: " + var6.getMessage(), var6);
            cb.ice_exception(var6);
         }
      } else {
         cb.ice_response(this.getLowestLoadedObjectCache().getMessageSwitchboard());
      }

   }

   private class BotServicePinger extends TimerTask {
      private BotServicePinger() {
      }

      public void run() {
         Iterator i$ = RegistryI.this.botServiceRefs.values().iterator();

         while(i$.hasNext()) {
            BotServiceRef botServiceRef = (BotServiceRef)i$.next();

            try {
               int load = botServiceRef.getAdminProxy().ping();
               botServiceRef.setLoad(load);
               if (!botServiceRef.isOnline()) {
                  RegistryI.log.info("Connection to BotService [" + botServiceRef.getServiceProxy() + "] restored");
                  botServiceRef.setOnline(true);
               }
            } catch (LocalException var4) {
               if (botServiceRef.isOnline()) {
                  RegistryI.log.warn("Have lost connection to BotService [" + botServiceRef.getServiceProxy() + "]", var4);
                  botServiceRef.setOnline(false);
               }
            }
         }

      }

      // $FF: synthetic method
      BotServicePinger(Object x1) {
         this();
      }
   }

   private class ObjectCachePinger extends TimerTask {
      private ObjectCachePinger() {
      }

      public void run() {
         Iterator i$ = RegistryI.this.objectCacheRefs.values().iterator();

         while(i$.hasNext()) {
            ObjectCacheRef objectCacheRef = (ObjectCacheRef)i$.next();

            try {
               objectCacheRef.getAdminProxy().ice_timeout(RegistryI.this.iceObjectCachePingerTimeout).ice_ping();
               if (!objectCacheRef.isOnline()) {
                  RegistryI.log.info("Connection to [" + objectCacheRef.getCacheProxy() + "] restored");
                  objectCacheRef.setOnline(true);
               }
            } catch (LocalException var4) {
               if (objectCacheRef.isOnline()) {
                  RegistryI.log.warn("Have lost connection to [" + objectCacheRef.getCacheProxy() + "]", var4);
                  objectCacheRef.setOnline(false);
               } else if (RegistryI.log.isDebugEnabled()) {
                  RegistryI.log.debug("object cache [" + objectCacheRef.getAdminProxy() + "] is still offline");
               }
            }
         }

      }

      // $FF: synthetic method
      ObjectCachePinger(Object x1) {
         this();
      }
   }

   private class InvalidProxyPurger extends Thread {
      public InvalidProxyPurger() {
         this.setName("InvalidProxyPurger");
      }

      public void run() {
         long purgeInterval = (long)(RegistryI.this.applicationContext.getProperties().getPropertyAsIntWithDefault("InvalidProxyPurgeInterval", 300) * 1000);

         while(true) {
            while(true) {
               try {
                  try {
                     sleep(purgeInterval);
                  } catch (InterruptedException var4) {
                  }

                  if (RegistryI.log.isDebugEnabled()) {
                     RegistryI.log.debug("Purging invalid object references...");
                  }

                  RegistryI.this.userMap.purgeExpired();
                  RegistryI.this.connectionMap.purgeExpired();
                  RegistryI.this.roomMap.purgeExpired();
                  RegistryI.this.groupMap.purgeExpired();
                  if (RegistryI.log.isDebugEnabled()) {
                     RegistryI.log.debug("Purge complete");
                  }
               } catch (Exception var5) {
                  RegistryI.log.error("InvalidProxyPurger thread FAILED", var5);
               }
            }
         }
      }
   }

   private class GroupMap extends ProxyMap<GroupChatPrx> {
      private GroupMap() {
      }

      public void purgeExpired(Set<Entry<String, GroupChatPrx>> entries) {
         Iterator i$ = entries.iterator();

         while(i$.hasNext()) {
            Entry<String, GroupChatPrx> group = (Entry)i$.next();
            String name = (String)group.getKey();

            try {
               GroupChatPrx prx = (GroupChatPrx)group.getValue();
               if (prx != null) {
                  prx.ice_timeout(RegistryI.this.iceInvalidProxyPurgerTimeout).ice_ping();
               }
            } catch (LocalException var6) {
               if (RegistryI.log.isDebugEnabled()) {
                  RegistryI.log.debug("Purging the invalid ChatGroup object proxy '" + name + "'");
               }

               this.remove(name);
               RegistryI.this.applicationContext.getRegistryNode().deregisterGroupChatObjectFromOtherRegistries(name);
            } catch (Exception var7) {
               RegistryI.log.warn("Exception purging the invalid ChatGroup object proxy '" + name + "'", var7);
               this.remove(name);
               RegistryI.this.applicationContext.getRegistryNode().deregisterGroupChatObjectFromOtherRegistries(name);
            }
         }

      }

      // $FF: synthetic method
      GroupMap(Object x1) {
         this();
      }
   }

   private class RoomMap extends ProxyMap<ChatRoomPrx> {
      private RoomMap() {
      }

      public void purgeExpired(Set<Entry<String, ChatRoomPrx>> entries) {
         Iterator i$ = entries.iterator();

         while(i$.hasNext()) {
            Entry<String, ChatRoomPrx> room = (Entry)i$.next();
            String name = (String)room.getKey();

            try {
               ChatRoomPrx prx = (ChatRoomPrx)room.getValue();
               if (prx != null) {
                  prx.ice_timeout(RegistryI.this.iceInvalidProxyPurgerTimeout).ice_ping();
               }
            } catch (LocalException var6) {
               if (RegistryI.log.isDebugEnabled()) {
                  RegistryI.log.debug("Purging the invalid ChatRoom object proxy '" + name + "'");
               }

               this.remove(name);
               RegistryI.this.applicationContext.getRegistryNode().deregisterChatRoomObjectFromOtherRegistries(name);
            } catch (Exception var7) {
               RegistryI.log.warn("Exception purging the invalid ChatRoom object proxy '" + name + "'", var7);
               this.remove(name);
               RegistryI.this.applicationContext.getRegistryNode().deregisterChatRoomObjectFromOtherRegistries(name);
            }
         }

      }

      // $FF: synthetic method
      RoomMap(Object x1) {
         this();
      }
   }

   private class ConnectionMap extends ProxyMap<ConnectionPrx> {
      private ConnectionMap() {
      }

      public void purgeExpired(Set<Entry<String, ConnectionPrx>> entries) {
         Iterator i$ = entries.iterator();

         while(i$.hasNext()) {
            Entry<String, ConnectionPrx> session = (Entry)i$.next();
            String sessionID = (String)session.getKey();

            try {
               ConnectionPrx prx = (ConnectionPrx)session.getValue();
               if (prx != null) {
                  prx.ice_timeout(RegistryI.this.iceInvalidProxyPurgerTimeout).ice_ping();
               }
            } catch (LocalException var6) {
               if (RegistryI.log.isDebugEnabled()) {
                  RegistryI.log.debug("Purging the invalid Connection object proxy '" + sessionID + "'");
               }

               this.remove(sessionID);
               RegistryI.this.applicationContext.getRegistryNode().deregisterConnectionObjectFromOtherRegistries(sessionID);
            } catch (Exception var7) {
               RegistryI.log.warn("Exception purging the invalid Connection object proxy '" + sessionID + "'", var7);
               this.remove(sessionID);
               RegistryI.this.applicationContext.getRegistryNode().deregisterConnectionObjectFromOtherRegistries(sessionID);
            }
         }

      }

      // $FF: synthetic method
      ConnectionMap(Object x1) {
         this();
      }
   }

   private class UserMap extends ProxyMap<UserPrx> {
      private UserMap() {
      }

      public void purgeExpired(Set<Entry<String, UserPrx>> entries) {
         Iterator i$ = entries.iterator();

         while(i$.hasNext()) {
            Entry<String, UserPrx> user = (Entry)i$.next();
            String username = (String)user.getKey();

            try {
               UserPrx prx = (UserPrx)user.getValue();
               if (prx != null) {
                  prx.ice_timeout(RegistryI.this.iceInvalidProxyPurgerTimeout).ice_ping();
               }
            } catch (LocalException var6) {
               if (RegistryI.log.isDebugEnabled()) {
                  RegistryI.log.debug("Purging the invalid User object proxy '" + username + "'");
               }

               this.remove(username);
               RegistryI.this.applicationContext.getRegistryNode().deregisterUserObjectFromOtherRegistries(username, (String)null);
            } catch (Exception var7) {
               RegistryI.log.warn("Exception purging the invalid User object proxy '" + username + "'", var7);
               this.remove(username);
               RegistryI.this.applicationContext.getRegistryNode().deregisterUserObjectFromOtherRegistries(username, (String)null);
            }
         }

      }

      // $FF: synthetic method
      UserMap(Object x1) {
         this();
      }
   }
}
