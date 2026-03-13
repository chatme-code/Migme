package com.projectgoth.fusion.emailalert;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.RequestCounter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;
import org.apache.log4j.Logger;

public class EmailAlert extends Application {
   private static final String APP_NAME = "EmailAlert";
   private static final String CONFIG_FILE = "EmailAlert.cfg";
   public static Logger logger = Logger.getLogger("EmailAlert");
   public static ObjectAdapter emailAlertAdapter = null;
   public static Properties properties = null;
   public static IcePrxFinder icePrxFinder = null;
   public static EmailAlertI emailAlert = null;
   public static SMTPServer smtpServer = null;
   public static String hostName = null;
   public static String mailDomain = null;
   public static String imapServerAddress = null;
   public static String aliasesToIgnore = null;
   public static long startTime = System.currentTimeMillis();
   public static RequestCounter receivedNotificationsCounter = new RequestCounter();
   public static RequestCounter processedNotificationsCounter = new RequestCounter();
   public static RequestCounter receivedGatewayQueriesCounter = new RequestCounter();
   public static RequestCounter processedGatewayQueriesCounter = new RequestCounter();
   public static RequestCounter discardedGatewayQueriesCounter = new RequestCounter();
   public static ThreadPoolExecutor gatewayQueriesPool;
   public static ThreadPoolExecutor notificationsThreadPool;

   public static void main(String[] args) {
      logger.info("EmailAlert version @version@");
      logger.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      EmailAlert app = new EmailAlert();
      int status;
      if (args.length >= 1) {
         logger.info("Using custom configuration file: " + args[0]);
         status = app.main("EmailAlert", args, args[0]);
      } else {
         status = app.main("EmailAlert", args, "EmailAlert.cfg");
      }

      System.exit(status);
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();
      gatewayQueriesPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(properties.getPropertyAsIntWithDefault("GatewayQueriesThreadPoolSize", 10));
      notificationsThreadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(properties.getPropertyAsIntWithDefault("NotificationsThreadPoolSize", 10));
      icePrxFinder = new IcePrxFinder(communicator(), properties);

      try {
         hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var6) {
         hostName = "UNKNOWN";
      }

      mailDomain = properties.getProperty("MailDomain").toLowerCase();
      if (mailDomain != null && mailDomain.length() != 0) {
         logger.info("Mail domain: " + mailDomain);
         aliasesToIgnore = properties.getProperty("AliasesToIgnore");
         if (aliasesToIgnore != null && aliasesToIgnore.length() != 0) {
            logger.info("Aliases to ignore: " + aliasesToIgnore);
            imapServerAddress = properties.getProperty("IMAPServerAddress");
            if (imapServerAddress != null && imapServerAddress.length() != 0) {
               logger.info("IMAP server: " + imapServerAddress);
               emailAlertAdapter = communicator().createObjectAdapter("EmailAlertAdapter");
               emailAlert = new EmailAlertI();
               emailAlertAdapter.add(emailAlert, Util.stringToIdentity("EmailAlert"));
               int port = properties.getPropertyAsIntWithDefault("SocketPort", 25);

               try {
                  smtpServer = new SMTPServer(port);
               } catch (Exception var5) {
                  System.out.println("Could not listen on port " + port + ": " + var5.getMessage());
                  return 0;
               }

               ObjectAdapter emailAlertAdminAdapter = communicator().createObjectAdapter("EmailAlertAdminAdapter");
               EmailAlertAdminI emailAlertAdmin = new EmailAlertAdminI();
               emailAlertAdminAdapter.add(emailAlertAdmin, Util.stringToIdentity("EmailAlertAdmin"));
               emailAlertAdminAdapter.activate();
               emailAlertAdapter.activate();
               logger.info("Service started");
               communicator().waitForShutdown();
               if (interrupted()) {
                  logger.fatal("EmailAlert " + hostName + ": terminating");
               }

               return 0;
            } else {
               System.out.println("IMAPServerAddress must be specified in the configuration file");
               return 0;
            }
         } else {
            System.out.println("AliasesToIgnore must be specified in the configuration file");
            return 0;
         }
      } else {
         System.out.println("MailDomain must be specified in the configuration file");
         return 0;
      }
   }

   public static int getUnreadEmailCountFromIMAP(String username, String password) throws Exception {
      Store store = null;
      Folder folder = null;

      int var5;
      try {
         Session session = Session.getInstance(System.getProperties(), (Authenticator)null);
         store = session.getStore("imap");
         store.connect(imapServerAddress, username, password);
         folder = store.getFolder("INBOX");
         folder.open(1);
         var5 = folder.getUnreadMessageCount();
      } finally {
         if (folder != null) {
            folder.close(false);
         }

         if (store != null) {
            store.close();
         }

      }

      return var5;
   }
}
