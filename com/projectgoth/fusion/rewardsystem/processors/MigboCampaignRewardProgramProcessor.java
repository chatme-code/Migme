package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.MigboCampaignTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class MigboCampaignRewardProgramProcessor extends RewardProgramProcessor {
   @RewardProgramParamName
   public static final String CAMPAIGNID_PARAM_KEY = "campaignID";
   @RewardProgramParamName
   public static final String ENTITY_TYPE_PARAM_KEY = "entityType";
   @RewardProgramParamName
   public static final String ENTITY_ID_PARAM_KEY = "entityId";
   @RewardProgramParamName
   public static final String TAG_VALUE_PARAM_KEY = "tagValue";
   @RewardProgramParamName
   public static final String EVENT_TYPE_PARAM_KEY = "eventType";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MigboCampaignRewardProgramProcessor.class));

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (log.isDebugEnabled()) {
         log.debug("MigbocampaignRewardProgramProcessor activated for program ID [" + programData.id + "] trigger :" + trigger.toString());
      }

      if (!(trigger instanceof MigboCampaignTrigger)) {
         return false;
      } else {
         MigboCampaignTrigger mcTrigger = (MigboCampaignTrigger)trigger;
         int requiredCampaignID = programData.getIntParam("campaignID", 0);
         String requiredEntityType = programData.getStringParam("entityType", "");
         int requiredTagValue = programData.getIntParam("tagValue", -1);
         String requiredEventType = programData.getStringParam("eventType", "");
         String requiredEntityId = programData.getStringParam("entityId", "");
         if (log.isDebugEnabled()) {
            log.debug("userID[" + trigger.userData.userID + "]  campaignID in Trigger[" + mcTrigger.campaignID + "] required[" + requiredCampaignID + "]");
            log.debug("userID[" + trigger.userData.userID + "]  entityType in Trigger[" + mcTrigger.entityType + "] required[" + requiredEntityType + "]");
            log.debug("userID[" + trigger.userData.userID + "]  entityId in Trigger[" + mcTrigger.entityId + "] required[" + requiredEntityId + "]");
            log.debug("userID[" + trigger.userData.userID + "]  tagValue in Trigger[" + mcTrigger.tagValue + "] required[" + requiredTagValue + "]");
            log.debug("userID[" + trigger.userData.userID + "]  eventType in Trigger[" + mcTrigger.eventType.toString() + "] required[" + requiredEventType + "]");
         }

         boolean triggerValid = true;
         if (requiredCampaignID > 0) {
            triggerValid &= requiredCampaignID == mcTrigger.campaignID;
         }

         if (triggerValid && !StringUtil.isBlank(requiredEventType)) {
            triggerValid &= requiredEventType.equalsIgnoreCase(mcTrigger.eventType.toString());
         }

         if (triggerValid && !StringUtil.isBlank(requiredEntityId)) {
            triggerValid &= requiredEntityId.equalsIgnoreCase(mcTrigger.entityId);
         }

         if (triggerValid && !StringUtil.isBlank(requiredEntityType)) {
            triggerValid &= requiredEntityType.equalsIgnoreCase(mcTrigger.entityType);
         }

         if (triggerValid && requiredTagValue != -1) {
            triggerValid &= requiredTagValue == mcTrigger.tagValue;
         }

         return triggerValid;
      }
   }
}
