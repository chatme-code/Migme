package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.MutuallyFollowingEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MutuallyFollowingUsersRewardProvider extends RewardProgramOutcomeProcessor {
   private static final Logger log = Log4JUtils.getLogger(MutuallyFollowingUsersRewardProvider.class);
   public static final String PARAM_INITIATOR_REWARD = "initiatorReward";
   public static final String PARAM_FOLLOW_BACKER_REWARD = "followBackerReward";

   public RewardProgramOutcomeData getOutcome(RewardProgramData data, RewardProgramTrigger trigger) throws JSONException {
      if (!(trigger instanceof MutuallyFollowingEventTrigger)) {
         if (log.isDebugEnabled()) {
            log.debug("does not support selecting outcome from trigger:[" + trigger + "]");
         }

         return null;
      } else {
         MutuallyFollowingEventTrigger mfeTrigger = (MutuallyFollowingEventTrigger)trigger;
         String outcomeJSONString;
         if (mfeTrigger.isThisUserFollowedBacked()) {
            outcomeJSONString = data.getStringParam("followBackerReward", (String)null);
         } else {
            outcomeJSONString = data.getStringParam("initiatorReward", (String)null);
         }

         if (outcomeJSONString != null) {
            JSONObject jsonObject = new JSONObject(outcomeJSONString);
            return RewardProgramOutcomeDataDeserializer.deserialize(jsonObject);
         } else {
            return null;
         }
      }
   }
}
