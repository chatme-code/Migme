package com.projectgoth.fusion.payment.paypal;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.payment.PaymentException;
import java.util.Date;
import java.util.HashMap;
import javax.ejb.CreateException;

public class Restrictions {
   private static MemCachedClient MEMCACHE_CLIENT_RATE_LIMIT;

   /** @deprecated */
   public static boolean hitIndividualTransactionCount(UserData userData) {
      int threshold = (Integer)getThreshold(userData.type, Restrictions.RateLimit.INDIVIDUAL_USER_TRANSACTIONS_COUNT, Integer.class);
      return threshold > 0 && MEMCACHE_CLIENT_RATE_LIMIT.getCounter(Restrictions.RateLimit.INDIVIDUAL_USER_TRANSACTIONS_COUNT.getMemcacheKey(userData.username)) >= (long)threshold;
   }

   /** @deprecated */
   public static boolean hitIndividualTransactionAmount(UserData userData, double amount) throws CreateException {
      AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
      double threshold = (Double)getThreshold(userData.type, Restrictions.RateLimit.INDIVIDUAL_TRANSACTIONS_AMOUNT, Double.class);
      threshold = accountEJB.convertCurrency(threshold, "AUD", "USD");
      return threshold > 0.0D && Double.valueOf(MEMCACHE_CLIENT_RATE_LIMIT.get(Restrictions.RateLimit.INDIVIDUAL_TRANSACTIONS_AMOUNT.getMemcacheKey(userData.username)).toString()) + amount > threshold;
   }

   /** @deprecated */
   public static boolean hitCountryTransactionCount(UserData userData) {
      int threshold = (Integer)getThreshold(userData.type, Restrictions.RateLimit.COUNTRY_TRANSACTIONS_COUNT, Integer.class);
      return threshold > 0 && MEMCACHE_CLIENT_RATE_LIMIT.getCounter(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_COUNT.getMemcacheKey(userData.countryID)) > (long)threshold;
   }

   /** @deprecated */
   public static boolean hitCountryTransactionAmount(UserData userData, double amount) throws CreateException {
      AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
      double threshold = (Double)getThreshold(userData.type, Restrictions.RateLimit.COUNTRY_TRANSACTIONS_AMOUNT, Double.class);
      threshold = accountEJB.convertCurrency(threshold, "AUD", "USD");
      return threshold > 0.0D && Double.valueOf(MEMCACHE_CLIENT_RATE_LIMIT.get(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_AMOUNT.getMemcacheKey(userData.countryID)).toString()) + amount > threshold;
   }

   /** @deprecated */
   public static boolean hitAllTransactionCount(UserData userData) {
      int threshold = (Integer)getThreshold(userData.type, Restrictions.RateLimit.ALL_USER_TRANSACTIONS_COUNT, Integer.class);
      return threshold > 0 && MEMCACHE_CLIENT_RATE_LIMIT.getCounter(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_COUNT.getMemcacheKey()) > (long)threshold;
   }

   /** @deprecated */
   public static boolean hitAllTransactionAmount(UserData userData, double amount) throws CreateException {
      AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
      double threshold = (Double)getThreshold(userData.type, Restrictions.RateLimit.ALL_USER_TRANSACTIONS_AMOUNT, Double.class);
      threshold = accountEJB.convertCurrency(threshold, "AUD", "USD");
      return threshold > 0.0D && Double.valueOf(MEMCACHE_CLIENT_RATE_LIMIT.get(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_AMOUNT.getMemcacheKey()).toString()) + amount > threshold;
   }

   /** @deprecated */
   public static boolean hitPaypalAccountCount(UserData userData, String paypalAccount) {
      int threshold = (Integer)getThreshold(userData.type, Restrictions.RateLimit.PAYPAL_ACCOUNT_TRANSACTIONS_COUNT, Integer.class);
      return threshold > 0 && MEMCACHE_CLIENT_RATE_LIMIT.getCounter(Restrictions.RateLimit.PAYPAL_ACCOUNT_TRANSACTIONS_COUNT.getMemcacheKey(paypalAccount)) > (long)threshold;
   }

   private static <T extends Number> T getThreshold(UserData.TypeEnum type, Restrictions.RateLimit rateLimit, Class<T> valueType) {
      String propValue = "";
      if (type == UserData.TypeEnum.MIG33) {
         propValue = SystemProperty.get(Restrictions.UserRestrictionsLookup.fromRateLimit(rateLimit.rateLimitVal).getSysPropEntry());
      } else if (type == UserData.TypeEnum.MIG33_MERCHANT) {
         propValue = SystemProperty.get(Restrictions.MerchantRestrictionsLookup.fromRateLimit(rateLimit.rateLimitVal).getSysPropEntry());
      } else {
         if (type != UserData.TypeEnum.MIG33_TOP_MERCHANT) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
         }

         propValue = SystemProperty.get(Restrictions.TopMerchantRestrictionsLookup.fromRateLimit(rateLimit.rateLimitVal).getSysPropEntry());
      }

