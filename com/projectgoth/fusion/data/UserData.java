package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.leto.common.user.UserDetails;
import com.projectgoth.leto.common.user.UserType;
import com.projectgoth.leto.common.user.VerifiedAccountType;
import com.projectgoth.leto.common.user.VerifiedStatusType;
import com.projectgoth.leto.common.utils.enums.IEnumValueGetter;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

public class UserData implements Serializable, UserDetails {
   public static final String CURRENT_VERSION = "1.0";
   public Integer userID;
   public String username;
   public Date dateRegistered;
   public String password;
   public String displayName;
   public String displayPicture;
   public String avatar;
   public String statusMessage;
   public Date statusTimeStamp;
   public Integer countryID;
   public String language;
   public String emailAddress;
   public Boolean onMailingList;
   public Boolean chatRoomAdmin;
   public Integer chatRoomBans;
   public String registrationIPAddress;
   public String registrationDevice;
   public Date firstLoginDate;
   public Date lastLoginDate;
   public Integer failedLoginAttempts;
   public Integer failedActivationAttempts;
   public String mobilePhone;
   public String mobileDevice;
   public String userAgent;
   public Boolean mobileVerified;
   public String verificationCode;
   public Boolean emailActivated;
   public Boolean emailAlert;
   public Boolean emailAlertSent;
   public Date emailActivationDate;
   public Boolean allowBuzz;
   public Double UTCOffset;
   public UserData.TypeEnum type;
   public Integer affiliateID;
   public String merchantCreated;
   public String referredBy;
   public Integer referralLevel;
   public Integer bonusProgramID;
   public String currency;
   public Double balance;
   public Double fundedBalance;
   public String notes;
   public UserData.StatusEnum status;
   public String fullbodyAvatar;
   public UserData.AccountVerifiedEnum accountVerified;
   public UserData.AccountTypeEnum accountType;
   public String aboutMeVerified;
   public Boolean emailVerified;
   public String alias;
   public UserSettingData.MessageEnum messageSetting = UserSettingData.MessageEnum.defaultValue();
   public UserSettingData.AnonymousCallEnum anonymousCallSetting = UserSettingData.AnonymousCallEnum.defaultValue();
   public UserSettingData.EmailSettingEnum emailAllSetting = UserSettingData.EmailSettingEnum.defaultValue();
   public UserSettingData.EmailSettingEnum emailMentionSetting = UserSettingData.EmailSettingEnum.defaultValue();
   public UserSettingData.EmailSettingEnum emailReplyToPostSetting = UserSettingData.EmailSettingEnum.defaultValue();
   public UserSettingData.EmailSettingEnum emailReceiveGiftSetting = UserSettingData.EmailSettingEnum.defaultValue();
   public UserSettingData.EmailSettingEnum emailNewFollowerSetting = UserSettingData.EmailSettingEnum.defaultValue();
   public Set<String> blockList;
   public Set<String> broadcastList;
   public Set<String> pendingContacts;

   public UserData() {
   }

