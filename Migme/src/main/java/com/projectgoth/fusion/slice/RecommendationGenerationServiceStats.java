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

public class RecommendationGenerationServiceStats extends BaseServiceStats {
   private static ObjectFactory _factory = new RecommendationGenerationServiceStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::RecommendationGenerationServiceStats"};
   public long totalJobs;
   public long successfulJobs;
   public long failedJobs;
   public long failedJobsDueToAlreadyRunning;
   public long totalRecommendationsGenerated;
   public long totalPipelinesUsed;
   public long totalGenerationTimeSeconds;
   public long shortestGenerationTimeSeconds;
   public long longestGenerationTimeSeconds;

   public RecommendationGenerationServiceStats() {
   }

   public RecommendationGenerationServiceStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, long totalJobs, long successfulJobs, long failedJobs, long failedJobsDueToAlreadyRunning, long totalRecommendationsGenerated, long totalPipelinesUsed, long totalGenerationTimeSeconds, long shortestGenerationTimeSeconds, long longestGenerationTimeSeconds) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.totalJobs = totalJobs;
      this.successfulJobs = successfulJobs;
      this.failedJobs = failedJobs;
      this.failedJobsDueToAlreadyRunning = failedJobsDueToAlreadyRunning;
      this.totalRecommendationsGenerated = totalRecommendationsGenerated;
      this.totalPipelinesUsed = totalPipelinesUsed;
      this.totalGenerationTimeSeconds = totalGenerationTimeSeconds;
      this.shortestGenerationTimeSeconds = shortestGenerationTimeSeconds;
      this.longestGenerationTimeSeconds = longestGenerationTimeSeconds;
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
      __os.writeLong(this.totalJobs);
      __os.writeLong(this.successfulJobs);
      __os.writeLong(this.failedJobs);
      __os.writeLong(this.failedJobsDueToAlreadyRunning);
      __os.writeLong(this.totalRecommendationsGenerated);
      __os.writeLong(this.totalPipelinesUsed);
      __os.writeLong(this.totalGenerationTimeSeconds);
      __os.writeLong(this.shortestGenerationTimeSeconds);
      __os.writeLong(this.longestGenerationTimeSeconds);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.totalJobs = __is.readLong();
      this.successfulJobs = __is.readLong();
      this.failedJobs = __is.readLong();
      this.failedJobsDueToAlreadyRunning = __is.readLong();
      this.totalRecommendationsGenerated = __is.readLong();
      this.totalPipelinesUsed = __is.readLong();
      this.totalGenerationTimeSeconds = __is.readLong();
      this.shortestGenerationTimeSeconds = __is.readLong();
      this.longestGenerationTimeSeconds = __is.readLong();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::RecommendationGenerationServiceStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::RecommendationGenerationServiceStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(RecommendationGenerationServiceStats.ice_staticId());

         return new RecommendationGenerationServiceStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
