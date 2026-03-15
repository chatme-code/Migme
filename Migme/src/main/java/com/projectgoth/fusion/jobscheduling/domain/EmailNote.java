package com.projectgoth.fusion.jobscheduling.domain;

import com.projectgoth.fusion.slice.EmailUserNotification;

public class EmailNote extends NotificationNote {
   private String subject;
   private String emailAddress;

   public EmailNote(String message, String subject, String emailAddress) {
      super(message);
      this.subject = subject;
      this.emailAddress = emailAddress;
   }

   public String getSubject() {
      return this.subject;
   }

   public String getMessage() {
      return this.emailAddress;
   }

   public static EmailNote fromEmailUserNotification(EmailUserNotification note) {
      return new EmailNote(note.message, note.subject, note.emailAddress);
   }

   public EmailUserNotification toEmailUserNotification() {
      return new EmailUserNotification(this.getMessage(), this.subject, this.emailAddress);
   }
}
