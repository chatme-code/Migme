package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.restapi.data.SSOCheckResponseData;
import org.apache.log4j.Logger;

public class SSOLoginData {
   public String username = "";
   public Integer userID = null;
   public Integer presence = null;
   public Integer voiceCapability = null;
   public Short clientVersion = null;
   public String mobileDevice = "";
   public String userAgent = "";
   public Integer deviceType;
   public Integer passwordHash;
   public Long sessionStartTimeInMillis;
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SSOLoginData.class));
   public static final String DATA_SEPARATOR = "##";

   public SSOLoginData() {
      this.deviceType = Integer.valueOf(ClientType.AJAX1.value());
      this.passwordHash = null;
      this.sessionStartTimeInMillis = null;
   }

   public String toString() {
      return this.username + "##" + Integer.toString(this.userID != null ? this.userID : 0) + "##" + Integer.toString(this.presence != null ? this.presence : 0) + "##" + Integer.toString(0) + "##" + Short.toString(this.clientVersion != null ? this.clientVersion : 0) + "##" + (this.mobileDevice != null ? this.mobileDevice : "") + "##" + (this.userAgent != null ? this.userAgent : "") + "##" + Integer.toString(this.passwordHash != null ? this.passwordHash : 0) + "##" + Long.toString(this.sessionStartTimeInMillis != null ? this.sessionStartTimeInMillis : 0L);
   }

   public SSOLoginData(String data) throws IllegalArgumentException {
      this.deviceType = Integer.valueOf(ClientType.AJAX1.value());
      this.passwordHash = null;
      this.sessionStartTimeInMillis = null;
      String[] tokens = data.split("##");
      if (9 == tokens.length) {
         this.username = tokens[0];
         this.userID = Integer.parseInt(tokens[1]);
         this.presence = Integer.parseInt(tokens[2]);
         this.voiceCapability = Integer.parseInt(tokens[3]);
         this.clientVersion = Short.valueOf(tokens[4]);
         this.mobileDevice = tokens[5];
         this.userAgent = tokens[6];
         this.passwordHash = Integer.parseInt(tokens[7]);
         this.sessionStartTimeInMillis = Long.parseLong(tokens[8]);
      } else {
         log.error("Error in creating SSOLoginData, invalid string entered [" + data + "]");
         throw new IllegalArgumentException("Error in creating SSOLoginData, invalid string entered [" + data + "]");
      }
   }

   public SSOCheckResponseData toSSOCheckResponseData() {
      SSOCheckResponseData data = new SSOCheckResponseData();
      data.username = this.username;
      data.userid = this.userID;
      data.presence = this.presence;
      data.clientVersion = this.clientVersion;
      data.mobileDevice = this.mobileDevice;
      data.userAgent = this.userAgent;
      data.deviceType = this.deviceType;
      data.sessionStartTimeInSeconds = this.sessionStartTimeInMillis / 1000L;
      return data;
   }
}
