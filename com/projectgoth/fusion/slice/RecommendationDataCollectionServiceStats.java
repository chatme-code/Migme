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

public class RecommendationDataCollectionServiceStats extends BaseServiceStats {
   private static ObjectFactory _factory = new RecommendationDataCollectionServiceStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::BaseServiceStats", "::com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats"};
   public ServiceStatsLongFieldValue totalReceivedDataCount;
   public Map<Integer, ServiceStatsLongFieldValue> totalReceivedDataCountByDataType;
   public ServiceStatsLongFieldValue totalSuccessfullyProcessedDataCount;
   public Map<Integer, ServiceStatsLongFieldValue> totalSuccessfullyProcessedDataCountByDataType;
   public ServiceStatsLongFieldValue totalFailedProcessedDataCount;
   public Map<String, ServiceStatsLongFieldValue> totalFailedProcessedDataCountByErrorCauseCode;

   public RecommendationDataCollectionServiceStats() {
   }

   public RecommendationDataCollectionServiceStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, ServiceStatsLongFieldValue totalReceivedDataCount, Map<Integer, ServiceStatsLongFieldValue> totalReceivedDataCountByDataType, ServiceStatsLongFieldValue totalSuccessfullyProcessedDataCount, Map<Integer, ServiceStatsLongFieldValue> totalSuccessfullyProcessedDataCountByDataType, ServiceStatsLongFieldValue totalFailedProcessedDataCount, Map<String, ServiceStatsLongFieldValue> totalFailedProcessedDataCountByErrorCauseCode) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.totalReceivedDataCount = totalReceivedDataCount;
      this.totalReceivedDataCountByDataType = totalReceivedDataCountByDataType;
      this.totalSuccessfullyProcessedDataCount = totalSuccessfullyProcessedDataCount;
      this.totalSuccessfullyProcessedDataCountByDataType = totalSuccessfullyProcessedDataCountByDataType;
      this.totalFailedProcessedDataCount = totalFailedProcessedDataCount;
      this.totalFailedProcessedDataCountByErrorCauseCode = totalFailedProcessedDataCountByErrorCauseCode;
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
      this.totalReceivedDataCount.__write(__os);
      IntToServiceStatsLongFieldValueMapHelper.write(__os, this.totalReceivedDataCountByDataType);
      this.totalSuccessfullyProcessedDataCount.__write(__os);
      IntToServiceStatsLongFieldValueMapHelper.write(__os, this.totalSuccessfullyProcessedDataCountByDataType);
      this.totalFailedProcessedDataCount.__write(__os);
      StringToServiceStatsLongFieldValueMapHelper.write(__os, this.totalFailedProcessedDataCountByErrorCauseCode);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.totalReceivedDataCount = new ServiceStatsLongFieldValue();
      this.totalReceivedDataCount.__read(__is);
      this.totalReceivedDataCountByDataType = IntToServiceStatsLongFieldValueMapHelper.read(__is);
      this.totalSuccessfullyProcessedDataCount = new ServiceStatsLongFieldValue();
      this.totalSuccessfullyProcessedDataCount.__read(__is);
      this.totalSuccessfullyProcessedDataCountByDataType = IntToServiceStatsLongFieldValueMapHelper.read(__is);
      this.totalFailedProcessedDataCount = new ServiceStatsLongFieldValue();
      this.totalFailedProcessedDataCount.__read(__is);
      this.totalFailedProcessedDataCountByErrorCauseCode = StringToServiceStatsLongFieldValueMapHelper.read(__is);
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::RecommendationDataCollectionServiceStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(RecommendationDataCollectionServiceStats.ice_staticId());

         return new RecommendationDataCollectionServiceStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
