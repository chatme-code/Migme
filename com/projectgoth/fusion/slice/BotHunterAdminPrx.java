package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface BotHunterAdminPrx extends ObjectPrx {
   BotHunterStats getStats() throws FusionException;

   BotHunterStats getStats(Map<String, String> var1) throws FusionException;
}
