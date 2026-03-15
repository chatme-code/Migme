package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface GatewayAdminPrx extends ObjectPrx {
   GatewayStats getStats() throws FusionException;

   GatewayStats getStats(Map<String, String> var1) throws FusionException;

   void sendAlertToAllConnections(String var1, String var2);

   void sendAlertToAllConnections(String var1, String var2, Map<String, String> var3);
}
