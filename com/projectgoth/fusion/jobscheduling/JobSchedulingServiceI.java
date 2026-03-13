package com.projectgoth.fusion.jobscheduling;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.dao.FashionShowDAO;
import com.projectgoth.fusion.dao.GroupDAO;
import com.projectgoth.fusion.dao.GroupEventDAO;
import com.projectgoth.fusion.dao.LeaderboardDAO;
import com.projectgoth.fusion.dao.VasDAO;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupEventData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.jobscheduling.domain.EmailNote;
import com.projectgoth.fusion.jobscheduling.domain.SMSNote;
import com.projectgoth.fusion.jobscheduling.job.AvatarCandidatePooling;
import com.projectgoth.fusion.jobscheduling.job.DailyGatherAndProcess;
import com.projectgoth.fusion.jobscheduling.job.GenerateMigboRSSFeeds;
import com.projectgoth.fusion.jobscheduling.job.GroupNotificationViaAlert;
import com.projectgoth.fusion.jobscheduling.job.GroupNotificationViaEmail;
import com.projectgoth.fusion.jobscheduling.job.GroupNotificationViaSMS;
import com.projectgoth.fusion.jobscheduling.job.LeaderboardComputation;
import com.projectgoth.fusion.jobscheduling.job.LeaderboardDailyComputation;
import com.projectgoth.fusion.jobscheduling.job.LeaderboardWeeklyComputation;
import com.projectgoth.fusion.jobscheduling.job.MerchantTag;
import com.projectgoth.fusion.jobscheduling.job.MerchantTagExpiry;
import com.projectgoth.fusion.jobscheduling.job.MerchantTrail;
import com.projectgoth.fusion.jobscheduling.job.PaintWarsWeeklyRaffles;
import com.projectgoth.fusion.jobscheduling.job.RecommendationGenerationServiceJob;
import com.projectgoth.fusion.jobscheduling.job.SendMigboDailyDigestEmail;
import com.projectgoth.fusion.jobscheduling.job.SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope;
import com.projectgoth.fusion.jobscheduling.job.SubscriptionRenewal;
import com.projectgoth.fusion.jobscheduling.job.UserReferralNotification;
import com.projectgoth.fusion.jobscheduling.job.VasComputation;
import com.projectgoth.fusion.payment.mimopay.MIMOPayAsynchronousUpdate;
import com.projectgoth.fusion.payment.mol.MOLAsynchronousUpdate;
import com.projectgoth.fusion.payment.paypal.PaypalAsynchronousUpdate;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupEvent;
import com.projectgoth.fusion.slice.ReputationServicePrx;
import com.projectgoth.fusion.slice.SMSUserNotification;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice._JobSchedulingServiceDisp;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.InitializingBean;

