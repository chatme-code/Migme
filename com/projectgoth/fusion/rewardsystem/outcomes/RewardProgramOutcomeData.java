package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.data.JSONSerializable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class RewardProgramOutcomeData implements Serializable, JSONSerializable<RewardProgramOutcomeData> {
   public static final String FIELD_TYPE = "_t";
   public static final String FIELD_VERSION = "_v";
   public RewardProgramOutcomeData.TypeEnum type;
   public String version;

   protected RewardProgramOutcomeData(RewardProgramOutcomeData.TypeEnum type) {
      this.type = type;
   }

   public final JSONObject toJSONObject() throws JSONException {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("_v", this.currentDataFormatVersion());
      jsonObject.put("_t", this.type.value);
      this.serializeToJSONObject(jsonObject);
      return jsonObject;
   }

   protected abstract String currentDataFormatVersion();

   protected abstract void serializeToJSONObject(JSONObject var1) throws JSONException;

   public final RewardProgramOutcomeData fromJSONObject(JSONObject jsonObject) throws JSONException {
      this.deserializeFromJSONObject(jsonObject);
      this.type = RewardProgramOutcomeData.TypeEnum.fromCode(jsonObject.getInt("_t"));
      this.version = jsonObject.getString("_v");
      return this;
   }

   protected abstract void deserializeFromJSONObject(JSONObject var1) throws JSONException;

   protected void setTemplateDataValue(int currentIndex, Map<String, String> templateDataMap, String paramName, Object value) {
      if (value != null) {
         templateDataMap.put(this.type.name() + "[" + currentIndex + "]." + paramName, value.toString());
      }

   }

   public void populateTemplateDataMap(int currentIndex, Map<String, String> templateDataMap) {
   }

   public boolean requiresTemplateData() {
      return false;
   }

   public static enum TypeEnum implements EnumUtils.IEnumValueGetter<Integer> {
      BASIC(1),
      UNLOCKED_STORE_ITEMS(2),
      EMAIL_TEMPLATE_ID(3),
      IMNOTIFICATION_TEMPLATE(4);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public Integer getEnumValue() {
         return this.value;
      }

      public int getCode() {
         return this.value;
      }

      public static RewardProgramOutcomeData.TypeEnum fromCode(int value) {
         return (RewardProgramOutcomeData.TypeEnum)RewardProgramOutcomeData.TypeEnum.SingletonHolder.LOOKUP_BY_CODE.get(value);
      }

      private static class SingletonHolder {
         public static final Map<Integer, RewardProgramOutcomeData.TypeEnum> LOOKUP_BY_CODE = EnumUtils.buildLookUpMap(new HashMap(), RewardProgramOutcomeData.TypeEnum.class);
      }
   }
}
