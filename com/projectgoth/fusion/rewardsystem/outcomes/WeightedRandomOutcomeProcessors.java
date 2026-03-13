package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.security.SecureRandom;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class WeightedRandomOutcomeProcessors extends RewardProgramOutcomeProcessor {
   public static String PARAM_KEY_POSSIBLE_OUTCOMES = "possibleOutcomes";
   private static final Logger log = Log4JUtils.getLogger(WeightedRandomOutcomeProcessors.class);
   private static final ThreadLocal<SecureRandom> randomGeneratorTLS = new ThreadLocal<SecureRandom>() {
      protected SecureRandom initialValue() {
         return new SecureRandom();
      }
   };

   public RewardProgramOutcomeData getOutcome(RewardProgramData rewardProgramData, RewardProgramTrigger trigger) throws Exception {
      if (rewardProgramData.hasParameter(PARAM_KEY_POSSIBLE_OUTCOMES)) {
         String possibleOutcomesJSONStr = rewardProgramData.getStringParam(PARAM_KEY_POSSIBLE_OUTCOMES, (String)null);
         if (log.isDebugEnabled()) {
            log.debug("possibleOutcomesJSONStr:" + possibleOutcomesJSONStr);
         }

         JSONObject jsonObject = new JSONObject(possibleOutcomesJSONStr);
         OutcomeWeightsDataList possibleOutcomes = OutcomeWeightsDataList.create(jsonObject);
         OutcomeWeightData pickedOutcome = randomlyPick(possibleOutcomes);
         if (pickedOutcome == null) {
            return null;
         }

         String outcomeRewardParameterName = pickedOutcome.getOutcome();
         String rewardProgramOutcomeJSONStr = rewardProgramData.getStringParam(outcomeRewardParameterName, (String)null);
         if (!StringUtil.isBlank(rewardProgramOutcomeJSONStr)) {
            return RewardProgramOutcomeDataDeserializer.deserialize(new JSONObject(rewardProgramOutcomeJSONStr));
         }

         log.error("Missing reward parameter:[" + outcomeRewardParameterName + "] for reward program [" + rewardProgramData.id + "]");
      }

      return null;
   }

   public static OutcomeWeightData randomlyPick(OutcomeWeightsDataList possibleOutcomes) throws JSONException {
      if (possibleOutcomes.count() == 0) {
         return null;
      } else {
         SecureRandom randomGenerator = (SecureRandom)randomGeneratorTLS.get();
         int randomNum = randomGenerator.nextInt(possibleOutcomes.sumOfWeights()) + 1;
         int accumulatedWeights = 0;

         for(int i = 0; i < possibleOutcomes.count(); ++i) {
            OutcomeWeightData outcome = possibleOutcomes.get(i);
            accumulatedWeights += outcome.getWeight();
            if (randomNum <= accumulatedWeights) {
               return outcome;
            }
         }

         log.warn("Couldn't get any possible outcome from " + possibleOutcomes.toJSONObject() + ".Bug?");
         return possibleOutcomes.get(possibleOutcomes.count() - 1);
      }
   }
}
