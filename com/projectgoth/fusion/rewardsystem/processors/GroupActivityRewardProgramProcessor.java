package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.GroupActivityTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class GroupActivityRewardProgramProcessor extends RewardProgramProcessor {
   @RewardProgramParamName
   public static final String GROUPID_PARAM_KEY = "groupID";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupActivityRewardProgramProcessor.class));

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof GroupActivityTrigger)) {
         return false;
      } else {
         GroupActivityTrigger bgwTrigger = (GroupActivityTrigger)trigger;
         String requiredGroupID = programData.getStringParam("groupID", "");
         log.debug("userID[" + trigger.userData.userID + "]  groupID in Trigger[" + bgwTrigger.groupID + "] required[" + requiredGroupID + "]");
         return !StringUtil.isBlank(requiredGroupID) ? requiredGroupID.equalsIgnoreCase(bgwTrigger.groupID) : true;
      }
   }
}
