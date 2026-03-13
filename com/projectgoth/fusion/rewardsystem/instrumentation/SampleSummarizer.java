package com.projectgoth.fusion.rewardsystem.instrumentation;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SingleThreadedBoundedExecutor;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class SampleSummarizer {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SampleSummarizer.class));
   private Map<SampleCategory, SampleSummary> summaryMap = new HashMap();
   private final ExecutorService executor = new SingleThreadedBoundedExecutor() {
      public int getMaxTaskSize() {
         return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.SAMPLE_SUMMARIZER_MAX_TASK_SIZE);
      }
   };
   private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

   private SampleSummary putOrGetSummary(SampleCategory category) {
      SampleSummary summary = (SampleSummary)this.summaryMap.get(category);
      if (summary == null) {
         SampleSummary newSummary = new SampleSummary(generateSummaryID(), category);
         this.summaryMap.put(category, newSummary);
         return newSummary;
      } else {
         return summary;
      }
   }

   private static String generateSummaryID() {
      return UUID.randomUUID().toString();
   }

   private void appendSampleToSummary(Sample sample) {
      if (log.isDebugEnabled()) {
         log.debug("appendSampleToSummary:sample :[" + sample + "]");
      }

      SampleCategory category = sample.getSampleCategory();
      SampleSummary summary = this.putOrGetSummary(category);
      summary.append(sample);
   }

   private void flushSummaries() throws IOException {
      if (log.isDebugEnabled()) {
         log.debug("flushSummaries:Summary map : " + this.summaryMap);
      }

      if (!this.summaryMap.isEmpty()) {
         log.info("Flushing " + this.summaryMap.size() + " metrics summaries");
         SampleSummarySink.getInstance().write(new ArrayList(this.summaryMap.values()));
         this.summaryMap.clear();
      }

   }

   protected Callable<Boolean> createSampleAppenderTask(final Sample sample) {
      return new Callable<Boolean>() {
         public Boolean call() throws Exception {
            SampleSummarizer.this.appendSampleToSummary(sample);
            return Boolean.TRUE;
         }

         public String toString() {
            return "Callable:appendSampleToSummary(Sample:[category=" + sample.getSampleCategory() + "])";
         }
      };
   }

   private Callable<Boolean> createFlushMetricsTask() {
      return new Callable<Boolean>() {
         public Boolean call() throws Exception {
            SampleSummarizer.this.flushSummaries();
            return Boolean.TRUE;
         }

         public String toString() {
            return "Callable:flushSummaries()";
         }
      };
   }

   public SampleSummarizer() {
      this.schedulePeriodicalFlush();
   }

   private void schedulePeriodicalFlush() {
      this.scheduledExecutor.schedule(new SampleSummarizer.PeriodicalFlushMetricsTriggerTask(), 60L, TimeUnit.SECONDS);
   }

   private <T> Future<T> submit(Callable<T> callable) {
      try {
         return this.executor.submit(callable);
      } catch (RejectedExecutionException var3) {
         log.error("Sample summarizer isn't able to submit jobs. Dropping Task:[ " + callable + "]");
         return null;
      }
   }

   public Future<Boolean> append(Sample sample) {
      return this.submit(this.createSampleAppenderTask(sample));
   }

   public Future<Boolean> flushMetricSummaries() {
      return this.submit(this.createFlushMetricsTask());
   }

   public void destroy() {
      this.executor.shutdown();
   }

   private class PeriodicalFlushMetricsTriggerTask implements Runnable {
      private PeriodicalFlushMetricsTriggerTask() {
      }

      public void run() {
         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_TRIGGER_METRICS_LOGGING) && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_SAMPLE_SUMMARY_PERIODICAL_FLUSH)) {
               SampleSummarizer.this.submit(SampleSummarizer.this.createFlushMetricsTask());
            }
         } finally {
            SampleSummarizer.this.schedulePeriodicalFlush();
         }

      }

      // $FF: synthetic method
      PeriodicalFlushMetricsTriggerTask(Object x1) {
         this();
      }
   }
}
