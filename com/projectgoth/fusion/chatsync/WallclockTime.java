package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

public class WallclockTime {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(WallclockTime.class));
   public static final int MAX_SAMPLE = 400;
   public static final int BUCKETS_PER_SECOND = 20;
   private AtomicLong totalRequestsProcessed = new AtomicLong(0L);
   private AtomicLong totalWallclockTime = new AtomicLong(0L);
   private AtomicLong sum_x2 = new AtomicLong(0L);
   private AtomicLong maxWallclockTime = new AtomicLong(0L);
   private AtomicLong samplesOutsideFreqDistrib = new AtomicLong(0L);
   private ConcurrentHashMap<Integer, AtomicInteger> freqDistrib = new ConcurrentHashMap();

   public WallclockTime() {
      for(int i = 0; i < 400; ++i) {
         this.freqDistrib.put(i, new AtomicInteger(0));
      }

   }

   public void addRequest(long processingTimeMillis) {
      this.totalRequestsProcessed.incrementAndGet();
      this.totalWallclockTime.addAndGet(processingTimeMillis);
      this.sum_x2.addAndGet(processingTimeMillis * processingTimeMillis);
      if (processingTimeMillis > this.maxWallclockTime.get()) {
         synchronized(this) {
            if (processingTimeMillis > this.maxWallclockTime.get()) {
               this.maxWallclockTime.set(processingTimeMillis);
            }
         }
      }

      int seconds = (int)((double)processingTimeMillis / 50.0D);
      if (seconds >= 400) {
         log.warn("Sample exceeded maximum allowed size for frequency distribution:  value=" + processingTimeMillis + " millis");
         this.samplesOutsideFreqDistrib.incrementAndGet();
      } else {
         AtomicInteger freq = (AtomicInteger)this.freqDistrib.get(seconds);
         freq.incrementAndGet();
      }

   }

   public double getAverageWallclockTime() {
      long reqs = this.totalRequestsProcessed.get();
      return reqs != 0L ? (double)this.totalWallclockTime.get() / (double)reqs / 1000.0D : 0.0D;
   }

   public double getRunningStdev() {
      double mean = this.getAverageWallclockTime() * 1000.0D;
      double n = (double)this.totalRequestsProcessed.get();
      double stdevMillis = Math.sqrt((double)this.sum_x2.get() / n - mean * mean);
      return stdevMillis / 1000.0D;
   }

   public double getMaxWallclockTime() {
      return (double)this.maxWallclockTime.get() / 1000.0D;
   }

   public double getTotalWallclockTime() {
      return (double)this.totalWallclockTime.get() / 1000.0D;
   }

   public WallclockTime.WallclockTimeStats getStats() {
      HashMap<Integer, AtomicInteger> snapshot = new HashMap(this.freqDistrib);
      Set<Integer> buckets = snapshot.keySet();
      WallclockTime.WallclockTimeStats stats = new WallclockTime.WallclockTimeStats();
      int totalN = 0;

      int floor;
      for(Iterator i$ = buckets.iterator(); i$.hasNext(); totalN += ((AtomicInteger)snapshot.get(floor)).get()) {
         floor = (Integer)i$.next();
      }

      int sigmaF = 0;
      double sigmaXF = 0.0D;
      Iterator i$ = buckets.iterator();

      double midpoint;
      while(i$.hasNext()) {
         int floor = (Integer)i$.next();
         midpoint = this.makeMidpoint(floor);
         int f = ((AtomicInteger)snapshot.get(floor)).get();
         sigmaF += f;
         sigmaXF += (double)f * midpoint;
         double n = (double)sigmaF - (double)f / 2.0D;
         double percentile = n / (double)totalN;
         if (percentile >= 0.99D) {
            stats.setPercentile99th(midpoint);
         }

         if (percentile >= 0.95D) {
            stats.setPercentile95th(midpoint);
         }

         if (percentile >= 0.9D) {
            stats.setPercentile90th(midpoint);
         }

         if (percentile >= 0.8D) {
            stats.setPercentile80th(midpoint);
         }

         if (percentile >= 0.7D) {
            stats.setPercentile70th(midpoint);
         }

         if (percentile >= 0.6D) {
            stats.setPercentile60th(midpoint);
         }
      }

      double freqDistribMean = sigmaXF / (double)sigmaF;
      stats.setFreqDistribMean(freqDistribMean);
      midpoint = 0.0D;

      double delta;
      int floor;
      for(Iterator i$ = buckets.iterator(); i$.hasNext(); midpoint += delta * delta * (double)((AtomicInteger)snapshot.get(floor)).get()) {
         floor = (Integer)i$.next();
         double midpoint = this.makeMidpoint(floor);
         delta = midpoint - freqDistribMean;
      }

      stats.setStandardDeviation(Math.sqrt(midpoint / (double)totalN));
      return stats;
   }

   private double makeMidpoint(int floorIndex) {
      return ((double)floorIndex + 0.5D) / 20.0D;
   }

   public String toString() {
      return "Mean=" + this.getAverageWallclockTime() + " Max=" + this.getMaxWallclockTime() + this.getStats() + " (No of samples outside the freq distrib=" + this.samplesOutsideFreqDistrib.get() + ")";
   }

   public class WallclockTimeStats {
      private double freqDistribMean;
      private double standardDeviation;
      private Double percentile99th;
      private Double percentile95th;
      private Double percentile90th;
      private Double percentile80th;
      private Double percentile70th;
      private Double percentile60th;

      public void setFreqDistribMean(double mean) {
         this.freqDistribMean = mean;
      }

      public void setStandardDeviation(double sd) {
         this.standardDeviation = sd;
      }

      public double getStandardDeviation() {
         return this.standardDeviation;
      }

      public void setPercentile99th(double d) {
         if (this.percentile99th == null) {
            this.percentile99th = d;
         }

      }

      public Double getPercentile95th() {
         return this.percentile95th;
      }

      public void setPercentile95th(double d) {
         if (this.percentile95th == null) {
            this.percentile95th = d;
         }

      }

      public void setPercentile90th(double d) {
         if (this.percentile90th == null) {
            this.percentile90th = d;
         }

      }

      public void setPercentile80th(double d) {
         if (this.percentile80th == null) {
            this.percentile80th = d;
         }

      }

      public void setPercentile70th(double d) {
         if (this.percentile70th == null) {
            this.percentile70th = d;
         }

      }

      public void setPercentile60th(double d) {
         if (this.percentile60th == null) {
            this.percentile60th = d;
         }

      }

      public String toString() {
         return " mean(from freq distrib) = " + this.freqDistribMean + " standard deviation = " + this.standardDeviation + " 99th percentile = " + this.percentile99th + " 95th percentile = " + this.percentile95th + " 90th percentile = " + this.percentile90th + " 80th percentile = " + this.percentile80th + " 70th percentile = " + this.percentile70th + " 60th percentile = " + this.percentile60th;
      }
   }
}
