package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RegistrationContextData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.ExternalEmailVerifiedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class ExternalEmailVerifiedRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ExternalEmailVerifiedRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String MAX_USER_REGISTRATION_DATE_PARAM_KEY = "maxUserRegDate";
   @RewardProgramParamName
   public static final String MIN_USER_REGISTRATION_DATE_PARAM_KEY = "minUserRegDate";
   @RewardProgramParamName
   public static final String USER_REG_TYPE_REGEX_PARAM_KEY = "userRegTypeRegex";
   @RewardProgramParamName
   public static final String COUNTRY_IDS_PARAM_KEY = "countryIDs";
   @RewardProgramParamName
   public static final String COUNTRY_IDS_IS_WHITELIST_PARAM_KEY = "countryIDsIsWhitelist";
   @RewardProgramParamName
   public static final String VERIFIED_EMAIL_ADDRESS_REGEX_PARAM_KEY = "verifiedEmailAddrRegex";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof ExternalEmailVerifiedTrigger)) {
         return false;
      } else {
         ExternalEmailVerifiedTrigger externalEmailVerifiedTrigger = (ExternalEmailVerifiedTrigger)trigger;
         UserData userData = externalEmailVerifiedTrigger.userData;
         RegistrationContextData regContextData = externalEmailVerifiedTrigger.getRegContextData();
         String countryIDStr = userData.countryID == null ? "null" : userData.countryID.toString();
         boolean processInternalResult = programData.matchesSetOfStringsConstraint("countryIDs", "countryIDsIsWhitelist", countryIDStr) && programData.matchesDateConstraint("minUserRegDate", true, userData.dateRegistered) && programData.matchesDateConstraint("maxUserRegDate", false, userData.dateRegistered) && programData.matchesRegExKey("userRegTypeRegex", regContextData.registrationType) && programData.matchesRegExKey("verifiedEmailAddrRegex", externalEmailVerifiedTrigger.getVerifiedEmailAddress());
         if (log.isDebugEnabled()) {
            log.debug(ExternalEmailVerifiedRewardProgramProcessor.class.getName() + " Reward Program ID:[" + programData.id + "] passed:[" + processInternalResult + "]");
         }

         return processInternalResult;
      }
   }
}
