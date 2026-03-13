package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class SMSSentTrigger extends RewardProgramTrigger {
   public SMSSentTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.SMS_SENT, userData);
   }
}