   public UserData(ResultSet rs) throws SQLException {
      this.userID = rs.getInt("uid");
      this.username = rs.getString("username");
      this.dateRegistered = rs.getTimestamp("dateRegistered");
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT73368964_ENABLED)) {
         this.password = rs.getString("password");
      }

      this.displayName = rs.getString("displayName");
      this.displayPicture = rs.getString("displayPicture");
      this.avatar = rs.getString("avatar");
      this.statusMessage = rs.getString("statusMessage");
      this.countryID = (Integer)rs.getObject("countryID");
      this.language = rs.getString("language");
      this.emailAddress = rs.getString("emailAddress");
      this.chatRoomBans = (Integer)rs.getObject("chatRoomBans");
      this.registrationIPAddress = rs.getString("registrationIPAddress");
      this.registrationDevice = rs.getString("registrationDevice");
      this.firstLoginDate = rs.getTimestamp("firstLoginDate");
      this.lastLoginDate = rs.getTimestamp("lastLoginDate");
      this.failedLoginAttempts = (Integer)rs.getObject("failedLoginAttempts");
      this.failedActivationAttempts = (Integer)rs.getObject("failedActivationAttempts");
      this.mobilePhone = rs.getString("mobilePhone");
      this.mobileDevice = rs.getString("mobileDevice");
      this.userAgent = rs.getString("userAgent");
      this.verificationCode = rs.getString("verificationCode");
      this.UTCOffset = (Double)rs.getObject("UTCOffset");
      this.merchantCreated = rs.getString("merchantCreated");
      this.affiliateID = (Integer)rs.getObject("affiliateID");
      this.referredBy = rs.getString("referredBy");
      this.bonusProgramID = (Integer)rs.getObject("BonusProgramID");
      this.currency = rs.getString("currency");
      this.balance = (Double)rs.getObject("balance");
      this.fundedBalance = (Double)rs.getObject("fundedBalance");
      this.notes = rs.getString("notes");
      this.emailActivationDate = rs.getTimestamp("emailActivationDate");
      this.fullbodyAvatar = rs.getString("fullbodyavatar");
      this.aboutMeVerified = rs.getString("verifiedProfile");
      Integer intVal = (Integer)rs.getObject("onMailingList");
      if (intVal != null) {
         this.onMailingList = intVal != 0;
      }

      intVal = (Integer)rs.getObject("chatRoomAdmin");
      if (intVal != null) {
         this.chatRoomAdmin = intVal != 0;
      }

      intVal = (Integer)rs.getObject("mobileVerified");
      if (intVal != null) {
         this.mobileVerified = intVal != 0;
      }

      intVal = (Integer)rs.getObject("emailActivated");
      if (intVal != null) {
         this.emailActivated = intVal != 0;
      }

      intVal = (Integer)rs.getObject("emailAlert");
      if (intVal != null) {
         this.emailAlert = intVal != 0;
      }

      intVal = (Integer)rs.getObject("emailAlertSent");
      if (intVal != null) {
         this.emailAlertSent = intVal != 0;
      }

      intVal = (Integer)rs.getObject("allowBuzz");
      if (intVal != null) {
         this.allowBuzz = intVal != 0;
      }

      intVal = (Integer)rs.getObject("type");
      if (intVal != null) {
         this.type = UserData.TypeEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = UserData.StatusEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("accountVerified");
      this.accountVerified = intVal != null ? UserData.AccountVerifiedEnum.fromValue(intVal) : null;
      if (this.accountVerified == null) {
         this.accountVerified = UserData.AccountVerifiedEnum.PENDING;
      }

      intVal = (Integer)rs.getObject("accountType");
      this.accountType = intVal != null ? UserData.AccountTypeEnum.fromValue(intVal) : null;
      if (this.accountType == null) {
         this.accountType = UserData.AccountTypeEnum.INDIVIDUAL;
      }

      intVal = (Integer)rs.getObject("emailVerified");
      this.emailVerified = intVal != null && intVal != 0;
      String primaryEmail = rs.getString("primaryEmail");
      if (!StringUtil.isBlank(primaryEmail)) {
         this.emailAddress = primaryEmail;
      } else if (!StringUtil.isValidNonMig33Email(this.emailAddress)) {
         this.emailAddress = null;
      }

      this.alias = rs.getString("alias");
   }

   public UserData(UserDataIce userIce) {
      this.userID = userIce.userID == Integer.MIN_VALUE ? null : userIce.userID;
      this.username = userIce.username.equals("\u0000") ? null : userIce.username;
      this.dateRegistered = userIce.dateRegistered == Long.MIN_VALUE ? null : new Date(userIce.dateRegistered);
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT73368964_ENABLED)) {
         this.password = userIce.password.equals("\u0000") ? null : userIce.password;
      }

      this.displayName = userIce.displayName.equals("\u0000") ? null : userIce.displayName;
      this.displayPicture = userIce.displayPicture.equals("\u0000") ? null : userIce.displayPicture;
      this.avatar = userIce.avatar.equals("\u0000") ? null : userIce.avatar;
      this.statusMessage = userIce.statusMessage.equals("\u0000") ? null : userIce.statusMessage;
      this.countryID = userIce.countryID == Integer.MIN_VALUE ? null : userIce.countryID;
      this.language = userIce.language.equals("\u0000") ? null : userIce.language;
      this.emailAddress = userIce.emailAddress.equals("\u0000") ? null : userIce.emailAddress;
      this.onMailingList = userIce.onMailingList == Integer.MIN_VALUE ? null : userIce.onMailingList == 1;
      this.chatRoomAdmin = userIce.chatRoomAdmin == Integer.MIN_VALUE ? null : userIce.chatRoomAdmin == 1;
      this.chatRoomBans = userIce.chatRoomBans == Integer.MIN_VALUE ? null : userIce.chatRoomBans;
      this.registrationIPAddress = userIce.registrationIPAddress.equals("\u0000") ? null : userIce.registrationIPAddress;
      this.registrationDevice = userIce.registrationDevice.equals("\u0000") ? null : userIce.registrationDevice;
      this.firstLoginDate = userIce.firstLoginDate == Long.MIN_VALUE ? null : new Date(userIce.firstLoginDate);
      this.lastLoginDate = userIce.lastLoginDate == Long.MIN_VALUE ? null : new Date(userIce.lastLoginDate);
      this.failedLoginAttempts = userIce.failedLoginAttempts == Integer.MIN_VALUE ? null : userIce.failedLoginAttempts;
      this.failedActivationAttempts = userIce.failedActivationAttempts == Integer.MIN_VALUE ? null : userIce.failedActivationAttempts;
      this.mobilePhone = userIce.mobilePhone.equals("\u0000") ? null : userIce.mobilePhone;
      this.mobileDevice = userIce.mobileDevice.equals("\u0000") ? null : userIce.mobileDevice;
      this.userAgent = userIce.userAgent.equals("\u0000") ? null : userIce.userAgent;
      this.mobileVerified = userIce.mobileVerified == Integer.MIN_VALUE ? null : userIce.mobileVerified == 1;
      this.verificationCode = userIce.verificationCode.equals("\u0000") ? null : userIce.verificationCode;
      this.emailActivated = userIce.emailActivated == Integer.MIN_VALUE ? null : userIce.emailActivated == 1;
      this.emailAlert = userIce.emailAlert == Integer.MIN_VALUE ? null : userIce.emailAlert == 1;
      this.emailAlertSent = userIce.emailAlertSent == Integer.MIN_VALUE ? null : userIce.emailAlertSent == 1;
      this.emailActivationDate = userIce.emailActivationDate == Long.MIN_VALUE ? null : new Date(userIce.emailActivationDate);
      this.allowBuzz = userIce.allowBuzz == Integer.MIN_VALUE ? null : userIce.allowBuzz == 1;
      this.UTCOffset = userIce.UTCOffset == Double.MIN_VALUE ? null : userIce.UTCOffset;
      this.type = userIce.type == Integer.MIN_VALUE ? null : UserData.TypeEnum.fromValue(userIce.type);
      this.affiliateID = userIce.affiliateID == Integer.MIN_VALUE ? null : userIce.affiliateID;
      this.merchantCreated = userIce.merchantCreated.equals("\u0000") ? null : userIce.merchantCreated;
      this.referredBy = userIce.referredBy.equals("\u0000") ? null : userIce.referredBy;
      this.referralLevel = userIce.referralLevel == Integer.MIN_VALUE ? null : userIce.referralLevel;
      this.bonusProgramID = userIce.bonusProgramID == Integer.MIN_VALUE ? null : userIce.bonusProgramID;
      this.currency = userIce.currency.equals("\u0000") ? null : userIce.currency;
      this.balance = userIce.balance == Double.MIN_VALUE ? null : userIce.balance;
      this.fundedBalance = userIce.fundedBalance == Double.MIN_VALUE ? null : userIce.fundedBalance;
      this.notes = userIce.notes.equals("\u0000") ? null : userIce.notes;
      this.status = userIce.status == Integer.MIN_VALUE ? null : UserData.StatusEnum.fromValue(userIce.status);
      this.fullbodyAvatar = userIce.fullbodyAvatar.equals("\u0000") ? null : userIce.fullbodyAvatar;
      this.aboutMeVerified = userIce.aboutMeVerified.equals("\u0000") ? null : userIce.aboutMeVerified;
      this.accountType = userIce.accountType == Integer.MIN_VALUE ? UserData.AccountTypeEnum.INDIVIDUAL : UserData.AccountTypeEnum.fromValue(userIce.accountType);
      this.accountVerified = userIce.accountVerified == Integer.MIN_VALUE ? UserData.AccountVerifiedEnum.PENDING : UserData.AccountVerifiedEnum.fromValue(userIce.accountVerified);
      this.emailVerified = userIce.emailVerified == Integer.MIN_VALUE ? null : userIce.emailVerified == 1;
   }

   public UserDataIce toIceObject() {
      UserDataIce userIce = new UserDataIce();
      userIce.userID = this.userID == null ? Integer.MIN_VALUE : this.userID;
      userIce.username = this.username == null ? "\u0000" : this.username;
      userIce.dateRegistered = this.dateRegistered == null ? Long.MIN_VALUE : this.dateRegistered.getTime();
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT73368964_ENABLED)) {
         userIce.password = this.password == null ? "\u0000" : this.password;
      }

      userIce.displayName = this.displayName == null ? "\u0000" : this.displayName;
      userIce.displayPicture = this.displayPicture == null ? "\u0000" : this.displayPicture;
      userIce.avatar = this.avatar == null ? "\u0000" : this.avatar;
      userIce.statusMessage = this.statusMessage == null ? "\u0000" : this.statusMessage;
      userIce.countryID = this.countryID == null ? Integer.MIN_VALUE : this.countryID;
      userIce.language = this.language == null ? "\u0000" : this.language;
      userIce.emailAddress = this.emailAddress == null ? "\u0000" : this.emailAddress;
      userIce.onMailingList = this.onMailingList == null ? Integer.MIN_VALUE : (this.onMailingList ? 1 : 0);
      userIce.chatRoomAdmin = this.chatRoomAdmin == null ? Integer.MIN_VALUE : (this.chatRoomAdmin ? 1 : 0);
      userIce.chatRoomBans = this.chatRoomBans == null ? Integer.MIN_VALUE : this.chatRoomBans;
      userIce.registrationIPAddress = this.registrationIPAddress == null ? "\u0000" : this.registrationIPAddress;
      userIce.registrationDevice = this.registrationDevice == null ? "\u0000" : this.registrationDevice;
      userIce.firstLoginDate = this.firstLoginDate == null ? Long.MIN_VALUE : this.firstLoginDate.getTime();
      userIce.lastLoginDate = this.lastLoginDate == null ? Long.MIN_VALUE : this.lastLoginDate.getTime();
      userIce.failedLoginAttempts = this.failedLoginAttempts == null ? Integer.MIN_VALUE : this.failedLoginAttempts;
      userIce.failedActivationAttempts = this.failedActivationAttempts == null ? Integer.MIN_VALUE : this.failedActivationAttempts;
      userIce.mobilePhone = this.mobilePhone == null ? "\u0000" : this.mobilePhone;
      userIce.mobileDevice = this.mobileDevice == null ? "\u0000" : this.mobileDevice;
      userIce.userAgent = this.userAgent == null ? "\u0000" : this.userAgent;
      userIce.mobileVerified = this.mobileVerified == null ? Integer.MIN_VALUE : (this.mobileVerified ? 1 : 0);
      userIce.verificationCode = this.verificationCode == null ? "\u0000" : this.verificationCode;
      userIce.emailActivated = this.emailActivated == null ? Integer.MIN_VALUE : (this.emailActivated ? 1 : 0);
      userIce.emailAlert = this.emailAlert == null ? Integer.MIN_VALUE : (this.emailAlert ? 1 : 0);
      userIce.emailAlertSent = this.emailAlertSent == null ? Integer.MIN_VALUE : (this.emailAlertSent ? 1 : 0);
      userIce.emailActivationDate = this.emailActivationDate == null ? Long.MIN_VALUE : this.emailActivationDate.getTime();
      userIce.allowBuzz = this.allowBuzz == null ? Integer.MIN_VALUE : (this.allowBuzz ? 1 : 0);
      userIce.UTCOffset = this.UTCOffset == null ? Double.MIN_VALUE : this.UTCOffset;
      userIce.type = this.type == null ? Integer.MIN_VALUE : this.type.value();
      userIce.affiliateID = this.affiliateID == null ? Integer.MIN_VALUE : this.affiliateID;
      userIce.merchantCreated = this.merchantCreated == null ? "\u0000" : this.merchantCreated;
      userIce.referredBy = this.referredBy == null ? "\u0000" : this.referredBy;
      userIce.referralLevel = this.referralLevel == null ? Integer.MIN_VALUE : this.referralLevel;
      userIce.bonusProgramID = this.bonusProgramID == null ? Integer.MIN_VALUE : this.bonusProgramID;
      userIce.currency = this.currency == null ? "\u0000" : this.currency;
      userIce.balance = this.balance == null ? Double.MIN_VALUE : this.balance;
      userIce.fundedBalance = this.fundedBalance == null ? Double.MIN_VALUE : this.fundedBalance;
      userIce.notes = this.notes == null ? "\u0000" : this.notes;
      userIce.status = this.status == null ? Integer.MIN_VALUE : this.status.value();
      userIce.fullbodyAvatar = this.fullbodyAvatar == null ? "\u0000" : this.fullbodyAvatar;
      userIce.aboutMeVerified = this.aboutMeVerified == null ? "\u0000" : this.aboutMeVerified;
      userIce.accountVerified = this.accountVerified == null ? Integer.MIN_VALUE : this.accountVerified.value();
      userIce.accountType = this.accountType == null ? Integer.MIN_VALUE : this.accountType.value();
      userIce.emailVerified = this.emailVerified == null ? Integer.MIN_VALUE : (this.emailVerified ? 1 : 0);
      return userIce;
   }

   public String getMetadataVersion() {
      return "1.0";
   }

   public long getUserId() {
      return this.userID == null ? -1L : this.userID.longValue();
   }

   public String getUserName() {
      return this.username;
   }

   public Date getDateRegistered() {
      return this.dateRegistered;
   }

   public boolean isMobileVerified() {
      return this.mobileVerified != null && this.mobileVerified;
   }

   public boolean isEmailVerified() {
      return this.emailVerified != null && this.emailVerified;
   }

   public Date getFirstLoginDate() {
      return this.firstLoginDate;
   }

   public Date getLastLoginDate() {
      return this.lastLoginDate;
   }

   public String getRegistrationDevice() {
      return this.registrationDevice;
   }

   public String getRegistrationIPAddress() {
      return this.registrationIPAddress;
   }

   public String getMobilePhone() {
      return this.mobilePhone;
   }

   public String getMobileDevice() {
      return this.mobileDevice;
   }

   public String getEmailAddress() {
      return this.emailAddress;
   }

   public Integer getCountryID() {
      return this.countryID;
   }

   public String getUserAgent() {
      return this.userAgent;
   }

   public UserType getUserType() {
      return this.type != null ? this.type.toUserType() : UserData.TypeEnum.MIG33.toUserType();
   }

   public boolean allowToSendEmailViaMM(RewardProgramData programData) {
      return programData.type != RewardProgramData.TypeEnum.VIRTUAL_GIFT_RECEIVED || this.emailReceiveGiftSetting != UserSettingData.EmailSettingEnum.DISABLED;
   }

   public boolean allowToSendNewFollowerEmail() {
      return this.emailNewFollowerSetting == UserSettingData.EmailSettingEnum.ENABLED;
   }

   public VerifiedStatusType getAccountVerifiedStatus() {
      return this.accountVerified != null ? this.accountVerified.toVerifiedStatusType() : null;
   }

   public VerifiedAccountType getVerifiedAccountType() {
      return this.accountType != null ? this.accountType.toVerifiedAccountType() : null;
   }

   public static enum AccountTypeEnum implements IEnumValueGetter<Integer> {
      INDIVIDUAL(VerifiedAccountType.INDIVIDUAL),
      ENTITY(VerifiedAccountType.ENTITY);

      private final VerifiedAccountType verifiedAccountType;

      private AccountTypeEnum(VerifiedAccountType verifiedAccountType) {
         this.verifiedAccountType = verifiedAccountType;
      }

      public int value() {
         return this.getEnumValue();
      }

      public String toString() {
         return this.getEnumValue().toString();
      }

      public static UserData.AccountTypeEnum fromValue(int value) {
         return (UserData.AccountTypeEnum)UserData.AccountTypeEnum.ValueToEnumMapInstance.INSTANCE.toEnum(value);
      }

      public VerifiedAccountType toVerifiedAccountType() {
         return this.verifiedAccountType;
      }

      public Integer getEnumValue() {
         return this.verifiedAccountType.getEnumValue();
      }

      private static final class ValueToEnumMapInstance {
         public static final ValueToEnumMap<Integer, UserData.AccountTypeEnum> INSTANCE = new ValueToEnumMap(UserData.AccountTypeEnum.class);
      }
   }

   public static enum AccountVerifiedEnum implements IEnumValueGetter<Integer> {
      PENDING(VerifiedStatusType.PENDING),
      VERIFIED_NOT_OK(VerifiedStatusType.VERIFIED_NOT_OK),
      VERIFIED_OK(VerifiedStatusType.VERIFIED_OK);

      private final VerifiedStatusType verifiedStatusType;

      private AccountVerifiedEnum(VerifiedStatusType verifiedStatusType) {
         this.verifiedStatusType = verifiedStatusType;
      }

      public int value() {
         return this.getEnumValue();
      }

      public String toString() {
         return this.getEnumValue().toString();
      }

      public static UserData.AccountVerifiedEnum fromValue(int value) {
         return (UserData.AccountVerifiedEnum)UserData.AccountVerifiedEnum.ValueToEnumMapInstance.INSTANCE.toEnum(value);
      }

      public VerifiedStatusType toVerifiedStatusType() {
         return this.verifiedStatusType;
      }

      public Integer getEnumValue() {
         return this.verifiedStatusType.getEnumValue();
      }

      private static final class ValueToEnumMapInstance {
         public static final ValueToEnumMap<Integer, UserData.AccountVerifiedEnum> INSTANCE = new ValueToEnumMap(UserData.AccountVerifiedEnum.class);
      }
   }

   public static enum TypeEnum implements IEnumValueGetter<Integer> {
      MIG33(UserType.MIG33, "Ordinary"),
      MIG33_MERCHANT(UserType.MIG33_MERCHANT, "Regular Merchant"),
      MIG33_TOP_MERCHANT(UserType.MIG33_TOP_MERCHANT, "Top Merchant"),
      MIG33_PREPAID_CARD(UserType.MIG33_PREPAID_CARD, "Ordinary (Prepaid)");

      private final UserType userType;
      private final String label;

      private TypeEnum(UserType userType, String label) {
         this.userType = userType;
         this.label = label;
      }

      public int value() {
         return this.getEnumValue();
      }

      public static UserData.TypeEnum fromValue(int value) {
         return (UserData.TypeEnum)UserData.TypeEnum.ValueToEnumMapInstance.INSTANCE.toEnum(value);
      }

      public Integer getEnumValue() {
         return this.userType.getEnumValue();
      }

      public UserType toUserType() {
         return this.userType;
      }

      public String getLabel() {
         return this.label;
      }

      private static final class ValueToEnumMapInstance {
         public static final ValueToEnumMap<Integer, UserData.TypeEnum> INSTANCE = new ValueToEnumMap(UserData.TypeEnum.class);
      }
   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserData.StatusEnum fromValue(int value) {
         UserData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
