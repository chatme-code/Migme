package com.projectgoth.fusion.rewardsystem.instrumentation;

import com.projectgoth.fusion.data.RewardProgramData;

public class RewardProgramTriggerSampleCategory implements SampleCategory {
   private final RewardProgramData.TypeEnum type;

   public RewardProgramTriggerSampleCategory(RewardProgramData.TypeEnum type) {
      this.type = type;
   }

   public int hashCode() {
      return this.type.hashCode();
   }

   public boolean equals(Object obj) {
      return obj instanceof RewardProgramTriggerSampleCategory ? this.type.equals(((RewardProgramTriggerSampleCategory)obj).type) : false;
   }

   public int intValue() {
      return this.type.value();
   }

   public String toString() {
      return "RewardProgramTriggerSampleCategory:type=[" + this.type + "]";
   }
}
