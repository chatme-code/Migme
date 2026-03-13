package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.LeaderboardDAO;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LeaderboardComputation implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserReferralNotification.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext context) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (!semaphoreAquired) {
            log.warn("Another job is still processing user referrals. Exiting...");
            return;
         }

         LeaderboardDAO leaderboardDAO = (LeaderboardDAO)context.getScheduler().getContext().get("leaderboardDAO");
         leaderboardDAO.generateLeaderboard(1);
         leaderboardDAO.generateLeaderboard(2);
         leaderboardDAO.generateLeaderboard(3);
         leaderboardDAO.generateLeaderboard(4);
         leaderboardDAO.generateLeaderboard(5);
         leaderboardDAO.generateLeaderboard(6);
         leaderboardDAO.generateLeaderboard(7);
         leaderboardDAO.generateLeaderboard(8);
         leaderboardDAO.generateLeaderboard(9);
         leaderboardDAO.generateLeaderboard(10);
         leaderboardDAO.generateLeaderboard(11);
      } catch (Exception var8) {
         log.error("xxxxx", var8);
         throw new JobExecutionException(var8);
      } finally {
         if (semaphoreAquired) {
            semaphore.release();
         }

      }

   }
}
