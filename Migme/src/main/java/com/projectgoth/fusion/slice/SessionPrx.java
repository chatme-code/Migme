package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface SessionPrx extends ObjectPrx {
   void sendMessage(MessageDataIce var1) throws FusionException;

   void sendMessage(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void setPresence(int var1) throws FusionException;

   void setPresence(int var1, Map<String, String> var2) throws FusionException;

   void endSession() throws FusionException;

   void endSession(Map<String, String> var1) throws FusionException;

   void endSessionOneWay();

   void endSessionOneWay(Map<String, String> var1);

   void touch() throws FusionException;

   void touch(Map<String, String> var1) throws FusionException;

   void putMessage(MessageDataIce var1) throws FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void putMessageOneWay(MessageDataIce var1);

   void putMessageOneWay(MessageDataIce var1, Map<String, String> var2);

   void sendMessageBackToUserAsEmote(MessageDataIce var1) throws FusionException;

   void sendMessageBackToUserAsEmote(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3, Map<String, String> var4) throws FusionException;

   void putAlertMessageOneWay(String var1, String var2, short var3);

   void putAlertMessageOneWay(String var1, String var2, short var3, Map<String, String> var4);

   String getParentUsername() throws FusionException;

   String getParentUsername(Map<String, String> var1) throws FusionException;

   UserPrx getUserProxy(String var1) throws FusionException;

   UserPrx getUserProxy(String var1, Map<String, String> var2) throws FusionException;

   void profileEdited();

   void profileEdited(Map<String, String> var1);

   void groupChatJoined(String var1);

   void groupChatJoined(String var1, Map<String, String> var2);

   void groupChatJoinedMultiple(String var1, int var2);

   void groupChatJoinedMultiple(String var1, int var2, Map<String, String> var3);

   void chatroomJoined(ChatRoomPrx var1, String var2);

   void chatroomJoined(ChatRoomPrx var1, String var2, Map<String, String> var3);

   void statusMessageSet();

   void statusMessageSet(Map<String, String> var1);

   void photoUploaded();

   void photoUploaded(Map<String, String> var1);

   void friendInvitedByPhoneNumber();

   void friendInvitedByPhoneNumber(Map<String, String> var1);

   void friendInvitedByUsername();

   void friendInvitedByUsername(Map<String, String> var1);

   void themeUpdated();

   void themeUpdated(Map<String, String> var1);

   void silentlyDropIncomingPackets();

   void silentlyDropIncomingPackets(Map<String, String> var1);

   String getSessionID();

   String getSessionID(Map<String, String> var1);

   String getRemoteIPAddress();

   String getRemoteIPAddress(Map<String, String> var1);

   String getMobileDeviceIce();

   String getMobileDeviceIce(Map<String, String> var1);

   String getUserAgentIce();

   String getUserAgentIce(Map<String, String> var1);

   short getClientVersionIce();

   short getClientVersionIce(Map<String, String> var1);

   int getDeviceTypeAsInt();

   int getDeviceTypeAsInt(Map<String, String> var1);

   void setLanguage(String var1);

   void setLanguage(String var1, Map<String, String> var2);

   void notifyUserLeftChatRoomOneWay(String var1, String var2);

   void notifyUserLeftChatRoomOneWay(String var1, String var2, Map<String, String> var3);

   void notifyUserJoinedChatRoomOneWay(String var1, String var2, boolean var3, boolean var4);

   void notifyUserJoinedChatRoomOneWay(String var1, String var2, boolean var3, boolean var4, Map<String, String> var5);

   void notifyUserLeftGroupChat(String var1, String var2) throws FusionException;

   void notifyUserLeftGroupChat(String var1, String var2, Map<String, String> var3) throws FusionException;

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3) throws FusionException;

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3, Map<String, String> var4) throws FusionException;

   void sendGroupChatParticipants(String var1, byte var2, String var3, String var4) throws FusionException;

   void sendGroupChatParticipants(String var1, byte var2, String var3, String var4, Map<String, String> var5) throws FusionException;

   void sendGroupChatParticipantArrays(String var1, byte var2, String[] var3, String[] var4) throws FusionException;

   void sendGroupChatParticipantArrays(String var1, byte var2, String[] var3, String[] var4, Map<String, String> var5) throws FusionException;

   int getChatListVersion() throws FusionException;

   int getChatListVersion(Map<String, String> var1) throws FusionException;

   void setChatListVersion(int var1) throws FusionException;

   void setChatListVersion(int var1, Map<String, String> var2) throws FusionException;

   void putSerializedPacket(byte[] var1) throws FusionException;

   void putSerializedPacket(byte[] var1, Map<String, String> var2) throws FusionException;

   void putSerializedPacketOneWay(byte[] var1);

   void putSerializedPacketOneWay(byte[] var1, Map<String, String> var2);

   GroupChatPrx findGroupChatObject(String var1) throws FusionException;

   GroupChatPrx findGroupChatObject(String var1, Map<String, String> var2) throws FusionException;

   MessageSwitchboardPrx getMessageSwitchboard() throws FusionException;

   MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> var1) throws FusionException;

   boolean privateChattedWith(String var1);

   boolean privateChattedWith(String var1, Map<String, String> var2);

   SessionMetricsIce getSessionMetrics();

   SessionMetricsIce getSessionMetrics(Map<String, String> var1);

   void setCurrentChatListGroupChatSubset(ChatListIce var1);

   void setCurrentChatListGroupChatSubset(ChatListIce var1, Map<String, String> var2);
}
