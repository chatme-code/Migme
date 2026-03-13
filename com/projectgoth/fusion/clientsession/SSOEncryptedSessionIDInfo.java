package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class SSOEncryptedSessionIDInfo {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SSOEncryptedSessionIDInfo.class));
   public String sid;
   public long ex;
   public int uid;
   public String un;
   public String ip;
   public String ua;

   public SSOEncryptedSessionIDInfo(String sessionID, int userid, String username, String ipAddress, String userAgent) {
      this(sessionID, userid, username, ipAddress, userAgent, System.currentTimeMillis() / 1000L + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.SESSION_INITIAL_EXPIRY_IN_SECONDS));
   }

   public SSOEncryptedSessionIDInfo(String sessionID, int userid, String username, String ipAddress, String userAgent, long expiry) {
      this.sid = sessionID;
      this.uid = userid;
      this.un = username;
      this.ip = ipAddress;
      this.ua = userAgent;
      this.ex = expiry;
   }

   public SSOEncryptedSessionIDInfo(int userid, String username, SSOLoginSessionInfo sessionInfo) {
      this(sessionInfo.sessionID, userid, username, sessionInfo.remoteAddress, sessionInfo.userAgent);
   }

   public String toString() {
      return String.format("[sid=%s, ex=%d, uid=%d, un=%s, ip=%s, ua=%s]", this.sid, this.ex, this.uid, this.un, this.ip, this.ua);
   }

   public String toBase64JSON() {
      String jsonStr = this.toJSON();
      return jsonStr == null ? null : new String(Base64.encodeBase64(jsonStr.getBytes()));
   }

   public String toJSON() {
      JSONObject root = new JSONObject();

      try {
         root.put("sid", this.sid);
         root.put("ex", this.ex);
         root.put("uid", this.uid);
         root.put("un", this.un);
         root.put("ip", this.ip);
         root.put("ua", this.ua);
         return root.toString();
      } catch (JSONException var3) {
         log.error(String.format("Unable to encode encrypted session id into json for %s", this), var3);
         return null;
      }
   }

   public static SSOEncryptedSessionIDInfo fromBase64JSON(String encodedStringBase64) {
      log.debug(String.format("Decoding encrypted session id %s", encodedStringBase64));
      if (StringUtil.isBlank(encodedStringBase64)) {
         return null;
      } else {
         String encodedString = new String(Base64.decodeBase64(encodedStringBase64.getBytes()));
         return fromJSON(encodedString);
      }
   }

   public static SSOEncryptedSessionIDInfo fromJSON(String encodedString) {
      try {
         JSONObject root = new JSONObject(encodedString);
         String sessionID = root.optString("sid", (String)null);
         if (StringUtil.isBlank(sessionID)) {
            log.error(String.format("sid field in encoded session id value is blank"));
            return null;
         } else {
            long expiry = root.optLong("ex", -1L);
            if (expiry == -1L) {
               log.error(String.format("ex field in encoded session id value is blank"));
               return null;
            } else {
               int userid = root.optInt("uid", -1);
               if (userid == -1) {
                  log.error(String.format("uid field in encoded session id value is blank"));
                  return null;
               } else {
                  String username = root.optString("un", (String)null);
                  if (StringUtil.isBlank(username)) {
                     log.error(String.format("un field in encoded session id value is blank"));
                     return null;
                  } else {
                     String ipAddress = root.optString("ip", (String)null);
                     if (StringUtil.isBlank(ipAddress)) {
                        log.error(String.format("ip field in encoded session id value is blank"));
                        return null;
                     } else {
                        String userAgent = root.optString("ua", (String)null);
                        if (StringUtil.isBlank(userAgent)) {
                           log.error(String.format("ua field in encoded session id value is blank"));
                           return null;
                        } else {
                           return new SSOEncryptedSessionIDInfo(sessionID, userid, username, ipAddress, userAgent, expiry);
                        }
                     }
                  }
               }
            }
         }
      } catch (JSONException var9) {
         log.error(String.format("Unable to decode JSON session id %s", encodedString), var9);
         return null;
      }
   }
}
