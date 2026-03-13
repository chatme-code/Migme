package com.projectgoth.fusion.uns;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.uns.domain.EmailNote;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;
import org.apache.log4j.Logger;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class EmailQueueWorkerThread extends Thread {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EmailQueueWorkerThread.class));
   private static final Logger emailLogger = Logger.getLogger("EmailQueueWorker");
   private static final String emailLoggerMsg = "|%s|%s|%s|%s";
   private BlockingQueue<EmailNote> queue;
   private MailSender mailSender;
   private String mailServer;
   private boolean dryRun;
   private AtomicLong emailsSent;

   public EmailQueueWorkerThread(BlockingQueue<EmailNote> queue, MailSender mailSender, String mailServer, boolean dryRun, AtomicLong emailsSent) {
      this.queue = queue;
      this.mailSender = mailSender;
      this.mailServer = mailServer;
      this.dryRun = dryRun;
      this.emailsSent = emailsSent;
      System.setProperty("mail.smtp.host", mailServer);
      System.setProperty("mail.smtp.timeout", "20000");
      System.setProperty("mail.smtp.connectiontimeout", "20000");
      System.setProperty("mail.imap.timeout", "20000");
      System.setProperty("mail.imap.connectiontimeout", "20000");
   }

   public void run() {
      while(true) {
         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.NEW_EMAIL_SENDING_LOGIC_ENABLED)) {
               this.newEmailSendingLogic();
            } else {
               this.oldEmailSendingLogic();
            }
         } catch (InterruptedException var2) {
         } catch (Exception var3) {
            log.error("failed to send email ", var3);
         }
      }
   }

   private void oldEmailSendingLogic() throws Exception {
      EmailNote note = (EmailNote)this.queue.take();
      if (note.getSender() == null) {
         SimpleMailMessage message = new SimpleMailMessage();
         if (note.getRecipients().size() > 1) {
            message.setTo("\"migme\" <mig@mig.me>");
            message.setBcc((String[])note.getRecipients().toArray(new String[0]));
         } else {
            message.setTo((String[])note.getRecipients().toArray(new String[0]));
         }

         message.setSubject(note.getSubject());
         message.setText(note.getText());
         message.setFrom(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.FROM_EMAIL_ADDRESS));
         if (log.isDebugEnabled()) {
            log.debug("sending email subject [" + message.getSubject() + "] to [" + StringUtil.asString(message.getTo()) + "] bcc [" + StringUtil.asString(message.getBcc()) + "] body:\n" + message.getText());
         }

         if (!this.dryRun) {
            try {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_BEFORE_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 0, "NULL"));
               }

               this.mailSender.send(message);
               this.emailsSent.incrementAndGet();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_AFTER_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 1, "NULL"));
               }
            } catch (Exception var7) {
               emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), -1, StringUtil.customizeStringForLogging(var7.getMessage(), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_EMAIL_SENDING_ERROR_MSG_LENGTH, 1000))));
               throw new Exception("Error sending " + note.getID() + ".Exception:" + var7, var7);
            }
         }
      } else {
         Session session = Session.getInstance(System.getProperties(), (Authenticator)null);
         MimeMessage message = new MimeMessage(session);
         message.setFrom(note.getSenderInternetAddress());
         message.setRecipients(RecipientType.TO, note.getRecipientInternetAddresses());
         message.setSubject(note.getSubject());
         message.setText(note.getText());
         if (!this.dryRun) {
            try {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_BEFORE_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 0, "NULL"));
               }

               Transport.send(message);
               this.emailsSent.incrementAndGet();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_AFTER_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 1, "NULL"));
               }
            } catch (Exception var6) {
               emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), -1, StringUtil.customizeStringForLogging(var6.getMessage(), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_EMAIL_SENDING_ERROR_MSG_LENGTH, 1000))));
               throw new Exception("Error sending " + note.getID() + ".Exception:" + var6, var6);
            }
         }

         Store store = session.getStore("imap");
         store.connect(this.mailServer, note.getSender(), note.getSenderPassword());
         Folder folder = store.getFolder("Sent Items");
         if (!folder.exists()) {
            folder.create(1);
         }

         folder.open(2);
         message.setFlag(Flag.SEEN, true);
         message.setSentDate(new Date());
         message.saveChanges();
         if (!this.dryRun) {
            folder.appendMessages(new Message[]{message});
         }

         folder.close(false);
         store.close();
      }

   }

   private void newEmailSendingLogic() throws Exception {
      EmailNote note = (EmailNote)this.queue.take();
      boolean isSystemNotification = note.getSender() == null;
      if (isSystemNotification && note.getMimeType() == null) {
         SimpleMailMessage message = new SimpleMailMessage();
         if (note.getRecipients().size() > 1) {
            message.setTo("\"migme\" <mig@mig.me>");
            message.setBcc((String[])note.getRecipients().toArray(new String[0]));
         } else {
            message.setTo((String[])note.getRecipients().toArray(new String[0]));
         }

         message.setSubject(note.getSubject());
         message.setText(note.getText());
         message.setFrom(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.FROM_EMAIL_ADDRESS));
         if (log.isDebugEnabled()) {
            log.debug("newEmailSendingLogic():sending email subject [" + message.getSubject() + "] to [" + StringUtil.asString(message.getTo()) + "] bcc [" + StringUtil.asString(message.getBcc()) + "] body:\n" + message.getText());
         }

         if (!this.dryRun) {
            try {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_BEFORE_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 0, "NULL"));
               }

               this.mailSender.send(message);
               this.emailsSent.incrementAndGet();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_AFTER_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 1, "NULL"));
               }
            } catch (Exception var12) {
               emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), -1, StringUtil.customizeStringForLogging(var12.getMessage(), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_EMAIL_SENDING_ERROR_MSG_LENGTH, 1000))));
               throw new Exception("Error sending " + note.getID() + ".Exception:" + var12, var12);
            }
         }
      } else {
         Session session = Session.getInstance(System.getProperties(), (Authenticator)null);
         MimeMessage message = new MimeMessage(session);
         if (isSystemNotification) {
            message.setFrom(new InternetAddress(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.FROM_EMAIL_ADDRESS)));
            InternetAddress[] recipients = note.getRecipientInternetAddresses();
            if (recipients != null && recipients.length > 1) {
               message.setRecipients(RecipientType.TO, "noreply@mig.me");
               message.setRecipients(RecipientType.BCC, recipients);
            } else {
               message.setRecipients(RecipientType.TO, recipients);
            }
         } else {
            message.setFrom(note.getSenderInternetAddress());
            message.setRecipients(RecipientType.TO, note.getRecipientInternetAddresses());
         }

         message.setSubject(note.getSubject());
         if (note.extraPartCount() == 0) {
            if (note.getMimeType() == null) {
               message.setText(note.getText());
            } else {
               message.setContent(note.getText(), note.getMimeType());
            }
         } else {
            MimeMultipart multiPart = new MimeMultipart("alternative");
            MimeBodyPart mainPart = new MimeBodyPart();
            if (StringUtil.isBlank(note.getMimeType())) {
               mainPart.setText(note.getText());
            } else {
               mainPart.setContent(note.getText(), note.getMimeType());
            }

            multiPart.addBodyPart(mainPart);

            for(int i = 0; i < note.extraPartCount(); ++i) {
               EmailNote.EmailPart extraEmailPart = note.get(i);
               MimeBodyPart extraMimePart = new MimeBodyPart();
               if (StringUtil.isBlank(extraEmailPart.mimeType)) {
                  extraMimePart.setText(extraEmailPart.message);
               } else {
                  extraMimePart.setContent(extraEmailPart.message, extraEmailPart.mimeType);
               }

               multiPart.addBodyPart(extraMimePart);
            }

            message.setContent(multiPart);
         }

         if (log.isDebugEnabled()) {
            try {
               Address[] recipients = message.getRecipients(javax.mail.Message.RecipientType.TO);
               String contentType = message.getContentType();
               Object content = message.getContent();
               String recipientListToString = recipients == null ? (String)null : Arrays.asList(recipients).toString();
               log.debug("newEmailSendingLogic():sending email subject [" + message.getSubject() + "] recipients:[" + recipientListToString + "] contentType:[" + contentType + "] body:\n" + content);
            } catch (Exception var11) {
               log.info("Unable to do a log debug for message[" + message + "]", var11);
            }
         }

         if (!this.dryRun) {
            try {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_BEFORE_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 0, "NULL"));
               }

               Transport.send(message);
               this.emailsSent.incrementAndGet();
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.LOG_AFTER_SENDING_EMAIL_TO_SMTP_ENABLED)) {
                  emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), 1, "NULL"));
               }
            } catch (Exception var10) {
               emailLogger.info(String.format("|%s|%s|%s|%s", System.currentTimeMillis(), note.getID(), -1, StringUtil.customizeStringForLogging(var10.getMessage(), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_EMAIL_SENDING_ERROR_MSG_LENGTH, 1000))));
               throw new Exception("Error sending " + note.getID() + ".Exception:" + var10, var10);
            }
         }

         if (!isSystemNotification) {
            Store store = session.getStore("imap");
            store.connect(this.mailServer, note.getSender(), note.getSenderPassword());
            Folder folder = store.getFolder("Sent Items");
            if (!folder.exists()) {
               folder.create(1);
            }

            folder.open(2);
            message.setFlag(Flag.SEEN, true);
            message.setSentDate(new Date());
            message.saveChanges();
            if (!this.dryRun) {
               folder.appendMessages(new Message[]{message});
            }

            folder.close(false);
            store.close();
         }
      }

   }
}
