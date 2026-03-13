package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.MerchantTaggedUserMeetsRewardCriteriaTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;
import org.apache.log4j.Logger;

public class MerchantTaggedUserMeetsRewardCriteriaProcessor extends ReferredUserRewardedBaseProcessor {
   @RewardProgramParamName
   public static final String MERCHANT_TAGGED_USER_IS_REWARDED = "mtTaggedUsrIsRewarded";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MerchantTaggedUserMeetsRewardCriteriaProcessor.class));

   protected boolean processInternal(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) {
      if (!(trigger instanceof MerchantTaggedUserMeetsRewardCriteriaTrigger)) {
         return false;
      } else {
         MerchantTaggedUserMeetsRewardCriteriaTrigger taggedUserMeetsRewardCriteriaTrigger = (MerchantTaggedUserMeetsRewardCriteriaTrigger)trigger;
         if (programData.hasParameter("mtTaggedUsrIsRewarded")) {
            boolean taggedUserIsRewardedConstraint = programData.getBoolParam("mtTaggedUsrIsRewarded", false);
            boolean taggedUserIsRewarded = taggedUserMeetsRewardCriteriaTrigger.isRewarded();
            if (log.isDebugEnabled()) {
               log.debug("taggedUserIsRewardedConstraint:[" + taggedUserIsRewardedConstraint + "].referredUserIsRewarded:[" + taggedUserIsRewarded + "]");
            }

            return taggedUserIsRewardedConstraint == taggedUserIsRewarded;
         } else {
            return true;
         }
      }
   }
}
