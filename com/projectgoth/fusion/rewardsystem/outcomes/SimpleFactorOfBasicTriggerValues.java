package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SimpleFactorOfBasicTriggerValues extends RewardProgramOutcomeProcessor {
   private static final Pattern TERM_SEPARATOR_PATTERN = Pattern.compile("\\s*\\*\\s*");
   public static final String PARAM_CREDIT_REWARD = "credit.reward";
   public static final String PARAM_SCORE_REWARD = "score.reward";
   public static final String PARAM_MIGLEVEL_REWARD = "miglevel.reward";
   public static final String PARAM_ACC_ENTRY_REMARKS = "acc.entry.remarks";
   public static final String FIELD_TRIGGER_AMOUNT = "trigger.amt";
   public static final String FIELD_TRIGGER_QUANTITY = "trigger.qty";

   public RewardProgramOutcomeData getOutcome(RewardProgramData data, RewardProgramTrigger trigger) {
      String creditRewardRule = data.getStringParam("credit.reward", "");
      String scoreRewardRule = data.getStringParam("score.reward", "");
      String migLevelRewardRule = data.getStringParam("miglevel.reward", "");
      String accEntryRewardRemarks = data.getStringParam("acc.entry.remarks", "");
      BasicRewardProgramOutcomeData outcomeData = new BasicRewardProgramOutcomeData();
      Map<String, Double> triggerValuesMap = this.getTriggerValuesMap(trigger);
      Double reward;
      if (!StringUtil.isBlank(creditRewardRule)) {
         reward = this.eval(creditRewardRule, triggerValuesMap);
         if (reward != null) {
            outcomeData.migCreditAmount = reward;
            outcomeData.migCreditCurrency = trigger.currency;
         }
      }

      if (!StringUtil.isBlank(scoreRewardRule)) {
         reward = this.eval(scoreRewardRule, triggerValuesMap);
         if (reward != null) {
            outcomeData.scoreReward = reward.intValue();
         }
      }

      if (!StringUtil.isBlank(migLevelRewardRule)) {
         reward = this.eval(migLevelRewardRule, triggerValuesMap);
         if (reward != null) {
            outcomeData.migLevelReward = reward.intValue();
         }
      }

      if (!StringUtil.isBlank(accEntryRewardRemarks)) {
         outcomeData.accountEntryRemarks = accEntryRewardRemarks;
      } else {
         outcomeData.accountEntryRemarks = null;
      }

      return outcomeData;
   }

   private Map<String, Double> getTriggerValuesMap(RewardProgramTrigger trigger) {
      Map<String, Double> triggerValues = new HashMap();
      if (trigger.amountDelta > 0.0D) {
         triggerValues.put("trigger.amt", trigger.amountDelta);
      }

      if (trigger.quantityDelta > 0) {
         triggerValues.put("trigger.qty", (double)trigger.quantityDelta);
      }

      return triggerValues;
   }

   private Double eval(String multiplicationTerm, Map<String, Double> triggerValues) {
      String[] term = TERM_SEPARATOR_PATTERN.split(multiplicationTerm);
      if (term != null && term.length == 2) {
         double factor;
         try {
            factor = Double.parseDouble(term[0].trim());
         } catch (NumberFormatException var8) {
            throw new IllegalArgumentException("Unable to evaluate:[" + multiplicationTerm + "]", var8);
         }

         String triggerField = term[1].trim();
         Double value = (Double)triggerValues.get(triggerField);
         return value != null ? factor * value : null;
      } else {
         throw new IllegalArgumentException("Unable to evaluate:[" + multiplicationTerm + "]");
      }
   }
}
