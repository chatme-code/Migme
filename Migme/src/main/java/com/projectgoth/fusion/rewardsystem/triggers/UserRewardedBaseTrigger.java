package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.userreward.UserRewardedEvent;
import java.util.Date;

public abstract class UserRewardedBaseTrigger extends RewardProgramTrigger implements UserRewardedEvent {
   private final RewardProgramData qualifiedUserRewardProgram;
   private final Date rewardedTime;

   protected UserRewardedBaseTrigger(RewardProgramData.TypeEnum programType, UserData userData, RewardProgramData fulfilledUserRewardProgram, Date rewardedTime) {
      super(programType, userData);
      this.amountDelta = 0.0D;
      this.currency = "USD";
      this.quantityDelta = 1;
      this.qualifiedUserRewardProgram = fulfilledUserRewardProgram;
      this.rewardedTime = rewardedTime;
   }

   public RewardProgramData getQualifiedUserRewardProgram() {
      return this.qualifiedUserRewardProgram;
   }

   public long getRewardedProgramID() {
      return this.qualifiedUserRewardProgram.id.longValue();
   }

   public int getRewardedProgramTriggerType() {
      return this.qualifiedUserRewardProgram.type.getId();
   }

   public Date getRewardedTime() {
      return this.rewardedTime;
   }
}
