package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserFirstAuthenticatedTrigger;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.log4j.Logger;

public class UserFirstAuthenticatedRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserFirstAuthenticatedRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String CAMPAIGN_REGEX_PARAM_KEY = "campaignRegex";
   @RewardProgramParamName
   public static final String REG_TYPE_REGEX_KEY = "regTypeRegex";
   @RewardProgramParamName
   public static final String COUNTRY_IDS_KEY = "countryIDs";
   @RewardProgramParamName
   public static final String REG_IP_ADDRESS_REGEX_KEY = "regIpAddrRegex";
   @RewardProgramParamName
   public static final String REG_EMAIL_ADDRESS_REGEX_KEY = "regEmailAddrRegex";
   @RewardProgramParamName
   public static final String REG_USER_AGENT_REGEX_KEY = "regUserAgentRegex";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof UserFirstAuthenticatedTrigger)) {
         return false;
      } else {
         UserFirstAuthenticatedTrigger ufaTrigger = (UserFirstAuthenticatedTrigger)trigger;
         boolean processInternalResult = countryMatches(programData, ufaTrigger.userData.countryID) && matchesRegExKey(programData, "regTypeRegex", ufaTrigger.regContextData.registrationType) && matchesRegExKey(programData, "campaignRegex", ufaTrigger.regContextData.campaign) && matchesRegExKey(programData, "regIpAddrRegex", ufaTrigger.regContextData.ipAddress) && matchesRegExKey(programData, "regEmailAddrRegex", ufaTrigger.regContextData.email) && matchesRegExKey(programData, "regUserAgentRegex", ufaTrigger.regContextData.userAgent);
         if (log.isDebugEnabled()) {
            log.debug(UserFirstAuthenticatedRewardProgramProcessor.class.getName() + " Reward Program ID:[" + programData.id + "] passed:[" + processInternalResult + "]");
         }

         return processInternalResult;
      }
   }

   private static boolean matchesRegExKey(RewardProgramData programData, String regexKey, String input) throws PatternSyntaxException {
      String regex = programData.getStringParam(regexKey, (String)null);
      if (log.isDebugEnabled()) {
         log.debug(String.format("RewardProgramID:[%s] regexkey:[%s] regex:[%s] input:[%s]", programData.id, regexKey, regex, input));
      }

      boolean passed = false;
      if (regex == null) {
         passed = true;
      } else if (StringUtil.isBlank(regex)) {
         if (input == null) {
            passed = false;
         } else if (StringUtil.isBlank(input)) {
            passed = true;
         } else {
            passed = false;
         }
      } else if (input == null) {
         passed = false;
      } else {
         try {
            passed = Pattern.matches(regex, input);
         } catch (PatternSyntaxException var6) {
            log.error(String.format("Incorrect regex RewardProgramID:[%s] regexkey:[%s] regex:[%s] input:[%s]", programData.id, regexKey, regex, input), var6);
            passed = false;
         }
      }

      if (log.isDebugEnabled()) {
         log.debug(String.format("RewardProgramID:[%s] regexkey:[%s] regex:[%s] input:[%s] passed:[%s]", programData.id, regexKey, regex, input, passed));
      }

      return passed;
   }

   private static boolean countryMatches(RewardProgramData programData, Integer countryId) {
      if (programData.hasParameter("countryIDs")) {
         String countryIdStr = countryId == null ? "null" : countryId.toString();
         Set<String> eligibleCountries = programData.getStringSetParam("countryIDs");
         if (log.isDebugEnabled()) {
            log.debug(String.format("RewardProgramID:[%s] key:[%s] countries:[%s] countryIdStr[%s]", programData.id, "countryIDs", eligibleCountries, countryIdStr));
         }

         boolean passed = eligibleCountries.contains(countryIdStr);
         if (log.isDebugEnabled()) {
            log.debug(String.format("RewardProgramID:[%s] key:[%s] countries:[%s] countryIdStr[%s] passed:[%s]", programData.id, "countryIDs", eligibleCountries, countryIdStr, passed));
         }

         return passed;
      } else {
         return true;
      }
   }
}
