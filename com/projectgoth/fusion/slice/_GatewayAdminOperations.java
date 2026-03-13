package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _GatewayAdminOperations {
   GatewayStats getStats(Current var1) throws FusionException;

   void sendAlertToAllConnections(String var1, String var2, Current var3);
}
