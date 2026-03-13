package com.projectgoth.fusion.payment.paypal;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentMetaDetails;
import com.projectgoth.fusion.payment.PaymentSummaryData;
import org.apache.log4j.Logger;

public class PaypalInitialValueProviders {
   public static final long DOLLAR_MULTIPLIER = 10000L;

   public static class AllCountPerPaypalUser extends PaypalInitialValueProvider<String> {
      public static final PaypalInitialValueProviders.AllCountPerPaypalUser INSTANCE = new PaypalInitialValueProviders.AllCountPerPaypalUser();
      private static final Logger LOG = Logger.getLogger(ConfigUtils.getLoggerName(PaypalInitialValueProviders.AllValuePerCountry.class));

      public AllCountPerPaypalUser() {
         super(PaypalRateLimitKeySpace.TOTAL_SUCCESS_COUNT_PER_VENDOR_USERID);
      }

      public long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, String paypalUserID) {
         try {
            PaypalInitialValueProvider.QueryStartDateEndDateParam queryStartDateEndDate = this.getQueryStartDateEndDate();
            AccountLocal accountLocal = getAccountLocal();
            PaymentSummaryData summaryData = accountLocal.getPaymentSummaryByMetaDetails(PaymentData.TypeEnum.PAYPAL.value(), PaymentData.StatusEnum.APPROVED.value(), PaymentMetaDetails.MetaType.PAYPAL_ACCOUNT.code(), paypalUserID, queryStartDateEndDate.startDate, queryStartDateEndDate.endDate);
            return (long)summaryData.count;
         } catch (Exception var7) {
            this.getLog().error("getInitialValue error", var7);
            return -1L;
         }
      }

