package com.projectgoth.fusion.userevent.store;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class Main {
   private static final String APP_NAME = "EventStore";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "EventStore.cfg";
   private static Logger log = Logger.getLogger(Main.class);
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("EventStore version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = EventStoreApplicationContext.getContext();
      EventStore app = (EventStore)context.getBean("eventStore");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("EventStore", args, args[0]);
      } else {
         status = app.main("EventStore", args, CONFIG_FILE);
      }

      System.exit(status);
   }
}
