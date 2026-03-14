package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.userreward.UserMeetsRewardCriteriaEvent;
import java.util.Date;

public class UserMeetsRewardCriteriaTrigger extends UserRewardedBaseTrigger implements UserMeetsRewardCriteriaEvent {
   private final boolean rewarded;

   public UserMeetsRewardCriteriaTrigger(UserData userData, RewardProgramData qualifiedUserRewardProgram, boolean hasRewards, Date rewardedTime) {
      super(RewardProgramData.TypeEnum.USER_MEETS_REWARD_CRITERIA, userData, qualifiedUserRewardProgram, rewardedTime);
      this.rewarded = hasRewards;
      this.amountDelta = 0.0D;
      this.quantityDelta = 1;
   }

   public boolean isRewarded() {
      return this.rewarded;
   }
}
