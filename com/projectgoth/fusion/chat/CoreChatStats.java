package com.projectgoth.fusion.chat;

import com.projectgoth.fusion.chatsync.WallclockTime;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LazyStats;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

public class CoreChatStats extends LazyStats {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CoreChatStats.class));
   private AtomicLong totalMessagePacketsReceived;
   private WallclockTime messagePacketProcessRequestWallclockTime;
   private WallclockTime messagePacketProcessRequestWallclockTimePeak;
   private WallclockTime messagePacketProcessRequestWallclockTimeOffpeak;
   private WallclockTime sendFusionMessageWallclockTime;
   private WallclockTime sendFusionMessageWallclockTimePeak;
   private WallclockTime sendFusionMessageWallclockTimeOffpeak;

   protected boolean isStatsEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.STATS_COLLECTION_ENABLED);
   }

   protected int getStatsIntervalMinutes() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.STATS_COLLECTION_INTERVAL_MINUTES);
   }

   private CoreChatStats() {
      this.totalMessagePacketsReceived = new AtomicLong(0L);
      this.messagePacketProcessRequestWallclockTime = new WallclockTime();
      this.messagePacketProcessRequestWallclockTimePeak = new WallclockTime();
      this.messagePacketProcessRequestWallclockTimeOffpeak = new WallclockTime();
      this.sendFusionMessageWallclockTime = new WallclockTime();
      this.sendFusionMessageWallclockTimePeak = new WallclockTime();
      this.sendFusionMessageWallclockTimeOffpeak = new WallclockTime();
   }

   public static CoreChatStats getInstance() {
      return CoreChatStats.SingletonHolder.INSTANCE;
   }

   public void incrementTotalMessagePacketsReceived() {
      if (this.isStatsEnabled()) {
         this.totalMessagePacketsReceived.incrementAndGet();
         this.logStatsPeriodically();
      }

   }

   public void addMessagePacketProcessRequestWallclockTime(long millis) {
      if (this.isStatsEnabled()) {
         this.messagePacketProcessRequestWallclockTime.addRequest(millis);
         if (this.isPeakTime()) {
            this.messagePacketProcessRequestWallclockTimePeak.addRequest(millis);
         } else {
            this.messagePacketProcessRequestWallclockTimeOffpeak.addRequest(millis);
         }

         this.logStatsPeriodically();
      }

   }

   public void addSendFusionMessageWallclockTime(long millis) {
      if (this.isStatsEnabled()) {
         this.sendFusionMessageWallclockTime.addRequest(millis);
         if (this.isPeakTime()) {
            this.sendFusionMessageWallclockTimePeak.addRequest(millis);
         } else {
            this.sendFusionMessageWallclockTimeOffpeak.addRequest(millis);
         }

         this.logStatsPeriodically();
      }

   }

   protected void doLog() {
      log.info("CoreChat: stats");
      log.info("CoreChat: total MESSAGE packets received=" + this.totalMessagePacketsReceived.get());
      log.info("CoreChat: MESSAGE.processRequest wallclock time=" + this.messagePacketProcessRequestWallclockTime);
      log.info("CoreChat: MESSAGE.processRequest wallclock time (peak)=" + this.messagePacketProcessRequestWallclockTimePeak);
      log.info("CoreChat: MESSAGE.processRequest wallclock time (offpeak)=" + this.messagePacketProcessRequestWallclockTimeOffpeak);
      log.info("CoreChat: sendFusionMessage wallclock time=" + this.sendFusionMessageWallclockTime);
      log.info("CoreChat: sendFusionMessage wallclock time (peak)=" + this.sendFusionMessageWallclockTimePeak);
      log.info("CoreChat: sendFusionMessage wallclock time (offpeak)=" + this.sendFusionMessageWallclockTimeOffpeak);
   }

   // $FF: synthetic method
   CoreChatStats(Object x0) {
      this();
   }

   private static class SingletonHolder {
      public static final CoreChatStats INSTANCE = new CoreChatStats();
   }
}
