package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface MessageSwitchboardAdminPrx extends ObjectPrx {
   MessageSwitchboardStats getStats() throws FusionException;

   MessageSwitchboardStats getStats(Map<String, String> var1) throws FusionException;
}
