package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface SMSEngineAdminPrx extends ObjectPrx {
   SMSEngineStats getStats() throws FusionException;

   SMSEngineStats getStats(Map<String, String> var1) throws FusionException;
}
