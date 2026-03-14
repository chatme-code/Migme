package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface BotServiceAdminPrx extends ObjectPrx {
   int ping();

   int ping(Map<String, String> var1);

   BotServiceStats getStats() throws FusionException;

   BotServiceStats getStats(Map<String, String> var1) throws FusionException;
}
