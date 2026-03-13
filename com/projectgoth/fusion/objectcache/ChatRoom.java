package com.projectgoth.fusion.objectcache;

import Ice.LocalException;
import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.botservice.BotChannelHelper;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.cache.RecentChatRoomList;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.ExceptionHelper;
import com.projectgoth.fusion.common.ExceptionWithErrorCause;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.RateLimiter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.WebCommon;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.emote.ChatRoomWelcomeMessageData;
import com.projectgoth.fusion.emote.EmoteCommandStateStorage;
import com.projectgoth.fusion.emote.GiftAllTask;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Group;
import com.projectgoth.fusion.interfaces.GroupHome;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.paintwars.Painter;
import com.projectgoth.fusion.slice.BotInstance;
import com.projectgoth.fusion.slice.BotServicePrx;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.FusionExceptionWithErrorCauseCode;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatRoom implements ChatSourceRoom, ChatRoomParticipantListener {
   private ChatObjectManagerRoom objectManager;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatRoom.class));
   private static Logger auditLog = Logger.getLogger("ChatroomAudit");
   private static MemCachedClient recentChatRoomMemcache;
   private static ScheduledExecutorService scheduler;
   private final ChatRoomDataWrapper chatRoomData;
   private final ChatRoomParticipants participants;
   private int numFakeParticipants;
   private Map<String, ChatRoom.BannedInfo> bannedUsers = new ConcurrentHashMap();
   private Map<String, Long> recentlyLeftUsers = new ConcurrentHashMap();
   private Map<String, Long> silencedUsers = new ConcurrentHashMap();
   private Map<String, Boolean> mutedUsers = new ConcurrentHashMap();
   private boolean useBoundedQueueForChatRoomMessageDispatch = false;
   private int chatRoomDispatchBoundedQueueMaxSize = Integer.MAX_VALUE;
   private ChatRoom.AbstractBlockingQueue<ChatRoom.MessageToDispatch> messageQueue = null;
   private Semaphore messageSemaphore = new Semaphore(1);
   private Runnable messageDispatcher = new Runnable() {
      public void run() {
         ChatRoom.this.dispatchMessages();
      }
   };
   private Map<String, BotInstance> bots = new ConcurrentHashMap();
   private Semaphore botSemaphore = new Semaphore(1);
   private String botChannelID;
   private ChatRoom.KickUserVote kickUserVote;
   private final Object kickUserVoteMonitor = new Object();
   private final Object adminKickMonitor = new Object();
   private RateLimiter enterLeaveNotificationLimiter;
   private boolean purging;
   private long lastTimeMessageSent = System.currentTimeMillis();
   private int maxMessageLength = 320;
   private int maxStatdiumAdminMessageLength = 500;
   private int maxUserDuration = 28800;
   private int maxMessageRepetitions = 5;
   private int maxEnterLeaveNotifications = 3;
   private int minKickVotesRequired = 3;
   private int minSpamMessageLength = 7;
   private int enterLeaveNotificationInterval = 1;
   private int kickUserVoteDuration = 60;
   private int kickUserVoteUpdateInterval = 20;
   private int timeBetweenKickVotes = 0;
   private int timeBetweenKickingSameUser = 300;
   private int banDuration = 900;
   private int globalAdminBanDuration = 3600;
   private int adminBanDuration = 7200;
   private int grpAdminSuspendDuration = 3600;
   private int grpModSuspendDuration = 3600;
   private int userIdleTimeout = 600;
   private int reenterInterval = 20;
   private long silenceExpiry = 0L;
   private int defaultRoomSilencePeriod = 20;
   private int defaultUserSilencePeriod = 300;
   private int listEntrantBufferSize = 50;
   private int listLockPeriod = 45;
   private int dbUpdateInterval = 1800;
   private long blockBotsUntilTimestamp = 0L;
   private Timer announceTimer;
   ChatroomEntrantSnapshot chatroomEntrantSnapshot;
   private EmoteCommandStateStorage emoteCommandStates;
   private static Pattern MULTI_KICK_PATTERN_CHECK;
   private static String MIMETYPE_DESCRIPTION;
   private static String MIMETYPE_MANAGED_BY;
   private static String MIMETYPE_LOCKED;
   private static String MIMETYPE_PARTICIPANTS;
   private static String MIMETYPE_ANNOUNCE;
   private static String MIMETYPE_STADIUM;
   private static String MIMETYPE_HELP;
   private static String MIMETYPE_WELCOME;
   private static String MIMETYPE_PARTICPANT_ENTER;
   private static String MIMETYPE_PARTICPANT_EXIT;
   private static String EMPTY_MIMETYPE_DATA;

   protected ChatRoom() {
      this.chatRoomData = null;
      this.participants = null;
   }

   public ChatRoom(ChatObjectManagerRoom objectManager, ChatRoomData chatRoomData, GroupData groupData) {
      this.objectManager = objectManager;
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se351ChatRoomDataConcurrentCollectionsEnabled.getValue()) {
         this.chatRoomData = new ChatRoomDataWrapper(this, chatRoomData, groupData);
      } else {
         this.chatRoomData = new ChatRoomDataWrapperPreSE351(this, chatRoomData, groupData);
      }

      this.participants = new ChatRoomParticipants(chatRoomData.name);
      this.maxMessageLength = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomMaxMessageLength", this.maxMessageLength);
      this.maxStatdiumAdminMessageLength = this.objectManager.getProperties().getPropertyAsIntWithDefault("MaxStatdiumAdminMessageLength", this.maxStatdiumAdminMessageLength);
      this.maxUserDuration = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomMaxUserDuration", this.maxUserDuration) * 1000;
      this.maxMessageRepetitions = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomMaxMessageRepetition", this.maxMessageRepetitions);
      this.maxEnterLeaveNotifications = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomMaxEnterLeaveNotifications", this.maxEnterLeaveNotifications);
      this.minKickVotesRequired = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomKickUserVoteMinVotesRequired", this.minKickVotesRequired);
      this.minSpamMessageLength = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomMinSpamMessageLength", this.minSpamMessageLength);
      this.enterLeaveNotificationInterval = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomEnterLeaveNotificationInterval", this.enterLeaveNotificationInterval) * 1000;
      this.kickUserVoteDuration = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomKickUserVoteDuration", this.kickUserVoteDuration) * 1000;
      this.kickUserVoteUpdateInterval = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomKickUserVoteUpdateInterval", this.kickUserVoteUpdateInterval) * 1000;
      this.timeBetweenKickVotes = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomTimeBetweenKickVotes", this.timeBetweenKickVotes) * 1000;
      this.timeBetweenKickingSameUser = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomTimeBetweenKickingSameUser", this.timeBetweenKickingSameUser) * 1000;
      this.banDuration = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomBanDuration", this.banDuration) * 1000;
      this.globalAdminBanDuration = this.objectManager.getProperties().getPropertyAsIntWithDefault("GlobalAdminBanDuration", this.globalAdminBanDuration) * 1000;
      this.adminBanDuration = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomAdminBanDuration", this.adminBanDuration) * 1000;
      this.grpAdminSuspendDuration = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomGroupAdminSuspendDuration", this.grpAdminSuspendDuration) * 1000;
      this.grpModSuspendDuration = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomModSuspendDuration", this.grpModSuspendDuration) * 1000;
      this.userIdleTimeout = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomUserIdleTimeout", this.userIdleTimeout) * 1000;
      this.reenterInterval = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomReenterInterval", this.reenterInterval) * 1000;
      this.dbUpdateInterval = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomDBUpdateInterval", this.dbUpdateInterval) * 1000;
      this.defaultRoomSilencePeriod = this.objectManager.getProperties().getPropertyAsIntWithDefault("DefaultSilencePeriod", this.defaultRoomSilencePeriod);
      this.defaultUserSilencePeriod = this.objectManager.getProperties().getPropertyAsIntWithDefault("DefaultUserSilencePeriod", this.defaultUserSilencePeriod);
      this.listEntrantBufferSize = this.objectManager.getProperties().getPropertyAsIntWithDefault("ListEntrantBufferSize", this.listEntrantBufferSize);
      this.listLockPeriod = this.objectManager.getProperties().getPropertyAsIntWithDefault("ListLockPeriod", this.listLockPeriod);
      this.useBoundedQueueForChatRoomMessageDispatch = StringUtil.toBooleanOrDefault(this.objectManager.getProperties().getPropertyWithDefault("UseBoundedQueueForChatRoomMessageDispatch", "0"), false);
      if (this.useBoundedQueueForChatRoomMessageDispatch) {
         this.chatRoomDispatchBoundedQueueMaxSize = this.objectManager.getProperties().getPropertyAsIntWithDefault("ChatRoomDispatchBoundedQueueMaxSize", Integer.MAX_VALUE);
         log.info(String.format("Using bounded queue as chatroom message queue, chatroom=%s, maxSize=%d", this.chatRoomData.getName(), this.chatRoomDispatchBoundedQueueMaxSize));
         this.messageQueue = new ChatRoom.BlockingQueueViaBlockingQueue(new ArrayBlockingQueue(this.chatRoomDispatchBoundedQueueMaxSize, false));
      } else {
         log.info(String.format("Using linkedlist as chatroom message queue, chatroom=%s, maxSize by sys prop ChatRoom:MsgQueueViaListMaxSize, current value=%d", this.chatRoomData.getName(), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_VIA_LIST_MAX_SIZE)));
         this.messageQueue = new ChatRoom.BlockingQueueViaList(Collections.synchronizedList(new LinkedList()));
      }

      this.enterLeaveNotificationLimiter = new RateLimiter((long)this.maxEnterLeaveNotifications, (long)this.enterLeaveNotificationInterval);
      chatRoomData.dateLastAccessed = new Date();
      this.chatroomEntrantSnapshot = new ChatroomEntrantSnapshot(this.listEntrantBufferSize, this.listLockPeriod);
      this.emoteCommandStates = new EmoteCommandStateStorage(objectManager.getIcePrxFinder());
   }

   public void prepareForPurge() {
      this.purging = true;
      log.warn("Chatroom being purged: " + this.chatRoomData.getName());
   }

   public boolean isIdle() {
      return this.participants.size() == 0 && this.lastTimeMessageSent < System.currentTimeMillis() - this.objectManager.getChatRoomIdleTimeout();
   }

   public ChatRoomDataIce getRoomData() {
      return this.chatRoomData.getRoomData();
   }

   public boolean isStadium() {
      return this.chatRoomData.isStadium();
   }

   public boolean isUserOwned() {
      return this.chatRoomData.isUserOwned();
   }

   public String getCreator() {
      return this.chatRoomData.getCreator();
   }

   public void setMaximumSize(int maximumSize) {
      this.chatRoomData.setMaximumSize(maximumSize);
   }

   public void setAllowKicking(boolean allowKicking) {
      this.chatRoomData.setAllowKicking(allowKicking);
   }

   public void setDescription(String description) {
      this.chatRoomData.setDescription(description);
   }

   public void setAdultOnly(boolean adultOnly) {
      this.chatRoomData.setAdultOnly(adultOnly);
   }

   public void updateDescription(String instigator, String description) throws FusionException {
      ChatRoomParticipant participant = this.participants.verifyYouAreParticipant(instigator);
      if (this.isGroupLinkedChatroom()) {
         if (!participant.isGroupAdmin() && !participant.isGroupMod()) {
            throw new FusionException("You must be a group admin or moderator to set description");
         }
      } else if (!participant.hasAdminOrModeratorRights()) {
         throw new FusionException("You must be a chatroom admin or moderator to set description");
      }

      try {
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         messageEJB.updateRoomDescription(this.chatRoomData.getName(), description);
         this.queueAdminMessage(instigator + " updated description to \"" + description + "\"", (String)null, (String)null);
      } catch (Exception var5) {
         log.warn("Failed to update chatroom description. " + var5.getMessage());
         throw new FusionException("Failed to update room description to " + description);
      }
   }

   public void mute(String username, String target) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.ENABLE_MUTE)) {
         ChatRoomParticipant participant = this.participants.verifyYouAreParticipant(username);
         if (this.isGroupLinkedChatroom()) {
            if (!participant.isGroupAdmin() && !participant.isGroupMod()) {
               throw new FusionException("/mute can only be done by group owners and moderators");
            }
         } else if (!participant.hasAdminOrModeratorRights()) {
            throw new FusionException("/mute can only be done by chatroom admins and moderators");
         }

         ChatRoomParticipant targetParticipant = this.participants.get(target);
         if (targetParticipant == null) {
            throw new FusionException("The user " + target + " is not in the room");
         } else if (targetParticipant.hasAdminOrModeratorRights()) {
            throw new FusionException("The user " + target + " is an admin and can't be muted");
         } else if (this.mutedUsers.containsKey(target)) {
            throw new FusionException("The user " + target + " is already muted");
         } else {
            this.mutedUsers.put(target, Boolean.TRUE);
            this.queueAdminMessage("You have muted " + target, participant.getUsername(), (String)null);
         }
      } else {
         throw new FusionException("Mute has been deprecated . Use silence instead.");
      }
   }

   public void unmute(String username, String target) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.ENABLE_MUTE)) {
         ChatRoomParticipant participant = this.participants.verifyYouAreParticipant(username);
         if (this.isGroupLinkedChatroom()) {
            if (!participant.isGroupAdmin() && !participant.isGroupMod()) {
               throw new FusionException("/unmute can only be done by group owners and moderators");
            }
         } else if (!participant.hasAdminOrModeratorRights()) {
            throw new FusionException("/unmute can only be done by chatroom admins and moderators");
         }

         Boolean b = (Boolean)this.mutedUsers.remove(target);
         if (b == null) {
            throw new FusionException("The user " + target + " is not muted!");
         } else {
            this.queueAdminMessage("You have unmuted " + target, participant.getUsername(), (String)null);
         }
      } else {
         throw new FusionException("Mute has been deprecated . Use silence instead.");
      }
   }

   public void silence(String username, int timeout) throws FusionException {
      ChatRoomParticipant senderParticipant = this.participants.verifyYouAreParticipant(username);
      if (!senderParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("/silence can only be used by admins and moderators");
      } else {
         if (timeout < 0) {
            timeout = this.defaultRoomSilencePeriod;
         }

         this.silenceExpiry = System.currentTimeMillis() + (long)(timeout * 1000);
         this.queueAdminMessage("You have silenced this room for " + timeout + "s", username, (String)null);
         this.queueAdminMessage("This room has been silenced by " + username + " for " + timeout + "s. You cannot send messages at the moment.", (String)null, username);
      }
   }

   public void unsilence(String username) throws FusionException {
      ChatRoomParticipant senderParticipant = this.participants.verifyYouAreParticipant(username);
      if (!senderParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Unsilence can only be used by admins and moderators");
      } else if (this.silenceExpiry < System.currentTimeMillis()) {
         throw new FusionException("This room it not currently silenced");
      } else {
         this.silenceExpiry = 0L;
         this.queueAdminMessage("This room has been unsilenced by " + username, (String)null, (String)null);
      }
   }

   public void silenceUser(String instigator, String target, int timeout) throws FusionException {
      ChatRoomParticipant senderParticipant = this.participants.verifyYouAreParticipant(instigator);
      if (!senderParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("/silence can only be used by admins and moderators");
      } else {
         ChatRoomParticipant targetParticipant = this.participants.verifyIsParticipant(target);
         if (targetParticipant.hasAdminOrModeratorRights()) {
            throw new FusionException("Admins and moderators cannot be silenced");
         } else {
            if (timeout < 0) {
               timeout = this.defaultUserSilencePeriod;
            }

            long expiry = System.currentTimeMillis() + (long)(timeout * 1000);
            this.silencedUsers.put(target, expiry);
            this.queueAdminMessage("You have been silenced by " + instigator + ". You will not be able to send messages for the next " + timeout + " seconds", target, (String)null);
            this.queueAdminMessage(target + " has been silenced by " + instigator + " for " + timeout + " seconds", (String)null, target);
         }
      }
   }

   public void unsilenceUser(String instigator, String target) throws FusionException {
      ChatRoomParticipant senderParticipant = this.participants.verifyYouAreParticipant(instigator);
      if (!senderParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("/unsilence can only be used by admins and moderators");
      } else {
         Long expiry = (Long)this.silencedUsers.get(target);
         if (expiry == null) {
            throw new FusionException(target + " is not currently silenced");
         } else {
            this.silencedUsers.remove(target);
            this.queueAdminMessage("You have unsilenced " + target, instigator, (String)null);
            this.queueAdminMessage("You have been unsilenced by " + instigator, target, (String)null);
         }
      }
   }

   public void convertIntoGroupChatRoom(int groupID, String groupName) throws FusionException {
      GroupData tmpGroupData;
      try {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_GROUP_DAO)) {
            tmpGroupData = DAOFactory.getInstance().getGroupDAO().getGroup(groupID);
         } else {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            tmpGroupData = userEJB.getGroup(groupID);
         }
      } catch (Exception var8) {
         throw new FusionException(var8.getMessage());
      }

      if (tmpGroupData == null) {
         throw new FusionException("Invalid group ID " + groupID);
      } else {
         GroupData groupData = this.chatRoomData.convertIntoGroupChatRoom(tmpGroupData);
         this.queueAdminMessage("This chat room has been attached to the group '" + groupData.name + "' by " + this.chatRoomData.getCreator() + ". You will be disconnected from the room.", (String)null, (String)null);
         Iterator i$ = this.participants.getAllNames().iterator();

         while(i$.hasNext()) {
            String participant = (String)i$.next();
            this.removeParticipant(participant);
         }

         this.recentlyLeftUsers.clear();

         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            messageEJB.resetRoomModerators(this.chatRoomData.getName());
         } catch (Exception var7) {
            log.error("Failed to reset moderators " + var7.getMessage());
         }

      }
   }

   public void convertIntoUserOwnedChatRoom() throws FusionException {
      GroupData groupDataRemoved = this.chatRoomData.convertIntoUserChatRoom();
      if (groupDataRemoved != null) {
         this.chatRoomData.makeUserOwned();
         this.queueAdminMessage("This chat room has been removed from the group '" + groupDataRemoved.name + "' by " + groupDataRemoved.createdBy, (String)null, (String)null);
         Iterator i$ = this.participants.getAllNames().iterator();

         while(i$.hasNext()) {
            String participant = (String)i$.next();
            this.removeParticipant(participant);
         }

         this.recentlyLeftUsers.clear();
      }
   }

   public void changeOwner(String oldOwnerUsername, String newOwnerUsername) {
      this.chatRoomData.setCreator(newOwnerUsername);
   }

   public void addParticipant(UserPrx userPrx, UserData userData, SessionPrx sessionPrx, String sessionID, String ipAddress, String mobileDevice, String userAgent, short clientVersion, int deviceType) throws FusionException {
      ChatRoomParticipant participant = new ChatRoomParticipant(this.chatRoomData, userPrx, sessionPrx, sessionID, ipAddress, mobileDevice, userAgent, userData, clientVersion, deviceType);
      this.addParticipantInner(participant, userData);
   }

   public void addParticipant(UserPrx userPrx, UserData userData, SessionPrx sessionPrx, String sessionID, String ipAddress, String mobileDevice, String userAgent) throws FusionException {
      ChatRoomParticipant participant = new ChatRoomParticipant(this.chatRoomData, userPrx, sessionPrx, sessionID, ipAddress, mobileDevice, userAgent, userData);
      this.addParticipantInner(participant, userData);
   }

   private void addParticipantInner(final ChatRoomParticipant participant, UserData userData) throws FusionException {
      if (participant.isTopMerchant()) {
         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
               participant.setMerchantDetails((new UserObject(participant.getUsername())).getBasicMerchantDetails());
            } else {
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               participant.setMerchantDetails(userEJB.getBasicMerchantDetails(participant.getUsername()));
            }
         } catch (Exception var10) {
            throw new FusionException(var10.getMessage());
         }
      }

      this.screenUserForEntrance(userData, participant);
      this.removeIdleParticipants();
      ChatRoomData chatRoomDataSnapshot;
      boolean alreadyInChatRoom;
      synchronized(this.chatRoomData) {
         if (!participant.hasAdminOrModeratorRights() && this.participants.size() >= this.chatRoomData.getMaximumSize()) {
            throw new FusionException("Chat room is full");
         }

         chatRoomDataSnapshot = this.chatRoomData.snapshotChatRoomData();
         participant.setOnRemoveListener(this);
         alreadyInChatRoom = this.participants.add(participant) != null;
      }

      this.sendGreetingMessagesAsync(participant, chatRoomDataSnapshot);
      RecentChatRoomList.addRecentChatRoom(recentChatRoomMemcache, participant.getUsername(), this.chatRoomData.getName());
      if (!alreadyInChatRoom && !participant.isHiddenAdmin() && (!this.isStadium() || participant.hasAdminOrModeratorRights()) && this.enterLeaveNotificationLimiter.hit()) {
         this.objectManager.onRoomSessionAdded();
         this.queueEntryExitAdminMessage(participant, true);
         this.objectManager.getDistributionService().execute(new Runnable() {
            public void run() {
               try {
                  ChatRoom.this.notifyUserJoinedChatRoom(participant.getUsername());
               } catch (Exception var2) {
                  ChatRoom.log.error("Exception caught notifying chatroom that user [" + participant.getUsername() + "] joined", var2);
               }

            }
         });
      }

      Iterator i$ = this.bots.values().iterator();

      while(i$.hasNext()) {
         BotInstance bot = (BotInstance)i$.next();
         bot.botServiceProxy.sendNotificationToBotsInChannel(this.botChannelID, participant.getUsername(), 1);
      }

      participant.addToUsersCurrentChatroomList();
      this.updateChatRoomDetailInDB();

      try {
         int migLevel = MemCacheOrEJB.getUserReputationLevel(userData.username, userData.userID);
         synchronized(this.chatroomEntrantSnapshot) {
            this.chatroomEntrantSnapshot.addEntrant(participant.getUsername(), migLevel, participant.getIPAddress());
         }
      } catch (Exception var9) {
         log.error("Unable to check mig level: " + var9);
      }

   }

   public void removeParticipantOnException(ChatRoomParticipant participant) {
      try {
         this.removeParticipant(participant.getUsername());
      } catch (Exception var3) {
         log.warn("Failed to remove participant [" + participant.getUsername() + "] from chat room [" + this.chatRoomData.getName() + "]", var3);
      }

   }

   public void removeParticipant(String username) throws FusionException {
      this.removeParticipant(username, true);
   }

   public void removeParticipant(String username, boolean removeFromUsersChatRoomList) throws FusionException {
      ChatRoomParticipant participant = this.participants.remove(username);
      if (participant != null) {
         synchronized(this.kickUserVoteMonitor) {
            if (this.kickUserVote != null) {
               this.kickUserVote.removeYesVote(username);
            }
         }

         if (!this.isStadium()) {
            this.recentlyLeftUsers.put(username, System.currentTimeMillis());
         }

         if (!participant.isHiddenAdmin() && (!this.isStadium() || participant.hasAdminOrModeratorRights()) && this.enterLeaveNotificationLimiter.hit()) {
            this.objectManager.onRoomSessionRemoved();
            if (!this.participants.isEmpty()) {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.SILENCE_FAST_EXIT_MESSAGES)) {
                  if (participant.getTimeInRoomMillis() > (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.EXIT_SILENCE_TIME_IN_MS)) {
                     this.queueEntryExitAdminMessage(participant, false);
                  }
               } else {
                  this.queueEntryExitAdminMessage(participant, false);
               }

               final String participantName = participant.getUsername();
               this.objectManager.getDistributionService().execute(new Runnable() {
                  public void run() {
                     try {
                        ChatRoom.this.notifyUserLeftChatRoom(participantName);
                     } catch (Exception var2) {
                        ChatRoom.log.error("Exception caught notifying chatroom [" + ChatRoom.this.chatRoomData.getName() + "] that user [" + participantName + "] left", var2);
                     }

                  }
               });
            }
         }

         if (participant.hasAdminOrModeratorRights()) {
            synchronized(this.chatRoomData) {
               if (this.chatRoomData.isLocked() && username.equals(this.chatRoomData.getLocker())) {
                  this.chatRoomData.unlock();
                  this.queueAdminMessage("This chat room has been unlocked because [" + participant.getUsername() + "] the user who locked it has left", (String)null, (String)null);
               }
            }
         }

         if (this.getNumParticipants() == 0) {
            synchronized(this.chatRoomData) {
               if (this.chatRoomData.isAnnouncementOn()) {
                  this.turnAnnouncementOff(participant, false);
               }
            }
         }

         Iterator i$ = this.bots.values().iterator();

         while(i$.hasNext()) {
            BotInstance bot = (BotInstance)i$.next();
            bot.botServiceProxy.sendNotificationToBotsInChannel(this.botChannelID, username, BotData.BotCommandEnum.QUIT.value());
         }

         if (removeFromUsersChatRoomList) {
            participant.removeFromUsersCurrentChatroomList();
         }

      }
   }

   private void notifyUserJoinedChatRoom(String username) throws FusionException {
      boolean isBanned = this.chatRoomData.isOnBannedList(username);
      this.participants.notifyUserJoinedChatRoom(isBanned, username);
   }

   private void notifyUserLeftChatRoom(String username) throws FusionException {
      this.participants.notifyUserLeftChatRoom(username);
   }

   public boolean isAdministrator(String requestingUsername) {
      return this.participants.isAdministrator(requestingUsername);
   }

   public String[] getParticipants(String requestingUsername) {
      return this.isStadium() ? this.getAdministrators(requestingUsername) : this.participants.getAllParticipantsExceptHiddenAdmins(requestingUsername);
   }

   public String[] getAllParticipants(String requestingUsername) {
      return this.participants.getAllParticipants(requestingUsername);
   }

   public String[] getAdministrators(String requestingUsername) {
      return this.participants.getAdministrators(requestingUsername);
   }

   public int getNumParticipants() {
      return Math.min(this.participants.size() + this.numFakeParticipants, this.chatRoomData.getMaximumSize());
   }

   public void setNumberOfFakeParticipants(String username, int number) {
      ChatRoomParticipant participant = this.participants.get(username);
      if (participant != null && participant.hasAdminOrModeratorRights() && this.isStadium() && number > 0) {
         synchronized(this.chatRoomData) {
            this.numFakeParticipants = number;
         }

         this.queueAdminMessage("Reserved participants set to " + number, username, (String)null);
      }

   }

   public boolean isParticipant(String username) throws FusionException {
      return this.participants.isParticipant(username);
   }

   public boolean isVisibleParticipant(String username) throws FusionException {
      ChatRoomParticipant p = this.participants.get(username);
      return p != null && !p.isHiddenAdmin() && (!this.isStadium() || p.hasAdminOrModeratorRights());
   }

   public void listParticipants(String requestingUsername, int size, int startIndex) throws FusionException {
      ChatRoomParticipant requestor = this.participants.verifyYouAreParticipant(requestingUsername);
      if (!requestor.hasAdminOrModeratorRights()) {
         throw new FusionException("Only admins and moderators may use the list command");
      } else {
         synchronized(this.chatroomEntrantSnapshot) {
            if (this.chatroomEntrantSnapshot.isCurrentSnapshotRunning() && this.chatroomEntrantSnapshot.hasLockExpired()) {
               this.chatRoomData.unlock();
               this.chatroomEntrantSnapshot.clearSnapshot();
            }

            if (!this.chatroomEntrantSnapshot.isCurrentSnapshotRunning()) {
               this.lock(requestingUsername);
               this.queueAdminMessage("This chat room has been locked for " + this.listLockPeriod + " seconds.", (String)null, (String)null);
               this.silence(requestingUsername, this.defaultRoomSilencePeriod);
               this.chatroomEntrantSnapshot.initLockExpiry();
            }

            String entrantsList = this.chatroomEntrantSnapshot.getEntrantListStr(size, startIndex);
            this.queueAdminMessage(entrantsList, requestingUsername, (String)null);
         }
      }
   }

   public int getMaximumMessageLength(String sender) {
      ChatRoomParticipant senderParticipant = this.participants.get(sender);
      return this.getMaximumMessageLength(senderParticipant);
   }

   public ChatRoomData getNewRoomData() {
      return this.chatRoomData.getNewChatRoomData();
   }

   private int getMaximumMessageLength(ChatRoomParticipant sender) {
      return sender != null && this.isStadium() && sender.hasAdminOrModeratorRights() ? this.maxStatdiumAdminMessageLength : this.maxMessageLength;
   }

   public void putMessage(final MessageDataIce message, String sessionID) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_CHECK_SIZE_BEFORE_PUTMESSAGE_ENABLED)) {
         int maxQueueSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_VIA_LIST_MAX_SIZE);
         int currentQueueSize = this.messageQueue.size();
         if (currentQueueSize > maxQueueSize) {
            log.warn("ChatroomMaxDispatchQueueSize [" + maxQueueSize + "] exceeded. CurrentSize[" + currentQueueSize + "]. User[" + message.source + "] Chatroom[" + this.chatRoomData.getName() + "]");
            throw new FusionException("Unable to send chat messages now. Please try again later.");
         }
      }

      ChatRoomParticipant senderParticipant = this.participants.get(message.source);
      if (senderParticipant == null) {
         throw new FusionException("You are not in the " + this.chatRoomData.getName() + " chat room");
      } else if (senderParticipant.getSessionID().equals(sessionID)) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.DROP_MESSAGES_FROM_MUTED_USERS, true) && this.mutedUsers.containsKey(message.source)) {
            this.queueAdminMessage("You have been muted in this room and cannot send messages.", senderParticipant.getUsername(), (String)null);
         } else if (this.silenceExpiry >= System.currentTimeMillis() && !senderParticipant.hasAdminOrModeratorRights()) {
            this.queueAdminMessage("This room has been silenced at the moment, you cannot send messages.", senderParticipant.getUsername(), (String)null);
         } else {
            Long userSilenceTimeout = (Long)this.silencedUsers.get(message.source);
            if (userSilenceTimeout != null) {
               if (userSilenceTimeout >= System.currentTimeMillis()) {
                  this.queueAdminMessage("You cannot send messages while silenced.", message.source, (String)null);
                  return;
               }

               this.silencedUsers.remove(message.source);
            }

            int maxLength = this.getMaximumMessageLength(senderParticipant);
            if (message.messageText.length() > maxLength) {
               throw new FusionException("Your message is too long. Please keep messages under " + maxLength + " characters");
            } else if (senderParticipant.isSpamming(message.messageText, this.maxMessageRepetitions, this.minSpamMessageLength)) {
               log.warn("Disconnect user [" + message.source + "] with session ID [" + senderParticipant.getSessionID() + "] for spamming message [" + message.messageText + "] in chat room [" + this.chatRoomData.getName() + "]");
               senderParticipant.silentlyDropIncomingPackets();
            } else {
               message.messageDestinations[0].destination = this.chatRoomData.getName();
               Integer specialColor = senderParticipant.getMessageSourceColorOverride();
               if (specialColor != null) {
                  message.sourceColour = specialColor;
               }

               if (senderParticipant.hasAdminOrModeratorRights() && !senderParticipant.isTopMerchant()) {
                  message.fromAdministrator = 1;
               }

               message.messageDestinations[0].status = MessageDestinationData.StatusEnum.SENT.value();
               message.messageDestinations[0].dateDispatched = System.currentTimeMillis();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.CHECK_USER_PRESENT_BEFORE_DISPATCH)) {
                  final ChatRoom.MessageToDispatch mtd = new ChatRoom.UserMessageToDispatchToRoom(message, message.source);
                  int messageDelay = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.DELAY_USER_MESSAGE_TIME);
                  if (messageDelay > 0) {
                     this.objectManager.getDistributionService().schedule(new Runnable() {
                        public void run() {
                           if (!ChatRoom.this.queueMessage(mtd, message.source, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS))) {
                              ChatRoom.log.warn(String.format("Unable to queue message from [%s] to chat room: [%s]" + message.source, ChatRoom.this.chatRoomData.getName()));
                           }

                        }
                     }, (long)messageDelay, TimeUnit.MILLISECONDS);
                  } else if (!this.queueMessage(mtd, message.source, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS))) {
                     throw new FusionException("Unable to send chat messages now. Please try again later.");
                  }
               } else if (!this.queueMessage(message, (String)null, message.source, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS))) {
                  throw new FusionException("Unable to send chat messages now. Please try again later.");
               }

               this.logMessage(senderParticipant, message.messageText);
               senderParticipant.updateLastTimeMessageSent();
               if (this.isStadium() && !senderParticipant.hasAdminOrModeratorRights()) {
                  this.queueAdminMessage("Thank you for your message. It has been sent to moderators", senderParticipant.getUsername(), (String)null);
               }

            }
         }
      }
   }

   public void putSystemMessage(String messageText, String[] emoticonKeys) {
      this.putSystemMessageWithColour(messageText, emoticonKeys, -1);
   }

   public void putSystemMessageWithColour(String messageText, String[] emoticonKeys, int messageColour) {
      MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), messageText, emoticonKeys);
      messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
      if (messageColour >= 0) {
         messageData.messageColour = messageColour;
      }

      this.queueMessage(messageData.toIceObject(), (String)null, (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
      this.logMessage((ChatRoomParticipant)null, messageText);
   }

   public void addModerator(String username) {
      this.chatRoomData.addModerator(username);
   }

   public void removeModerator(String username) {
      this.chatRoomData.removeModerator(username);
   }

   public boolean isModerator(String username) {
      return this.chatRoomData.isModerator(username);
   }

   public Map<String, String> getTheme() {
      return this.chatRoomData.getTheme();
   }

   public void banUser(String username) {
      this.chatRoomData.addBannedUser(username);
      ChatRoomParticipant participant = this.participants.get(username);
      if (participant != null) {
         try {
            MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), "You have been banned from this chat room", (String[])null);
            participant.putMessage(messageData.toIceObject());
         } catch (Exception var5) {
         }

         try {
            this.removeParticipant(username);
         } catch (Exception var4) {
         }
      }

   }

   public void unbanUser(String username) {
      this.chatRoomData.removeBannedUser(username);
   }

   public void banGroupMembers(String[] bannedList, String bannedby, int reason) throws FusionException {
      GroupData groupData = this.chatRoomData.getGroupData();
      if (groupData == null) {
         throw new FusionException("/ban is not a valid command");
      } else {
         ChatRoomParticipant instigator = this.participants.get(bannedby);
         if (instigator == null) {
            throw new FusionException("You are no longer in the room " + this.chatRoomData.getName());
         } else if (!instigator.isGroupAdmin() && !instigator.isGroupMod()) {
            throw new FusionException("You need to be a group admin or moderator to ban users");
         } else {
            String errorMsg = "";

            for(int i = 0; i < bannedList.length; ++i) {
               String banned = bannedList[i];
               ChatRoomParticipant target = this.participants.get(banned);
               if (target != null) {
                  if (target.isGroupAdmin() || target.isGlobalAdmin()) {
                     errorMsg = errorMsg + "You cannot ban admin (" + banned + "). ";
                     continue;
                  }

                  if (target.isGroupMod() && !instigator.isGroupAdmin()) {
                     errorMsg = errorMsg + "You cannot ban a group moderator (" + banned + "). ";
                     continue;
                  }
               }

               Message messageEJB = null;

               try {
                  String strReason = Enums.GroupBanReasonEnum.getDescription(reason);
                  messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
                  messageEJB.banGroupMember(instigator.getUsername(), groupData, banned);
                  if (target != null) {
                     this.removeParticipant(banned);
                     StringBuffer message = new StringBuffer();
                     message.append("You have been banned from the group (and all the rooms in this group) ");
                     message.append(groupData.name);
                     message.append(" by ");
                     if (instigator.isGroupAdmin()) {
                        message.append(" group admin ");
                     } else {
                        message.append(" group moderator ");
                     }

                     message.append(this.formatUserNameWithLevel(instigator));
                     message.append(", reason: " + strReason);
                     MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), message.toString(), (String[])null);
                     target.putMessage(messageData.toIceObject());
                  }

                  this.queueAdminMessage(this.formatUserNameWithLevel(banned) + " has been banned by " + this.formatUserNameWithLevel(instigator.getUsername()) + ", reason: " + strReason, (String)null, banned);
               } catch (RemoteException var14) {
                  errorMsg = errorMsg + "Unable to ban user " + banned + ". ";
               } catch (Exception var15) {
                  log.warn("Unable to perma ban " + banned + " from room: " + this.chatRoomData.getName() + ", reason: " + var15.getMessage());
                  errorMsg = errorMsg + banned + " is either inactive/banned or not part of this group. ";
               }
            }

            if (errorMsg != null && errorMsg.length() != 0) {
               throw new FusionException(errorMsg);
            }
         }
      }
   }

   public void unbanGroupMember(String unbanned, String unbannedby, int reason) throws FusionException {
      GroupData groupData = this.chatRoomData.getGroupData();
      if (groupData == null) {
         throw new FusionException("/unban is not a valid command");
      } else {
         ChatRoomParticipant instigator = this.participants.verifyYouAreParticipant(unbannedby);
         if (!instigator.isGroupAdmin() && !instigator.isGroupMod()) {
            throw new FusionException("You need to be a group admin or moderator to unban users");
         } else {
            Message messageEJB = null;

            try {
               String strReason = Enums.GroupUnbanReasonEnum.getDescription(reason);
               messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               messageEJB.unbanGroupMember(instigator.getUsername(), groupData, unbanned);
               this.queueAdminMessage(this.formatUserNameWithLevel(unbanned) + " has been unbanned by " + this.formatUserNameWithLevel(instigator) + ", reason: " + strReason, (String)null, (String)null);
            } catch (Exception var8) {
               log.warn("Unable to unban " + unbanned + " from room: " + this.chatRoomData.getName() + ", reason: " + var8.getMessage());
               throw new FusionException(unbanned + " is either active or not a member of this group");
            }
         }
      }
   }

   public void banIndexes(int[] indexes, String bannedBy, int reason) throws FusionException {
      if (!this.isGroupLinkedChatroom()) {
         throw new FusionException("/banindex is not a valid command");
      } else if (this.chatroomEntrantSnapshot.isCurrentSnapshotRunning() && !this.chatroomEntrantSnapshot.hasLockExpired()) {
         String[] bannedUsernames = this.chatroomEntrantSnapshot.getSnapshotUsernamesFromIndexes(indexes);
         this.banGroupMembers(bannedUsernames, bannedBy, reason);
      } else {
         throw new FusionException("/banindex can only be used after the list command");
      }
   }

   public void banMultiIds(String requestingUsername) throws FusionException {
      ChatRoomParticipant requestor = this.participants.verifyYouAreParticipant(requestingUsername);
      if (!requestor.hasAdminOrModeratorRights()) {
         throw new FusionException("Only admins and moderators may use the kill command");
      } else {
         Set<String> banList = MultiIdFinder.getMultiIds(this.getIpToUserMap(), this.chatRoomData.getName());
         String banMsg = "";
         if (banList.isEmpty()) {
            banMsg = "No users have met the criteria for banning";
         } else {
            banMsg = "The following users have been banned from this chatroom:\n";
            Iterator i$ = banList.iterator();

            while(i$.hasNext()) {
               String username = (String)i$.next();
               if (!username.equals(requestingUsername)) {
                  try {
                     Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
                     if (messageEJB.updateChatroomBannedList(this.chatRoomData.getName(), username)) {
                        this.banUser(username);
                        banMsg = banMsg + username + "\n";
                     }
                  } catch (Exception var8) {
                     log.warn("Unable to ban User [" + username + "] from Room [" + this.chatRoomData.getName() + "]", var8);
                  }
               }
            }
         }

         this.queueAdminMessage(banMsg, requestingUsername, (String)null);
      }
   }

   private HashMap<String, List<String>> getIpToUserMap() {
      return this.participants.getIpToUserMap();
   }

   public void kickIndexes(int[] indexes, String kickedBy) throws FusionException {
      if (this.chatroomEntrantSnapshot.isCurrentSnapshotRunning() && !this.chatroomEntrantSnapshot.hasLockExpired()) {
         ChatRoomParticipant voterParticipant = this.participants.verifyYouAreParticipant(kickedBy);
         if (!this.chatRoomData.isAllowKicking() && !voterParticipant.hasAdminOrModeratorRights()) {
            throw new FusionException("Only admins and moderators may kick in this room");
         } else {
            String[] kickUsernames = this.chatroomEntrantSnapshot.getSnapshotUsernamesFromIndexes(indexes);
            String errorMsg = "";

            for(int i = 0; i < kickUsernames.length; ++i) {
               String target = kickUsernames[i];
               ChatRoomParticipant targetParticipant = this.participants.get(target);
               if (targetParticipant == null) {
                  errorMsg = errorMsg + target + " is no longer in the chat room " + this.chatRoomData.getName() + ". ";
               } else if (targetParticipant.hasAdminOrModeratorRights()) {
                  errorMsg = errorMsg + "User " + target + " is an admin or moderator and cannot be kicked. ";
               } else {
                  synchronized(this.adminKickMonitor) {
                     if (this.participants.get(target) == null) {
                        errorMsg = errorMsg + target + " is no longer in the chat room " + this.chatRoomData.getName() + ". ";
                     } else {
                        this.kickParticipant(voterParticipant, targetParticipant, (String)null);
                     }
                  }
               }
            }

            if (errorMsg != null && errorMsg.length() != 0) {
               throw new FusionException(errorMsg);
            }
         }
      } else {
         throw new FusionException("/kickindex can only be used after the list command");
      }
   }

   public void inviteUserToGroup(String invitee, String inviter) throws FusionException {
      GroupData groupData = this.chatRoomData.getGroupData();
      if (groupData == null) {
         throw new FusionException("/invite is valid only in group chat rooms");
      } else {
         ChatRoomParticipant inviterParticipant = this.participants.verifyYouAreParticipant(inviter);
         if ((groupData.type == GroupData.TypeEnum.CLOSED || groupData.type == GroupData.TypeEnum.UNLISTED) && !inviterParticipant.isGroupAdmin() && !inviterParticipant.isGroupMod()) {
            throw new FusionException("You need to be a group admin or moderator to invite users to a group");
         } else {
            try {
               Web webBean = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
               String result = webBean.inviteUserToGroup(inviter, invitee, groupData.id);
               if (!result.equals("TRUE")) {
                  throw new FusionException(ExceptionHelper.removeErrorMessagePrefix(result));
               } else {
                  this.queueAdminMessage("An invite has been sent to " + invitee + " to join group " + groupData.name, inviter, (String)null);
               }
            } catch (FusionException var7) {
               throw new FusionException(var7.message);
            } catch (Exception var8) {
               log.debug("Unable to invite user to group: ", var8);
               throw new FusionException("Unable to invite " + invitee + " to group " + groupData.name);
            }
         }
      }
   }

   public void broadcastMessage(String instigator, String message) throws FusionException {
      GroupData groupData = this.chatRoomData.getGroupData();
      if (groupData == null) {
         throw new FusionException("/broadcast is valid only in group chat rooms");
      } else {
         ChatRoomParticipant instigatorParticipant = this.participants.verifyYouAreParticipant(instigator);
         if (!instigatorParticipant.isGroupAdmin() && !instigatorParticipant.isGroupMod()) {
            throw new FusionException("You need to be a group admin or moderator to broadcast to a group");
         } else {
            String broadcastMsg = String.format("%s (%s) BROADCAST: %s", groupData.name, instigator, message);

            try {
               String[] chatrooms = null;
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
                  try {
                     chatrooms = DAOFactory.getInstance().getChatRoomDAO().getGroupChatRooms(groupData.id);
                  } catch (DAOException var12) {
                     log.warn(String.format("DAO: Failed to get GroupChatRooms for groupid:%s", groupData.id), var12);
                  }
               } else {
                  Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
                  chatrooms = messageEJB.getGroupChatRooms(groupData.id);
               }

               ChatRoomPrx[] chatRoomProxies = null;
               if (chatrooms != null && chatrooms.length > 0) {
                  chatRoomProxies = this.objectManager.getRegistryPrx().findChatRoomObjects(chatrooms);
               }

               if (chatRoomProxies != null) {
                  ChatRoomPrx[] arr$ = chatRoomProxies;
                  int len$ = chatRoomProxies.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     ChatRoomPrx chatRoomPrx = arr$[i$];
                     if (chatRoomPrx != null) {
                        chatRoomPrx.putSystemMessageWithColour(broadcastMsg, (String[])null, SystemProperty.getInt("ChatroomBroadcastMessageColor", 7798784));
                     }
                  }
               }

            } catch (LocalException var13) {
               throw new EJBException("Failed to broadcast message to chat rooms: " + var13.getMessage());
            } catch (Exception var14) {
               throw new FusionException("Unable to broadcast message [" + message + "] to group " + groupData.name);
            }
         }
      }
   }

   public void voteToKickUser(String voter, String target) throws FusionException {
      ChatRoomParticipant voterParticipant = this.participants.verifyYouAreParticipant(voter);
      if (!this.chatRoomData.isAllowKicking() && !voterParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Only admins and moderators may kick in this room");
      } else {
         ChatRoomParticipant targetParticipant;
         boolean found;
         if (voterParticipant.hasAdminOrModeratorRights()) {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.PT67727788_USE_LOCKLESS_ADMIN_KICK)) {
               if (target.contains("#")) {
                  if (!MULTI_KICK_PATTERN_CHECK.matcher(target).find()) {
                     throw new FusionException("target with # must contain at least a letter, number, period (.), hyphens (-), or underscore (_)");
                  }

                  Pattern p = Pattern.compile(target.replaceAll("#", "[\\.A-Za-z0-9_-]+"));
                  boolean found = false;
                  Iterator i$ = this.participants.getAllNames().iterator();

                  while(i$.hasNext()) {
                     String participantName = (String)i$.next();
                     Matcher m = p.matcher(participantName);
                     if (m.matches()) {
                        ChatRoomParticipant participant = this.participants.get(participantName);
                        if (participant != null) {
                           if (participant.hasAdminOrModeratorRights()) {
                              throw new FusionException("User is an admin or moderator and cannot be kicked");
                           }

                           this.kickParticipant(voterParticipant, participant, (String)null);
                           found = true;
                        }
                     }
                  }

                  if (!found) {
                     throw new FusionException("there is no participants match for " + target);
                  }
               } else {
                  ChatRoomParticipant targetParticipant = this.participants.verifyIsParticipant(target);
                  if (targetParticipant.hasAdminOrModeratorRights()) {
                     throw new FusionException("User is an admin or moderator and cannot be kicked");
                  }

                  this.kickParticipant(voterParticipant, targetParticipant, (String)null);
               }

            } else {
               synchronized(this.adminKickMonitor) {
                  if (target.contains("#")) {
                     if (!Pattern.compile("[\\.A-Za-z0-9_-]{1}").matcher(target).find()) {
                        throw new FusionException("target with # must contain at least a letter, number, period (.), hyphens (-), or underscore (_)");
                     }

                     Pattern p = Pattern.compile(target.replaceAll("#", "[\\.A-Za-z0-9_-]+"));
                     found = false;
                     Iterator i$ = this.participants.getAllNames().iterator();

                     while(i$.hasNext()) {
                        String participantName = (String)i$.next();
                        Matcher m = p.matcher(participantName);
                        if (m.matches()) {
                           ChatRoomParticipant participant = this.participants.get(participantName);
                           if (participant.hasAdminOrModeratorRights()) {
                              throw new FusionException("User is an admin or moderator and cannot be kicked");
                           }

                           this.kickParticipant(voterParticipant, participant, (String)null);
                           found = true;
                        }
                     }

                     if (!found) {
                        throw new FusionException("there is no participants match for " + target);
                     }
                  } else {
                     targetParticipant = this.participants.verifyIsParticipant(target);
                     if (targetParticipant.hasAdminOrModeratorRights()) {
                        throw new FusionException("User is an admin or moderator and cannot be kicked");
                     }

                     this.kickParticipant(voterParticipant, targetParticipant, (String)null);
                  }

               }
            }
         } else {
            int disableVoteKickTimeInMs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.DISABLE_VOTE_KICK_TIME_IN_SECONDS) * 1000;
            if (voterParticipant.getTimeInRoomMillis() < (long)disableVoteKickTimeInMs) {
               throw new FusionException("You must be in a room longer before you can vote kick other users");
            } else {
               targetParticipant = this.participants.verifyIsParticipant(target);
               if (targetParticipant.hasAdminOrModeratorRights()) {
                  throw new FusionException("User is an admin or moderator and cannot be kicked");
               } else {
                  found = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.ENABLE_OVERRIDE_EXPIRED_KICK_USER_VOTE);
                  synchronized(this.kickUserVoteMonitor) {
                     if (this.participants.get(target) == null) {
                        throw new FusionException(target + " is no longer in the chat room " + this.chatRoomData.getName());
                     } else {
                        boolean hasKickUserVoteInProgress = false;
                        if (found) {
                           hasKickUserVoteInProgress = this.kickUserVote != null && !this.kickUserVote.isVotingExpired();
                        } else {
                           hasKickUserVoteInProgress = this.kickUserVote != null;
                        }

                        if (hasKickUserVoteInProgress) {
                           if (!target.equals(this.kickUserVote.getTarget())) {
                              throw new FusionException("A vote to kick " + this.kickUserVote.getTarget() + " is currently in progress");
                           }

                           this.kickUserVote.addYesVote(voterParticipant);
                        } else {
                           boolean detectedExpiredVoting = this.kickUserVote != null && this.kickUserVote.isVotingExpired();
                           if (detectedExpiredVoting) {
                              this.logWarn("Detected a vote to kick (" + this.kickUserVote.toString() + ") that has been running too long and has not completed.");
                           }

                           if (System.currentTimeMillis() - voterParticipant.getLastTimeKickVoteInitiated() < (long)this.timeBetweenKickVotes) {
                              throw new FusionException("You recently started a kick vote. Please wait a short while before starting another one");
                           }

                           if (System.currentTimeMillis() - targetParticipant.getLastTimeTargetOfKickVote() < (long)this.timeBetweenKickingSameUser) {
                              throw new FusionException("A vote to kick " + target + " recently failed. Please wait a short while before trying to kick " + target + " again");
                           }

                           try {
                              double cost = SystemProperty.getDouble("ChatRoomKickCost", 0.0D);
                              if (cost > 0.0D) {
                                 AccountBalanceData balanceData = null;
                                 Account accountEJB = null;
                                 if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                                    balanceData = (new UserObject(voter)).getAccountBalance();
                                 } else {
                                    accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
                                    balanceData = accountEJB.getAccountBalance(voter);
                                 }

                                 double balance = balanceData.getBaseBalance();
                                 if (balance < cost) {
                                    throw new FusionException("You do not have enough credit to start a kick. Please recharge your account");
                                 }

                                 AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(voterParticipant.getIPAddress(), voterParticipant.getSessionID(), voterParticipant.getMobileDevice(), voterParticipant.getUserAgent());
                                 if (accountEJB == null) {
                                    accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
                                 }

                                 accountEJB.chargeUserForChatRoomKick(voter, target, "Started vote to kick " + target + " from a chatroom", cost, accountEntrySourceData);
                              }
                           } catch (CreateException var20) {
                              throw new FusionException("Unable to create EJB to charge for kick");
                           } catch (RemoteException var21) {
                              throw new FusionException(RMIExceptionHelper.getRootMessage(var21));
                           } catch (DAOException var22) {
                              log.error(String.format("DAO: Failed to get Account Balance Data for user:%s", voter), var22);
                              throw new FusionException(String.format("Failed to get Account Balance Data for user:%s", voter));
                           }

                           voterParticipant.setLastTimeKickVoteInitiated(System.currentTimeMillis());
                           targetParticipant.setLastTimeTargetOfKickVote(System.currentTimeMillis());

                           try {
                              this.kickUserVote = new ChatRoom.KickUserVote(voterParticipant, this.chatRoomData.getName(), target);
                           } catch (Throwable var18) {
                              this.logError("Failed to start vote kick. Voter:[" + voter + "].Target:[" + target + "]", var18);
                              throw new FusionException("Failed to start vote kick");
                           }
                        }

                     }
                  }
               }
            }
         }
      }
   }

   public void clearUserKick(String instigator, String target) throws FusionException {
      ChatRoomParticipant instigatorParticipant = this.participants.verifyYouAreParticipant(instigator);
      if (!instigatorParticipant.isGlobalAdmin()) {
         throw new FusionException("Only global admins may clear kicks in this room");
      } else {
         instigatorParticipant.verifyClientMeetsMinVersion("/kick");
         ChatRoom.BannedInfo bannedInfo = (ChatRoom.BannedInfo)this.bannedUsers.get(target);
         if (bannedInfo == null) {
            throw new FusionException("User " + target + " was never kicked from the chat room " + this.chatRoomData.getName());
         } else if (bannedInfo.getInstigator() != null && !bannedInfo.getInstigator().equals(instigator)) {
            throw new FusionException("Only the admin who kicked the user is allowed to clear the kick");
         } else if (bannedInfo.getReason() != ChatRoom.BannedInfo.ReasonEnum.KICK) {
            throw new FusionException("You can only clear the kick on a kicked user");
         } else if (this.bannedUsers.remove(target) == null) {
            throw new FusionException("User " + target + " was never kicked from the chat room " + this.chatRoomData.getName());
         } else {
            try {
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               userEJB.decrementChatroomBanCounter(target);
            } catch (Exception var6) {
               log.warn("Unable to log chat room ban", var6);
            }

            this.queueAdminMessage(instigator + " has cleared the last kick made on " + target, (String)null, (String)null);
         }
      }
   }

   private String formatLogMessage(String stmt) {
      StringBuilder msgBuilder = new StringBuilder();
      msgBuilder.append("Room:");
      if (this.chatRoomData != null) {
         msgBuilder.append("[");
         msgBuilder.append(this.chatRoomData.getName());
         msgBuilder.append("]");
      } else {
         msgBuilder.append("null");
      }

      msgBuilder.append(";");
      msgBuilder.append(stmt);
      return msgBuilder.toString();
   }

   private void logWarn(String msg) {
      log.warn(this.formatLogMessage(msg));
   }

   private void logError(String msg, Throwable t) {
      log.error(this.formatLogMessage(msg), t);
   }

   public void startBot(String username, String botName) throws FusionException {
      boolean semaphoreAcquired = false;

      try {
         ChatRoomParticipant participant = this.participants.get(username);
         boolean startByChatRoom = this.chatRoomData.getName().equals(username);
         if (!startByChatRoom && this.participants == null) {
            throw new FusionException("You are no longer in the room");
         }

         this.chatRoomData.verifyBotsAllowed();
         if (participant != null && participant.hasAdminOrModeratorRights()) {
            this.blockBotsUntilTimestamp = 0L;
         } else if (System.currentTimeMillis() < this.blockBotsUntilTimestamp) {
            if (this.blockBotsUntilTimestamp == Long.MAX_VALUE) {
               throw new FusionException("Bots have been blocked in this room.");
            }

            throw new FusionException("Bots have been temporarily blocked. Please wait " + (this.blockBotsUntilTimestamp - System.currentTimeMillis()) / 1000L + "s");
         }

         semaphoreAcquired = this.botSemaphore.tryAcquire();
         if (!semaphoreAcquired) {
            throw new FusionException("Another user is starting or stopping a bot. Please try again later");
         }

         Iterator i$ = this.bots.values().iterator();
         if (i$.hasNext()) {
            BotInstance botInstance = (BotInstance)i$.next();
            throw new FusionException("A bot of type [" + botInstance.displayName + "] is already running");
         }

         this.chatRoomData.verifySpecificBotOnly(botName);
         boolean purgeIfIdle = !startByChatRoom;
         ChatRoomPrx chatRoomPrx = this.objectManager.findChatRoomPrx(this.chatRoomData.getName());
         this.botChannelID = chatRoomPrx.ice_getIdentity().name;
         BotServicePrx botServicePrx = this.objectManager.getRegistryPrx().getLowestLoadedBotService();
         BotInstance newBotInstance = botServicePrx.addBotToChannel(chatRoomPrx, botName, username, purgeIfIdle);
         this.bots.put(newBotInstance.id, newBotInstance);
      } catch (CreateException var16) {
         throw new FusionException("migGames are temporarily unavailable. Please try again later");
      } catch (RemoteException var17) {
         throw new FusionException("migGames are temporarily unavailable. Please try again later");
      } catch (ObjectNotFoundException var18) {
         throw new FusionException("migGames are temporarily unavailable. Please try again later");
      } finally {
         if (semaphoreAcquired) {
            this.botSemaphore.release();
         }

      }

   }

   public void stopBot(String username, String botName) throws FusionException {
      boolean semaphoreAcquired = false;

      try {
         ChatRoomParticipant participant = this.participants.verifyYouAreParticipant(username);
         if (this.bots.size() == 0) {
            throw new FusionException("There is currently no bot running in this room");
         } else if (this.chatRoomData.botID() != null) {
            throw new FusionException("You do not have permission to stop the bot at this time");
         } else {
            semaphoreAcquired = this.botSemaphore.tryAcquire();
            if (!semaphoreAcquired) {
               throw new FusionException("Another user is starting or stopping a bot. Please try again later");
            } else {
               int botsRemoved = 0;
               Iterator i$ = this.bots.values().iterator();

               while(true) {
                  BotInstance botInstance;
                  do {
                     if (!i$.hasNext()) {
                        if (botsRemoved == 0) {
                           throw new FusionException("You do not have permission to stop the bot at this time");
                        }

                        return;
                     }

                     botInstance = (BotInstance)i$.next();
                  } while(!participant.hasAdminOrModeratorRights() && !botInstance.startedBy.equals(username) && this.participants.get(botInstance.startedBy) != null);

                  botInstance.botServiceProxy.removeBot(botInstance.id, false);
                  this.bots.remove(botInstance.id);
                  this.queueAdminMessage("Bot '" + botInstance.displayName + "' has been stopped by " + username, (String)null, (String)null);
                  ++botsRemoved;
               }
            }
         }
      } finally {
         if (semaphoreAcquired) {
            this.botSemaphore.release();
         }

      }
   }

   public void stopAllBots(String username, int timeout) throws FusionException {
      boolean semaphoreAcquired = false;

      try {
         ChatRoomParticipant participant = this.participants.verifyYouAreParticipant(username);
         if (!participant.hasAdminOrModeratorRights()) {
            throw new FusionException("/botstop ! can only be done by admins and moderators");
         }

         semaphoreAcquired = this.botSemaphore.tryAcquire();
         if (!semaphoreAcquired) {
            throw new FusionException("Another user is starting or stopping a bot. Please try again later");
         }

         int botsRemoved = 0;

         for(Iterator i$ = this.bots.values().iterator(); i$.hasNext(); ++botsRemoved) {
            BotInstance botInstance = (BotInstance)i$.next();
            botInstance.botServiceProxy.removeBot(botInstance.id, true);
            this.bots.remove(botInstance.id);
            this.queueAdminMessage("Bot '" + botInstance.displayName + "' has been stopped by " + username, (String)null, (String)null);
         }

         if (timeout > 0) {
            this.blockBotsUntilTimestamp = System.currentTimeMillis() + (long)(timeout * 1000);
            this.queueAdminMessage("Bots may not be started for " + timeout + "s", (String)null, (String)null);
         } else {
            this.blockBotsUntilTimestamp = Long.MAX_VALUE;
            this.queueAdminMessage("Bots may not be started except by owners and moderators", (String)null, (String)null);
         }
      } finally {
         if (semaphoreAcquired) {
            this.botSemaphore.release();
         }

      }

   }

   public void botKilled(String botInstanceID) throws FusionException {
      BotInstance botInstance = (BotInstance)this.bots.remove(botInstanceID);
      if (botInstance != null) {
         this.queueAdminMessage("Bot '" + botInstance.displayName + "' was stopped for being idle too long.", (String)null, (String)null);
      }

   }

   public void sendMessageToBots(String username, String message, long receivedTimestamp) throws FusionException {
      Iterator i$ = this.bots.values().iterator();

      while(i$.hasNext()) {
         BotInstance botInstance = (BotInstance)i$.next();
         botInstance.botServiceProxy.sendMessageToBot(botInstance.id, username, message, receivedTimestamp);
      }

      this.participants.updateLastTimeMessageSent(username);
   }

   public void putBotMessage(String botInstanceID, String username, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      ChatRoomParticipant participant = this.participants.get(username);
      BotInstance botInstance;
      if (participant != null) {
         MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), "[PVT] " + message, emoticonHotKeys);
         botInstance = (BotInstance)this.bots.get(botInstanceID);
         if (botInstance != null) {
            messageData.source = botInstance.displayName;
            messageData.sourceColour = MessageData.SourceTypeEnum.BOT.colorHex();
            messageData.messageColour = 34734;
         }

         ChatRoom.MessageToDispatchTypeEnum type = ChatRoom.MessageToDispatchTypeEnum.TEXT;
         if (displayPopUp) {
            type = ChatRoom.MessageToDispatchTypeEnum.TEXT_AND_POPUP;
         }

         this.queueMessage(messageData.toIceObject(), username, (String)null, type, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
      } else {
         Iterator i$ = this.bots.values().iterator();

         while(i$.hasNext()) {
            botInstance = (BotInstance)i$.next();
            botInstance.botServiceProxy.sendNotificationToBotsInChannel(this.botChannelID, username, BotData.BotCommandEnum.PART.value());
         }
      }

   }

   public void putBotMessageToUsers(String botInstanceID, String[] usernames, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      String[] arr$ = usernames;
      int len$ = usernames.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String username = arr$[i$];

         try {
            this.putBotMessage(botInstanceID, username, message, emoticonHotKeys, displayPopUp);
         } catch (FusionException var11) {
         }
      }

   }

   public void putBotMessageToAllUsers(String botInstanceID, String message, String[] emoticonHotKeys, boolean displayPopUp) throws FusionException {
      MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), message, emoticonHotKeys);
      BotInstance botInstance = (BotInstance)this.bots.get(botInstanceID);
      if (botInstance != null) {
         messageData.source = botInstance.displayName;
         messageData.sourceColour = MessageData.SourceTypeEnum.BOT.colorHex();
         messageData.messageColour = 34734;
      }

      ChatRoom.MessageToDispatchTypeEnum type = ChatRoom.MessageToDispatchTypeEnum.TEXT;
      if (displayPopUp) {
         type = ChatRoom.MessageToDispatchTypeEnum.TEXT_AND_POPUP;
      }

      this.queueMessage(messageData.toIceObject(), (String)null, (String)null, type, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
      this.logMessage((ChatRoomParticipant)null, message);
   }

   public void sendGamesHelpToUser(String username) throws FusionException {
      if (!this.chatRoomData.isAllowBots()) {
         throw new FusionException("No games in this room.");
      } else {
         this.participants.verifyYouAreParticipant(username);
         Iterator i$ = BotChannelHelper.getGames().iterator();

         while(i$.hasNext()) {
            BotData botData = (BotData)i$.next();
            MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), "To start " + botData.getGame() + ", type: /bot " + botData.getCommandName(), (String[])null);
            messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
            this.queueMessage(messageData.toIceObject(), username, (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
         }

         MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), "For help, see: migWorld", (String[])null);
         messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
         this.queueMessage(messageData.toIceObject(), username, (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
      }
   }

   private void removeIdleParticipants() {
      Iterator i$ = this.participants.getAll().iterator();

      while(i$.hasNext()) {
         ChatRoomParticipant participant = (ChatRoomParticipant)i$.next();
         if (participant.isIdle((long)this.userIdleTimeout, (long)this.maxUserDuration)) {
            try {
               this.removeParticipant(participant.getUsername());
            } catch (Exception var4) {
               log.error("Unable to remove idle user [" + participant.getUsername() + "] from chat room [" + this.chatRoomData.getName() + "]", var4);
            }
         }
      }

   }

   private void screenUserForEntrance(UserData userData, ChatRoomParticipant participant) throws FusionException {
      if (this.purging) {
         throw new FusionException("You cannot enter the " + this.chatRoomData.getName() + " room at this time. Please try again later");
      } else {
         UserObject user = new UserObject(userData.username);
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.ENTER_CHATROOM, userData)) {
            throw new FusionException("Please authenticate your migme account to access chat rooms. For more information, please email contact@mig.me");
         } else {
            String mcKeyBlockIP = String.format("%d%s%s", this.chatRoomData.getID(), "/", participant.getIPAddress());
            boolean ipWhitelisted = false;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MULTI_ID_CONTROL_ENABLED) && !participant.hasAdminOrModeratorRights()) {
               String[] ipWhitelist = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MULTI_ID_CONTROL_IP_WHITELIST);
               if (ipWhitelist != null) {
                  String[] arr$ = ipWhitelist;
                  int len$ = ipWhitelist.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     String ip = arr$[i$];
                     if (participant.getIPAddress().equalsIgnoreCase(ip)) {
                        ipWhitelisted = true;
                        break;
                     }
                  }
               }

               if (ipWhitelisted) {
                  log.info(String.format("user %s from whitelisted IP %s allowed into chatroom %s", userData.username, participant.getIPAddress(), this.getRoomData().name));
               } else if (MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BLOCK_IP, mcKeyBlockIP) != null) {
                  log.info(String.format("ip suspended to enter chatroom %s due to IP rate limit exceeded, ip %s, user %s, rate limit %s", this.getRoomData().name, participant.getIPAddress(), userData.username, this.chatRoomData.getRateLimitByIp()));
                  throw new FusionException("Too many users have entered the chat room from your IP. Please try again later");
               }
            }

            if (userData.chatRoomBans != null) {
               int maxAdminBansAllowed = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CHATROOM_BANS);
               if (userData.chatRoomBans >= maxAdminBansAllowed || MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BAN, userData.username) != null) {
                  throw new FusionException("You have been banned from chat rooms");
               }

               int bansBeforeSuspension = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BANS_BEFORE_SUSPENSION);
               if (userData.chatRoomBans == bansBeforeSuspension && MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BAN, userData.username) != null) {
                  throw new FusionException("You have been banned from chat rooms for " + WebCommon.toNiceDuration(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BAN.getCacheTime()));
               }
            }

            GroupData groupData = this.chatRoomData.getGroupData();
            if (!participant.isGlobalAdmin()) {
               if (groupData == null && this.chatRoomData.isOnBannedList(userData.username)) {
                  throw new FusionException("You have been banned from the chat room " + this.chatRoomData.getName());
               }

               ChatRoom.BannedInfo bannedInfo = (ChatRoom.BannedInfo)this.bannedUsers.get(userData.username);
               if (bannedInfo != null) {
                  long currentTime = System.currentTimeMillis();
                  if (currentTime < bannedInfo.getExpiredTime()) {
                     if (bannedInfo.getReason() == ChatRoom.BannedInfo.ReasonEnum.BUMP) {
                        double dblPeriod = (double)(bannedInfo.getExpiredTime() - currentTime) / 60000.0D;
                        if (dblPeriod <= 1.0D) {
                           dblPeriod = 1.0D;
                        }

                        long period = (long)Math.ceil(dblPeriod);
                        throw new FusionException("Sorry you have been bumped out of the chat-room for violating the rules of this chat-room. Please try again after " + period + (period > 1L ? " minutes" : " minute") + ".");
                     }

                     throw new FusionException("You were recently " + bannedInfo.getReason().getPrevAction() + " from the " + this.chatRoomData.getName() + " chat room. You may not rejoin at this time");
                  }

                  this.bannedUsers.remove(userData.username);
               }
            }

            User userEJB = null;
            this.chatRoomData.verifyMigLevel(userData, participant.hasAdminOrModeratorRights());
            GroupMemberData groupMemberData = null;
            if (groupData != null) {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                  try {
                     groupMemberData = user.getGroupMember(groupData.id);
                  } catch (DAOException var21) {
                     log.error(String.format("DAO: Failed to get GroupMemberData for user:%s, group:%s", user, groupData.id));
                     throw new FusionException("Unable to verify group membership");
                  }
               } else {
                  try {
                     if (userEJB == null) {
                        userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                     }

                     groupMemberData = userEJB.getGroupMember(userData.username, groupData.id);
                  } catch (Exception var20) {
                     throw new FusionException("Unable to verify group membership");
                  }
               }

               if (groupMemberData == null && groupData != null && groupData.type != GroupData.TypeEnum.OPEN && (groupData.type == GroupData.TypeEnum.CLOSED || !groupData.allowNonMembersToJoinRooms) && !participant.isGlobalAdmin()) {
                  throw new FusionException("You must be a member of the " + groupData.name + " group to enter the " + this.chatRoomData.getName() + " chat room");
               }

               if (groupMemberData != null && groupMemberData.status == GroupMemberData.StatusEnum.BANNED) {
                  throw new FusionException("You have been banned from the group " + groupData.name);
               }

               if (MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.GROUP_SUSPENSION, groupData.id + "/" + participant.getUsername()) != null && !participant.isGlobalAdmin()) {
                  throw new FusionException("You are temporarily suspended from all the chat rooms in " + groupData.name + " group for " + WebCommon.toNiceDuration((long)this.grpAdminSuspendDuration));
               }

               if (groupMemberData == null) {
                  try {
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                        try {
                           if (user.isUserBlackListedInGroup(groupData.id)) {
                              throw new FusionException("You have been blacklisted from the group " + groupData.name + ". Please contact the admin or a moderator of this group.");
                           }
                        } catch (DAOException var17) {
                           log.error(String.format("DAO: Failed to check isUserBlackListedInGroup for user:%s, group:%s", user, groupData.id), var17);
                           throw new FusionException("You have been blacklisted from the group " + groupData.name + ". Please contact the admin or a moderator of this group.");
                        }
                     } else {
                        Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
                        if (messageEJB.isUserBlackListedInGroup(userData.username, groupData.id)) {
                           throw new FusionException("You have been blacklisted from the group " + groupData.name + ". Please contact the admin or a moderator of this group.");
                        }
                     }
                  } catch (CreateException var18) {
                     log.error("Failed to check blacklist: " + var18);
                  } catch (RemoteException var19) {
                     log.error("Failed to check blacklist: " + var19);
                  }
               }
            }

            if (this.isStadium()) {
               participant.setGroupAdmin(groupMemberData != null && (groupMemberData.isAdministrator() || groupMemberData.isModerator()));
            } else if (groupData != null) {
               participant.setGroupAdmin(groupMemberData != null && groupMemberData.isAdministrator());
               participant.setGroupMod(groupMemberData != null && groupMemberData.isModerator());
            }

            if (!participant.hasAdminOrModeratorRights()) {
               synchronized(this.chatRoomData) {
                  if (this.chatRoomData.isLocked()) {
                     if (!this.chatroomEntrantSnapshot.isCurrentSnapshotRunning() || !this.chatroomEntrantSnapshot.hasLockExpired()) {
                        throw new FusionException("[" + this.chatRoomData.getLocker() + "] has locked the room.");
                     }

                     this.chatRoomData.unlock();
                     this.chatroomEntrantSnapshot.clearSnapshot();
                  }
               }
            }

            Iterator i$ = this.recentlyLeftUsers.entrySet().iterator();

            while(i$.hasNext()) {
               Entry<String, Long> e = (Entry)i$.next();
               if (System.currentTimeMillis() - (Long)e.getValue() > (long)this.reenterInterval) {
                  this.recentlyLeftUsers.remove(e.getKey());
               }
            }

            if (!participant.hasAdminOrModeratorRights() && this.recentlyLeftUsers.containsKey(userData.username)) {
               throw new FusionException("You have recently left the " + this.chatRoomData.getName() + " chat room. You may not rejoin at this time");
            } else {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.USE_IP_RATELIMIT) && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MULTI_ID_CONTROL_ENABLED) && !participant.hasAdminOrModeratorRights() && !StringUtil.isBlank(this.chatRoomData.getRateLimitByIp()) && !ipWhitelisted) {
                  try {
                     String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.OVERRIDE_IP_RATELIMIT);
                     if (StringUtil.isBlank(rateLimit)) {
                        rateLimit = this.chatRoomData.getRateLimitByIp();
                     }

                     MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHATROOM_ENTRY.toString(), mcKeyBlockIP, rateLimit);
                  } catch (MemCachedRateLimiter.LimitExceeded var15) {
                     int secondsToBlock = this.chatRoomData.getSecondsToBlock();
                     MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BLOCK_IP, mcKeyBlockIP, "1", (long)(secondsToBlock * 1000));
                     log.info(String.format("rate limit of ip for chatroom %s has been exceeded, blocking for %d seconds, ip %s, user %s, rate limit %s", this.chatRoomData.getName(), secondsToBlock, participant.getIPAddress(), userData.username, this.chatRoomData.getRateLimitByIp()));
                     throw new FusionException("Too many users have entered the chat room from your IP. Please try again later");
                  } catch (MemCachedRateLimiter.FormatError var16) {
                     log.error(String.format("incorrect rate limit by ip '%s' for chatroom %s, error '%s', ignoring it...", this.chatRoomData.getRateLimitByIp(), this.chatRoomData.getName(), var16.getMessage()));
                  }
               }

            }
         }
      }
   }

   private void sendGreetingMessagesAsync(ChatRoomParticipant participant, ChatRoomData chatRoomData) throws FusionException {
      if (chatRoomData.description != null && chatRoomData.description.length() > 0) {
         this.queueAdminMessage(chatRoomData.description, participant.getUsername(), (String)null, MIMETYPE_DESCRIPTION, EMPTY_MIMETYPE_DATA);
      }

      String amd;
      ChatRoom.MimeDataJSONObject mimeData;
      if (chatRoomData.isUserOwned()) {
         amd = "This room is managed by " + chatRoomData.getCreator();
         mimeData = new ChatRoom.MimeDataJSONObject();
         mimeData.put("managedBy", chatRoomData.getCreator());
         this.queueAdminMessage(amd, participant.getUsername(), (String)null, MIMETYPE_MANAGED_BY, mimeData.toString());
      }

      if (participant.hasAdminOrModeratorRights() && chatRoomData.isLocked()) {
         amd = "This room is currently locked by " + chatRoomData.getLocker();
         mimeData = new ChatRoom.MimeDataJSONObject();
         mimeData.put("lockedBy", chatRoomData.getLocker());
         this.queueAdminMessage(amd, participant.getUsername(), (String)null, MIMETYPE_LOCKED, mimeData.toString());
      }

      ChatRoom.MimeDataJSONObject mimeData;
      String helpCommandInfoMessage;
      if (chatRoomData.isStadium()) {
         if (!participant.hasAdminOrModeratorRights()) {
            amd = "This room is read-only. Messages you write will only be seen by moderators of this room";
            this.queueAdminMessage(amd, participant.getUsername(), (String)null, MIMETYPE_STADIUM, EMPTY_MIMETYPE_DATA);
         }
      } else {
         amd = StringUtil.join((Object[])this.getParticipants(participant.getUsername()), ",");
         if (amd.length() == 0) {
            amd = participant.getUsername();
         } else {
            amd = participant.getUsername() + ", " + amd;
         }

         helpCommandInfoMessage = "Currently in the room: " + amd;
         mimeData = new ChatRoom.MimeDataJSONObject();
         JSONArray participantArray = new JSONArray();
         String[] arr$ = this.getParticipants(participant.getUsername());
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String participantName = arr$[i$];
            participantArray.put(participantName);
         }

         mimeData.put("participants", participantArray);
         this.queueAdminMessage(helpCommandInfoMessage, participant.getUsername(), (String)null, MIMETYPE_PARTICIPANTS, mimeData.toString());
      }

      int minClientVersion;
      if (chatRoomData.isAnnouncementOn()) {
         minClientVersion = SystemProperty.getInt("ChatroomAnnouncementColor", 7798784);
         MessageData announceMessageData = this.formatAnnounceMessage(chatRoomData.getAnnounceMessage(), minClientVersion);
         mimeData = new ChatRoom.MimeDataJSONObject();
         mimeData.put("announcer", chatRoomData.getAnnouncer());
         mimeData.put("message", chatRoomData.getAnnounceMessage());
         announceMessageData.mimeType = MIMETYPE_ANNOUNCE;
         announceMessageData.mimeTypeData = mimeData.toString();
         this.queueMessage(announceMessageData.toIceObject(), participant.getUsername(), (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.HELP_COMMAND_INFO_MESSAGE_ENABLED)) {
         minClientVersion = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.NEW_TAB_CLIENT_VERSION_MIN);
         if (participant.getClientVersionIce() >= minClientVersion) {
            helpCommandInfoMessage = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.HELP_COMMAND_INFO_MESSAGE);
            if (!StringUtil.isBlank(helpCommandInfoMessage)) {
               this.queueAdminMessage(helpCommandInfoMessage, participant.getUsername(), (String)null, MIMETYPE_HELP, EMPTY_MIMETYPE_DATA);
            }
         }
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.CHATROOM_WELCOME_MESSAGE_ENABLED)) {
         try {
            amd = null;
            AlertMessageData amd;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_MESSAGE_DAO)) {
               amd = DAOFactory.getInstance().getMessageDAO().getLatestAlertMessage(participant.getClientVersionIce(), AlertMessageData.TypeEnum.CHAT_ROOM_WELCOME_MESSAGE, participant.getCountryID(), participant.getLastLoginDate(), (AlertContentType)null, participant.getDeviceTypeAsInt());
            } else {
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               amd = userEJB.getLatestAlertMessage(participant.getClientVersionIce(), AlertMessageData.TypeEnum.CHAT_ROOM_WELCOME_MESSAGE, participant.getCountryID(), participant.getLastLoginDate(), (AlertContentType)null, participant.getDeviceTypeAsInt());
            }

            if (amd != null) {
               ChatRoomWelcomeMessageData announceMessageData = new ChatRoomWelcomeMessageData(chatRoomData.name, amd.content);
               announceMessageData.mimeType = MIMETYPE_WELCOME;
               announceMessageData.mimeTypeData = EMPTY_MIMETYPE_DATA;
               this.queueMessage(announceMessageData.toIceObject(), participant.getUsername(), (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
            }
         } catch (Exception var11) {
            log.error("Exception occured while getting chat welcome message." + var11.getMessage(), var11);
            throw new FusionException("Unexpected Exception:" + var11.getMessage());
         }
      }

   }

   private void queueCheckedEntryExitAdminMessage(MessageData messageData, final String username, boolean isEntering) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.USE_ENTER_EXIT_MESSAGE_CHECKS)) {
         final ChatRoom.MessageToDispatch mtd = isEntering ? new ChatRoom.EnterMessageToDispatch(messageData.toIceObject(), username) : new ChatRoom.ExitMessageToDispatch(messageData.toIceObject(), username);
         int enterExitDelay = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.ENTER_EXIT_MESSAGE_DELAY_IN_MS);
         if (enterExitDelay > 0) {
            this.objectManager.getDistributionService().schedule(new Runnable() {
               public void run() {
                  ChatRoom.this.queueMessage((ChatRoom.MessageToDispatch)mtd, username, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
               }
            }, (long)enterExitDelay, TimeUnit.MILLISECONDS);
         } else {
            this.queueMessage((ChatRoom.MessageToDispatch)mtd, username, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
         }
      } else {
         this.queueMessage(messageData.toIceObject(), (String)null, username, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN));
      }

   }

   private void queueEntryExitAdminMessage(ChatRoomParticipant participant, boolean isEntering) throws FusionException {
      String username = participant.getUsername();
      int userLevel = participant.getLevel();
      String usernameWithLevel = userLevel == 0 ? username : username + "[" + userLevel + "]";
      String suffix = isEntering ? " has entered" : " has left";
      MessageData messageData;
      if (Painter.isClean(username, participant.getUserID())) {
         messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), usernameWithLevel + suffix, (String[])null);
      } else {
         String emoticon = "(paintwars-paintemoticon)";
         String message = this.formatUserNameWithLevel(username) + " " + emoticon + suffix;
         String[] emoticonKey = new String[]{emoticon};
         messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), message, emoticonKey);
      }

      ChatRoom.MimeDataJSONObject mimeData = new ChatRoom.MimeDataJSONObject();
      mimeData.put("username", username);
      mimeData.put("level", userLevel);
      messageData.mimeType = isEntering ? MIMETYPE_PARTICPANT_ENTER : MIMETYPE_PARTICPANT_EXIT;
      messageData.mimeTypeData = mimeData.toString();
      this.queueCheckedEntryExitAdminMessage(messageData, username, isEntering);
   }

   private void queueAdminMessage(String message, String usernameToReceive, String usernameToExclude) {
      if (message != null && message.length() > 0) {
         MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), message, (String[])null);
         if (!this.queueMessage(messageData.toIceObject(), usernameToReceive, usernameToExclude, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN))) {
            log.warn(String.format("failed to queueAdminMessage to chatroom. CurrentSize[%d]. UserToRec[%s] UserToExcl[%s] Chatroom[%s]", this.messageQueue.size(), usernameToReceive, usernameToExclude, this.chatRoomData.getName()));
         }
      }

   }

   private void queueAdminMessage(String message, String usernameToReceive, String usernameToExclude, String mimeType, String mimeData) {
      if (message != null && message.length() > 0) {
         MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), message, (String[])null);
         messageData.mimeType = mimeType;
         messageData.mimeTypeData = mimeData;
         if (!this.queueMessage(messageData.toIceObject(), usernameToReceive, usernameToExclude, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS_ADMIN))) {
            log.warn(String.format("failed to queueAdminMessage to chatroom. CurrentSize[%d]. UserToRec[%s] UserToExcl[%s] Chatroom[%s]", this.messageQueue.size(), usernameToReceive, usernameToExclude, this.chatRoomData.getName()));
         }
      }

   }

   private boolean queueMessage(ChatRoom.MessageToDispatch mtd, String username, long addTimeoutInMillis) {
      if (!this.messageQueue.offer(mtd, addTimeoutInMillis)) {
         log.warn("could not queue enter message to chatroom. CurrentSize[" + this.messageQueue.size() + "]. User[" + username + "] Chatroom[" + this.chatRoomData.getName() + "]");
         return false;
      } else {
         if (this.messageSemaphore.tryAcquire()) {
            this.objectManager.getDistributionService().execute(this.messageDispatcher);
         }

         return true;
      }
   }

   private boolean queueMessage(MessageDataIce message, String usernameToReceive, String usernameToExclude, ChatRoom.MessageToDispatchTypeEnum type, long addTimeoutInMillis) {
      Object mtd;
      if (usernameToReceive != null) {
         mtd = new ChatRoom.MessageToDispatchToSingleUser(message, type, usernameToReceive);
      } else if (usernameToExclude != null) {
         mtd = new ChatRoom.MessageToDispatchToRoomExcludingUser(message, type, usernameToExclude);
      } else {
         mtd = new ChatRoom.MessageToDispatchToRoom(message, type);
      }

      if (!this.messageQueue.offer(mtd, addTimeoutInMillis)) {
         log.warn("could not queue message to chatroom. CurrentSize[" + this.messageQueue.size() + "]. User[" + message.source + "] Chatroom[" + this.chatRoomData.getName() + "]");
         return false;
      } else {
         if (this.messageSemaphore.tryAcquire()) {
            this.objectManager.getDistributionService().execute(this.messageDispatcher);
         }

         return true;
      }
   }

   private void dispatchMessages() {
      long maxDispatchWindowDurationInMs = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_DISPATCH_WINDOW_MAX_DURATION_IN_MILLIS);
      int maxMessagesPerDispatchWindow = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_DISPATCH_WINDOW_MAX_MESSAGE);

      try {
         int messagesSentThisRound = 0;
         long roundStartTime = System.currentTimeMillis();

         for(ChatRoom.MessageToDispatch msg = (ChatRoom.MessageToDispatch)this.messageQueue.takeIfAvailable(); msg != null; msg = (ChatRoom.MessageToDispatch)this.messageQueue.takeIfAvailable()) {
            msg.dispatch();
            ++messagesSentThisRound;
            this.lastTimeMessageSent = System.currentTimeMillis();
            long currentDuration = System.currentTimeMillis() - roundStartTime;
            if (currentDuration > maxDispatchWindowDurationInMs || messagesSentThisRound > maxMessagesPerDispatchWindow) {
               log.warn("Dispatch window limit exceeded. chatroom[" + this.chatRoomData.getName() + "] maxDuration[" + maxDispatchWindowDurationInMs + "] maxMessages[" + maxMessagesPerDispatchWindow + "] curDuration[" + currentDuration + "] curMessageQueueSize[" + this.messageQueue.size() + "]");
               break;
            }
         }
      } finally {
         this.messageSemaphore.release();
         if (!this.messageQueue.isEmpty() && this.messageSemaphore.tryAcquire()) {
            this.objectManager.getDistributionService().execute(this.messageDispatcher);
         }

      }

   }

   private void kickParticipant(ChatRoomParticipant instigator, ChatRoomParticipant target, String reason) throws FusionException {
      String kickedByMessage = "";
      String implication = "";
      long duration = (long)this.banDuration;
      if (instigator != null) {
         if (instigator.isGlobalAdmin()) {
            duration = (long)this.globalAdminBanDuration;
            kickedByMessage = " by administrator " + this.formatUserNameWithLevel(instigator);
            implication = " You are suspended from this chatroom for " + WebCommon.toNiceDuration(duration);
            if (target.getNumOfPriorKicks() + 1 != 2 && target.getNumOfPriorKicks() + 1 != 5) {
               if (target.getNumOfPriorKicks() + 1 == SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BANS_BEFORE_SUSPENSION)) {
                  implication = " You have been suspended from all the chatrooms for " + WebCommon.toNiceDuration(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BAN.getCacheTime());
               } else if (target.getNumOfPriorKicks() + 1 >= SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CHATROOM_BANS)) {
                  implication = " You are permanently banned from entering chatrooms.";
               }
            } else {
               duration = (long)(this.globalAdminBanDuration * 2);
            }

            try {
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               userEJB.bannedFromChatRoom(this.chatRoomData.getName(), instigator.getUsername(), target.getUsername(), reason);
            } catch (Exception var12) {
               log.warn("Unable to log chat room ban", var12);
            }
         } else if (instigator.isGroupAdmin()) {
            duration = (long)this.grpAdminSuspendDuration;
            kickedByMessage = " by group administrator " + this.formatUserNameWithLevel(instigator);
            implication = " You have been suspended from all the chatrooms in this group for " + WebCommon.toNiceDuration(duration);
            this.suspendGroupMember(target.getUsername(), duration);
         } else if (instigator.isGroupMod()) {
            duration = (long)this.grpModSuspendDuration;
            kickedByMessage = " by group moderator " + this.formatUserNameWithLevel(instigator);
            implication = " You have been suspended from all the chatrooms in this group for " + WebCommon.toNiceDuration(duration);
            this.suspendGroupMember(target.getUsername(), duration);
         } else if (instigator.isRoomOwner()) {
            duration = (long)this.adminBanDuration;
            kickedByMessage = " by chatroom admin " + this.formatUserNameWithLevel(instigator);
            implication = " You have been suspended from this chatroom for " + WebCommon.toNiceDuration(duration);

            try {
               Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               messageEJB.banUserFromRoom(instigator.getUsername(), this.chatRoomData.getName(), target.getUsername());
               implication = " You are permanently banned from entering this chatroom.";
            } catch (Exception var11) {
               log.warn("Unable to perma ban " + target.getUsername() + " from room: " + this.chatRoomData.getName(), var11);
            }
         } else {
            if (!this.isModerator(instigator.getUsername())) {
               throw new FusionException(instigator.getUsername() + " doesn't have admin/moderator rights to kick users");
            }

            duration = (long)this.adminBanDuration;
            kickedByMessage = " by chatroom moderator " + this.formatUserNameWithLevel(instigator);
            implication = " You have been suspended from this chatroom for " + WebCommon.toNiceDuration(duration);
         }
      }

      if (reason != null && reason.length() != 0) {
         reason = " (" + reason + ").";
      } else {
         reason = ".";
      }

      this.participants.remove(target.getUsername());
      this.bannedUsers.put(target.getUsername(), new ChatRoom.BannedInfo(System.currentTimeMillis() + duration, ChatRoom.BannedInfo.ReasonEnum.KICK, instigator == null ? null : instigator.getUsername()));
      Iterator i$ = this.bannedUsers.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, ChatRoom.BannedInfo> e = (Entry)i$.next();
         ChatRoom.BannedInfo bannedInfo = (ChatRoom.BannedInfo)e.getValue();
         if (bannedInfo != null && System.currentTimeMillis() > bannedInfo.getExpiredTime()) {
            this.bannedUsers.remove(e.getKey());
         }
      }

      String targetUNameWithLevel = this.formatUserNameWithLevel(target);
      this.queueAdminMessage(targetUNameWithLevel + " has been kicked" + kickedByMessage + reason, (String)null, (String)null);
      ChatRoomParticipant source = instigator == null ? target : instigator;
      this.logMessage(source, "<" + target.getUsername() + " was kicked" + kickedByMessage + reason + ">");
      auditLog.info(target.getUsername() + " has been kicked" + kickedByMessage + reason + " from room [" + this.chatRoomData.getName() + "]. Prior recorded kicks: " + target.getNumOfPriorKicks());
      MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), "You have been kicked" + kickedByMessage + reason + implication, (String[])null);
      target.putMessage(messageData.toIceObject());
   }

   private void suspendGroupMember(String username, long duration) {
      GroupData groupData = this.chatRoomData.getGroupData();

      try {
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GROUP_SUSPENSION, groupData.id + "/" + username, "", duration);
      } catch (Exception var6) {
         log.warn(username + " suspended from room [" + this.chatRoomData.getName() + "], but suspending from group [" + groupData.name + "] failed!");
      }

   }

   private void endKickUserVote() {
      synchronized(this.kickUserVoteMonitor) {
         this.kickUserVote = null;
      }
   }

   private void logMessage(ChatRoomParticipant participant, String message) {
      if (ObjectCache.logMessagesToFile) {
         try {
            this.objectManager.logMessage(MessageToLog.TypeEnum.CHATROOM, participant == null ? 0 : participant.getCountryID(), participant == null ? this.chatRoomData.getName() : participant.getUsername(), this.chatRoomData.getName(), this.participants.size(), message);
         } catch (LocalException var4) {
            this.logWarn("Unable to log chat room message to the MessageLogger. Exception: " + var4.toString());
         } catch (Exception var5) {
            this.logError("Unable to log chat room message to the MessageLogger. Exception: " + var5.toString(), var5);
         }
      }

   }

   private void updateChatRoomDetailInDB() {
      if (this.chatRoomData.updateDBAccessed(this.dbUpdateInterval)) {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            messageEJB.chatRoomAccessed(this.chatRoomData.getID(), this.chatRoomData.getName(), this.chatRoomData.getPrimaryCountryID(), this.chatRoomData.getSecondaryCountryID());
         } catch (Exception var2) {
            log.warn("Unable to update chat room data [" + this.chatRoomData.getName() + "]", var2);
         }

      }
   }

   private String formatUserNameWithLevel(String username) {
      return formatUserNameWithLevel(username, (Integer)null);
   }

   private String formatUserNameWithLevel(ChatRoomParticipant p) {
      return formatUserNameWithLevel(p.getUsername(), p.getUserID());
   }

   private static String formatUserNameWithLevel(String username, Integer userid) {
      try {
         int userReputation = MemCacheOrEJB.getUserReputationLevel(username, userid);
         return username + " [" + userReputation + "]";
      } catch (Exception var3) {
         return username;
      }
   }

   public boolean isLocked() {
      synchronized(this.chatRoomData) {
         return this.chatRoomData.isLocked();
      }
   }

   public void lock(String locker) throws FusionException {
      ChatRoomParticipant lockerParticipant = this.participants.verifyYouAreParticipant(locker);
      if (!lockerParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Only admins and moderators may lock this room");
      } else {
         this.chatRoomData.tryLock(locker);
         this.queueAdminMessage("This chat room has been locked by " + locker + ". Only admins and moderators may join and/or unlock this chat room.", (String)null, (String)null);
         this.logMessage(lockerParticipant, "<chat room has been locked by " + locker + ">");
         auditLog.info("chat room [" + this.chatRoomData.getName() + "] has been locked by " + locker);
      }
   }

   public void unlock(String unlocker) throws FusionException {
      ChatRoomParticipant unlockerParticipant = this.participants.verifyYouAreParticipant(unlocker);
      if (!unlockerParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Only admins and moderators may unlock this room");
      } else if (this.chatroomEntrantSnapshot.isCurrentSnapshotRunning()) {
         if (this.chatroomEntrantSnapshot.hasLockExpired()) {
            this.chatRoomData.unlock();
            this.chatroomEntrantSnapshot.clearSnapshot();
            throw new FusionException("This chat room is not locked. You can not unlock an unlocked room.");
         } else {
            throw new FusionException("Please wait until the list command lockout period has expired");
         }
      } else {
         String locker = this.chatRoomData.tryUnlock();
         this.queueAdminMessage("This chat room has been unlocked by " + unlocker + " (previously locked by " + locker + ").", (String)null, (String)null);
         this.logMessage(unlockerParticipant, "<chat room has been unlocked by " + unlocker + " (previously locked by " + locker + ")>");
         auditLog.info("chat room [" + this.chatRoomData.getName() + "] has been unlocked by " + unlocker + " (previously locked by " + locker + ")");
      }
   }

   private MessageData formatAnnounceMessage(String msg, int messageColor) {
      String message = String.format("%s Announcement: <<%s>>", "(announce)", msg);
      MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), message, new String[]{"(announce)"});
      messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
      messageData.messageColour = messageColor != -1 ? messageColor : 7798784;
      return messageData;
   }

   private void turnAnnouncementOff(ChatRoomParticipant participant, boolean issuedByUser) {
      if (this.announceTimer != null) {
         this.announceTimer.cancel();
         this.announceTimer = null;
      }

      String curAnnouncer = this.chatRoomData.getAnnouncer();
      String msg;
      if (issuedByUser) {
         msg = "[" + participant.getUsername() + "] has turned off the announcement made previously by [" + curAnnouncer + "].";
      } else {
         msg = "The announcement made previously by [" + curAnnouncer + "] has been turned off because the room is empty.";
      }

      if (issuedByUser) {
         this.queueAdminMessage(msg, (String)null, (String)null);
      }

      this.logMessage(participant, "<" + msg + ">");
      auditLog.info(msg + " chat room [" + this.chatRoomData.getName() + "]");
      this.chatRoomData.setAnnouncementOff();
   }

   public void announceOff(String announcer) throws FusionException {
      ChatRoomParticipant announcerParticipant = this.participants.verifyYouAreParticipant(announcer);
      if (!announcerParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Only admins and moderators may do an announce in this room");
      } else {
         synchronized(this.chatRoomData) {
            if (!this.chatRoomData.isAnnouncementOn()) {
               throw new FusionException("There is no announcement in this room");
            } else {
               this.turnAnnouncementOff(announcerParticipant, true);
            }
         }
      }
   }

   public void announceOn(String announcer, String announceMessage, int waitTime) throws FusionException {
      ChatRoomParticipant announcerParticipant = this.participants.verifyYouAreParticipant(announcer);
      if (!announcerParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Only admins and moderators may do an announce in this room");
      } else {
         synchronized(this.chatRoomData) {
            if (this.chatRoomData.isAnnouncementOn()) {
               throw new FusionException("There is already one announcement made by [" + this.chatRoomData.getAnnouncer() + "]. Please use /announce off to turn it off first.");
            } else {
               if (this.announceTimer != null) {
                  this.announceTimer.cancel();
               }

               this.chatRoomData.setAnnouncementOn(announcer, announceMessage);
               int messageColor = SystemProperty.getInt("ChatroomAnnouncementColor", 7798784);
               if (waitTime > 0) {
                  this.announceTimer = new Timer("ChatRoom Announcer " + this.getRoomData().name, true);
                  ChatRoom.AnnounceTask announceTask = new ChatRoom.AnnounceTask(announceMessage, messageColor, this);
                  this.announceTimer.scheduleAtFixedRate(announceTask, (long)(waitTime * 1000), (long)(waitTime * 1000));
               }

               String msg;
               if (waitTime > 0) {
                  msg = "[" + announcer + "] has initiated an announcement \"" + announceMessage + "\" for every " + waitTime + " seconds";
               } else {
                  msg = "[" + announcer + "] has initiated an one-time announcement \"" + announceMessage + "\"";
               }

               this.queueAdminMessage(msg + ".", (String)null, (String)null);
               this.queueMessage(this.formatAnnounceMessage(announceMessage, messageColor).toIceObject(), (String)null, (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS));
               this.logMessage(announcerParticipant, "<" + msg + ">");
               auditLog.info(msg + " in chat room [" + this.chatRoomData.getName() + "]");
            }
         }
      }
   }

   public void adminAnnounce(String announceMessage, int waitTime) throws FusionException {
      try {
         synchronized(this.chatRoomData) {
            String announcer;
            if (this.chatRoomData.isAnnouncementOn()) {
               announcer = this.chatRoomData.getAnnouncer();
               String msg = "The announcement made previously by [" + announcer + "] has been turned off.";
               this.queueAdminMessage(msg, (String)null, (String)null);
               auditLog.info(msg + " chat room [" + this.chatRoomData.getName() + "]");
               this.chatRoomData.setAnnouncementOff();
            }

            if (this.announceTimer != null) {
               this.announceTimer.cancel();
            }

            announcer = SystemProperty.get("AdminChatroomAnnouncer", "migme");
            int messageColor = SystemProperty.getInt("AdminChatroomAnnouncementColor", 7798784);
            this.chatRoomData.setAnnouncementOn(announcer, announceMessage);
            if (waitTime > 0) {
               this.announceTimer = new Timer("ChatRoom Announcer " + this.getRoomData().name, true);
               ChatRoom.AnnounceTask announceTask = new ChatRoom.AnnounceTask(announceMessage, messageColor, this);
               this.announceTimer.scheduleAtFixedRate(announceTask, (long)(waitTime * 1000), (long)(waitTime * 1000));
            }

            String msg;
            if (waitTime > 0) {
               msg = "[" + announcer + "] has initiated an announcement \"" + announceMessage + "\" for every " + waitTime + " seconds";
            } else {
               msg = "[" + announcer + "] has initiated an one-time announcement \"" + announceMessage + "\"";
            }

            this.queueAdminMessage(msg + ".", (String)null, (String)null);
            this.queueMessage(this.formatAnnounceMessage(announceMessage, messageColor).toIceObject(), (String)null, (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS));
            auditLog.info(msg + " in chat room [" + this.chatRoomData.getName() + "]");
         }
      } catch (Exception var9) {
         throw new FusionException(var9.getMessage());
      }
   }

   private boolean isGroupLinkedChatroom() {
      return this.chatRoomData.hasGroupData();
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy) throws FusionException {
      return this.emoteCommandStates.executeEmoteCommandWithState(emoteCommand, message, sessionProxy, (ChatSourceRoom)this);
   }

   public void submitGiftAllTask(int giftId, String giftMessage, MessageDataIce message) throws FusionException {
      if (this.purging) {
         throw new FusionException("Chatroom marked for purging...");
      } else {
         ChatRoomParticipant participant = this.participants.get(message.username);
         if (participant == null) {
            throw new FusionException(message.username + " has left the chat");
         } else {
            GiftAllTask task = participant.createGiftAllTask(giftId, giftMessage, new MessageData(message), this, this.objectManager);
            this.objectManager.getDistributionService().execute(task);
         }
      }
   }

   public void updateExtraData(ChatRoomDataIce newChatRoomDataWithExtraData) {
      this.chatRoomData.updateExtraData(newChatRoomDataWithExtraData);
   }

   public void updateGroupModeratorStatus(String username, boolean promote) {
      ChatRoomParticipant user = this.participants.get(username);
      if (user != null) {
         user.setGroupMod(promote);
         if (log.isDebugEnabled()) {
            log.debug("Chatroom [" + this.chatRoomData.getName() + "]-user [" + username + "] moderator status is changed to " + promote);
         }
      }

      String announcement;
      if (promote) {
         announcement = String.format("Congratulations! '%s' is now a group moderator!", username);
      } else {
         announcement = String.format("'%s' is no longer a group moderator", username);
      }

      try {
         this.putSystemMessage(announcement, (String[])null);
      } catch (Exception var6) {
         log.warn("Unable to send announcement:[" + announcement + "]" + var6.getMessage());
      }

   }

   public void bumpUser(String instigator, String target) throws FusionException {
      ChatRoomParticipant instigatorParticipant = this.participants.verifyYouAreParticipant(instigator, ErrorCause.EmoteCommandError.INSTIGATOR_NOT_IN_CHATROOM);
      if (!instigatorParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Bumps are reserved only for owners, admins and moderators");
      } else {
         instigatorParticipant.verifyClientMeetsMinVersion("/bump");
         ChatRoomParticipant targetParticipant = this.participants.verifyYouAreParticipant(target, ErrorCause.EmoteCommandError.TARGET_NOT_IN_CHATROOM);
         if (targetParticipant.hasAdminOrModeratorRights()) {
            throw new FusionException("You cannot bump owners, admins and moderators");
         } else {
            synchronized(this.adminKickMonitor) {
               if ((targetParticipant = this.participants.get(target)) == null) {
                  throw new FusionException(target + " is no longer in the chat room " + this.chatRoomData.getName());
               }

               this.bannedUsers.put(target, new ChatRoom.BannedInfo(System.currentTimeMillis() + (long)(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.BUMP_DURATION_SEC) * 1000), ChatRoom.BannedInfo.ReasonEnum.BUMP, instigator));
               this.removeParticipant(target);
            }

            auditLog.info(instigator + " has bumped " + target + " from " + this.chatRoomData.getName());
            this.logMessage(instigatorParticipant, "has bumped " + target + " from " + this.chatRoomData.getName());
            if (targetParticipant != null) {
               String chatRoomName = StringUtil.truncateWithEllipsis(this.chatRoomData.getName(), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.MAX_CHATROOMNAME_LENGTH_DISPLAY));
               MessageData messageData = MessageData.newChatRoomMessage(this.chatRoomData.getName(), String.format("You have just been bumped out the chatroom '%s' due to violating the rules of this chatroom by administrator %s", chatRoomName, instigator), (String[])null);
               targetParticipant.putMessage(messageData.toIceObject());
            }

            this.queueAdminMessage(target + " has been bumped by administrator " + instigator, (String)null, (String)null);
         }
      }
   }

   public void warnUser(String instigator, String target, String message) throws FusionException {
      ChatRoomParticipant instigatorParticipant = this.participants.verifyYouAreParticipant(instigator, ErrorCause.EmoteCommandError.INSTIGATOR_NOT_IN_CHATROOM);
      if (!instigatorParticipant.hasAdminOrModeratorRights()) {
         throw new FusionException("Issuing warnings are reserved only for owners, admins and moderators");
      } else {
         instigatorParticipant.verifyClientMeetsMinVersion("/warn");
         ChatRoomParticipant targetParticipant = this.participants.verifyIsParticipant(target, ErrorCause.EmoteCommandError.TARGET_NOT_IN_CHATROOM);
         if (targetParticipant.hasAdminOrModeratorRights()) {
            throw new FusionException("You cannot issue warnings to owners, admins and moderators");
         } else {
            auditLog.info(instigator + " has warned " + target + " in " + this.chatRoomData.getName() + ".Message: <" + message + ">");
            this.logMessage(instigatorParticipant, "has warned " + target + " in " + this.chatRoomData.getName() + ".Message: <" + message + ">");
            ClientType device = targetParticipant.getDeviceType();
            boolean isWeb = false;
            switch(device) {
            case AJAX1:
            case AJAX2:
               isWeb = true;
               break;
            default:
               isWeb = false;
            }

            StringBuilder warningMessage = new StringBuilder("[PVT]");
            if (StringUtil.isBlank(message)) {
               warningMessage.append("Your chatroom moderator has warned you. Do adhere to chatroom Do's and Don'ts at all times.");
            } else {
               warningMessage.append(String.format("Your chatroom moderator has a warning message for you: %s. Do adhere to chatroom Do's and Don'ts at all times.", message));
            }

            if (!isWeb) {
               warningMessage.append("Go to migWorld to find out more.");
            }

            auditLog.info(instigator + " has warned " + target + " from " + this.chatRoomData.getName());
            this.logMessage(instigatorParticipant, "has warned " + target + " from " + this.chatRoomData.getName());
            this.queueAdminMessage(warningMessage.toString(), target, (String)null);
            this.queueAdminMessage(String.format("'%s' has been warned", target), instigator, (String)null);
         }
      }
   }

   public void addGroupModerator(String instigator, String target) throws FusionException {
      if (!this.isGroupLinkedChatroom()) {
         throw new FusionExceptionWithErrorCauseCode("Command is only available for group-linked chatrooms", ErrorCause.EmoteCommandError.INVALID_CHATROOM_TYPE.getCode());
      } else {
         ChatRoomParticipant instigatorParticipant = this.participants.verifyYouAreParticipant(instigator, ErrorCause.EmoteCommandError.INSTIGATOR_NOT_IN_CHATROOM);
         if (!instigatorParticipant.isGroupAdmin()) {
            throw new FusionExceptionWithErrorCauseCode("Command is only available for group admins", ErrorCause.EmoteCommandError.INVALID_USER_TYPE.getCode());
         } else {
            instigatorParticipant.verifyClientMeetsMinVersion("/mod");
            ChatRoomInfo info = this.chatRoomData.getInfo();
            String chatRoomName = info.chatRoomName;
            Integer groupID = info.groupID;
            String groupName = info.groupName;
            String groupOwner = info.groupOwner;

            try {
               GroupMemberData targetGroupMemberData = null;
               UserObject user = new UserObject(target);
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                  try {
                     targetGroupMemberData = user.getGroupMember(groupID);
                  } catch (DAOException var18) {
                     log.error(String.format("DAO: Failed to get group member data for user:%s, group:%s", target, groupID), var18);
                  }
               } else {
                  User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                  targetGroupMemberData = userEJB.getGroupMember(target, groupID);
               }

               MessageData messageDataToInstigator;
               String messageText;
               if (targetGroupMemberData == null) {
                  log.info(String.format("'%s' cannot be added as a group moderator for group '%s' since he/she is not a group member", target, groupName));
                  messageText = String.format("[PVT] '%s' is not a group member yet", target);
                  messageDataToInstigator = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                  instigatorParticipant.putMessage(messageDataToInstigator.toIceObject());
               } else if (targetGroupMemberData.type == GroupMemberData.TypeEnum.MODERATOR) {
                  log.info(String.format("'%s' cannot be added as a group moderator for group '%s' since he/she is already a moderator", target, groupName));
                  messageText = String.format("[PVT] '%s' is already a group moderator", target);
                  messageDataToInstigator = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                  instigatorParticipant.putMessage(messageDataToInstigator.toIceObject());
               } else if (targetGroupMemberData.type == GroupMemberData.TypeEnum.ADMINISTRATOR) {
                  log.info(String.format("'%s' cannot be added as a group moderator for group '%s' since he/she is already a moderator", target, groupName));
                  messageText = String.format("[PVT] '%s' is a group administrator", target);
                  messageDataToInstigator = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                  instigatorParticipant.putMessage(messageDataToInstigator.toIceObject());
               } else {
                  if (targetGroupMemberData.type != GroupMemberData.TypeEnum.REGULAR) {
                     throw new FusionExceptionWithErrorCauseCode("Target group member type is invalid/not supported", ErrorCause.EmoteCommandError.INVALID_USER_TYPE.getCode());
                  }

                  boolean isUserBlackListedInGroup = false;
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                     try {
                        isUserBlackListedInGroup = user.isUserBlackListedInGroup(groupID);
                     } catch (DAOException var17) {
                        log.warn(String.format("DAO: Failed to check isUserBlackListedInGroup for user:%s, group:%s", user, groupID), var17);
                     }
                  } else {
                     Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
                     isUserBlackListedInGroup = messageEJB.isUserBlackListedInGroup(target, groupID);
                  }

                  if (isUserBlackListedInGroup) {
                     log.info(String.format("'%s' cannot be added as a group moderator for group '%s' since he/she is blacklisted", target, groupName));
                     String messageText = String.format("[PVT] '%s' has been banned. you can’t add to be a group moderator.", target);
                     MessageData messageDataToInstigator = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                     instigatorParticipant.putMessage(messageDataToInstigator.toIceObject());
                  } else {
                     Group groupEJB = (Group)EJBHomeCache.getObject("ejb/Group", GroupHome.class);
                     groupEJB.giveGroupMemberModeratorRights(groupOwner, groupID, target);
                     String logMessage = instigator + " has added " + target + " into the group moderator list for group " + groupName;
                     log.info(logMessage);
                     auditLog.info(logMessage);
                     this.logMessage(instigatorParticipant, " has added " + target + " into the group moderator list for group " + groupName);
                     ChatRoomParticipant targetParticipant = this.participants.get(target);
                     if (targetParticipant != null) {
                        String messageText = String.format("[PVT] Congratulations! %s is now a group moderator for group '%s'", target, groupName);
                        MessageData messageDataToTarget = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                        targetParticipant.putMessage(messageDataToTarget.toIceObject());
                     }
                  }
               }
            } catch (CreateException var19) {
               log.error("Caught create exception", var19);
               throw new FusionExceptionWithErrorCauseCode("Internal error", ErrorCause.EmoteCommandError.INTERNAL_ERROR.getCode());
            } catch (RemoteException var20) {
               interceptExceptionCause(var20);
            }

         }
      }
   }

   public void removeGroupModerator(String instigator, String target) throws FusionException {
      if (!this.isGroupLinkedChatroom()) {
         throw new FusionExceptionWithErrorCauseCode("Command is only available for group-linked chatrooms", ErrorCause.EmoteCommandError.INVALID_CHATROOM_TYPE.getCode());
      } else {
         ChatRoomParticipant instigatorParticipant = this.participants.verifyYouAreParticipant(instigator, ErrorCause.EmoteCommandError.INSTIGATOR_NOT_IN_CHATROOM);
         if (!instigatorParticipant.isGroupAdmin()) {
            throw new FusionExceptionWithErrorCauseCode("Command is only available for group admins", ErrorCause.EmoteCommandError.INVALID_USER_TYPE.getCode());
         } else {
            instigatorParticipant.verifyClientMeetsMinVersion("/unmod");
            ChatRoomInfo info = this.chatRoomData.getInfo();
            String chatRoomName = info.chatRoomName;
            Integer groupID = info.groupID;
            String groupName = info.groupName;
            String groupOwner = info.groupOwner;

            try {
               GroupMemberData targetGroupMemberData = null;
               UserObject user = new UserObject(target);
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
                  try {
                     targetGroupMemberData = user.getGroupMember(groupID);
                  } catch (DAOException var16) {
                     log.error(String.format("DAO: Failed to get GroupMember for user:%s, group:%s", user, groupID));
                  }
               } else {
                  User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                  targetGroupMemberData = userEJB.getGroupMember(target, groupID);
               }

               MessageData messageDataToInstigator;
               String messageText;
               if (targetGroupMemberData == null) {
                  log.info(String.format("'%s' cannot be removed from the group moderator list for group '%s' since he/she is not a group member", target, groupName));
                  messageText = String.format("[PVT] '%s' is not a group member yet", target);
                  messageDataToInstigator = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                  instigatorParticipant.putMessage(messageDataToInstigator.toIceObject());
               } else if (targetGroupMemberData.type == GroupMemberData.TypeEnum.REGULAR) {
                  log.info(String.format("'%s' cannot be removed from the group moderator list for group '%s' since he/she is not yet a moderator", target, groupName));
                  messageText = String.format("[PVT] '%s' is not yet a group moderator", target);
                  messageDataToInstigator = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                  instigatorParticipant.putMessage(messageDataToInstigator.toIceObject());
               } else if (targetGroupMemberData.type == GroupMemberData.TypeEnum.ADMINISTRATOR) {
                  log.info(String.format("'%s' cannot be removed from the group moderator for group '%s' since he/she is a group administrator", target, groupName));
                  messageText = String.format("[PVT] '%s' is a group administrator", target);
                  messageDataToInstigator = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                  instigatorParticipant.putMessage(messageDataToInstigator.toIceObject());
               } else {
                  if (targetGroupMemberData.type != GroupMemberData.TypeEnum.MODERATOR) {
                     throw new FusionExceptionWithErrorCauseCode("Target group member type is invalid/not supported", ErrorCause.EmoteCommandError.INVALID_USER_TYPE.getCode());
                  }

                  Group groupEJB = (Group)EJBHomeCache.getObject("ejb/Group", GroupHome.class);
                  groupEJB.removeGroupMemberModeratorRights(groupOwner, groupID, target);
                  String logMessage = instigator + " has removed " + target + " from the group moderator list for group " + groupName;
                  log.info(logMessage);
                  auditLog.info(logMessage);
                  this.logMessage(instigatorParticipant, " has removed " + target + " from the group moderator list for group " + groupName);
                  String messageText = String.format("[PVT] %s is no longer a moderator for group '%s'", target, groupName);
                  MessageData messageDataToTarget = MessageData.newChatRoomMessage(chatRoomName, messageText, (String[])null);
                  ChatRoomParticipant targetParticipant = this.participants.get(target);
                  if (targetParticipant != null) {
                     targetParticipant.putMessage(messageDataToTarget.toIceObject());
                  }
               }
            } catch (CreateException var17) {
               log.error("Caught create exception", var17);
               throw new FusionExceptionWithErrorCauseCode("Internal error", ErrorCause.EmoteCommandError.INTERNAL_ERROR.getCode());
            } catch (RemoteException var18) {
               interceptExceptionCause(var18);
            }

         }
      }
   }

   private static Throwable getCause(Throwable t) {
      return (Throwable)(t instanceof EJBException ? ((EJBException)t).getCausedByException() : t.getCause());
   }

   private static void interceptExceptionCause(RemoteException re) throws FusionException {
      Throwable cause = getCause(re);

      while(cause != null) {
         if (cause instanceof EJBExceptionWithErrorCause) {
            EJBExceptionWithErrorCause ejbExWithCause = (EJBExceptionWithErrorCause)cause;
            throw new FusionExceptionWithErrorCauseCode(ejbExWithCause.getMessage(), ejbExWithCause.getErrorCause().getCode());
         }

         if (cause instanceof EJBException) {
            EJBException ejbEx = (EJBException)cause;
            cause = getCause(ejbEx);
            if (cause == null) {
               log.error("Caught Remote Exception due to EJBException", re);
               throw new FusionException(ejbEx.getMessage());
            }
         } else {
            if (cause instanceof ExceptionWithErrorCause) {
               ExceptionWithErrorCause exWithCause = (ExceptionWithErrorCause)cause;
               throw new FusionExceptionWithErrorCauseCode(exWithCause.getMessage(), exWithCause.getErrorCause().getCode());
            }

            cause = getCause(cause);
         }
      }

      log.error("Caught Remote Exception", re);
      throw new FusionException("Internal error");
   }

   public String[] getGroupModerators(String instigator) throws FusionException {
      if (!this.isGroupLinkedChatroom()) {
         throw new FusionExceptionWithErrorCauseCode("Command is only available for group-linked chatrooms", ErrorCause.EmoteCommandError.INVALID_CHATROOM_TYPE.getCode());
      } else {
         ChatRoomParticipant instigatorParticipant = this.participants.verifyYouAreParticipant(instigator, ErrorCause.EmoteCommandError.INSTIGATOR_NOT_IN_CHATROOM);
         if (!instigatorParticipant.isGroupAdmin()) {
            throw new FusionExceptionWithErrorCauseCode("Command is only available for group admins", ErrorCause.EmoteCommandError.INVALID_USER_TYPE.getCode());
         } else {
            Integer groupID = this.chatRoomData.getGroupID();

            try {
               Set<String> moderatorNames = null;
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_GROUP_DAO)) {
                  try {
                     moderatorNames = DAOFactory.getInstance().getGroupDAO().getModeratorUserNames(groupID, false);
                  } catch (DAOException var9) {
                     log.error(String.format("Failed to get ModeratorUserNames for group:%s", groupID), var9);
                  }
               } else {
                  Group groupEJB = (Group)EJBHomeCache.getObject("ejb/Group", GroupHome.class);
                  moderatorNames = groupEJB.getModeratorUserNames(groupID, false);
               }

               if (moderatorNames == null) {
                  throw new FusionExceptionWithErrorCauseCode(groupID + " not valid.", ErrorCause.EmoteCommandError.INVALID_GROUP.getCode());
               } else {
                  String[] moderators = new String[moderatorNames.size()];
                  int i = 0;

                  for(Iterator i$ = moderatorNames.iterator(); i$.hasNext(); ++i) {
                     String mods = (String)i$.next();
                     moderators[i] = mods;
                  }

                  return moderators;
               }
            } catch (CreateException var10) {
               log.error("Caught create exception", var10);
               throw new FusionExceptionWithErrorCauseCode("Internal error", ErrorCause.EmoteCommandError.INTERNAL_ERROR.getCode());
            } catch (RemoteException var11) {
               interceptExceptionCause(var11);
               log.error("Caught remote exception", var11);
               throw new FusionException("INTERNAL ERROR");
            }
         }
      }
   }

   static {
      recentChatRoomMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.recentChatRooms);
      scheduler = Executors.newScheduledThreadPool(20);
      MULTI_KICK_PATTERN_CHECK = Pattern.compile("[\\.A-Za-z0-9_-]{1}");
      MIMETYPE_DESCRIPTION = "system/chatroom-description";
      MIMETYPE_MANAGED_BY = "system/chatroom-managed-by";
      MIMETYPE_LOCKED = "system/chatroom-locked";
      MIMETYPE_PARTICIPANTS = "system/chatroom-participants";
      MIMETYPE_ANNOUNCE = "system/chatroom-announce";
      MIMETYPE_STADIUM = "system/chatroom-stadium";
      MIMETYPE_HELP = "system/chatroom-help";
      MIMETYPE_WELCOME = "system/chatroom-welcome";
      MIMETYPE_PARTICPANT_ENTER = "system/chatroom-participant-enter";
      MIMETYPE_PARTICPANT_EXIT = "system/chatroom-participant-exit";
      EMPTY_MIMETYPE_DATA = "{}";
   }

   private class KickUserVote implements Runnable {
      private ChatRoomParticipant instigator;
      private String target;
      private HashSet<String> yesVoters = new HashSet();
      private boolean votePassed;
      private int iteration;
      private final long createTime;
      private final String roomName;
      private final String instanceID;

      public KickUserVote(ChatRoomParticipant instigator, String roomName, String target) {
         this.instigator = instigator;
         this.target = target;
         this.yesVoters.add(instigator.getUsername());
         this.createTime = System.currentTimeMillis();
         this.roomName = roomName;
         this.instanceID = UUID.randomUUID().toString();
         ChatRoom.scheduler.execute(this);
      }

      private String formatLogMessage(String stmt) {
         StringBuilder msgBuilder = new StringBuilder();
         msgBuilder.append("Room:[");
         msgBuilder.append(this.roomName);
         msgBuilder.append("];Vote Instance:[");
         msgBuilder.append(this.instanceID);
         msgBuilder.append("];");
         msgBuilder.append(stmt);
         return msgBuilder.toString();
      }

      private void logInfo(String msg) {
         ChatRoom.log.info(this.formatLogMessage(msg));
      }

      private void logError(String msg, Throwable t) {
         ChatRoom.log.error(this.formatLogMessage(msg), t);
      }

      public void run() {
         try {
            boolean enableOverrideExpiredKickUserVote = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.ENABLE_OVERRIDE_EXPIRED_KICK_USER_VOTE);
            synchronized(ChatRoom.this.kickUserVoteMonitor) {
               ChatRoom.KickUserVote currentKickUserVote = ChatRoom.this.kickUserVote;
               if (enableOverrideExpiredKickUserVote && currentKickUserVote != null && currentKickUserVote != this) {
                  this.logInfo("The vote to kick (" + this.toString() + ") has ended (due replaced by another vote to kick " + currentKickUserVote.toString() + ".");
                  ChatRoom.this.queueAdminMessage("The vote to kick " + this.target + " has failed", (String)null, (String)null);
                  return;
               }

               if (this.votePassed) {
                  return;
               }

               int secondsRemaining = (ChatRoom.this.kickUserVoteDuration - ChatRoom.this.kickUserVoteUpdateInterval * this.iteration) / 1000;
               if (secondsRemaining <= 0) {
                  this.logInfo("The vote to kick (" + this.toString() + ") has ended (due to max voting duration reached)");
                  ChatRoom.this.queueAdminMessage("The vote to kick " + this.target + " has failed", (String)null, (String)null);
                  ChatRoom.this.endKickUserVote();
                  return;
               }

               ChatRoom.BannedInfo bannedInfo = (ChatRoom.BannedInfo)ChatRoom.this.bannedUsers.get(this.target);
               if (bannedInfo != null && bannedInfo.getExpiredTime() > System.currentTimeMillis()) {
                  this.logInfo("The vote to kick (" + this.toString() + ") has ended (due to target already " + bannedInfo.getReason().getPrevAction() + ")");
                  ChatRoom.this.endKickUserVote();
                  return;
               }

               int numYesVotes = this.yesVoters.size();
               int numVotesRequired = this.numYesVotesRequired() - numYesVotes;
               String progressMessage;
               if (this.iteration++ == 0) {
                  progressMessage = "A vote to kick " + this.target + " has been started by " + this.instigator.getUsername() + ". ";
                  progressMessage = progressMessage + numVotesRequired + " more vote" + (numVotesRequired == 1 ? " " : "s ");
                  progressMessage = progressMessage + "needed, " + secondsRemaining + "s remaining";
                  ChatRoom.this.logMessage(this.instigator, "<" + this.instigator.getUsername() + " started a vote to kick " + this.target + ">");
               } else {
                  progressMessage = "Vote to kick " + this.target + ": " + numYesVotes + " vote" + (numYesVotes == 1 ? "" : "s") + ", ";
                  progressMessage = progressMessage + numVotesRequired + " more needed. " + secondsRemaining + "s remaining";
               }

               ChatRoom.this.queueAdminMessage(progressMessage, (String)null, (String)null);
               ChatRoom.scheduler.schedule(this, (long)ChatRoom.this.kickUserVoteUpdateInterval, TimeUnit.MILLISECONDS);
            }
         } catch (Throwable var11) {
            StringBuilder logDetails = new StringBuilder();
            logDetails.append("Unhandled exception on KickUserVote.run();");
            logDetails.append("ErrorID:<" + UUID.randomUUID().toString() + ">;");
            logDetails.append("KickUserVote:<").append(this.toString()).append(">;");
            logDetails.append("Exception:<").append(var11.getMessage()).append(">");
            this.logError(logDetails.toString(), var11);
            ChatRoom.this.logMessage(this.instigator, logDetails.toString());
         }

      }

      public String toString() {
         String instigatorName = this.instigator != null ? this.instigator.getUsername() : "";
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("roomName:[").append(this.roomName).append("];");
         stringBuilder.append("instigator:[").append(instigatorName).append("];");
         stringBuilder.append("iteration:[").append(this.iteration).append("];");
         stringBuilder.append("target:[").append(this.target).append("];");
         stringBuilder.append("votePassed:[").append(this.votePassed).append("];");
         stringBuilder.append("yesVoters:[").append(this.yesVoters).append("];");
         stringBuilder.append("createTime:[").append(DateTimeUtils.getStringForTimestamp(new Date(this.createTime))).append("];");
         stringBuilder.append("instanceID:[").append(this.instanceID).append("];");
         return stringBuilder.toString();
      }

      public boolean isVotingExpired() {
         return System.currentTimeMillis() - this.createTime > (long)(ChatRoom.this.kickUserVoteDuration * 3 / 2);
      }

      public void addYesVote(ChatRoomParticipant voterParticipant) throws FusionException {
         if (!this.votePassed) {
            if (!this.yesVoters.add(voterParticipant.getUsername())) {
               throw new FusionException("You have already voted to kick " + this.target);
            } else {
               ChatRoom.this.logMessage(voterParticipant, "<" + voterParticipant.getUsername() + " voted to kick " + this.target + ">");
               if (this.yesVoters.size() >= this.numYesVotesRequired()) {
                  this.votePassed = true;

                  try {
                     ChatRoomParticipant targetParticipant = ChatRoom.this.participants.get(this.target);
                     if (targetParticipant != null) {
                        ChatRoom.this.kickParticipant((ChatRoomParticipant)null, targetParticipant, (String)null);
                     }
                  } catch (FusionException var3) {
                  } catch (Exception var4) {
                     this.logError("kickParticipant [" + this.target + "] failed. Vote to kick (" + this.toString() + ")", var4);
                  }

                  ChatRoom.this.endKickUserVote();
               }

            }
         }
      }

      public void removeYesVote(String username) {
         this.yesVoters.remove(username);
      }

      public String getTarget() {
         return this.target;
      }

      public long getCreateTime() {
         return this.createTime;
      }

      private int numYesVotesRequired() {
         return (int)Math.max((double)ChatRoom.this.minKickVotesRequired, Math.ceil((double)ChatRoom.this.participants.size() / 3.0D));
      }
   }

   private class ExitMessageToDispatch extends ChatRoom.MessageToDispatchToRoomExcludingUser {
      public ExitMessageToDispatch(MessageDataIce message, String userLeaving) {
         super(message, ChatRoom.MessageToDispatchTypeEnum.TEXT, userLeaving);
      }

      boolean shouldSendMessage() {
         ChatRoom.BannedInfo bi = (ChatRoom.BannedInfo)ChatRoom.this.bannedUsers.get(this.usernameToExclude);
         if (bi != null && bi.getExpiredTime() < System.currentTimeMillis()) {
            return false;
         } else {
            return ChatRoom.this.chatRoomData.isOnBannedList(this.usernameToExclude) ? false : super.shouldSendMessage();
         }
      }
   }

   private class EnterMessageToDispatch extends ChatRoom.MessageToDispatchToRoomExcludingUser {
      public EnterMessageToDispatch(MessageDataIce message, String usernameToExclude) {
         super(message, ChatRoom.MessageToDispatchTypeEnum.TEXT, usernameToExclude);
      }

      boolean shouldSendMessage() {
         return !ChatRoom.this.participants.isParticipant(this.usernameToExclude) ? false : super.shouldSendMessage();
      }
   }

   private class UserMessageToDispatchToRoom extends ChatRoom.MessageToDispatchToRoomExcludingUser {
      public UserMessageToDispatchToRoom(MessageDataIce message, String usernameToExclude) {
         super(message, ChatRoom.MessageToDispatchTypeEnum.TEXT, usernameToExclude);
      }

      boolean shouldSendMessage() {
         return !ChatRoom.this.participants.isParticipant(this.usernameToExclude) ? false : super.shouldSendMessage();
      }
   }

   private class MessageToDispatchToRoomExcludingUser extends ChatRoom.MessageToDispatchToRoom {
      protected String usernameToExclude;

      public MessageToDispatchToRoomExcludingUser(MessageDataIce message, ChatRoom.MessageToDispatchTypeEnum type, String usernameToExclude) {
         super(message, type);
         this.usernameToExclude = usernameToExclude;
      }

      boolean shouldSendToParticipant(ChatRoomParticipant participant, boolean toModerators) {
         return participant.getUsername().equals(this.usernameToExclude) ? false : super.shouldSendToParticipant(participant, toModerators);
      }
   }

   private class MessageToDispatchToRoom extends ChatRoom.MessageToDispatch {
      public MessageToDispatchToRoom(MessageDataIce message, ChatRoom.MessageToDispatchTypeEnum type) {
         super(message, type);
      }

      public void dispatch() {
         if (this.shouldSendMessage()) {
            boolean toModerators = this.message.fromAdministrator != 1 && ChatRoom.this.isStadium();
            Iterator i$ = ChatRoom.this.participants.getAll().iterator();

            while(i$.hasNext()) {
               ChatRoomParticipant participant = (ChatRoomParticipant)i$.next();
               if (this.shouldSendToParticipant(participant, toModerators)) {
                  this.putMessage(participant);
               }
            }
         }

      }

      boolean shouldSendMessage() {
         boolean sysMsg = this.message.sourceType == MessageData.SourceTypeEnum.CHATROOM.value() || this.message.sourceType == MessageData.SourceTypeEnum.SYSTEM_GENERAL.value();
         if (sysMsg) {
            return true;
         } else {
            boolean mutedUser = ChatRoom.this.mutedUsers.containsKey(this.message.source);
            if (mutedUser) {
               if (ChatRoom.log.isDebugEnabled()) {
                  ChatRoom.log.debug("Ignoring dispatch of message from muted user:" + this.message.source);
               }

               return false;
            } else {
               return true;
            }
         }
      }

      boolean shouldSendToParticipant(ChatRoomParticipant participant, boolean toModerators) {
         return !toModerators || participant.hasAdminOrModeratorRights();
      }
   }

   private class MessageToDispatchToSingleUser extends ChatRoom.MessageToDispatch {
      private String usernameToReceive;

      public MessageToDispatchToSingleUser(MessageDataIce message, ChatRoom.MessageToDispatchTypeEnum type, String usernameToReceive) {
         super(message, type);
         this.usernameToReceive = usernameToReceive;
      }

      public void dispatch() {
         ChatRoomParticipant participant = ChatRoom.this.participants.get(this.usernameToReceive);
         if (participant != null) {
            this.putMessage(participant);
         }

      }
   }

   private abstract class MessageToDispatch {
      protected MessageDataIce message;
      private ChatRoom.MessageToDispatchTypeEnum type;

      protected MessageToDispatch(MessageDataIce message, ChatRoom.MessageToDispatchTypeEnum type) {
         this.message = message;
         this.type = type;
      }

      public abstract void dispatch();

      protected void putMessage(ChatRoomParticipant participant) {
         participant.putMessage_async(this.message);
         if (this.type == ChatRoom.MessageToDispatchTypeEnum.POPUP || this.type == ChatRoom.MessageToDispatchTypeEnum.TEXT_AND_POPUP) {
            participant.putAlertMessage_async(this.message.messageText);
         }

      }
   }

   private static enum MessageToDispatchTypeEnum {
      TEXT,
      POPUP,
      TEXT_AND_POPUP;
   }

   private class AnnounceTask extends TimerTask {
      String announceMessage;
      ChatRoom chatRoom;
      int messageColor = -1;

      AnnounceTask(String announceMessage, int messageColor, ChatRoom chatRoom) {
         this.announceMessage = announceMessage;
         this.chatRoom = chatRoom;
         this.messageColor = messageColor;
      }

      public void run() {
         this.chatRoom.queueMessage(ChatRoom.this.formatAnnounceMessage(this.announceMessage, this.messageColor).toIceObject(), (String)null, (String)null, ChatRoom.MessageToDispatchTypeEnum.TEXT, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_ADD_TIMEOUT_IN_MILLIS));
      }
   }

   private class BlockingQueueViaList<T> implements ChatRoom.AbstractBlockingQueue<T> {
      private List<T> list;

      BlockingQueueViaList(List<T> list) {
         this.list = list;
      }

      public boolean offer(T e, long timeoutInMillis) {
         long waitedTime = 0L;
         if (timeoutInMillis >= 0L) {
            long waitInterval = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_VIA_LIST_ADD_WAIT_INTERVAL_IN_MILLIS);

            for(long sleepInterval = timeoutInMillis - waitedTime > waitInterval ? waitInterval : timeoutInMillis - waitedTime; waitedTime < timeoutInMillis && this.list.size() > SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MESSAGE_QUEUE_VIA_LIST_MAX_SIZE); sleepInterval = timeoutInMillis - waitedTime > waitInterval ? waitInterval : timeoutInMillis - waitedTime) {
               try {
                  Thread.sleep(sleepInterval);
               } catch (InterruptedException var11) {
                  ChatRoom.log.error(String.format("BlockingQueueViaList.offer interruppted while adding %s with %d timeout", e, timeoutInMillis), var11);
                  return false;
               }

               waitedTime += sleepInterval;
            }
         }

         if (timeoutInMillis != -1L && waitedTime >= timeoutInMillis) {
            ChatRoom.log.error(String.format("BlockingQueueViaList.offer dropping msg %s with %d timeout", e, timeoutInMillis));
            return false;
         } else {
            return this.list.add(e);
         }
      }

      public T takeIfAvailable() {
         try {
            return this.list.remove(0);
         } catch (IndexOutOfBoundsException var2) {
            return null;
         } catch (NoSuchElementException var3) {
            return null;
         } catch (Exception var4) {
            ChatRoom.log.error(String.format("Unexpected exception in BlockingQueueViaList.takeIfAvailable"), var4);
            return null;
         }
      }

      public int size() {
         return this.list.size();
      }

      public boolean isEmpty() {
         return this.list.isEmpty();
      }
   }

   private class BlockingQueueViaBlockingQueue<T> implements ChatRoom.AbstractBlockingQueue<T> {
      private BlockingQueue<T> queue;

      BlockingQueueViaBlockingQueue(BlockingQueue<T> queue) {
         this.queue = queue;
      }

      public boolean offer(T e, long timeoutInMillis) {
         try {
            return this.queue.offer(e, timeoutInMillis, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var5) {
            ChatRoom.log.error(String.format("BlockingQueueViaBlockingQueue.offer interruppted while adding %s with %d timeout", e, timeoutInMillis), var5);
            return false;
         }
      }

      public T takeIfAvailable() {
         try {
            return this.queue.poll(0L, TimeUnit.MILLISECONDS);
         } catch (InterruptedException var2) {
            ChatRoom.log.error(String.format("BlockingQueueViaBlockingQueue.takeIfAvailable interruppted"), var2);
            return null;
         }
      }

      public int size() {
         return this.queue.size();
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }
   }

   private interface AbstractBlockingQueue<T> {
      boolean offer(T var1, long var2);

      T takeIfAvailable();

      int size();

      boolean isEmpty();
   }

   static class MimeDataJSONObject extends JSONObject {
      public ChatRoom.MimeDataJSONObject put(String key, int value) {
         try {
            super.put(key, value);
         } catch (JSONException var4) {
            ChatRoom.log.error(String.format("Unable to serialize key [%s], value [%d]", key, value), var4);
         }

         return this;
      }

      public ChatRoom.MimeDataJSONObject put(String key, Object value) {
         try {
            super.put(key, value);
         } catch (JSONException var4) {
            ChatRoom.log.error(String.format("Unable to serialize key [%s], value [%s]", key, value), var4);
         }

         return this;
      }
   }

   private static class BannedInfo {
      private long expiredTime;
      private ChatRoom.BannedInfo.ReasonEnum reason;
      private String instigator;

      public BannedInfo(long expiredTime, ChatRoom.BannedInfo.ReasonEnum reason, String instigator) {
         this.expiredTime = expiredTime;
         this.reason = reason;
         this.instigator = instigator;
      }

      public long getExpiredTime() {
         return this.expiredTime;
      }

      public ChatRoom.BannedInfo.ReasonEnum getReason() {
         return this.reason;
      }

      public String getInstigator() {
         return this.instigator;
      }

      public static enum ReasonEnum {
         KICK("kicked"),
         BUMP("bumped");

         private String prevAction;

         private ReasonEnum(String prevAction) {
            this.prevAction = prevAction;
         }

         public String getPrevAction() {
            return this.prevAction;
         }
      }
   }
}
