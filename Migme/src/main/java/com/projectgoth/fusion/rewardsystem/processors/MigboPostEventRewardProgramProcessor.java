package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.MigboPostEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;

public class MigboPostEventRewardProgramProcessor extends RewardProgramProcessor {
   @RewardProgramParamName
   public static final String EVENT_TYPE_PARAM_KEY = "eventType";
   @RewardProgramParamName
   public static final String POSTID_PARAM_KEY = "postID";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof MigboPostEventTrigger)) {
         return false;
      } else {
         MigboPostEventTrigger bgwTrigger = (MigboPostEventTrigger)trigger;
         MigboPostEventTrigger.PostEventTypeEnum eventType = MigboPostEventTrigger.PostEventTypeEnum.fromType(programData.getIntParam("eventType", -1));
         String postID = programData.getStringParam("postID", "");
         boolean result = true;
         if (eventType != null) {
            result &= eventType == bgwTrigger.eventType;
         }

         if (!StringUtil.isBlank(postID)) {
            result &= postID.equals(bgwTrigger.postID);
         }

         return result;
      }
   }
}
