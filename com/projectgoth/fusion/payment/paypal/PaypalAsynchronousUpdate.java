package com.projectgoth.fusion.payment.paypal;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.payment.PaymentAsynchStatusUpdate;
import com.projectgoth.fusion.payment.PaymentData;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class PaypalAsynchronousUpdate extends PaymentAsynchStatusUpdate {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PaypalAsynchronousUpdate.class));
   private static final Semaphore semaphore = new Semaphore(1);

   protected Logger getLogger() {
      return log;
   }

   protected Semaphore getSemaphore() {
      return semaphore;
   }

   public boolean isEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_ASYNCH_UPDATE.ENABLED);
   }

   protected int getVendorTypeFilter() {
      return PaymentData.TypeEnum.PAYPAL.getEnumValue();
   }

   protected Integer getRecordCountsPerIteration() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_ASYNCH_UPDATE.MAX_REC_COUNT_PER_FETCH);
   }

   protected int getSleepMillisecPeriodAfterProcessingOneRecord() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_ASYNCH_UPDATE.SLEEP_MILLIS_AFTER_ONE_RECORD);
   }

   protected int getSleepMillisecPeriodAfterProcessingOneBatch() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_ASYNCH_UPDATE.SLEEP_MILLIS_AFTER_ONE_BATCH);
   }

   public static PaymentData.StatusEnum getPaymentStatus(Date currentTime, Date createdDateTime, long maxPendingLifeSpanSec, int resCode, String txStatus) {
      return PaymentData.StatusEnum.CANCELLED;
   }

   protected PaymentData retrieveStatusFromVendor(PaymentData paymentData) throws Exception {
      Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
      long maxPendingLifeSpanSec = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL_ASYNCH_UPDATE.MAX_PENDING_LIFE_SPAN_SEC);
      long elapsedTime = DateTimeUtils.getElapsedTimeInSeconds(currentTimestamp, paymentData.fetchCreatedTime());
      if (elapsedTime > maxPendingLifeSpanSec) {
         paymentData.status = PaymentData.StatusEnum.CANCELLED;
         log.warn("Payment.id " + paymentData.id + " has been pending for too long. Will explicitly set the status as " + paymentData.status);
      }

      paymentData.assignUpdatedTime(currentTimestamp);
      return paymentData;
   }
}
