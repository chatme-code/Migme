package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _BotChannelDel extends _ObjectDel {
   void startBot(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void stopBot(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void stopAllBots(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   void botKilled(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void sendMessageToBots(String var1, String var2, long var3, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void putBotMessage(String var1, String var2, String var3, String[] var4, boolean var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void putBotMessageToUsers(String var1, String[] var2, String var3, String[] var4, boolean var5, Map<String, String> var6) throws LocalExceptionWrapper, FusionException;

   void putBotMessageToAllUsers(String var1, String var2, String[] var3, boolean var4, Map<String, String> var5) throws LocalExceptionWrapper, FusionException;

   void sendGamesHelpToUser(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   boolean isParticipant(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   String[] getParticipants(String var1, Map<String, String> var2) throws LocalExceptionWrapper;
}
