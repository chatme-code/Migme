package com.projectgoth.fusion.jobscheduling;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class JobSchedulingService extends Application {
   private static Logger log = Logger.getLogger(JobSchedulingService.class);
   public static ObjectAdapter JobSchedulingServiceAdapter = null;
   public static Properties properties = null;
   public static long startTime = System.currentTimeMillis();
   private String hostName;
   private JobSchedulingServiceI jobSchedulingServiceServant;
   private IcePrxFinder icePrxFinder;

   @Required
   public void setJobSchedulingServiceServant(JobSchedulingServiceI jobSchedulingServiceServant) {
      this.jobSchedulingServiceServant = jobSchedulingServiceServant;
   }

   private void configureServant(Properties properties) throws Exception {
      this.icePrxFinder = new IcePrxFinder(communicator(), properties);
      UserNotificationServicePrx unsProxy = this.icePrxFinder.waitForUserNotificationServiceProxy();
      if (unsProxy == null) {
         log.fatal("Aborting, weren't able to find a UNS proxy..");
         System.exit(1);
      }

      this.jobSchedulingServiceServant.setIcePrxFinder(this.icePrxFinder);
      this.jobSchedulingServiceServant.setUserNotificationServiceProxy(unsProxy);
      this.jobSchedulingServiceServant.initializeScheduler();
      this.jobSchedulingServiceServant.scheduleDailyGatherAndProcessJob(properties.getPropertyWithDefault("DailyGatherAndProcessCronExpression", "0 0 2 ? * *"));
      int interval = properties.getPropertyAsIntWithDefault("SubscriptionRenewalInterval", 900000);
      if (interval > 0) {
         this.jobSchedulingServiceServant.scheduleSubscriptionRenewal(System.currentTimeMillis(), interval);
      }

      interval = properties.getPropertyAsIntWithDefault("UserReferralNotificationInterval", 900000);
      if (interval > 0) {
         this.jobSchedulingServiceServant.scheduleUserReferralNotification(System.currentTimeMillis(), interval);
      }

      this.jobSchedulingServiceServant.scheduleLeaderboardWeeklyComputation(properties.getPropertyWithDefault("LeaderboardWeeklyUpdateCronExpression", "0 0 4 ? * 7"));
      this.jobSchedulingServiceServant.schedulePaintWarsWeeklyRaffles(properties.getPropertyWithDefault("PaintWarsWeeklyRafflesUpdateCronExpression", "0 0 4 ? * 7"));
      this.jobSchedulingServiceServant.scheduleLeaderboardDailyComputation(properties.getPropertyWithDefault("LeaderboardDailyUpdateCronExpression", "0 0 4 * * ?"));
      this.jobSchedulingServiceServant.scheduleMerchantTrail(properties.getPropertyWithDefault("MerchantTrailCronExpression", "0 0 2 ? * *"));
      this.jobSchedulingServiceServant.scheduleMerchantTagExpiry(properties.getPropertyWithDefault("MerchantTagExpiry.cronExpression", "0 0 2 ? * *"), properties.getPropertyAsIntWithDefault("MerchantTagExpiry.limit", 1000), properties.getPropertyAsIntWithDefault("MerchantTagExpiry.throttle", 10));
      this.jobSchedulingServiceServant.scheduleAvatarCandidatePooling(properties.getPropertyWithDefault("AvatarCandidatePoolingCronExpression", "0 0 2 * * ?"));
      this.jobSchedulingServiceServant.scheduleMigboRSSFeedGeneration(properties.getPropertyWithDefault("MigboRSSFeedGenerationCronExpression", "0 0 * * * ?"));
      this.jobSchedulingServiceServant.scheduleMigboEmailDailyDigestSending(properties.getPropertyWithDefault("MigboDailyDigestEmailCronExpression", "0 0 8 * * ?"));
      this.jobSchedulingServiceServant.scheduleMigboEmailDailyDigestSendingToUsersBeingFollowed(properties.getPropertyWithDefault("MigboDailyDigestEmailToUsersBeingFollowedCronExpression", "0 0 8 * * ?"));
      this.jobSchedulingServiceServant.scheduleVasComputation(properties.getPropertyWithDefault("VasUpdateCronExpression", "0 0 5 * * ?"));
      interval = properties.getPropertyAsIntWithDefault("TopMerchantTagging", 900000);
      if (interval > 0) {
         this.jobSchedulingServiceServant.scheduleTopMerchantTagging(System.currentTimeMillis(), interval);
      }

      interval = properties.getPropertyAsIntWithDefault("MOLAsynchronousUpdateInterval", 300000);
      if (interval > 0) {
         this.jobSchedulingServiceServant.scheduleMOLAsynchronousUpdate(System.currentTimeMillis(), interval);
      }

      interval = properties.getPropertyAsIntWithDefault("MIMOPayAsynchronousUpdateInterval", 300000);
      if (interval > 0) {
         this.jobSchedulingServiceServant.scheduleMIMOPayAsynchronousUpdate(System.currentTimeMillis(), interval);
      }

      interval = properties.getPropertyAsIntWithDefault("PaypalAsynchronousUpdateInterval", 300000);
      if (interval > 0) {
         this.jobSchedulingServiceServant.schedulePaypalAsynchronousUpdate(System.currentTimeMillis(), interval);
      }

      interval = properties.getPropertyAsIntWithDefault("RecommendationGenerationServiceInterval", 300000);
      if (interval > 0) {
         this.jobSchedulingServiceServant.scheduleRecommendationGeneration(System.currentTimeMillis(), interval);
      }

   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         this.configureServant(properties);
         log.info("Configured endpoint [" + properties.getProperty("JobSchedulingServiceAdapter.Endpoints") + "]");

         try {
            this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
         } catch (UnknownHostException var9) {
            this.hostName = "UNKNOWN";
         }

         log.debug("Initialising JobSchedulingService interface");
         JobSchedulingServiceAdapter = communicator().createObjectAdapter("JobSchedulingServiceAdapter");
         JobSchedulingServiceAdapter.add(this.jobSchedulingServiceServant, Util.stringToIdentity("JobSchedulingService"));
         IceStats.getInstance().setIceObjects(communicator(), JobSchedulingServiceAdapter, (ConfigurableExecutor)null);
         log.debug("Initialising JobSchedulingServiceAdmin interface");
         ObjectAdapter jobSchedulingServiceAdminAdapter = communicator().createObjectAdapter("JobSchedulingServiceAdminAdapter");
         JobSchedulingServiceAdminI jobSchedulingServiceAdmin = new JobSchedulingServiceAdminI(this.jobSchedulingServiceServant);
         jobSchedulingServiceAdminAdapter.add(jobSchedulingServiceAdmin, Util.stringToIdentity("JobSchedulingServiceAdmin"));
         jobSchedulingServiceAdminAdapter.activate();
         JobSchedulingServiceAdapter.activate();
         log.info("Service started");
         communicator().waitForShutdown();
      } catch (Exception var10) {
         log.error("failed to configure servant", var10);
      } finally {
         if (interrupted()) {
            log.fatal("JobSchedulingService " + this.hostName + ": terminating");
            this.jobSchedulingServiceServant.shutdown();
         }

      }

      return 0;
   }
}
