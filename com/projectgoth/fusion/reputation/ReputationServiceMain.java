package com.projectgoth.fusion.reputation;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class ReputationServiceMain {
   private static final String APP_NAME = "ReputationService";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "ReputationService.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ReputationServiceMain.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("ReputationService version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = ReputationServiceContext.getContext();
      ReputationService app = (ReputationService)context.getBean("reputationService");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("ReputationService", args, args[0]);
      } else {
         status = app.main("ReputationService", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
