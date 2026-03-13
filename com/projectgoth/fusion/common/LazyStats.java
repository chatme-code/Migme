package com.projectgoth.fusion.common;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

public abstract class LazyStats {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LazyStats.class));
   protected static final double GIGA = 1.0E9D;
   private AtomicLong lastStatsLoggingTime = new AtomicLong(0L);

   protected boolean isPeakTime() {
      int hr = Calendar.getInstance().get(11);
      return hr >= 6 && hr < 18;
   }

   public long getCpuTime() {
      ThreadMXBean bean = ManagementFactory.getThreadMXBean();
      if (!bean.isCurrentThreadCpuTimeSupported()) {
         return 0L;
      } else {
         return !bean.isThreadCpuTimeEnabled() ? 0L : bean.getCurrentThreadCpuTime();
      }
   }

   protected void logStatsPeriodically() {
      if (this.isStatsEnabled() && System.currentTimeMillis() - this.lastStatsLoggingTime.get() > (long)(this.getStatsIntervalMinutes() * 60 * 1000)) {
         synchronized(this) {
            if (System.currentTimeMillis() - this.lastStatsLoggingTime.get() > (long)(this.getStatsIntervalMinutes() * 60 * 1000)) {
               this.doLog();
               this.lastStatsLoggingTime.set(System.currentTimeMillis());
            }
         }
      }

   }

   protected abstract boolean isStatsEnabled();

   protected abstract int getStatsIntervalMinutes();

   protected abstract void doLog();
}
