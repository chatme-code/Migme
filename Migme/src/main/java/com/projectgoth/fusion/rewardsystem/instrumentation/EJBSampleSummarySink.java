package com.projectgoth.fusion.rewardsystem.instrumentation;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.HostUtils;
import com.projectgoth.fusion.common.SingleThreadedBoundedExecutor;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Metrics;
import com.projectgoth.fusion.interfaces.MetricsHome;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class EJBSampleSummarySink extends SampleSummarySink {
   private ExecutorService executor = new SingleThreadedBoundedExecutor() {
      public int getMaxTaskSize() {
         return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.EJB_SAMPLE_SUMMARY_SINK_MAX_TASK_SIZE);
      }
   };

   protected boolean writeSummary(Collection<SampleSummary> sampleSummaries) throws Throwable {
      return this.writeSummary(false, sampleSummaries);
   }

   private static Callable<Boolean> createLoggingTask(final Collection<SampleSummary> sampleSummaries) {
      return new Callable<Boolean>() {
         public Boolean call() throws Exception {
            Metrics metricsEJB = (Metrics)EJBHomeCache.getObject("ejb/Metrics", MetricsHome.class);
            return metricsEJB.logMetricsSampleSummaries(HostUtils.getHostname(), ConfigUtils.getInstanceName(), sampleSummaries);
         }

         public String toString() {
            return "EJBSampleSummary:createLoggingTask(" + sampleSummaries + ")";
         }
      };
   }

   private <T> Future<T> submit(Callable<T> callable) {
      try {
         return this.executor.submit(callable);
      } catch (RejectedExecutionException var3) {
         log.error("EJBSampleSummary isn't able to submit jobs. Dropping Task:[ " + callable + "]");
         return null;
      }
   }

   protected Future<Boolean> asynchWriteSummary(Collection<SampleSummary> sampleSummaries) {
      return this.submit(createLoggingTask(sampleSummaries));
   }

   protected boolean writeSummary(boolean wait, Collection<SampleSummary> sampleSummaries) throws InterruptedException, ExecutionException {
      Future<Boolean> resultFuture = this.submit(createLoggingTask(sampleSummaries));
      if (resultFuture == null) {
         return false;
      } else {
         Boolean opResult = (Boolean)resultFuture.get();
         return opResult == null ? false : opResult;
      }
   }
}
