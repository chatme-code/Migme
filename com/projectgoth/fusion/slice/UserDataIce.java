package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class UserDataIce implements Cloneable, Serializable {
   public int userID;
   public String username;
   public long dateRegistered;
   public String password;
   public String displayName;
   public String displayPicture;
   public String avatar;
   public String statusMessage;
   public int countryID;
   public String language;
   public String emailAddress;
   public int onMailingList;
   public int chatRoomAdmin;
   public int chatRoomBans;
   public String registrationIPAddress;
   public String registrationDevice;
   public long firstLoginDate;
   public long lastLoginDate;
   public int failedLoginAttempts;
   public int failedActivationAttempts;
   public String mobilePhone;
   public String mobileDevice;
   public String userAgent;
   public int mobileVerified;
   public String verificationCode;
   public int emailActivated;
   public int emailAlert;
   public int emailAlertSent;
   public long emailActivationDate;
   public int allowBuzz;
   public double UTCOffset;
   public int type;
   public int affiliateID;
   public String merchantCreated;
   public String referredBy;
   public int referralLevel;
   public int bonusProgramID;
   public String currency;
   public double balance;
   public double fundedBalance;
   public String notes;
   public int status;
   public String fullbodyAvatar;
   public int accountVerified;
   public int accountType;
   public String aboutMeVerified;
   public int emailVerified;

   public UserDataIce() {
   }

   public UserDataIce(int userID, String username, long dateRegistered, String password, String displayName, String displayPicture, String avatar, String statusMessage, int countryID, String language, String emailAddress, int onMailingList, int chatRoomAdmin, int chatRoomBans, String registrationIPAddress, String registrationDevice, long firstLoginDate, long lastLoginDate, int failedLoginAttempts, int failedActivationAttempts, String mobilePhone, String mobileDevice, String userAgent, int mobileVerified, String verificationCode, int emailActivated, int emailAlert, int emailAlertSent, long emailActivationDate, int allowBuzz, double UTCOffset, int type, int affiliateID, String merchantCreated, String referredBy, int referralLevel, int bonusProgramID, String currency, double balance, double fundedBalance, String notes, int status, String fullbodyAvatar, int accountVerified, int accountType, String aboutMeVerified, int emailVerified) {
      this.userID = userID;
      this.username = username;
      this.dateRegistered = dateRegistered;
      this.password = password;
      this.displayName = displayName;
      this.displayPicture = displayPicture;
      this.avatar = avatar;
      this.statusMessage = statusMessage;
      this.countryID = countryID;
      this.language = language;
      this.emailAddress = emailAddress;
      this.onMailingList = onMailingList;
      this.chatRoomAdmin = chatRoomAdmin;
      this.chatRoomBans = chatRoomBans;
      this.registrationIPAddress = registrationIPAddress;
      this.registrationDevice = registrationDevice;
      this.firstLoginDate = firstLoginDate;
      this.lastLoginDate = lastLoginDate;
      this.failedLoginAttempts = failedLoginAttempts;
      this.failedActivationAttempts = failedActivationAttempts;
      this.mobilePhone = mobilePhone;
      this.mobileDevice = mobileDevice;
      this.userAgent = userAgent;
      this.mobileVerified = mobileVerified;
      this.verificationCode = verificationCode;
      this.emailActivated = emailActivated;
      this.emailAlert = emailAlert;
      this.emailAlertSent = emailAlertSent;
      this.emailActivationDate = emailActivationDate;
      this.allowBuzz = allowBuzz;
      this.UTCOffset = UTCOffset;
      this.type = type;
      this.affiliateID = affiliateID;
      this.merchantCreated = merchantCreated;
      this.referredBy = referredBy;
      this.referralLevel = referralLevel;
      this.bonusProgramID = bonusProgramID;
      this.currency = currency;
      this.balance = balance;
      this.fundedBalance = fundedBalance;
      this.notes = notes;
      this.status = status;
      this.fullbodyAvatar = fullbodyAvatar;
      this.accountVerified = accountVerified;
      this.accountType = accountType;
      this.aboutMeVerified = aboutMeVerified;
      this.emailVerified = emailVerified;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         UserDataIce _r = null;

         try {
            _r = (UserDataIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.userID != _r.userID) {
               return false;
            } else if (this.username != _r.username && this.username != null && !this.username.equals(_r.username)) {
               return false;
            } else if (this.dateRegistered != _r.dateRegistered) {
               return false;
            } else if (this.password != _r.password && this.password != null && !this.password.equals(_r.password)) {
               return false;
            } else if (this.displayName != _r.displayName && this.displayName != null && !this.displayName.equals(_r.displayName)) {
               return false;
            } else if (this.displayPicture != _r.displayPicture && this.displayPicture != null && !this.displayPicture.equals(_r.displayPicture)) {
               return false;
            } else if (this.avatar != _r.avatar && this.avatar != null && !this.avatar.equals(_r.avatar)) {
               return false;
            } else if (this.statusMessage != _r.statusMessage && this.statusMessage != null && !this.statusMessage.equals(_r.statusMessage)) {
               return false;
            } else if (this.countryID != _r.countryID) {
               return false;
            } else if (this.language != _r.language && this.language != null && !this.language.equals(_r.language)) {
               return false;
            } else if (this.emailAddress != _r.emailAddress && this.emailAddress != null && !this.emailAddress.equals(_r.emailAddress)) {
               return false;
            } else if (this.onMailingList != _r.onMailingList) {
               return false;
            } else if (this.chatRoomAdmin != _r.chatRoomAdmin) {
               return false;
            } else if (this.chatRoomBans != _r.chatRoomBans) {
               return false;
            } else if (this.registrationIPAddress != _r.registrationIPAddress && this.registrationIPAddress != null && !this.registrationIPAddress.equals(_r.registrationIPAddress)) {
               return false;
            } else if (this.registrationDevice != _r.registrationDevice && this.registrationDevice != null && !this.registrationDevice.equals(_r.registrationDevice)) {
               return false;
            } else if (this.firstLoginDate != _r.firstLoginDate) {
               return false;
            } else if (this.lastLoginDate != _r.lastLoginDate) {
               return false;
            } else if (this.failedLoginAttempts != _r.failedLoginAttempts) {
               return false;
            } else if (this.failedActivationAttempts != _r.failedActivationAttempts) {
               return false;
            } else if (this.mobilePhone != _r.mobilePhone && this.mobilePhone != null && !this.mobilePhone.equals(_r.mobilePhone)) {
               return false;
            } else if (this.mobileDevice != _r.mobileDevice && this.mobileDevice != null && !this.mobileDevice.equals(_r.mobileDevice)) {
               return false;
            } else if (this.userAgent != _r.userAgent && this.userAgent != null && !this.userAgent.equals(_r.userAgent)) {
               return false;
            } else if (this.mobileVerified != _r.mobileVerified) {
               return false;
            } else if (this.verificationCode != _r.verificationCode && this.verificationCode != null && !this.verificationCode.equals(_r.verificationCode)) {
               return false;
            } else if (this.emailActivated != _r.emailActivated) {
               return false;
            } else if (this.emailAlert != _r.emailAlert) {
               return false;
            } else if (this.emailAlertSent != _r.emailAlertSent) {
               return false;
            } else if (this.emailActivationDate != _r.emailActivationDate) {
               return false;
            } else if (this.allowBuzz != _r.allowBuzz) {
               return false;
            } else if (this.UTCOffset != _r.UTCOffset) {
               return false;
            } else if (this.type != _r.type) {
               return false;
            } else if (this.affiliateID != _r.affiliateID) {
               return false;
            } else if (this.merchantCreated != _r.merchantCreated && this.merchantCreated != null && !this.merchantCreated.equals(_r.merchantCreated)) {
               return false;
            } else if (this.referredBy != _r.referredBy && this.referredBy != null && !this.referredBy.equals(_r.referredBy)) {
               return false;
            } else if (this.referralLevel != _r.referralLevel) {
               return false;
            } else if (this.bonusProgramID != _r.bonusProgramID) {
               return false;
            } else if (this.currency != _r.currency && this.currency != null && !this.currency.equals(_r.currency)) {
               return false;
            } else if (this.balance != _r.balance) {
               return false;
            } else if (this.fundedBalance != _r.fundedBalance) {
               return false;
            } else if (this.notes != _r.notes && this.notes != null && !this.notes.equals(_r.notes)) {
               return false;
            } else if (this.status != _r.status) {
               return false;
            } else if (this.fullbodyAvatar != _r.fullbodyAvatar && this.fullbodyAvatar != null && !this.fullbodyAvatar.equals(_r.fullbodyAvatar)) {
               return false;
            } else if (this.accountVerified != _r.accountVerified) {
               return false;
            } else if (this.accountType != _r.accountType) {
               return false;
            } else if (this.aboutMeVerified != _r.aboutMeVerified && this.aboutMeVerified != null && !this.aboutMeVerified.equals(_r.aboutMeVerified)) {
               return false;
            } else {
               return this.emailVerified == _r.emailVerified;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.userID;
      if (this.username != null) {
         __h = 5 * __h + this.username.hashCode();
      }

      __h = 5 * __h + (int)this.dateRegistered;
      if (this.password != null) {
         __h = 5 * __h + this.password.hashCode();
      }

      if (this.displayName != null) {
         __h = 5 * __h + this.displayName.hashCode();
      }

      if (this.displayPicture != null) {
         __h = 5 * __h + this.displayPicture.hashCode();
      }

      if (this.avatar != null) {
         __h = 5 * __h + this.avatar.hashCode();
      }

      if (this.statusMessage != null) {
         __h = 5 * __h + this.statusMessage.hashCode();
      }

      __h = 5 * __h + this.countryID;
      if (this.language != null) {
         __h = 5 * __h + this.language.hashCode();
      }

      if (this.emailAddress != null) {
         __h = 5 * __h + this.emailAddress.hashCode();
      }

      __h = 5 * __h + this.onMailingList;
      __h = 5 * __h + this.chatRoomAdmin;
      __h = 5 * __h + this.chatRoomBans;
      if (this.registrationIPAddress != null) {
         __h = 5 * __h + this.registrationIPAddress.hashCode();
      }

      if (this.registrationDevice != null) {
         __h = 5 * __h + this.registrationDevice.hashCode();
      }

      __h = 5 * __h + (int)this.firstLoginDate;
      __h = 5 * __h + (int)this.lastLoginDate;
      __h = 5 * __h + this.failedLoginAttempts;
      __h = 5 * __h + this.failedActivationAttempts;
      if (this.mobilePhone != null) {
         __h = 5 * __h + this.mobilePhone.hashCode();
      }

      if (this.mobileDevice != null) {
         __h = 5 * __h + this.mobileDevice.hashCode();
      }

      if (this.userAgent != null) {
         __h = 5 * __h + this.userAgent.hashCode();
      }

      __h = 5 * __h + this.mobileVerified;
      if (this.verificationCode != null) {
         __h = 5 * __h + this.verificationCode.hashCode();
      }

      __h = 5 * __h + this.emailActivated;
      __h = 5 * __h + this.emailAlert;
      __h = 5 * __h + this.emailAlertSent;
      __h = 5 * __h + (int)this.emailActivationDate;
      __h = 5 * __h + this.allowBuzz;
      __h = 5 * __h + (int)Double.doubleToLongBits(this.UTCOffset);
      __h = 5 * __h + this.type;
      __h = 5 * __h + this.affiliateID;
      if (this.merchantCreated != null) {
         __h = 5 * __h + this.merchantCreated.hashCode();
      }

      if (this.referredBy != null) {
         __h = 5 * __h + this.referredBy.hashCode();
      }

      __h = 5 * __h + this.referralLevel;
      __h = 5 * __h + this.bonusProgramID;
      if (this.currency != null) {
         __h = 5 * __h + this.currency.hashCode();
      }

      __h = 5 * __h + (int)Double.doubleToLongBits(this.balance);
      __h = 5 * __h + (int)Double.doubleToLongBits(this.fundedBalance);
      if (this.notes != null) {
         __h = 5 * __h + this.notes.hashCode();
      }

      __h = 5 * __h + this.status;
      if (this.fullbodyAvatar != null) {
         __h = 5 * __h + this.fullbodyAvatar.hashCode();
      }

      __h = 5 * __h + this.accountVerified;
      __h = 5 * __h + this.accountType;
      if (this.aboutMeVerified != null) {
         __h = 5 * __h + this.aboutMeVerified.hashCode();
      }

      __h = 5 * __h + this.emailVerified;
      return __h;
   }

   public Object clone() {
      Object o = null;

      try {
         o = super.clone();
      } catch (CloneNotSupportedException var3) {
         assert false;
      }

      return o;
   }

   public void __write(BasicStream __os) {
      __os.writeInt(this.userID);
      __os.writeString(this.username);
      __os.writeLong(this.dateRegistered);
      __os.writeString(this.password);
      __os.writeString(this.displayName);
      __os.writeString(this.displayPicture);
      __os.writeString(this.avatar);
      __os.writeString(this.statusMessage);
      __os.writeInt(this.countryID);
      __os.writeString(this.language);
      __os.writeString(this.emailAddress);
      __os.writeInt(this.onMailingList);
      __os.writeInt(this.chatRoomAdmin);
      __os.writeInt(this.chatRoomBans);
      __os.writeString(this.registrationIPAddress);
      __os.writeString(this.registrationDevice);
      __os.writeLong(this.firstLoginDate);
      __os.writeLong(this.lastLoginDate);
      __os.writeInt(this.failedLoginAttempts);
      __os.writeInt(this.failedActivationAttempts);
      __os.writeString(this.mobilePhone);
      __os.writeString(this.mobileDevice);
      __os.writeString(this.userAgent);
      __os.writeInt(this.mobileVerified);
      __os.writeString(this.verificationCode);
      __os.writeInt(this.emailActivated);
      __os.writeInt(this.emailAlert);
      __os.writeInt(this.emailAlertSent);
      __os.writeLong(this.emailActivationDate);
      __os.writeInt(this.allowBuzz);
      __os.writeDouble(this.UTCOffset);
      __os.writeInt(this.type);
      __os.writeInt(this.affiliateID);
      __os.writeString(this.merchantCreated);
      __os.writeString(this.referredBy);
      __os.writeInt(this.referralLevel);
      __os.writeInt(this.bonusProgramID);
      __os.writeString(this.currency);
      __os.writeDouble(this.balance);
      __os.writeDouble(this.fundedBalance);
      __os.writeString(this.notes);
      __os.writeInt(this.status);
      __os.writeString(this.fullbodyAvatar);
      __os.writeInt(this.accountVerified);
      __os.writeInt(this.accountType);
      __os.writeString(this.aboutMeVerified);
      __os.writeInt(this.emailVerified);
   }

   public void __read(BasicStream __is) {
      this.userID = __is.readInt();
      this.username = __is.readString();
      this.dateRegistered = __is.readLong();
      this.password = __is.readString();
      this.displayName = __is.readString();
      this.displayPicture = __is.readString();
      this.avatar = __is.readString();
      this.statusMessage = __is.readString();
      this.countryID = __is.readInt();
      this.language = __is.readString();
      this.emailAddress = __is.readString();
      this.onMailingList = __is.readInt();
      this.chatRoomAdmin = __is.readInt();
      this.chatRoomBans = __is.readInt();
      this.registrationIPAddress = __is.readString();
      this.registrationDevice = __is.readString();
      this.firstLoginDate = __is.readLong();
      this.lastLoginDate = __is.readLong();
      this.failedLoginAttempts = __is.readInt();
      this.failedActivationAttempts = __is.readInt();
      this.mobilePhone = __is.readString();
      this.mobileDevice = __is.readString();
      this.userAgent = __is.readString();
      this.mobileVerified = __is.readInt();
      this.verificationCode = __is.readString();
      this.emailActivated = __is.readInt();
      this.emailAlert = __is.readInt();
      this.emailAlertSent = __is.readInt();
      this.emailActivationDate = __is.readLong();
      this.allowBuzz = __is.readInt();
      this.UTCOffset = __is.readDouble();
      this.type = __is.readInt();
      this.affiliateID = __is.readInt();
      this.merchantCreated = __is.readString();
      this.referredBy = __is.readString();
      this.referralLevel = __is.readInt();
      this.bonusProgramID = __is.readInt();
      this.currency = __is.readString();
      this.balance = __is.readDouble();
      this.fundedBalance = __is.readDouble();
      this.notes = __is.readString();
      this.status = __is.readInt();
      this.fullbodyAvatar = __is.readString();
      this.accountVerified = __is.readInt();
      this.accountType = __is.readInt();
      this.aboutMeVerified = __is.readString();
      this.emailVerified = __is.readInt();
   }
}
