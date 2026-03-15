package com.projectgoth.fusion.slice;

public interface _GatewayAdminOperationsNC {
   GatewayStats getStats() throws FusionException;

   void sendAlertToAllConnections(String var1, String var2);
}
