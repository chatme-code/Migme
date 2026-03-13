package com.projectgoth.fusion.rewardsystem.stateprocessors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;

public abstract class SpecificTriggerTypeStateHandler<T extends RewardProgramTrigger> extends RewardProgramStateHandler {
   private final Class<T> expectedTriggerClass;

   protected SpecificTriggerTypeStateHandler(Class<T> expectedTriggerClass) {
      this.expectedTriggerClass = expectedTriggerClass;
   }

   public RewardProgramStateHandler.PerformReturn perform(RewardProgramData program, RewardProgramTrigger trigger, String stateData) {
      return trigger != null && this.expectedTriggerClass.isAssignableFrom(trigger.getClass()) ? this.performWithSpecificTrigger(program, trigger, stateData) : RewardProgramStateHandler.PerformReturn.NOTHING;
   }

   protected abstract RewardProgramStateHandler.PerformReturn performWithSpecificTrigger(RewardProgramData var1, T var2, String var3);
}
