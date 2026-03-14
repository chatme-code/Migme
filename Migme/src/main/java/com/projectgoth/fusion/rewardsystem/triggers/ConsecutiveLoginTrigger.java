package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import java.util.Date;

public class ConsecutiveLoginTrigger extends RewardProgramTrigger {
   public Date lastLoginDate;

   public ConsecutiveLoginTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.CONSECUTIVE_LOGIN, userData);
   }
}
