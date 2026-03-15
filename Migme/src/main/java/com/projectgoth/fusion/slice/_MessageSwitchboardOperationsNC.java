package com.projectgoth.fusion.slice;

public interface _MessageSwitchboardOperationsNC {
   boolean isUserChatSyncEnabled(ConnectionPrx var1, String var2, int var3) throws FusionException;

   ChatDefinitionIce[] getChats(int var1, int var2, int var3, byte var4) throws FusionException;

   ChatDefinitionIce[] getChats2(int var1, int var2, int var3, byte var4, ConnectionPrx var5) throws FusionException;

   void onGetChats(ConnectionPrx var1, int var2, int var3, int var4, byte var5, short var6, String var7) throws FusionException;

   void getAndPushMessages(String var1, byte var2, String var3, long var4, long var6, int var8, ConnectionPrx var9) throws FusionException;

   void getAndPushMessages2(String var1, byte var2, String var3, long var4, long var6, int var8, ConnectionPrx var9, int var10, short var11, short var12) throws FusionException;

   void onCreateGroupChat(ChatDefinitionIce var1, String var2, String var3, GroupChatPrx var4) throws FusionException;

   void onJoinGroupChat(String var1, int var2, String var3, boolean var4, UserPrx var5) throws FusionException;

   void onLeaveGroupChat(String var1, int var2, String var3, UserPrx var4) throws FusionException;

   void onJoinChatRoom(String var1, int var2, String var3) throws FusionException;

   void onLeaveChatRoom(String var1, int var2, String var3, UserPrx var4) throws FusionException;

   boolean onSendFusionMessageToIndividual(SessionPrx var1, UserPrx var2, MessageDataIce var3, String var4, String[] var5, int var6, short var7, UserDataIce var8, String var9) throws FusionException;

   void onSendFusionMessageToGroupChat(SessionPrx var1, UserPrx var2, MessageDataIce var3, String var4, int var5, short var6) throws FusionException;

   void onSendFusionMessageToChatRoom(SessionPrx var1, UserPrx var2, MessageDataIce var3, String var4, int var5, short var6) throws FusionException;

   boolean onSendMessageToAllUsersInChat(SessionPrx var1, UserPrx var2, MessageDataIce var3, UserDataIce var4) throws FusionException;

   void onCreatePrivateChat(int var1, String var2, String var3, int var4, short var5, UserDataIce var6, String var7) throws FusionException;

   void onLeavePrivateChat(int var1, String var2, String var3, int var4, short var5) throws FusionException;

   GroupChatPrx ensureGroupChatExists(SessionPrx var1, String var2) throws FusionException;

   void onLogon(int var1, SessionPrx var2, short var3, String var4) throws FusionException;

   void setChatName(String var1, String var2, byte var3, String var4, RegistryPrx var5) throws FusionException;
}
