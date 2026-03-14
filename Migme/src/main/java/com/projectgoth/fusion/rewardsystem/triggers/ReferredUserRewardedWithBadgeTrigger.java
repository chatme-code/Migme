package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.RewardedBadgeData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.referreduserrewarded.ReferredUserRewardedWithBadgeEvent;

public class ReferredUserRewardedWithBadgeTrigger extends ReferredUserRewardedBaseTrigger implements ReferredUserRewardedWithBadgeEvent {
   private final RewardedBadgeData rewardedBadge;

   public ReferredUserRewardedWithBadgeTrigger(UserData referrerUserData, UserData referredUserData, RewardProgramData referredUserRewardProgram, RewardedBadgeData rewardedBadge) {
      super(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_BADGES, referrerUserData, referredUserData, referredUserRewardProgram);
      this.quantityDelta = 1;
      this.amountDelta = 0.0D;
      this.rewardedBadge = rewardedBadge;
   }

   public RewardedBadgeData getRewardedBadge() {
      return this.rewardedBadge;
   }

   public int getBadgeId() {
      return this.rewardedBadge != null ? this.rewardedBadge.getId() : -1;
   }
}
