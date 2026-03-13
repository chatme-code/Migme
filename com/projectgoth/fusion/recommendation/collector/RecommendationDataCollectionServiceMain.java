package com.projectgoth.fusion.recommendation.collector;

import com.projectgoth.fusion.common.AppStartupInfo;
import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class RecommendationDataCollectionServiceMain {
   private static final String APP_NAME = "RecommendationDataCollectionServiceMain";
   private static final String CONFIG_FILE = ConfigUtils.getConfigDirectory() + "RecommendationDataCollectionServiceMain.cfg";
   public static final String JVM_PROPERTY_RDCS_INSTANCE_NAME = "RDCS.Instance.Name";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecommendationDataCollectionServiceMain.class));

   public static void main(String[] args) {
      System.out.println("Log Folder :[" + ConfigUtils.getRootLogFolder() + "]");
      ConfigUtils.mkdirsRootLogFolder();
      DOMConfigurator.configureAndWatch(ConfigUtils.getDefaultLog4jConfigFilename());
      log.info("RecommendationDataCollectionServiceMain version @version@");
      log.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      String rdcsInstanceName = System.getProperty("RDCS.Instance.Name");
      AppStartupInfo appStartupInfo = new AppStartupInfo(System.currentTimeMillis(), args);
      DataCollectorContext dataCollectorCtx = DataCollectorUtils.createDefaultDataCollectorContext(appStartupInfo);
      RecommendationDataCollectionServiceAdminI rdcsAdminI = new RecommendationDataCollectionServiceAdminI(dataCollectorCtx);
      RecommendationDataCollectionServiceI rdcsI = new RecommendationDataCollectionServiceI(dataCollectorCtx);
      RecommendationDataCollectionService app = new RecommendationDataCollectionService(rdcsInstanceName, rdcsI, rdcsAdminI);
      int status;
      if (args.length >= 1) {
         log.info("Using custom configuration file: " + args[0]);
         status = app.main("RecommendationDataCollectionServiceMain", args, args[0]);
      } else {
         status = app.main("RecommendationDataCollectionServiceMain", args, CONFIG_FILE);
      }

      log.info("Exiting application. Status=" + status);
      System.exit(status);
   }
}
