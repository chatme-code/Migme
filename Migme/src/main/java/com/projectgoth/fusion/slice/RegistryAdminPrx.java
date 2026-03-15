package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface RegistryAdminPrx extends ObjectPrx {
   RegistryStats getStats() throws FusionException;

   RegistryStats getStats(Map<String, String> var1) throws FusionException;
}
