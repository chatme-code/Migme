package com.projectgoth.fusion.restapi.data;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class RSSFeedForUser implements Comparable<RSSFeedForUser> {
   private static final Logger log = Logger.getLogger(RSSFeedForUser.class);
   public static final String CURRENT_JSON_SCHEMA_VERSION = "1.0";
   private static final String RSSFEED_ID_FORMAT = "%d-%s";
   public int userId;
   public URL url;
   public String id;
   public String _version;
   public String description;

   public RSSFeedForUser() {
   }

   public RSSFeedForUser(int userId, String url) throws MalformedURLException {
      this._version = "1.0";
      this.url = new URL(url);
      this.userId = userId;
      this.id = this.getID();
      this.description = "";
   }

   private String getID() {
      return String.format("%d-%s", this.userId, DigestUtils.md5Hex(this.url.toString()).toString());
   }

   public String toJSONString() {
      return this.toJSONObject().toString();
   }

   public JSONObject toJSONObject() {
      JSONObject obj = new JSONObject();

      try {
         obj.put("id", this.id);
         obj.put("_version", this._version);
         obj.put("url", this.url.toString());
         obj.put("userId", this.userId);
         obj.put("description", this.description);
      } catch (JSONException var3) {
         log.error("Unable to convert RSSFeedForUser object to JSON " + var3.getMessage());
      }

      return obj;
   }

   public int compareTo(RSSFeedForUser other) {
      return this.id.compareTo(other.id);
   }
}
