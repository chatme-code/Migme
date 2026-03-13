package com.projectgoth.fusion.rewardsystem.instrumentation;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.Collection;
import org.apache.log4j.Logger;

public class SampleSummarySink {
   public static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SampleSummarySink.class));
   private static SampleSummarySink defaultInstance = resetToDefaultInstance();
   public static final SampleSummarySink NULL_SAMPLE_SUMMARY_SINK = new SampleSummarySink() {
      protected boolean writeSummary(Collection<SampleSummary> sampleSummary) throws Throwable {
         return true;
      }
   };

   public static SampleSummarySink getInstance() {
      return defaultInstance;
   }

   protected SampleSummarySink() {
   }

   public final boolean write(Collection<SampleSummary> sampleSummary) {
      try {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_TRIGGER_METRICS_LOGGING)) {
            this.writeSummary(sampleSummary);
         }

         return true;
      } catch (Throwable var3) {
         log.warn("Unable to write summary [" + sampleSummary + "].Exception:" + var3, var3);
         return false;
      }
   }

   protected boolean writeSummary(Collection<SampleSummary> sampleSummary) throws Throwable {
      return true;
   }

   public static SampleSummarySink resetToDefaultInstance() {
      defaultInstance = new EJBSampleSummarySink();
      return defaultInstance;
   }

   public static SampleSummarySink setInstance(SampleSummarySink instance) {
      SampleSummarySink oldInstance = defaultInstance;
      defaultInstance = instance;
      return oldInstance;
   }
}
