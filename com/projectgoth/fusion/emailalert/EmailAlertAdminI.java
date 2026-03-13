package com.projectgoth.fusion.emailalert;

import Ice.Current;
import com.projectgoth.fusion.slice.EmailAlertStats;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice._EmailAlertAdminDisp;

public class EmailAlertAdminI extends _EmailAlertAdminDisp {
   public EmailAlertStats getStats(Current __current) throws FusionException {
      EmailAlertStats stats = new EmailAlertStats();

      try {
         stats.hostName = EmailAlert.hostName;
         stats.numNotificationsReceivedPerSecond = EmailAlert.receivedNotificationsCounter.getRequestsPerSecond();
         stats.maxNotificationsReceivedPerSecond = EmailAlert.receivedNotificationsCounter.getMaxRequestsPerSecond();
         stats.numNotificationsProcessedPerSecond = EmailAlert.processedNotificationsCounter.getRequestsPerSecond();
         stats.maxNotificationsProcessedPerSecond = EmailAlert.processedNotificationsCounter.getMaxRequestsPerSecond();
         stats.notificationsThreadPoolSize = EmailAlert.notificationsThreadPool.getActiveCount();
         stats.notificationsMaxThreadPoolSize = EmailAlert.notificationsThreadPool.getLargestPoolSize();
         stats.notificationsThreadPoolQueueSize = EmailAlert.notificationsThreadPool.getQueue().size();
         stats.gatewayQueriesThreadPoolSize = EmailAlert.gatewayQueriesPool.getActiveCount();
         stats.gatewayQueriesMaxThreadPoolSize = EmailAlert.gatewayQueriesPool.getLargestPoolSize();
         stats.gatewayQueriesThreadPoolQueueSize = EmailAlert.gatewayQueriesPool.getQueue().size();
         stats.numGatewayQueriesReceivedPerSecond = EmailAlert.receivedGatewayQueriesCounter.getRequestsPerSecond();
         stats.maxGatewayQueriesReceivedPerSecond = EmailAlert.receivedGatewayQueriesCounter.getMaxRequestsPerSecond();
         stats.numGatewayQueriesProcessedPerSecond = EmailAlert.processedGatewayQueriesCounter.getRequestsPerSecond();
         stats.maxGatewayQueriesProcessedPerSecond = EmailAlert.processedGatewayQueriesCounter.getMaxRequestsPerSecond();
         stats.numGatewayQueriesDiscardedPerSecond = EmailAlert.discardedGatewayQueriesCounter.getRequestsPerSecond();
         stats.maxGatewayQueriesDiscardedPerSecond = EmailAlert.discardedGatewayQueriesCounter.getMaxRequestsPerSecond();
         stats.jvmFreeMemory = Runtime.getRuntime().freeMemory();
         stats.jvmTotalMemory = Runtime.getRuntime().totalMemory();
         stats.uptime = System.currentTimeMillis() - EmailAlert.startTime;
         return stats;
      } catch (Exception var5) {
         FusionException fe = new FusionException();
         fe.message = "Initialisation incomplete";
         throw fe;
      }
   }
}
