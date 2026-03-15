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

public class RegistryStats extends BaseServiceStats {
   private static ObjectFactory _factory = new RegistryStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::RegistryStats"};
   public int numUserProxies;
   public int maxUserProxies;
   public int numConnectionProxies;
   public int maxConnectionProxies;
   public int numChatRoomProxies;
   public int maxChatRoomProxies;
   public int numGroupChatProxies;
   public int maxGroupChatProxies;
   public String objectCaches;
   public String otherRegistries;
   public float requestsPerSecond;
   public float maxRequestsPerSecond;

   public RegistryStats() {
   }

   public RegistryStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, int numUserProxies, int maxUserProxies, int numConnectionProxies, int maxConnectionProxies, int numChatRoomProxies, int maxChatRoomProxies, int numGroupChatProxies, int maxGroupChatProxies, String objectCaches, String otherRegistries, float requestsPerSecond, float maxRequestsPerSecond) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.numUserProxies = numUserProxies;
      this.maxUserProxies = maxUserProxies;
      this.numConnectionProxies = numConnectionProxies;
      this.maxConnectionProxies = maxConnectionProxies;
      this.numChatRoomProxies = numChatRoomProxies;
      this.maxChatRoomProxies = maxChatRoomProxies;
      this.numGroupChatProxies = numGroupChatProxies;
      this.maxGroupChatProxies = maxGroupChatProxies;
      this.objectCaches = objectCaches;
      this.otherRegistries = otherRegistries;
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
      __os.writeInt(this.numUserProxies);
      __os.writeInt(this.maxUserProxies);
      __os.writeInt(this.numConnectionProxies);
      __os.writeInt(this.maxConnectionProxies);
      __os.writeInt(this.numChatRoomProxies);
      __os.writeInt(this.maxChatRoomProxies);
      __os.writeInt(this.numGroupChatProxies);
      __os.writeInt(this.maxGroupChatProxies);
      __os.writeString(this.objectCaches);
      __os.writeString(this.otherRegistries);
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
      this.numUserProxies = __is.readInt();
      this.maxUserProxies = __is.readInt();
      this.numConnectionProxies = __is.readInt();
      this.maxConnectionProxies = __is.readInt();
      this.numChatRoomProxies = __is.readInt();
      this.maxChatRoomProxies = __is.readInt();
      this.numGroupChatProxies = __is.readInt();
      this.maxGroupChatProxies = __is.readInt();
      this.objectCaches = __is.readString();
      this.otherRegistries = __is.readString();
      this.requestsPerSecond = __is.readFloat();
      this.maxRequestsPerSecond = __is.readFloat();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::RegistryStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::RegistryStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(RegistryStats.ice_staticId());

         return new RegistryStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
