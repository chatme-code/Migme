package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class CreditRechargeTrigger extends RewardProgramTrigger {
   public CreditRechargeTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.CREDIT_RECHARGE, userData);
   }
}
