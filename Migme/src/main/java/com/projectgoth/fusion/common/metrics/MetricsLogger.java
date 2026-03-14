package com.projectgoth.fusion.common.metrics;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import org.slf4j.Logger;

public class MetricsLogger {
   static final Logger metricsLog = LoggerFactory.getLogger("MetricsLog");
   private static final Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(MetricsLogger.class));
   static final String SEPARATOR_CHARACTER = ",";

   public static void log(MetricsEnums.MetricsEntryInterface metricsEntry, String eventID, Object value) {
      try {
         boolean isMetricEntryEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.MetricsLoggingEnabled(metricsEntry)));
         if (isMetricEntryEnabled) {
            metricsLog.info(metricsEntry.getScope() + "," + metricsEntry.getEventName() + "," + eventID + "," + metricsEntry.getMetricName() + "," + metricsEntry.getMetricType() + "," + value);
         }
      } catch (Exception var4) {
         log.error("Unable to log metrics :" + var4.getMessage(), var4);
      }

   }
}
