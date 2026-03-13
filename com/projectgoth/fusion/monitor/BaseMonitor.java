package com.projectgoth.fusion.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.eclipse.swt.widgets.TreeItem;

public abstract class BaseMonitor implements Runnable {
   protected String hostName;
   protected boolean isOnline = true;
   protected TreeItem baseTreeItem;

   public BaseMonitor(String hostName, TreeItem parentTreeItem) {
      this.hostName = hostName;
      this.baseTreeItem = new TreeItem(parentTreeItem, 0);
      this.baseTreeItem.setText(hostName);
      parentTreeItem.setExpanded(true);
   }

   public String getHostName() {
      return this.hostName;
   }

   public boolean getIsOnline() {
      return this.isOnline;
   }

   public abstract void getStats();

   protected void sendAlert(String messageText) {
      SimpleDateFormat formatter = new SimpleDateFormat("HH:mm d MMM yy");
      String now = formatter.format(new Date());
      messageText = messageText.concat(" [" + now + "]");
      System.err.println("ALERT: " + messageText);
      if (Monitor.properties.getPropertyAsInt("SMTP.Disable") != 1) {
         try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", Monitor.smtpHost);
            Session session = Session.getDefaultInstance(properties, (Authenticator)null);
            MimeMessage message = new MimeMessage(session);

            try {
               message.setText(messageText);
               message.setSubject("ALERT: " + messageText);
            } catch (MessagingException var9) {
               var9.printStackTrace();
               return;
            }

            message.setFrom(new InternetAddress("alerts@projectgoth.com"));
            System.out.println("Sending alert email to: " + Monitor.smtpRecipients);
            String[] recipients = Monitor.smtpRecipients.split(";");

            for(int i = 0; i < recipients.length; ++i) {
               message.addRecipient(RecipientType.TO, new InternetAddress(recipients[i]));
            }

            Transport.send(message);
         } catch (MessagingException var10) {
            System.err.println("WARNING: Unable to send alert email");
            var10.printStackTrace();
            return;
         }
      } else {
         System.out.println("WARNING: Sending of email alerts is disabled");
      }

   }

   protected String toNiceDuration(long milliseconds) {
      long days = milliseconds / 86400000L;
      long hours = milliseconds % 86400000L / 3600000L;
      long minutes = milliseconds % 3600000L / 60000L;
      long seconds = milliseconds % 60000L / 1000L;
      StringBuffer sb = new StringBuffer();
      sb.append(days);
      sb.append(" day");
      if (days != 1L) {
         sb.append("s");
      }

      sb.append(" ");
      sb.append(hours);
      sb.append(" hour");
      if (hours != 1L) {
         sb.append("s");
      }

      sb.append(" ");
      sb.append(minutes);
      sb.append(" minute");
      if (minutes != 1L) {
         sb.append("s");
      }

      sb.append(" ");
      sb.append(seconds);
      sb.append(" second");
      if (seconds != 1L) {
         sb.append("s");
      }

      return sb.toString();
   }

   protected String toMegaBytes(long bytes) {
      return bytes < 1048576L ? new String(bytes / 1024L + "kB") : bytes / 1024L / 1024L + "MB";
   }
}
