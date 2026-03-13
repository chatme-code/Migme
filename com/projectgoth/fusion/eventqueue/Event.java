package com.projectgoth.fusion.eventqueue;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Event {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Event.class));
   public String eventSubject;
   public Long timestamp;
   public Enums.EventTypeEnum type;
   public HashMap<String, String> params;
   private static final String PARAMS_KEY = "params";
   private static final String TYPE_KEY = "type";
   private static final String TIMESTAMP_KEY = "timestamp";
   private static final String SUBJECT_KEY = "subject";
   private boolean isBeingProcessed = false;

   protected Event(Enums.EventTypeEnum type) {
      this.eventSubject = "";
      this.type = type;
      this.timestamp = System.currentTimeMillis();
      this.params = new HashMap();
   }

   protected Event(String eventSubject, Enums.EventTypeEnum type) {
      this.eventSubject = eventSubject;
      this.type = type;
      this.timestamp = System.currentTimeMillis();
      this.params = new HashMap();
   }

   public void putParameter(String key, String value) {
      this.params.put(key, value);
   }

   public String getParameter(String key) {
      return (String)this.params.get(key);
   }

   public String toJSONString() {
      return this.toJSONString(false);
   }

   public String toJSONString(boolean pretty) {
      String jsonStr = "";

      try {
         JSONObject o = new JSONObject();
         o.put("subject", this.eventSubject);
         o.put("timestamp", this.timestamp);
         o.put("type", this.type.value);
         JSONObject p = new JSONObject();
         Iterator i$ = this.params.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, String> entry = (Entry)i$.next();
            p.put((String)entry.getKey(), entry.getValue());
         }

         o.put("params", p);
         if (!pretty) {
            jsonStr = o.toString();
         } else {
            jsonStr = o.toString(4);
         }
      } catch (JSONException var7) {
         log.error("Unable to serialize event to JSON String", var7);
      }

      return jsonStr;
   }

   public static Event fromJSONString(String jsonString) {
      Event e = null;

      try {
         JSONObject o = new JSONObject(jsonString);
         String subject = o.getString("subject");
         Long timestamp = o.getLong("timestamp");
         Enums.EventTypeEnum type = Enums.EventTypeEnum.fromValue(o.getInt("type"));
         JSONObject p = o.getJSONObject("params");
         Iterator<String> keys = p.keys();
         HashMap params = new HashMap();

         while(keys.hasNext()) {
            String key = (String)keys.next();
            params.put(key, p.getString(key));
         }

         e = EventFactory.createEvent(type);
         if (e != null) {
            e.eventSubject = subject;
            e.timestamp = timestamp;
            e.params = params;
         }
      } catch (JSONException var10) {
         log.error(String.format("Error parsing JSON String [%s] [%s]", jsonString, var10.getMessage()), var10);
      }

      return e;
   }

   public final boolean execute() {
      log.info(String.format("Executing event for username [%s]", this.eventSubject));

      boolean var2;
      try {
         this.isBeingProcessed = true;
         boolean var1 = this.executeInternal();
         return var1;
      } catch (Exception var7) {
         log.error(String.format("Failed to submit event [%s] for [%s].", this.type.description, this.eventSubject), var7);
         var2 = false;
      } finally {
         log.info(String.format("Execution of event for username [%s] completed.", this.eventSubject));
         this.isBeingProcessed = false;
      }

      return var2;
   }

   public boolean executeInternal() throws Exception {
      log.info(String.format("Looking up userid for username [%s]", this.eventSubject));
      User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      int userid = userBean.getUserID(this.eventSubject, (Connection)null, false);
      if (userid == -1) {
         log.error(String.format("Unable to execute event: UserID not found for [%s]", this.eventSubject));
         return false;
      } else {
         log.info(String.format("Found userid [%d] for username[%s]", userid, this.eventSubject));
         String jsonStr = this.toJSONString();
         String pathPrefix = String.format("/user/%d/activity", userid);
         log.info("Making API call to PUT " + pathPrefix);
         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         JSONObject response = apiUtil.put(pathPrefix, jsonStr);
         log.info(String.format("Activity [%s] [%d-%d-%d] for [%s] submitted successfully: response : %s", this.type.description, userid, this.type.value, this.timestamp, this.eventSubject, response.toString()));
         return true;
      }
   }

   public boolean isBeingProcessed() {
      return this.isBeingProcessed;
   }

   public boolean equals(Event o) {
      if (o.getClass().getName().equals(this.getClass().getName()) && o.eventSubject.equals(this.eventSubject) && o.timestamp.equals(this.timestamp)) {
         Iterator keys = this.params.keySet().iterator();

         String key;
         do {
            if (!keys.hasNext()) {
               return true;
            }

            key = (String)keys.next();
         } while(((String)this.params.get(key)).equals(o.getParameter(key)));

         return false;
      } else {
         return false;
      }
   }

   public boolean retryOnFailure() {
      return false;
   }
}
