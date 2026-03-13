package com.projectgoth.fusion.messagelogger;

import Ice.Current;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.slice._MessageLoggerDisp;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class MessageLoggerI extends _MessageLoggerDisp implements Runnable {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MessageLoggerI.class));
   public LinkedList<MessageToLog> messagesToLog = new LinkedList();
   public int numMessagesQueued = 0;
   public int maxMessagesQueued = 0;
   private Thread logToDiskThread;
   private MessageLoggerI.StatsSaver statsSaver;
   public RequestCounter receivedMessagesCounter;
   public RequestCounter loggedMessagesCounter;
   private String basePath;
   private BufferedWriter writer = null;
   private boolean disableWritingToFile = false;
   private Date lastMessageDateCreated = null;
   SimpleDateFormat dateFormatterFullTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   SimpleDateFormat dateFormatterDateOnly = new SimpleDateFormat("yyyy-MM-dd");
   HashMap<Integer, MessageLoggerI.MessageStats> messageStatsPerCountry = new HashMap();

   public MessageLoggerI() {
      this.basePath = MessageLogger.properties.getPropertyWithDefault("BasePath", ".");
      this.receivedMessagesCounter = new RequestCounter((long)MessageLogger.properties.getPropertyAsIntWithDefault("RequestCounterInterval", 10));
      this.loggedMessagesCounter = new RequestCounter((long)MessageLogger.properties.getPropertyAsIntWithDefault("RequestCounterInterval", 10));
      this.logToDiskThread = new Thread(this);
      this.logToDiskThread.start();
      long saveStatsToDBInterval = (long)(MessageLogger.properties.getPropertyAsIntWithDefault("SaveStatsToDBInterval", 600) * 1000);
      log.info("Message stats will be written to the DB every " + saveStatsToDBInterval / 1000L + " seconds");
      this.disableWritingToFile = MessageLogger.properties.getPropertyAsIntWithDefault("DisableWritingToFile", 0) == 1;
      if (this.disableWritingToFile) {
         log.info("Messages will NOT be written to file (DisableWritingToFile is set to 1 in the config file)");
      }

      this.statsSaver = new MessageLoggerI.StatsSaver();
      Timer timer = new Timer(true);
      timer.schedule(this.statsSaver, saveStatsToDBInterval, saveStatsToDBInterval);
   }

   public void logMessage(int type, int sourceCountryID, String source, String destination, int numRecipients, String messageText, Current __current) {
      MessageToLog.TypeEnum messageType = MessageToLog.TypeEnum.fromValue(type);
      if (messageType == null) {
         log.warn("Received unknown message type: " + type);
      } else {
         synchronized(this.messagesToLog) {
            this.messagesToLog.add(new MessageToLog(new Date(), sourceCountryID, messageType, source, destination, numRecipients, messageText));
            ++this.numMessagesQueued;
            if (this.numMessagesQueued > this.maxMessagesQueued) {
               this.maxMessagesQueued = this.numMessagesQueued;
            }
         }

         this.receivedMessagesCounter.add();
      }
   }

   public void run() {
      while(true) {
         MessageToLog messageToLog;
         synchronized(this.messagesToLog) {
            messageToLog = (MessageToLog)this.messagesToLog.peek();
         }

         if (messageToLog == null) {
            try {
               Thread.sleep(200L);
            } catch (InterruptedException var6) {
            }
         } else {
            try {
               this.writeMessage(messageToLog);
            } catch (Exception var8) {
               Exception e = var8;

               try {
                  log.warn("Unable to write message. Exception: " + e.getMessage());
                  Thread.sleep(10000L);
               } catch (InterruptedException var5) {
               }
               continue;
            }

            synchronized(this.messagesToLog) {
               this.messagesToLog.poll();
               --this.numMessagesQueued;
            }
         }
      }
   }

   private synchronized void writeMessage(MessageToLog messageToLog) throws Exception {
      if (!this.datesAreInTheSameHour(this.lastMessageDateCreated, messageToLog.dateCreated)) {
         this.writeMessageStatsToDB();
         if (this.writer != null) {
            this.writer.close();
         }

         if (!this.disableWritingToFile) {
            this.writer = this.openNewFile(messageToLog.dateCreated);
         }
      }

      MessageLoggerI.MessageStats countryStats = (MessageLoggerI.MessageStats)this.messageStatsPerCountry.get(messageToLog.sourceCountryID);
      if (countryStats == null) {
         countryStats = new MessageLoggerI.MessageStats(messageToLog.sourceCountryID);
         this.messageStatsPerCountry.put(messageToLog.sourceCountryID, countryStats);
      }

      switch(messageToLog.type) {
      case PRIVATE:
         ++countryStats.numPrivate;
         break;
      case GROUPCHAT:
         ++countryStats.numGroupChatSent;
         countryStats.numGroupChatReceived += messageToLog.numRecipients;
         break;
      case CHATROOM:
         ++countryStats.numChatRoomSent;
         countryStats.numChatRoomReceived += messageToLog.numRecipients;
         break;
      case SMS:
         ++countryStats.numSMS;
         break;
      case MSN_SENT:
         ++countryStats.numMSNSent;
         break;
      case MSN_RECEIVED:
         ++countryStats.numMSNReceived;
         break;
      case YAHOO_SENT:
         ++countryStats.numYahooSent;
         break;
      case YAHOO_RECEIVED:
         ++countryStats.numYahooReceived;
         break;
      case AIM_SENT:
         ++countryStats.numAIMSent;
         break;
      case AIM_RECEIVED:
         ++countryStats.numAIMReceived;
         break;
      case GTALK_SENT:
         ++countryStats.numGTalkSent;
         break;
      case GTALK_RECEIVED:
         ++countryStats.numGTalkReceived;
         break;
      case FACEBOOK_SENT:
         ++countryStats.numFacebookSent;
         break;
      case FACEBOOK_RECEIVED:
         ++countryStats.numFacebookReceived;
         break;
      default:
         log.warn("Received unrecognised message type: " + messageToLog.type);
      }

      if (!this.disableWritingToFile) {
         this.writer.write(this.convertMessageToString(messageToLog));
         this.writer.newLine();
         this.loggedMessagesCounter.add();
      }

      this.lastMessageDateCreated = messageToLog.dateCreated;
   }

   private boolean datesAreInTheSameHour(Date date1, Date date2) {
      if (date1 != null && date2 != null) {
         if (date1.getYear() != date2.getYear()) {
            return false;
         } else if (date1.getMonth() != date2.getMonth()) {
            return false;
         } else if (date1.getDate() != date2.getDate()) {
            return false;
         } else {
            return date1.getHours() == date2.getHours();
         }
      } else {
         return false;
      }
   }

   private BufferedWriter openNewFile(Date date) throws Exception {
      String year = Integer.toString(date.getYear() + 1900);
      String month = Integer.toString(date.getMonth() + 1);
      if (month.length() < 2) {
         month = "0" + month;
      }

      String day = Integer.toString(date.getDate());
      if (day.length() < 2) {
         day = "0" + day;
      }

      String hour = Integer.toString(date.getHours());
      if (hour.length() < 2) {
         hour = "0" + hour;
      }

      File logFilePath = new File(this.basePath + File.separator + year + File.separator + month + File.separator + day);
      File logFile = new File(logFilePath.getPath() + File.separator + year + month + day + "T" + hour + ".log");
      logFilePath.mkdirs();
      logFile.createNewFile();
      return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));
   }

   private String convertMessageToString(MessageToLog messageToLog) {
      StringBuilder sb = new StringBuilder(messageToLog.messageText.length() + messageToLog.source.length() + messageToLog.destination.length() + 40);
      sb.append(this.dateFormatterFullTime.format(messageToLog.dateCreated));
      sb.append(',');
      sb.append(messageToLog.type.value());
      sb.append(',');
      sb.append(messageToLog.sourceCountryID);
      sb.append(",\"");
      sb.append(messageToLog.source);
      sb.append("\",\"");
      sb.append(messageToLog.destination);
      sb.append("\",");
      sb.append(messageToLog.numRecipients);
      sb.append(",\"");
      sb.append(messageToLog.messageText);
      sb.append('"');
      return sb.toString();
   }

   public void terminating() {
      if (this.writer != null) {
         try {
            this.writer.close();
         } catch (IOException var3) {
            log.warn("Unable to close file: " + var3.getMessage());
         }
      }

      if (this.lastMessageDateCreated != null) {
         try {
            this.writeMessageStatsToDB();
         } catch (Exception var2) {
            log.warn("Unable to save remaining message stats to DB: " + var2.getMessage());
         }
      }

   }

   private synchronized void writeMessageStatsToDB() throws Exception {
      if (this.lastMessageDateCreated != null) {
         Message messageEJB = null;

         try {
            messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         } catch (Exception var7) {
            throw new Exception("Unable to create EJB to log message stats for " + this.dateFormatterDateOnly.format(this.lastMessageDateCreated) + " to the DB. Exception: " + var7.getMessage());
         }

         Iterator i = this.messageStatsPerCountry.values().iterator();

         while(i.hasNext()) {
            MessageLoggerI.MessageStats countryStat = (MessageLoggerI.MessageStats)i.next();

            try {
               messageEJB.logMessageStats(this.lastMessageDateCreated, countryStat.countryID, countryStat.numPrivate, countryStat.numGroupChatSent, countryStat.numGroupChatReceived, countryStat.numChatRoomSent, countryStat.numChatRoomReceived, countryStat.numSMS, countryStat.numMSNSent, countryStat.numMSNReceived, countryStat.numYahooSent, countryStat.numYahooReceived, countryStat.numAIMSent, countryStat.numAIMReceived, countryStat.numGTalkSent, countryStat.numGTalkReceived, countryStat.numFacebookSent, countryStat.numFacebookReceived);
               i.remove();
            } catch (RemoteException var5) {
               throw new Exception("Unable to log message stats for " + this.dateFormatterDateOnly.format(this.lastMessageDateCreated) + " for the country ID " + countryStat.countryID + " to the DB. Exception: " + RMIExceptionHelper.getRootMessage(var5));
            } catch (Exception var6) {
               throw new Exception("Unable to log message stats for " + this.dateFormatterDateOnly.format(this.lastMessageDateCreated) + " for the country ID " + countryStat.countryID + " to the DB. Exception: " + var6.getMessage());
            }
         }

      }
   }

   private class MessageStats {
      public int countryID;
      public int numPrivate = 0;
      public int numGroupChatSent = 0;
      public int numGroupChatReceived = 0;
      public int numChatRoomSent = 0;
      public int numChatRoomReceived = 0;
      public int numSMS = 0;
      public int numMSNSent = 0;
      public int numMSNReceived = 0;
      public int numYahooSent = 0;
      public int numYahooReceived = 0;
      public int numAIMSent = 0;
      public int numAIMReceived = 0;
      public int numGTalkSent = 0;
      public int numGTalkReceived = 0;
      public int numFacebookSent = 0;
      public int numFacebookReceived = 0;

      public MessageStats(int countryID) {
         this.countryID = countryID;
      }
   }

   private class StatsSaver extends TimerTask {
      private StatsSaver() {
      }

      public void run() {
         try {
            MessageLoggerI.this.writeMessageStatsToDB();
         } catch (Exception var2) {
            MessageLoggerI.log.warn("Unable to save stats to DB: " + var2.getMessage());
         }

      }

      // $FF: synthetic method
      StatsSaver(Object x1) {
         this();
      }
   }
}
