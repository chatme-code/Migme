package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface ImageServerAdminPrx extends ObjectPrx {
   ImageServerStats getStats() throws FusionException;

   ImageServerStats getStats(Map<String, String> var1) throws FusionException;
}
