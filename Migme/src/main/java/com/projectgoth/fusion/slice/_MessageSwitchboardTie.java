package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _MessageSwitchboardTie extends _MessageSwitchboardDisp implements TieBase {
   private _MessageSwitchboardOperations _ice_delegate;

   public _MessageSwitchboardTie() {
   }

   public _MessageSwitchboardTie(_MessageSwitchboardOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_MessageSwitchboardOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _MessageSwitchboardTie) ? false : this._ice_delegate.equals(((_MessageSwitchboardTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public GroupChatPrx ensureGroupChatExists(SessionPrx currentSession, String groupChatID, Current __current) throws FusionException {
      return this._ice_delegate.ensureGroupChatExists(currentSession, groupChatID, __current);
   }

   public void getAndPushMessages(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, Current __current) throws FusionException {
      this._ice_delegate.getAndPushMessages(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, __current);
   }

   public void getAndPushMessages2(String username, byte chatType, String suppliedChatID, long oldestMessageTimestamp, long newestMessageTimestamp, int limit, ConnectionPrx cxn, int deviceType, short clientVersion, short fusionPktTransactionId, Current __current) throws FusionException {
      this._ice_delegate.getAndPushMessages2(username, chatType, suppliedChatID, oldestMessageTimestamp, newestMessageTimestamp, limit, cxn, deviceType, clientVersion, fusionPktTransactionId, __current);
   }

   public ChatDefinitionIce[] getChats(int userID, int chatListVersion, int limit, byte chatType, Current __current) throws FusionException {
      return this._ice_delegate.getChats(userID, chatListVersion, limit, chatType, __current);
   }

   public ChatDefinitionIce[] getChats2(int userID, int chatListVersion, int limit, byte chatType, ConnectionPrx cxn, Current __current) throws FusionException {
      return this._ice_delegate.getChats2(userID, chatListVersion, limit, chatType, cxn, __current);
   }

   public boolean isUserChatSyncEnabled(ConnectionPrx cxn, String username, int userID, Current __current) throws FusionException {
      return this._ice_delegate.isUserChatSyncEnabled(cxn, username, userID, __current);
   }

   public void onCreateGroupChat(ChatDefinitionIce storedGroupChat, String creatorUsername, String privateChatPartnerUsername, GroupChatPrx groupChatRemote, Current __current) throws FusionException {
      this._ice_delegate.onCreateGroupChat(storedGroupChat, creatorUsername, privateChatPartnerUsername, groupChatRemote, __current);
   }

   public void onCreatePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Current __current) throws FusionException {
      this._ice_delegate.onCreatePrivateChat(userID, username, otherUser, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __current);
   }

   public void onGetChats(ConnectionPrx cxn, int userID, int chatListVersion, int limit, byte chatType, short transactionId, String parentUsername, Current __current) throws FusionException {
      this._ice_delegate.onGetChats(cxn, userID, chatListVersion, limit, chatType, transactionId, parentUsername, __current);
   }

   public void onJoinChatRoom(String username, int userID, String chatRoomName, Current __current) throws FusionException {
      this._ice_delegate.onJoinChatRoom(username, userID, chatRoomName, __current);
   }

   public void onJoinGroupChat(String username, int userID, String groupChatGUID, boolean debug, UserPrx userProxy, Current __current) throws FusionException {
      this._ice_delegate.onJoinGroupChat(username, userID, groupChatGUID, debug, userProxy, __current);
   }

   public void onLeaveChatRoom(String username, int userID, String chatRoomName, UserPrx userProxy, Current __current) throws FusionException {
      this._ice_delegate.onLeaveChatRoom(username, userID, chatRoomName, userProxy, __current);
   }

   public void onLeaveGroupChat(String username, int userID, String groupChatGUID, UserPrx userProxy, Current __current) throws FusionException {
      this._ice_delegate.onLeaveGroupChat(username, userID, groupChatGUID, userProxy, __current);
   }

   public void onLeavePrivateChat(int userID, String username, String otherUser, int deviceType, short clientVersion, Current __current) throws FusionException {
      this._ice_delegate.onLeavePrivateChat(userID, username, otherUser, deviceType, clientVersion, __current);
   }

   public void onLogon(int userID, SessionPrx sess, short transactionID, String parentUsername, Current __current) throws FusionException {
      this._ice_delegate.onLogon(userID, sess, transactionID, parentUsername, __current);
   }

   public void onSendFusionMessageToChatRoom(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String chatRoomName, int deviceType, short clientVersion, Current __current) throws FusionException {
      this._ice_delegate.onSendFusionMessageToChatRoom(currentSession, parentUser, messageData, chatRoomName, deviceType, clientVersion, __current);
   }

   public void onSendFusionMessageToGroupChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String groupChatID, int deviceType, short clientVersion, Current __current) throws FusionException {
      this._ice_delegate.onSendFusionMessageToGroupChat(currentSession, parentUser, messageData, groupChatID, deviceType, clientVersion, __current);
   }

   public boolean onSendFusionMessageToIndividual(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, String destinationUsername, String[] uniqueUsersPrivateChattedWith, int deviceType, short clientVersion, UserDataIce senderUserData, String recipientDisplayPicture, Current __current) throws FusionException {
      return this._ice_delegate.onSendFusionMessageToIndividual(currentSession, parentUser, messageData, destinationUsername, uniqueUsersPrivateChattedWith, deviceType, clientVersion, senderUserData, recipientDisplayPicture, __current);
   }

   public boolean onSendMessageToAllUsersInChat(SessionPrx currentSession, UserPrx parentUser, MessageDataIce messageData, UserDataIce senderUserData, Current __current) throws FusionException {
      return this._ice_delegate.onSendMessageToAllUsersInChat(currentSession, parentUser, messageData, senderUserData, __current);
   }

   public void setChatName(String parentUsername, String suppliedChatID, byte chatType, String chatName, RegistryPrx regy, Current __current) throws FusionException {
      this._ice_delegate.setChatName(parentUsername, suppliedChatID, chatType, chatName, regy, __current);
   }
}
