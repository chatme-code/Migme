package com.projectgoth.fusion.payment.paypal;

import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import java.sql.Timestamp;
import java.util.Date;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public abstract class PaypalInitialValueProvider<T> implements MemCachedClientWrapper.InitialCounterValueProvider<T> {
   private PaypalRateLimitKeySpace rateLimitKeySpace;

   public PaypalInitialValueProvider(PaypalRateLimitKeySpace rateLimitKeySpace) {
      this.rateLimitKeySpace = rateLimitKeySpace;
   }

   public abstract Logger getLog();

   public PaypalRateLimitKeySpace getRateLimitKeySpace() {
      return this.rateLimitKeySpace;
   }

   public MemCachedKeySpaces.MemCachedKeySpaceInterface getInitialValueLockKeySpace(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, T ctx) {
      return MemCachedKeySpaces.CommonKeySpace.GET_INITIAL_COUNTER_VALUE_LOCK;
   }

   public String getInitialValueLockKeyName(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, T ctx) {
      String localKeyName = MemCachedKeyUtils.getFullKeyForKeySpace(keySpace, key);
      if (this.getLog().isDebugEnabled()) {
         this.getLog().debug("LocalKeyName lock:" + localKeyName);
      }

      return localKeyName;
   }

   public abstract long getInitialValue(MemCachedKeySpaces.MemCachedKeySpaceInterface var1, String var2, T var3);

   public long getInitialValueLockWaitTime(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, T ctx) {
      return 30000L;
   }

   public long getInitialValueTimeToLive(MemCachedKeySpaces.MemCachedKeySpaceInterface keySpace, String key, T ctx) {
      return (Long)SystemProperty.getNumber(this.rateLimitKeySpace.getCachedHitExpiryKey(), Long.class);
   }

   protected static AccountLocal getAccountLocal() throws CreateException {
      return (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
   }

   protected static Date getStartDate(Date endDate, long intervalInMillis) {
      return new Timestamp(endDate.getTime() - intervalInMillis);
   }

   protected PaypalInitialValueProvider.QueryStartDateEndDateParam getQueryStartDateEndDate() {
      Date endDateTime = new Timestamp(System.currentTimeMillis());
      long intervalInMillis = (Long)SystemProperty.getNumber(this.getRateLimitKeySpace().getQuotaLimitIntervalKey(), Long.class);
      Date startDateTime = getStartDate(endDateTime, intervalInMillis);
      return new PaypalInitialValueProvider.QueryStartDateEndDateParam(startDateTime, endDateTime);
   }

   public static class QueryStartDateEndDateParam {
      public Date startDate;
      public Date endDate;

      public QueryStartDateEndDateParam(Date startDate, Date endDate) {
         this.startDate = startDate;
         this.endDate = endDate;
      }
   }
}
