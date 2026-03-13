package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.VGSentTrigger;
import org.apache.log4j.Logger;

public class VGSentRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VGSentRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String UNIQUE_GIFT_PARAM_KEY = "virtualGiftID";
   @RewardProgramParamName
   public static final String IS_FROM_INVENTORY = "isFromInventory";
   @RewardProgramParamName
   public static final String UNIQUE_RECEIVER_PARAM_KEY = "receiverUserID";
   @RewardProgramParamName
   public static final String RECEIVER_VERIFIED_ACCOUNT_STATUS = "rcvrVfydAccStats";
   @RewardProgramParamName
   public static final String RECEIVER_VERIFIED_ACCOUNT_ENTITY_TYPE = "rcvrVfydAccEntityType";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof VGSentTrigger)) {
         return false;
      } else {
         VGSentTrigger vgTrigger = (VGSentTrigger)trigger;
         boolean valid = true;
         UserData receiverUserData = vgTrigger.getReceiverUserData();
         int userid = programData.getIntParam("receiverUserID", -1);
         if (userid > 0) {
            valid = userid == receiverUserData.userID;
         }

         int giftid = programData.getIntParam("virtualGiftID", -1);
         if (giftid > 0) {
            valid = valid && giftid == vgTrigger.virtualGiftID;
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.REWARD_PROCESSOR_MATCH_FROM_INVENTORY_ENABLED)) {
            valid = valid && this.matchesFromSenderInventory(programData, vgTrigger);
         }

         valid &= programData.matchesVerifiedAccountStatusConstraint("rcvrVfydAccStats", receiverUserData.accountVerified) && programData.matchesVerifiedAccountTypeConstraint("rcvrVfydAccEntityType", receiverUserData.accountType);
         if (log.isDebugEnabled()) {
            log.debug(userid + " " + receiverUserData.userID + " " + giftid + " " + vgTrigger.virtualGiftID + " " + valid);
         }

         return valid;
      }
   }

   private boolean matchesFromSenderInventory(RewardProgramData programData, VGSentTrigger vgTrigger) {
      if (programData.hasParameter("isFromInventory")) {
         return vgTrigger.fromSenderInventory == programData.getBoolParam("isFromInventory", false);
      } else {
         return true;
      }
   }
}
