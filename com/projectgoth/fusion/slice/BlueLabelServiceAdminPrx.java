package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface BlueLabelServiceAdminPrx extends ObjectPrx {
   BlueLabelServiceStats getStats() throws FusionException;

   BlueLabelServiceStats getStats(Map<String, String> var1) throws FusionException;
}
