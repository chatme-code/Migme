package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class UserReferralActivationTrigger extends RewardProgramTrigger {
   public UserReferralActivationTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.USER_REFERRAL_AUTHENTICATED, userData);
   }
}
