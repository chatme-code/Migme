package com.projectgoth.fusion.jobscheduling;

import Ice.Current;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.JobSchedulingServiceStats;
import com.projectgoth.fusion.slice._JobSchedulingServiceAdminDisp;

public class JobSchedulingServiceAdminI extends _JobSchedulingServiceAdminDisp {
   private JobSchedulingServiceI jobSchedulingService;

   public JobSchedulingServiceAdminI(JobSchedulingServiceI jobSchedulingService) {
      this.jobSchedulingService = jobSchedulingService;
   }

   public JobSchedulingServiceStats getStats(Current __current) throws FusionException {
      JobSchedulingServiceStats stats = ServiceStatsFactory.getJobSchedulingServiceStats(JobSchedulingService.startTime);
      stats.currentJobs = this.jobSchedulingService.getJobsRunningNow();
      return stats;
   }
}
