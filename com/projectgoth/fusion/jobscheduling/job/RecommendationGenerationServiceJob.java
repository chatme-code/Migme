package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.RecommendationGenerationServicePrx;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class RecommendationGenerationServiceJob implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecommendationGenerationServiceJob.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext context) throws JobExecutionException {
      if (!semaphore.tryAcquire()) {
         log.warn("Another job is processing this recommendation generation transformation. Exiting...");
      } else {
         try {
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.GENERATION_SERVICE_VIA_QUARTZ_SCHEDULER_ENABLED)) {
               log.info("Not running RecommendationGenerationServiceJob via quartz scheduler since disabled in system property");
               return;
            }

            log.info("triggering RecommendationGenerationServiceJob via quartz scheduler");
            IcePrxFinder icePrxFinder = (IcePrxFinder)context.getScheduler().getContext().get("icePrxFinder");
            RecommendationGenerationServicePrx rgsProxy = icePrxFinder.waitForRecommendationGenerationServiceProxy();
            JobDataMap jobDataMap = context.getMergedJobDataMap();
            int transID = jobDataMap.getInt("transformationID");
            rgsProxy.runTransformation(transID);
         } catch (Exception var10) {
            log.error("failed to recommendationGenerationServiceProxy.invokeJob()", var10);
         } finally {
            semaphore.release();
         }

      }
   }
}
