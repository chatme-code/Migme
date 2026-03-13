package com.projectgoth.fusion.uns;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class UserNotificationServiceMain {
   private static final String APP_NAME = "UserNotificationService";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "UserNotificationService.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserNotificationServiceMain.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("UserNotificationService version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = UserNotificationServiceContext.getContext();
      UserNotificationService app = (UserNotificationService)context.getBean("userNotificationService");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("UserNotificationService", args, args[0]);
      } else {
         status = app.main("UserNotificationService", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
