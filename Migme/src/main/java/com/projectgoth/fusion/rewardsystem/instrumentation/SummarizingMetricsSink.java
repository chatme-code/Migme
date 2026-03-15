package com.projectgoth.fusion.rewardsystem.instrumentation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SummarizingMetricsSink extends MetricsSink {
   private final SampleSummarizer summarizer;

   private SummarizingMetricsSink() {
      this.summarizer = new SampleSummarizer();
   }

   public static SummarizingMetricsSink getInstance() {
      return SummarizingMetricsSink.SingletonHolder.getSummarizingMetricsSink();
   }

   protected void writeSample(Sample sample) throws Throwable {
      this.summarizer.append(sample);
   }

   public boolean flushMetricsSummary(boolean wait) throws ExecutionException, InterruptedException {
      Future<Boolean> flushResult = this.summarizer.flushMetricSummaries();
      if (wait) {
         if (flushResult == null) {
            return false;
         } else {
            Boolean opResult = (Boolean)flushResult.get();
            return opResult == null ? false : opResult;
         }
      } else {
         return flushResult != null;
      }
   }

   // $FF: synthetic method
   SummarizingMetricsSink(Object x0) {
      this();
   }

   private static class SingletonHolder {
      private static final SummarizingMetricsSink summarizingMetricsSink = new SummarizingMetricsSink();

      public static SummarizingMetricsSink getSummarizingMetricsSink() {
         return summarizingMetricsSink;
      }
   }
}
