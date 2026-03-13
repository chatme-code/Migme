package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.sql.Connection;
import java.util.List;
import org.apache.log4j.Logger;

public class UserWhitelistRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserWhitelistRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String WHITELISTED_USERNAMES_PARAM_KEY = "whitelistedUsernames";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      List<String> whitelistedUsernames = programData.getStringListParam("whitelistedUsernames");
      if (log.isDebugEnabled()) {
         log.debug("Whitelisted users: " + whitelistedUsernames);
      }

      boolean passedWhiteListCheck = true;
      if (whitelistedUsernames.size() != 0) {
         try {
            User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            String triggerUsername = userBean.getUsernameByUserid(trigger.userData.userID, (Connection)null);
            if (log.isDebugEnabled()) {
               log.debug("Trigger username [" + triggerUsername + "]");
            }

            if (!whitelistedUsernames.contains(triggerUsername)) {
               passedWhiteListCheck = false;
            }
         } catch (Exception var7) {
            log.error("Unable to retrieve username for trigger [" + trigger.programType + "] for user [" + trigger.userData.userID + "]");
            passedWhiteListCheck = false;
         }
      }

      if (log.isDebugEnabled()) {
         log.debug("passedWhiteListCheck [" + whitelistedUsernames + "][" + passedWhiteListCheck + "]");
      }

      return passedWhiteListCheck;
   }
}
