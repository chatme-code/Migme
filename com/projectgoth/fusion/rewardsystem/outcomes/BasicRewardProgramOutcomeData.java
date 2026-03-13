package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.common.JSONUtils;
import com.projectgoth.fusion.common.StringUtil;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class BasicRewardProgramOutcomeData extends RewardProgramOutcomeData {
   private static final String CURRENT_DATA_FORMAT_VERSION = "1.0";
   public Double migCreditAmount;
   public String migCreditCurrency;
   public int scoreReward;
   public int migLevelReward;
   public String accountEntryRemarks;
   private static final String FIELD_MIG_CREDIT_AMOUNT = "migCrAmt";
   private static final String FIELD_MIG_CREDIT_CURRENCY = "migCrCcy";
   private static final String FIELD_SCORE_REWARD = "scoreRwd";
   private static final String FIELD_MIGLEVEL_REWARD = "migLvlRwd";
   private static final String FIELD_ACCOUNT_ENTRY_REMARKS = "acEntryRmk";

   public BasicRewardProgramOutcomeData() {
      super(RewardProgramOutcomeData.TypeEnum.BASIC);
   }

   protected void serializeToJSONObject(JSONObject jsonObject) throws JSONException {
      if (this.migCreditAmount != null) {
         jsonObject.put("migCrAmt", this.migCreditAmount);
         jsonObject.put("migCrCcy", this.migCreditCurrency);
      }

      if (this.scoreReward > 0) {
         jsonObject.put("scoreRwd", this.scoreReward);
      }

      if (this.migLevelReward > 0) {
         jsonObject.put("migLvlRwd", this.migLevelReward);
      }

      if (!StringUtil.isBlank(this.accountEntryRemarks)) {
         jsonObject.put("acEntryRmk", this.accountEntryRemarks);
      }

   }

   protected String currentDataFormatVersion() {
      return "1.0";
   }

   protected void deserializeFromJSONObject(JSONObject jsonObject) throws JSONException {
      Double migCreditAmountDouble = JSONUtils.getDouble(jsonObject, "migCrAmt");
      String migCreditCcy;
      if (migCreditAmountDouble != null) {
         migCreditCcy = jsonObject.getString("migCrCcy");
      } else {
         migCreditCcy = null;
      }

      int scoreReward = jsonObject.optInt("scoreRwd", 0);
      int migLevelReward = jsonObject.optInt("migLvlRwd", 0);
      String accEntryRemark = jsonObject.optString("acEntryRmk");
      this.migCreditAmount = migCreditAmountDouble;
      this.migCreditCurrency = migCreditCcy;
      this.scoreReward = scoreReward;
      this.migLevelReward = migLevelReward;
      this.accountEntryRemarks = accEntryRemark;
   }

   public void populateTemplateDataMap(int currentIndex, Map<String, String> templateDataMap) {
      if (this.migCreditAmount != null && this.migCreditAmount > 0.0D) {
         this.setTemplateDataValue(currentIndex, templateDataMap, "migCreditAmount", this.migCreditAmount);
      }

      if (!StringUtil.isBlank(this.migCreditCurrency)) {
         this.setTemplateDataValue(currentIndex, templateDataMap, "migCreditCurrency", this.migCreditCurrency);
      }

      if (this.scoreReward > 0) {
         this.setTemplateDataValue(currentIndex, templateDataMap, "scoreReward", this.scoreReward);
      }

      if (this.migLevelReward > 0) {
         this.setTemplateDataValue(currentIndex, templateDataMap, "migLevelReward", this.migLevelReward);
      }

   }

   public boolean requiresTemplateData() {
      return false;
   }
}
