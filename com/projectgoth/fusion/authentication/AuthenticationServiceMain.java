package com.projectgoth.fusion.authentication;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class AuthenticationServiceMain {
   private static final String APP_NAME = "AuthenticationService";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "AuthenticationService.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AuthenticationServiceMain.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("AuthenticationService version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = AuthenticationServiceContext.getContext();
      AuthenticationService app = (AuthenticationService)context.getBean("authenticationService");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("AuthenticationService", args, args[0]);
      } else {
         status = app.main("AuthenticationService", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
