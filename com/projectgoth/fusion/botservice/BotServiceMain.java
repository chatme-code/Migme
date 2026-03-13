package com.projectgoth.fusion.botservice;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class BotServiceMain {
   private static final String APP_NAME = "BotService";
   private static final String CONFIG_FILE = "BotService.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BotServiceMain.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("BotService version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = BotServiceContext.getContext();
      BotService app = (BotService)context.getBean("botService");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("BotService", args, args[0]);
      } else {
         status = app.main("BotService", args, "BotService.cfg");
      }

      log.info("Exiting the BotService application");
      System.exit(status);
   }
}
