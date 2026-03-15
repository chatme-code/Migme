package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.userreward.UserRewardedWithBadgeEvent;
import java.util.Date;

public class UserRewardedWithBadgeTrigger extends UserRewardedBaseTrigger implements UserRewardedWithBadgeEvent {
   private final int badgeId;

   public UserRewardedWithBadgeTrigger(UserData userData, RewardProgramData fulfilledUserRewardProgram, int badgeId, Date rewardedTime) {
      super(RewardProgramData.TypeEnum.USER_REWARDED_WITH_BADGE, userData, fulfilledUserRewardProgram, rewardedTime);
      this.badgeId = badgeId;
   }

   public int getBadgeId() {
      return this.badgeId;
   }
}
