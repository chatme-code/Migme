package com.projectgoth.fusion.slice;

import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _GroupChatDel extends _BotChannelDel {
   void addParticipantInner(String var1, String var2, boolean var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   void addParticipant(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   boolean removeParticipant(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void putFileReceived(MessageDataIce var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void sendInitialMessages(Map<String, String> var1) throws LocalExceptionWrapper;

   int getNumParticipants(Map<String, String> var1) throws LocalExceptionWrapper;

   boolean supportsBinaryMessage(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   String getId(Map<String, String> var1) throws LocalExceptionWrapper;

   String getCreatorUsername(Map<String, String> var1) throws LocalExceptionWrapper;

   int getCreatorUserID(Map<String, String> var1) throws LocalExceptionWrapper;

   int getPrivateChatPartnerUserID(Map<String, String> var1) throws LocalExceptionWrapper;

   String listOfParticipants(Map<String, String> var1) throws LocalExceptionWrapper;

   int[] getParticipantUserIDs(Map<String, String> var1) throws LocalExceptionWrapper;

   void addParticipants(String var1, String[] var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void addUserToGroupChatDebug(String var1, boolean var2, boolean var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;
}
