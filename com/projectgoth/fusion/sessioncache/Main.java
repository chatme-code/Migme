package com.projectgoth.fusion.sessioncache;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class Main {
   private static final String APP_NAME = "SessionCache";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "SessionCache.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Main.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("SessionCache version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = SessionCacheApplicationContext.getContext();
      SessionCache app = (SessionCache)context.getBean("sessionCache");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("SessionCache", args, args[0]);
      } else {
         status = app.main("SessionCache", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
