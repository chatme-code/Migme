package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;
import java.util.Map;

public class BotHunterStats extends BaseServiceStats {
   private static ObjectFactory _factory = new BotHunterStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::BotHunterStats"};
   public long statsIntervalSeconds;
   public long totalPacketProcessingNanos;
   public long packetsCaptured;
   public double averageProcessingTimePerPacketMicrosec;
   public double packetsPerSecond;
   public long ipsCached;
   public long portsCached;
   public long packetsCached;
   public long totalSequencePairsAnalyzed;
   public long totalSequenceTransitions;
   public long sequenceSuspectPairs;
   public long ratioSuspectPairs;
   public long suspectIPsReported;
   public long suspectPortsReported;

   public BotHunterStats() {
   }

   public BotHunterStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, long statsIntervalSeconds, long totalPacketProcessingNanos, long packetsCaptured, double averageProcessingTimePerPacketMicrosec, double packetsPerSecond, long ipsCached, long portsCached, long packetsCached, long totalSequencePairsAnalyzed, long totalSequenceTransitions, long sequenceSuspectPairs, long ratioSuspectPairs, long suspectIPsReported, long suspectPortsReported) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.statsIntervalSeconds = statsIntervalSeconds;
      this.totalPacketProcessingNanos = totalPacketProcessingNanos;
      this.packetsCaptured = packetsCaptured;
      this.averageProcessingTimePerPacketMicrosec = averageProcessingTimePerPacketMicrosec;
      this.packetsPerSecond = packetsPerSecond;
      this.ipsCached = ipsCached;
      this.portsCached = portsCached;
      this.packetsCached = packetsCached;
      this.totalSequencePairsAnalyzed = totalSequencePairsAnalyzed;
      this.totalSequenceTransitions = totalSequenceTransitions;
      this.sequenceSuspectPairs = sequenceSuspectPairs;
      this.ratioSuspectPairs = ratioSuspectPairs;
      this.suspectIPsReported = suspectIPsReported;
      this.suspectPortsReported = suspectPortsReported;
   }

   public static ObjectFactory ice_factory() {
      return _factory;
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[2];
   }

   public String ice_id(Current __current) {
      return __ids[2];
   }

   public static String ice_staticId() {
      return __ids[2];
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.writeLong(this.statsIntervalSeconds);
      __os.writeLong(this.totalPacketProcessingNanos);
      __os.writeLong(this.packetsCaptured);
      __os.writeDouble(this.averageProcessingTimePerPacketMicrosec);
      __os.writeDouble(this.packetsPerSecond);
      __os.writeLong(this.ipsCached);
      __os.writeLong(this.portsCached);
      __os.writeLong(this.packetsCached);
      __os.writeLong(this.totalSequencePairsAnalyzed);
      __os.writeLong(this.totalSequenceTransitions);
      __os.writeLong(this.sequenceSuspectPairs);
      __os.writeLong(this.ratioSuspectPairs);
      __os.writeLong(this.suspectIPsReported);
      __os.writeLong(this.suspectPortsReported);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.statsIntervalSeconds = __is.readLong();
      this.totalPacketProcessingNanos = __is.readLong();
      this.packetsCaptured = __is.readLong();
      this.averageProcessingTimePerPacketMicrosec = __is.readDouble();
      this.packetsPerSecond = __is.readDouble();
      this.ipsCached = __is.readLong();
      this.portsCached = __is.readLong();
      this.packetsCached = __is.readLong();
      this.totalSequencePairsAnalyzed = __is.readLong();
      this.totalSequenceTransitions = __is.readLong();
      this.sequenceSuspectPairs = __is.readLong();
      this.ratioSuspectPairs = __is.readLong();
      this.suspectIPsReported = __is.readLong();
      this.suspectPortsReported = __is.readLong();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BotHunterStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BotHunterStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(BotHunterStats.ice_staticId());

         return new BotHunterStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
