package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class MerchantTrailTrigger extends RewardProgramTrigger {
   public MerchantTrailTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.MERCHANT_TRAILS_EARNED, userData);
   }
}
