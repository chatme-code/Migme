package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ReputationServicePrx;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DailyGatherAndProcess implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DailyGatherAndProcess.class));

   public void execute(JobExecutionContext context) throws JobExecutionException {
      try {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JobSchedulerSettings.REPUTATION_DAILY_GATHER_AND_PROCESS_ENABLED)) {
            log.info("Not running DailyGatherAndProcess job since disabled in system property");
            return;
         }

         log.info("triggering DailyGatherAndProcess");
         ReputationServicePrx reputationServiceProxy = (ReputationServicePrx)context.getScheduler().getContext().get("reputationServiceProxy");
         if (reputationServiceProxy == null) {
            IcePrxFinder icePrxFinder = (IcePrxFinder)context.getScheduler().getContext().get("icePrxFinder");
            reputationServiceProxy = icePrxFinder.waitForReputationServiceProxy();
         }

         reputationServiceProxy.gatherAndProcess();
      } catch (Exception var4) {
         log.error("failed to reputationServiceProxy.gatherAndProcess()", var4);
      }

   }
}
