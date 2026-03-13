package com.projectgoth.fusion.jobscheduling.job;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.ServiceData;
import com.projectgoth.fusion.data.SubscriptionData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.jobscheduling.JobSchedulingService;
import com.projectgoth.fusion.voiceengine.VoiceEngine;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
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

public class SubscriptionRenewal implements Job {
   private static final int CHUNK_SIZE = 100;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SubscriptionRenewal.class));
   private static final Semaphore semaphore = new Semaphore(1);
   private Map<Integer, ServiceData> services = new HashMap();

   public void execute(JobExecutionContext context) throws JobExecutionException {
      boolean semaphoreAquired = false;

      try {
         semaphoreAquired = semaphore.tryAcquire();
         if (!semaphoreAquired) {
            log.warn("Another job is still processing subscriptions. Exiting...");
            return;
         }

         log.info("Triggering SubscriptionRenewal");
         this.processExpiringSubscriptions();
         this.processExpiredSubscriptions();
         this.processPendingSubscriptions();
      } catch (Exception var8) {
         throw new JobExecutionException(var8);
      } finally {
         if (semaphoreAquired) {
            semaphore.release();
         }

      }

   }

   private ServiceData getService(Account accountEJB, int serviceID) throws RemoteException {
      ServiceData serviceData = (ServiceData)this.services.get(serviceID);
      if (serviceData == null) {
         serviceData = accountEJB.getService(serviceID);
         if (serviceData != null) {
            this.services.put(serviceData.id, serviceData);
         }
      }

      return serviceData;
   }

   private void processExpiringSubscriptions() throws CreateException, RemoteException {
      int fromID = 0;
      int processed = 0;
      int notified = 0;
      int failed = 0;
      Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);

      for(List subscriptions = accountEJB.getExpiringSubscriptions(0, 100); subscriptions.size() > 0; subscriptions = accountEJB.getExpiringSubscriptions(fromID, 100)) {
         SubscriptionData subscription;
         for(Iterator i$ = subscriptions.iterator(); i$.hasNext(); fromID = subscription.id + 1) {
            subscription = (SubscriptionData)i$.next();

            try {
               if (!Boolean.TRUE.equals(subscription.expiryReminderSent)) {
                  ServiceData serviceData = this.getService(accountEJB, subscription.serviceID);
                  if (serviceData == null) {
                     throw new Exception("Subscription " + subscription.id + " has unknown service ID " + subscription.serviceID);
                  }

                  if (serviceData.expiryReminderSMS != null) {
                     accountEJB.sendSubscriptionExpiryReminderSMS(subscription.username, subscription.id, serviceData.expiryReminderSMS, new AccountEntrySourceData(JobSchedulingService.class));
                     ++notified;
                  }
               }
            } catch (Exception var10) {
               log.warn("Unable to process expired subscription " + subscription.id, var10);
               ++failed;
            }
         }

         processed += subscriptions.size();
      }

      log.info("Successfully processed " + processed + " expiring subscription(s), " + notified + " notification(s) sent, " + failed + " failed");
   }

   private void processExpiredSubscriptions() throws CreateException, RemoteException, UnknownHostException {
      int fromID = 0;
      int processed = 0;
      int failed = 0;
      AccountEntrySourceData accountEntrySource = new AccountEntrySourceData(VoiceEngine.class);
      Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);

      SubscriptionData subscription;
      for(List subscriptions = accountEJB.getExpiredSubscriptions(0, 100); subscriptions.size() > 0; subscriptions = accountEJB.getExpiredSubscriptions(fromID, 100)) {
         for(Iterator i$ = subscriptions.iterator(); i$.hasNext(); fromID = subscription.id + 1) {
            subscription = (SubscriptionData)i$.next();

            try {
               accountEJB.expireSubscription(subscription.username, subscription.id);
               accountEJB.subscribeService(subscription.username, subscription.serviceID, accountEntrySource);
               ++processed;
            } catch (RemoteException var10) {
               log.warn("Unable to process expired subscription " + subscription.id, var10);
               ++failed;
            }
         }
      }

      log.info("Successfully processed " + processed + " expired subscription(s), " + failed + " failed");
   }

   private void processPendingSubscriptions() throws CreateException, RemoteException {
   }
}
