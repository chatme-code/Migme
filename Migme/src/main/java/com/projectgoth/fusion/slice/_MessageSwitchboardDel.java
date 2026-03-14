package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _MessageSwitchboardDel extends _ObjectDel {
   boolean isUserChatSyncEnabled(ConnectionPrx var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   ChatDefinitionIce[] getChats(int var1, int var2, int var3, byte var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   ChatDefinitionIce[] getChats2(int var1, int var2, int var3, byte var4, ConnectionPrx var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void onGetChats(ConnectionPrx var1, int var2, int var3, int var4, byte var5, short var6, String var7, Map<String, String> var8) throws LocalExceptionWrapper, FusionException;

   void getAndPushMessages(String var1, byte var2, String var3, long var4, long var6, int var8, ConnectionPrx var9, Map<String, String> var10) throws LocalExceptionWrapper, FusionException;

   void getAndPushMessages2(String var1, byte var2, String var3, long var4, long var6, int var8, ConnectionPrx var9, int var10, short var11, short var12, Map<String, String> var13) throws LocalExceptionWrapper, FusionException;

   void onCreateGroupChat(ChatDefinitionIce var1, String var2, String var3, GroupChatPrx var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void onJoinGroupChat(String var1, int var2, String var3, boolean var4, UserPrx var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void onLeaveGroupChat(String var1, int var2, String var3, UserPrx var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void onJoinChatRoom(String var1, int var2, String var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void onLeaveChatRoom(String var1, int var2, String var3, UserPrx var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   boolean onSendFusionMessageToIndividual(SessionPrx var1, UserPrx var2, MessageDataIce var3, String var4, String[] var5, int var6, short var7, UserDataIce var8, String var9, Map<String, String> var10) throws LocalExceptionWrapper, FusionException;

   void onSendFusionMessageToGroupChat(SessionPrx var1, UserPrx var2, MessageDataIce var3, String var4, int var5, short var6, Map<String, String> var7) throws LocalExceptionWrapper, FusionException;

   void onSendFusionMessageToChatRoom(SessionPrx var1, UserPrx var2, MessageDataIce var3, String var4, int var5, short var6, Map<String, String> var7) throws LocalExceptionWrapper, FusionException;

   boolean onSendMessageToAllUsersInChat(SessionPrx var1, UserPrx var2, MessageDataIce var3, UserDataIce var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void onCreatePrivateChat(int var1, String var2, String var3, int var4, short var5, UserDataIce var6, String var7, Map<String, String> var8) throws LocalExceptionWrapper, FusionException;

   void onLeavePrivateChat(int var1, String var2, String var3, int var4, short var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   GroupChatPrx ensureGroupChatExists(SessionPrx var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void onLogon(int var1, SessionPrx var2, short var3, String var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void setChatName(String var1, String var2, byte var3, String var4, RegistryPrx var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;
}
