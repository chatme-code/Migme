package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _ConnectionWSTie extends _ConnectionWSDisp implements TieBase {
   private _ConnectionWSOperations _ice_delegate;

   public _ConnectionWSTie() {
   }

   public _ConnectionWSTie(_ConnectionWSOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_ConnectionWSOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _ConnectionWSTie) ? false : this._ice_delegate.equals(((_ConnectionWSTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency, Current __current) throws FusionException {
      this._ice_delegate.accountBalanceChanged(balance, fundedBalance, currency, __current);
   }

   public void avatarChanged(String displayPicture, String statusMessage, Current __current) throws FusionException {
      this._ice_delegate.avatarChanged(displayPicture, statusMessage, __current);
   }

   public void contactAdded(ContactDataIce contact, int contactListVersion, boolean guaranteedIsNew, Current __current) throws FusionException {
      this._ice_delegate.contactAdded(contact, contactListVersion, guaranteedIsNew, __current);
   }

   public void contactChangedDisplayPictureOneWay(int contactID, String displayPicture, long timeStamp, Current __current) {
      this._ice_delegate.contactChangedDisplayPictureOneWay(contactID, displayPicture, timeStamp, __current);
   }

   public void contactChangedPresenceOneWay(int contactID, int imType, int presence, Current __current) {
      this._ice_delegate.contactChangedPresenceOneWay(contactID, imType, presence, __current);
   }

   public void contactChangedStatusMessageOneWay(int contactID, String statusMessage, long timeStamp, Current __current) {
      this._ice_delegate.contactChangedStatusMessageOneWay(contactID, statusMessage, timeStamp, __current);
   }

   public void contactGroupAdded(ContactGroupDataIce contactGroup, int contactListVersion, Current __current) throws FusionException {
      this._ice_delegate.contactGroupAdded(contactGroup, contactListVersion, __current);
   }

   public void contactGroupRemoved(int contactGroupID, int contactListVersion, Current __current) throws FusionException {
      this._ice_delegate.contactGroupRemoved(contactGroupID, contactListVersion, __current);
   }

   public void contactRemoved(int contactID, int contactListVersion, Current __current) throws FusionException {
      this._ice_delegate.contactRemoved(contactID, contactListVersion, __current);
   }

   public void contactRequest(String contactUsername, int outstandingRequests, Current __current) throws FusionException {
      this._ice_delegate.contactRequest(contactUsername, outstandingRequests, __current);
   }

   public void contactRequestAccepted(ContactDataIce contact, int contactListVersion, int outstandingRequests, Current __current) throws FusionException {
      this._ice_delegate.contactRequestAccepted(contact, contactListVersion, outstandingRequests, __current);
   }

   public void contactRequestRejected(String contactUsername, int outstandingRequests, Current __current) throws FusionException {
      this._ice_delegate.contactRequestRejected(contactUsername, outstandingRequests, __current);
   }

   public void disconnect(String reason, Current __current) throws FusionException {
      this._ice_delegate.disconnect(reason, __current);
   }

   public void emailNotification(int unreadEmailCount, Current __current) throws FusionException {
      this._ice_delegate.emailNotification(unreadEmailCount, __current);
   }

   public void emoticonsChanged(String[] hotKeys, String[] alternateKeys, Current __current) throws FusionException {
      this._ice_delegate.emoticonsChanged(hotKeys, alternateKeys, __current);
   }

   public short getClientVersion(Current __current) {
      return this._ice_delegate.getClientVersion(__current);
   }

   public int getDeviceTypeAsInt(Current __current) {
      return this._ice_delegate.getDeviceTypeAsInt(__current);
   }

   public String getMobileDevice(Current __current) {
      return this._ice_delegate.getMobileDevice(__current);
   }

   public ChatRoomDataIce[] getPopularChatRooms(Current __current) throws FusionException {
      return this._ice_delegate.getPopularChatRooms(__current);
   }

   public String getRemoteIPAddress(Current __current) {
      return this._ice_delegate.getRemoteIPAddress(__current);
   }

   public SessionPrx getSessionObject(Current __current) {
      return this._ice_delegate.getSessionObject(__current);
   }

   public String getUserAgent(Current __current) {
      return this._ice_delegate.getUserAgent(__current);
   }

   public UserPrx getUserObject(Current __current) {
      return this._ice_delegate.getUserObject(__current);
   }

   public String getUsername(Current __current) {
      return this._ice_delegate.getUsername(__current);
   }

   public void logout(Current __current) {
      this._ice_delegate.logout(__current);
   }

   public void otherIMConferenceCreated(int imType, String conferenceID, String creator, Current __current) throws FusionException {
      this._ice_delegate.otherIMConferenceCreated(imType, conferenceID, creator, __current);
   }

   public void otherIMLoggedIn(int imType, Current __current) throws FusionException {
      this._ice_delegate.otherIMLoggedIn(imType, __current);
   }

   public void otherIMLoggedOut(int imType, String reason, Current __current) throws FusionException {
      this._ice_delegate.otherIMLoggedOut(imType, reason, __current);
   }

   public void packetProcessed(byte[] result, Current __current) {
      this._ice_delegate.packetProcessed(result, __current);
   }

   public void privateChatNowAGroupChat(String groupChatID, String creator, Current __current) throws FusionException {
      this._ice_delegate.privateChatNowAGroupChat(groupChatID, creator, __current);
   }

   public boolean processPacket(ConnectionPrx requestingConnection, byte[] packet, Current __current) throws FusionException {
      return this._ice_delegate.processPacket(requestingConnection, packet, __current);
   }

   public void pushNotification(Message msg, Current __current) throws FusionException {
      this._ice_delegate.pushNotification(msg, __current);
   }

   public void putAlertMessage(String message, String title, short timeout, Current __current) throws FusionException {
      this._ice_delegate.putAlertMessage(message, title, timeout, __current);
   }

   public void putAlertMessageOneWay(String message, String title, short timeout, Current __current) {
      this._ice_delegate.putAlertMessageOneWay(message, title, timeout, __current);
   }

   public void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone, Current __current) throws FusionException {
      this._ice_delegate.putAnonymousCallNotification(requestingUsername, requestingMobilePhone, __current);
   }

   public void putEvent(UserEventIce event, Current __current) throws FusionException {
      this._ice_delegate.putEvent(event, __current);
   }

   public void putFileReceived(MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putFileReceived(message, __current);
   }

   public void putGenericPacket(byte[] packet, Current __current) throws FusionException {
      this._ice_delegate.putGenericPacket(packet, __current);
   }

   public void putMessage(MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putMessage(message, __current);
   }

   public void putMessageAsync_async(AMD_Connection_putMessageAsync __cb, MessageDataIce message, Current __current) throws FusionException {
      this._ice_delegate.putMessageAsync_async(__cb, message, __current);
   }

   public void putMessageOneWay(MessageDataIce message, Current __current) {
      this._ice_delegate.putMessageOneWay(message, __current);
   }

   public void putMessageStatusEvent(MessageStatusEventIce mseIce, Current __current) throws FusionException {
      this._ice_delegate.putMessageStatusEvent(mseIce, __current);
   }

   public void putMessageStatusEvents(MessageStatusEventIce[] events, short requestTxnId, Current __current) throws FusionException {
      this._ice_delegate.putMessageStatusEvents(events, requestTxnId, __current);
   }

   public void putMessages(MessageDataIce[] messages, Current __current) throws FusionException {
      this._ice_delegate.putMessages(messages, __current);
   }

   public void putSerializedPacket(byte[] packet, Current __current) throws FusionException {
      this._ice_delegate.putSerializedPacket(packet, __current);
   }

   public void putSerializedPacketOneWay(byte[] packet, Current __current) {
      this._ice_delegate.putSerializedPacketOneWay(packet, __current);
   }

   public void putServerQuestion(String message, String url, Current __current) throws FusionException {
      this._ice_delegate.putServerQuestion(message, url, __current);
   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol, Current __current) throws FusionException {
      this._ice_delegate.putWebCallNotification(source, destination, gateway, gatewayName, protocol, __current);
   }

   public void silentlyDropIncomingPackets(Current __current) {
      this._ice_delegate.silentlyDropIncomingPackets(__current);
   }

   public void themeChanged(String themeLocation, Current __current) throws FusionException {
      this._ice_delegate.themeChanged(themeLocation, __current);
   }

   public void accessed(Current __current) {
      this._ice_delegate.accessed(__current);
   }

   public void addRemoteChildConnectionWS(String uuid, ConnectionWSPrx childConnectionWS, Current __current) {
      this._ice_delegate.addRemoteChildConnectionWS(uuid, childConnectionWS, __current);
   }

   public void removeRemoteChildConnectionWS(String uuid, ConnectionWSPrx childConnectionWS, Current __current) {
      this._ice_delegate.removeRemoteChildConnectionWS(uuid, childConnectionWS, __current);
   }
}
