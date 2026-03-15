package com.projectgoth.fusion.common;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
   public static boolean hasNonNullProperty(JSONObject jsonObj, String key) {
      return jsonObj.has(key) && !jsonObj.isNull(key);
   }

   public static String getString(JSONObject jsonObj, String key) throws JSONException {
      return jsonObj.has(key) ? jsonObj.getString(key) : null;
   }

   public static Integer getInteger(JSONObject jsonObj, String key) throws JSONException {
      return jsonObj.has(key) && !jsonObj.isNull(key) ? jsonObj.getInt(key) : null;
   }

   public static int getInteger(JSONObject jsonObj, String key, int defaultValue) throws JSONException {
      Integer val = getInteger(jsonObj, key);
      return val == null ? defaultValue : val;
   }

   public static Double getDouble(JSONObject jsonObj, String key) throws JSONException {
      return jsonObj.has(key) && !jsonObj.isNull(key) ? jsonObj.getDouble(key) : null;
   }

   public static JSONObject getJSONObject(JSONObject jsonObj, String key) throws JSONException {
      return jsonObj.has(key) && !jsonObj.isNull(key) ? jsonObj.getJSONObject(key) : null;
   }

   public static double getDouble(JSONObject jsonObj, String key, double defaultValue) throws JSONException {
      Double val = getDouble(jsonObj, key);
      return val == null ? defaultValue : val;
   }

   public static Long getLong(JSONObject jsonObj, String key) throws JSONException {
      return jsonObj.has(key) && !jsonObj.isNull(key) ? jsonObj.getLong(key) : null;
   }
}
