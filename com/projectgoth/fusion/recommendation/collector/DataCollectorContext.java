package com.projectgoth.fusion.recommendation.collector;

import com.projectgoth.fusion.common.AppStartupInfo;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.slice.CollectedDataIce;
import com.projectgoth.fusion.slice.FusionExceptionWithErrorCauseCode;
import com.projectgoth.fusion.slice.FusionExceptionWithRefCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class DataCollectorContext {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DataCollectorContext.class));
   private final Map<Integer, List<DataCollectorWriterStrategy<?>>> dataTypeToStrategyImplementationsMap = new HashMap();
   private final DataCollectorStatsCounter statsCounter;

   public DataCollectorContext(AppStartupInfo appStartupInfo) {
      this.statsCounter = new DataCollectorStatsCounter(appStartupInfo);
   }

   public DataCollectorStatsCounter getStatsCounter() {
      return this.statsCounter;
   }

   public DataCollectorContext addStrategy(int dataType, DataCollectorWriterStrategy<?> strategy) {
      if (strategy == null) {
         throw new IllegalArgumentException("strategy is null");
      } else {
         List<DataCollectorWriterStrategy<?>> strategies = (List)this.dataTypeToStrategyImplementationsMap.get(dataType);
         if (strategies == null) {
            strategies = new ArrayList();
            this.dataTypeToStrategyImplementationsMap.put(dataType, strategies);
         }

         ((List)strategies).add(strategy);
         return this;
      }
   }

   public void write(CollectedDataIce dataIce, DataCollectorInvocationCtx invokeCtx, boolean throwWhenErrorDataListIsNotEmpty) throws FusionExceptionWithRefCode {
      try {
         this.statsCounter.updateOnDataReceived(dataIce, System.currentTimeMillis());

         try {
            this.process(dataIce, invokeCtx);
            this.statsCounter.updateOnDataProcessed(dataIce, (FusionExceptionWithErrorCauseCode)null, System.currentTimeMillis());
            if (throwWhenErrorDataListIsNotEmpty) {
               List<DataCollectorInvocationCtx.ErrorData> errorDataList = invokeCtx.getErrorDataList();
               if (!errorDataList.isEmpty()) {
                  throw DataCollectorUtils.prepareThrowingFusionExceptionWithRefCode(log, "Error while writing collected data", (String)errorDataList.toString(), (ErrorCause)ErrorCause.DataCollectorErrorReasonType.LOGGING_FAILED);
               }
            }

         } catch (FusionExceptionWithErrorCauseCode var5) {
            this.statsCounter.updateOnDataProcessed(dataIce, var5, System.currentTimeMillis());
            throw var5;
         }
      } catch (FusionExceptionWithRefCode var6) {
         throw var6;
      } catch (Throwable var7) {
         throw DataCollectorUtils.prepareThrowingFusionExceptionWithRefCode(log, "Unhandled exception", (ErrorCause)ErrorCause.DataCollectorErrorReasonType.UNHANDLED_EXCEPTION, (Throwable)var7);
      }
   }

   public void write(CollectedDataIce dataIce) throws FusionExceptionWithRefCode {
      this.write(dataIce, new DataCollectorInvocationCtx(), true);
   }

   public void clearStats() {
      this.statsCounter.clear();
   }

   private void process(CollectedDataIce dataIce, DataCollectorInvocationCtx invokeCtx) throws FusionExceptionWithRefCode {
      if (dataIce == null) {
         throw DataCollectorUtils.prepareThrowingFusionExceptionWithRefCode(log, "Required parameter is missing", (String)"'dataIce' is null", (ErrorCause)ErrorCause.DataCollectorErrorReasonType.ILLEGAL_PARAMETER_VALUE);
      } else if (invokeCtx == null) {
         throw DataCollectorUtils.prepareThrowingFusionExceptionWithRefCode(log, "Required parameter is missing", (String)"'invokeCtx' is null", (ErrorCause)ErrorCause.DataCollectorErrorReasonType.ILLEGAL_PARAMETER_VALUE);
      } else {
         Integer dataType = DataCollectorUtils.getDataType(dataIce);
         if (dataType == DataCollectorUtils.UNSPECIFIED_DATA_TYPE) {
            throw DataCollectorUtils.prepareThrowingFusionExceptionWithRefCode(log, "Invalid data type", (String)("Received data type:" + dataIce.dataType), (ErrorCause)ErrorCause.DataCollectorErrorReasonType.INVALID_DATA_TYPE);
         } else {
            try {
               List<DataCollectorWriterStrategy<?>> strategyList = (List)this.dataTypeToStrategyImplementationsMap.get(dataType);
               if (strategyList == null) {
                  throw DataCollectorUtils.prepareThrowingFusionExceptionWithRefCode(log, "Unsupported data type", (String)("Received data type:" + dataIce.dataType), (ErrorCause)ErrorCause.DataCollectorErrorReasonType.UNSUPPORTED_DATA_TYPE);
               } else {
                  Iterator i$ = strategyList.iterator();

                  while(i$.hasNext()) {
                     DataCollectorWriterStrategy<?> strategy = (DataCollectorWriterStrategy)i$.next();
                     strategy.write(dataIce, invokeCtx);
                  }

               }
            } catch (FusionExceptionWithRefCode var7) {
               throw var7;
            } catch (Exception var8) {
               throw DataCollectorUtils.prepareThrowingFusionExceptionWithRefCode(log, "Internal error", (ErrorCause)ErrorCause.DataCollectorErrorReasonType.INTERNAL_ERROR, (Throwable)var8);
            }
         }
      }
   }
}
