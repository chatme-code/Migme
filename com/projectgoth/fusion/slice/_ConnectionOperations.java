package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _ConnectionOperations {
   String getUsername(Current var1);

   ChatRoomDataIce[] getPopularChatRooms(Current var1) throws FusionException;

   String getRemoteIPAddress(Current var1);

   String getMobileDevice(Current var1);

   String getUserAgent(Current var1);

   int getDeviceTypeAsInt(Current var1);

   short getClientVersion(Current var1);

   UserPrx getUserObject(Current var1);

   SessionPrx getSessionObject(Current var1);

   boolean processPacket(ConnectionPrx var1, byte[] var2, Current var3) throws FusionException;

   void packetProcessed(byte[] var1, Current var2);

   void disconnect(String var1, Current var2) throws FusionException;

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5, Current var6) throws FusionException;

   void contactChangedPresenceOneWay(int var1, int var2, int var3, Current var4);

   void contactChangedDisplayPictureOneWay(int var1, String var2, long var3, Current var5);

   void contactChangedStatusMessageOneWay(int var1, String var2, long var3, Current var5);

   void contactRequest(String var1, int var2, Current var3) throws FusionException;

   void contactRequestAccepted(ContactDataIce var1, int var2, int var3, Current var4) throws FusionException;

   void contactRequestRejected(String var1, int var2, Current var3) throws FusionException;

   void contactGroupAdded(ContactGroupDataIce var1, int var2, Current var3) throws FusionException;

   void contactGroupRemoved(int var1, int var2, Current var3) throws FusionException;

   void contactAdded(ContactDataIce var1, int var2, boolean var3, Current var4) throws FusionException;

   void contactRemoved(int var1, int var2, Current var3) throws FusionException;

   void otherIMLoggedIn(int var1, Current var2) throws FusionException;

   void otherIMLoggedOut(int var1, String var2, Current var3) throws FusionException;

   void otherIMConferenceCreated(int var1, String var2, String var3, Current var4) throws FusionException;

   void privateChatNowAGroupChat(String var1, String var2, Current var3) throws FusionException;

   void putEvent(UserEventIce var1, Current var2) throws FusionException;

   void putMessage(MessageDataIce var1, Current var2) throws FusionException;

   void putMessageAsync_async(AMD_Connection_putMessageAsync var1, MessageDataIce var2, Current var3) throws FusionException;

   void putMessageOneWay(MessageDataIce var1, Current var2);

   void putMessages(MessageDataIce[] var1, Current var2) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3, Current var4) throws FusionException;

   void putAlertMessageOneWay(String var1, String var2, short var3, Current var4);

   void putServerQuestion(String var1, String var2, Current var3) throws FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5, Current var6) throws FusionException;

   void putAnonymousCallNotification(String var1, String var2, Current var3) throws FusionException;

   void putFileReceived(MessageDataIce var1, Current var2) throws FusionException;

   void putGenericPacket(byte[] var1, Current var2) throws FusionException;

   void emailNotification(int var1, Current var2) throws FusionException;

   void emoticonsChanged(String[] var1, String[] var2, Current var3) throws FusionException;

   void themeChanged(String var1, Current var2) throws FusionException;

   void avatarChanged(String var1, String var2, Current var3) throws FusionException;

   void silentlyDropIncomingPackets(Current var1);

   void pushNotification(Message var1, Current var2) throws FusionException;

   void logout(Current var1);

   void putSerializedPacket(byte[] var1, Current var2) throws FusionException;

   void putSerializedPacketOneWay(byte[] var1, Current var2);

   void putMessageStatusEvent(MessageStatusEventIce var1, Current var2) throws FusionException;

   void putMessageStatusEvents(MessageStatusEventIce[] var1, short var2, Current var3) throws FusionException;
}
