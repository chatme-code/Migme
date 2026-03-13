package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.BotGameWonTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;

public class BotGameWonRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BotGameWonRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String BOT_ID_PARAM_KEY = "botID";

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof BotGameWonTrigger)) {
         return false;
      } else {
         BotGameWonTrigger bgwTrigger = (BotGameWonTrigger)trigger;
         int botID = programData.getIntParam("botID", -1);
         if (log.isDebugEnabled()) {
            log.debug("userID[" + trigger.userData.userID + "] botID in Trigger[" + bgwTrigger.botID + "] required[" + botID + "]");
         }

         if (botID > 0) {
            return botID == bgwTrigger.botID;
         } else {
            return true;
         }
      }
   }
}
