package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithBadgeTrigger;

public class ReferredUserRewardedWithBadgeProcessor extends ReferredUserRewardedBaseProcessor {
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_BADGE_IDS_PARAM_KEY = "refdUsrRwdBadgeIDs";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_BADGE_IDS_IS_WHITELIST_PARAM_KEY = "refdUsrRwdBadgeIDsIsWhiteLst";

   protected boolean processInternal(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) throws Exception {
      if (!(trigger instanceof ReferredUserRewardedWithBadgeTrigger)) {
         return false;
      } else {
         ReferredUserRewardedWithBadgeTrigger rfdUserRewardedWithBadgeTrigger = (ReferredUserRewardedWithBadgeTrigger)trigger;
         return programData.matchesSetOfStringsConstraint("refdUsrRwdBadgeIDs", "refdUsrRwdBadgeIDsIsWhiteLst", String.valueOf(rfdUserRewardedWithBadgeTrigger.getRewardedBadge().getId()));
      }
   }
}
