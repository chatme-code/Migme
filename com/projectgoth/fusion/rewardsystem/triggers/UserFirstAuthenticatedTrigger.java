package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RegistrationContextData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.authenticated.RegistrationContext;
import com.projectgoth.leto.common.event.authenticated.UserFirstAuthenticatedEvent;

public class UserFirstAuthenticatedTrigger extends RewardProgramTrigger implements UserFirstAuthenticatedEvent {
   public RegistrationContextData regContextData;

   public UserFirstAuthenticatedTrigger(UserData userData, RegistrationContextData regContextData) {
      super(RewardProgramData.TypeEnum.USER_FIRST_AUTHENTICATED, userData);
      this.regContextData = regContextData;
      this.quantityDelta = 1;
      this.amountDelta = 0.0D;
   }

   public RegistrationContext getRegistrationContext() {
      return this.regContextData;
   }
}
