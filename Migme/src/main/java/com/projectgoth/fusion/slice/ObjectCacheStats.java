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

public class ObjectCacheStats extends BaseServiceStats {
   private static ObjectFactory _factory = new ObjectCacheStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::ObjectCacheStats"};
   public int numUserObjects;
   public int maxUserObjects;
   public int numOnlineUserObjects;
   public int maxOnlineUserObjects;
   public int numSessionObjects;
   public int maxSessionObjects;
   public int numChatRoomObjects;
   public int maxChatRoomObjects;
   public int numGroupChatObjects;
   public int maxGroupChatObjects;
   public int distributionServiceQueueSize;
   public int stadiumDistributionServiceQueueSize;
   public float requestsPerSecond;
   public float maxRequestsPerSecond;
   public long eldestUserObject;
   public int weightage;
   public int numSessionsInChatrooms;
   public int numSessionsInGroupChats;
   public int totalChatUserSessionsIntermediateFails;
   public int totalChatUserSessionsFinalFails;

   public ObjectCacheStats() {
   }

   public ObjectCacheStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, int numUserObjects, int maxUserObjects, int numOnlineUserObjects, int maxOnlineUserObjects, int numSessionObjects, int maxSessionObjects, int numChatRoomObjects, int maxChatRoomObjects, int numGroupChatObjects, int maxGroupChatObjects, int distributionServiceQueueSize, int stadiumDistributionServiceQueueSize, float requestsPerSecond, float maxRequestsPerSecond, long eldestUserObject, int weightage, int numSessionsInChatrooms, int numSessionsInGroupChats, int totalChatUserSessionsIntermediateFails, int totalChatUserSessionsFinalFails) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.numUserObjects = numUserObjects;
      this.maxUserObjects = maxUserObjects;
      this.numOnlineUserObjects = numOnlineUserObjects;
      this.maxOnlineUserObjects = maxOnlineUserObjects;
      this.numSessionObjects = numSessionObjects;
      this.maxSessionObjects = maxSessionObjects;
      this.numChatRoomObjects = numChatRoomObjects;
      this.maxChatRoomObjects = maxChatRoomObjects;
      this.numGroupChatObjects = numGroupChatObjects;
      this.maxGroupChatObjects = maxGroupChatObjects;
      this.distributionServiceQueueSize = distributionServiceQueueSize;
      this.stadiumDistributionServiceQueueSize = stadiumDistributionServiceQueueSize;
      this.requestsPerSecond = requestsPerSecond;
      this.maxRequestsPerSecond = maxRequestsPerSecond;
      this.eldestUserObject = eldestUserObject;
      this.weightage = weightage;
      this.numSessionsInChatrooms = numSessionsInChatrooms;
      this.numSessionsInGroupChats = numSessionsInGroupChats;
      this.totalChatUserSessionsIntermediateFails = totalChatUserSessionsIntermediateFails;
      this.totalChatUserSessionsFinalFails = totalChatUserSessionsFinalFails;
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
      __os.writeInt(this.numUserObjects);
      __os.writeInt(this.maxUserObjects);
      __os.writeInt(this.numOnlineUserObjects);
      __os.writeInt(this.maxOnlineUserObjects);
      __os.writeInt(this.numSessionObjects);
      __os.writeInt(this.maxSessionObjects);
      __os.writeInt(this.numChatRoomObjects);
      __os.writeInt(this.maxChatRoomObjects);
      __os.writeInt(this.numGroupChatObjects);
      __os.writeInt(this.maxGroupChatObjects);
      __os.writeInt(this.distributionServiceQueueSize);
      __os.writeInt(this.stadiumDistributionServiceQueueSize);
      __os.writeFloat(this.requestsPerSecond);
      __os.writeFloat(this.maxRequestsPerSecond);
      __os.writeLong(this.eldestUserObject);
      __os.writeInt(this.weightage);
      __os.writeInt(this.numSessionsInChatrooms);
      __os.writeInt(this.numSessionsInGroupChats);
      __os.writeInt(this.totalChatUserSessionsIntermediateFails);
      __os.writeInt(this.totalChatUserSessionsFinalFails);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.numUserObjects = __is.readInt();
      this.maxUserObjects = __is.readInt();
      this.numOnlineUserObjects = __is.readInt();
      this.maxOnlineUserObjects = __is.readInt();
      this.numSessionObjects = __is.readInt();
      this.maxSessionObjects = __is.readInt();
      this.numChatRoomObjects = __is.readInt();
      this.maxChatRoomObjects = __is.readInt();
      this.numGroupChatObjects = __is.readInt();
      this.maxGroupChatObjects = __is.readInt();
      this.distributionServiceQueueSize = __is.readInt();
      this.stadiumDistributionServiceQueueSize = __is.readInt();
      this.requestsPerSecond = __is.readFloat();
      this.maxRequestsPerSecond = __is.readFloat();
      this.eldestUserObject = __is.readLong();
      this.weightage = __is.readInt();
      this.numSessionsInChatrooms = __is.readInt();
      this.numSessionsInGroupChats = __is.readInt();
      this.totalChatUserSessionsIntermediateFails = __is.readInt();
      this.totalChatUserSessionsFinalFails = __is.readInt();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::ObjectCacheStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::ObjectCacheStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(ObjectCacheStats.ice_staticId());

         return new ObjectCacheStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
