package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fashionshow.AvatarCandidates;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AvatarCandidatePooling implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AvatarCandidatePooling.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext context) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (semaphoreAquired) {
            log.info("Populating avatar candidates");
            AvatarCandidates.populateAvatarCandidates();
            log.info("[COMPLETED] Populating avatar candidates");
            return;
         }

         log.warn("Another job is still processing user referrals. Exiting...");
      } catch (Exception var8) {
         log.error("Unable to populate avatar candidate pool: " + var8.getMessage(), var8);
         throw new JobExecutionException(var8);
      } finally {
         if (semaphoreAquired) {
            semaphore.release();
         }

      }

   }
}
