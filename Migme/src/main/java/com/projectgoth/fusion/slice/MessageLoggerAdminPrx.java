package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface MessageLoggerAdminPrx extends ObjectPrx {
   MessageLoggerStats getStats() throws FusionException;

   MessageLoggerStats getStats(Map<String, String> var1) throws FusionException;
}
