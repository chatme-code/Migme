package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.userreward.UserMigLevelIncreaseEvent;
import java.util.Date;

public class ReputationLevelIncreaseTrigger extends RewardProgramTrigger implements UserMigLevelIncreaseEvent {
   private final int newMigLevel;
   private final Date rewardedTime;
   private final int rewardedProgramID;
   private final int rewardedProgramTriggerType;

   public ReputationLevelIncreaseTrigger(UserData userData, int newMigLevel, Date rewardedTime, int rewardedProgramID, int rewardedProgramTriggerType) {
      super(RewardProgramData.TypeEnum.MIG_LEVEL, userData);
      this.newMigLevel = newMigLevel;
      this.rewardedTime = rewardedTime;
      this.rewardedProgramID = rewardedProgramID;
      this.rewardedProgramTriggerType = rewardedProgramTriggerType;
   }

   public int getNewMigLevel() {
      return this.newMigLevel;
   }

   public Date getRewardedTime() {
      return this.rewardedTime;
   }

   public long getRewardedProgramID() {
      return (long)this.rewardedProgramID;
   }

   public int getRewardedProgramTriggerType() {
      return this.rewardedProgramTriggerType;
   }
}
