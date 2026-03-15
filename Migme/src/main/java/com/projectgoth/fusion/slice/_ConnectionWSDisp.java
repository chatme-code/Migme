package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;

public abstract class _ConnectionWSDisp extends ObjectImpl implements ConnectionWS {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::Connection", "::com::projectgoth::fusion::slice::ConnectionWS"};
   private static final String[] __all = new String[]{"accessed", "accountBalanceChanged", "addRemoteChildConnectionWS", "avatarChanged", "contactAdded", "contactChangedDisplayPictureOneWay", "contactChangedPresenceOneWay", "contactChangedStatusMessageOneWay", "contactGroupAdded", "contactGroupRemoved", "contactRemoved", "contactRequest", "contactRequestAccepted", "contactRequestRejected", "disconnect", "emailNotification", "emoticonsChanged", "getClientVersion", "getDeviceTypeAsInt", "getMobileDevice", "getPopularChatRooms", "getRemoteIPAddress", "getSessionObject", "getUserAgent", "getUserObject", "getUsername", "ice_id", "ice_ids", "ice_isA", "ice_ping", "logout", "otherIMConferenceCreated", "otherIMLoggedIn", "otherIMLoggedOut", "packetProcessed", "privateChatNowAGroupChat", "processPacket", "pushNotification", "putAlertMessage", "putAlertMessageOneWay", "putAnonymousCallNotification", "putEvent", "putFileReceived", "putGenericPacket", "putMessage", "putMessageAsync", "putMessageOneWay", "putMessageStatusEvent", "putMessageStatusEvents", "putMessages", "putSerializedPacket", "putSerializedPacketOneWay", "putServerQuestion", "putWebCallNotification", "removeRemoteChildConnectionWS", "silentlyDropIncomingPackets", "themeChanged"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[2];
   }

   public String ice_id(Current __current) {
      return __ids[2];
   }

   public static String ice_staticId() {
      return __ids[2];
   }

