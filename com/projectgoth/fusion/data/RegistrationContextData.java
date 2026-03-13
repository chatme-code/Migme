package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import com.projectgoth.leto.common.event.authenticated.RegistrationContext;
import com.projectgoth.leto.common.event.authenticated.RegistrationMethod;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationContextData implements Serializable, RegistrationContext {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RegistrationContextData.class));
   public int userid;
   public RegistrationContextData.RegistrationMethodEnum method;
   public String ipAddress;
   public String device;
   public String userAgent;
   public String imei;
   public Date date;
   public String email;
   public String mobilePhone;
   public String campaign;
   public String registrationType;
   public Integer invitationID;

   public RegistrationContextData(UserData userData, UserRegistrationContextData userRegContextData, AccountEntrySourceData accountEntrySourceData) {
      if (userData.userID != null) {
         this.userid = userData.userID;
      }

      if (userRegContextData.isEmailBased()) {
         this.method = RegistrationContextData.RegistrationMethodEnum.EMAIL;
      } else {
         this.method = RegistrationContextData.RegistrationMethodEnum.MOBILE;
      }

      this.ipAddress = userData.registrationIPAddress;
      this.device = userData.registrationDevice;
      this.userAgent = accountEntrySourceData.userAgent;
      this.imei = accountEntrySourceData.imei;
      this.date = userData.dateRegistered;
      this.email = userData.emailAddress;
      this.mobilePhone = userData.mobilePhone;
      this.campaign = userRegContextData.campaign;
      this.registrationType = userRegContextData.registrationType.value();
      this.invitationID = userRegContextData.invitationID;
   }

   public RegistrationContextData(ResultSet rs) throws SQLException {
      while(rs.next()) {
         this.userid = rs.getInt("userid");
         int typeInt = rs.getInt("type");
         String value = rs.getString("value");
         this.updateFieldByTypeValue(typeInt, value);
      }

   }

   public RegistrationContextData(String jsonStr) throws JSONException {
      JSONObject root = new JSONObject(jsonStr);
      Iterator iter = root.keys();

      while(iter.hasNext()) {
         String typeStr = iter.next().toString();
         String valueStr = root.optString(typeStr, (String)null);
         this.updateFieldByTypeValue(Integer.parseInt(typeStr), valueStr);
      }

   }

   private void updateFieldByTypeValue(int typeInt, String value) {
      RegistrationContextData.RegistrationContextTypeEnum type = RegistrationContextData.RegistrationContextTypeEnum.fromValue(typeInt);
      if (type == null) {
         log.warn(String.format("Unrecognized type %d for user %d, value=%s, ignoring it...", typeInt, this.userid, value));
      } else {
         switch(type) {
         case METHOD:
            try {
               this.method = RegistrationContextData.RegistrationMethodEnum.fromValue(Integer.parseInt(value));
               if (this.method == null) {
                  log.error(String.format("Unrecognized registration method value %s, user id %d, ignoring it", value, this.userid));
               }
            } catch (NumberFormatException var6) {
               log.error(String.format("Unable to convert registration method value %s to int, user id %d, ignoring it", value, this.userid));
            }
            break;
         case IP_ADDRESS:
            this.ipAddress = value;
            break;
         case DEVICE:
            this.device = value;
            break;
         case USER_AGENT:
            this.userAgent = value;
            break;
         case IMEI:
            this.imei = value;
            break;
         case DATE:
            try {
               this.date = DateTimeUtils.getDateForRegistrationContext(value);
            } catch (ParseException var5) {
               log.error(String.format("Unable to convert registration date value %s to date, user id %d, ignoring it", value, this.userid));
            }
            break;
         case EMAIL:
            this.email = value;
            break;
         case MOBILE_PHONE:
            this.mobilePhone = value;
            break;
         case CAMPAIGN:
            this.campaign = value;
            break;
         case REGISTRATION_TYPE:
            this.registrationType = value;
            break;
         case INVITATION_ID:
            this.invitationID = Integer.parseInt(StringUtil.trimmedLowerCase(value));
            break;
         default:
            log.error(String.format("Unsupported registration context type %s, value %s, user id %d, ignoring it", type, value, this.userid));
         }
      }

   }

   public Map<Integer, String> toIntegerAndStringMap() {
      Map<Integer, String> m = new HashMap();
      if (this.method != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.METHOD.value, Integer.toString(this.method.value()));
      }

      if (this.ipAddress != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.IP_ADDRESS.value, this.ipAddress);
      }

      if (this.device != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.DEVICE.value, this.device);
      }

      if (this.userAgent != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.USER_AGENT.value, this.userAgent);
      }

      if (this.imei != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.IMEI.value, this.imei);
      }

      if (this.date != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.DATE.value, DateTimeUtils.getStringForRegistrationContext(this.date));
      }

      if (!StringUtil.isBlank(this.email)) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.EMAIL.value, this.email);
      }

      if (!StringUtil.isBlank(this.mobilePhone)) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.MOBILE_PHONE.value, this.mobilePhone);
      }

      if (this.campaign != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.CAMPAIGN.value, this.campaign);
      }

      if (this.registrationType != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.REGISTRATION_TYPE.value, this.registrationType);
      }

      if (this.invitationID != null) {
         m.put(RegistrationContextData.RegistrationContextTypeEnum.INVITATION_ID.value, this.invitationID.toString());
      }

      return m;
   }

   public String toJSONString() throws JSONException {
      return this.toJSONObject().toString();
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject jsonObj = new JSONObject();
      if (!StringUtil.isBlank(this.campaign)) {
         jsonObj.put(Integer.toString(RegistrationContextData.RegistrationContextTypeEnum.CAMPAIGN.value), this.campaign);
      }

      if (!StringUtil.isBlank(this.registrationType)) {
         jsonObj.put(Integer.toString(RegistrationContextData.RegistrationContextTypeEnum.REGISTRATION_TYPE.value), this.registrationType);
      }

      if (this.invitationID != null) {
         jsonObj.put(Integer.toString(RegistrationContextData.RegistrationContextTypeEnum.INVITATION_ID.value), this.invitationID);
      }

      return jsonObj;
   }

   public UserRegistrationContextData updateUserRegistrationContextData(UserRegistrationContextData data) {
      data.campaign = this.campaign;
      data.registrationType = RegistrationType.fromValue(this.registrationType);
      data.invitationID = this.invitationID;
      return data;
   }

   public RegistrationMethod getMethod() {
      return this.method == null ? null : this.method.toRegistrationMethod();
   }

   public String getIpAddress() {
      return this.ipAddress;
   }

   public String getDevice() {
      return this.device;
   }

   public String getUserAgent() {
      return this.userAgent;
   }

   public String getImei() {
      return this.imei;
   }

   public Date getDate() {
      return this.date;
   }

   public String getEmail() {
      return this.email;
   }

   public String getMobilePhone() {
      return this.mobilePhone;
   }

   public com.projectgoth.leto.common.event.authenticated.RegistrationType getRegistrationType() {
      RegistrationType regType = RegistrationType.fromValue(this.registrationType);
      return regType == null ? null : regType.toRegistrationTypeEnum();
   }

   public Integer getInvitationID() {
      return this.invitationID;
   }

   public int getUserid() {
      return this.userid;
   }

   public String getCampaignRef() {
      return this.campaign;
   }

   public static enum RegistrationMethodEnum {
      MOBILE(RegistrationMethod.MOBILE),
      EMAIL(RegistrationMethod.EMAIL);

      private static final Map<Integer, RegistrationContextData.RegistrationMethodEnum> lookup = new HashMap();
      private final RegistrationMethod value;

      private RegistrationMethodEnum(RegistrationMethod value) {
         this.value = value;
      }

      public int value() {
         return this.value.getEnumValue();
      }

      public static RegistrationContextData.RegistrationMethodEnum fromValue(int value) {
         return (RegistrationContextData.RegistrationMethodEnum)lookup.get(value);
      }

      public RegistrationMethod toRegistrationMethod() {
         return this.value;
      }

      static {
         RegistrationContextData.RegistrationMethodEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            RegistrationContextData.RegistrationMethodEnum e = arr$[i$];
            lookup.put(e.value.getEnumValue(), e);
         }

      }
   }

   public static enum RegistrationContextTypeEnum {
      METHOD(1),
      IP_ADDRESS(2),
      DEVICE(3),
      USER_AGENT(4),
      IMEI(5),
      DATE(6),
      EMAIL(7),
      MOBILE_PHONE(8),
      CAMPAIGN(9),
      REGISTRATION_TYPE(10),
      INVITATION_ID(11);

      private static final Map<Integer, RegistrationContextData.RegistrationContextTypeEnum> lookup = new HashMap();
      private int value;

      private RegistrationContextTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static RegistrationContextData.RegistrationContextTypeEnum fromValue(int value) {
         return (RegistrationContextData.RegistrationContextTypeEnum)lookup.get(value);
      }

      static {
         RegistrationContextData.RegistrationContextTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            RegistrationContextData.RegistrationContextTypeEnum e = arr$[i$];
            lookup.put(e.value, e);
         }

      }
   }
}
