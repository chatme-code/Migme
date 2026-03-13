package com.projectgoth.fusion.recommendation.collector;

import com.projectgoth.fusion.common.AppStartupInfo;
import com.projectgoth.fusion.common.ServiceStatsFactory;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.slice.CollectedDataIce;
import com.projectgoth.fusion.slice.FusionExceptionWithErrorCauseCode;
import com.projectgoth.fusion.slice.RecommendationDataCollectionServiceStats;
import com.projectgoth.fusion.slice.ServiceStatsLongFieldValue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataCollectorStatsCounter {
   private final DataCollectorStatsCounter.StatsLongFieldValue totalReceived = new DataCollectorStatsCounter.StatsLongFieldValue();
   private final DataCollectorStatsCounter.StatsLongFieldValue totalSuccessfulProcess = new DataCollectorStatsCounter.StatsLongFieldValue();
   private final DataCollectorStatsCounter.StatsLongFieldValue totalFailedProcess = new DataCollectorStatsCounter.StatsLongFieldValue();
   private final ConcurrentHashMap<Integer, DataCollectorStatsCounter.StatsLongFieldValue> totalReceivedByDataTypeMap = new ConcurrentHashMap();
   private final ConcurrentHashMap<Integer, DataCollectorStatsCounter.StatsLongFieldValue> totalSuccessfulProcessByDataTypeMap = new ConcurrentHashMap();
   private final ConcurrentHashMap<String, DataCollectorStatsCounter.StatsLongFieldValue> totalFailedProcessByErrorCauseCodeMap = new ConcurrentHashMap();
   private final AppStartupInfo appStartupInfo;

   public DataCollectorStatsCounter(AppStartupInfo appStartupInfo) {
      this.appStartupInfo = appStartupInfo;
   }

   private static <K, V> V associate(ConcurrentHashMap<K, V> m, K k, V initialValue) {
      V previousValue = m.putIfAbsent(k, initialValue);
      return previousValue == null ? initialValue : previousValue;
   }

   public void updateOnDataReceived(CollectedDataIce data, long currentTimestamp) {
      Integer dataType = DataCollectorUtils.getDataType(data);
      this.totalReceived.increment(currentTimestamp);
      DataCollectorStatsCounter.StatsLongFieldValue totalReceivedByDataType = (DataCollectorStatsCounter.StatsLongFieldValue)associate(this.totalReceivedByDataTypeMap, dataType, new DataCollectorStatsCounter.StatsLongFieldValue());
      totalReceivedByDataType.increment(currentTimestamp);
   }

   public void updateOnDataProcessed(CollectedDataIce data, FusionExceptionWithErrorCauseCode ex, long currentTimestamp) {
      Integer dataType = DataCollectorUtils.getDataType(data);
      DataCollectorStatsCounter.StatsLongFieldValue totalSuccessfulProcessByDataType;
      if (ex == null) {
         this.totalSuccessfulProcess.increment(currentTimestamp);
         totalSuccessfulProcessByDataType = (DataCollectorStatsCounter.StatsLongFieldValue)associate(this.totalSuccessfulProcessByDataTypeMap, dataType, new DataCollectorStatsCounter.StatsLongFieldValue());
         totalSuccessfulProcessByDataType.increment(currentTimestamp);
      } else {
         this.totalFailedProcess.increment(currentTimestamp);
         totalSuccessfulProcessByDataType = (DataCollectorStatsCounter.StatsLongFieldValue)associate(this.totalFailedProcessByErrorCauseCodeMap, StringUtil.isBlank(ex.errorCauseCode) ? "" : ex.errorCauseCode, new DataCollectorStatsCounter.StatsLongFieldValue());
         totalSuccessfulProcessByDataType.increment(currentTimestamp);
      }

   }

   public RecommendationDataCollectionServiceStats populate(RecommendationDataCollectionServiceStats rdcsStats) {
      ServiceStatsFactory.initializeServiceStats(rdcsStats, this.appStartupInfo.getStartTime());
      rdcsStats.totalSuccessfullyProcessedDataCount = this.totalSuccessfulProcess.toServiceStatsLongFieldValue();
      rdcsStats.totalFailedProcessedDataCount = this.totalFailedProcess.toServiceStatsLongFieldValue();
      rdcsStats.totalReceivedDataCount = this.totalReceived.toServiceStatsLongFieldValue();
      rdcsStats.totalReceivedDataCountByDataType = new HashMap();
      Iterator i$ = this.totalReceivedByDataTypeMap.entrySet().iterator();

      Entry eErrorCauseToStatsField;
      while(i$.hasNext()) {
         eErrorCauseToStatsField = (Entry)i$.next();
         rdcsStats.totalReceivedDataCountByDataType.put(eErrorCauseToStatsField.getKey(), ((DataCollectorStatsCounter.StatsLongFieldValue)eErrorCauseToStatsField.getValue()).toServiceStatsLongFieldValue());
      }

      rdcsStats.totalSuccessfullyProcessedDataCountByDataType = new HashMap();
      i$ = this.totalSuccessfulProcessByDataTypeMap.entrySet().iterator();

      while(i$.hasNext()) {
         eErrorCauseToStatsField = (Entry)i$.next();
         rdcsStats.totalSuccessfullyProcessedDataCountByDataType.put(eErrorCauseToStatsField.getKey(), ((DataCollectorStatsCounter.StatsLongFieldValue)eErrorCauseToStatsField.getValue()).toServiceStatsLongFieldValue());
      }

      rdcsStats.totalFailedProcessedDataCountByErrorCauseCode = new HashMap();
      i$ = this.totalFailedProcessByErrorCauseCodeMap.entrySet().iterator();

      while(i$.hasNext()) {
         eErrorCauseToStatsField = (Entry)i$.next();
         rdcsStats.totalFailedProcessedDataCountByErrorCauseCode.put(eErrorCauseToStatsField.getKey(), ((DataCollectorStatsCounter.StatsLongFieldValue)eErrorCauseToStatsField.getValue()).toServiceStatsLongFieldValue());
      }

      return rdcsStats;
   }

   public void clear() {
      this.totalFailedProcess.clear();
      this.totalFailedProcessByErrorCauseCodeMap.clear();
      this.totalReceived.clear();
      this.totalReceivedByDataTypeMap.clear();
      this.totalSuccessfulProcess.clear();
      this.totalSuccessfulProcessByDataTypeMap.clear();
   }

   private static final class StatsLongFieldValue {
      private static final long TIME_NOT_AVAILABLE = -1L;
      public final AtomicLong count;
      public final AtomicLong lastUpdatedTime;

      private StatsLongFieldValue() {
         this.count = new AtomicLong(0L);
         this.lastUpdatedTime = new AtomicLong(-1L);
      }

      public ServiceStatsLongFieldValue toServiceStatsLongFieldValue() {
         return new ServiceStatsLongFieldValue(this.count.longValue(), this.lastUpdatedTime.longValue());
      }

      public void increment(long currentTimestamp) {
         this.count.incrementAndGet();
         this.lastUpdatedTime.set(currentTimestamp);
      }

      public void clear() {
         this.count.set(0L);
         this.lastUpdatedTime.set(-1L);
      }

      // $FF: synthetic method
      StatsLongFieldValue(Object x0) {
         this();
      }
   }
}
