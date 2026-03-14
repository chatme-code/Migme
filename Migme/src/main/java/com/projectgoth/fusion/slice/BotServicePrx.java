package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface BotServicePrx extends ObjectPrx {
   BotInstance addBotToChannel(BotChannelPrx var1, String var2, String var3, boolean var4) throws FusionException;

   BotInstance addBotToChannel(BotChannelPrx var1, String var2, String var3, boolean var4, Map<String, String> var5) throws FusionException;

   void removeBot(String var1, boolean var2) throws FusionException;

   void removeBot(String var1, boolean var2, Map<String, String> var3) throws FusionException;

   void sendMessageToBot(String var1, String var2, String var3, long var4) throws FusionException;

   void sendMessageToBot(String var1, String var2, String var3, long var4, Map<String, String> var6) throws FusionException;

   void sendMessageToBotsInChannel(String var1, String var2, String var3, long var4) throws FusionException;

   void sendMessageToBotsInChannel(String var1, String var2, String var3, long var4, Map<String, String> var6) throws FusionException;

   void sendNotificationToBotsInChannel(String var1, String var2, int var3) throws FusionException;

   void sendNotificationToBotsInChannel(String var1, String var2, int var3, Map<String, String> var4) throws FusionException;
}
