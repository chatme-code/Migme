package com.projectgoth.fusion.messageswitchboard;

import Ice.Communicator;
import Ice.Current;
import Ice.ObjectNotExistException;
import com.projectgoth.fusion.chatsync.ChatDefinition;
import com.projectgoth.fusion.chatsync.ChatRenamer;
import com.projectgoth.fusion.chatsync.ChatSyncEntity;
import com.projectgoth.fusion.chatsync.ChatSyncRetrievalExecutor;
import com.projectgoth.fusion.chatsync.ChatSyncStats;
import com.projectgoth.fusion.chatsync.ChatSyncStorageExecutor;
import com.projectgoth.fusion.chatsync.ClosedChatNotificationPusher;
import com.projectgoth.fusion.chatsync.CurrentChatListUpdater;
import com.projectgoth.fusion.chatsync.GroupChatCreationHandler;
import com.projectgoth.fusion.chatsync.LastNChatMessagesPusher;
import com.projectgoth.fusion.chatsync.LatestMessagesDigestPusher;
import com.projectgoth.fusion.chatsync.MessageSendHandler;
import com.projectgoth.fusion.chatsync.MessageSendLiveSyncer;
import com.projectgoth.fusion.chatsync.MessageStatusEventsRetriever;
import com.projectgoth.fusion.chatsync.UserMissingChats;
import com.projectgoth.fusion.chatsync.UserMissingChatsPusher;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.emote.EmoteCommand;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.objectcache.Emote;
import com.projectgoth.fusion.slice.ChatDefinitionIce;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._MessageSwitchboardDisp;
import java.sql.Connection;
import java.util.HashSet;
import org.apache.log4j.Logger;

public class MessageSwitchboardI extends _MessageSwitchboardDisp {
   private static final LogFilter log;
   private IcePrxFinder icePrxFinder;
   private Communicator communicator;

   public void initialize(Communicator communicator) throws Exception {
      this.communicator = communicator;
   }

   void shutdown() {
   }

   public void setIcePrxFinder(IcePrxFinder icePrxFinder) {
      this.icePrxFinder = icePrxFinder;
   }

