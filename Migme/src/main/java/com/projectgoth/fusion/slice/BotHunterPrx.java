package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface BotHunterPrx extends ObjectPrx {
   SuspectGroupIce[] getLatestSuspects() throws FusionException;

   SuspectGroupIce[] getLatestSuspects(Map<String, String> var1) throws FusionException;
}