      if (Double.class.equals(valueType)) {
         return Double.valueOf(propValue);
      } else if (Integer.class.equals(valueType)) {
         return Integer.valueOf(propValue);
      } else {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }
   }

   public static double getMinimumAmount(UserData.TypeEnum type) {
      if (type == UserData.TypeEnum.MIG33) {
         return SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.MIN_AMOUNT_USD_MIG33);
      } else if (type == UserData.TypeEnum.MIG33_MERCHANT) {
         return SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.MIN_AMOUNT_USD_MIG33_MERCHANT);
      } else if (type == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
         return SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.MIN_AMOUNT_USD_MIG33_TOP_MERCHANT);
      } else {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }
   }

   public static double getMaximumAmount(UserData.TypeEnum type) {
      if (type == UserData.TypeEnum.MIG33) {
         return SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.MAX_AMOUNT_USD_MIG33);
      } else if (type == UserData.TypeEnum.MIG33_MERCHANT) {
         return SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.MAX_AMOUNT_USD_MIG33_MERCHANT);
      } else if (type == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
         return SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_PAYPAL.MAX_AMOUNT_USD_MIG33_TOP_MERCHANT);
      } else {
         throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
      }
   }

   static {
      MEMCACHE_CLIENT_RATE_LIMIT = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.rateLimit);
   }

   /** @deprecated */
   public static enum RateLimit {
      INDIVIDUAL_USER_TRANSACTIONS_COUNT(1, "PAYPAL_TX/USR_TXNS_CTR", 86400000L),
      ALL_USER_TRANSACTIONS_COUNT(2, "PAYPAL_TX/USR_ALL_TXNS_CTR", 600000L),
      PAYPAL_ACCOUNT_TRANSACTIONS_COUNT(3, "PAYPAL_TX/ACCT_TXNS_CTR", 600000L),
      COUNTRY_TRANSACTIONS_COUNT(4, "PAYPAL_TX/COUNTRY_TXNS_CTR", 600000L),
      INDIVIDUAL_TRANSACTIONS_AMOUNT(5, "PAYPAL_TX/USR_TTL_AMNT", 86400000L),
      ALL_USER_TRANSACTIONS_AMOUNT(6, "PAYPAL_TX/ALL_USR_TXNS_AMNT", 600000L),
      COUNTRY_TRANSACTIONS_AMOUNT(7, "PAYPAL_TX/COUNTRY_TXNS_AMNT", 600000L);

      private int rateLimitVal;
      private String memcacheKey;
      private long expiry;

      private RateLimit(int rateLimitVal, String memcacheKey, Long expiry) {
         this.rateLimitVal = rateLimitVal;
         this.memcacheKey = memcacheKey;
         this.expiry = expiry;
      }

      public int value() {
         return this.rateLimitVal;
      }

      public String getMemcacheKey() {
         return this.memcacheKey;
      }

      public String getMemcacheKey(String key) {
         return this.memcacheKey + "/" + key;
      }

      public String getMemcacheKey(int key) {
         return this.memcacheKey + "/" + key;
      }

      public Date getExpirationDate() {
         return new Date(System.currentTimeMillis() + this.expiry);
      }
   }

   /** @deprecated */
   private static enum TopMerchantRestrictionsLookup implements EnumUtils.IEnumValueGetter<Integer> {
      INDIVIDUAL_USER_TRANSACTIONS_COUNT(Restrictions.RateLimit.INDIVIDUAL_USER_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_MIG33_TOP_MERCHANT),
      ALL_USER_TRANSACTIONS_COUNT(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_MIG33_TOP_MERCHANT),
      PAYPAL_ACCOUNT_TRANSACTIONS_COUNT(Restrictions.RateLimit.PAYPAL_ACCOUNT_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33_TOP_MERCHANT),
      COUNTRY_TRANSACTIONS_COUNT(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_MIG33_TOP_MERCHANT),
      INDIVIDUAL_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.INDIVIDUAL_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_MIG33_TOP_MERCHANT),
      ALL_USER_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33_TOP_MERCHANT),
      COUNTRY_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33_TOP_MERCHANT);

      private Integer rateLimit;
      private SystemPropertyEntities.SystemPropertyEntryInterface sysPropEntry;
      private static HashMap<Integer, Restrictions.TopMerchantRestrictionsLookup> lookupByRateLimit = new HashMap();

      private TopMerchantRestrictionsLookup(int rateLimit, SystemPropertyEntities.SystemPropertyEntryInterface sysPropEntry) {
         this.rateLimit = rateLimit;
         this.sysPropEntry = sysPropEntry;
      }

      public SystemPropertyEntities.SystemPropertyEntryInterface getSysPropEntry() {
         return this.sysPropEntry;
      }

      public Integer getEnumValue() {
         return this.rateLimit;
      }

      public static Restrictions.TopMerchantRestrictionsLookup fromRateLimit(Integer rateLimit) {
         return (Restrictions.TopMerchantRestrictionsLookup)lookupByRateLimit.get(rateLimit);
      }

      static {
         EnumUtils.populateLookUpMap(lookupByRateLimit, Restrictions.TopMerchantRestrictionsLookup.class);
      }
   }

   /** @deprecated */
   private static enum MerchantRestrictionsLookup implements EnumUtils.IEnumValueGetter<Integer> {
      INDIVIDUAL_USER_TRANSACTIONS_COUNT(Restrictions.RateLimit.INDIVIDUAL_USER_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_MIG33_MERCHANT),
      ALL_USER_TRANSACTIONS_COUNT(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_MIG33_MERCHANT),
      PAYPAL_ACCOUNT_TRANSACTIONS_COUNT(Restrictions.RateLimit.PAYPAL_ACCOUNT_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33_MERCHANT),
      COUNTRY_TRANSACTIONS_COUNT(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_MIG33_MERCHANT),
      INDIVIDUAL_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.INDIVIDUAL_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_MIG33_MERCHANT),
      ALL_USER_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33_MERCHANT),
      COUNTRY_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33_MERCHANT);

      private Integer rateLimit;
      private SystemPropertyEntities.SystemPropertyEntryInterface sysPropEntry;
      private static HashMap<Integer, Restrictions.MerchantRestrictionsLookup> lookupByRateLimit = new HashMap();

      private MerchantRestrictionsLookup(int rateLimit, SystemPropertyEntities.SystemPropertyEntryInterface sysPropEntry) {
         this.rateLimit = rateLimit;
         this.sysPropEntry = sysPropEntry;
      }

      public SystemPropertyEntities.SystemPropertyEntryInterface getSysPropEntry() {
         return this.sysPropEntry;
      }

      public Integer getEnumValue() {
         return this.rateLimit;
      }

      public static Restrictions.MerchantRestrictionsLookup fromRateLimit(Integer rateLimit) {
         return (Restrictions.MerchantRestrictionsLookup)lookupByRateLimit.get(rateLimit);
      }

      static {
         EnumUtils.populateLookUpMap(lookupByRateLimit, Restrictions.MerchantRestrictionsLookup.class);
      }
   }

   /** @deprecated */
   private static enum UserRestrictionsLookup implements EnumUtils.IEnumValueGetter<Integer> {
      INDIVIDUAL_USER_TRANSACTIONS_COUNT(Restrictions.RateLimit.INDIVIDUAL_USER_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_TRANSACTIONS_MIG33),
      ALL_USER_TRANSACTIONS_COUNT(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_ALL_TRANSACTIONS_MIG33),
      PAYPAL_ACCOUNT_TRANSACTIONS_COUNT(Restrictions.RateLimit.PAYPAL_ACCOUNT_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_PAYPAL_ACCOUNT_TRANSACTIONS_MIG33),
      COUNTRY_TRANSACTIONS_COUNT(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_COUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.NUMBER_COUNTRY_TRANSACTIONS_MIG33),
      INDIVIDUAL_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.INDIVIDUAL_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_USD_MIG33),
      ALL_USER_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.ALL_USER_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_ALL_TRANSACTIONS_USD_MIG33),
      COUNTRY_TRANSACTIONS_AMOUNT(Restrictions.RateLimit.COUNTRY_TRANSACTIONS_AMOUNT.rateLimitVal, SystemPropertyEntities.Payments_PAYPAL_RateLimit.TOTAL_AMOUNT_COUNTRY_TRANSACTIONS_USD_MIG33);

      private Integer rateLimit;
      private SystemPropertyEntities.SystemPropertyEntryInterface sysPropEntry;
      private static HashMap<Integer, Restrictions.UserRestrictionsLookup> lookupByRateLimit = new HashMap();

      private UserRestrictionsLookup(int rateLimit, SystemPropertyEntities.SystemPropertyEntryInterface sysPropEntry) {
         this.rateLimit = rateLimit;
         this.sysPropEntry = sysPropEntry;
      }

      public SystemPropertyEntities.SystemPropertyEntryInterface getSysPropEntry() {
         return this.sysPropEntry;
      }

      public Integer getEnumValue() {
         return this.rateLimit;
      }

      public static Restrictions.UserRestrictionsLookup fromRateLimit(Integer rateLimit) {
         return (Restrictions.UserRestrictionsLookup)lookupByRateLimit.get(rateLimit);
      }

      static {
         EnumUtils.populateLookUpMap(lookupByRateLimit, Restrictions.UserRestrictionsLookup.class);
      }
   }
}
