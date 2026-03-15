package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectFactory;
import Ice.ObjectImpl;
import Ice.OutputStream;
import IceInternal.BasicStream;
import java.util.Arrays;
import java.util.Map;

public class BaseServiceStats extends ObjectImpl {
   private static ObjectFactory _factory = new BaseServiceStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats"};
   public String hostname;
   public String version;
   public long jvmTotalMemory;
   public long jvmFreeMemory;
   public long uptime;
   public long lastUpdatedTime;
   public boolean izeRequestStatsEnabled;
   public long izeRequestCount;
   public Map<String, Long> izeRequestCountByOrigin;
   public Map<String, Long> izeRequestCountByOperation;
   public Map<String, Double> izeRequestMeanProcessingTimeByOperation;
   public Map<String, Double> izeRequestMaxProcessingTimeByOperation;
   public Map<String, Double> izeRequestStdevProcessingTimeByOperation;
   public Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation;
   public Map<String, Double> izeRequestTotalProcessingTimeByOperation;
   public boolean izeThreadStatsEnabled;
   public long izeObjectAdapterThreadPoolRunning;
   public long izeObjectAdapterThreadPoolInUse;
   public long izeObjectAdapterThreadPoolInUseHighWatermark;
   public double izeObjectAdapterThreadPoolLoad;
   public long izeObjectAdapterThreadPoolSize;
   public long izeObjectAdapterThreadPoolSizeMax;
   public long izeObjectAdapterThreadPoolSizeWarn;
   public boolean izeClientThreadPoolStatsEnabled;
   public long izeClientThreadPoolRunning;
   public long izeClientThreadPoolInUse;
   public long izeClientThreadPoolInUseHighWatermark;
   public double izeClientThreadPoolLoad;
   public long izeClientThreadPoolSize;
   public long izeClientThreadPoolSizeMax;
   public long izeClientThreadPoolSizeWarn;
   public long amdObjectAdapterThreadPoolRunning;
   public long amdObjectAdapterThreadPoolInUse;
   public long amdObjectAdapterThreadPoolInUseHighWatermark;
   public long amdObjectAdapterThreadPoolSize;
   public long amdObjectAdapterThreadPoolSizeMax;
   public long amdObjectAdapterThreadPoolQueueLength;
   public String dataGridStats;

   public BaseServiceStats() {
   }

