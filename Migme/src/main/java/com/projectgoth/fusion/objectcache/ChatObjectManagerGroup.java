package com.projectgoth.fusion.objectcache;

import Ice.Properties;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.messagelogger.MessageToLog;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;

public interface ChatObjectManagerGroup {
   IcePrxFinder getIcePrxFinder();

   UserPrx findUserPrx(String var1) throws FusionException;

   RegistryPrx getRegistryPrx();

   void onGroupSessionRemoved();

   void onGroupSessionAdded();

   void logMessage(MessageToLog.TypeEnum var1, int var2, String var3, String var4, int var5, String var6);

   Properties getProperties();

   boolean isLogMessagesToFile();

   boolean isLogMessagesToDB();
}
