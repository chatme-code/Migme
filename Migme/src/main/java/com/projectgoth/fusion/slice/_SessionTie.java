package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _SessionTie extends _SessionDisp implements TieBase {
   private _SessionOperations _ice_delegate;

   public _SessionTie() {
   }

   public _SessionTie(_SessionOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_SessionOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _SessionTie) ? false : this._ice_delegate.equals(((_SessionTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void chatroomJoined(ChatRoomPrx roomProxy, String name, Current __current) {
      this._ice_delegate.chatroomJoined(roomProxy, name, __current);
   }

   public void endSession_async(AMD_Session_endSession __cb, Current __current) throws FusionException {
      this._ice_delegate.endSession_async(__cb, __current);
   }

   public void endSessionOneWay(Current __current) {
      this._ice_delegate.endSessionOneWay(__current);
   }

   public GroupChatPrx findGroupChatObject(String groupChatID, Current __current) throws FusionException {
      return this._ice_delegate.findGroupChatObject(groupChatID, __current);
   }

   public void friendInvitedByPhoneNumber(Current __current) {
      this._ice_delegate.friendInvitedByPhoneNumber(__current);
   }

   public void friendInvitedByUsername(Current __current) {
      this._ice_delegate.friendInvitedByUsername(__current);
   }

   public int getChatListVersion(Current __current) throws FusionException {
      return this._ice_delegate.getChatListVersion(__current);
   }

   public short getClientVersionIce(Current __current) {
      return this._ice_delegate.getClientVersionIce(__current);
   }

   public int getDeviceTypeAsInt(Current __current) {
      return this._ice_delegate.getDeviceTypeAsInt(__current);
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Current __current) throws FusionException {
      return this._ice_delegate.getMessageSwitchboard(__current);
   }

   public String getMobileDeviceIce(Current __current) {
      return this._ice_delegate.getMobileDeviceIce(__current);
   }

   public String getParentUsername(Current __current) throws FusionException {
      return this._ice_delegate.getParentUsername(__current);
   }

   public String getRemoteIPAddress(Current __current) {
      return this._ice_delegate.getRemoteIPAddress(__current);
   }

   public String getSessionID(Current __current) {
      return this._ice_delegate.getSessionID(__current);
   }

   public SessionMetricsIce getSessionMetrics(Current __current) {
      return this._ice_delegate.getSessionMetrics(__current);
   }

   public String getUserAgentIce(Current __current) {
      return this._ice_delegate.getUserAgentIce(__current);
   }

   public UserPrx getUserProxy(String username, Current __current) throws FusionException {
      return this._ice_delegate.getUserProxy(username, __current);
   }

   public void groupChatJoined(String id, Current __current) {
      this._ice_delegate.groupChatJoined(id, __current);
   }

   public void groupChatJoinedMultiple(String id, int increment, Current __current) {
      this._ice_delegate.groupChatJoinedMultiple(id, increment, __current);
   }

   public void notifyUserJoinedChatRoomOneWay(String chatroomname, String username, boolean isAdministrator, boolean isMuted, Current __current) {
      this._ice_delegate.notifyUserJoinedChatRoomOneWay(chatroomname, username, isAdministrator, isMuted, __current);
   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Current __current) throws FusionException {
      this._ice_delegate.notifyUserJoinedGroupChat(groupChatId, username, isMuted, __current);
   }

   public void notifyUserLeftChatRoomOneWay(String chatroomname, String username, Current __current) {
      this._ice_delegate.notifyUserLeftChatRoomOneWay(chatroomname, username, __current);
   }

   public void notifyUserLeftGroupChat(String groupChatId, String username, Current __current) throws FusionException {
      this._ice_delegate.notifyUserLeftGroupChat(groupChatId, username, __current);
   }

   public void photoUploaded(Current __current) {
      this._ice_delegate.photoUploaded(__current);
   }

   public boolean privateChattedWith(String username, Current __current) {
      return this._ice_delegate.privateChattedWith(username, __current);
   }

   public void profileEdited(Current __current) {
      this._ice_delegate.profileEdited(__current);
   }

   public void putAlertMessage(String message, String title, short timeout, Current __current) throws FusionException {
      this._ice_delegate.putAlertMessage(message, title, timeout, __current);
   }

   public void putAlertMessageOneWay(String message, String title, short timeout, Current __current) {
      this._ice_delegate.putAlertMessageOneWay(message, title, timeout, __current);
   }

   public void putMessage_async(AMD_Session_putMessage __cb, MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putMessage_async(__cb, message, __current);
   }

   public void putMessageOneWay(MessageDataIce message, Current __current) {
      this._ice_delegate.putMessageOneWay(message, __current);
   }

   public void putSerializedPacket(byte[] packet, Current __current) throws FusionException {
      this._ice_delegate.putSerializedPacket(packet, __current);
   }

   public void putSerializedPacketOneWay(byte[] packet, Current __current) {
      this._ice_delegate.putSerializedPacketOneWay(packet, __current);
   }

   public void sendGroupChatParticipantArrays(String groupChatId, byte imType, String[] participants, String[] mutedParticipants, Current __current) throws FusionException {
      this._ice_delegate.sendGroupChatParticipantArrays(groupChatId, imType, participants, mutedParticipants, __current);
   }

   public void sendGroupChatParticipants(String groupChatId, byte imType, String participants, String mutedParticipants, Current __current) throws FusionException {
      this._ice_delegate.sendGroupChatParticipants(groupChatId, imType, participants, mutedParticipants, __current);
   }

   public void sendMessage_async(AMD_Session_sendMessage __cb, MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.sendMessage_async(__cb, message, __current);
   }

   public void sendMessageBackToUserAsEmote(MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.sendMessageBackToUserAsEmote(message, __current);
   }

   public void setChatListVersion(int version, Current __current) throws FusionException {
      this._ice_delegate.setChatListVersion(version, __current);
   }

   public void setCurrentChatListGroupChatSubset(ChatListIce ccl, Current __current) {
      this._ice_delegate.setCurrentChatListGroupChatSubset(ccl, __current);
   }

   public void setLanguage(String language, Current __current) {
      this._ice_delegate.setLanguage(language, __current);
   }

   public void setPresence(int presence, Current __current) throws FusionException {
      this._ice_delegate.setPresence(presence, __current);
   }

   public void silentlyDropIncomingPackets(Current __current) {
      this._ice_delegate.silentlyDropIncomingPackets(__current);
   }

   public void statusMessageSet(Current __current) {
      this._ice_delegate.statusMessageSet(__current);
   }

   public void themeUpdated(Current __current) {
      this._ice_delegate.themeUpdated(__current);
   }

   public void touch(Current __current) throws FusionException {
      this._ice_delegate.touch(__current);
   }
}
