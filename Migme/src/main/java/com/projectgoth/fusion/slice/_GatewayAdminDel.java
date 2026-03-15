package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _GatewayAdminDel extends _ObjectDel {
   GatewayStats getStats(Map<String, String> var1) throws LocalExceptionWrapper, FusionException;

   void sendAlertToAllConnections(String var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper;
}
