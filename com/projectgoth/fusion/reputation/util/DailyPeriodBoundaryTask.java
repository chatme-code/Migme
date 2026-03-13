package com.projectgoth.fusion.reputation.util;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.reputation.ReputationServiceI;
import java.util.Date;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class DailyPeriodBoundaryTask extends TimerTask {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DailyPeriodBoundaryTask.class));
   private ReputationServiceI reputationService;

   public DailyPeriodBoundaryTask(ReputationServiceI reputationService) {
      this.reputationService = reputationService;
   }

   public void run() {
      Date newDate = DateTimeUtils.midnightToday();

      while(!newDate.after(this.reputationService.getEndOfPeriodObserving())) {
         newDate = DateTimeUtils.midnightToday();

         try {
            Thread.sleep(100L);
         } catch (InterruptedException var3) {
         }
      }

      this.log.info("setting end of observation period " + newDate);
      this.reputationService.setEndOfPeriodObserving(newDate);
   }
}
