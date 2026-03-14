package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class EmailSentTrigger extends RewardProgramTrigger {
   public EmailSentTrigger(UserData userData) {
      super(RewardProgramData.TypeEnum.EMAIL_SENT, userData);
   }
}
