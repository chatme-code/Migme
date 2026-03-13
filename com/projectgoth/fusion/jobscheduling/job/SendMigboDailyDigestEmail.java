package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendMigboDailyDigestEmail implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SendMigboDailyDigestEmail.class));
   private static final Semaphore semaphore = new Semaphore(1);

   public void execute(JobExecutionContext arg0) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (!semaphoreAquired) {
            log.warn("Another job is still triggering the Migbo Daily Digest Emails. Exiting...");
            return;
         }

         log.info("Migbo Daily Digest Email Trigger [START]");
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         int windowDurationInDays = SystemProperty.getInt((String)"DailyDigestWindowInDays", 14);
         long ONEDAY_IN_MILLIS = 86400000L;
         long current = System.currentTimeMillis();
         long oneDayAgo = current - ONEDAY_IN_MILLIS;
         long windowBoundary = current - (long)windowDurationInDays * ONEDAY_IN_MILLIS;
         long totalProcessed = 0L;
         long totalSentOK = 0L;
         long totalSentFailed = 0L;
         long totalInvalidAddress = 0L;
         long totalInactive = 0L;

         long startTime;
         for(startTime = System.currentTimeMillis(); current > windowBoundary; oneDayAgo -= ONEDAY_IN_MILLIS) {
            try {
               Map<String, Integer> users = userBean.getLastLoggedInUsersWithVerifiedEmail(UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY, new Timestamp(oneDayAgo), new Timestamp(current), (Connection)null);
               Iterator i$ = users.entrySet().iterator();

               while(i$.hasNext()) {
                  Entry<String, Integer> u = (Entry)i$.next();
                  ++totalProcessed;
                  String username = (String)u.getKey();
                  Integer userid = (Integer)u.getValue();

                  try {
                     String pathPrefix = String.format("/user/%d/email/%d", userid, Enums.EmailTypeEnum.DAILY_DIGEST.value());
                     String postData = "{}";
                     if (apiUtil.postAndCheckOk(pathPrefix, postData)) {
                        ++totalSentOK;
                        log.info(String.format("Successfully triggered daily digest email for [%s][%d] via migbo-datasvc [%s]", username, userid, pathPrefix));
                     } else {
                        ++totalSentFailed;
                        log.warn(String.format("Faled to trigger email for [%s][%d] via migbo-datasvc [%s]", username, userid, pathPrefix));
                     }
                  } catch (Exception var39) {
                     log.error(String.format("Unable to send daily digest email to username[%s] userid[%d]: %s", username, userid, var39.getMessage()), var39);
                  }
               }
            } catch (RemoteException var40) {
               log.error(String.format("Unable to send email for users logged in between [%d] and [%d]: %s", oneDayAgo, current, var40.getMessage()), var40);
            }

            current -= ONEDAY_IN_MILLIS;
         }

         long timeTaken = System.currentTimeMillis() - startTime;
         log.info(String.format("Processed [%d] users. Account Inactive [%d] Invalid address [%d]. Sent OK [%d]. Sent Failed [%d]", totalProcessed, totalInactive, totalInvalidAddress, totalSentOK, totalSentFailed));
         log.info(String.format("Migbo Daily Digest Email Trigger [COMPLETE] Time taken: %d seconds", TimeUnit.MILLISECONDS.toSeconds(timeTaken)));
      } catch (CreateException var41) {
         log.error(String.format("Unable to load UserEJB - createException caught : %s", var41.getMessage()), var41);
      } finally {
         if (semaphoreAquired) {
            semaphore.release();
         }

      }

   }
}
