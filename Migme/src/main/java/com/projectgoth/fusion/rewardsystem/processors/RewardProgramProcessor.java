package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;

public class RewardProgramProcessor {
   public boolean checkValidity(RewardProgramData programData, RewardProgramTrigger trigger) throws Exception {
      return this.processInternal(programData, trigger);
   }

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) throws Exception {
      return true;
   }
}
