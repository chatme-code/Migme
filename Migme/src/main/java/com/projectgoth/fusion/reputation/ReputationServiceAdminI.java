package com.projectgoth.fusion.reputation;

import Ice.Current;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ReputationServiceStats;
import com.projectgoth.fusion.slice._ReputationServiceAdminDisp;

public class ReputationServiceAdminI extends _ReputationServiceAdminDisp {
   private ReputationServiceI reputationService;

   public ReputationServiceAdminI(ReputationServiceI reputationService) {
      this.reputationService = reputationService;
   }

   public ReputationServiceStats getStats(Current __current) throws FusionException {
      ReputationServiceStats stats = ServiceStatsFactory.getReputationServiceStats(ReputationService.startTime);
      stats.lastTimeRunCompleted = this.reputationService.getLastTimeRunCompleted();
      stats.processing = this.reputationService.isProcessing();
      return stats;
   }
}
