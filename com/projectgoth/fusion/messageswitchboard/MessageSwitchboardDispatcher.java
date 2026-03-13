package com.projectgoth.fusion.messageswitchboard;

import com.projectgoth.fusion.chatsync.ChatDefinition;
import com.projectgoth.fusion.chatsync.ChatSyncStats;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.objectcache.ChatSourceSession;
import com.projectgoth.fusion.objectcache.ChatSourceUser;
import com.projectgoth.fusion.objectcache.ObjectCacheContext;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.MessageDestinationDataIce;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.Set;
import org.apache.log4j.Logger;

public class MessageSwitchboardDispatcher {
   private static final LogFilter log;

   public static MessageSwitchboardDispatcher getInstance() {
      return MessageSwitchboardDispatcher.SingletonHolder.INSTANCE;
   }

   public boolean isFeatureEnabled() {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHAT_SYNC_ENABLED)) {
         return true;
      } else {
         if (log.isDebugEnabled()) {
            log.debug("Chat sync disabled via kill switch");
         }

         return false;
      }
   }

   public void onGetChats(ConnectionI connection, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername) throws FusionException {
      ChatSyncStats.getInstance().incrementTotalGetChatsReceived();
      if (this.enablementChecks(connection.getUsername(), userID, connection.getDeviceType(), connection.getClientVersion())) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = connection.getGatewayContext().getRegistryPrx().getMessageSwitchboard();
            msp.onGetChats(connection.getConnectionPrx(), userID, chatListVersion, limit, chatType, transactionId, parentUsername);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            msi.onGetChats(connection.getConnectionPrx(), userID, chatListVersion, limit, chatType, transactionId, parentUsername);
         }

      }
   }

   public void getAndPushMessages(ConnectionI connection, String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTxnId) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Entering getAndPushMessages for chatType=" + chatType + " suppliedChatID=" + suppliedChatID + " user=" + username);
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
         MessageSwitchboardPrx msp = connection.getGatewayContext().getRegistryPrx().getMessageSwitchboard();
         msp.getAndPushMessages2(connection.getUsername(), chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, connection.getConnectionPrx(), deviceType, clientVersion, fusionPktTxnId);
      } else {
         MessageSwitchboardI msi = new MessageSwitchboardI();
         msi.getAndPushMessages2(connection.getUsername(), chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, connection.getConnectionPrx(), deviceType, clientVersion, fusionPktTxnId);
      }

   }

   public void onCreateGroupChat(ObjectCacheContext parentObjectCacheContext, ChatDefinition cdGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChatPrx, int creatorUserID) throws FusionException {
      if (this.enablementChecks(creatorUsername, creatorUserID, (ClientType)null, (Short)null, false, true)) {
         if (log.isDebugEnabled()) {
            log.debug("onCreateGroupChat for groupChatGUID=" + cdGroupChat.getChatID());
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = parentObjectCacheContext.getRegistryPrx().getMessageSwitchboard();
            msp.onCreateGroupChat(cdGroupChat.toIceObject(), creatorUsername, privateChatPartnerUsername, groupChatPrx);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            msi.onCreateGroupChat(cdGroupChat.toIceObject(), creatorUsername, privateChatPartnerUsername, groupChatPrx);
         }

      }
   }

   public void onJoinGroupChat(RegistryPrx registryPrx, String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy) throws FusionException {
      if (this.enablementChecks(username, userID, (ClientType)null, (Short)null)) {
         if (log.isDebugEnabled() || debug) {
            log.info("onJoinGroupChat (overload) for userID=" + userID + " groupChatGUID=" + groupChatGUID);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = registryPrx.getMessageSwitchboard();
            msp.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            msi.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy);
         }

         if (log.isDebugEnabled() || debug) {
            log.info("onJoinGroupChat (overload): called MSI.onJoinGroupChat ok");
         }

      }
   }

   public void onLeaveGroupChat(RegistryPrx regy, String username, int userID, String groupChatGUID, UserPrx userProxy) throws FusionException {
      if (this.enablementChecks(username, userID, (ClientType)null, (Short)null)) {
         if (log.isDebugEnabled()) {
            log.debug("onLeaveGroupChat for userID=" + userID + " groupChatGUID=" + groupChatGUID);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = regy.getMessageSwitchboard();
            msp.onLeaveGroupChat(username, userID, groupChatGUID, userProxy);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            msi.onLeaveGroupChat(username, userID, groupChatGUID, userProxy);
         }

      }
   }

   public void onJoinChatRoom(ObjectCacheContext parentObjectCacheContext, String username, int userID, String chatRoomName) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
         if (this.enablementChecks(username, userID, (ClientType)null, (Short)null)) {
            if (log.isDebugEnabled()) {
               log.debug("onJoinChatRoom for userID=" + userID + " username=" + username + " chatRoomName=" + chatRoomName);
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
               MessageSwitchboardPrx msp = parentObjectCacheContext.getRegistryPrx().getMessageSwitchboard();
               msp.onJoinChatRoom(username, userID, chatRoomName);
            } else {
               MessageSwitchboardI msi = new MessageSwitchboardI();
               msi.onJoinChatRoom(username, userID, chatRoomName);
            }

         }
      }
   }

   public void onLeaveChatRoom(RegistryPrx reg, String username, int userID, String chatRoomName, UserPrx userProxy) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
         if (this.enablementChecks(username, userID, (ClientType)null, (Short)null)) {
            if (log.isDebugEnabled()) {
               log.debug("onLeaveChatRoom for userID=" + userID + " username=" + username + " chatRoomName=" + chatRoomName);
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
               MessageSwitchboardPrx msp = reg.getMessageSwitchboard();
               msp.onLeaveChatRoom(username, userID, chatRoomName, userProxy);
            } else {
               MessageSwitchboardI msi = new MessageSwitchboardI();
               msi.onLeaveChatRoom(username, userID, chatRoomName, userProxy);
            }

         }
      }
   }

   private boolean enablementChecks(String parentUsername, int parentUserID, ClientType deviceType, Short clientVersion) throws FusionException {
      return this.enablementChecks(parentUsername, parentUserID, deviceType, clientVersion, false, false);
   }

   private boolean enablementChecks(String parentUsername, int parentUserID, ClientType deviceType, Short clientVersion, boolean sendMessage, boolean createChat) throws FusionException {
      if (!this.isFeatureEnabled()) {
         return false;
      } else if (sendMessage && !this.messageSendGlobalRateLimiting(parentUsername)) {
         return false;
      } else if (sendMessage && !this.messageSendPerUserRateLimiting(parentUsername)) {
         return false;
      } else if (createChat && !this.chatCreationGlobalRateLimiting(parentUsername)) {
         return false;
      } else {
         return !createChat || this.chatCreationPerUserRateLimiting(parentUsername);
      }
   }

   private boolean messageSendGlobalRateLimiting(String parentUsername) {
      int maxPerMin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_MESSAGES_STORED_PER_MINUTE);
      if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_GLOBAL_RATE_LIMITS.toString(), "maxMessagesStoredPerMin", (long)maxPerMin, 60000L)) {
         log.warn("MAX_MESSAGES_STORED_PER_MINUTE global rate limit exceeded: could not store message from " + parentUsername);
         return false;
      } else {
         return true;
      }
   }

   private boolean messageSendPerUserRateLimiting(String parentUsername) {
      int limit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_MESSAGES_STORED_PER_USER_PER_DAY);
      if (limit < Integer.MAX_VALUE) {
         boolean result = MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_MAX_MESSAGES_STORED_PER_USER_PER_DAY.toString(), parentUsername, (long)limit, 86400000L);
         if (!result) {
            log.warn("MAX_MESSAGES_STORED_PER_USER_PER_DAY rate limit exceeded: could not store message sent by " + parentUsername);
         }

         return result;
      } else {
         return true;
      }
   }

   private boolean chatCreationGlobalRateLimiting(String parentUsername) {
      int maxPerMin = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_CHATS_STORED_PER_MINUTE);
      if (!MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_GLOBAL_RATE_LIMITS.toString(), "maxChatsStoredPerMin", (long)maxPerMin, 60000L)) {
         log.warn("MAX_CHATS_STORED_PER_MINUTE global rate limit exceeded: could not store chat created by " + parentUsername);
         return false;
      } else {
         return true;
      }
   }

   private boolean chatCreationPerUserRateLimiting(String parentUsername) {
      int limit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.MAX_CHATS_STORED_PER_USER_PER_DAY);
      if (limit < Integer.MAX_VALUE) {
         boolean result = MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CHAT_SYNC_MAX_CHATS_STORED_PER_USER_PER_DAY.toString(), parentUsername, (long)limit, 86400000L);
         if (!result) {
            log.warn("MAX_CHATS_STORED_PER_USER_PER_DAY rate limit exceeded: could not store chat created by " + parentUsername);
         }

         return result;
      } else {
         return true;
      }
   }

   public boolean onSendMessageToAllUsersInChat(MessageData messageData, String parentUsername, SessionPrx remoteSession, ChatSourceSession localSession) throws FusionException {
      UserPrx parentUser = null;
      ClientType deviceType;
      Short clientVersion;
      int userID;
      UserDataIce senderUserData;
      if (localSession != null) {
         deviceType = localSession.getDeviceType();
         clientVersion = localSession.getClientVersion();
         userID = localSession.getUserID();
         senderUserData = localSession.getUserDataIce();
      } else {
         deviceType = ClientType.fromValue(remoteSession.getDeviceTypeAsInt());
         clientVersion = remoteSession.getClientVersionIce();
         parentUser = remoteSession.getUserProxy(parentUsername);
         senderUserData = parentUser.getUserData();
         userID = senderUserData.userID;
      }

      if (!this.enablementChecks(parentUsername, userID, deviceType, clientVersion, true, false)) {
         return false;
      } else {
         if (localSession != null) {
            parentUser = localSession.findUserPrx(parentUsername);
            remoteSession = localSession.findSessionPrx(localSession.getSessionID());
         }

         this.setChatSyncExtensionFieldsOnMessage(messageData, (MessageDataIce)null, remoteSession);
         MessageDataIce mdi = messageData.toIceObject();
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = remoteSession.getMessageSwitchboard();
            return msp.onSendMessageToAllUsersInChat(remoteSession, parentUser, mdi, senderUserData);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            return msi.onSendMessageToAllUsersInChat(remoteSession, parentUser, mdi, senderUserData);
         }
      }
   }

   public boolean onSendFusionMessageToIndividual(ObjectCacheContext parentObjectCacheContext, String sessionID, UserPrx parentUserPrx, SessionPrx currentSession, ChatSourceUser parentUser, MessageDataIce messageData, String destinationUsername, Set<String> uniqueUsersPrivateChattedWith, ClientType deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture) throws FusionException {
      if (!this.enablementChecks(senderUserData.username, senderUserData.userID, deviceType, clientVersion, true, false)) {
         return false;
      } else {
         this.setChatSyncExtensionFieldsOnMessage((MessageData)null, messageData, currentSession);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = parentObjectCacheContext.getRegistryPrx().getMessageSwitchboard();
            return msp.onSendFusionMessageToIndividual(parentUser.getSessionPrx(sessionID), parentUserPrx, messageData, destinationUsername, (String[])uniqueUsersPrivateChattedWith.toArray(new String[0]), deviceType.value(), clientVersion, parentUser.getUserData(), recipientDisplayPicture);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            return msi.onSendFusionMessageToIndividual(parentUser.getSessionPrx(sessionID), parentUserPrx, messageData, destinationUsername, (String[])uniqueUsersPrivateChattedWith.toArray(new String[0]), deviceType.value(), clientVersion, parentUser.getUserData(), recipientDisplayPicture);
         }
      }
   }

   public void onSendFusionMessageToGroupChat(ObjectCacheContext parentObjectCacheContext, SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, ClientType deviceType, short clientVersion) throws FusionException {
      UserDataIce parentUserData = parentUser.getUserData();
      if (this.enablementChecks(parentUserData.username, parentUserData.userID, deviceType, clientVersion, true, false)) {
         this.setChatSyncExtensionFieldsOnMessage((MessageData)null, messageData, currentSession);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = parentObjectCacheContext.getRegistryPrx().getMessageSwitchboard();
            msp.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType.value(), clientVersion);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            msi.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType.value(), clientVersion);
         }

      }
   }

   public void onSendFusionMessageToChatRoom(ObjectCacheContext parentObjectCacheContext, SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, ClientType deviceType, short clientVersion) throws FusionException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.CHATROOM_SYNC_ENABLED)) {
         UserDataIce parentUserData = parentUser.getUserData();
         if (this.enablementChecks(parentUserData.username, parentUserData.userID, deviceType, clientVersion, true, false)) {
            this.setChatSyncExtensionFieldsOnMessage((MessageData)null, messageData, currentSession);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
               MessageSwitchboardPrx msp = parentObjectCacheContext.getRegistryPrx().getMessageSwitchboard();
               msp.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType.value(), clientVersion);
            } else {
               MessageSwitchboardI msi = new MessageSwitchboardI();
               msi.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType.value(), clientVersion);
            }

         }
      }
   }

   public void onLeavePrivateChat(ConnectionI connection, int userID, String username, String otherUser, ClientType deviceType, short clientVersion) throws FusionException {
      if (this.enablementChecks(username, userID, deviceType, clientVersion, false, false)) {
         if (log.isDebugEnabled()) {
            log.debug("onLeavePrivateChat for user=" + username + " otherUser=" + otherUser);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = connection.getGatewayContext().getRegistryPrx().getMessageSwitchboard();
            msp.onLeavePrivateChat(userID, username, otherUser, deviceType.value(), clientVersion);
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            msi.onLeavePrivateChat(userID, username, otherUser, deviceType.value(), clientVersion);
         }

      }
   }

   private void setChatSyncExtensionFieldsOnMessage(MessageData md, MessageDataIce mdi, SessionPrx currentSession) throws FusionException {
      String var4;
      String destination;
      int destType;
      if (md != null) {
         MessageDestinationData dest = (MessageDestinationData)md.messageDestinations.get(0);
         destination = dest.destination;
         destType = dest.type.value();
         var4 = md.source;
      } else {
         MessageDestinationDataIce dest = mdi.messageDestinations[0];
         destination = dest.destination;
         destType = dest.type;
         var4 = mdi.source;
      }

      String groupChatName = null;
      String groupChatOwner = null;
      if (destType == MessageDestinationData.TypeEnum.GROUP.value()) {
         GroupChatPrx groupChat = currentSession.findGroupChatObject(destination);
         groupChatName = groupChat.listOfParticipants();
         groupChatOwner = groupChat.getCreatorUsername();
         if (md != null) {
            md.groupChatName = groupChatName;
            md.groupChatOwner = groupChatOwner;
         } else {
            mdi.groupChatName = groupChatName;
            mdi.groupChatOwner = groupChatOwner;
         }
      }

   }

   public void setChatName(ConnectionI cxn, String suppliedChatID, byte chatType, String chatName) throws FusionException {
      if (this.enablementChecks(cxn.getUsername(), cxn.getUserID(), cxn.getDeviceType(), cxn.getClientVersion(), false, false)) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.STANDALONE_MESSAGE_SWITCHBOARD_ENABLED)) {
            MessageSwitchboardPrx msp = cxn.getGatewayContext().getRegistryPrx().getMessageSwitchboard();
            msp.setChatName(cxn.getUsername(), suppliedChatID, chatType, chatName, cxn.getGatewayContext().getRegistryPrx());
         } else {
            MessageSwitchboardI msi = new MessageSwitchboardI();
            msi.setChatName(cxn.getUsername(), suppliedChatID, chatType, chatName, cxn.getGatewayContext().getRegistryPrx());
         }

      }
   }

   public void getAndPushMessageStatusEvents(ConnectionI connection, String username, byte chatType, String suppliedChatID, Long startTime, Long endTime, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTxnId) throws FusionException {
      if (this.enablementChecks(connection.getUsername(), connection.getUserID(), connection.getDeviceType(), connection.getClientVersion(), false, false)) {
         MessageSwitchboardI msi = new MessageSwitchboardI();
         msi.getAndPushMessageStatusEvents(connection, username, chatType, suppliedChatID, startTime, endTime, limit, cxn, deviceType, clientVersion, fusionPktTxnId);
      }
   }

   public void getAndPushMessageStatusEvents(ConnectionI connection, String username, byte chatType, String suppliedChatID, String[] messageGUIDs, long[] messageTimestamps, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTxnId) throws FusionException {
      if (this.enablementChecks(connection.getUsername(), connection.getUserID(), connection.getDeviceType(), connection.getClientVersion(), false, false)) {
         MessageSwitchboardI msi = new MessageSwitchboardI();
         msi.getAndPushMessageStatusEvents(connection, username, chatType, suppliedChatID, messageGUIDs, messageTimestamps, limit, cxn, deviceType, clientVersion, fusionPktTxnId);
      }
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageSwitchboardDispatcher.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }

   private static class SingletonHolder {
      public static final MessageSwitchboardDispatcher INSTANCE = new MessageSwitchboardDispatcher();
   }
}
