package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface EventSystemAdminPrx extends ObjectPrx {
   EventSystemStats getStats() throws FusionException;

   EventSystemStats getStats(Map<String, String> var1) throws FusionException;
}
