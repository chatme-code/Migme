package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithGroupMembershipTrigger;

public class ReferredUserRewardedWithGroupMembershipProcessor extends ReferredUserRewardedBaseProcessor {
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_GROUP_IDS_PARAM_KEY = "refdUsrRwdGrpIDs";
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_GROUP_IDS_IS_WHITELIST_PARAM_KEY = "refdUsrRwdGrpIDsIsWhiteLst";

   protected boolean processInternal(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) throws Exception {
      if (!(trigger instanceof ReferredUserRewardedWithGroupMembershipTrigger)) {
         return false;
      } else {
         ReferredUserRewardedWithGroupMembershipTrigger rfdUserRewardedWithGroupMembershipTrigger = (ReferredUserRewardedWithGroupMembershipTrigger)trigger;
         return programData.matchesSetOfStringsConstraint("refdUsrRwdGrpIDs", "refdUsrRwdGrpIDs", String.valueOf(rfdUserRewardedWithGroupMembershipTrigger.getRewardedGroupMembership().getGroupId()));
      }
   }
}
