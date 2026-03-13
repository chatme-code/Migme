package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface EventQueueWorkerServiceAdminPrx extends ObjectPrx {
   EventQueueWorkerServiceStats getStats() throws FusionException;

   EventQueueWorkerServiceStats getStats(Map<String, String> var1) throws FusionException;
}
