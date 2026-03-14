package com.projectgoth.fusion.uns.domain;

import com.projectgoth.fusion.data.SystemSMSData;

public class SMSNote extends Note {
   private String phoneNumber;
   private String username;
   private SystemSMSData.SubTypeEnum subType;

   public SMSNote(String text, int subType) {
      super(text);
      this.subType = SystemSMSData.SubTypeEnum.fromValue(subType);
   }

   public SMSNote(String text, String phoneNumber, String username, int subType) {
      super(text);
      this.phoneNumber = phoneNumber;
      this.username = username;
      this.subType = SystemSMSData.SubTypeEnum.fromValue(subType);
   }

   public String getPhoneNumber() {
      return this.phoneNumber;
   }

   public void setPhoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public SystemSMSData.SubTypeEnum getSubType() {
      return this.subType;
   }
}
