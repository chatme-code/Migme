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

public class GatewayStats extends BaseServiceStats {
   private static ObjectFactory _factory = new GatewayStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::GatewayStats"};
   public String serverType;
   public int port;
   public int numConnectionObjects;
   public int maxConnectionObjects;
   public GatewayThreadPoolStats[] threadPoolStats;
   public int connectionsRejected;
   public int timesTooBusy;
   public long lastTimeTooBusy;
   public boolean tooBusy;
   public float connectionsPerRemoteIP;
   public int openUrlAttempts;
   public int openUrlFailures;
   public float openUrlFailurePercent;
   public float averageSuccessfulProcessingTimeSeconds;
   public Map<String, Integer> openUrlFailuresByUrl;

   public GatewayStats() {
   }

   public GatewayStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, String serverType, int port, int numConnectionObjects, int maxConnectionObjects, GatewayThreadPoolStats[] threadPoolStats, int connectionsRejected, int timesTooBusy, long lastTimeTooBusy, boolean tooBusy, float connectionsPerRemoteIP, int openUrlAttempts, int openUrlFailures, float openUrlFailurePercent, float averageSuccessfulProcessingTimeSeconds, Map<String, Integer> openUrlFailuresByUrl) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.serverType = serverType;
      this.port = port;
      this.numConnectionObjects = numConnectionObjects;
      this.maxConnectionObjects = maxConnectionObjects;
      this.threadPoolStats = threadPoolStats;
      this.connectionsRejected = connectionsRejected;
      this.timesTooBusy = timesTooBusy;
      this.lastTimeTooBusy = lastTimeTooBusy;
      this.tooBusy = tooBusy;
      this.connectionsPerRemoteIP = connectionsPerRemoteIP;
      this.openUrlAttempts = openUrlAttempts;
      this.openUrlFailures = openUrlFailures;
      this.openUrlFailurePercent = openUrlFailurePercent;
      this.averageSuccessfulProcessingTimeSeconds = averageSuccessfulProcessingTimeSeconds;
      this.openUrlFailuresByUrl = openUrlFailuresByUrl;
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
      __os.writeString(this.serverType);
      __os.writeInt(this.port);
      __os.writeInt(this.numConnectionObjects);
      __os.writeInt(this.maxConnectionObjects);
      GatewayThreadPoolStatsArrayHelper.write(__os, this.threadPoolStats);
      __os.writeInt(this.connectionsRejected);
      __os.writeInt(this.timesTooBusy);
      __os.writeLong(this.lastTimeTooBusy);
      __os.writeBool(this.tooBusy);
      __os.writeFloat(this.connectionsPerRemoteIP);
      __os.writeInt(this.openUrlAttempts);
      __os.writeInt(this.openUrlFailures);
      __os.writeFloat(this.openUrlFailurePercent);
      __os.writeFloat(this.averageSuccessfulProcessingTimeSeconds);
      IntStatsMapHelper.write(__os, this.openUrlFailuresByUrl);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.serverType = __is.readString();
      this.port = __is.readInt();
      this.numConnectionObjects = __is.readInt();
      this.maxConnectionObjects = __is.readInt();
      this.threadPoolStats = GatewayThreadPoolStatsArrayHelper.read(__is);
      this.connectionsRejected = __is.readInt();
      this.timesTooBusy = __is.readInt();
      this.lastTimeTooBusy = __is.readLong();
      this.tooBusy = __is.readBool();
      this.connectionsPerRemoteIP = __is.readFloat();
      this.openUrlAttempts = __is.readInt();
      this.openUrlFailures = __is.readInt();
      this.openUrlFailurePercent = __is.readFloat();
      this.averageSuccessfulProcessingTimeSeconds = __is.readFloat();
      this.openUrlFailuresByUrl = IntStatsMapHelper.read(__is);
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::GatewayStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::GatewayStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(GatewayStats.ice_staticId());

         return new GatewayStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
