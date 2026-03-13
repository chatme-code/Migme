package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;

public class ReferredUserRewardedWithMigCreditProcessor extends ReferredUserRewardedBaseProcessor {
   protected boolean processInternal(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) throws Exception {
      return true;
   }
}
