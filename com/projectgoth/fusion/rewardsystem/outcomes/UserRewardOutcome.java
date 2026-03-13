package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.JSONSerializable;
import com.projectgoth.leto.common.outcome.Outcomes;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserRewardOutcome implements Outcomes, Serializable, JSONSerializable<UserRewardOutcome> {
   private static final String DATA_FORMAT_VERSION_1_2 = "1.2";
   /** @deprecated */
   @Deprecated
   private static final String DATA_FORMAT_VERSION_1_1 = "1.1";
   /** @deprecated */
   @Deprecated
   private static final String DATA_FORMAT_VERSION_1_0 = "1.0";
   private static final String FIELD_VERSION = "_v";
   private static final String FIELD_USERID = "uid";
   private static final String FIELD_OUTCOME_DATA_LIST = "ocdLst";
   private static final String FIELD_TEMPLATE_DATA = "tmpltData";
   private static final String FIELD_CREATE_TS = "createTs";
   private static final String FIELD_ORIGIN = "origin";
   private static final String FIELD_ID = "id";
   private int userid;
   private List<RewardProgramOutcomeData> outcomeDataList;
   private Map<String, String> templateData;
   private String id;
   private String origin;
   private long createTs;
   private int outcomeType;

   public UserRewardOutcome() {
      this.outcomeType = 1;
   }

   public UserRewardOutcome(String id, String origin, long createTs) {
      this();
      this.id = id;
      this.origin = origin;
      this.createTs = createTs;
   }

   public List<RewardProgramOutcomeData> getOutcomeDataList() {
      return this.outcomeDataList;
   }

   public void setOutcomeDataList(List<RewardProgramOutcomeData> outcomeDataList) {
      this.outcomeDataList = outcomeDataList;
   }

   public int getUserid() {
      return this.userid;
   }

   public void setUserid(int userid) {
      this.userid = userid;
   }

   public Map<String, String> getTemplateData() {
      return this.templateData;
   }

   public void setTemplateData(Map<String, String> templateData) {
      this.templateData = templateData;
   }

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getOrigin() {
      return this.origin;
   }

   public void setOrigin(String origin) {
      this.origin = origin;
   }

   public long getCreateTs() {
      return this.createTs;
   }

   public void setCreateTs(long createTs) {
      this.createTs = createTs;
   }

   public JSONObject toJSONObject() throws JSONException {
      return this.toJSONObject_1_2();
   }

   protected final JSONObject toJSONObject_1_2() throws JSONException {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("id", this.id);
      jsonObject.put("type", this.outcomeType);
      jsonObject.put("createTs", this.createTs);
      jsonObject.put("origin", this.origin);
      jsonObject.put("_v", "1.2");
      jsonObject.put("uid", this.userid);
      if (this.outcomeDataList != null) {
         JSONArray outcomeDataJsonArray = new JSONArray();
         Iterator i$ = this.outcomeDataList.iterator();

         while(i$.hasNext()) {
            RewardProgramOutcomeData outcomeData = (RewardProgramOutcomeData)i$.next();
            if (outcomeData != null) {
               outcomeDataJsonArray.put(outcomeData.toJSONObject());
            }
         }

         if (outcomeDataJsonArray.length() != 0) {
            jsonObject.put("ocdLst", outcomeDataJsonArray);
         }
      }

      if (this.templateData != null && this.templateData.size() > 0) {
         jsonObject.put("tmpltData", new JSONObject(this.templateData));
      }

      return jsonObject;
   }

   /** @deprecated */
   @Deprecated
   protected final JSONObject toJSONObject_1_1() throws JSONException {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("id", this.id);
      jsonObject.put("createTs", this.createTs);
      jsonObject.put("origin", this.origin);
      jsonObject.put("_v", "1.1");
      jsonObject.put("uid", this.userid);
      if (this.outcomeDataList != null) {
         JSONArray outcomeDataJsonArray = new JSONArray();
         Iterator i$ = this.outcomeDataList.iterator();

         while(i$.hasNext()) {
            RewardProgramOutcomeData outcomeData = (RewardProgramOutcomeData)i$.next();
            if (outcomeData != null) {
               outcomeDataJsonArray.put(outcomeData.toJSONObject());
            }
         }

         if (outcomeDataJsonArray.length() != 0) {
            jsonObject.put("ocdLst", outcomeDataJsonArray);
         }
      }

      if (this.templateData != null && this.templateData.size() > 0) {
         jsonObject.put("tmpltData", new JSONObject(this.templateData));
      }

      return jsonObject;
   }

   /** @deprecated */
   @Deprecated
   protected final JSONObject toJSONObject_1_0() throws JSONException {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("_v", "1.0");
      jsonObject.put("uid", this.userid);
      if (this.outcomeDataList != null) {
         JSONArray outcomeDataJsonArray = new JSONArray();
         Iterator i$ = this.outcomeDataList.iterator();

         while(i$.hasNext()) {
            RewardProgramOutcomeData outcomeData = (RewardProgramOutcomeData)i$.next();
            if (outcomeData != null) {
               outcomeDataJsonArray.put(outcomeData.toJSONObject());
            }
         }

         if (outcomeDataJsonArray.length() != 0) {
            jsonObject.put("ocdLst", outcomeDataJsonArray);
         }
      }

      if (this.templateData != null && this.templateData.size() > 0) {
         jsonObject.put("tmpltData", new JSONObject(this.templateData));
      }

      return jsonObject;
   }

   public UserRewardOutcome fromJSONObject(JSONObject jsonObject) throws JSONException {
      String version = jsonObject.getString("_v");
      if (StringUtil.equals("1.2", version)) {
         return this.fromJSONObject_1_2(jsonObject);
      } else if (StringUtil.equals("1.1", version)) {
         return this.fromJSONObject_1_1(jsonObject);
      } else if (StringUtil.equals("1.0", version)) {
         return this.fromJSONObject_1_0(jsonObject);
      } else {
         throw new JSONException("Unsupported version [" + version + "]");
      }
   }

   protected final UserRewardOutcome fromJSONObject_1_2(JSONObject jsonObject) throws JSONException {
      int outcomeType = jsonObject.optInt("type", 1);
      if (outcomeType != 1) {
         throw new JSONException("Expected value for field [type] is [1] found [" + outcomeType + "] instead");
      } else {
         String id = jsonObject.getString("id");
         String origin = jsonObject.getString("origin");
         long createTs = jsonObject.getLong("createTs");
         int userid = jsonObject.getInt("uid");
         JSONArray outcomeDataJsonArray = jsonObject.optJSONArray("ocdLst");
         ArrayList outcomeDataList;
         if (outcomeDataJsonArray != null && outcomeDataJsonArray.length() != 0) {
            outcomeDataList = new ArrayList(outcomeDataJsonArray.length());

            for(int i = 0; i < outcomeDataJsonArray.length(); ++i) {
               JSONObject outcomeDataJson = outcomeDataJsonArray.getJSONObject(i);
               outcomeDataList.add(RewardProgramOutcomeDataDeserializer.deserialize(outcomeDataJson));
            }
         } else {
            outcomeDataList = null;
         }

         JSONObject templateDataJSON = jsonObject.optJSONObject("tmpltData");
         HashMap templateDataMap;
         if (templateDataJSON != null && templateDataJSON.length() > 0) {
            templateDataMap = new HashMap();
            Iterator jsonKeys = templateDataJSON.keys();

            while(jsonKeys.hasNext()) {
               String jsonKey = (String)jsonKeys.next();
               templateDataMap.put(jsonKey, templateDataJSON.getString(jsonKey));
            }
         } else {
            templateDataMap = null;
         }

         this.id = id;
         this.origin = origin;
         this.createTs = createTs;
         this.userid = userid;
         this.outcomeDataList = outcomeDataList;
         this.templateData = templateDataMap;
         return this;
      }
   }

   /** @deprecated */
   @Deprecated
   protected final UserRewardOutcome fromJSONObject_1_1(JSONObject jsonObject) throws JSONException {
      String id = jsonObject.getString("id");
      String origin = jsonObject.getString("origin");
      long createTs = jsonObject.getLong("createTs");
      int userid = jsonObject.getInt("uid");
      JSONArray outcomeDataJsonArray = jsonObject.optJSONArray("ocdLst");
      ArrayList outcomeDataList;
      if (outcomeDataJsonArray != null && outcomeDataJsonArray.length() != 0) {
         outcomeDataList = new ArrayList(outcomeDataJsonArray.length());

         for(int i = 0; i < outcomeDataJsonArray.length(); ++i) {
            JSONObject outcomeDataJson = outcomeDataJsonArray.getJSONObject(i);
            outcomeDataList.add(RewardProgramOutcomeDataDeserializer.deserialize(outcomeDataJson));
         }
      } else {
         outcomeDataList = null;
      }

      JSONObject templateDataJSON = jsonObject.optJSONObject("tmpltData");
      HashMap templateDataMap;
      if (templateDataJSON != null && templateDataJSON.length() > 0) {
         templateDataMap = new HashMap();
         Iterator jsonKeys = templateDataJSON.keys();

         while(jsonKeys.hasNext()) {
            String jsonKey = (String)jsonKeys.next();
            templateDataMap.put(jsonKey, templateDataJSON.getString(jsonKey));
         }
      } else {
         templateDataMap = null;
      }

      this.id = id;
      this.origin = origin;
      this.createTs = createTs;
      this.userid = userid;
      this.outcomeDataList = outcomeDataList;
      this.templateData = templateDataMap;
      return this;
   }

   /** @deprecated */
   @Deprecated
   protected final UserRewardOutcome fromJSONObject_1_0(JSONObject jsonObject) throws JSONException {
      int userid = jsonObject.getInt("uid");
      JSONArray outcomeDataJsonArray = jsonObject.optJSONArray("ocdLst");
      ArrayList outcomeDataList;
      if (outcomeDataJsonArray != null && outcomeDataJsonArray.length() != 0) {
         outcomeDataList = new ArrayList(outcomeDataJsonArray.length());

         for(int i = 0; i < outcomeDataJsonArray.length(); ++i) {
            JSONObject outcomeDataJson = outcomeDataJsonArray.getJSONObject(i);
            outcomeDataList.add(RewardProgramOutcomeDataDeserializer.deserialize(outcomeDataJson));
         }
      } else {
         outcomeDataList = null;
      }

      JSONObject templateDataJSON = jsonObject.optJSONObject("tmpltData");
      HashMap templateDataMap;
      if (templateDataJSON != null && templateDataJSON.length() > 0) {
         templateDataMap = new HashMap();
         Iterator jsonKeys = templateDataJSON.keys();

         while(jsonKeys.hasNext()) {
            String jsonKey = (String)jsonKeys.next();
            templateDataMap.put(jsonKey, templateDataJSON.getString(jsonKey));
         }
      } else {
         templateDataMap = null;
      }

      this.userid = userid;
      this.outcomeDataList = outcomeDataList;
      this.templateData = templateDataMap;
      return this;
   }

   public String getMetadataVersion() {
      return "1.2";
   }

   public Timestamp getCreateTimestamp() {
      return new Timestamp(this.createTs);
   }

   public int getOutcomeType() {
      return this.outcomeType;
   }

   public void setOutcomeType(int outcomeType) {
      this.outcomeType = outcomeType;
   }
}
