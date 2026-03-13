package com.projectgoth.fusion.recommendation.generation;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class RecommendationGenerationServiceMain {
   private static final String APP_NAME = "RecommendationGenerationService";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "RecommendationGenerationService.cfg";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecommendationGenerationServiceMain.class));

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("RecommendationGenerationService version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      RecommendationGenerationServiceI servant = new RecommendationGenerationServiceI();
      RecommendationGenerationService app = new RecommendationGenerationService(servant);
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("RecommendationGenerationService", args, args[0]);
      } else {
         status = app.main("RecommendationGenerationService", args, CONFIG_FILE);
      }

      log.info("Exiting application");
      System.exit(status);
   }
}
