package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;

public class NoOpRewardProgramProcessor extends RewardProgramProcessor {
   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      return true;
   }
}
