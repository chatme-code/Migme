package com.projectgoth.fusion.objectcache;

import Ice.Properties;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

public interface ChatObjectManagerRoom {
   RegistryPrx getRegistryPrx();

   ChatRoomPrx findChatRoomPrx(String var1) throws FusionException;

   Properties getProperties();

   IcePrxFinder getIcePrxFinder();

   void onRoomSessionRemoved();

   void onRoomSessionAdded();

   void logMessage(MessageToLog.TypeEnum var1, int var2, String var3, String var4, int var5, String var6);

   ScheduledExecutorService getDistributionService();

   long getChatRoomIdleTimeout();

   Semaphore getGiftAllSemaphore();
}
