package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.LastLoginTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class LastLoginRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LastLoginRewardProgramProcessor.class));
   private static final long MILLIS_IN_A_DAY = 86400000L;
   @RewardProgramParamName
   public static final String DAYS_SINCE_LAST_LOGIN_PARAM_KEY = "minDaysSinceLastLogin";
   @RewardProgramParamName
   public static final String USE_CURR_DATE_ON_NULL_LAST_LOGIN_DATE = "useCurrDtForNullLastLoginDt";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof LastLoginTrigger)) {
         return false;
      } else {
         LastLoginTrigger llTrigger = (LastLoginTrigger)trigger;
         long nowTs = System.currentTimeMillis();
         long lastLoginDateTime;
         if (llTrigger.lastLoginDate == null) {
            boolean useCurrDateForNullLastLoginDate = programData.getBoolParam("useCurrDtForNullLastLoginDt", false);
            if (!useCurrDateForNullLastLoginDate) {
               log.info("User " + llTrigger.userData.username + " last login date is null.");
               return false;
            }

            lastLoginDateTime = nowTs;
         } else {
            lastLoginDateTime = llTrigger.lastLoginDate.getTime();
         }

         long lastLoginDay = lastLoginDateTime / 86400000L;
         long currentDay = nowTs / 86400000L;
         int minDaysSinceLastLoginParam = programData.getIntParam("minDaysSinceLastLogin", 30);
         return currentDay - lastLoginDay > (long)minDaysSinceLastLoginParam;
      }
   }
}
