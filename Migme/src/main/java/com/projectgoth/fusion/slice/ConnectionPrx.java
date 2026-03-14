package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface ConnectionPrx extends ObjectPrx {
   String getUsername();

   String getUsername(Map<String, String> var1);

   ChatRoomDataIce[] getPopularChatRooms() throws FusionException;

   ChatRoomDataIce[] getPopularChatRooms(Map<String, String> var1) throws FusionException;

   String getRemoteIPAddress();

   String getRemoteIPAddress(Map<String, String> var1);

   String getMobileDevice();

   String getMobileDevice(Map<String, String> var1);

   String getUserAgent();

   String getUserAgent(Map<String, String> var1);

   int getDeviceTypeAsInt();

   int getDeviceTypeAsInt(Map<String, String> var1);

   short getClientVersion();

   short getClientVersion(Map<String, String> var1);

   UserPrx getUserObject();

   UserPrx getUserObject(Map<String, String> var1);

   SessionPrx getSessionObject();

   SessionPrx getSessionObject(Map<String, String> var1);

   boolean processPacket(ConnectionPrx var1, byte[] var2) throws FusionException;

   boolean processPacket(ConnectionPrx var1, byte[] var2, Map<String, String> var3) throws FusionException;

   void packetProcessed(byte[] var1);

   void packetProcessed(byte[] var1, Map<String, String> var2);

   void disconnect(String var1) throws FusionException;

   void disconnect(String var1, Map<String, String> var2) throws FusionException;

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5) throws FusionException;

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5, Map<String, String> var6) throws FusionException;

   void contactChangedPresenceOneWay(int var1, int var2, int var3);

   void contactChangedPresenceOneWay(int var1, int var2, int var3, Map<String, String> var4);

   void contactChangedDisplayPictureOneWay(int var1, String var2, long var3);

   void contactChangedDisplayPictureOneWay(int var1, String var2, long var3, Map<String, String> var5);

   void contactChangedStatusMessageOneWay(int var1, String var2, long var3);

   void contactChangedStatusMessageOneWay(int var1, String var2, long var3, Map<String, String> var5);

   void contactRequest(String var1, int var2) throws FusionException;

   void contactRequest(String var1, int var2, Map<String, String> var3) throws FusionException;

   void contactRequestAccepted(ContactDataIce var1, int var2, int var3) throws FusionException;

   void contactRequestAccepted(ContactDataIce var1, int var2, int var3, Map<String, String> var4) throws FusionException;

   void contactRequestRejected(String var1, int var2) throws FusionException;

   void contactRequestRejected(String var1, int var2, Map<String, String> var3) throws FusionException;

   void contactGroupAdded(ContactGroupDataIce var1, int var2) throws FusionException;

   void contactGroupAdded(ContactGroupDataIce var1, int var2, Map<String, String> var3) throws FusionException;

   void contactGroupRemoved(int var1, int var2) throws FusionException;

   void contactGroupRemoved(int var1, int var2, Map<String, String> var3) throws FusionException;

   void contactAdded(ContactDataIce var1, int var2, boolean var3) throws FusionException;

   void contactAdded(ContactDataIce var1, int var2, boolean var3, Map<String, String> var4) throws FusionException;

   void contactRemoved(int var1, int var2) throws FusionException;

   void contactRemoved(int var1, int var2, Map<String, String> var3) throws FusionException;

   void otherIMLoggedIn(int var1) throws FusionException;

   void otherIMLoggedIn(int var1, Map<String, String> var2) throws FusionException;

   void otherIMLoggedOut(int var1, String var2) throws FusionException;

   void otherIMLoggedOut(int var1, String var2, Map<String, String> var3) throws FusionException;

   void otherIMConferenceCreated(int var1, String var2, String var3) throws FusionException;

   void otherIMConferenceCreated(int var1, String var2, String var3, Map<String, String> var4) throws FusionException;

   void privateChatNowAGroupChat(String var1, String var2) throws FusionException;

   void privateChatNowAGroupChat(String var1, String var2, Map<String, String> var3) throws FusionException;

   void putEvent(UserEventIce var1) throws FusionException;

   void putEvent(UserEventIce var1, Map<String, String> var2) throws FusionException;

   void putMessage(MessageDataIce var1) throws FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void putMessageAsync(MessageDataIce var1) throws FusionException;

   void putMessageAsync(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   boolean putMessageAsync_async(AMI_Connection_putMessageAsync var1, MessageDataIce var2);

   boolean putMessageAsync_async(AMI_Connection_putMessageAsync var1, MessageDataIce var2, Map<String, String> var3);

   void putMessageOneWay(MessageDataIce var1);

   void putMessageOneWay(MessageDataIce var1, Map<String, String> var2);

   void putMessages(MessageDataIce[] var1) throws FusionException;

   void putMessages(MessageDataIce[] var1, Map<String, String> var2) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3, Map<String, String> var4) throws FusionException;

   void putAlertMessageOneWay(String var1, String var2, short var3);

   void putAlertMessageOneWay(String var1, String var2, short var3, Map<String, String> var4);

   void putServerQuestion(String var1, String var2) throws FusionException;

   void putServerQuestion(String var1, String var2, Map<String, String> var3) throws FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5) throws FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5, Map<String, String> var6) throws FusionException;

   void putAnonymousCallNotification(String var1, String var2) throws FusionException;

   void putAnonymousCallNotification(String var1, String var2, Map<String, String> var3) throws FusionException;

   void putFileReceived(MessageDataIce var1) throws FusionException;

   void putFileReceived(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void putGenericPacket(byte[] var1) throws FusionException;

   void putGenericPacket(byte[] var1, Map<String, String> var2) throws FusionException;

   void emailNotification(int var1) throws FusionException;

   void emailNotification(int var1, Map<String, String> var2) throws FusionException;

   void emoticonsChanged(String[] var1, String[] var2) throws FusionException;

   void emoticonsChanged(String[] var1, String[] var2, Map<String, String> var3) throws FusionException;

   void themeChanged(String var1) throws FusionException;

   void themeChanged(String var1, Map<String, String> var2) throws FusionException;

   void avatarChanged(String var1, String var2) throws FusionException;

   void avatarChanged(String var1, String var2, Map<String, String> var3) throws FusionException;

   void silentlyDropIncomingPackets();

   void silentlyDropIncomingPackets(Map<String, String> var1);

   void pushNotification(Message var1) throws FusionException;

   void pushNotification(Message var1, Map<String, String> var2) throws FusionException;

   void logout();

   void logout(Map<String, String> var1);

   void putSerializedPacket(byte[] var1) throws FusionException;

   void putSerializedPacket(byte[] var1, Map<String, String> var2) throws FusionException;

   void putSerializedPacketOneWay(byte[] var1);

   void putSerializedPacketOneWay(byte[] var1, Map<String, String> var2);

   void putMessageStatusEvent(MessageStatusEventIce var1) throws FusionException;

   void putMessageStatusEvent(MessageStatusEventIce var1, Map<String, String> var2) throws FusionException;

   void putMessageStatusEvents(MessageStatusEventIce[] var1, short var2) throws FusionException;

   void putMessageStatusEvents(MessageStatusEventIce[] var1, short var2, Map<String, String> var3) throws FusionException;
}
