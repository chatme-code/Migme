package com.projectgoth.fusion.messagelogger;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class MessageLogger extends Application {
   private static final String APP_NAME = "MessageLogger";
   private static final String CONFIG_FILE = "MessageLogger.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MessageLogger.class));
   public static ObjectAdapter messageLoggerAdapter = null;
   public static Properties properties = null;
   public static MessageLoggerI messageLogger = null;
   public static String hostName = null;
   public static long startTime = System.currentTimeMillis();

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("MessageLogger version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      MessageLogger app = new MessageLogger();
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("MessageLogger", args, args[0]);
      } else {
         status = app.main("MessageLogger", args, "MessageLogger.cfg");
      }

      log.info("Exiting application");
      System.exit(status);
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var4) {
         hostName = "UNKNOWN";
      }

      log.debug("Initialising MessageLogger interface");
      messageLoggerAdapter = communicator().createObjectAdapter("MessageLoggerAdapter");
      messageLogger = new MessageLoggerI();
      messageLoggerAdapter.add(messageLogger, Util.stringToIdentity("MessageLogger"));
      IceStats.getInstance().setIceObjects(communicator(), messageLoggerAdapter, (ConfigurableExecutor)null);
      log.debug("Initialising MessageLoggerAdmin interface");
      ObjectAdapter messageLoggerAdminAdapter = communicator().createObjectAdapter("MessageLoggerAdminAdapter");
      MessageLoggerAdminI messageLoggerAdmin = new MessageLoggerAdminI();
      messageLoggerAdminAdapter.add(messageLoggerAdmin, Util.stringToIdentity("MessageLoggerAdmin"));
      messageLoggerAdminAdapter.activate();
      messageLoggerAdapter.activate();
      log.info("Service started");
      communicator().waitForShutdown();
      if (interrupted()) {
         log.fatal("MessageLogger " + hostName + ": terminating");
         messageLogger.terminating();
      }

      return 0;
   }
}
