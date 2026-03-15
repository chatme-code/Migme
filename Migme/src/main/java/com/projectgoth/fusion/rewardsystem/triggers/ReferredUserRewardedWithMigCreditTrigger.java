package com.projectgoth.fusion.rewardsystem.triggers;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.RewardedMigCreditData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.leto.common.event.referreduserrewarded.ReferredUserRewardedWithMigCreditEvent;

public class ReferredUserRewardedWithMigCreditTrigger extends ReferredUserRewardedBaseTrigger implements ReferredUserRewardedWithMigCreditEvent {
   private final RewardedMigCreditData rewardedMigCreditData;

   public ReferredUserRewardedWithMigCreditTrigger(UserData referrerUserData, UserData referredUserData, RewardProgramData referredUserRewardProgram, RewardedMigCreditData rewardedMigCredit) {
      super(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_MIGCREDITS, referrerUserData, referredUserData, referredUserRewardProgram);
      this.rewardedMigCreditData = rewardedMigCredit;
      this.quantityDelta = 1;
      this.amountDelta = this.rewardedMigCreditData.getAmount();
      this.currency = this.rewardedMigCreditData.getCurrency();
   }

   public RewardedMigCreditData getRewardedMigCreditData() {
      return this.rewardedMigCreditData;
   }
}
