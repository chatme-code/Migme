package com.projectgoth.fusion.rewardsystem.outcomes.notification;

import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeProcessor;
import com.projectgoth.fusion.rewardsystem.triggers.MutuallyFollowingEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class MutuallyFollowingEmailTemplateIDProvider extends RewardProgramOutcomeProcessor {
   private static Logger log = Log4JUtils.getLogger(MutuallyFollowingEmailTemplateIDProvider.class);
   public static final String PARAM_INITIATOR_EMAIL_TEMPLATE_ID = "initiatorEmailTmpltID";
   public static final String PARAM_FOLLOW_BACKER_EMAIL_TEMPLATE_ID = "followBackerEmailTmpltID";

   public RewardProgramOutcomeData getOutcome(RewardProgramData data, RewardProgramTrigger trigger) throws Exception {
      if (!(trigger instanceof MutuallyFollowingEventTrigger)) {
         if (log.isDebugEnabled()) {
            log.debug("does not support selecting outcome from trigger:[" + trigger + "]");
         }

         return null;
      } else {
         MutuallyFollowingEventTrigger mfeTrigger = (MutuallyFollowingEventTrigger)trigger;
         int emailTemplateID;
         if (mfeTrigger.isThisUserFollowedBacked()) {
            emailTemplateID = data.getIntParam("followBackerEmailTmpltID", -1);
         } else {
            emailTemplateID = data.getIntParam("initiatorEmailTmpltID", -1);
         }

         return emailTemplateID >= 1 ? new EmailTemplateIDOutcomeData(emailTemplateID) : null;
      }
   }
}
