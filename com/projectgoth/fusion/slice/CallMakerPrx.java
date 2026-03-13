package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface CallMakerPrx extends ObjectPrx {
   CallDataIce requestCallback(CallDataIce var1, int var2, int var3) throws FusionException;

   CallDataIce requestCallback(CallDataIce var1, int var2, int var3, Map<String, String> var4) throws FusionException;
}
