package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LeaderboardWeeklyComputation implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Leaderboard.class));
   private static final Semaphore semaphore = new Semaphore(1);
   private static final Set<Leaderboard.Type> TO_EXCLUDE;

   public void execute(JobExecutionContext context) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (!semaphoreAquired) {
            log.warn("Another job is still processing LeaderboardWeeklyComputation. Exiting...");
            return;
         }
      } catch (Exception var10) {
         log.error("Unable to acquire semaphore");
         return;
      }

      if (!SystemProperty.getBool("EnableLeaderboards", false)) {
         log.warn("Leaderboards disabled through SystemProperty.EnableLeaderboards key");
      } else {
         try {
            log.info("Get last login users");
            Leaderboard.setLastLoggedIn();
            log.info("[COMPLETED] Get last login users");
         } catch (Exception var9) {
            log.error("Unable to process job 'Get last login users': " + var9.getLocalizedMessage());
         }

         this.executeJobAllTime(Leaderboard.Type.MIG_LEVEL);
         Leaderboard.Type[] arr$ = Leaderboard.Type.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Leaderboard.Type t = arr$[i$];
            if (!TO_EXCLUDE.contains(t)) {
               this.executeJob(t);
            }
         }

         try {
            log.info("Free last login users");
            Leaderboard.freeLastLoggedIn();
            log.info("[COMPLETED] Free last login users");
         } catch (Exception var8) {
            log.error("Unable to free LastLoggedIn: " + var8.getLocalizedMessage());
         }

         try {
            if (semaphoreAquired) {
               semaphore.release();
            }

         } catch (Exception var7) {
            log.warn("Unable to release semaphore: " + var7.getMessage());
            throw new JobExecutionException(var7);
         }
      }
   }

   private void executeJob(Leaderboard.Type leaderboardType) {
      String item = leaderboardType.toString() + Leaderboard.Period.WEEKLY.toString();

      try {
         log.info("Leaderboard reset for " + item);
         Leaderboard.reset(leaderboardType, Leaderboard.Period.WEEKLY, Leaderboard.Period.PREVIOUS_WEEKLY);
         log.info("[COMPLETED] Leaderboard reset for " + item);
      } catch (Exception var4) {
         log.error("Unable to process job " + item + ": " + var4.getLocalizedMessage());
      }

      this.executeJobAllTime(leaderboardType);
   }

   private void executeJobAllTime(Leaderboard.Type leaderboardType) {
   }

   static {
      TO_EXCLUDE = new HashSet(Arrays.asList(Leaderboard.Type.MIG_LEVEL));
   }
}
