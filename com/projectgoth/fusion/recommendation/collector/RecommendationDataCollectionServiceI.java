package com.projectgoth.fusion.recommendation.collector;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.CollectedDataIce;
import com.projectgoth.fusion.slice.FusionExceptionWithRefCode;
import com.projectgoth.fusion.slice._RecommendationDataCollectionServiceDisp;
import org.apache.log4j.Logger;

public class RecommendationDataCollectionServiceI extends _RecommendationDataCollectionServiceDisp {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecommendationDataCollectionServiceI.class));
   private final DataCollectorContext dataCollectorCtx;

   public void logData(CollectedDataIce dataIce, Current __current) throws FusionExceptionWithRefCode {
      this.dataCollectorCtx.write(dataIce);
   }

   public RecommendationDataCollectionServiceI(DataCollectorContext dataCollectorCtx) {
      this.dataCollectorCtx = dataCollectorCtx;
   }
}
