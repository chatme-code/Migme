package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface EmailAlertAdminPrx extends ObjectPrx {
   EmailAlertStats getStats() throws FusionException;

   EmailAlertStats getStats(Map<String, String> var1) throws FusionException;
}
