package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface ReputationServiceAdminPrx extends ObjectPrx {
   ReputationServiceStats getStats() throws FusionException;

   ReputationServiceStats getStats(Map<String, String> var1) throws FusionException;
}
