package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserMeetsRewardCriteriaTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;
import org.apache.log4j.Logger;

public class ReferredUserMeetsRewardCriteriaProcessor extends ReferredUserRewardedBaseProcessor {
   @RewardProgramParamName
   public static final String REFERRED_USER_IS_REWARDED = "refdUsrIsRewarded";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ReferredUserMeetsRewardCriteriaProcessor.class));

   protected boolean processInternal(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) {
      if (!(trigger instanceof ReferredUserMeetsRewardCriteriaTrigger)) {
         return false;
      } else {
         ReferredUserMeetsRewardCriteriaTrigger rfdUserMeetsRewardCriteriaTrigger = (ReferredUserMeetsRewardCriteriaTrigger)trigger;
         if (programData.hasParameter("refdUsrIsRewarded")) {
            boolean referredUserIsRewardedConstraint = programData.getBoolParam("refdUsrIsRewarded", false);
            boolean referredUserIsRewarded = rfdUserMeetsRewardCriteriaTrigger.isRewarded();
            if (log.isDebugEnabled()) {
               log.debug("referredUserIsRewardedConstraint:[" + referredUserIsRewardedConstraint + "].referredUserIsRewarded:[" + referredUserIsRewarded + "]");
            }

            return referredUserIsRewardedConstraint == referredUserIsRewarded;
         } else {
            return true;
         }
      }
   }
}
