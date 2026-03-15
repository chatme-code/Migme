package com.projectgoth.fusion.slice;

import java.util.Map;

public interface GroupChatPrx extends BotChannelPrx {
   void addParticipantInner(String var1, String var2, boolean var3) throws FusionException;

   void addParticipantInner(String var1, String var2, boolean var3, Map<String, String> var4) throws FusionException;

   void addParticipant(String var1, String var2) throws FusionException;

   void addParticipant(String var1, String var2, Map<String, String> var3) throws FusionException;

   boolean removeParticipant(String var1) throws FusionException;

   boolean removeParticipant(String var1, Map<String, String> var2) throws FusionException;

   void putMessage(MessageDataIce var1) throws FusionException;

   void putMessage(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void putFileReceived(MessageDataIce var1) throws FusionException;

   void putFileReceived(MessageDataIce var1, Map<String, String> var2) throws FusionException;

   void sendInitialMessages();

   void sendInitialMessages(Map<String, String> var1);

   int getNumParticipants();

   int getNumParticipants(Map<String, String> var1);

   boolean supportsBinaryMessage(String var1);

   boolean supportsBinaryMessage(String var1, Map<String, String> var2);

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3) throws FusionException;

   int executeEmoteCommandWithState(String var1, MessageDataIce var2, SessionPrx var3, Map<String, String> var4) throws FusionException;

   String getId();

   String getId(Map<String, String> var1);

   String getCreatorUsername();

   String getCreatorUsername(Map<String, String> var1);

   int getCreatorUserID();

   int getCreatorUserID(Map<String, String> var1);

   int getPrivateChatPartnerUserID();

   int getPrivateChatPartnerUserID(Map<String, String> var1);

   String listOfParticipants();

   String listOfParticipants(Map<String, String> var1);

   int[] getParticipantUserIDs();

   int[] getParticipantUserIDs(Map<String, String> var1);

   void addParticipants(String var1, String[] var2) throws FusionException;

   void addParticipants(String var1, String[] var2, Map<String, String> var3) throws FusionException;

   void addUserToGroupChatDebug(String var1, boolean var2, boolean var3) throws FusionException;

   void addUserToGroupChatDebug(String var1, boolean var2, boolean var3, Map<String, String> var4) throws FusionException;
}
