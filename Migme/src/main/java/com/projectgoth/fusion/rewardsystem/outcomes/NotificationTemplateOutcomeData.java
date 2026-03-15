package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.common.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class NotificationTemplateOutcomeData extends RewardProgramOutcomeData {
   private static final String CURRENT_DATA_FORMAT_VERSION = "1.0";
   private static final String FIELD_SUBJECT_TEMPLATE = "subjTmplt";
   private static final String FIELD_BODY_TEMPLATE = "bodyTmplt";
   private String subjectTemplate;
   private String contentTemplate;

   protected NotificationTemplateOutcomeData(RewardProgramOutcomeData.TypeEnum type, String subjectTemplate, String contentTemplate) {
      this(type);
      this.subjectTemplate = contentTemplate;
      this.contentTemplate = contentTemplate;
   }

   public String getContentTemplate() {
      return this.contentTemplate;
   }

   public String getSubjectTemplate() {
      return this.subjectTemplate;
   }

   protected NotificationTemplateOutcomeData(RewardProgramOutcomeData.TypeEnum type) {
      super(type);
   }

   protected String currentDataFormatVersion() {
      return "1.0";
   }

   protected void serializeToJSONObject(JSONObject jsonObject) throws JSONException {
      if (!StringUtil.isBlank(this.contentTemplate)) {
         jsonObject.put("bodyTmplt", this.contentTemplate);
      }

      if (!StringUtil.isBlank(this.subjectTemplate)) {
         jsonObject.put("subjTmplt", this.subjectTemplate);
      }

   }

   protected void deserializeFromJSONObject(JSONObject jsonObject) throws JSONException {
      this.subjectTemplate = jsonObject.optString("subjTmplt", "");
      this.contentTemplate = jsonObject.optString("bodyTmplt", "");
   }

   public final boolean requiresTemplateData() {
      return true;
   }
}
