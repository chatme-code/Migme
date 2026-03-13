package com.projectgoth.fusion.payment.paypal;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.ratelimit.PaymentRateLimitKeySpace;
import com.projectgoth.fusion.payment.ratelimit.PaymentRateLimitType;
import java.util.HashMap;
import java.util.Map;

public enum PaypalRateLimitKeySpace implements PaymentRateLimitKeySpace {
   TOTAL_SUCCESS_VALUE_PER_USERID(PaymentRateLimitType.TOTAL_SUCCESS_VALUE_PER_USERID, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_INTERVAL_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_CACHE_EXPIRY_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_MIG33, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_MIG33_MERCHANT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_MIG33_TOP_MERCHANT),
   TOTAL_SUCCESS_VALUE_PER_USER_TYPE(PaymentRateLimitType.TOTAL_SUCCESS_VALUE_PER_USERTYPE, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_INTERVAL_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_CACHE_EXPIRY_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33_MERCHANT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33_TOP_MERCHANT),
   TOTAL_SUCCESS_VALUE_PER_COUNTRY(PaymentRateLimitType.TOTAL_SUCCESS_VALUE_PER_COUNTRY, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_INTERVAL_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_CACHE_EXPIRY_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33_MERCHANT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33_TOP_MERCHANT),
   TOTAL_SUCCESS_COUNT_PER_USERID(PaymentRateLimitType.TOTAL_SUCCESS_COUNT_PER_USERID, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_INTERVAL_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_CACHE_EXPIRY_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_MIG33, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_MIG33_MERCHANT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_MIG33_TOP_MERCHANT),
   TOTAL_SUCCESS_COUNT_PER_COUNTRY(PaymentRateLimitType.TOTAL_SUCCESS_COUNT_PER_COUNTRY, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_INTERVAL_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_CACHE_EXPIRY_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_MIG33, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_MIG33_MERCHANT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_MIG33_TOP_MERCHANT),
   TOTAL_SUCCESS_COUNT_PER_USERTYPE(PaymentRateLimitType.TOTAL_SUCCESS_COUNT_PER_USERTYPE, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_INTERVAL_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_CACHE_EXPIRY_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_MIG33, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_MIG33_MERCHANT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_MIG33_TOP_MERCHANT),
   TOTAL_SUCCESS_COUNT_PER_VENDOR_USERID(PaymentRateLimitType.TOTAL_SUCCESS_COUNT_PER_VENDOR_USERID, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_INTERVAL_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_CACHE_EXPIRY_SECS, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33_MERCHANT, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33_TOP_MERCHANT);

   private PaymentRateLimitType rateLimitType;
   private final Map<UserData.TypeEnum, SystemPropertyEntities.Payments_PAYPAL_RateLimit> userRateLimitKey = new HashMap();
   private SystemPropertyEntities.Payments_PAYPAL_RateLimit quotaLimitIntervalKey;
   private SystemPropertyEntities.Payments_PAYPAL_RateLimit cachedHitExpiryKey;

   private PaypalRateLimitKeySpace(PaymentRateLimitType rateLimitType, SystemPropertyEntities.Payments_PAYPAL_RateLimit quotaLimitIntervalKey, SystemPropertyEntities.Payments_PAYPAL_RateLimit cachedHitExpiryKey, SystemPropertyEntities.Payments_PAYPAL_RateLimit normalUserRateLimit, SystemPropertyEntities.Payments_PAYPAL_RateLimit merchantUserRateLimit, SystemPropertyEntities.Payments_PAYPAL_RateLimit topMerchantUserRateLimit) {
      this.rateLimitType = rateLimitType;
      this.cachedHitExpiryKey = cachedHitExpiryKey;
      this.quotaLimitIntervalKey = quotaLimitIntervalKey;
      this.userRateLimitKey.put(UserData.TypeEnum.MIG33, normalUserRateLimit);
      this.userRateLimitKey.put(UserData.TypeEnum.MIG33_MERCHANT, merchantUserRateLimit);
      this.userRateLimitKey.put(UserData.TypeEnum.MIG33_TOP_MERCHANT, topMerchantUserRateLimit);
   }

   public SystemPropertyEntities.Payments_PAYPAL_RateLimit getQuotaLimitIntervalKey() {
      return this.quotaLimitIntervalKey;
   }

   public SystemPropertyEntities.Payments_PAYPAL_RateLimit getCachedHitExpiryKey() {
      return this.cachedHitExpiryKey;
   }

   public PaymentRateLimitType getRateLimitType() {
      return this.rateLimitType;
   }

   public PaymentData.TypeEnum getVendorType() {
      return PaymentData.TypeEnum.PAYPAL;
   }

   private SystemPropertyEntities.Payments_PAYPAL_RateLimit getUserRateLimitKey(UserData.TypeEnum userType) {
      return userType == null ? null : (SystemPropertyEntities.Payments_PAYPAL_RateLimit)this.userRateLimitKey.get(userType);
   }

   public <T extends Number> T getUserRateLimitValue(UserData.TypeEnum userType, Class<T> valueType) {
      return SystemProperty.getNumber(this.getUserRateLimitKey(userType), valueType);
   }

   public String getSubNamespace() {
      return "0";
   }
}
