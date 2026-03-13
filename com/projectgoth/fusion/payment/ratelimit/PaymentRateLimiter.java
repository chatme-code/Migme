package com.projectgoth.fusion.payment.ratelimit;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.log4j.Logger;

public class PaymentRateLimiter {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PaymentRateLimiter.class));
   private MemCachedKeySpaces.MemCachedKeySpaceInterface rateLimiterRootKeySpace;
   private SystemPropertyEntities.SystemPropertyEntryInterface nameSpaceSystemProperty;

   public PaymentRateLimiter(MemCachedKeySpaces.MemCachedKeySpaceInterface rateLimiterRootKeySpace, SystemPropertyEntities.SystemPropertyEntryInterface nameSpaceSystemProperty) {
      this.rateLimiterRootKeySpace = rateLimiterRootKeySpace;
      this.nameSpaceSystemProperty = nameSpaceSystemProperty;
   }

   public String getSubNameSpaceWithLocalKey(PaymentRateLimitKeySpace rateLimiterKeySpace, String localKey) {
      return MemCachedKeyUtils.getFullKeyFromStrings(String.valueOf(SystemProperty.getLong(this.nameSpaceSystemProperty)), String.valueOf(rateLimiterKeySpace.getVendorType().value()), String.valueOf(rateLimiterKeySpace.getRateLimitType().getTypeCode()), rateLimiterKeySpace.getSubNamespace(), localKey);
   }

   public void reset(PaymentRateLimitKeySpace rateLimiterKeySpace, String localKey) {
      String key = this.getSubNameSpaceWithLocalKey(rateLimiterKeySpace, localKey);
      MemCachedClientWrapper.delete(this.rateLimiterRootKeySpace, key);
   }

   public void set(PaymentRateLimitKeySpace rateLimiterKeySpace, String localKey, long value, long ttl) {
      String key = this.getSubNameSpaceWithLocalKey(rateLimiterKeySpace, localKey);
      MemCachedClientWrapper.set(this.rateLimiterRootKeySpace, key, value, ttl);
   }

   public long get(PaymentRateLimitKeySpace rateLimiterKeySpace, String localKey) {
      String key = this.getSubNameSpaceWithLocalKey(rateLimiterKeySpace, localKey);
      return MemCachedClientWrapper.getCounter(this.rateLimiterRootKeySpace, key);
   }

   public <TContext> boolean checkWithoutHit(PaymentRateLimitKeySpace rateLimiterKeySpace, String localKey, long incrValue, long maxHitCount, MemCachedClientWrapper.InitialCounterValueProvider<TContext> initialCounterValueProvider, TContext contextData) {
      String key = this.getSubNameSpaceWithLocalKey(rateLimiterKeySpace, localKey);
      long hitCount = this.getCounter(key, initialCounterValueProvider, contextData);
      if (log.isDebugEnabled()) {
         log.debug("rateLimiterKeySpace=" + this.rateLimiterRootKeySpace + ";key=" + key + " hitCount=" + hitCount + ";maxHitcount=" + maxHitCount + ";incrValue=" + incrValue);
      }

      return hitCount + incrValue > maxHitCount;
   }

   private <TContext> long getCounter(String key, MemCachedClientWrapper.InitialCounterValueProvider<TContext> initialCounterValueProvider, TContext contextData) {
      long hitCount = MemCachedClientWrapper.getCounter(this.rateLimiterRootKeySpace, key, initialCounterValueProvider, contextData);
      if (hitCount < 0L) {
         throw new IllegalStateException("Unable to fetch counter for " + this.rateLimiterRootKeySpace + "/" + key);
      } else {
         return hitCount;
      }
   }

   public <TContext> long incrHitCount(PaymentRateLimitKeySpace rateLimiterKeySpace, String localKey, long incrValue, MemCachedClientWrapper.InitialCounterValueProvider<TContext> initialCounterValueProvider, TContext contextData) {
      String key = this.getSubNameSpaceWithLocalKey(rateLimiterKeySpace, localKey);
      return MemCachedClientWrapper.incr(this.rateLimiterRootKeySpace, key, initialCounterValueProvider, incrValue, contextData);
   }

   protected static long getCurrentPreallocationTimeSlot(long actionTimestamp, long preallocationLifeSpan) {
      return actionTimestamp - actionTimestamp % preallocationLifeSpan;
   }

   protected <TContext> PaymentRateLimiter.HitInstance beginHit(PaymentRateLimitKeySpace rateLimiterKeySpace, String localMainKey, long incrValue, long maxHitCount, long preallocationLifeSpan, Date actionTime, MemCachedClientWrapper.InitialCounterValueProvider<TContext> initialCounterValueProvider, TContext contextData) {
      String mainKey = this.getSubNameSpaceWithLocalKey(rateLimiterKeySpace, localMainKey);
      long mainHitValue = this.getCounter(mainKey, initialCounterValueProvider, contextData);
      if (mainHitValue > maxHitCount) {
         if (log.isDebugEnabled()) {
            log.debug("rateLimiterKeySpace=" + this.rateLimiterRootKeySpace + ";mainKey=" + mainKey + ";mainHitValue=" + mainHitValue + ";maxHitCount=" + maxHitCount);
         }

         return null;
      } else {
         long actionTimeStamp = actionTime.getTime();
         long currentPreallocationTimeSlot = getCurrentPreallocationTimeSlot(actionTimeStamp, preallocationLifeSpan);
         long prevPreallocationTimeSlot = currentPreallocationTimeSlot - preallocationLifeSpan;
         String prevPreallocationSlotKey = MemCachedKeyUtils.getFullKeyFromStrings(mainKey, DateTimeUtils.getStringForPaymentRateLimitPreallocationTime(new Timestamp(prevPreallocationTimeSlot)));
         long prevPreallocationHitCount = MemCachedClientWrapper.getCounter(this.rateLimiterRootKeySpace, prevPreallocationSlotKey);
         if (prevPreallocationHitCount < 0L) {
            prevPreallocationHitCount = 0L;
         }

         long prevPreallocationHitCount_mainHitValue = prevPreallocationHitCount + mainHitValue;
         if (prevPreallocationHitCount_mainHitValue > maxHitCount) {
            if (log.isDebugEnabled()) {
               log.debug("rateLimiterKeySpace=" + this.rateLimiterRootKeySpace + ";mainKey=" + mainKey + ";mainHitValue=" + mainHitValue + ";prevPreallocationSlotKey=" + prevPreallocationSlotKey + ";prevPreallocationHitCount=" + prevPreallocationHitCount + ";maxHitCount=" + maxHitCount);
            }

            return null;
         } else {
            String currentPreallocationSlotKey = MemCachedKeyUtils.getFullKeyFromStrings(mainKey, DateTimeUtils.getStringForPaymentRateLimitPreallocationTime(new Timestamp(currentPreallocationTimeSlot)));
            long currentPreallocationHitCount = MemCachedClientWrapper.addOrIncr(this.rateLimiterRootKeySpace, currentPreallocationSlotKey, incrValue, preallocationLifeSpan << 1);
            if (currentPreallocationHitCount >= 0L && currentPreallocationHitCount + prevPreallocationHitCount_mainHitValue > maxHitCount) {
               if (log.isDebugEnabled()) {
                  log.debug("rateLimiterKeySpace=" + this.rateLimiterRootKeySpace + ";mainKey=" + mainKey + ";mainHitValue=" + mainHitValue + ";prevPreallocationSlotKey=" + prevPreallocationSlotKey + ";prevPreallocationHitCount=" + prevPreallocationHitCount + ";currentPreallocationSlotKey=" + currentPreallocationSlotKey + ";currentPreallocationHitCount=" + currentPreallocationHitCount + ";maxHitCount=" + maxHitCount);
               }

               MemCachedClientWrapper.decr(this.rateLimiterRootKeySpace, currentPreallocationSlotKey, incrValue);
               return null;
            } else {
               return new PaymentRateLimiter.HitInstance(incrValue, this.rateLimiterRootKeySpace, mainKey, currentPreallocationSlotKey);
            }
         }
      }
   }

   public <TContext> PaymentRateLimiter.HitInstance beginHit(PaymentRateLimitKeySpace rateLimiterKeySpace, String localMainKey, long incrValue, long maxHitCount, long preallocationLifeSpan, MemCachedClientWrapper.InitialCounterValueProvider<TContext> initialCounterValueProvider, TContext contextData) {
      return this.beginHit(rateLimiterKeySpace, localMainKey, incrValue, maxHitCount, preallocationLifeSpan, new Timestamp(System.currentTimeMillis()), initialCounterValueProvider, contextData);
   }

   public static class HitInstance {
      private String currentPreallocationSlotKey;
      private String mainKey;
      private long incrValue;
      private MemCachedKeySpaces.MemCachedKeySpaceInterface rateLimiterRootKeySpace;

      public HitInstance(long incrValue, MemCachedKeySpaces.MemCachedKeySpaceInterface rateLimiterRootKeySpace, String mainKey, String currentPreallocationSlotKey) {
         this.incrValue = incrValue;
         this.currentPreallocationSlotKey = currentPreallocationSlotKey;
         this.mainKey = mainKey;
         this.rateLimiterRootKeySpace = rateLimiterRootKeySpace;
      }

      public void end(boolean doConfirm) {
         if (doConfirm) {
            MemCachedClientWrapper.incr(this.rateLimiterRootKeySpace, this.mainKey, this.incrValue);
         }

         long currentPreallocCount = MemCachedClientWrapper.decr(this.rateLimiterRootKeySpace, this.currentPreallocationSlotKey, this.incrValue);
         if (PaymentRateLimiter.log.isDebugEnabled()) {
            PaymentRateLimiter.log.debug("currentPreallocCount:" + currentPreallocCount);
         }

      }
   }
}
