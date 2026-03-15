package com.projectgoth.fusion.sessioncache;

import Ice.Current;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionCacheStats;
import com.projectgoth.fusion.slice._SessionCacheAdminDisp;

public class SessionCacheAdminI extends _SessionCacheAdminDisp {
   private SessionCacheI sessionCache;

   public SessionCacheAdminI(SessionCacheI sessionCacheI) {
      this.sessionCache = sessionCacheI;
   }

   public SessionCacheStats getStats(Current __current) throws FusionException {
      SessionCacheStats stats = ServiceStatsFactory.getSessionCacheStats(SessionCache.startTime);

      try {
         stats.sessionsReceivedPerSecond = this.sessionCache.getReceivedSessionsCounter().getRequestsPerSecond();
         stats.maxSessionsReceivedPerSecond = this.sessionCache.getReceivedSessionsCounter().getMaxRequestsPerSecond();
         stats.dateOfMaxSessionsReceivedPerSecond = this.sessionCache.getReceivedSessionsCounter().getDateOfMaxRequestsPerSecond().getTime();
         stats.sessionsQueuedToBeArchived = this.sessionCache.getSessionsWaitingToBeArchived();
         stats.uniqueSummariesTaskRunning = this.sessionCache.isUniqueSummariesTaskRunning();
         stats.abortedBatches = this.sessionCache.getAbortedBatches().intValue();
         return stats;
      } catch (Exception var4) {
         throw new FusionException("Initialisation incomplete");
      }
   }
}
