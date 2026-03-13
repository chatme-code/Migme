package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class ThirdPartyAppStartEventTrigger extends RewardProgramTrigger {
   public String applicationName = "";

   public ThirdPartyAppStartEventTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.THIRDPARTY_APP_START, userData);
   }
}
