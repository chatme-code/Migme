package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface UserNotificationServiceAdminPrx extends ObjectPrx {
   UserNotificationServiceStats getStats() throws FusionException;

   UserNotificationServiceStats getStats(Map<String, String> var1) throws FusionException;
}
