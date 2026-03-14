package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;

public interface ChatSourceGroup {
   void putMessage(MessageDataIce var1) throws FusionException;

   String[] getParticipants(String var1);

   boolean isParticipant(String var1) throws FusionException;

   void sendGamesHelpToUser(String var1) throws FusionException;

   void startBot(String var1, String var2) throws FusionException;

   void stopAllBots(String var1, int var2) throws FusionException;

   void stopBot(String var1, String var2) throws FusionException;
}
