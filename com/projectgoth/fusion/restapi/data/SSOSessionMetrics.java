package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.common.ConfigUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class SSOSessionMetrics {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SSOSessionMetrics.class));
   Map<String, Integer> counters;
   Map<String, String> records;
   String sessionID;
   SSOEnums.View view;
   Long lastupdatedTimestampInSec;
   Long sessionStartTimeInSec;

   private SSOSessionMetrics(String sessionID, SSOEnums.View view, Long sessionStartTime) throws Exception {
      this.sessionID = sessionID;
      this.view = view;
      this.sessionStartTimeInSec = sessionStartTime;
      this.counters = new HashMap();
      this.records = new HashMap();
   }

   public static SSOSessionMetrics fromMap(String sessionID, SSOEnums.View view, Long sessionStartTime, Map<String, String> map) throws Exception {
      SSOSessionMetrics metrics = new SSOSessionMetrics(sessionID, view, sessionStartTime);
      String timestampStr = (String)map.get("timestamp");

      try {
         metrics.lastupdatedTimestampInSec = Long.parseLong(timestampStr);
      } catch (Exception var12) {
         log.warn(String.format("Unable to parse timestamp information [%s]", timestampStr), var12);
         metrics.lastupdatedTimestampInSec = new Long(System.currentTimeMillis() / 1000L);
      }

      map.remove("sessionStartTime");
      map.remove("timestamp");
      Iterator i$ = map.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, String> entry = (Entry)i$.next();
         String key = (String)entry.getKey();
         String value = (String)entry.getValue();

         try {
            metrics.counters.put(key, Integer.parseInt(value));
         } catch (NumberFormatException var11) {
            metrics.records.put(key, value);
         }
      }

      return metrics;
   }

   public static SSOSessionMetrics fromJSONString(String sessionID, SSOEnums.View view, String username, Long sessionStartTime, String jsonStrPost) throws Exception {
      log.debug(String.format("Received [%s][%s][%s][%d][%s]", sessionID, view.toString(), username, sessionStartTime, jsonStrPost));
      SSOSessionMetrics metrics = new SSOSessionMetrics(sessionID, view, sessionStartTime);
      JSONObject jsonPost = new JSONObject(jsonStrPost);
      metrics.lastupdatedTimestampInSec = jsonPost.optLong("timestamp", System.currentTimeMillis() / 1000L);
      metrics.counters = new HashMap();
      metrics.records = new HashMap();
      JSONObject c = jsonPost.optJSONObject("counters");
      if (c != null) {
         Iterator it = c.keys();

         while(it.hasNext()) {
            String key = (String)it.next();
            metrics.counters.put(key, new Integer(c.getInt(key)));
         }
      }

      JSONObject r = jsonPost.optJSONObject("records");
      if (r != null) {
         Iterator it = r.keys();

         while(it.hasNext()) {
            String key = (String)it.next();
            metrics.records.put(key, r.getString(key));
         }
      }

      metrics.records.put("username", username);
      return metrics;
   }

   public String toJSONString() {
      JSONObject o = new JSONObject();

      try {
         o.put("sessionID", this.sessionID);
         o.put("view", this.view.toString());
         o.put("sessionStartTime", this.sessionStartTimeInSec);
         o.put("timestamp", this.lastupdatedTimestampInSec);
         JSONObject c = new JSONObject();
         Iterator i$ = this.counters.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Integer> entry = (Entry)i$.next();
            c.put((String)entry.getKey(), entry.getValue());
         }

         o.put("counters", c);
         JSONObject r = new JSONObject();
         Iterator i$ = this.records.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, String> entry = (Entry)i$.next();
            r.put((String)entry.getKey(), entry.getValue());
         }

         o.put("records", r);
      } catch (JSONException var6) {
         log.warn("Unable to convert to JSON string :", var6);
      }

      return o.toString();
   }

   public Long getSessionStartTime() {
      return this.sessionStartTimeInSec;
   }

   public void setSessionStartTime(Long sessionStartTime) {
      this.sessionStartTimeInSec = sessionStartTime;
   }

   public Long getTimestamp() {
      return this.lastupdatedTimestampInSec;
   }

   public void setTimestamp(Long timestamp) {
      this.lastupdatedTimestampInSec = timestamp;
   }

   public Map<String, Integer> getCounters() {
      return this.counters;
   }

   public void setCounters(Map<String, Integer> counters) {
      this.counters = counters;
   }

   public Map<String, String> getRecords() {
      return this.records;
   }

   public void setRecords(Map<String, String> records) {
      this.records = records;
   }

   public String getSessionID() {
      return this.sessionID;
   }

   public SSOEnums.View getView() {
      return this.view;
   }
}
