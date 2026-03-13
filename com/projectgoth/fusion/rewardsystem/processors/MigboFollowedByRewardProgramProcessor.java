package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.MigboFollowedByEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RelationshipEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class MigboFollowedByRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MigboFollowedByRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String FOLLOWER_USERID_PARAM_KEY = "followerUserID";
   @RewardProgramParamName
   public static final String EVENT_TYPE_PARAM_KEY = "eventType";
   @RewardProgramParamName
   public static final String IS_AUTO_FOLLOW = "isAutoFollow";
   @RewardProgramParamName
   public static final String OTHER_USER_VERIFIED_ACCOUNT_STATUS = "otherUsrVfydAccStats";
   @RewardProgramParamName
   public static final String OTHER_USER_VERIFIED_ACCOUNT_ENTITY_TYPE = "otherUsrVfydAccEntityType";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof MigboFollowedByEventTrigger)) {
         return false;
      } else {
         MigboFollowedByEventTrigger mfeTrigger = (MigboFollowedByEventTrigger)trigger;
         RelationshipEventTrigger.RelationshipEventTypeEnum expectedEventType = RelationshipEventTrigger.RelationshipEventTypeEnum.fromValue(programData.getIntParam("eventType", -1));
         int expectedFollowerUserID = programData.getIntParam("followerUserID", -1);
         if (log.isDebugEnabled()) {
            log.debug("expected relationshipEventType [" + expectedEventType + "] expected followerUserID[" + expectedFollowerUserID + "] triggeredRelationshipEventType[" + mfeTrigger.getRelationshipEvent() + "] triggeredFollowerUserID[" + mfeTrigger.getFollowerUser() + "]");
         }

         boolean result = true;
         if (expectedEventType != null) {
            result &= expectedEventType == mfeTrigger.getRelationshipEvent();
         }

         if (expectedFollowerUserID != -1) {
            result &= expectedFollowerUserID == mfeTrigger.getFollowerUser().userID;
         }

         if (programData.hasParameter("isAutoFollow")) {
            boolean expectedAutoFollowFlag = programData.getBoolParam("isAutoFollow", false);
            result &= expectedAutoFollowFlag == mfeTrigger.isAutoFollow();
         }

         result &= programData.matchesVerifiedAccountStatusConstraint("otherUsrVfydAccStats", mfeTrigger.getFollowerUser().accountVerified) && programData.matchesVerifiedAccountTypeConstraint("otherUsrVfydAccEntityType", mfeTrigger.getFollowerUser().accountType);
         return result;
      }
   }
}
