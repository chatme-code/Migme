package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _GroupChatOperations extends _BotChannelOperations {
   void addParticipantInner(String var1, String var2, boolean var3, Current var4) throws FusionException;

   void addParticipant(String var1, String var2, Current var3) throws FusionException;

   boolean removeParticipant(String var1, Current var2) throws FusionException;

   void putMessage(MessageDataIce var1, Current var2) throws FusionException;

   void putFileReceived(MessageDataIce var1, Current var2) throws FusionException;

   void sendInitialMessages(Current var1);

   int getNumParticipants(Current var1);

   boolean supportsBinaryMessage(String var1, Current var2);

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3, Current var4) throws FusionException;

   String getId(Current var1);

   String getCreatorUsername(Current var1);

   int getCreatorUserID(Current var1);

   int getPrivateChatPartnerUserID(Current var1);

   String listOfParticipants(Current var1);

   int[] getParticipantUserIDs(Current var1);

   void addParticipants(String var1, String[] var2, Current var3) throws FusionException;

   void addUserToGroupChatDebug(String var1, boolean var2, boolean var3, Current var4) throws FusionException;
}
