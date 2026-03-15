package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface SessionCacheAdminPrx extends ObjectPrx {
   SessionCacheStats getStats() throws FusionException;

   SessionCacheStats getStats(Map<String, String> var1) throws FusionException;
}
