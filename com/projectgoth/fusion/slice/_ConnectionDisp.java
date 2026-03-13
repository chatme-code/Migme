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

public abstract class _ConnectionDisp extends ObjectImpl implements Connection {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::Connection"};
   private static final String[] __all = new String[]{"accountBalanceChanged", "avatarChanged", "contactAdded", "contactChangedDisplayPictureOneWay", "contactChangedPresenceOneWay", "contactChangedStatusMessageOneWay", "contactGroupAdded", "contactGroupRemoved", "contactRemoved", "contactRequest", "contactRequestAccepted", "contactRequestRejected", "disconnect", "emailNotification", "emoticonsChanged", "getClientVersion", "getDeviceTypeAsInt", "getMobileDevice", "getPopularChatRooms", "getRemoteIPAddress", "getSessionObject", "getUserAgent", "getUserObject", "getUsername", "ice_id", "ice_ids", "ice_isA", "ice_ping", "logout", "otherIMConferenceCreated", "otherIMLoggedIn", "otherIMLoggedOut", "packetProcessed", "privateChatNowAGroupChat", "processPacket", "pushNotification", "putAlertMessage", "putAlertMessageOneWay", "putAnonymousCallNotification", "putEvent", "putFileReceived", "putGenericPacket", "putMessage", "putMessageAsync", "putMessageOneWay", "putMessageStatusEvent", "putMessageStatusEvents", "putMessages", "putSerializedPacket", "putSerializedPacketOneWay", "putServerQuestion", "putWebCallNotification", "silentlyDropIncomingPackets", "themeChanged"};

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
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
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