   public boolean isUserChatSyncEnabled(ConnectionPrx cxn, String username, int userID, Current __current) throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_SYNC_ENABLED);
   }

   public ChatDefinitionIce[] getChats(int userID, int chatListVersion, int limit, byte chatType, Current __current) throws FusionException {
      return this.getChats2(userID, chatListVersion, limit, chatType, (ConnectionPrx)null);
   }

   public ChatDefinitionIce[] getChats2(int userID, int chatListVersion, int limit, byte chatType, ConnectionPrx connection, Current __current) throws FusionException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.J2ME_CHAT_MANAGER_ENABLED)) {
         log.debug("J2ME_CHAT_MANAGER_ENABLED==false so returning null from getChats");
         return null;
      } else {
         int maxPerMin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_GET_CHAT_REQUESTS_PER_MINUTE);
         if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_GLOBAL_RATE_LIMITS.toString(), "maxGetChatRequestsPerMin", (long)maxPerMin, 60000L)) {
            log.debug("GET_CHATS global rate limit exceeeded so returning null from getChats");
            return null;
         } else {
            int maxPerDay = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_GET_CHATS_REQUESTS_PER_USER_PER_DAY);
            if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_MAX_GET_CHATS_PER_USER_PER_DAY.toString(), Integer.toString(userID), (long)maxPerDay, 86400000L)) {
               if (log.isDebugEnabled()) {
                  log.debug("GET_CHATS rate limit exceeded for userID=" + userID + " so returning null from getChats");
               }

               return null;
            } else {
               if (log.isDebugEnabled()) {
                  log.debug("Entering MessageSwitchboardI.getChats with userID=" + userID + " chatType=" + chatType + " limit=" + limit + " chatListVersion=" + chatListVersion);
               }

               Integer iLimit = limit == Integer.MIN_VALUE ? null : limit;
               Byte bChatType = chatType == -128 ? null : chatType;
               UserMissingChats missingChats = new UserMissingChats(userID, chatListVersion, iLimit, bChatType, connection);
               UserMissingChats missingChatsResult = (UserMissingChats)ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrievalAndWait(missingChats);
               if (missingChatsResult == null) {
                  return new ChatDefinitionIce[0];
               } else {
                  ChatDefinition[] mc = missingChatsResult.getMissingChats();
                  ChatDefinitionIce[] results = new ChatDefinitionIce[mc.length];

                  for(int i = 0; i < mc.length; ++i) {
                     results[i] = mc[i].toIceObject();
                  }

                  return results;
               }
            }
         }
      }
   }

   public void onGetChats(ConnectionPrx cxn, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("onGetChats for user=" + userID);
      }

      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      try {
         ChatSyncStats.getInstance().incrementTotalGetChatsReceived();
         Byte bChatType = chatType == -128 ? null : chatType;
         Integer iLimit = limit == Integer.MIN_VALUE ? null : limit;
         UserMissingChatsPusher userMissingChats = new UserMissingChatsPusher(userID, cxn, chatListVersion, iLimit, bChatType, transactionId, parentUsername);
         ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrieval(userMissingChats);
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_RETRIEVAL);
      }

   }

   public void getAndPushMessages(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, Current __current) throws FusionException {
      this.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, cxn.getDeviceTypeAsInt(), cxn.getClientVersion(), (short)-32768);
   }

   public void getAndPushMessages2(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTransactionId, Current __current) throws FusionException {
      int maxPerDay = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_GET_MESSAGES_REQUESTS_PER_USER_PER_DAY);
      if (log.isDebugEnabled()) {
         log.debug("Rate limiting getAndPushMessages for user=" + username + " to " + maxPerDay + " per day");
      }

      if (!MemCachedRateLimiter.bypassRateLimit(username) && !MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_MAX_GET_MESSAGES_PER_USER_PER_DAY.toString(), username, (long)maxPerDay, 86400000L)) {
         if (log.isDebugEnabled()) {
            log.debug("getAndPushMessages rate limit exceeded for user=" + username);
         }

      } else {
         Long oldest = oldestMessageTimestamp == Long.MIN_VALUE ? null : oldestMessageTimestamp;
         Long newest = newestMessageTimestamp == Long.MIN_VALUE ? null : newestMessageTimestamp;
         Integer iLimit = limit == Integer.MIN_VALUE ? null : limit;
         if (log.isDebugEnabled()) {
            log.debug("getAndPushMessages: invoking LastNChatMessagesPusher for user=" + username);
         }

         ChatDefinition chatKey = new ChatDefinition(suppliedChatID, chatType, username);
         Short sTxnId = fusionPktTransactionId == -32768 ? null : fusionPktTransactionId;
         ChatSyncEntity cm = new LastNChatMessagesPusher(chatKey, chatType, oldest, newest, iLimit, cxn.getSessionObject(), username, ClientType.fromValue(deviceType), clientVersion, sTxnId);
         ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrieval(cm);
      }
   }

   public void onCreateGroupChat(ChatDefinitionIce cdiGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChat, Current __current) throws FusionException {
      ChatDefinition parentPrivateChatID;
      if (privateChatPartnerUsername != null) {
         parentPrivateChatID = new ChatDefinition(privateChatPartnerUsername, (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value(), creatorUsername);
      } else {
         parentPrivateChatID = null;
      }

      try {
         ChatDefinition cdGroupChat = new ChatDefinition(cdiGroupChat);
         GroupChatCreationHandler handler = new GroupChatCreationHandler(cdGroupChat, creatorUsername, privateChatPartnerUsername, groupChat, parentPrivateChatID);
         ChatSyncStorageExecutor.getInstance().scheduleStorage(handler);
      } catch (Exception var9) {
         log.error("While constructing ChatDefinition:", var9);
      }

   }

   public void onJoinGroupChat(String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy, Current __current) throws FusionException {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      try {
         if (log.isDebugEnabled() || debug) {
            log.info("onJoinGroupChat for " + username + ": creating groupChatDef");
         }

         ChatDefinition groupChatDef = new ChatDefinition(groupChatGUID, (byte)MessageDestinationData.TypeEnum.GROUP.value());
         if (log.isDebugEnabled() || debug) {
            log.info("onJoinGroupChat for " + username + ": calling updateChatList");
         }

         this.updateChatList(username, userID, groupChatDef, (ChatDefinition)null, debug, userProxy);
         if (log.isDebugEnabled() || debug) {
            log.info("onJoinGroupChat for " + username + ": called updateChatList");
         }
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
      }

   }

   public void onLeaveGroupChat(String username, int userID, String groupChatGUID, UserPrx userProxy, Current __current) throws FusionException {
      try {
         long startCpu = ChatSyncStats.getInstance().getCpuTime();

         try {
            if (userProxy != null) {
               try {
                  SessionPrx[] sessionPrxs = userProxy.getSessions();
                  this.pushClosedChatNotification(userID, username, (byte)MessageDestinationData.TypeEnum.GROUP.value(), groupChatGUID, (short)0, sessionPrxs);
               } catch (Exception var14) {
                  if (log.isDebugEnabled()) {
                     log.debug("Exception pushing closed chat notification to sessions of user=" + username + ": " + var14, var14);
                  }
               }
            }

            ChatDefinition groupChatDef = new ChatDefinition(groupChatGUID, (byte)MessageDestinationData.TypeEnum.GROUP.value());
            this.updateChatList(username, userID, (ChatDefinition)null, groupChatDef, userProxy);
            if (log.isDebugEnabled()) {
               log.debug("Group guid=" + groupChatGUID + " removed from chat list of userID=" + userID);
            }
         } finally {
            ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
         }
      } catch (ObjectNotExistException var16) {
         if (log.isDebugEnabled()) {
            log.debug("MessageSwitchboardI.onLeaveGroupChat: UserPrx of user=" + username + "who has left group chat is already invalid");
         }
      }

   }

   public void onJoinChatRoom(String username, int userID, String chatRoomName, Current __current) throws FusionException {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      try {
         ChatDefinition chatRoomDef = new ChatDefinition(chatRoomName, (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value());
         this.updateChatList(username, userID, chatRoomDef, (ChatDefinition)null, (UserPrx)null);
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
      }

   }

   public void onLeaveChatRoom(String username, int userID, String chatRoomName, UserPrx userProxy, Current __current) throws FusionException {
      try {
         long startCpu = ChatSyncStats.getInstance().getCpuTime();

         try {
            SessionPrx[] sessionPrxs = userProxy.getSessions();
            this.pushClosedChatNotification(userID, username, (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value(), chatRoomName, (short)0, sessionPrxs);
            ChatDefinition chatRoomDef = new ChatDefinition(chatRoomName, (byte)MessageDestinationData.TypeEnum.CHAT_ROOM.value());
            this.updateChatList(username, userID, (ChatDefinition)null, chatRoomDef, (UserPrx)null);
            if (log.isDebugEnabled()) {
               log.debug("Chatroom=" + chatRoomName + " removed from chat list of userID=" + userID);
            }
         } finally {
            ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
         }
      } catch (ObjectNotExistException var15) {
         if (log.isDebugEnabled()) {
            log.debug("MessageSwitchboardI.onLeaveChatRoom: UserPrx of user=" + username + "who has left chatroom is already invalid");
         }
      }

   }

   private void pushClosedChatNotification(int userID, String username, byte chatType, String chatID, short txnID, SessionPrx[] sessions) throws FusionException {
      ClosedChatNotificationPusher pusher = new ClosedChatNotificationPusher(userID, username, chatType, chatID, txnID, sessions);
      ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrieval(pusher);
   }

   public boolean onSendFusionMessageToIndividual(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String destinationUsername, String[] uniqueUsersPrivateChattedWith, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Current __current) throws FusionException {
      try {
         ClientType dt = ClientType.fromValue(deviceType);
         HashSet<String> hs = new HashSet();
         String[] arr$ = uniqueUsersPrivateChattedWith;
         int len$ = uniqueUsersPrivateChattedWith.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            hs.add(s);
         }

         long startCpu = ChatSyncStats.getInstance().getCpuTime();

         boolean var25;
         try {
            this.onSendFusionMessage(currentSession, messageData, parentUser.getUserData(), parentUser);
            ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.MESSAGE_STORAGE);
            startCpu = ChatSyncStats.getInstance().getCpuTime();
            if (!hs.contains(destinationUsername)) {
               UserDataIce parentUserData = parentUser.getUserData();
               this.onCreatePrivateChat(parentUserData.userID, parentUserData.username, destinationUsername, deviceType, clientVersion, senderUserData, recipientDisplayPicture);
            }

            var25 = true;
         } finally {
            ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
         }

         return var25;
      } catch (Exception var22) {
         log.error("Exception in onSendFusionMessageToIndividual for destinationUsername=" + destinationUsername, var22);
         return false;
      }
   }

   public void onSendFusionMessageToGroupChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, int deviceType, short clientVersion, Current __current) throws FusionException {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      try {
         if (messageData.contentType == MessageData.ContentTypeEnum.TEXT.value() && !Emote.isEmote(messageData.messageText) && !EmoteCommand.hasMessageVariables(messageData.messageText)) {
            this.onSendFusionMessage(currentSession, messageData, parentUser.getUserData(), parentUser, groupChatID);
         }
      } catch (Exception var15) {
         log.error("Exception in onSendFusionMessageToGroupChat for groupChatID=" + groupChatID, var15);
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.MESSAGE_STORAGE);
      }

   }

   public void onSendFusionMessageToChatRoom(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, int deviceType, short clientVersion, Current __current) throws FusionException {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      try {
         if (messageData.contentType == MessageData.ContentTypeEnum.TEXT.value() && !Emote.isEmote(messageData.messageText) && !EmoteCommand.hasMessageVariables(messageData.messageText)) {
            this.onSendFusionMessage(currentSession, messageData, parentUser.getUserData(), parentUser);
         }
      } catch (Exception var15) {
         log.error("Exception in onSendFusionMessageToChatRoom for chatRoomName=" + chatRoomName, var15);
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.MESSAGE_STORAGE);
      }

   }

   public boolean onSendMessageToAllUsersInChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, UserDataIce senderUserData, Current __current) throws FusionException {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      boolean var19;
      try {
         this.onSendFusionMessage(currentSession, messageData, senderUserData, parentUser);
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.MESSAGE_STORAGE);
         if (MessageData.isMessageToAnIndividual(messageData)) {
            startCpu = ChatSyncStats.getInstance().getCpuTime();
            String recipUsername = messageData.messageDestinations[0].destination;

            String recipientDisplayPicture;
            try {
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               recipientDisplayPicture = userEJB.getDisplayPicture(recipUsername);
            } catch (Exception var16) {
               throw new FusionException(var16.getMessage());
            }

            if (!currentSession.privateChattedWith(recipUsername)) {
               int deviceType = currentSession.getDeviceTypeAsInt();
               short clientVersion = currentSession.getClientVersionIce();
               this.onCreatePrivateChat(senderUserData.userID, senderUserData.username, recipUsername, deviceType, clientVersion, senderUserData, recipientDisplayPicture);
            }
         }

         var19 = true;
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
      }

      return var19;
   }

   public void onCreatePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Current __current) throws FusionException {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      try {
         this.onCreatePrivateChatInner(userID, username, otherUser, senderUserData, recipientDisplayPicture);
      } catch (FusionException var17) {
         throw var17;
      } catch (Exception var18) {
         log.error("onCreatePrivateChat:", var18);
         throw new FusionException(var18.getMessage());
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
      }

   }

   private void onCreatePrivateChatInner(int userID, String username, String otherUser, UserDataIce senderUserData, String recipientDisplayPicture) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("onCreatePrivateChat for user=" + username + " otherUser=" + otherUser);
      }

      User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      int recipientID = userEJB.getUserID(otherUser, (Connection)null);

      try {
         log.debug("onCreatePrivateChat: constructing ChatDefinition");
         String[] participants = new String[]{username, otherUser};
         ChatDefinition def = new ChatDefinition(username, otherUser, participants, (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value(), (Integer)null, (Integer)null, (String)null, (Byte)null, recipientDisplayPicture, MessageType.FUSION.value());
         log.debug("onCreatePrivateChat: ChatDefinition constructed");
         ChatSyncStorageExecutor.getInstance().scheduleStorage(def);
         log.debug("onCreatePrivateChat: storage scheduled");
      } catch (Exception var10) {
         log.error("While constructing ChatDefinition:" + var10);
      }

      ChatDefinition chatKey = new ChatDefinition(otherUser, (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value(), username);
      this.updateChatList(username, userID, chatKey, (ChatDefinition)null, (UserPrx)null);
      this.updateChatList(otherUser, recipientID, chatKey, (ChatDefinition)null, (UserPrx)null);
   }

   private void updateChatList(String username, int userID, ChatDefinition addChatID, ChatDefinition removeChatID, UserPrx userProxy) throws FusionException {
      this.updateChatList(username, userID, addChatID, removeChatID, false, userProxy);
   }

   private void updateChatList(String username, int userID, ChatDefinition addChatID, ChatDefinition removeChatID, boolean debug, UserPrx userProxy) throws FusionException {
      if (!MemCachedRateLimiter.bypassRateLimit(username)) {
         int maxPerMin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_CHAT_LIST_UPDATES_PER_MINUTE);
         if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_GLOBAL_RATE_LIMITS.toString(), "maxChatListUpdatesPerMin", (long)maxPerMin, 60000L)) {
            log.warn("MAX_CHAT_LIST_UPDATES_PER_MINUTE rate limit exceeded: could not update chat list for userID=" + userID);
            return;
         }
      }

      if (log.isDebugEnabled() || debug) {
         log.info("Creating and scheduling CurrentChatListUpdater for " + username);
      }

      CurrentChatListUpdater updater = new CurrentChatListUpdater(username, userID, addChatID, removeChatID, debug, userProxy);
      ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrieval(updater);
      if (log.isDebugEnabled() || debug) {
         log.info("Created and scheduled CurrentChatListUpdater for " + username);
      }

   }

   public void onLeavePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, Current __current) throws FusionException {
      long startCpu = ChatSyncStats.getInstance().getCpuTime();

      try {
         ChatDefinition chatKey = new ChatDefinition(otherUser, (byte)MessageDestinationData.TypeEnum.INDIVIDUAL.value(), username);
         this.updateChatList(username, userID, (ChatDefinition)null, chatKey, (UserPrx)null);
      } finally {
         ChatSyncStats.getInstance().addCpuTime(startCpu, ChatSyncStats.ChatSyncOpCategory.CHAT_STORAGE);
      }

   }

   public GroupChatPrx ensureGroupChatExists(SessionPrx currentSession, String groupChatID, Current __current) throws FusionException {
      return currentSession.findGroupChatObject(groupChatID);
   }

   public void onLogon(int userID, SessionPrx session, short transactionID, String parentUsername, Current __current) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.LATEST_MESSAGES_DIGEST_PACKET_SEND_ENABLED)) {
         if (log.isDebugEnabled()) {
            log.debug("onLogon: Scheduling LatestMessagesDigestPusher for userID=" + userID);
         }

         LatestMessagesDigestPusher pusher = new LatestMessagesDigestPusher(userID, session, transactionID, parentUsername);
         ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrieval(pusher);
      }

   }

   private void onSendFusionMessage(SessionPrx currentSession, MessageDataIce msg, UserDataIce senderUserData, UserPrx sender) throws FusionException {
      this.onSendFusionMessage(currentSession, msg, senderUserData, sender, (String)null);
   }

   private void onSendFusionMessage(SessionPrx currentSession, MessageDataIce msg, UserDataIce senderUserData, UserPrx sender, String groupChatID) throws FusionException {
      GroupChatPrx groupChatPrx = null;
      if (groupChatID != null) {
         groupChatPrx = currentSession.findGroupChatObject(groupChatID);
      }

      MessageSendHandler msh = new MessageSendHandler(currentSession, msg, senderUserData, sender, groupChatPrx);
      ChatSyncStorageExecutor.getInstance().scheduleStorage(msh);
      if (!MessageData.isMessageToAChatRoom(msg)) {
         MessageSendLiveSyncer syncer = new MessageSendLiveSyncer(currentSession, msg, senderUserData, sender, groupChatPrx);
         ChatSyncStorageExecutor.getInstance().scheduleStorage(syncer);
      }

   }

   public void setChatName(String parentUsername, String suppliedChatID, byte chatType, String chatName, RegistryPrx regy, Current __current) throws FusionException {
      ChatDefinition chatKey = new ChatDefinition(suppliedChatID, chatType, parentUsername);
      ChatRenamer renamer = new ChatRenamer(chatKey, chatName, regy, parentUsername);
      ChatSyncStorageExecutor.getInstance().scheduleStorage(renamer);
   }

   public void getAndPushMessageStatusEvents(ConnectionI connection, String parentUsername, byte chatType, String suppliedChatID, Long startTime, Long endTime, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short requestTxnId) throws FusionException {
      ChatDefinition chatKey = new ChatDefinition(suppliedChatID, chatType, parentUsername);
      MessageStatusEventsRetriever retriever = new MessageStatusEventsRetriever(chatKey, startTime, endTime, limit, parentUsername, cxn, requestTxnId);
      ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrieval(retriever);
   }

   public void getAndPushMessageStatusEvents(ConnectionI connection, String parentUsername, byte chatType, String suppliedChatID, String[] messageGUIDs, long[] messageTimestamps, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short requestTxnId) throws FusionException {
      ChatDefinition chatKey = new ChatDefinition(suppliedChatID, chatType, parentUsername);
      MessageStatusEventsRetriever retriever = new MessageStatusEventsRetriever(chatKey, messageGUIDs, messageTimestamps, limit, parentUsername, cxn, requestTxnId);
      ChatSyncRetrievalExecutor.getInstance().scheduleEntityRetrieval(retriever);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageSwitchboardI.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
