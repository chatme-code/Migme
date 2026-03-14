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

public class BotServiceStats extends BaseServiceStats {
   private static ObjectFactory _factory = new BotServiceStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::BotServiceStats"};
   public int numBotObjects;
   public int maxBotObjects;
   public int numBotChannelObjects;
   public int maxBotChannelObjects;
   public int threadPoolSize;
   public int maxThreadPoolSize;
   public int threadPoolQueueSize;
   public int maxThreadPoolQueueSize;
   public float requestsPerSecond;
   public float maxRequestsPerSecond;

   public BotServiceStats() {
   }

   public BotServiceStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, int numBotObjects, int maxBotObjects, int numBotChannelObjects, int maxBotChannelObjects, int threadPoolSize, int maxThreadPoolSize, int threadPoolQueueSize, int maxThreadPoolQueueSize, float requestsPerSecond, float maxRequestsPerSecond) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.numBotObjects = numBotObjects;
      this.maxBotObjects = maxBotObjects;
      this.numBotChannelObjects = numBotChannelObjects;
      this.maxBotChannelObjects = maxBotChannelObjects;
      this.threadPoolSize = threadPoolSize;
      this.maxThreadPoolSize = maxThreadPoolSize;
      this.threadPoolQueueSize = threadPoolQueueSize;
      this.maxThreadPoolQueueSize = maxThreadPoolQueueSize;
      this.requestsPerSecond = requestsPerSecond;
      this.maxRequestsPerSecond = maxRequestsPerSecond;
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
      __os.writeInt(this.numBotObjects);
      __os.writeInt(this.maxBotObjects);
      __os.writeInt(this.numBotChannelObjects);
      __os.writeInt(this.maxBotChannelObjects);
      __os.writeInt(this.threadPoolSize);
      __os.writeInt(this.maxThreadPoolSize);
      __os.writeInt(this.threadPoolQueueSize);
      __os.writeInt(this.maxThreadPoolQueueSize);
      __os.writeFloat(this.requestsPerSecond);
      __os.writeFloat(this.maxRequestsPerSecond);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.numBotObjects = __is.readInt();
      this.maxBotObjects = __is.readInt();
      this.numBotChannelObjects = __is.readInt();
      this.maxBotChannelObjects = __is.readInt();
      this.threadPoolSize = __is.readInt();
      this.maxThreadPoolSize = __is.readInt();
      this.threadPoolQueueSize = __is.readInt();
      this.maxThreadPoolQueueSize = __is.readInt();
      this.requestsPerSecond = __is.readFloat();
      this.maxRequestsPerSecond = __is.readFloat();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BotServiceStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BotServiceStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(BotServiceStats.ice_staticId());

         return new BotServiceStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
