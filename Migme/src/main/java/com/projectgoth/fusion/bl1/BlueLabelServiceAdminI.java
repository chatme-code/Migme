package com.projectgoth.fusion.bl1;

import Ice.Current;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.BlueLabelServiceStats;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice._BlueLabelServiceAdminDisp;

public class BlueLabelServiceAdminI extends _BlueLabelServiceAdminDisp {
   private BlueLabelServiceI blueLabelService;

   public BlueLabelServiceAdminI(BlueLabelServiceI blueLabelService) {
      this.blueLabelService = blueLabelService;
   }

   public BlueLabelServiceStats getStats(Current __current) throws FusionException {
      BlueLabelServiceStats stats = ServiceStatsFactory.getBlueLabelServiceStats(BlueLabelService.startTime);
      stats.recentErrors = this.blueLabelService.getRecentErrorCount();
      return stats;
   }
}
