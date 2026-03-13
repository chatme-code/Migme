package com.projectgoth.fusion.jobscheduling;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;

public class JobSchedulingServiceMain {
   private static final String APP_NAME = "JobSchedulingService";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "JobSchedulingService.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(JobSchedulingServiceMain.class));
   private static ApplicationContext context;

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      log.info("JobSchedulingService version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      context = JobSchedulingServiceContext.getContext();
      JobSchedulingService app = (JobSchedulingService)context.getBean("jobSchedulingService");
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("JobSchedulingService", args, args[0]);
      } else {
         status = app.main("JobSchedulingService", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
