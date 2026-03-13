package com.projectgoth.fusion.slice;

public interface _BotServiceOperationsNC {
   BotInstance addBotToChannel(BotChannelPrx var1, String var2, String var3, boolean var4) throws FusionException;

   void removeBot(String var1, boolean var2) throws FusionException;

   void sendMessageToBot(String var1, String var2, String var3, long var4) throws FusionException;

   void sendMessageToBotsInChannel(String var1, String var2, String var3, long var4) throws FusionException;

   void sendNotificationToBotsInChannel(String var1, String var2, int var3) throws FusionException;
}
