package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _BotServiceDel extends _ObjectDel {
   BotInstance addBotToChannel(BotChannelPrx var1, String var2, String var3, boolean var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void removeBot(String var1, boolean var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void sendMessageToBot(String var1, String var2, String var3, long var4, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void sendMessageToBotsInChannel(String var1, String var2, String var3, long var4, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void sendNotificationToBotsInChannel(String var1, String var2, int var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;
}
