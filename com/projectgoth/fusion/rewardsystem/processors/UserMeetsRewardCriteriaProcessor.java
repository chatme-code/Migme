package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.UserMeetsRewardCriteriaTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserRewardedBaseTrigger;
import org.apache.log4j.Logger;

public class UserMeetsRewardCriteriaProcessor extends UserRewardedBaseProcessor {
   @RewardProgramParamName
   public static final String USER_IS_REWARDED = "usrIsRewarded";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserMeetsRewardCriteriaProcessor.class));

   protected boolean processInternal(RewardProgramData programData, UserRewardedBaseTrigger trigger) {
      if (!(trigger instanceof UserMeetsRewardCriteriaTrigger)) {
         return false;
      } else {
         UserMeetsRewardCriteriaTrigger userMeetsRewardCriteriaTrigger = (UserMeetsRewardCriteriaTrigger)trigger;
         if (programData.hasParameter("usrIsRewarded")) {
            boolean userIsRewardedConstraint = programData.getBoolParam("usrIsRewarded", false);
            boolean userIsRewarded = userMeetsRewardCriteriaTrigger.isRewarded();
            if (log.isDebugEnabled()) {
               log.debug("userIsRewardedConstraint:[" + userIsRewardedConstraint + "].userIsRewarded:[" + userIsRewarded + "]");
            }

            return userIsRewardedConstraint == userIsRewarded;
         } else {
            return true;
         }
      }
   }
}
