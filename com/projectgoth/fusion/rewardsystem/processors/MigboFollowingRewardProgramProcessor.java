package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.MigboFollowingEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RelationshipEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class MigboFollowingRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MigboFollowingRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String FOLLOWED_USERID_PARAM_KEY = "followedUserID";
   @RewardProgramParamName
   public static final String EVENT_TYPE_PARAM_KEY = "eventType";
   @RewardProgramParamName
   public static final String IS_AUTO_FOLLOW = "isAutoFollow";
   @RewardProgramParamName
   public static final String OTHER_USER_VERIFIED_ACCOUNT_STATUS = "otherUsrVfydAccStats";
   @RewardProgramParamName
   public static final String OTHER_USER_VERIFIED_ACCOUNT_ENTITY_TYPE = "otherUsrVfydAccEntityType";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof MigboFollowingEventTrigger)) {
         return false;
      } else {
         MigboFollowingEventTrigger mfeTrigger = (MigboFollowingEventTrigger)trigger;
         RelationshipEventTrigger.RelationshipEventTypeEnum expectedEventType = RelationshipEventTrigger.RelationshipEventTypeEnum.fromValue(programData.getIntParam("eventType", -1));
         int expectedFollowedUserID = programData.getIntParam("followedUserID", -1);
         if (log.isDebugEnabled()) {
            log.debug("expected relationshipEventType [" + expectedEventType + "] expected followedUserID[" + expectedFollowedUserID + "] triggeredRelationshipEventType[" + mfeTrigger.getRelationshipEvent() + "] triggeredFollowedUserID[" + mfeTrigger.getFollowedUser() + "]");
         }

         boolean result = true;
         if (expectedEventType != null) {
            result &= expectedEventType == mfeTrigger.getRelationshipEvent();
         }

         if (expectedFollowedUserID != -1) {
            result &= expectedFollowedUserID == mfeTrigger.getFollowedUser().userID;
         }

         if (programData.hasParameter("isAutoFollow")) {
            boolean expectedAutoFollowFlag = programData.getBoolParam("isAutoFollow", false);
            result &= expectedAutoFollowFlag == mfeTrigger.isAutoFollow();
         }

         result &= programData.matchesVerifiedAccountStatusConstraint("otherUsrVfydAccStats", mfeTrigger.getFollowedUser().accountVerified) && programData.matchesVerifiedAccountTypeConstraint("otherUsrVfydAccEntityType", mfeTrigger.getFollowedUser().accountType);
         return result;
      }
   }
}
