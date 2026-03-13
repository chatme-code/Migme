package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.slice.BaseServiceStats;
import com.projectgoth.fusion.slice.BotHunterStats;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StatsCollector {
   private static StatsCollector instance;
   private long startTime = System.currentTimeMillis();
   private BotHunterStats stats = new BotHunterStats();
   private final Object DUMMY_VALUE = new Object();
   private ConcurrentHashMap<String, Object> distinctClientIPs = new ConcurrentHashMap();
   private ConcurrentHashMap<String, Object> distinctSuspects = new ConcurrentHashMap();
   private AtomicLong totalSequencePairsAnalyzed = new AtomicLong(0L);
   private AtomicLong totalSequenceTransitions = new AtomicLong(0L);
   private AtomicLong sequenceSuspectPairs = new AtomicLong(0L);
   private AtomicLong ratioSuspectPairs = new AtomicLong(0L);

   private StatsCollector() {
   }

   public static synchronized StatsCollector getInstance() {
      if (instance == null) {
         instance = new StatsCollector();
      }

      return instance;
   }

   public void setInstanceStats(long statsIntervalSeconds) {
      this.stats.statsIntervalSeconds = statsIntervalSeconds;
   }

   public void updatePacketCaptureStats(long totalPacketProcessingNanos, long packetsCaptured, double averageProcessingTimePerPacketMicrosec, double packetsPerSecond, long ipsCached, long portsCached, long packetsCached) {
      this.stats.totalPacketProcessingNanos = totalPacketProcessingNanos;
      this.stats.packetsCaptured = packetsCaptured;
      this.stats.averageProcessingTimePerPacketMicrosec = averageProcessingTimePerPacketMicrosec;
      this.stats.packetsPerSecond = packetsPerSecond;
      this.stats.ipsCached = ipsCached;
      this.stats.portsCached = portsCached;
      this.stats.packetsCached = packetsCached;
   }

   public void addSuspects(Set<Suspect> suspects) {
      Iterator i$ = suspects.iterator();

      while(i$.hasNext()) {
         Suspect s = (Suspect)i$.next();
         this.distinctClientIPs.put(s.getClientIP(), this.DUMMY_VALUE);
         this.distinctSuspects.put(s.toString(), this.DUMMY_VALUE);
      }

   }

   public int getDistinctIPsCount() {
      return this.distinctClientIPs.keySet().size();
   }

   public int getDistinctSuspectsCount() {
      return this.distinctSuspects.keySet().size();
   }

   public void incrementTotalSequencePairsAnalyzed() {
      this.totalSequencePairsAnalyzed.incrementAndGet();
   }

   public void incrementTotalSequenceTransitions(long lDiff) {
      this.totalSequenceTransitions.addAndGet(lDiff);
   }

   public void incrementSequenceSuspectPairs() {
      this.sequenceSuspectPairs.incrementAndGet();
   }

   public void incrementRatioSuspectPairs() {
      this.ratioSuspectPairs.incrementAndGet();
   }

   public BotHunterStats getStats() {
      BaseServiceStats bss = ServiceStatsFactory.getBaseServiceStats(this.startTime);
      this.stats.jvmFreeMemory = bss.jvmFreeMemory;
      this.stats.jvmTotalMemory = bss.jvmTotalMemory;
      this.stats.uptime = bss.uptime;
      this.stats.totalSequencePairsAnalyzed = this.totalSequencePairsAnalyzed.get();
      this.stats.totalSequenceTransitions = this.totalSequenceTransitions.get();
      this.stats.sequenceSuspectPairs = this.sequenceSuspectPairs.get();
      this.stats.ratioSuspectPairs = this.ratioSuspectPairs.get();
      this.stats.suspectIPsReported = (long)this.distinctClientIPs.keySet().size();
      this.stats.suspectPortsReported = (long)this.distinctSuspects.keySet().size();
      return this.stats;
   }
}
