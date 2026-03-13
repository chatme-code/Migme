package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MiniblogDailyDigestHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext arg0) throws JobExecutionException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JobSchedulerSettings.DAILY_DIGEST_FOR_USERS_BEING_FOLLOWED_ENABLED)) {
         log.info("Daily digest is disable");
      } else {
         boolean semaphoreAquired = false;

         try {
            semaphoreAquired = semaphore.tryAcquire();
            if (!semaphoreAquired) {
               log.warn("Another job is still triggering the Migbo Daily Digest Emails To Users Being Followed Within A Cerain Time Scope. Exiting...");
               return;
            }

            log.info("Migbo Daily Digest Email To Users Being Followed Within A Cerain Time Scope Trigger [START]");
            long total = MiniblogDailyDigestHelper.sendRecentlyFollowedDailyDigest(System.currentTimeMillis());
            MiniblogDailyDigestHelper.truncateSortedSetAndHashForDailyDigest();
            log.info("Migbo Daily Digest Email To Users Being Followed Within A Cerain Time Scope Trigger [END], total users:" + total);
         } finally {
            if (semaphoreAquired) {
               semaphore.release();
            }

         }

      }
   }
}