   public static DispatchStatus ___getUsername(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String __ret = __obj.getUsername(__current);
      __os.writeString(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getPopularChatRooms(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();

      try {
         ChatRoomDataIce[] __ret = __obj.getPopularChatRooms(__current);
         ChatRoomDataIceArrayHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var5) {
         __os.writeUserException(var5);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getRemoteIPAddress(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String __ret = __obj.getRemoteIPAddress(__current);
      __os.writeString(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getMobileDevice(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String __ret = __obj.getMobileDevice(__current);
      __os.writeString(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getUserAgent(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      String __ret = __obj.getUserAgent(__current);
      __os.writeString(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getDeviceTypeAsInt(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      int __ret = __obj.getDeviceTypeAsInt(__current);
      __os.writeInt(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getClientVersion(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      short __ret = __obj.getClientVersion(__current);
      __os.writeShort(__ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getUserObject(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      UserPrx __ret = __obj.getUserObject(__current);
      UserPrxHelper.__write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___getSessionObject(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      BasicStream __os = __inS.os();
      SessionPrx __ret = __obj.getSessionObject(__current);
      SessionPrxHelper.__write(__os, __ret);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___processPacket(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ConnectionPrx requestingConnection = ConnectionPrxHelper.__read(__is);
      byte[] packet = ByteArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         boolean __ret = __obj.processPacket(requestingConnection, packet, __current);
         __os.writeBool(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___packetProcessed(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      byte[] result = ByteArrayHelper.read(__is);
      __is.endReadEncaps();
      __obj.packetProcessed(result, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___disconnect(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String reason = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.disconnect(reason, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___accountBalanceChanged(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      double balance = __is.readDouble();
      double fundedBalance = __is.readDouble();
      CurrencyDataIce currency = new CurrencyDataIce();
      currency.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.accountBalanceChanged(balance, fundedBalance, currency, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactChangedPresenceOneWay(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactID = __is.readInt();
      int imType = __is.readInt();
      int presence = __is.readInt();
      __is.endReadEncaps();
      __obj.contactChangedPresenceOneWay(contactID, imType, presence, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactChangedDisplayPictureOneWay(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactID = __is.readInt();
      String displayPicture = __is.readString();
      long timeStamp = __is.readLong();
      __is.endReadEncaps();
      __obj.contactChangedDisplayPictureOneWay(contactID, displayPicture, timeStamp, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactChangedStatusMessageOneWay(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactID = __is.readInt();
      String statusMessage = __is.readString();
      long timeStamp = __is.readLong();
      __is.endReadEncaps();
      __obj.contactChangedStatusMessageOneWay(contactID, statusMessage, timeStamp, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___contactRequest(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String contactUsername = __is.readString();
      int outstandingRequests = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.contactRequest(contactUsername, outstandingRequests, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactRequestAccepted(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      int contactListVersion = __is.readInt();
      int outstandingRequests = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.contactRequestAccepted(contact, contactListVersion, outstandingRequests, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactRequestRejected(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String contactUsername = __is.readString();
      int outstandingRequests = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.contactRequestRejected(contactUsername, outstandingRequests, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactGroupAdded(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactGroupDataIce contactGroup = new ContactGroupDataIce();
      contactGroup.__read(__is);
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.contactGroupAdded(contactGroup, contactListVersion, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactGroupRemoved(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactGroupID = __is.readInt();
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.contactGroupRemoved(contactGroupID, contactListVersion, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactAdded(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      ContactDataIce contact = new ContactDataIce();
      contact.__read(__is);
      int contactListVersion = __is.readInt();
      boolean guaranteedIsNew = __is.readBool();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.contactAdded(contact, contactListVersion, guaranteedIsNew, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___contactRemoved(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int contactID = __is.readInt();
      int contactListVersion = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.contactRemoved(contactID, contactListVersion, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___otherIMLoggedIn(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.otherIMLoggedIn(imType, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___otherIMLoggedOut(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String reason = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.otherIMLoggedOut(imType, reason, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___otherIMConferenceCreated(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int imType = __is.readInt();
      String conferenceID = __is.readString();
      String creator = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.otherIMConferenceCreated(imType, conferenceID, creator, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___privateChatNowAGroupChat(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String groupChatID = __is.readString();
      String creator = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.privateChatNowAGroupChat(groupChatID, creator, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putEvent(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      UserEventIceHolder event = new UserEventIceHolder();
      __is.readObject(event.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putEvent(event.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putMessage(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putMessage(message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putMessageAsync(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      _AMD_Connection_putMessageAsync __cb = new _AMD_Connection_putMessageAsync(__inS);

      try {
         __obj.putMessageAsync_async(__cb, message, __current);
      } catch (Exception var7) {
         __cb.ice_exception(var7);
      }

      return DispatchStatus.DispatchAsync;
   }

   public static DispatchStatus ___putMessageOneWay(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      __obj.putMessageOneWay(message, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___putMessages(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce[] messages = MessageDataSequenceHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putMessages(messages, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putAlertMessage(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String message = __is.readString();
      String title = __is.readString();
      short timeout = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putAlertMessage(message, title, timeout, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var9) {
         __os.writeUserException(var9);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putAlertMessageOneWay(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String message = __is.readString();
      String title = __is.readString();
      short timeout = __is.readShort();
      __is.endReadEncaps();
      __obj.putAlertMessageOneWay(message, title, timeout, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___putServerQuestion(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String message = __is.readString();
      String url = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putServerQuestion(message, url, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putWebCallNotification(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String source = __is.readString();
      String destination = __is.readString();
      int gateway = __is.readInt();
      String gatewayName = __is.readString();
      int protocol = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putWebCallNotification(source, destination, gateway, gatewayName, protocol, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var11) {
         __os.writeUserException(var11);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putAnonymousCallNotification(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String requestingUsername = __is.readString();
      String requestingMobilePhone = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putAnonymousCallNotification(requestingUsername, requestingMobilePhone, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putFileReceived(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageDataIce message = new MessageDataIce();
      message.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putFileReceived(message, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putGenericPacket(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      byte[] packet = ByteArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putGenericPacket(packet, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___emailNotification(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int unreadEmailCount = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.emailNotification(unreadEmailCount, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___emoticonsChanged(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String[] hotKeys = StringArrayHelper.read(__is);
      String[] alternateKeys = StringArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.emoticonsChanged(hotKeys, alternateKeys, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___themeChanged(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String themeLocation = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.themeChanged(themeLocation, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___avatarChanged(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String displayPicture = __is.readString();
      String statusMessage = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.avatarChanged(displayPicture, statusMessage, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___silentlyDropIncomingPackets(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      __obj.silentlyDropIncomingPackets(__current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___pushNotification(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageHolder msg = new MessageHolder();
      __is.readObject(msg.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.pushNotification(msg.value, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___logout(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      __inS.is().skipEmptyEncaps();
      __obj.logout(__current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___putSerializedPacket(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      byte[] packet = ByteArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putSerializedPacket(packet, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putSerializedPacketOneWay(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      byte[] packet = ByteArrayHelper.read(__is);
      __is.endReadEncaps();
      __obj.putSerializedPacketOneWay(packet, __current);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___putMessageStatusEvent(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageStatusEventIce mseIce = new MessageStatusEventIce();
      mseIce.__read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putMessageStatusEvent(mseIce, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___putMessageStatusEvents(Connection __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      MessageStatusEventIce[] events = MessageStatusEventSequenceHelper.read(__is);
      short requestTxnId = __is.readShort();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.putMessageStatusEvents(events, requestTxnId, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___accountBalanceChanged(this, in, __current);
         case 1:
            return ___avatarChanged(this, in, __current);
         case 2:
            return ___contactAdded(this, in, __current);
         case 3:
            return ___contactChangedDisplayPictureOneWay(this, in, __current);
         case 4:
            return ___contactChangedPresenceOneWay(this, in, __current);
         case 5:
            return ___contactChangedStatusMessageOneWay(this, in, __current);
         case 6:
            return ___contactGroupAdded(this, in, __current);
         case 7:
            return ___contactGroupRemoved(this, in, __current);
         case 8:
            return ___contactRemoved(this, in, __current);
         case 9:
            return ___contactRequest(this, in, __current);
         case 10:
            return ___contactRequestAccepted(this, in, __current);
         case 11:
            return ___contactRequestRejected(this, in, __current);
         case 12:
            return ___disconnect(this, in, __current);
         case 13:
            return ___emailNotification(this, in, __current);
         case 14:
            return ___emoticonsChanged(this, in, __current);
         case 15:
            return ___getClientVersion(this, in, __current);
         case 16:
            return ___getDeviceTypeAsInt(this, in, __current);
         case 17:
            return ___getMobileDevice(this, in, __current);
         case 18:
            return ___getPopularChatRooms(this, in, __current);
         case 19:
            return ___getRemoteIPAddress(this, in, __current);
         case 20:
            return ___getSessionObject(this, in, __current);
         case 21:
            return ___getUserAgent(this, in, __current);
         case 22:
            return ___getUserObject(this, in, __current);
         case 23:
            return ___getUsername(this, in, __current);
         case 24:
            return ___ice_id(this, in, __current);
         case 25:
            return ___ice_ids(this, in, __current);
         case 26:
            return ___ice_isA(this, in, __current);
         case 27:
            return ___ice_ping(this, in, __current);
         case 28:
            return ___logout(this, in, __current);
         case 29:
            return ___otherIMConferenceCreated(this, in, __current);
         case 30:
            return ___otherIMLoggedIn(this, in, __current);
         case 31:
            return ___otherIMLoggedOut(this, in, __current);
         case 32:
            return ___packetProcessed(this, in, __current);
         case 33:
            return ___privateChatNowAGroupChat(this, in, __current);
         case 34:
            return ___processPacket(this, in, __current);
         case 35:
            return ___pushNotification(this, in, __current);
         case 36:
            return ___putAlertMessage(this, in, __current);
         case 37:
            return ___putAlertMessageOneWay(this, in, __current);
         case 38:
            return ___putAnonymousCallNotification(this, in, __current);
         case 39:
            return ___putEvent(this, in, __current);
         case 40:
            return ___putFileReceived(this, in, __current);
         case 41:
            return ___putGenericPacket(this, in, __current);
         case 42:
            return ___putMessage(this, in, __current);
         case 43:
            return ___putMessageAsync(this, in, __current);
         case 44:
            return ___putMessageOneWay(this, in, __current);
         case 45:
            return ___putMessageStatusEvent(this, in, __current);
         case 46:
            return ___putMessageStatusEvents(this, in, __current);
         case 47:
            return ___putMessages(this, in, __current);
         case 48:
            return ___putSerializedPacket(this, in, __current);
         case 49:
            return ___putSerializedPacketOneWay(this, in, __current);
         case 50:
            return ___putServerQuestion(this, in, __current);
         case 51:
            return ___putWebCallNotification(this, in, __current);
         case 52:
            return ___silentlyDropIncomingPackets(this, in, __current);
         case 53:
            return ___themeChanged(this, in, __current);
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
      ex.reason = "type com::projectgoth::fusion::slice::Connection was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::Connection was not generated with stream support";
      throw ex;
   }
}
