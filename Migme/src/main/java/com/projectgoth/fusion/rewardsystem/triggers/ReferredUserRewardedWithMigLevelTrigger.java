package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.referreduserrewarded.ReferredUserMigLevelIncreaseEvent;

public class ReferredUserRewardedWithMigLevelTrigger extends ReferredUserRewardedBaseTrigger implements ReferredUserMigLevelIncreaseEvent {
   private final int referredUserNewMigLevel;

   public ReferredUserRewardedWithMigLevelTrigger(UserData referrerUserData, UserData referredUserData, RewardProgramData referredUserRewardProgram, int referredUserNewMigLevel) {
      super(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_MIGLEVEL, referrerUserData, referredUserData, referredUserRewardProgram);
      this.referredUserNewMigLevel = referredUserNewMigLevel;
      this.quantityDelta = 1;
      this.amountDelta = 0.0D;
   }

   public int getReferredUserNewMigLevel() {
      return this.referredUserNewMigLevel;
   }

   public int getNewMigLevel() {
      return this.referredUserNewMigLevel;
   }
}
