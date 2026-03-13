package com.projectgoth.fusion.payment.mol;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.payment.PaymentAsynchStatusUpdate;
import com.projectgoth.fusion.payment.PaymentData;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class MOLAsynchronousUpdate extends PaymentAsynchStatusUpdate {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MOLAsynchronousUpdate.class));
   private static final Semaphore semaphore = new Semaphore(1);

   protected Logger getLogger() {
      return log;
   }

   protected Semaphore getSemaphore() {
      return semaphore;
   }

   public boolean isEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL_ASYNCH_UPDATE.ENABLED);
   }

   protected int getVendorTypeFilter() {
      return PaymentData.TypeEnum.MOL.getEnumValue();
   }

   protected Integer getRecordCountsPerIteration() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL_ASYNCH_UPDATE.MAX_REC_COUNT_PER_FETCH);
   }

   protected int getSleepMillisecPeriodAfterProcessingOneRecord() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL_ASYNCH_UPDATE.SLEEP_MILLIS_AFTER_ONE_RECORD);
   }

   protected int getSleepMillisecPeriodAfterProcessingOneBatch() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL_ASYNCH_UPDATE.SLEEP_MILLIS_AFTER_ONE_BATCH);
   }

   public static PaymentData.StatusEnum getPaymentStatus(Date currentTime, Date createdDateTime, long maxPendingLifeSpanSec, int resCode, String txStatus) {
      PaymentData.StatusEnum paymentStatus = PaymentData.StatusEnum.PENDING;
      MOLPayment.MOLResultCodeEnum resCodeEnum = MOLPayment.MOLResultCodeEnum.getMOLResult(resCode);
      if (resCodeEnum == MOLPayment.MOLResultCodeEnum.TRANSACTION_SUCCESSFULLY_COMPLETED) {
         String trimmedTxStatus = txStatus.trim();
         if (trimmedTxStatus.equalsIgnoreCase("1")) {
            paymentStatus = PaymentData.StatusEnum.APPROVED;
         } else {
            long elapsedInSec = DateTimeUtils.getElapsedTimeInSeconds(currentTime, createdDateTime);
            if (elapsedInSec > maxPendingLifeSpanSec) {
               paymentStatus = PaymentData.StatusEnum.CANCELLED;
            } else {
               paymentStatus = PaymentData.StatusEnum.PENDING;
            }
         }
      } else if (resCodeEnum == MOLPayment.MOLResultCodeEnum.USER_HAS_CHOSEN_TO_CANCEL_THE_TRANSACTION) {
         paymentStatus = PaymentData.StatusEnum.CANCELLED;
      } else if (resCodeEnum == MOLPayment.MOLResultCodeEnum.DUPLICATED_MERCHANT_REFERENCE_ID) {
         paymentStatus = PaymentData.StatusEnum.REJECTED;
      } else if (resCodeEnum == MOLPayment.MOLResultCodeEnum.INVALID_AMOUNT) {
         paymentStatus = PaymentData.StatusEnum.REJECTED;
      } else {
         long elapsedInSec = DateTimeUtils.getElapsedTimeInSeconds(currentTime, createdDateTime);
         if (elapsedInSec > maxPendingLifeSpanSec) {
            if (resCodeEnum == MOLPayment.MOLResultCodeEnum.ORDER_NOT_FOUND) {
               paymentStatus = PaymentData.StatusEnum.INVALID;
            } else {
               paymentStatus = PaymentData.StatusEnum.TIMEOUT;
            }
         } else {
            paymentStatus = PaymentData.StatusEnum.PENDING;
         }
      }

      return paymentStatus;
   }

   protected PaymentData retrieveStatusFromVendor(PaymentData paymentData) throws Exception {
      MOLPaymentData molPaymentData = (MOLPaymentData)paymentData;
      MOLQueryTransactionStatusResult qryTxResult = MOLPayment.getVendorTransactionStatus(molPaymentData.id.toString());
      long currentTimeMillis = System.currentTimeMillis();
      Timestamp currentTime = new Timestamp(currentTimeMillis);
      molPaymentData.assignUpdatedTime(currentTime);
      molPaymentData.vendorStatusUpdResCode = null;
      molPaymentData.asynchStatusUpdResCode = qryTxResult.resCode;
      molPaymentData.asynchStatusUpdStatusCode = qryTxResult.status;
      if (StringUtil.isBlank(qryTxResult.vendorTransactionId)) {
         molPaymentData.vendorTransactionId = "";
      } else {
         molPaymentData.vendorTransactionId = qryTxResult.vendorTransactionId;
      }

      int resCodeInt = Integer.parseInt(qryTxResult.resCode.trim());
      long maxPendingLifeSpanSec = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MOL_ASYNCH_UPDATE.MAX_PENDING_LIFE_SPAN_SEC);
      molPaymentData.status = getPaymentStatus(currentTime, molPaymentData.fetchCreatedTime(), maxPendingLifeSpanSec, resCodeInt, qryTxResult.status);
      log.info("ID:" + molPaymentData.id + " qryTxResult: " + qryTxResult + " Mapped to:" + molPaymentData.status);
      return molPaymentData;
   }
}
