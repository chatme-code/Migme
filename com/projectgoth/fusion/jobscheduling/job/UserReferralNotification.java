package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.UserReferralData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.jobscheduling.JobSchedulingService;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class UserReferralNotification implements Job {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserReferralNotification.class));
   private static final Semaphore semaphore = new Semaphore(1);
   private static int fromActivationID = 0;

   public void execute(JobExecutionContext context) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (semaphoreAquired) {
            log.info("Triggering UserReferralNotification. From activation ID = " + fromActivationID);
            this.processUserReferralNotifications();
            return;
         }

         log.warn("Another job is still processing user referrals. Exiting...");
      } catch (Exception var8) {
         throw new JobExecutionException(var8);
      } finally {
         if (semaphoreAquired) {
            semaphore.release();
         }

      }

   }

   private void processUserReferralNotifications() throws CreateException, RemoteException {
      int processed = 0;
      int credited = 0;
      int failedCounter = 0;
      Map<UserReferralData, String> failedReferrals = new HashMap();
      Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
      User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      List<UserReferralData> unpaidUserReferrals = accountEJB.getUnpaidUserReferrals(fromActivationID);
      Collections.sort(unpaidUserReferrals, new UserReferralNotification.ActivationIDComparator());
      if (unpaidUserReferrals.size() > 0) {
         log.info("Processing " + unpaidUserReferrals.size() + " unpaid user referrals from activation ID " + ((UserReferralData)unpaidUserReferrals.get(0)).activationID + " to " + ((UserReferralData)unpaidUserReferrals.get(unpaidUserReferrals.size() - 1)).activationID);
      }

      Iterator i$ = unpaidUserReferrals.iterator();

      while(i$.hasNext()) {
         UserReferralData userReferralData = (UserReferralData)i$.next();

         try {
            AccountEntryData accountEntryData = null;
            if (!userReferralData.username.equalsIgnoreCase(userReferralData.referredUsername)) {
               accountEntryData = accountEJB.creditReferrer(userReferralData, new AccountEntrySourceData(JobSchedulingService.class));
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.RESET_REFERRAL_SUCCESS_RATIO_ON_CREDIT_AWARD)) {
                  try {
                     userEJB.getUserReferralSuccessRate(userReferralData.username, true);
                  } catch (Exception var12) {
                     MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.USER_REFERRAL_SUCCESS_RATE, userReferralData.username);
                     log.warn("Unable to reset user referral success rate [" + userReferralData.username + "] : " + var12.getMessage(), var12);
                  }
               }
            } else {
               log.warn("Self referral: username[" + userReferralData.username + "], referrer name[" + userReferralData.referrerName + "]... skipping credit");
            }

            if (accountEntryData != null) {
               ++credited;
            }

            if (failedReferrals.size() == 0) {
               fromActivationID = userReferralData.activationID + 1;
            }

            ++processed;
            failedCounter = 0;
         } catch (Exception var14) {
            ++failedCounter;
            log.error("Unable to process user referral ID " + userReferralData.id + ", Referrer [" + userReferralData.username + "]. Reason: " + var14.getLocalizedMessage());
            if (failedReferrals.size() == 0) {
               fromActivationID = userReferralData.activationID;
            }

            failedReferrals.put(userReferralData, var14.getLocalizedMessage());
            if (failedCounter == 3) {
               break;
            }
         }
      }

      log.info("Successfully processed " + processed + " user referral notification(s), " + credited + " credited, " + failedReferrals.size() + " failed");
      if (failedReferrals.size() > 0) {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            StringBuffer content = new StringBuffer();
            content.append("Processing the following user referrals failed: ");
            content.append(System.getProperty("line.separator"));
            content.append(System.getProperty("line.separator"));
            Iterator i$ = failedReferrals.keySet().iterator();

            while(i$.hasNext()) {
               UserReferralData userReferralData = (UserReferralData)i$.next();
               content.append("Referral ID: [").append(userReferralData.id).append("] Referrer: [").append(userReferralData.username).append("] Referred username: [").append(userReferralData.referredUsername).append("] Caused by: [").append((String)failedReferrals.get(userReferralData)).append("]").append(System.getProperty("line.separator"));
            }

            messageEJB.sendEmailFromNoReply(SystemProperty.get("TechOpsEmail", "techops@mig33global.com"), "Failed user referrals", content.toString());
            messageEJB.sendEmailFromNoReply(SystemProperty.get("KLSupportTeamEmail", "KLSupportTeam@mig33global.com"), "Failed user referrals", content.toString());
         } catch (CreateException var13) {
            log.error("Unable to load MessageBean to send email message... Reason: " + var13.getLocalizedMessage());
         }
      }

   }

   private static class ActivationIDComparator implements Comparator<UserReferralData> {
      private ActivationIDComparator() {
      }

      public int compare(UserReferralData o1, UserReferralData o2) {
         return o1.activationID.compareTo(o2.activationID);
      }

      // $FF: synthetic method
      ActivationIDComparator(Object x0) {
         this();
      }
   }
}
