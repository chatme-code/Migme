package com.projectgoth.fusion.slice;

public interface _SessionOperationsNC {
   void sendMessage_async(AMD_Session_sendMessage var1, MessageDataIce var2) throws FusionException;

   void setPresence(int var1) throws FusionException;

   void endSession_async(AMD_Session_endSession var1) throws FusionException;

   void endSessionOneWay();

   void touch() throws FusionException;

   void putMessage_async(AMD_Session_putMessage var1, MessageDataIce var2) throws FusionException;

   void putMessageOneWay(MessageDataIce var1);

   void sendMessageBackToUserAsEmote(MessageDataIce var1) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3) throws FusionException;

   void putAlertMessageOneWay(String var1, String var2, short var3);

   String getParentUsername() throws FusionException;

   UserPrx getUserProxy(String var1) throws FusionException;

   void profileEdited();

   void groupChatJoined(String var1);

   void groupChatJoinedMultiple(String var1, int var2);

   void chatroomJoined(ChatRoomPrx var1, String var2);

   void statusMessageSet();

   void photoUploaded();

   void friendInvitedByPhoneNumber();

   void friendInvitedByUsername();

   void themeUpdated();

   void silentlyDropIncomingPackets();

   String getSessionID();

   String getRemoteIPAddress();

   String getMobileDeviceIce();

   String getUserAgentIce();

   short getClientVersionIce();

   int getDeviceTypeAsInt();

   void setLanguage(String var1);

   void notifyUserLeftChatRoomOneWay(String var1, String var2);

   void notifyUserJoinedChatRoomOneWay(String var1, String var2, boolean var3, boolean var4);

   void notifyUserLeftGroupChat(String var1, String var2) throws FusionException;

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3) throws FusionException;

   void sendGroupChatParticipants(String var1, byte var2, String var3, String var4) throws FusionException;

   void sendGroupChatParticipantArrays(String var1, byte var2, String[] var3, String[] var4) throws FusionException;

   int getChatListVersion() throws FusionException;

   void setChatListVersion(int var1) throws FusionException;

   void putSerializedPacket(byte[] var1) throws FusionException;

   void putSerializedPacketOneWay(byte[] var1);

   GroupChatPrx findGroupChatObject(String var1) throws FusionException;

   MessageSwitchboardPrx getMessageSwitchboard() throws FusionException;

   boolean privateChattedWith(String var1);

   SessionMetricsIce getSessionMetrics();

   void setCurrentChatListGroupChatSubset(ChatListIce var1);
}
