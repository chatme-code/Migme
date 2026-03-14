package com.projectgoth.fusion.jobscheduling.domain;

import com.projectgoth.fusion.slice.SMSUserNotification;

public class SMSNote extends NotificationNote {
   private String phoneNumber;
   private int smsSubType;

   public SMSNote(String username, String phoneNumber, int smsSubType) {
      super(username);
      this.phoneNumber = phoneNumber;
      this.smsSubType = smsSubType;
   }

   public String getPhoneNumber() {
      return this.phoneNumber;
   }

   public int getSmsSubType() {
      return this.smsSubType;
   }

   public static SMSNote fromSMSUserNotification(SMSUserNotification note) {
      return new SMSNote(note.message, note.phoneNumber, note.smsSubType);
   }

   public SMSUserNotification toSMSUserNotification() {
      return new SMSUserNotification(this.getMessage(), this.phoneNumber, this.smsSubType);
   }
}
