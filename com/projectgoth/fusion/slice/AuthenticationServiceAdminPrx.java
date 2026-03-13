package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface AuthenticationServiceAdminPrx extends ObjectPrx {
   AuthenticationServiceStats getStats() throws FusionException;

   AuthenticationServiceStats getStats(Map<String, String> var1) throws FusionException;
}