   public final void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency) throws FusionException {
      this.accountBalanceChanged(balance, fundedBalance, currency, (Current)null);
   }

   public final void avatarChanged(String displayPicture, String statusMessage) throws FusionException {
      this.avatarChanged(displayPicture, statusMessage, (Current)null);
   }

   public final void contactAdded(ContactDataIce contact, int contactListVersion, boolean guaranteedIsNew) throws FusionException {
      this.contactAdded(contact, contactListVersion, guaranteedIsNew, (Current)null);
   }

   public final void contactChangedDisplayPictureOneWay(int contactID, String displayPicture, long timeStamp) {
      this.contactChangedDisplayPictureOneWay(contactID, displayPicture, timeStamp, (Current)null);
   }

   public final void contactChangedPresenceOneWay(int contactID, int imType, int presence) {
      this.contactChangedPresenceOneWay(contactID, imType, presence, (Current)null);
   }

   public final void contactChangedStatusMessageOneWay(int contactID, String statusMessage, long timeStamp) {
      this.contactChangedStatusMessageOneWay(contactID, statusMessage, timeStamp, (Current)null);
   }

   public final void contactGroupAdded(ContactGroupDataIce contactGroup, int contactListVersion) throws FusionException {
      this.contactGroupAdded(contactGroup, contactListVersion, (Current)null);
   }

   public final void contactGroupRemoved(int contactGroupID, int contactListVersion) throws FusionException {
      this.contactGroupRemoved(contactGroupID, contactListVersion, (Current)null);
   }

   public final void contactRemoved(int contactID, int contactListVersion) throws FusionException {
      this.contactRemoved(contactID, contactListVersion, (Current)null);
   }

   public final void contactRequest(String contactUsername, int outstandingRequests) throws FusionException {
      this.contactRequest(contactUsername, outstandingRequests, (Current)null);
   }

   public final void contactRequestAccepted(ContactDataIce contact, int contactListVersion, int outstandingRequests) throws FusionException {
      this.contactRequestAccepted(contact, contactListVersion, outstandingRequests, (Current)null);
   }

   public final void contactRequestRejected(String contactUsername, int outstandingRequests) throws FusionException {
      this.contactRequestRejected(contactUsername, outstandingRequests, (Current)null);
   }

   public final void disconnect(String reason) throws FusionException {
      this.disconnect(reason, (Current)null);
   }

   public final void emailNotification(int unreadEmailCount) throws FusionException {
      this.emailNotification(unreadEmailCount, (Current)null);
   }

   public final void emoticonsChanged(String[] hotKeys, String[] alternateKeys) throws FusionException {
      this.emoticonsChanged(hotKeys, alternateKeys, (Current)null);
   }

   public final short getClientVersion() {
      return this.getClientVersion((Current)null);
   }

   public final int getDeviceTypeAsInt() {
      return this.getDeviceTypeAsInt((Current)null);
   }

   public final String getMobileDevice() {
      return this.getMobileDevice((Current)null);
   }

   public final ChatRoomDataIce[] getPopularChatRooms() throws FusionException {
      return this.getPopularChatRooms((Current)null);
   }

   public final String getRemoteIPAddress() {
      return this.getRemoteIPAddress((Current)null);
   }

   public final SessionPrx getSessionObject() {
      return this.getSessionObject((Current)null);
   }

   public final String getUserAgent() {
      return this.getUserAgent((Current)null);
   }

   public final UserPrx getUserObject() {
      return this.getUserObject((Current)null);
   }

   public final String getUsername() {
      return this.getUsername((Current)null);
   }

   public final void logout() {
      this.logout((Current)null);
   }

   public final void otherIMConferenceCreated(int imType, String conferenceID, String creator) throws FusionException {
      this.otherIMConferenceCreated(imType, conferenceID, creator, (Current)null);
   }

   public final void otherIMLoggedIn(int imType) throws FusionException {
      this.otherIMLoggedIn(imType, (Current)null);
   }

   public final void otherIMLoggedOut(int imType, String reason) throws FusionException {
      this.otherIMLoggedOut(imType, reason, (Current)null);
   }

   public final void packetProcessed(byte[] result) {
      this.packetProcessed(result, (Current)null);
   }

   public final void privateChatNowAGroupChat(String groupChatID, String creator) throws FusionException {
      this.privateChatNowAGroupChat(groupChatID, creator, (Current)null);
   }

   public final boolean processPacket(ConnectionPrx requestingConnection, byte[] packet) throws FusionException {
      return this.processPacket(requestingConnection, packet, (Current)null);
   }

   public final void pushNotification(Message msg) throws FusionException {
      this.pushNotification(msg, (Current)null);
   }

   public final void putAlertMessage(String message, String title, short timeout) throws FusionException {
      this.putAlertMessage(message, title, timeout, (Current)null);
   }

   public final void putAlertMessageOneWay(String message, String title, short timeout) {
      this.putAlertMessageOneWay(message, title, timeout, (Current)null);
   }

   public final void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone) throws FusionException {
      this.putAnonymousCallNotification(requestingUsername, requestingMobilePhone, (Current)null);
   }

   public final void putEvent(UserEventIce event) throws FusionException {
      this.putEvent(event, (Current)null);
   }

   public final void putFileReceived(MessageDataIce message) throws FusionException {
      this.putFileReceived(message, (Current)null);
   }

   public final void putGenericPacket(byte[] packet) throws FusionException {
      this.putGenericPacket(packet, (Current)null);
   }

   public final void putMessage(MessageDataIce message) throws FusionException {
      this.putMessage(message, (Current)null);
   }

   public final void putMessageAsync_async(AMD_Connection_putMessageAsync __cb, MessageDataIce message) throws FusionException {
      this.putMessageAsync_async(__cb, message, (Current)null);
   }

   public final void putMessageOneWay(MessageDataIce message) {
      this.putMessageOneWay(message, (Current)null);
   }

   public final void putMessageStatusEvent(MessageStatusEventIce mseIce) throws FusionException {
      this.putMessageStatusEvent(mseIce, (Current)null);
   }

   public final void putMessageStatusEvents(MessageStatusEventIce[] events, short requestTxnId) throws FusionException {
      this.putMessageStatusEvents(events, requestTxnId, (Current)null);
   }

   public final void putMessages(MessageDataIce[] messages) throws FusionException {
      this.putMessages(messages, (Current)null);
   }

   public final void putSerializedPacket(byte[] packet) throws FusionException {
      this.putSerializedPacket(packet, (Current)null);
   }

   public final void putSerializedPacketOneWay(byte[] packet) {
      this.putSerializedPacketOneWay(packet, (Current)null);
   }

   public final void putServerQuestion(String message, String url) throws FusionException {
      this.putServerQuestion(message, url, (Current)null);
   }

   public final void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol) throws FusionException {
      this.putWebCallNotification(source, destination, gateway, gatewayName, protocol, (Current)null);
   }

   public final void silentlyDropIncomingPackets() {
      this.silentlyDropIncomingPackets((Current)null);
   }

   public final void themeChanged(String themeLocation) throws FusionException {
      this.themeChanged(themeLocation, (Current)null);
   }

   public final void accessed() {
      this.accessed((Current)null);
   }

   public final void addRemoteChildConnectionWS(String uuid, ConnectionWSPrx childConnectionWS) {
      this.addRemoteChildConnectionWS(uuid, childConnectionWS, (Current)null);
   }

   public final void removeRemoteChildConnectionWS(String uuid, ConnectionWSPrx childConnectionWS) {
      this.removeRemoteChildConnectionWS(uuid, childConnectionWS, (Current)null);
   }

   public static DispatchStatus ___accessed(ConnectionWS __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      __obj.accessed(__current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___addRemoteChildConnectionWS(ConnectionWS __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String uuid = __is.readString();
      ConnectionWSPrx childConnectionWS = ConnectionWSPrxHelper.__read(__is);
      __is.endReadEncaps();
      __obj.addRemoteChildConnectionWS(uuid, childConnectionWS, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___removeRemoteChildConnectionWS(ConnectionWS __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String uuid = __is.readString();
      ConnectionWSPrx childConnectionWS = ConnectionWSPrxHelper.__read(__is);
      __is.endReadEncaps();
      __obj.removeRemoteChildConnectionWS(uuid, childConnectionWS, __current);
      return DispatchStatus.DispatchOK;
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___accessed(this, in, __current);
         case 1:
            return _ConnectionDisp.___accountBalanceChanged(this, in, __current);
         case 2:
            return ___addRemoteChildConnectionWS(this, in, __current);
         case 3:
            return _ConnectionDisp.___avatarChanged(this, in, __current);
         case 4:
            return _ConnectionDisp.___contactAdded(this, in, __current);
         case 5:
            return _ConnectionDisp.___contactChangedDisplayPictureOneWay(this, in, __current);
         case 6:
            return _ConnectionDisp.___contactChangedPresenceOneWay(this, in, __current);
         case 7:
            return _ConnectionDisp.___contactChangedStatusMessageOneWay(this, in, __current);
         case 8:
            return _ConnectionDisp.___contactGroupAdded(this, in, __current);
         case 9:
            return _ConnectionDisp.___contactGroupRemoved(this, in, __current);
         case 10:
            return _ConnectionDisp.___contactRemoved(this, in, __current);
         case 11:
            return _ConnectionDisp.___contactRequest(this, in, __current);
         case 12:
            return _ConnectionDisp.___contactRequestAccepted(this, in, __current);
         case 13:
            return _ConnectionDisp.___contactRequestRejected(this, in, __current);
         case 14:
            return _ConnectionDisp.___disconnect(this, in, __current);
         case 15:
            return _ConnectionDisp.___emailNotification(this, in, __current);
         case 16:
            return _ConnectionDisp.___emoticonsChanged(this, in, __current);
         case 17:
            return _ConnectionDisp.___getClientVersion(this, in, __current);
         case 18:
            return _ConnectionDisp.___getDeviceTypeAsInt(this, in, __current);
         case 19:
            return _ConnectionDisp.___getMobileDevice(this, in, __current);
         case 20:
            return _ConnectionDisp.___getPopularChatRooms(this, in, __current);
         case 21:
            return _ConnectionDisp.___getRemoteIPAddress(this, in, __current);
         case 22:
            return _ConnectionDisp.___getSessionObject(this, in, __current);
         case 23:
            return _ConnectionDisp.___getUserAgent(this, in, __current);
         case 24:
            return _ConnectionDisp.___getUserObject(this, in, __current);
         case 25:
            return _ConnectionDisp.___getUsername(this, in, __current);
         case 26:
            return ___ice_id(this, in, __current);
         case 27:
            return ___ice_ids(this, in, __current);
         case 28:
            return ___ice_isA(this, in, __current);
         case 29:
            return ___ice_ping(this, in, __current);
         case 30:
            return _ConnectionDisp.___logout(this, in, __current);
         case 31:
            return _ConnectionDisp.___otherIMConferenceCreated(this, in, __current);
         case 32:
            return _ConnectionDisp.___otherIMLoggedIn(this, in, __current);
         case 33:
            return _ConnectionDisp.___otherIMLoggedOut(this, in, __current);
         case 34:
            return _ConnectionDisp.___packetProcessed(this, in, __current);
         case 35:
            return _ConnectionDisp.___privateChatNowAGroupChat(this, in, __current);
         case 36:
            return _ConnectionDisp.___processPacket(this, in, __current);
         case 37:
            return _ConnectionDisp.___pushNotification(this, in, __current);
         case 38:
            return _ConnectionDisp.___putAlertMessage(this, in, __current);
         case 39:
            return _ConnectionDisp.___putAlertMessageOneWay(this, in, __current);
         case 40:
            return _ConnectionDisp.___putAnonymousCallNotification(this, in, __current);
         case 41:
            return _ConnectionDisp.___putEvent(this, in, __current);
         case 42:
            return _ConnectionDisp.___putFileReceived(this, in, __current);
         case 43:
            return _ConnectionDisp.___putGenericPacket(this, in, __current);
         case 44:
            return _ConnectionDisp.___putMessage(this, in, __current);
         case 45:
            return _ConnectionDisp.___putMessageAsync(this, in, __current);
         case 46:
            return _ConnectionDisp.___putMessageOneWay(this, in, __current);
         case 47:
            return _ConnectionDisp.___putMessageStatusEvent(this, in, __current);
         case 48:
            return _ConnectionDisp.___putMessageStatusEvents(this, in, __current);
         case 49:
            return _ConnectionDisp.___putMessages(this, in, __current);
         case 50:
            return _ConnectionDisp.___putSerializedPacket(this, in, __current);
         case 51:
            return _ConnectionDisp.___putSerializedPacketOneWay(this, in, __current);
         case 52:
            return _ConnectionDisp.___putServerQuestion(this, in, __current);
         case 53:
            return _ConnectionDisp.___putWebCallNotification(this, in, __current);
         case 54:
            return ___removeRemoteChildConnectionWS(this, in, __current);
         case 55:
            return _ConnectionDisp.___silentlyDropIncomingPackets(this, in, __current);
         case 56:
            return _ConnectionDisp.___themeChanged(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::ConnectionWS was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::ConnectionWS was not generated with stream support";
      throw ex;
   }
}
