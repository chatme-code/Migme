package com.projectgoth.fusion.recommendation.collector;

import Ice.Application;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.HostUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecommendationDataCollectionService extends Application {
   private static Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(RecommendationDataCollectionService.class));
   private final RecommendationDataCollectionServiceI rdcsI;
   private final RecommendationDataCollectionServiceAdminI rdcsAdminI;
   private final String instanceName;

   public RecommendationDataCollectionService(String instanceName, RecommendationDataCollectionServiceI rdcsI, RecommendationDataCollectionServiceAdminI rdcsAdminI) {
      this.instanceName = instanceName;
      this.rdcsI = rdcsI;
      this.rdcsAdminI = rdcsAdminI;
   }

   public int run(String[] arg0) {
      RDCSIceServerApp iceServerApp = new RDCSIceServerApp(this.instanceName, communicator(), this.rdcsI, this.rdcsAdminI);
      iceServerApp.activateAdaptersAndWaitTillShutdown();
      if (interrupted()) {
         log.error("RecommendationDataCollectionService@" + HostUtils.getHostname() + ": terminating");
      }

      return 0;
   }
}
