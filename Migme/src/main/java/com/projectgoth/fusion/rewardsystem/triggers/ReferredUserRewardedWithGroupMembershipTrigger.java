package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.RewardedGroupMembershipData;
import com.projectgoth.fusion.data.UserData;

public class ReferredUserRewardedWithGroupMembershipTrigger extends ReferredUserRewardedBaseTrigger {
   private RewardedGroupMembershipData rewardedGroupMembership;

   public ReferredUserRewardedWithGroupMembershipTrigger(UserData referrerUserData, UserData referredUserData, RewardProgramData referredUserRewardProgram, RewardedGroupMembershipData rewardedGroupMembership) {
      super(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_GROUPMEMBERSHIP, referrerUserData, referredUserData, referredUserRewardProgram);
      this.rewardedGroupMembership = rewardedGroupMembership;
      this.amountDelta = 0.0D;
      this.quantityDelta = 1;
   }

   public RewardedGroupMembershipData getRewardedGroupMembership() {
      return this.rewardedGroupMembership;
   }
}
