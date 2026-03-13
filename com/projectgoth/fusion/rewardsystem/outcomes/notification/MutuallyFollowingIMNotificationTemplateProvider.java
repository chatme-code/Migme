package com.projectgoth.fusion.rewardsystem.outcomes.notification;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeProcessor;
import com.projectgoth.fusion.rewardsystem.triggers.MutuallyFollowingEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class MutuallyFollowingIMNotificationTemplateProvider extends RewardProgramOutcomeProcessor {
   private static Logger log = Log4JUtils.getLogger(MutuallyFollowingIMNotificationTemplateProvider.class);
   public static final String PARAM_INITIATOR_IM_NOTE_TEMPLATE = "initiatorIMNoteTmplt";
   public static final String PARAM_FOLLOW_BACKER_IM_NOTE_TEMPLATE = "followBackerIMNoteTmplt";

   public RewardProgramOutcomeData getOutcome(RewardProgramData data, RewardProgramTrigger trigger) throws Exception {
      if (!(trigger instanceof MutuallyFollowingEventTrigger)) {
         if (log.isDebugEnabled()) {
            log.debug("does not support selecting outcome from trigger:[" + trigger + "]");
         }

         return null;
      } else {
         MutuallyFollowingEventTrigger mfeTrigger = (MutuallyFollowingEventTrigger)trigger;
         String imNoteTemplate;
         if (mfeTrigger.isThisUserFollowedBacked()) {
            imNoteTemplate = data.getStringParam("followBackerIMNoteTmplt", (String)null);
         } else {
            imNoteTemplate = data.getStringParam("initiatorIMNoteTmplt", (String)null);
         }

         return !StringUtil.isBlank(imNoteTemplate) ? new IMNotificationTemplateOutcomeData(imNoteTemplate) : null;
      }
   }
}
