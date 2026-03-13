package com.projectgoth.fusion.bl1;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class BlueLabelServiceMain {
   private static final String APP_NAME = "BlueLabelService";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "BlueLabelService.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(BlueLabelServiceMain.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("BlueLabelService version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = BlueLabelServiceContext.getContext();
      BlueLabelService app = (BlueLabelService)context.getBean("blueLabelService");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("BlueLabelService", args, args[0]);
      } else {
         status = app.main("BlueLabelService", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