      public Logger getLog() {
         return LOG;
      }
   }

   public static class AllValuePerCountry extends PaypalInitialValueProvider<PaypalInitialValueProviders.AllValuePerCountry.Param> {
      public static final PaypalInitialValueProviders.AllValuePerCountry INSTANCE = new PaypalInitialValueProviders.AllValuePerCountry();
      private static final Logger LOG = Logger.getLogger(ConfigUtils.getLoggerName(PaypalInitialValueProviders.AllValuePerCountry.class));

      public AllValuePerCountry() {
         super(PaypalRateLimitKeySpace.TOTAL_SUCCESS_VALUE_PER_COUNTRY);
      }

      public long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, PaypalInitialValueProviders.AllValuePerCountry.Param param) {
         try {
            PaypalInitialValueProvider.QueryStartDateEndDateParam queryStartDateEndDate = this.getQueryStartDateEndDate();
            AccountLocal accountLocal = getAccountLocal();
            PaymentSummaryData summaryData = accountLocal.getPaymentSummaryByCountryAndUserType(PaymentData.TypeEnum.PAYPAL.value(), param.countryID, param.userType.value(), PaymentData.StatusEnum.APPROVED.value(), queryStartDateEndDate.startDate, queryStartDateEndDate.endDate);
            return (long)(summaryData.cummValue * 10000.0D);
         } catch (Exception var7) {
            this.getLog().error("getInitialValue error", var7);
            return -1L;
         }
      }

      public Logger getLog() {
         return LOG;
      }

      public static class Param {
         public Integer countryID;
         public UserData.TypeEnum userType;

         public Param(Integer countryID, UserData.TypeEnum userType) {
            this.countryID = countryID;
            this.userType = userType;
         }
      }
   }

   public static class AllCountPerCountry extends PaypalInitialValueProvider<PaypalInitialValueProviders.AllCountPerCountry.Param> {
      public static final PaypalInitialValueProviders.AllCountPerCountry INSTANCE = new PaypalInitialValueProviders.AllCountPerCountry();
      private static final Logger LOG = Logger.getLogger(ConfigUtils.getLoggerName(PaypalInitialValueProviders.AllCountPerCountry.class));

      public AllCountPerCountry() {
         super(PaypalRateLimitKeySpace.TOTAL_SUCCESS_COUNT_PER_COUNTRY);
      }

      public long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, PaypalInitialValueProviders.AllCountPerCountry.Param param) {
         try {
            PaypalInitialValueProvider.QueryStartDateEndDateParam queryStartDateEndDate = this.getQueryStartDateEndDate();
            AccountLocal accountLocal = getAccountLocal();
            PaymentSummaryData summaryData = accountLocal.getPaymentSummaryByCountryAndUserType(PaymentData.TypeEnum.PAYPAL.value(), param.countryID, param.userType.value(), PaymentData.StatusEnum.APPROVED.value(), queryStartDateEndDate.startDate, queryStartDateEndDate.endDate);
            return (long)summaryData.count;
         } catch (Exception var7) {
            this.getLog().error("getInitialValue error", var7);
            return -1L;
         }
      }

      public Logger getLog() {
         return LOG;
      }

      public static class Param {
         public Integer countryID;
         public UserData.TypeEnum userType;

         public Param(Integer countryID, UserData.TypeEnum userType) {
            this.countryID = countryID;
            this.userType = userType;
         }
      }
   }

   public static class AllValuePerUserType extends PaypalInitialValueProvider<UserData.TypeEnum> {
      public static final PaypalInitialValueProviders.AllValuePerUserType INSTANCE = new PaypalInitialValueProviders.AllValuePerUserType();
      private static final Logger LOG = Logger.getLogger(ConfigUtils.getLoggerName(PaypalInitialValueProviders.AllValuePerUserType.class));

      public AllValuePerUserType() {
         super(PaypalRateLimitKeySpace.TOTAL_SUCCESS_VALUE_PER_USER_TYPE);
      }

      public long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, UserData.TypeEnum userType) {
         try {
            PaypalInitialValueProvider.QueryStartDateEndDateParam queryStartDateEndDate = this.getQueryStartDateEndDate();
            AccountLocal accountLocal = getAccountLocal();
            PaymentSummaryData summaryData = accountLocal.getPaymentSummaryByUserType(PaymentData.TypeEnum.PAYPAL.value(), userType.value(), PaymentData.StatusEnum.APPROVED.value(), queryStartDateEndDate.startDate, queryStartDateEndDate.endDate);
            return (long)(summaryData.cummValue * 10000.0D);
         } catch (Exception var7) {
            this.getLog().error("getInitialValue error", var7);
            return -1L;
         }
      }

      public Logger getLog() {
         return LOG;
      }
   }

   public static class AllCountPerUserType extends PaypalInitialValueProvider<UserData.TypeEnum> {
      public static final PaypalInitialValueProviders.AllCountPerUserType INSTANCE = new PaypalInitialValueProviders.AllCountPerUserType();
      private static final Logger LOG = Logger.getLogger(ConfigUtils.getLoggerName(PaypalInitialValueProviders.AllCountPerUserType.class));

      public AllCountPerUserType() {
         super(PaypalRateLimitKeySpace.TOTAL_SUCCESS_COUNT_PER_USERTYPE);
      }

      public long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, UserData.TypeEnum userType) {
         try {
            PaypalInitialValueProvider.QueryStartDateEndDateParam queryStartDateEndDate = this.getQueryStartDateEndDate();
            AccountLocal accountLocal = getAccountLocal();
            PaymentSummaryData summaryData = accountLocal.getPaymentSummaryByUserType(PaymentData.TypeEnum.PAYPAL.value(), userType.value(), PaymentData.StatusEnum.APPROVED.value(), queryStartDateEndDate.startDate, queryStartDateEndDate.endDate);
            return (long)summaryData.count;
         } catch (Exception var7) {
            this.getLog().error("getInitialValue error", var7);
            return -1L;
         }
      }

      public Logger getLog() {
         return LOG;
      }
   }

   public static class AllValuePerUserID extends PaypalInitialValueProvider<Integer> {
      public static final PaypalInitialValueProviders.AllValuePerUserID INSTANCE = new PaypalInitialValueProviders.AllValuePerUserID();
      private static final Logger LOG = Logger.getLogger(ConfigUtils.getLoggerName(PaypalInitialValueProviders.AllValuePerUserID.class));

      public AllValuePerUserID() {
         super(PaypalRateLimitKeySpace.TOTAL_SUCCESS_VALUE_PER_USERID);
      }

      public long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Integer userID) {
         try {
            PaypalInitialValueProvider.QueryStartDateEndDateParam queryStartDateEndDate = this.getQueryStartDateEndDate();
            AccountLocal accountLocal = getAccountLocal();
            PaymentSummaryData summaryData = accountLocal.getPaymentSummaryByUserID(PaymentData.TypeEnum.PAYPAL.value(), userID, PaymentData.StatusEnum.APPROVED.value(), queryStartDateEndDate.startDate, queryStartDateEndDate.endDate);
            return (long)(summaryData.cummValue * 10000.0D);
         } catch (Exception var7) {
            this.getLog().error("getInitialValue error", var7);
            return -1L;
         }
      }

      public Logger getLog() {
         return LOG;
      }
   }

   public static class AllCountPerUserID extends PaypalInitialValueProvider<Integer> {
      public static final PaypalInitialValueProviders.AllCountPerUserID INSTANCE = new PaypalInitialValueProviders.AllCountPerUserID();
      private static final Logger LOG = Logger.getLogger(ConfigUtils.getLoggerName(PaypalInitialValueProviders.AllCountPerUserID.class));

      public AllCountPerUserID() {
         super(PaypalRateLimitKeySpace.TOTAL_SUCCESS_COUNT_PER_USERID);
      }

      public long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, Integer userID) {
         try {
            PaypalInitialValueProvider.QueryStartDateEndDateParam queryStartDateEndDate = this.getQueryStartDateEndDate();
            AccountLocal accountLocal = getAccountLocal();
            PaymentSummaryData summaryData = accountLocal.getPaymentSummaryByUserID(PaymentData.TypeEnum.PAYPAL.value(), userID, PaymentData.StatusEnum.APPROVED.value(), queryStartDateEndDate.startDate, queryStartDateEndDate.endDate);
            return (long)summaryData.count;
         } catch (Exception var7) {
            this.getLog().error("getInitialValue error", var7);
            return -1L;
         }
      }

      public Logger getLog() {
         return LOG;
      }
   }
}