public class JobSchedulingServiceI extends _JobSchedulingServiceDisp implements InitializingBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(JobSchedulingServiceI.class));
   private Scheduler scheduler;
   private IcePrxFinder icePrxFinder;
   private UserNotificationServicePrx userNotificationServiceProxy;
   private ReputationServicePrx reputationServiceProxy;
   private GroupEventDAO groupEventDAO;
   private GroupDAO groupDAO;
   private LeaderboardDAO leaderboardDAO;
   private VasDAO vasDAO;
   private FashionShowDAO fashionShowDAO;

   public void afterPropertiesSet() throws Exception {
   }

   void initializeScheduler() throws Exception {
      this.scheduler = StdSchedulerFactory.getDefaultScheduler();
      log.info("storing icePrxFinder in scheduler context [" + this.icePrxFinder + "]");
      this.scheduler.getContext().put("icePrxFinder", this.icePrxFinder);
      log.info("storing UNS proxy in scheduler context [" + this.userNotificationServiceProxy + "]");
      this.scheduler.getContext().put("userNotificationServiceProxy", this.userNotificationServiceProxy);
      log.info("storing reputation proxy in scheduler context [" + this.reputationServiceProxy + "]");
      this.scheduler.getContext().put("reputationServiceProxy", this.reputationServiceProxy);

      try {
         JobDetail jobDetail;
         if (this.scheduler.getJobDetail(this.getFusionJobName(LeaderboardDailyComputation.class), JobGroup.LEADERBOARD_DAILY_COMPUTATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(LeaderboardDailyComputation.class), JobGroup.LEADERBOARD_DAILY_COMPUTATION.toString(), LeaderboardDailyComputation.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(LeaderboardWeeklyComputation.class), JobGroup.LEADERBOARD_WEEKLY_COMPUTATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(LeaderboardWeeklyComputation.class), JobGroup.LEADERBOARD_WEEKLY_COMPUTATION.toString(), LeaderboardWeeklyComputation.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
            log.info("Leaderboard Weekly Computation job added.");
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(MerchantTagExpiry.class), JobGroup.MERCHANT_TAG_EXPIRY.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(MerchantTagExpiry.class), JobGroup.MERCHANT_TAG_EXPIRY.toString(), MerchantTagExpiry.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(MerchantTrail.class), JobGroup.MERCHANT_TRAIL.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(MerchantTrail.class), JobGroup.MERCHANT_TRAIL.toString(), MerchantTrail.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(UserReferralNotification.class.getName(), JobGroup.REFERRAL_MANAGEMENT.toString()) == null) {
            jobDetail = new JobDetail(UserReferralNotification.class.getName(), JobGroup.REFERRAL_MANAGEMENT.toString(), UserReferralNotification.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(SubscriptionRenewal.class.getName(), JobGroup.SUBSCRIPTION_MANAGEMENT.toString()) == null) {
            jobDetail = new JobDetail(SubscriptionRenewal.class.getName(), JobGroup.SUBSCRIPTION_MANAGEMENT.toString(), SubscriptionRenewal.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(GroupNotificationViaEmail.class), JobGroup.GROUP_NOTIFICATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(GroupNotificationViaEmail.class), JobGroup.GROUP_NOTIFICATION.toString(), GroupNotificationViaEmail.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(GroupNotificationViaSMS.class), JobGroup.GROUP_NOTIFICATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(GroupNotificationViaSMS.class), JobGroup.GROUP_NOTIFICATION.toString(), GroupNotificationViaSMS.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(GroupNotificationViaAlert.class), JobGroup.GROUP_NOTIFICATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(GroupNotificationViaAlert.class), JobGroup.GROUP_NOTIFICATION.toString(), GroupNotificationViaAlert.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(DailyGatherAndProcess.class), JobGroup.REPUTATION_SERVICE.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(DailyGatherAndProcess.class), JobGroup.REPUTATION_SERVICE.toString(), DailyGatherAndProcess.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(LeaderboardComputation.class), JobGroup.LEADERBOARD_COMPUTATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(LeaderboardComputation.class), JobGroup.LEADERBOARD_COMPUTATION.toString(), LeaderboardComputation.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(VasComputation.class), JobGroup.VAS_COMPUTATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(VasComputation.class), JobGroup.VAS_COMPUTATION.toString(), VasComputation.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(AvatarCandidatePooling.class), JobGroup.AVATAR_CANDIDATE_POOLING.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(AvatarCandidatePooling.class), JobGroup.AVATAR_CANDIDATE_POOLING.toString(), AvatarCandidatePooling.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(MerchantTag.class.getName(), JobGroup.TOPMERCHANT_TAGGING.toString()) == null) {
            jobDetail = new JobDetail(MerchantTag.class.getName(), JobGroup.TOPMERCHANT_TAGGING.toString(), MerchantTag.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(PaintWarsWeeklyRaffles.class.getName(), JobGroup.PAINT_WARS_WEEKLY_RAFFLES.toString()) == null) {
            jobDetail = new JobDetail(PaintWarsWeeklyRaffles.class.getName(), JobGroup.PAINT_WARS_WEEKLY_RAFFLES.toString(), PaintWarsWeeklyRaffles.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(GenerateMigboRSSFeeds.class.getName(), JobGroup.MIGBO_RSSFEED_GENERATION.toString()) == null) {
            jobDetail = new JobDetail(GenerateMigboRSSFeeds.class.getName(), JobGroup.MIGBO_RSSFEED_GENERATION.toString(), GenerateMigboRSSFeeds.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(SendMigboDailyDigestEmail.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST.toString()) == null) {
            jobDetail = new JobDetail(SendMigboDailyDigestEmail.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST.toString(), SendMigboDailyDigestEmail.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST_TO_USERS_BEING_FOLLOWED.toString()) == null) {
            jobDetail = new JobDetail(SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST_TO_USERS_BEING_FOLLOWED.toString(), SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(MOLAsynchronousUpdate.class), JobGroup.MOL_ASYNCHRONOUS_UPDATE.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(MOLAsynchronousUpdate.class), JobGroup.MOL_ASYNCHRONOUS_UPDATE.toString(), MOLAsynchronousUpdate.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(MIMOPayAsynchronousUpdate.class), JobGroup.MIMOPAY_ASYNCHRONOUS_UPDATE.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(MIMOPayAsynchronousUpdate.class), JobGroup.MIMOPAY_ASYNCHRONOUS_UPDATE.toString(), MIMOPayAsynchronousUpdate.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(PaypalAsynchronousUpdate.class), JobGroup.PAYPAL_ASYNCHRONOUS_UPDATE.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(PaypalAsynchronousUpdate.class), JobGroup.PAYPAL_ASYNCHRONOUS_UPDATE.toString(), MIMOPayAsynchronousUpdate.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }

         if (this.scheduler.getJobDetail(this.getFusionJobName(RecommendationGenerationServiceJob.class), JobGroup.RECOMMENDATION_GENERATION.toString()) == null) {
            jobDetail = new JobDetail(this.getFusionJobName(RecommendationGenerationServiceJob.class), JobGroup.RECOMMENDATION_GENERATION.toString(), RecommendationGenerationServiceJob.class);
            jobDetail.setDurability(true);
            this.scheduler.addJob(jobDetail, false);
         }
      } catch (ObjectAlreadyExistsException var2) {
         log.warn("ignoring already scheduled initial jobs", var2);
      } catch (SchedulerException var3) {
         log.error("failed to schedule initial jobs", var3);
      }

      this.scheduler.start();
   }

   void shutdown() {
      try {
         this.scheduler.shutdown();
      } catch (SchedulerException var2) {
         log.error("failed to shut down scheduler", var2);
      }

   }

   private String getFusionGroupEventNotificationTriggerName(int eventId) {
      return "event-" + eventId;
   }

   private String getFusionJobName(Class<? extends Job> jobClass) {
      return jobClass.getName();
   }

   public String[] getJobsRunningNow() {
      try {
         List<JobExecutionContext> jobsRunning = this.scheduler.getCurrentlyExecutingJobs();
         String[] result = new String[jobsRunning.size()];
         int index = 0;

         JobExecutionContext context;
         for(Iterator i$ = jobsRunning.iterator(); i$.hasNext(); result[index++] = context.getJobDetail().getFullName() + ", fire time " + context.getFireTime() + ", next fire time " + context.getNextFireTime()) {
            context = (JobExecutionContext)i$.next();
         }

         return result;
      } catch (SchedulerException var6) {
         return new String[0];
      }
   }

   void scheduleDailyGatherAndProcessJob(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(DailyGatherAndProcess.class.getName(), JobGroup.REPUTATION_SERVICE.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(DailyGatherAndProcess.class));
      cronTrigger.setJobGroup(JobGroup.REPUTATION_SERVICE.toString());
      cronTrigger.setVolatility(true);
      Date firstFire;
      if (this.scheduler.getTrigger(DailyGatherAndProcess.class.getName(), JobGroup.REPUTATION_SERVICE.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(DailyGatherAndProcess.class.getName(), JobGroup.REPUTATION_SERVICE.toString(), cronTrigger);
      }

      log.info("scheduled " + DailyGatherAndProcess.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   String scheduleSubscriptionRenewal(long startTime, int repeatInterval) throws FusionException {
      log.info("scheduling subscription renewal for time [" + new Date(startTime) + "] and repeat interval [" + repeatInterval + "]");
      String triggerName = "subscription renewal";
      String triggerGroupname = TriggerGroup.SUBSCRIPTION_RENEWAL.toString();
      SimpleTrigger trigger = new SimpleTrigger(triggerName, triggerGroupname, new Date(startTime));
      trigger.setJobName(SubscriptionRenewal.class.getName());
      trigger.setJobGroup(JobGroup.SUBSCRIPTION_MANAGEMENT.toString());
      trigger.setRepeatCount(-1);
      trigger.setRepeatInterval((long)repeatInterval);

      try {
         if (this.scheduler.getTrigger(triggerName, triggerGroupname) == null) {
            this.scheduler.scheduleJob(trigger);
         } else {
            this.scheduler.rescheduleJob(triggerName, triggerGroupname, trigger);
         }

         return triggerName;
      } catch (SchedulerException var8) {
         log.error("failed to schedule trigger [" + triggerName + "] for time [" + new Date(startTime) + "]", var8);
         throw new FusionException("failed to schedule to subscription renewal");
      }
   }

   String scheduleUserReferralNotification(long startTime, int repeatInterval) throws FusionException {
      log.info("scheduling user referral notification for time [" + new Date(startTime) + "] and repeat interval [" + repeatInterval + "]");
      String triggerName = "user referral notification";
      String triggerGroupname = TriggerGroup.USER_REFERRAL_NOTIFICATION.toString();
      SimpleTrigger trigger = new SimpleTrigger(triggerName, triggerGroupname, new Date(startTime));
      trigger.setJobName(UserReferralNotification.class.getName());
      trigger.setJobGroup(JobGroup.REFERRAL_MANAGEMENT.toString());
      trigger.setRepeatCount(-1);
      trigger.setRepeatInterval((long)repeatInterval);

      try {
         if (this.scheduler.getTrigger(triggerName, triggerGroupname) == null) {
            this.scheduler.scheduleJob(trigger);
         } else {
            this.scheduler.rescheduleJob(triggerName, triggerGroupname, trigger);
         }

         return triggerName;
      } catch (SchedulerException var8) {
         log.error("failed to schedule trigger [" + triggerName + "] for time [" + new Date(startTime) + "]", var8);
         throw new FusionException("failed to schedule to user referral notification");
      }
   }

   String scheduleTopMerchantTagging(long startTime, int repeatInterval) throws FusionException {
      log.info("scheduling top merchant tagging process for time [" + new Date(startTime) + "] and repeat interval [" + repeatInterval + "]");
      String triggerName = "top merchant tagging";
      String triggerGroupname = TriggerGroup.TOPMERCHANT_TAGGING.toString();
      SimpleTrigger trigger = new SimpleTrigger(triggerName, triggerGroupname, new Date(startTime));
      trigger.setJobName(MerchantTag.class.getName());
      trigger.setJobGroup(JobGroup.TOPMERCHANT_TAGGING.toString());
      trigger.setRepeatCount(-1);
      trigger.setRepeatInterval((long)repeatInterval);

      try {
         if (this.scheduler.getTrigger(triggerName, triggerGroupname) == null) {
            this.scheduler.scheduleJob(trigger);
         } else {
            this.scheduler.rescheduleJob(triggerName, triggerGroupname, trigger);
         }

         return triggerName;
      } catch (SchedulerException var8) {
         log.error("failed to schedule trigger [" + triggerName + "] for time [" + new Date(startTime) + "]", var8);
         throw new FusionException("failed to schedule to top merchant tagging process");
      }
   }

   String scheduleMOLAsynchronousUpdate(long startTime, int repeatInterval) throws FusionException {
      log.info("scheduling MOL asynchronous update process for time [" + new Date(startTime) + "] and repeat interval [" + repeatInterval + "]");
      String triggerName = "mol asynchronous payment status update";
      String triggerGroupname = TriggerGroup.MOL_ASYNCHRONOUS_UPDATE.toString();
      SimpleTrigger trigger = new SimpleTrigger(triggerName, triggerGroupname, new Date(startTime));
      trigger.setJobName(this.getFusionJobName(MOLAsynchronousUpdate.class));
      trigger.setJobGroup(JobGroup.MOL_ASYNCHRONOUS_UPDATE.toString());
      trigger.setRepeatCount(-1);
      trigger.setRepeatInterval((long)repeatInterval);

      try {
         if (this.scheduler.getTrigger(triggerName, triggerGroupname) == null) {
            this.scheduler.scheduleJob(trigger);
         } else {
            this.scheduler.rescheduleJob(triggerName, triggerGroupname, trigger);
         }

         return triggerName;
      } catch (SchedulerException var8) {
         log.error("failed to schedule trigger [" + triggerName + "] for time [" + new Date(startTime) + "]", var8);
         throw new FusionException("failed to schedule MOL Asynchronous Update process");
      }
   }

   String scheduleMIMOPayAsynchronousUpdate(long startTime, int repeatInterval) throws FusionException {
      log.info("scheduling MIMOPay asynchronous update process for time [" + new Date(startTime) + "] and repeat interval [" + repeatInterval + "]");
      String triggerName = "MIMOPay asynchronous payment status update";
      String triggerGroupname = TriggerGroup.MIMOPAY_ASYNCHRONOUS_UPDATE.toString();
      SimpleTrigger trigger = new SimpleTrigger(triggerName, triggerGroupname, new Date(startTime));
      trigger.setJobName(this.getFusionJobName(MIMOPayAsynchronousUpdate.class));
      trigger.setJobGroup(JobGroup.MIMOPAY_ASYNCHRONOUS_UPDATE.toString());
      trigger.setRepeatCount(-1);
      trigger.setRepeatInterval((long)repeatInterval);

      try {
         if (this.scheduler.getTrigger(triggerName, triggerGroupname) == null) {
            this.scheduler.scheduleJob(trigger);
         } else {
            this.scheduler.rescheduleJob(triggerName, triggerGroupname, trigger);
         }

         return triggerName;
      } catch (SchedulerException var8) {
         log.error("failed to schedule trigger [" + triggerName + "] for time [" + new Date(startTime) + "]", var8);
         throw new FusionException("failed to schedule MIMOPay Asynchronous Update process");
      }
   }

   String schedulePaypalAsynchronousUpdate(long startTime, int repeatInterval) throws FusionException {
      log.info("scheduling Paypal asynchronous update process for time [" + new Date(startTime) + "] and repeat interval [" + repeatInterval + "]");
      String triggerName = "Paypal asynchronous payment status update";
      String triggerGroupname = TriggerGroup.PAYPAL_ASYNCHRONOUS_UPDATE.toString();
      SimpleTrigger trigger = new SimpleTrigger(triggerName, triggerGroupname, new Date(startTime));
      trigger.setJobName(this.getFusionJobName(PaypalAsynchronousUpdate.class));
      trigger.setJobGroup(JobGroup.PAYPAL_ASYNCHRONOUS_UPDATE.toString());
      trigger.setRepeatCount(-1);
      trigger.setRepeatInterval((long)repeatInterval);

      try {
         if (this.scheduler.getTrigger(triggerName, triggerGroupname) == null) {
            this.scheduler.scheduleJob(trigger);
         } else {
            this.scheduler.rescheduleJob(triggerName, triggerGroupname, trigger);
         }

         return triggerName;
      } catch (SchedulerException var8) {
         log.error("failed to schedule trigger [" + triggerName + "] for time [" + new Date(startTime) + "]", var8);
         throw new FusionException("failed to schedule PAYPAL Asynchronous Update process");
      }
   }

   String scheduleRecommendationGeneration(long startTime, int repeatInterval) throws FusionException {
      log.info("scheduling RecommendationGenerationService run for time [" + new Date(startTime) + "] and repeat interval [" + repeatInterval + "]");
      String triggerName = "RecommendationGenerationService";
      String triggerGroupname = TriggerGroup.RECOMMENDATION_GENERATION.toString();
      SimpleTrigger trigger = new SimpleTrigger(triggerName, triggerGroupname, new Date(startTime));
      trigger.setJobName(this.getFusionJobName(RecommendationGenerationServiceJob.class));
      trigger.setJobGroup(JobGroup.RECOMMENDATION_GENERATION.toString());
      trigger.setRepeatCount(-1);
      trigger.setRepeatInterval((long)repeatInterval);

      try {
         if (this.scheduler.getTrigger(triggerName, triggerGroupname) == null) {
            this.scheduler.scheduleJob(trigger);
         } else {
            this.scheduler.rescheduleJob(triggerName, triggerGroupname, trigger);
         }

         return triggerName;
      } catch (SchedulerException var8) {
         log.error("failed to schedule trigger [" + triggerName + "] for time [" + new Date(startTime) + "]", var8);
         throw new FusionException("failed to schedule RecommendationGenerationService process");
      }
   }

   void scheduleVasComputation(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(VasComputation.class.getName(), JobGroup.VAS_COMPUTATION.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(VasComputation.class));
      cronTrigger.setJobGroup(JobGroup.VAS_COMPUTATION.toString());
      cronTrigger.setVolatility(true);
      this.scheduler.getContext().put("vasDAO", this.vasDAO);
      Date firstFire;
      if (this.scheduler.getTrigger(VasComputation.class.getName(), JobGroup.VAS_COMPUTATION.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(VasComputation.class.getName(), JobGroup.VAS_COMPUTATION.toString(), cronTrigger);
      }

      log.info("scheduled " + VasComputation.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleLeaderboardComputation(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(LeaderboardComputation.class.getName(), JobGroup.LEADERBOARD_COMPUTATION.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(LeaderboardComputation.class));
      cronTrigger.setJobGroup(JobGroup.LEADERBOARD_COMPUTATION.toString());
      cronTrigger.setVolatility(true);
      this.scheduler.getContext().put("leaderboardDAO", this.leaderboardDAO);
      Date firstFire;
      if (this.scheduler.getTrigger(LeaderboardComputation.class.getName(), JobGroup.LEADERBOARD_COMPUTATION.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(LeaderboardComputation.class.getName(), JobGroup.LEADERBOARD_COMPUTATION.toString(), cronTrigger);
      }

      log.info("scheduled " + LeaderboardComputation.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleLeaderboardDailyComputation(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(LeaderboardDailyComputation.class.getName(), JobGroup.LEADERBOARD_DAILY_COMPUTATION.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(LeaderboardDailyComputation.class));
      cronTrigger.setJobGroup(JobGroup.LEADERBOARD_DAILY_COMPUTATION.toString());
      cronTrigger.setVolatility(true);
      Date firstFire;
      if (this.scheduler.getTrigger(LeaderboardDailyComputation.class.getName(), JobGroup.LEADERBOARD_DAILY_COMPUTATION.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(LeaderboardDailyComputation.class.getName(), JobGroup.LEADERBOARD_DAILY_COMPUTATION.toString(), cronTrigger);
      }

      log.info("scheduled " + LeaderboardDailyComputation.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleLeaderboardWeeklyComputation(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(LeaderboardWeeklyComputation.class.getName(), JobGroup.LEADERBOARD_WEEKLY_COMPUTATION.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(LeaderboardWeeklyComputation.class));
      cronTrigger.setJobGroup(JobGroup.LEADERBOARD_WEEKLY_COMPUTATION.toString());
      cronTrigger.setVolatility(true);
      log.info("Start scheduling " + LeaderboardWeeklyComputation.class.getName() + " with cron expression [" + cronExpression + "]");
      Date firstFire;
      if (this.scheduler.getTrigger(LeaderboardWeeklyComputation.class.getName(), JobGroup.LEADERBOARD_WEEKLY_COMPUTATION.toString()) == null) {
         log.info("Schedule Job");
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         log.info("Reschedule Job");
         firstFire = this.scheduler.rescheduleJob(LeaderboardWeeklyComputation.class.getName(), JobGroup.LEADERBOARD_WEEKLY_COMPUTATION.toString(), cronTrigger);
      }

      log.info("scheduled " + LeaderboardWeeklyComputation.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleMigboRSSFeedGeneration(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(GenerateMigboRSSFeeds.class.getName(), JobGroup.MIGBO_RSSFEED_GENERATION.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(GenerateMigboRSSFeeds.class));
      cronTrigger.setJobGroup(JobGroup.MIGBO_RSSFEED_GENERATION.toString());
      cronTrigger.setVolatility(true);
      log.info("Start scheduling " + GenerateMigboRSSFeeds.class.getName() + " with cron expression [" + cronExpression + "]");
      Date firstFire;
      if (this.scheduler.getTrigger(GenerateMigboRSSFeeds.class.getName(), JobGroup.MIGBO_RSSFEED_GENERATION.toString()) == null) {
         log.info("Scheduling MigboRSSFeedGeneration Job for First Time");
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         log.info("Rescheduling MigboRSSFeedGeneration Job");
         firstFire = this.scheduler.rescheduleJob(GenerateMigboRSSFeeds.class.getName(), JobGroup.MIGBO_RSSFEED_GENERATION.toString(), cronTrigger);
      }

      log.info("Successfully scheduled " + GenerateMigboRSSFeeds.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   public void scheduleMigboEmailDailyDigestSending(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(SendMigboDailyDigestEmail.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(SendMigboDailyDigestEmail.class));
      cronTrigger.setJobGroup(JobGroup.MIGBO_EMAIL_DAILY_DIGEST.toString());
      cronTrigger.setVolatility(true);
      log.info("Start scheduling " + SendMigboDailyDigestEmail.class.getName() + " with cron expression [" + cronExpression + "]");
      Date firstFire;
      if (this.scheduler.getTrigger(SendMigboDailyDigestEmail.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST.toString()) == null) {
         log.info("Scheduling SendMigboDailyDigestEmail Job for First Time");
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         log.info("Rescheduling SendMigboDailyDigestEmail Job");
         firstFire = this.scheduler.rescheduleJob(SendMigboDailyDigestEmail.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST.toString(), cronTrigger);
      }

      log.info("Successfully scheduled " + SendMigboDailyDigestEmail.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   public void scheduleMigboEmailDailyDigestSendingToUsersBeingFollowed(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST_TO_USERS_BEING_FOLLOWED.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class));
      cronTrigger.setJobGroup(JobGroup.MIGBO_EMAIL_DAILY_DIGEST_TO_USERS_BEING_FOLLOWED.toString());
      cronTrigger.setVolatility(true);
      log.info("Start scheduling " + SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class.getName() + " with cron expression [" + cronExpression + "]");
      Date firstFire;
      if (this.scheduler.getTrigger(SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST_TO_USERS_BEING_FOLLOWED.toString()) == null) {
         log.info("Scheduling scheduleMigboEmailDailyDigestSendingToUsersBeingFollowed Job for First Time");
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         log.info("Rescheduling scheduleMigboEmailDailyDigestSendingToUsersBeingFollowed Job");
         firstFire = this.scheduler.rescheduleJob(SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class.getName(), JobGroup.MIGBO_EMAIL_DAILY_DIGEST_TO_USERS_BEING_FOLLOWED.toString(), cronTrigger);
      }

      log.info("Successfully scheduled " + SendMigboDailyDigestEmailToUsersBeingFollowedWithinATimeScope.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void schedulePaintWarsWeeklyRaffles(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(PaintWarsWeeklyRaffles.class.getName(), JobGroup.PAINT_WARS_WEEKLY_RAFFLES.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(PaintWarsWeeklyRaffles.class));
      cronTrigger.setJobGroup(JobGroup.PAINT_WARS_WEEKLY_RAFFLES.toString());
      cronTrigger.setVolatility(true);
      log.info("Start scheduling " + PaintWarsWeeklyRaffles.class.getName() + " with cron expression [" + cronExpression + "]");
      Date firstFire;
      if (this.scheduler.getTrigger(PaintWarsWeeklyRaffles.class.getName(), JobGroup.PAINT_WARS_WEEKLY_RAFFLES.toString()) == null) {
         log.info("Schedule Job");
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         log.info("Reschedule Job");
         firstFire = this.scheduler.rescheduleJob(PaintWarsWeeklyRaffles.class.getName(), JobGroup.PAINT_WARS_WEEKLY_RAFFLES.toString(), cronTrigger);
      }

      log.info("scheduled " + PaintWarsWeeklyRaffles.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleAvatarCandidatePooling(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(AvatarCandidatePooling.class.getName(), JobGroup.AVATAR_CANDIDATE_POOLING.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(AvatarCandidatePooling.class));
      cronTrigger.setJobGroup(JobGroup.AVATAR_CANDIDATE_POOLING.toString());
      cronTrigger.setVolatility(true);
      Date firstFire;
      if (this.scheduler.getTrigger(AvatarCandidatePooling.class.getName(), JobGroup.AVATAR_CANDIDATE_POOLING.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(AvatarCandidatePooling.class.getName(), JobGroup.AVATAR_CANDIDATE_POOLING.toString(), cronTrigger);
      }

      log.info("scheduled " + AvatarCandidatePooling.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleMerchantTrail(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(MerchantTrail.class.getName(), JobGroup.MERCHANT_TRAIL.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(MerchantTrail.class));
      cronTrigger.setJobGroup(JobGroup.MERCHANT_TRAIL.toString());
      cronTrigger.setVolatility(true);
      Date firstFire;
      if (this.scheduler.getTrigger(MerchantTrail.class.getName(), JobGroup.MERCHANT_TRAIL.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(MerchantTrail.class.getName(), JobGroup.MERCHANT_TRAIL.toString(), cronTrigger);
      }

      log.info("scheduled " + MerchantTrail.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleMerchantTagExpiry(String cronExpression, int limit, int throttle) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(MerchantTagExpiry.class.getName(), JobGroup.MERCHANT_TAG_EXPIRY.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(MerchantTagExpiry.class));
      cronTrigger.setJobGroup(JobGroup.MERCHANT_TAG_EXPIRY.toString());
      cronTrigger.setVolatility(true);
      cronTrigger.getJobDataMap().put("limit", limit);
      cronTrigger.getJobDataMap().put("throttle", throttle);
      Date firstFire;
      if (this.scheduler.getTrigger(MerchantTagExpiry.class.getName(), JobGroup.MERCHANT_TAG_EXPIRY.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(MerchantTagExpiry.class.getName(), JobGroup.MERCHANT_TAG_EXPIRY.toString(), cronTrigger);
      }

      log.info("scheduled " + MerchantTagExpiry.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   void scheduleRecommendationGeneration(String cronExpression) throws ParseException, SchedulerException {
      CronTrigger cronTrigger = new CronTrigger(RecommendationGenerationServiceJob.class.getName(), JobGroup.RECOMMENDATION_GENERATION.toString(), cronExpression);
      cronTrigger.setJobName(this.getFusionJobName(RecommendationGenerationServiceJob.class));
      cronTrigger.setJobGroup(JobGroup.RECOMMENDATION_GENERATION.toString());
      cronTrigger.setVolatility(true);
      Date firstFire;
      if (this.scheduler.getTrigger(RecommendationGenerationServiceJob.class.getName(), JobGroup.RECOMMENDATION_GENERATION.toString()) == null) {
         firstFire = this.scheduler.scheduleJob(cronTrigger);
      } else {
         firstFire = this.scheduler.rescheduleJob(RecommendationGenerationServiceJob.class.getName(), JobGroup.MERCHANT_TRAIL.toString(), cronTrigger);
      }

      log.info("scheduled " + RecommendationGenerationServiceJob.class.getName() + " with cron expression [" + cronExpression + "] with first fire " + firstFire);
   }

   public String scheduleFusionGroupEventNotificationViaEmail(int eventId, int groupId, long time, EmailUserNotification note, Current __current) throws FusionException {
      log.info("scheduling group notification via email for event [" + eventId + "] group [" + groupId + "] and time [" + new Date(time) + "]");
      String triggerName = this.getFusionGroupEventNotificationTriggerName(eventId);
      Trigger trigger = new SimpleTrigger(triggerName, TriggerGroup.EMAIL_GROUP_NOTIFICATION.toString(), new Date(time));
      trigger.getJobDataMap().put("note", EmailNote.fromEmailUserNotification(note));
      trigger.getJobDataMap().put("groupId", groupId);
      trigger.setJobName(this.getFusionJobName(GroupNotificationViaEmail.class));
      trigger.setJobGroup(JobGroup.GROUP_NOTIFICATION.toString());

      try {
         this.scheduler.scheduleJob(trigger);
         return triggerName;
      } catch (SchedulerException var10) {
         log.error("failed to schedule trigger [" + triggerName + "] for group [" + groupId + "] and time [" + new Date(time) + "]", var10);
         throw new FusionException("failed to schedule to notification");
      }
   }

   public String scheduleFusionGroupEventNotificationViaSMS(int eventId, int groupId, long time, SMSUserNotification note, Current __current) throws FusionException {
      log.info("scheduling group notification via SMS for event [" + eventId + "] group [" + groupId + "] and time [" + new Date(time) + "]");
      String triggerName = this.getFusionGroupEventNotificationTriggerName(eventId);
      Trigger trigger = new SimpleTrigger(triggerName, TriggerGroup.SMS_GROUP_NOTIFICATION.toString(), new Date(time));
      trigger.getJobDataMap().put("note", SMSNote.fromSMSUserNotification(note));
      trigger.getJobDataMap().put("groupId", groupId);
      trigger.setJobName(this.getFusionJobName(GroupNotificationViaSMS.class));
      trigger.setJobGroup(JobGroup.GROUP_NOTIFICATION.toString());

      try {
         this.scheduler.scheduleJob(trigger);
         return triggerName;
      } catch (SchedulerException var10) {
         log.error("failed to schedule trigger [" + triggerName + "] for group [" + groupId + "] and time [" + new Date(time) + "]", var10);
         throw new FusionException("failed to schedule to notification");
      }
   }

   public String scheduleFusionGroupEventNotificationViaAlert(int eventId, int groupId, long time, String message, Current __current) throws FusionException {
      log.info("scheduling group notification via alert for event [" + eventId + "] group [" + groupId + "] and time [" + new Date(time) + "]");
      String triggerName = this.getFusionGroupEventNotificationTriggerName(eventId);
      Trigger trigger = new SimpleTrigger(triggerName, TriggerGroup.ALERT_GROUP_NOTIFICATION.toString(), new Date(time));
      trigger.getJobDataMap().put("message", message);
      trigger.getJobDataMap().put("groupId", groupId);
      trigger.setJobName(this.getFusionJobName(GroupNotificationViaAlert.class));
      trigger.setJobGroup(JobGroup.GROUP_NOTIFICATION.toString());

      try {
         this.scheduler.scheduleJob(trigger);
         return triggerName;
      } catch (SchedulerException var10) {
         log.error("failed to schedule trigger [" + triggerName + "] for group [" + groupId + "] and time [" + new Date(time) + "]", var10);
         throw new FusionException("failed to schedule to notification");
      }
   }

   public void rescheduleFusionGroupEvent(GroupEvent event, Current __current) throws FusionException {
      try {
         log.info("REscheduling event [" + event.description + "] for group id [" + event.groupId + "] start time to [" + new Date(event.startTime) + "]");
         this.groupEventDAO.updateGroupEvent(GroupEventData.fromGroupEvent(event));
         long fiveMinutesBeforeStart = event.startTime - 300000L;
         Trigger smsTrigger = this.scheduler.getTrigger(this.getFusionGroupEventNotificationTriggerName(event.id), TriggerGroup.SMS_GROUP_NOTIFICATION.toString());
         if (smsTrigger != null) {
            smsTrigger.setStartTime(new Date(fiveMinutesBeforeStart));
            this.scheduler.rescheduleJob(this.getFusionGroupEventNotificationTriggerName(event.id), TriggerGroup.SMS_GROUP_NOTIFICATION.toString(), smsTrigger);
         } else {
            log.warn("could not find sms trigger for event id [" + event.id + "] to reschedule, ignoring and continuing");
         }

         Trigger alertTrigger = this.scheduler.getTrigger(this.getFusionGroupEventNotificationTriggerName(event.id), TriggerGroup.ALERT_GROUP_NOTIFICATION.toString());
         if (alertTrigger != null) {
            alertTrigger.setStartTime(new Date(fiveMinutesBeforeStart));
            this.scheduler.rescheduleJob(this.getFusionGroupEventNotificationTriggerName(event.id), TriggerGroup.ALERT_GROUP_NOTIFICATION.toString(), alertTrigger);
         } else {
            log.warn("could not find alert trigger for event id [" + event.id + "] to reschedule, ignoring and continuing");
         }

      } catch (SchedulerException var7) {
         log.error("failed to reschedule trigger", var7);
         throw new FusionException("failed to reschedule trigger");
      }
   }

   public int scheduleFusionGroupEvent(GroupEvent event, Current __current) throws FusionException {
      GroupData group = this.groupDAO.getGroup(event.groupId);
      log.info("scheduling event [" + event.description + "] for group [" + event.groupId + "] at [" + new Date(event.startTime) + "]");
      int newEventId = this.groupEventDAO.persistGroupEvent(GroupEventData.fromGroupEvent(event));
      event.id = newEventId;
      long fiveMinutesBeforeStart = event.startTime - 300000L;
      SMSUserNotification smsNote = new SMSUserNotification(group.name + " alert! \"" + event.description + "\" is about to start. Login to migme", (String)null, SystemSMSData.SubTypeEnum.GROUP_EVENT_NOTIFICATION.value());
      this.scheduleFusionGroupEventNotificationViaSMS(event.id, event.groupId, fiveMinutesBeforeStart, smsNote);
      this.scheduleFusionGroupEventNotificationViaAlert(event.id, event.groupId, fiveMinutesBeforeStart, group.name + " alert! \"" + event.description + "\" is about to start. Join chatroom now");
      return newEventId;
   }

   public void unscheduleFusionGroupEvent(int groupEventId, Current __current) throws FusionException {
      try {
         log.info("UNscheduling event [" + groupEventId + "]");
         if (this.groupEventDAO.updateGroupEventStatus(groupEventId, GroupEventData.StatusEnum.INACTIVE.value()) < 1) {
            log.warn("no event id [" + groupEventId + " found in DB to unschedule");
         }

         this.scheduler.unscheduleJob(this.getFusionGroupEventNotificationTriggerName(groupEventId), TriggerGroup.SMS_GROUP_NOTIFICATION.toString());
         this.scheduler.unscheduleJob(this.getFusionGroupEventNotificationTriggerName(groupEventId), TriggerGroup.ALERT_GROUP_NOTIFICATION.toString());
      } catch (SchedulerException var4) {
         log.error("failed to unschedule trigger", var4);
         throw new FusionException("failed to unschedule trigger");
      }
   }

   public void setGroupEventDAO(GroupEventDAO groupEventDAO) {
      this.groupEventDAO = groupEventDAO;
   }

   public void setGroupDAO(GroupDAO groupDAO) {
      this.groupDAO = groupDAO;
   }

   public void setLeaderboardDAO(LeaderboardDAO leaderboardDAO) {
      this.leaderboardDAO = leaderboardDAO;
   }

   public void setVasDAO(VasDAO vasDAO) {
      this.vasDAO = vasDAO;
   }

   public void setUserNotificationServiceProxy(UserNotificationServicePrx userNotificationServiceProxy) {
      this.userNotificationServiceProxy = userNotificationServiceProxy;
   }

   public void setIcePrxFinder(IcePrxFinder icePrxFinder) {
      this.icePrxFinder = icePrxFinder;
   }

   public void setReputationServicePrx(ReputationServicePrx reputationServiceProxy) {
      this.reputationServiceProxy = reputationServiceProxy;
   }

   public void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap, Current __current) throws FusionException {
      JobDataMap map = new JobDataMap();
      if (jobDataMap != null) {
         map.putAll(jobDataMap);
      }

      try {
         this.scheduler.triggerJobWithVolatileTrigger(jobName, jobGroup, map);
      } catch (SchedulerException var7) {
         throw new FusionException(var7.getLocalizedMessage());
      }
   }
}
