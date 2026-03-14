package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _SessionDel extends _ObjectDel {
   void sendMessage(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void setPresence(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void endSession(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   void endSessionOneWay(Map<String, String> var1) throws LocalExceptionWrapper;

   void touch(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putMessageOneWay(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void sendMessageBackToUserAsEmote(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putAlertMessage(String var1, String var2, short var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void putAlertMessageOneWay(String var1, String var2, short var3, Map<String, String> var4) throws LocalExceptionWrapper;

   String getParentUsername(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   UserPrx getUserProxy(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void profileEdited(Map<String, String> var1) throws LocalExceptionWrapper;

   void groupChatJoined(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void groupChatJoinedMultiple(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void chatroomJoined(ChatRoomPrx var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void statusMessageSet(Map<String, String> var1) throws LocalExceptionWrapper;

   void photoUploaded(Map<String, String> var1) throws LocalExceptionWrapper;

   void friendInvitedByPhoneNumber(Map<String, String> var1) throws LocalExceptionWrapper;

   void friendInvitedByUsername(Map<String, String> var1) throws LocalExceptionWrapper;

   void themeUpdated(Map<String, String> var1) throws LocalExceptionWrapper;

   void silentlyDropIncomingPackets(Map<String, String> var1) throws LocalExceptionWrapper;

   String getSessionID(Map<String, String> var1) throws LocalExceptionWrapper;

   String getRemoteIPAddress(Map<String, String> var1) throws LocalExceptionWrapper;

   String getMobileDeviceIce(Map<String, String> var1) throws LocalExceptionWrapper;

   String getUserAgentIce(Map<String, String> var1) throws LocalExceptionWrapper;

   short getClientVersionIce(Map<String, String> var1) throws LocalExceptionWrapper;

   int getDeviceTypeAsInt(Map<String, String> var1) throws LocalExceptionWrapper;

   void setLanguage(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void notifyUserLeftChatRoomOneWay(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void notifyUserJoinedChatRoomOneWay(String var1, String var2, boolean var3, boolean var4, Map<String, String> var5) throws LocalExceptionWrapper;

   void notifyUserLeftGroupChat(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void sendGroupChatParticipants(String var1, byte var2, String var3, String var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void sendGroupChatParticipantArrays(String var1, byte var2, String[] var3, String[] var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   int getChatListVersion(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   void setChatListVersion(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putSerializedPacket(byte[] var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putSerializedPacketOneWay(byte[] var1, Map<String, String> var2) throws LocalExceptionWrapper;

   GroupChatPrx findGroupChatObject(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   boolean privateChattedWith(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   SessionMetricsIce getSessionMetrics(Map<String, String> var1) throws LocalExceptionWrapper;

   void setCurrentChatListGroupChatSubset(ChatListIce var1, Map<String, String> var2) throws LocalExceptionWrapper;
}
