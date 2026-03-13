package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.referreduserrewarded.ReferredUserMeetsRewardCriteriaEvent;

public class ReferredUserMeetsRewardCriteriaTrigger extends ReferredUserRewardedBaseTrigger implements ReferredUserMeetsRewardCriteriaEvent {
   private final boolean rewarded;

   public ReferredUserMeetsRewardCriteriaTrigger(UserData referrerUserData, UserData referredUserData, RewardProgramData referredUserRewardProgram, boolean hasRewards) {
      super(RewardProgramData.TypeEnum.REFERRED_USER_MEETS_REWARD_CRITERIA, referrerUserData, referredUserData, referredUserRewardProgram);
      this.rewarded = hasRewards;
      this.amountDelta = 0.0D;
      this.quantityDelta = 1;
   }

   public boolean isRewarded() {
      return this.rewarded;
   }
}
