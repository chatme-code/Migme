package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedBaseTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithMigLevelTrigger;
import org.apache.log4j.Logger;

public class ReferredUserRewardedWithMigLevelProcessor extends ReferredUserRewardedBaseProcessor {
   @RewardProgramParamName
   public static final String REFERRED_USER_REWARDED_MIG_LEVEL_RANGE_PARAM_KEY = "refdUsrRwdMigLvlRange";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ReferredUserRewardedWithMigLevelProcessor.class));

   protected boolean processInternal(RewardProgramData programData, ReferredUserRewardedBaseTrigger trigger) {
      if (!(trigger instanceof ReferredUserRewardedWithMigLevelTrigger)) {
         return false;
      } else {
         ReferredUserRewardedWithMigLevelTrigger rfdUserRewardedWithMigLevelTrigger = (ReferredUserRewardedWithMigLevelTrigger)trigger;
         if (programData.hasParameter("refdUsrRwdMigLvlRange")) {
            String rewardedMigLevelRangeConstraint = programData.getStringParam("refdUsrRwdMigLvlRange", "");
            int rewardedNewMigLevel = rfdUserRewardedWithMigLevelTrigger.getReferredUserNewMigLevel();
            if (log.isDebugEnabled()) {
               log.debug("rewardedMigLevelRangeConstraint:[" + rewardedMigLevelRangeConstraint + "].rewardedNewMigLevel:[" + rewardedNewMigLevel + "]");
            }

            return checkRewardedNewMigLevel(rewardedMigLevelRangeConstraint, rewardedNewMigLevel);
         } else {
            return true;
         }
      }
   }

   private static boolean checkRewardedNewMigLevel(String rewardedMigLevelRangeConstraint, int rewardedNewMigLevel) {
      if (StringUtil.isBlank(rewardedMigLevelRangeConstraint)) {
         return true;
      } else {
         int separatorPos = rewardedMigLevelRangeConstraint.indexOf(44);
         if (separatorPos == -1) {
            int rewardedMigLevelConstraint = Integer.parseInt(rewardedMigLevelRangeConstraint);
            return rewardedMigLevelConstraint == rewardedNewMigLevel;
         } else {
            String minValStr = rewardedMigLevelRangeConstraint.substring(0, separatorPos);
            int maxValStart = separatorPos + 1;
            String maxValStr;
            if (maxValStart < rewardedMigLevelRangeConstraint.length()) {
               maxValStr = rewardedMigLevelRangeConstraint.substring(maxValStart);
            } else {
               maxValStr = "";
            }

            return isAboveMin(minValStr, rewardedNewMigLevel) && isBelowMax(maxValStr, rewardedNewMigLevel);
         }
      }
   }

   private static boolean isAboveMin(String minValStr, int val) {
      if (!StringUtil.isBlank(minValStr)) {
         return Integer.parseInt(minValStr) <= val;
      } else {
         return true;
      }
   }

   private static boolean isBelowMax(String maxValStr, int val) {
      if (!StringUtil.isBlank(maxValStr)) {
         return val <= Integer.parseInt(maxValStr);
      } else {
         return true;
      }
   }
}
