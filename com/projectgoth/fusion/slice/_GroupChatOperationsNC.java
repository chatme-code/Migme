package com.projectgoth.fusion.slice;

public interface _GroupChatOperationsNC extends _BotChannelOperationsNC {
   void addParticipantInner(String var1, String var2, boolean var3) throws FusionException;

   void addParticipant(String var1, String var2) throws FusionException;

   boolean removeParticipant(String var1) throws FusionException;

   void putMessage(MessageDataIce var1) throws FusionException;

   void putFileReceived(MessageDataIce var1) throws FusionException;

   void sendInitialMessages();

   int getNumParticipants();

   boolean supportsBinaryMessage(String var1);

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3) throws FusionException;

   String getId();

   String getCreatorUsername();

   int getCreatorUserID();

   int getPrivateChatPartnerUserID();

   String listOfParticipants();

   int[] getParticipantUserIDs();

   void addParticipants(String var1, String[] var2) throws FusionException;

   void addUserToGroupChatDebug(String var1, boolean var2, boolean var3) throws FusionException;
}
