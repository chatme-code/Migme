package com.projectgoth.fusion.stats;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.monitor.MonitorHelpers;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class FusionPktOpenURLStats {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktOpenURLStats.class));
   private static final int MAX_FAILURES_BY_URL = 20;
   private AtomicInteger totalAttempts;
   private AtomicInteger totalFailures;
   private AtomicLong totalSuccessesProcessingTime;
   private final Cache<String, AtomicInteger> failuresByURLCache;
   private final ConcurrentMap<String, AtomicInteger> failuresByURL;
   private final Pattern imgURLPattern;

   private FusionPktOpenURLStats() {
      this.totalAttempts = new AtomicInteger(0);
      this.totalFailures = new AtomicInteger(0);
      this.totalSuccessesProcessingTime = new AtomicLong(0L);
      this.failuresByURLCache = CacheBuilder.newBuilder().maximumSize(100L).build();
      this.failuresByURL = this.failuresByURLCache.asMap();
      this.imgURLPattern = Pattern.compile("(http.*?img.*?com)(.*?)");
   }

   public static FusionPktOpenURLStats getInstance() {
      return FusionPktOpenURLStats.SingletonHolder.INSTANCE;
   }

   public void addSuccess(long processingTime) {
      this.totalAttempts.incrementAndGet();
      this.totalSuccessesProcessingTime.addAndGet(processingTime);
   }

   public void addFailure(String url) {
      this.totalAttempts.incrementAndGet();
      this.totalFailures.incrementAndGet();
      if (url != null) {
         try {
            String urlString = url.toString();
            Matcher imgURLMatcher = this.imgURLPattern.matcher(urlString);
            String urlStub;
            if (imgURLMatcher.matches()) {
               urlStub = imgURLMatcher.group(1);
            } else {
               urlStub = urlString.split("\\?")[0];
            }

            AtomicInteger ai = (AtomicInteger)this.failuresByURL.get(urlStub);
            if (ai == null) {
               this.failuresByURL.putIfAbsent(urlStub, new AtomicInteger(0));
               ai = (AtomicInteger)this.failuresByURL.get(urlStub);
            }

            ai.incrementAndGet();
         } catch (Exception var6) {
            log.error("Exception recording OPEN_URL failure by URL: e=" + var6, var6);
         }
      }

   }

   public int getTotalAttempts() {
      return this.totalAttempts.get();
   }

   public int getTotalFailures() {
      return this.totalFailures.get();
   }

   public long getTotalSuccessesProcessingTime() {
      return this.totalSuccessesProcessingTime.get();
   }

   public Map<String, Integer> getFailuresByURL() {
      Map<String, Integer> clone = this.cloneMap(this.failuresByURL);
      Map<String, Integer> result = MonitorHelpers.sortDesc(clone, 20);
      if (log.isDebugEnabled()) {
         log.debug("Returning failuresByURL results of size=" + (result != null ? result.size() : "null"));
      }

      return result;
   }

   private Map<String, Integer> cloneMap(ConcurrentMap<String, AtomicInteger> source) {
      HashMap<String, Integer> clone = new HashMap();
      Set<String> keys = source.keySet();
      Iterator i$ = keys.iterator();

      while(i$.hasNext()) {
         String key = (String)i$.next();
         clone.put(key, ((AtomicInteger)source.get(key)).intValue());
      }

      return clone;
   }

   // $FF: synthetic method
   FusionPktOpenURLStats(Object x0) {
      this();
   }

   private static class SingletonHolder {
      public static final FusionPktOpenURLStats INSTANCE = new FusionPktOpenURLStats();
   }
}
