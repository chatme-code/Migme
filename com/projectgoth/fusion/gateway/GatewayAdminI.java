package com.projectgoth.fusion.gateway;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GatewayStats;
import com.projectgoth.fusion.slice._GatewayAdminDisp;
import java.util.Iterator;
import org.apache.log4j.Logger;

public class GatewayAdminI extends _GatewayAdminDisp {
   private Gateway gateway;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GatewayAdminI.class));

   public GatewayAdminI(Gateway gateway) {
      this.gateway = gateway;
   }

   public GatewayStats getStats(Current __current) throws FusionException {
      try {
         return this.gateway.getStats();
      } catch (Exception var4) {
         FusionException fe = new FusionException();
         fe.message = "Initialisation incomplete";
         throw fe;
      }
   }

   public void sendAlertToAllConnections(String message, String title, Current __current) {
      int exceptionCount = 0;
      int dispatchCount = 0;
      Iterator i$ = this.gateway.getConnections().iterator();

      while(i$.hasNext()) {
         ConnectionI connection = (ConnectionI)i$.next();

         try {
            connection.putAlertMessage(message, title, (short)0);
            ++dispatchCount;
         } catch (Exception var9) {
            ++exceptionCount;
         }
      }

      if (exceptionCount != 0) {
         log.error(String.format("Caught %d exceptions while dispatching alert to all conections", exceptionCount));
      }

      log.info(String.format("Message \"%s\" dispatched successfully to %d connections", message, dispatchCount));
   }
}
