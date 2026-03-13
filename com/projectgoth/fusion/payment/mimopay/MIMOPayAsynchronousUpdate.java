package com.projectgoth.fusion.payment.mimopay;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.payment.PaymentAsynchStatusUpdate;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentException;
import java.sql.Timestamp;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class MIMOPayAsynchronousUpdate extends PaymentAsynchStatusUpdate {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MIMOPayAsynchronousUpdate.class));
   private static final Semaphore semaphore = new Semaphore(1);

   protected Logger getLogger() {
      return log;
   }

   protected Semaphore getSemaphore() {
      return semaphore;
   }

   public boolean isEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY_ASYNCH_UPDATE.ENABLED);
   }

   protected int getVendorTypeFilter() {
      return PaymentData.TypeEnum.MIMOPAY.getEnumValue();
   }

   protected Integer getRecordCountsPerIteration() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY_ASYNCH_UPDATE.MAX_REC_COUNT_PER_FETCH);
   }

   protected int getSleepMillisecPeriodAfterProcessingOneRecord() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY_ASYNCH_UPDATE.SLEEP_MILLIS_AFTER_ONE_RECORD);
   }

   protected int getSleepMillisecPeriodAfterProcessingOneBatch() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY_ASYNCH_UPDATE.SLEEP_MILLIS_AFTER_ONE_BATCH);
   }

   protected PaymentData retrieveStatusFromVendor(PaymentData paymentData) throws Exception {
      MIMOVoucherData currentMIMOVoucherData = (MIMOVoucherData)paymentData;
      PendingStatusInquiryRequest pendingStatusInquiryRequest = new PendingStatusInquiryRequest();
      pendingStatusInquiryRequest.gameCode = currentMIMOVoucherData.gameCode;
      String ourFormattedTransactionID = MIMOPayment.formatOurTransactionID(currentMIMOVoucherData.id);
      String secretKey = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY.GATEWAY_SHARED_SECRET);
      String merchantCode = currentMIMOVoucherData.merchantCode;
      pendingStatusInquiryRequest.hashKey = MIMOPayment.generateHashKey(merchantCode, ourFormattedTransactionID, secretKey);
      pendingStatusInquiryRequest.merchantCode = currentMIMOVoucherData.merchantCode;
      pendingStatusInquiryRequest.ourTransactionId = ourFormattedTransactionID;
      pendingStatusInquiryRequest.pType = currentMIMOVoucherData.creditReloadPType;
      pendingStatusInquiryRequest.reloadCardOrTokenKey = currentMIMOVoucherData.voucherCode;
      pendingStatusInquiryRequest.serviceName = MIMOPayment.ServiceNameEnum.PENDING_STATUS_INQUIRY.getServiceCode();
      pendingStatusInquiryRequest.timestamp = MIMOPayment.getUnixTimestamp(System.currentTimeMillis());
      pendingStatusInquiryRequest.userID = String.valueOf(currentMIMOVoucherData.userId);
      PendingStatusInquiryResponse pendingStatusInquiryResponse = (PendingStatusInquiryResponse)MIMOPayment.dispatchRequest(pendingStatusInquiryRequest, PendingStatusInquiryResponse.class);
      MIMOPayment.ServiceNameEnum serviceName = MIMOPayment.ServiceNameEnum.fromServiceCode(pendingStatusInquiryResponse.serviceName);
      if (serviceName != MIMOPayment.ServiceNameEnum.PENDING_STATUS_INQUIRY) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Wrong service name " + serviceName});
      } else if (pendingStatusInquiryResponse.retCode == null) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"pendingStatusInquiryResponse.retCode"});
      } else if (StringUtil.isBlank(pendingStatusInquiryResponse.transID)) {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"pendingStatusInquiryResponse.transID"});
      } else {
         int transID = Integer.parseInt(pendingStatusInquiryResponse.transID);
         if (currentMIMOVoucherData.id != transID) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INVALID_VENDOR_DATA, new Object[]{"Wrong pendingStatusInquiryResponse.transID"});
         } else {
            MIMOPayment.MIMOResultCodeEnum mimoResultCodeEnum = MIMOPayment.MIMOResultCodeEnum.getMIMOResult(pendingStatusInquiryResponse.retCode);
            PaymentData.StatusEnum paymentStatus;
            if (mimoResultCodeEnum == null) {
               paymentStatus = PaymentData.StatusEnum.PENDING;
            } else {
               paymentStatus = mimoResultCodeEnum.getStatusOnSyncUpdate();
            }

            long currentTimeInMillis = System.currentTimeMillis();
            Timestamp currentTimestamp = new Timestamp(currentTimeInMillis);
            if (paymentStatus == PaymentData.StatusEnum.PENDING) {
               long maxPendingLifeSpanSec = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_MIMOPAY_ASYNCH_UPDATE.MAX_PENDING_LIFE_SPAN_SEC);
               long elapsedTime = DateTimeUtils.getElapsedTimeInSeconds(currentTimestamp, currentMIMOVoucherData.fetchCreatedTime());
               if (elapsedTime > maxPendingLifeSpanSec) {
                  paymentStatus = PaymentData.StatusEnum.TIMEOUT;
                  log.warn("Payment.id " + paymentData.id + " has been pending for too long. Will explicitly set the status as " + paymentStatus);
               }
            } else if (paymentStatus == PaymentData.StatusEnum.APPROVED && StringUtil.isBlank(pendingStatusInquiryResponse.mimoTransactionID)) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"pendingStatusInquiryResponse.mimoTransactionID"});
            }

            currentMIMOVoucherData.amount = pendingStatusInquiryResponse.reloadValue == null ? 0.0D : pendingStatusInquiryResponse.reloadValue;
            currentMIMOVoucherData.assignUpdatedTime(currentTimestamp);
            currentMIMOVoucherData.rvalue = pendingStatusInquiryResponse.reloadValue;
            currentMIMOVoucherData.status = paymentStatus;
            currentMIMOVoucherData.vendorRemark = pendingStatusInquiryResponse.remark;
            currentMIMOVoucherData.vendorStatusCode = pendingStatusInquiryResponse.retCode;
            currentMIMOVoucherData.vendorTimestamp = pendingStatusInquiryResponse.timestamp;
            currentMIMOVoucherData.vendorTransactionId = StringUtil.isBlank(pendingStatusInquiryResponse.mimoTransactionID) ? "" : pendingStatusInquiryResponse.mimoTransactionID;
            return currentMIMOVoucherData;
         }
      }
   }
}
