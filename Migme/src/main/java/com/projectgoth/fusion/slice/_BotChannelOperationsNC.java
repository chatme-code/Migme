package com.projectgoth.fusion.slice;

public interface _BotChannelOperationsNC {
   void startBot(String var1, String var2) throws FusionException;

   void stopBot(String var1, String var2) throws FusionException;

   void stopAllBots(String var1, int var2) throws FusionException;

   void botKilled(String var1) throws FusionException;

   void sendMessageToBots(String var1, String var2, long var3) throws FusionException;

   void putBotMessage(String var1, String var2, String var3, String[] var4, boolean var5) throws FusionException;

   void putBotMessageToUsers(String var1, String[] var2, String var3, String[] var4, boolean var5) throws FusionException;

   void putBotMessageToAllUsers(String var1, String var2, String[] var3, boolean var4) throws FusionException;

   void sendGamesHelpToUser(String var1) throws FusionException;

   boolean isParticipant(String var1) throws FusionException;

   String[] getParticipants(String var1);
}
