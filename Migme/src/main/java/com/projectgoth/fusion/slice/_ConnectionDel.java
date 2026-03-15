package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _ConnectionDel extends _ObjectDel {
   String getUsername(Map<String, String> var1) throws LocalExceptionWrapper;

   ChatRoomDataIce[] getPopularChatRooms(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   String getRemoteIPAddress(Map<String, String> var1) throws LocalExceptionWrapper;

   String getMobileDevice(Map<String, String> var1) throws LocalExceptionWrapper;

   String getUserAgent(Map<String, String> var1) throws LocalExceptionWrapper;

   int getDeviceTypeAsInt(Map<String, String> var1) throws LocalExceptionWrapper;

   short getClientVersion(Map<String, String> var1) throws LocalExceptionWrapper;

   UserPrx getUserObject(Map<String, String> var1) throws LocalExceptionWrapper;

   SessionPrx getSessionObject(Map<String, String> var1) throws LocalExceptionWrapper;

   boolean processPacket(ConnectionPrx var1, byte[] var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void packetProcessed(byte[] var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void disconnect(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void accountBalanceChanged(double var1, double var3, CurrencyDataIce var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void contactChangedPresenceOneWay(int var1, int var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper;

   void contactChangedDisplayPictureOneWay(int var1, String var2, long var3, Map<String, String> var5) throws LocalExceptionWrapper;

   void contactChangedStatusMessageOneWay(int var1, String var2, long var3, Map<String, String> var5) throws LocalExceptionWrapper;

   void contactRequest(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void contactRequestAccepted(ContactDataIce var1, int var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void contactRequestRejected(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void contactGroupAdded(ContactGroupDataIce var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void contactGroupRemoved(int var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void contactAdded(ContactDataIce var1, int var2, boolean var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void contactRemoved(int var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void otherIMLoggedIn(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void otherIMLoggedOut(int var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void otherIMConferenceCreated(int var1, String var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void privateChatNowAGroupChat(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putEvent(UserEventIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putMessageAsync(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putMessageOneWay(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void putMessages(MessageDataIce[] var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putAlertMessage(String var1, String var2, short var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void putAlertMessageOneWay(String var1, String var2, short var3, Map<String, String> var4) throws LocalExceptionWrapper;

   void putServerQuestion(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putWebCallNotification(String var1, String var2, int var3, String var4, int var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void putAnonymousCallNotification(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void putFileReceived(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putGenericPacket(byte[] var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void emailNotification(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void emoticonsChanged(String[] var1, String[] var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void themeChanged(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void avatarChanged(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void silentlyDropIncomingPackets(Map<String, String> var1) throws LocalExceptionWrapper;

   void pushNotification(Message var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void logout(Map<String, String> var1) throws LocalExceptionWrapper;

   void putSerializedPacket(byte[] var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putSerializedPacketOneWay(byte[] var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void putMessageStatusEvent(MessageStatusEventIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putMessageStatusEvents(MessageStatusEventIce[] var1, short var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;
}
