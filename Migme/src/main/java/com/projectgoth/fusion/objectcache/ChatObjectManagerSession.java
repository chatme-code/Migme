package com.projectgoth.fusion.objectcache;

import Ice.Properties;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;

public interface ChatObjectManagerSession {
   RequestCounter getRequestCounter();

   MessageSwitchboardPrx getMessageSwitchboardPrx() throws FusionException;

   UserPrx findUserPrx(String var1) throws FusionException;

   GroupChatPrx findGroupChatPrx(String var1) throws FusionException;

   ChatRoomPrx findChatRoomPrx(String var1) throws FusionException;

   SessionPrx findSessionPrx(String var1);

   SessionPrx makeSessionPrx(String var1);

   Properties getProperties();

   IcePrxFinder getIcePrxFinder();

   void logMessage(MessageToLog.TypeEnum var1, int var2, String var3, String var4, int var5, String var6);

   ObjectCacheContext getApplicationContext();

   ChatContentStore getFileStore();
}