   public BaseServiceStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats) {
      this.hostname = hostname;
      this.version = version;
      this.jvmTotalMemory = jvmTotalMemory;
      this.jvmFreeMemory = jvmFreeMemory;
      this.uptime = uptime;
      this.lastUpdatedTime = lastUpdatedTime;
      this.izeRequestStatsEnabled = izeRequestStatsEnabled;
      this.izeRequestCount = izeRequestCount;
      this.izeRequestCountByOrigin = izeRequestCountByOrigin;
      this.izeRequestCountByOperation = izeRequestCountByOperation;
      this.izeRequestMeanProcessingTimeByOperation = izeRequestMeanProcessingTimeByOperation;
      this.izeRequestMaxProcessingTimeByOperation = izeRequestMaxProcessingTimeByOperation;
      this.izeRequestStdevProcessingTimeByOperation = izeRequestStdevProcessingTimeByOperation;
      this.izeRequest95thPercentileProcessingTimeByOperation = izeRequest95thPercentileProcessingTimeByOperation;
      this.izeRequestTotalProcessingTimeByOperation = izeRequestTotalProcessingTimeByOperation;
      this.izeThreadStatsEnabled = izeThreadStatsEnabled;
      this.izeObjectAdapterThreadPoolRunning = izeObjectAdapterThreadPoolRunning;
      this.izeObjectAdapterThreadPoolInUse = izeObjectAdapterThreadPoolInUse;
      this.izeObjectAdapterThreadPoolInUseHighWatermark = izeObjectAdapterThreadPoolInUseHighWatermark;
      this.izeObjectAdapterThreadPoolLoad = izeObjectAdapterThreadPoolLoad;
      this.izeObjectAdapterThreadPoolSize = izeObjectAdapterThreadPoolSize;
      this.izeObjectAdapterThreadPoolSizeMax = izeObjectAdapterThreadPoolSizeMax;
      this.izeObjectAdapterThreadPoolSizeWarn = izeObjectAdapterThreadPoolSizeWarn;
      this.izeClientThreadPoolStatsEnabled = izeClientThreadPoolStatsEnabled;
      this.izeClientThreadPoolRunning = izeClientThreadPoolRunning;
      this.izeClientThreadPoolInUse = izeClientThreadPoolInUse;
      this.izeClientThreadPoolInUseHighWatermark = izeClientThreadPoolInUseHighWatermark;
      this.izeClientThreadPoolLoad = izeClientThreadPoolLoad;
      this.izeClientThreadPoolSize = izeClientThreadPoolSize;
      this.izeClientThreadPoolSizeMax = izeClientThreadPoolSizeMax;
      this.izeClientThreadPoolSizeWarn = izeClientThreadPoolSizeWarn;
      this.amdObjectAdapterThreadPoolRunning = amdObjectAdapterThreadPoolRunning;
      this.amdObjectAdapterThreadPoolInUse = amdObjectAdapterThreadPoolInUse;
      this.amdObjectAdapterThreadPoolInUseHighWatermark = amdObjectAdapterThreadPoolInUseHighWatermark;
      this.amdObjectAdapterThreadPoolSize = amdObjectAdapterThreadPoolSize;
      this.amdObjectAdapterThreadPoolSizeMax = amdObjectAdapterThreadPoolSizeMax;
      this.amdObjectAdapterThreadPoolQueueLength = amdObjectAdapterThreadPoolQueueLength;
      this.dataGridStats = dataGridStats;
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
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.writeString(this.hostname);
      __os.writeString(this.version);
      __os.writeLong(this.jvmTotalMemory);
      __os.writeLong(this.jvmFreeMemory);
      __os.writeLong(this.uptime);
      __os.writeLong(this.lastUpdatedTime);
      __os.writeBool(this.izeRequestStatsEnabled);
      __os.writeLong(this.izeRequestCount);
      LongStatsMapHelper.write(__os, this.izeRequestCountByOrigin);
      LongStatsMapHelper.write(__os, this.izeRequestCountByOperation);
      DoubleStatsMapHelper.write(__os, this.izeRequestMeanProcessingTimeByOperation);
      DoubleStatsMapHelper.write(__os, this.izeRequestMaxProcessingTimeByOperation);
      DoubleStatsMapHelper.write(__os, this.izeRequestStdevProcessingTimeByOperation);
      DoubleStatsMapHelper.write(__os, this.izeRequest95thPercentileProcessingTimeByOperation);
      DoubleStatsMapHelper.write(__os, this.izeRequestTotalProcessingTimeByOperation);
      __os.writeBool(this.izeThreadStatsEnabled);
      __os.writeLong(this.izeObjectAdapterThreadPoolRunning);
      __os.writeLong(this.izeObjectAdapterThreadPoolInUse);
      __os.writeLong(this.izeObjectAdapterThreadPoolInUseHighWatermark);
      __os.writeDouble(this.izeObjectAdapterThreadPoolLoad);
      __os.writeLong(this.izeObjectAdapterThreadPoolSize);
      __os.writeLong(this.izeObjectAdapterThreadPoolSizeMax);
      __os.writeLong(this.izeObjectAdapterThreadPoolSizeWarn);
      __os.writeBool(this.izeClientThreadPoolStatsEnabled);
      __os.writeLong(this.izeClientThreadPoolRunning);
      __os.writeLong(this.izeClientThreadPoolInUse);
      __os.writeLong(this.izeClientThreadPoolInUseHighWatermark);
      __os.writeDouble(this.izeClientThreadPoolLoad);
      __os.writeLong(this.izeClientThreadPoolSize);
      __os.writeLong(this.izeClientThreadPoolSizeMax);
      __os.writeLong(this.izeClientThreadPoolSizeWarn);
      __os.writeLong(this.amdObjectAdapterThreadPoolRunning);
      __os.writeLong(this.amdObjectAdapterThreadPoolInUse);
      __os.writeLong(this.amdObjectAdapterThreadPoolInUseHighWatermark);
      __os.writeLong(this.amdObjectAdapterThreadPoolSize);
      __os.writeLong(this.amdObjectAdapterThreadPoolSizeMax);
      __os.writeLong(this.amdObjectAdapterThreadPoolQueueLength);
      __os.writeString(this.dataGridStats);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.hostname = __is.readString();
      this.version = __is.readString();
      this.jvmTotalMemory = __is.readLong();
      this.jvmFreeMemory = __is.readLong();
      this.uptime = __is.readLong();
      this.lastUpdatedTime = __is.readLong();
      this.izeRequestStatsEnabled = __is.readBool();
      this.izeRequestCount = __is.readLong();
      this.izeRequestCountByOrigin = LongStatsMapHelper.read(__is);
      this.izeRequestCountByOperation = LongStatsMapHelper.read(__is);
      this.izeRequestMeanProcessingTimeByOperation = DoubleStatsMapHelper.read(__is);
      this.izeRequestMaxProcessingTimeByOperation = DoubleStatsMapHelper.read(__is);
      this.izeRequestStdevProcessingTimeByOperation = DoubleStatsMapHelper.read(__is);
      this.izeRequest95thPercentileProcessingTimeByOperation = DoubleStatsMapHelper.read(__is);
      this.izeRequestTotalProcessingTimeByOperation = DoubleStatsMapHelper.read(__is);
      this.izeThreadStatsEnabled = __is.readBool();
      this.izeObjectAdapterThreadPoolRunning = __is.readLong();
      this.izeObjectAdapterThreadPoolInUse = __is.readLong();
      this.izeObjectAdapterThreadPoolInUseHighWatermark = __is.readLong();
      this.izeObjectAdapterThreadPoolLoad = __is.readDouble();
      this.izeObjectAdapterThreadPoolSize = __is.readLong();
      this.izeObjectAdapterThreadPoolSizeMax = __is.readLong();
      this.izeObjectAdapterThreadPoolSizeWarn = __is.readLong();
      this.izeClientThreadPoolStatsEnabled = __is.readBool();
      this.izeClientThreadPoolRunning = __is.readLong();
      this.izeClientThreadPoolInUse = __is.readLong();
      this.izeClientThreadPoolInUseHighWatermark = __is.readLong();
      this.izeClientThreadPoolLoad = __is.readDouble();
      this.izeClientThreadPoolSize = __is.readLong();
      this.izeClientThreadPoolSizeMax = __is.readLong();
      this.izeClientThreadPoolSizeWarn = __is.readLong();
      this.amdObjectAdapterThreadPoolRunning = __is.readLong();
      this.amdObjectAdapterThreadPoolInUse = __is.readLong();
      this.amdObjectAdapterThreadPoolInUseHighWatermark = __is.readLong();
      this.amdObjectAdapterThreadPoolSize = __is.readLong();
      this.amdObjectAdapterThreadPoolSizeMax = __is.readLong();
      this.amdObjectAdapterThreadPoolQueueLength = __is.readLong();
      this.dataGridStats = __is.readString();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BaseServiceStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::BaseServiceStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(BaseServiceStats.ice_staticId());

         return new BaseServiceStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
