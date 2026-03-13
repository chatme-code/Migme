package com.projectgoth.fusion.rewardsystem.instrumentation;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import org.apache.log4j.Logger;

public class MetricsSink {
   public static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MetricsSink.class));
   private static MetricsSink defaultInstance = resetToDefaultInstance();
   public static final MetricsSink NULL_METRICS_SINK = new MetricsSink() {
      protected final void writeSample(Sample sample) throws Throwable {
      }
   };

   public static MetricsSink getInstance() {
      return defaultInstance;
   }

   protected MetricsSink() {
   }

   public final void write(Sample sample) {
      try {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_TRIGGER_METRICS_LOGGING)) {
            this.writeSample(sample);
         }
      } catch (Throwable var3) {
         log.warn("Unable to log sample [" + sample + "].Exception:" + var3, var3);
      }

   }

   protected void writeSample(Sample sample) throws Throwable {
   }

   public static MetricsSink resetToDefaultInstance() {
      defaultInstance = SummarizingMetricsSink.getInstance();
      return defaultInstance;
   }

   public static MetricsSink setInstance(MetricsSink instance) {
      MetricsSink oldInstance = defaultInstance;
      defaultInstance = instance;
      return oldInstance;
   }
}
