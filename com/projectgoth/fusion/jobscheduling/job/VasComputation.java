package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.VasDAO;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class VasComputation implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VasComputation.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext context) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (semaphoreAquired) {
            VasDAO vasDAO = (VasDAO)context.getScheduler().getContext().get("vasDAO");
            vasDAO.init();
            vasDAO.generateUniqueUsers();
            vasDAO.generateRegistration();
            vasDAO.generateAuthentication();
            vasDAO.generateActiveUsers();
            vasDAO.generateCreditSpending();
            return;
         }

         log.warn("Another job is still processing vas computation. Exiting...");
      } catch (Exception var8) {
         log.error("Error in executing VAS Computation", var8);
         throw new JobExecutionException(var8);
      } finally {
         if (semaphoreAquired) {
            semaphore.release();
         }

      }

   }
}
