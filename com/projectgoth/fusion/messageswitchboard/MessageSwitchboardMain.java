package com.projectgoth.fusion.messageswitchboard;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class MessageSwitchboardMain {
   private static final String APP_NAME = "MessageSwitchboard";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "MessageSwitchboard" + ".cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MessageSwitchboardMain.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("MessageSwitchboard version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = MessageSwitchboardContext.getContext();
      MessageSwitchboard app = (MessageSwitchboard)context.getBean("messageSwitchboard");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("MessageSwitchboard", args, args[0]);
      } else {
         status = app.main("MessageSwitchboard", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
