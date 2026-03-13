package com.projectgoth.fusion.payment;

import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class PaymentAsynchStatusUpdate implements Job {
   private final String jobName = this.getClass().getName();
   private static final int MAX_DELAY_MULTIPLIER = 15;
   private static final int SUSPEND_ON_ERROR_DELAY_MS = 2000;

   protected PaymentAsynchStatusUpdate() {
   }

   protected abstract Logger getLogger();

   protected abstract Semaphore getSemaphore();

   public String getJobName() {
      return this.jobName;
   }

   public abstract boolean isEnabled();

   protected abstract int getVendorTypeFilter();

   protected abstract Integer getRecordCountsPerIteration();

   protected abstract int getSleepMillisecPeriodAfterProcessingOneRecord();

   protected abstract int getSleepMillisecPeriodAfterProcessingOneBatch();

   protected abstract PaymentData retrieveStatusFromVendor(PaymentData var1) throws Exception;

   protected List<PaymentData> getPaymentTransactions(Account accountEJB, int vendorType, Integer recordCountsPerIteration) throws Exception {
      return accountEJB.getPendingPaymentTransactionsForRequery(vendorType, recordCountsPerIteration);
   }

   protected void updateStatus(Account accountEJB, PaymentData paymentData) throws Exception {
      Logger log = this.getLogger();
      AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(this.getClass());

      try {
         accountEJB.updatePayment(paymentData, accountEntrySourceData);
      } catch (PaymentException var7) {
         ErrorCause errorCause = var7.getErrorCause();
         if (errorCause == ErrorCause.PaymentErrorReasonType.DB_UPDATE_PAYMENT_FAILED) {
            log.warn("Unable to update paymentData.id=[" + paymentData.id + "] username = [" + paymentData.username + "].PaymentData state may have changed.");
         } else {
            log.error("Error while trying to update status for paymentData.id=" + paymentData.id + " username = " + paymentData.username, var7);
         }
      }

   }

   public void execute(JobExecutionContext context) throws JobExecutionException {
      Logger log = this.getLogger();
      if (!this.getSemaphore().tryAcquire()) {
         log.warn("Another job is processing . " + this.getJobName() + "  Exiting...");
      } else {
         log.info(this.getJobName() + " started... ");

         try {
            if (this.isEnabled()) {
               Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
               int vendorType = this.getVendorTypeFilter();
               Integer recordCountsPerIteration = this.getRecordCountsPerIteration();
               if (recordCountsPerIteration != null && recordCountsPerIteration < 1) {
                  recordCountsPerIteration = null;
               }

               if (log.isDebugEnabled()) {
                  log.debug("Retrieving pending payment transactions for " + vendorType + " max rows " + recordCountsPerIteration);
               }

               List paymentDataList = null;

               try {
                  paymentDataList = this.getPaymentTransactions(accountEJB, vendorType, recordCountsPerIteration);
               } catch (Exception var21) {
                  log.error("getPaymentTransactions( vendorType:" + vendorType + ",recordCountsPerIteration " + recordCountsPerIteration + ") failed", var21);
                  throw new JobExecutionException("Error in retrieving payment transactions", var21);
               }

               if (log.isDebugEnabled()) {
                  log.debug("Found " + paymentDataList.size() + " records ");
               }

               if (paymentDataList != null) {
                  Iterator i$ = paymentDataList.iterator();

                  while(i$.hasNext()) {
                     PaymentData paymentData = (PaymentData)i$.next();

                     try {
                        PaymentData newPaymentStatus = this.retrieveStatusFromVendor(paymentData);
                        if (newPaymentStatus != null) {
                           this.updateStatus(accountEJB, newPaymentStatus);
                        } else {
                           log.error("retrieveStatusFromVendor(paymentData.id=" + paymentData.id + ") implementation is returning null!");
                        }
                     } catch (Exception var20) {
                        log.error("Error while trying to update status for paymentData.id=" + paymentData.id + " username = " + paymentData.username, var20);
                     }

                     try {
                        int delayAfterOneRecord = this.getSleepMillisecPeriodAfterProcessingOneRecord();
                        Thread.sleep((long)delayAfterOneRecord);
                     } catch (InterruptedException var19) {
                     }
                  }
               } else {
                  log.warn("List of PaymentData is null");
               }
            } else {
               log.info(this.getJobName() + " disabled. Bailing out \\m/.");
            }
         } catch (CreateException var22) {
            log.error("Error in retrieving beans...", var22);
         } catch (JobExecutionException var23) {
            log.error("Unable to continue with job " + this.getJobName(), var23);
         } catch (Exception var24) {
            log.error("Unhandled exception while executing " + this.getJobName() + ": ", var24);
         } finally {
            this.getSemaphore().release();
            log.info(this.getJobName() + " terminated.");
         }

      }
   }
}
