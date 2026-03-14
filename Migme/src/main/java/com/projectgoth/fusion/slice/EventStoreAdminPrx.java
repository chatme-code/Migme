package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface EventStoreAdminPrx extends ObjectPrx {
   EventStoreStats getStats() throws FusionException;

   EventStoreStats getStats(Map<String, String> var1) throws FusionException;
}
