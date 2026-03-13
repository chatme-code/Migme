package com.projectgoth.fusion.common;

import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

public abstract class LazyLoader<TValueType> {
   private volatile TValueType cachedValue;
   private final long localCacheTTLms;
   private final ReentrantLock lock;
   private volatile long lastUpdatedTime;
   private final Logger logger;

   public LazyLoader(long localCacheTTLMillies) {
      this((String)null, localCacheTTLMillies);
   }

   public LazyLoader(String logCategorySubName, long localCacheTTLMillies) {
      this.cachedValue = null;
      this.lock = new ReentrantLock();
      this.lastUpdatedTime = -1L;
      this.localCacheTTLms = localCacheTTLMillies;
      if (StringUtil.isBlank(logCategorySubName)) {
         this.logger = Logger.getLogger(ConfigUtils.getLoggerName(this.getClass()));
      } else {
         this.logger = Logger.getLogger(ConfigUtils.getLoggerName(LazyLoader.class) + "[" + logCategorySubName + "]");
      }

   }

   public final TValueType getValue() {
      if (this.lastUpdatedTime == -1L) {
         this.lock.lock();

         try {
            if (this.lastUpdatedTime == -1L || this.getCurrentTimeMillis() - this.lastUpdatedTime > this.localCacheTTLms) {
               this.refreshCache();
            }
         } finally {
            this.lock.unlock();
         }
      } else if (this.getCurrentTimeMillis() - this.lastUpdatedTime > this.localCacheTTLms && this.lock.tryLock()) {
         try {
            this.refreshCache();
         } finally {
            this.lock.unlock();
         }
      }

      return this.cachedValue;
   }

   private void refreshCache() {
      try {
         if (this.logger.isDebugEnabled()) {
            this.logger.debug("Refreshing cache");
         }

         TValueType lastValue = this.cachedValue;
         this.cachedValue = this.fetchValue();
         this.lastUpdatedTime = this.getCurrentTimeMillis();
         if (this.cachedValue == null && lastValue != null || this.cachedValue != null && !this.cachedValue.equals(lastValue)) {
            this.onValueChanged(lastValue, this.cachedValue);
         }

         if (this.logger.isDebugEnabled()) {
            this.logger.debug("Cache refreshed with cachedValue=[" + this.cachedValue + "].Last Updated Time=[" + this.lastUpdatedTime + "]");
         }
      } catch (Exception var2) {
         this.logger.error("Unable to refresh cache ", var2);
      }

   }

   protected void onValueChanged(TValueType lastValue, TValueType currentValue) {
   }

   protected final int isLocked() {
      return this.lock.isLocked() ? 0 : 1;
   }

   protected long getCurrentTimeMillis() {
      return System.currentTimeMillis();
   }

   protected final long getLastUpdatedTime() {
      return this.lastUpdatedTime;
   }

   protected abstract TValueType fetchValue() throws Exception;

   public final void invalidateCache() {
      this.lastUpdatedTime = -1L;
      this.onInvalidateCache();
   }

   protected void onInvalidateCache() {
   }

   protected Logger getLogger() {
      return this.logger;
   }
}
