package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.Credential;

public class FusionPktUserProfile extends FusionPacket {
   public FusionPktUserProfile() {
      super((short)906);
   }

   public FusionPktUserProfile(short transactionId) {
      super((short)906, transactionId);
   }

   public FusionPktUserProfile(FusionPacket packet) {
      super(packet);
   }

   public FusionPktUserProfile(short transactionId, UserData userData, UserProfileData userProfileData, Credential[] credentials) {
      super((short)906, transactionId);
      if (userData != null) {
         if (userData.displayName != null) {
            this.setDisplayName(userData.displayName);
         }

         if (userData.countryID != null) {
            this.setCountryId(userData.countryID);
         }

         if (userData.UTCOffset != null) {
            this.setUTCOffset(userData.UTCOffset.byteValue());
         }

         if (userData.emailAddress != null) {
            this.setEmailAddress(userData.emailAddress);
         }

         if (userData.onMailingList != null) {
            this.setOnMailingList((byte)(userData.onMailingList ? 1 : 0));
         }

         if (userData.mobilePhone != null) {
            this.setMobilePhone(userData.mobilePhone);
         }
      }

      if (userProfileData != null) {
         if (userProfileData.firstName != null) {
            this.setFirstName(userProfileData.firstName);
         }

         if (userProfileData.lastName != null) {
            this.setLastName(userProfileData.lastName);
         }

         if (userProfileData.city != null) {
            this.setCity(userProfileData.city);
         }

         if (userProfileData.state != null) {
            this.setState(userProfileData.state);
         }

         if (userProfileData.dateOfBirth != null) {
            this.setDateOfBirth(userProfileData.dateOfBirth.getTime());
         }

         if (userProfileData.gender != null) {
            this.setGender((byte)userProfileData.gender.toString().charAt(0));
         }
      }

      Credential[] arr$ = credentials;
      int len$ = credentials.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Credential credential = arr$[i$];
         switch(PasswordType.fromValue(credential.passwordType)) {
         case MSN_IM:
            this.setMSNUsername(credential.username);
            break;
         case YAHOO_IM:
            this.setYahooUsername(credential.username);
            break;
         case AIM_IM:
            this.setAIMUsername(credential.username);
            break;
         case GTALK_IM:
            this.setGTalkUsername(credential.username);
            break;
         case FACEBOOK_IM:
            this.setFacebookUsername(credential.username);
         }
      }

   }

   public String getDisplayName() {
      return this.getStringField((short)1);
   }

   public void setDisplayName(String displayName) {
      this.setField((short)1, displayName);
   }

   public String getFirstName() {
      return this.getStringField((short)2);
   }

   public void setFirstName(String firstName) {
      this.setField((short)2, firstName);
   }

   public String getLastName() {
      return this.getStringField((short)3);
   }

   public void setLastName(String lastName) {
      this.setField((short)3, lastName);
   }

   public String getCity() {
      return this.getStringField((short)4);
   }

   public void setCity(String city) {
      this.setField((short)4, city);
   }

   public String getState() {
      return this.getStringField((short)5);
   }

   public void setState(String state) {
      this.setField((short)5, state);
   }

   public Integer getCountryId() {
      return this.getIntField((short)6);
   }

   public void setCountryId(int countryId) {
      this.setField((short)6, countryId);
   }

   public Byte getUTCOffset() {
      return this.getByteField((short)7);
   }

   public void setUTCOffset(byte utcOffset) {
      this.setField((short)7, utcOffset);
   }

   public Long getDateOfBirth() {
      return this.getLongField((short)8);
   }

   public void setDateOfBirth(long dateOfBirth) {
      this.setField((short)8, dateOfBirth);
   }

   public Byte getGender() {
      return this.getByteField((short)9);
   }

   public void setGender(byte gender) {
      this.setField((short)9, gender);
   }

   public String getEmailAddress() {
      return this.getStringField((short)10);
   }

   public void setEmailAddress(String emailAddress) {
      this.setField((short)10, emailAddress);
   }

   public Byte getOnMailingList() {
      return this.getByteField((short)11);
   }

   public void setOnMailingList(byte onMailingList) {
      this.setField((short)11, onMailingList);
   }

   public String getMobilePhone() {
      return this.getStringField((short)12);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)12, mobilePhone);
   }

   public String getAwayMessage() {
      return this.getStringField((short)13);
   }

   public void setAwayMessage(String awayMessage) {
      this.setField((short)13, awayMessage);
   }

   public Byte getAutoRecharge() {
      return this.getByteField((short)14);
   }

   public void setAutoRecharge(byte autoRecharge) {
      this.setField((short)14, autoRecharge);
   }

   public String getMSNUsername() {
      return this.getStringField((short)15);
   }

   public void setMSNUsername(String msnUsername) {
      this.setField((short)15, msnUsername);
   }

   public String getMSNPassword() {
      return this.getStringField((short)16);
   }

   public void setMSNPassword(String msnPassword) {
      this.setField((short)16, msnPassword);
   }

   public Byte getMSNAutoLogin() {
      return this.getByteField((short)17);
   }

   public void setMSNAutoLogin(byte msnAutoLogin) {
      this.setField((short)17, msnAutoLogin);
   }

   public String getYahooUsername() {
      return this.getStringField((short)18);
   }

   public void setYahooUsername(String yahooUsername) {
      this.setField((short)18, yahooUsername);
   }

   public String getYahooPassword() {
      return this.getStringField((short)19);
   }

   public void setYahooPassword(String yahooPassword) {
      this.setField((short)19, yahooPassword);
   }

   public Byte getYahooAutoLogin() {
      return this.getByteField((short)20);
   }

   public void setYahooAutoLogin(byte yahooAutoLogin) {
      this.setField((short)20, yahooAutoLogin);
   }

   public String getAIMUsername() {
      return this.getStringField((short)21);
   }

   public void setAIMUsername(String aimUsername) {
      this.setField((short)21, aimUsername);
   }

   public String getAIMPassword() {
      return this.getStringField((short)22);
   }

   public void setAIMPassword(String aimPassword) {
      this.setField((short)22, aimPassword);
   }

   public Byte getAIMAutoLogin() {
      return this.getByteField((short)23);
   }

   public void setAIMAutoLogin(byte aimAutoLogin) {
      this.setField((short)23, aimAutoLogin);
   }

   public String getGTalkUsername() {
      return this.getStringField((short)24);
   }

   public void setGTalkUsername(String gtalkUsername) {
      this.setField((short)24, gtalkUsername);
   }

   public String getGTalkPassword() {
      return this.getStringField((short)25);
   }

   public void setGTalkPassword(String gtalkPassword) {
      this.setField((short)25, gtalkPassword);
   }

   public Byte getGTalkAutoLogin() {
      return this.getByteField((short)26);
   }

   public void setGTalkAutoLogin(byte gtalkAutoLogin) {
      this.setField((short)26, gtalkAutoLogin);
   }

   public String getFacebookUsername() {
      return this.getStringField((short)27);
   }

   public void setFacebookUsername(String facebookUsername) {
      this.setField((short)27, facebookUsername);
   }

   public String getFacebookPassword() {
      return this.getStringField((short)28);
   }

   public void setFacebookPassword(String facebookPassword) {
      this.setField((short)28, facebookPassword);
   }

   public Byte getFacebookAutoLogin() {
      return this.getByteField((short)29);
   }

   public void setFacebookAutoLogin(byte facebookAutoLogin) {
      this.setField((short)29, facebookAutoLogin);
   }
}
