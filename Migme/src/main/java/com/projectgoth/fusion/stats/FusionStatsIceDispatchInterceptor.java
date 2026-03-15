package com.projectgoth.fusion.stats;

import Ice.DispatchInterceptor;
import Ice.DispatchInterceptorAsyncCallback;
import Ice.DispatchStatus;
import Ice.Object;
import Ice.Request;
import com.projectgoth.fusion.common.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionStatsIceDispatchInterceptor extends DispatchInterceptor {
   private static final Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(FusionStatsIceDispatchInterceptor.class));
   private Object servant;

   public FusionStatsIceDispatchInterceptor(Object servant) {
      this.servant = servant;
   }

   public DispatchStatus dispatch(Request request) {
      try {
         IceStats.getInstance().addRequest(request);
      } catch (Exception var7) {
         log.warn("Failed to add method invocation request " + request + " to IceStats: " + var7, var7);
      }

      long startTime = System.currentTimeMillis();
      DispatchStatus ds = this.servant.ice_dispatch(request, (DispatchInterceptorAsyncCallback)null);

      try {
         IceStats.getInstance().onRequestDispatched(request, startTime);
      } catch (Exception var6) {
         log.warn("Failed to add timing for method invocation request " + request + " to IceStats: " + var6, var6);
      }

      return ds;
   }
}
