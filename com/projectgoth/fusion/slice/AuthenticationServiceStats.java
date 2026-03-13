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

public class AuthenticationServiceStats extends BaseServiceStats {
   private static ObjectFactory _factory = new AuthenticationServiceStats.__F();
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::AuthenticationServiceStats", "::com::projectgoth::fusion::slice::BaseServiceStats"};
   public long successfulAuthentications;
   public long failedAuthentications;
   public int successfulAuthenticationRate;
   public int failedAuthenticationRate;
   public int peakSuccessfulAuthenticationRate;
   public int peakFailedAuthenticationRate;
   public long peakSuccessfulAuthenticationRateDate;
   public long peakFailedAuthenticationRateDate;

   public AuthenticationServiceStats() {
   }

   public AuthenticationServiceStats(String hostname, String version, long jvmTotalMemory, long jvmFreeMemory, long uptime, long lastUpdatedTime, boolean izeRequestStatsEnabled, long izeRequestCount, Map<String, Long> izeRequestCountByOrigin, Map<String, Long> izeRequestCountByOperation, Map<String, Double> izeRequestMeanProcessingTimeByOperation, Map<String, Double> izeRequestMaxProcessingTimeByOperation, Map<String, Double> izeRequestStdevProcessingTimeByOperation, Map<String, Double> izeRequest95thPercentileProcessingTimeByOperation, Map<String, Double> izeRequestTotalProcessingTimeByOperation, boolean izeThreadStatsEnabled, long izeObjectAdapterThreadPoolRunning, long izeObjectAdapterThreadPoolInUse, long izeObjectAdapterThreadPoolInUseHighWatermark, double izeObjectAdapterThreadPoolLoad, long izeObjectAdapterThreadPoolSize, long izeObjectAdapterThreadPoolSizeMax, long izeObjectAdapterThreadPoolSizeWarn, boolean izeClientThreadPoolStatsEnabled, long izeClientThreadPoolRunning, long izeClientThreadPoolInUse, long izeClientThreadPoolInUseHighWatermark, double izeClientThreadPoolLoad, long izeClientThreadPoolSize, long izeClientThreadPoolSizeMax, long izeClientThreadPoolSizeWarn, long amdObjectAdapterThreadPoolRunning, long amdObjectAdapterThreadPoolInUse, long amdObjectAdapterThreadPoolInUseHighWatermark, long amdObjectAdapterThreadPoolSize, long amdObjectAdapterThreadPoolSizeMax, long amdObjectAdapterThreadPoolQueueLength, String dataGridStats, long successfulAuthentications, long failedAuthentications, int successfulAuthenticationRate, int failedAuthenticationRate, int peakSuccessfulAuthenticationRate, int peakFailedAuthenticationRate, long peakSuccessfulAuthenticationRateDate, long peakFailedAuthenticationRateDate) {
      super(hostname, version, jvmTotalMemory, jvmFreeMemory, uptime, lastUpdatedTime, izeRequestStatsEnabled, izeRequestCount, izeRequestCountByOrigin, izeRequestCountByOperation, izeRequestMeanProcessingTimeByOperation, izeRequestMaxProcessingTimeByOperation, izeRequestStdevProcessingTimeByOperation, izeRequest95thPercentileProcessingTimeByOperation, izeRequestTotalProcessingTimeByOperation, izeThreadStatsEnabled, izeObjectAdapterThreadPoolRunning, izeObjectAdapterThreadPoolInUse, izeObjectAdapterThreadPoolInUseHighWatermark, izeObjectAdapterThreadPoolLoad, izeObjectAdapterThreadPoolSize, izeObjectAdapterThreadPoolSizeMax, izeObjectAdapterThreadPoolSizeWarn, izeClientThreadPoolStatsEnabled, izeClientThreadPoolRunning, izeClientThreadPoolInUse, izeClientThreadPoolInUseHighWatermark, izeClientThreadPoolLoad, izeClientThreadPoolSize, izeClientThreadPoolSizeMax, izeClientThreadPoolSizeWarn, amdObjectAdapterThreadPoolRunning, amdObjectAdapterThreadPoolInUse, amdObjectAdapterThreadPoolInUseHighWatermark, amdObjectAdapterThreadPoolSize, amdObjectAdapterThreadPoolSizeMax, amdObjectAdapterThreadPoolQueueLength, dataGridStats);
      this.successfulAuthentications = successfulAuthentications;
      this.failedAuthentications = failedAuthentications;
      this.successfulAuthenticationRate = successfulAuthenticationRate;
      this.failedAuthenticationRate = failedAuthenticationRate;
      this.peakSuccessfulAuthenticationRate = peakSuccessfulAuthenticationRate;
      this.peakFailedAuthenticationRate = peakFailedAuthenticationRate;
      this.peakSuccessfulAuthenticationRateDate = peakSuccessfulAuthenticationRateDate;
      this.peakFailedAuthenticationRateDate = peakFailedAuthenticationRateDate;
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
      __os.writeLong(this.successfulAuthentications);
      __os.writeLong(this.failedAuthentications);
      __os.writeInt(this.successfulAuthenticationRate);
      __os.writeInt(this.failedAuthenticationRate);
      __os.writeInt(this.peakSuccessfulAuthenticationRate);
      __os.writeInt(this.peakFailedAuthenticationRate);
      __os.writeLong(this.peakSuccessfulAuthenticationRateDate);
      __os.writeLong(this.peakFailedAuthenticationRateDate);
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      this.successfulAuthentications = __is.readLong();
      this.failedAuthentications = __is.readLong();
      this.successfulAuthenticationRate = __is.readInt();
      this.failedAuthenticationRate = __is.readInt();
      this.peakSuccessfulAuthenticationRate = __is.readInt();
      this.peakFailedAuthenticationRate = __is.readInt();
      this.peakSuccessfulAuthenticationRateDate = __is.readLong();
      this.peakFailedAuthenticationRateDate = __is.readLong();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::AuthenticationServiceStats was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::AuthenticationServiceStats was not generated with stream support";
      throw ex;
   }

   private static class __F implements ObjectFactory {
      private __F() {
      }

      public Object create(String type) {
         assert type.equals(AuthenticationServiceStats.ice_staticId());

         return new AuthenticationServiceStats();
      }

      public void destroy() {
      }

      // $FF: synthetic method
      __F(java.lang.Object x0) {
         this();
      }
   }
}
