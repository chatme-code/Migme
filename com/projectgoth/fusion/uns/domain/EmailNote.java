package com.projectgoth.fusion.uns.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class EmailNote extends Note {
   private String sender;
   private String senderPassword;
   private Set<String> recipients;
   private String subject;
   private List<EmailNote.EmailPart> emailExtraParts;
   private String mimeType;

   public EmailNote(String text, String subject) {
      this(text, subject, (String)null);
   }

   public EmailNote(String text, String subject, String mimeType) {
      super(text);
      this.recipients = new HashSet();
      this.emailExtraParts = new ArrayList();
      this.subject = subject;
      this.mimeType = mimeType;
   }

   public EmailNote(String sender, String senderPassword, String text, String subject) {
      super(text);
      this.recipients = new HashSet();
      this.emailExtraParts = new ArrayList();
      this.sender = sender;
      this.senderPassword = senderPassword;
      this.subject = subject;
   }

   public Set<String> getRecipients() {
      return this.recipients;
   }

   public void addRecipient(String recipient) {
      this.recipients.add(recipient);
   }

   public String getSender() {
      return this.sender;
   }

   public void setSender(String sender) {
      this.sender = sender;
   }

   public String getSenderPassword() {
      return this.senderPassword;
   }

   public void setSenderPassword(String senderPassword) {
      this.senderPassword = senderPassword;
   }

   public String getSubject() {
      return this.subject;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

   public boolean hasRecipients() {
      return !this.recipients.isEmpty();
   }

   public InternetAddress getSenderInternetAddress() throws AddressException {
      return new InternetAddress(this.sender);
   }

   public InternetAddress[] getRecipientInternetAddresses() throws AddressException {
      InternetAddress[] recipientAddresses = new InternetAddress[this.recipients.size()];
      int i = 0;

      String recipient;
      for(Iterator i$ = this.recipients.iterator(); i$.hasNext(); recipientAddresses[i++] = new InternetAddress(recipient)) {
         recipient = (String)i$.next();
      }

      return recipientAddresses;
   }

   public String getMimeType() {
      return this.mimeType;
   }

   public void addExtraPart(EmailNote.EmailPart part) {
      this.emailExtraParts.add(part);
   }

   public EmailNote.EmailPart get(int index) {
      return (EmailNote.EmailPart)this.emailExtraParts.get(index);
   }

   public int extraPartCount() {
      return this.emailExtraParts.size();
   }

   public static class EmailPart {
      public String message;
      public String mimeType;

      public EmailPart(String message, String mimeType) {
         this.message = message;
         this.mimeType = mimeType;
      }

      public String toString() {
         return "MIMEType=[" + this.mimeType + "];Message=[" + this.message + "]";
      }
   }
}
