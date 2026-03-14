package com.projectgoth.fusion.recommendation.collector.rewardsystem;

import com.projectgoth.fusion.recommendation.collector.sinks.log4j.Log4JSink;
import com.projectgoth.fusion.slice.CollectedRewardProgramTriggerSummaryDataIce;

public class CollectedRewardProgramTriggerSummaryLog4jSink extends Log4JSink<CollectedRewardProgramTriggerSummaryDataIce> {
   public CollectedRewardProgramTriggerSummaryLog4jSink(String name, String loggerSinkCategoryName) {
      super(name, loggerSinkCategoryName);
   }

   protected String toString(CollectedRewardProgramTriggerSummaryDataIce record) {
      return RewardProgramTriggerSummaryLogUtils.toString(record);
   }
}
