package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RegistrationContextData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;

public class ExternalEmailVerifiedTrigger extends RewardProgramTrigger {
   private final RegistrationContextData regContextData;
   private final String verifiedEmailAddress;

   public ExternalEmailVerifiedTrigger(UserData userData, RegistrationContextData regContextData, String verifiedEmailAddress) {
      super(RewardProgramData.TypeEnum.EXTERNAL_EMAIL_VERIFIED_EVENT, userData);
      this.regContextData = regContextData;
      this.verifiedEmailAddress = verifiedEmailAddress;
      this.quantityDelta = 1;
      this.currency = null;
      this.amountDelta = 0.0D;
   }

   public String getVerifiedEmailAddress() {
      return this.verifiedEmailAddress;
   }

   public RegistrationContextData getRegContextData() {
      return this.regContextData;
   }
}
