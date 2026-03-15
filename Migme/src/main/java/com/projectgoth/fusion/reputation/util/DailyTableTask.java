package com.projectgoth.fusion.reputation.util;

import net.spy.memcached.MemCachedClient;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.reputation.cache.ReputationLastRan;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyTableTask extends TimerTask {
   private Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(DailyTableTask.class));
   public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
   public static final String PREFIX = "sessionarchive";
   private MemCachedClient memCached;

   public DailyTableTask(MemCachedClient memCached) {
      this.memCached = memCached;
   }

   public void run() {
      String runDateString = DATE_FORMAT.format(DateTimeUtils.minusDays(new Date(), 1));
      this.log.info("setting session archive table name to sessionarchive" + runDateString);
      ReputationLastRan.setSessionArchiveTableName(this.memCached, "sessionarchive" + runDateString);
      this.log.info("setting session archive id to 0");
      ReputationLastRan.setSessionArchiveLastId(this.memCached, 0);
   }
}
