package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.login.LastLoginEvent;
import java.util.Date;

public class LastLoginTrigger extends RewardProgramTrigger implements LastLoginEvent {
   public Date lastLoginDate;
   private Date currentLoginTime;

   public LastLoginTrigger(UserData userData, Date currentLoginTime) {
      super(RewardProgramData.TypeEnum.LAST_LOGIN, userData);
      this.currentLoginTime = currentLoginTime;
   }

   public Date getLastLoginTime() {
      return this.lastLoginDate;
   }

   public Date getCurrentLoginTime() {
      return this.currentLoginTime;
   }
}
