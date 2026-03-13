package com.projectgoth.fusion.common;

import java.text.DecimalFormat;
import org.apache.log4j.Logger;

public class PerfTimer {
   private static Logger log = Logger.getLogger("PerfTimer");
   private static DecimalFormat DF = new DecimalFormat("#,###,###.##");
   private String name;
   private long startTime = 0L;
   private long lastStartTime = 0L;

   public PerfTimer(String name) {
      this.name = name;
   }

   public PerfTimer start() {
      this.startTime = System.nanoTime();
      this.lastStartTime = this.startTime;
      return this;
   }

   public PerfTimer log(String context) {
      long curTime = System.nanoTime();
      if (log.isInfoEnabled()) {
         log.info(String.format("%s: %s - since-last %s, total %s", this.name, context, DF.format((curTime - this.lastStartTime) / 1000L), DF.format((curTime - this.startTime) / 1000L)));
      }

      this.lastStartTime = curTime;
      return this;
   }

   public PerfTimer end() {
      long curTime = System.nanoTime();
      if (log.isInfoEnabled()) {
         log.info(String.format("%s: END - total %s", this.name, DF.format((curTime - this.startTime) / 1000L)));
      }

      return this;
   }
}
