package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class FacebookCredentialData {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FacebookCredentialData.class));
   public String id;
   public String key;

   public FacebookCredentialData() {
   }

   public FacebookCredentialData(String id, String key) {
      this.id = id;
      this.key = key;
   }

   public void fromJSONString(String jsonStr) throws JSONException {
      JSONObject jsonObj = new JSONObject(jsonStr);
      this.id = jsonObj.getString("id");
      this.key = jsonObj.getString("key");
   }

   public String toJSONString() throws JSONException {
      return this.toJSONObject().toString();
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("id", this.id);
      jsonObj.put("key", this.key);
      return jsonObj;
   }
}
