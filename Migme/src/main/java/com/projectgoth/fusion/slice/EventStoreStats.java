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

public class EventStoreStats extends BaseServiceStats {
   private static ObjectFactory _factory = new EventStoreStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::EventStoreStats"};
   public long generatorEvents;
   public int generatorEventRate;
   public int maxGeneratorEventRate;
   public long events;
   public int eventRate;
   public int maxEventRate;
   public long cacheExpiredEvents;
   public int cacheExpiredEventRate;
   public int maxCacheExpiredEventRate;
   public long persistBufferEvents;
   public int persistBufferRate;
   public int maxPersistBufferRate;
   public int persistBufferSize;
   public int cacheSize;

   public EventStoreStats() {
   }

   public EventStoreStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, long generatorEvents, int generatorEventRate, int maxGeneratorEventRate, long events, int eventRate, int maxEventRate, long cacheExpiredEvents, int cacheExpiredEventRate, int maxCacheExpiredEventRate, long persistBufferEvents, int persistBufferRate, int maxPersistBufferRate, int persistBufferSize, int cacheSize) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.generatorEvents = generatorEvents;
      this.generatorEventRate = generatorEventRate;
      this.maxGeneratorEventRate = maxGeneratorEventRate;
      this.events = events;
      this.eventRate = eventRate;
      this.maxEventRate = maxEventRate;
      this.cacheExpiredEvents = cacheExpiredEvents;
      this.cacheExpiredEventRate = cacheExpiredEventRate;
      this.maxCacheExpiredEventRate = maxCacheExpiredEventRate;
      this.persistBufferEvents = persistBufferEvents;
      this.persistBufferRate = persistBufferRate;
      this.maxPersistBufferRate = maxPersistBufferRate;
      this.persistBufferSize = persistBufferSize;
      this.cacheSize = cacheSize;
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
      __os.writeLong(this.generatorEvents);
      __os.writeInt(this.generatorEventRate);
      __os.writeInt(this.maxGeneratorEventRate);
      __os.writeLong(this.events);
      __os.writeInt(this.eventRate);
      __os.writeInt(this.maxEventRate);
      __os.writeLong(this.cacheExpiredEvents);
      __os.writeInt(this.cacheExpiredEventRate);
      __os.writeInt(this.maxCacheExpiredEventRate);
      __os.writeLong(this.persistBufferEvents);
      __os.writeInt(this.persistBufferRate);
      __os.writeInt(this.maxPersistBufferRate);
      __os.writeInt(this.persistBufferSize);
      __os.writeInt(this.cacheSize);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.generatorEvents = __is.readLong();
      this.generatorEventRate = __is.readInt();
      this.maxGeneratorEventRate = __is.readInt();
      this.events = __is.readLong();
      this.eventRate = __is.readInt();
      this.maxEventRate = __is.readInt();
      this.cacheExpiredEvents = __is.readLong();
      this.cacheExpiredEventRate = __is.readInt();
      this.maxCacheExpiredEventRate = __is.readInt();
      this.persistBufferEvents = __is.readLong();
      this.persistBufferRate = __is.readInt();
      this.maxPersistBufferRate = __is.readInt();
      this.persistBufferSize = __is.readInt();
      this.cacheSize = __is.readInt();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::EventStoreStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::EventStoreStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(EventStoreStats.ice_staticId());

         return new EventStoreStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
