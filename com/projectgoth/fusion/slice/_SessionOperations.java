package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _SessionOperations {
   void sendMessage_async(AMD_Session_sendMessage var1, MessageDataIce var2, Current var3) throws FusionException;

   void setPresence(int var1, Current var2) throws FusionException;

   void endSession_async(AMD_Session_endSession var1, Current var2) throws FusionException;

   void endSessionOneWay(Current var1);

   void touch(Current var1) throws FusionException;

   void putMessage_async(AMD_Session_putMessage var1, MessageDataIce var2, Current var3) throws FusionException;

   void putMessageOneWay(MessageDataIce var1, Current var2);

   void sendMessageBackToUserAsEmote(MessageDataIce var1, Current var2) throws FusionException;

   void putAlertMessage(String var1, String var2, short var3, Current var4) throws FusionException;

   void putAlertMessageOneWay(String var1, String var2, short var3, Current var4);

   String getParentUsername(Current var1) throws FusionException;

   UserPrx getUserProxy(String var1, Current var2) throws FusionException;

   void profileEdited(Current var1);

   void groupChatJoined(String var1, Current var2);

   void groupChatJoinedMultiple(String var1, int var2, Current var3);

   void chatroomJoined(ChatRoomPrx var1, String var2, Current var3);

   void statusMessageSet(Current var1);

   void photoUploaded(Current var1);

   void friendInvitedByPhoneNumber(Current var1);

   void friendInvitedByUsername(Current var1);

   void themeUpdated(Current var1);

   void silentlyDropIncomingPackets(Current var1);

   String getSessionID(Current var1);

   String getRemoteIPAddress(Current var1);

   String getMobileDeviceIce(Current var1);

   String getUserAgentIce(Current var1);

   short getClientVersionIce(Current var1);

   int getDeviceTypeAsInt(Current var1);

   void setLanguage(String var1, Current var2);

   void notifyUserLeftChatRoomOneWay(String var1, String var2, Current var3);

   void notifyUserJoinedChatRoomOneWay(String var1, String var2, boolean var3, boolean var4, Current var5);

   void notifyUserLeftGroupChat(String var1, String var2, Current var3) throws FusionException;

   void notifyUserJoinedGroupChat(String var1, String var2, boolean var3, Current var4) throws FusionException;

   void sendGroupChatParticipants(String var1, byte var2, String var3, String var4, Current var5) throws FusionException;

   void sendGroupChatParticipantArrays(String var1, byte var2, String[] var3, String[] var4, Current var5) throws FusionException;

   int getChatListVersion(Current var1) throws FusionException;

   void setChatListVersion(int var1, Current var2) throws FusionException;

   void putSerializedPacket(byte[] var1, Current var2) throws FusionException;

   void putSerializedPacketOneWay(byte[] var1, Current var2);

   GroupChatPrx findGroupChatObject(String var1, Current var2) throws FusionException;

   MessageSwitchboardPrx getMessageSwitchboard(Current var1) throws FusionException;

   boolean privateChattedWith(String var1, Current var2);

   SessionMetricsIce getSessionMetrics(Current var1);

   void setCurrentChatListGroupChatSubset(ChatListIce var1, Current var2);
}
