package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _BotServiceOperations {
   BotInstance addBotToChannel(BotChannelPrx var1, String var2, String var3, boolean var4, Current var5) throws FusionException;

   void removeBot(String var1, boolean var2, Current var3) throws FusionException;

   void sendMessageToBot(String var1, String var2, String var3, long var4, Current var6) throws FusionException;

   void sendMessageToBotsInChannel(String var1, String var2, String var3, long var4, Current var6) throws FusionException;

   void sendNotificationToBotsInChannel(String var1, String var2, int var3, Current var4) throws FusionException;
}
