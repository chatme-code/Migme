package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterCredentialData {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(TwitterCredentialData.class));
   public String id;
   public String key;
   public String secret;

   public TwitterCredentialData() {
   }

   public TwitterCredentialData(String username, String key, String secret) {
      this.id = username;
      this.key = key;
      this.secret = secret;
   }

   public void fromJSONString(String jsonStr) throws JSONException {
      JSONObject jsonObj = new JSONObject(jsonStr);
      this.id = jsonObj.optString("id");
      this.key = jsonObj.getString("key");
      this.secret = jsonObj.getString("secret");
   }

   public void fromJSONString(String id, String jsonStr) throws JSONException {
      this.id = id;
      JSONObject jsonObj = new JSONObject(jsonStr);
      this.key = jsonObj.getString("key");
      this.secret = jsonObj.getString("secret");
   }

   public String toJSONString() throws JSONException {
      return this.toJSONObject().toString();
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("id", this.id);
      jsonObj.put("key", this.key);
      jsonObj.put("secret", this.secret);
      return jsonObj;
   }

   public String toJSONStringAccessToken() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      jsonObj.put("key", this.key);
      jsonObj.put("secret", this.secret);
      return jsonObj.toString();
   }
}
