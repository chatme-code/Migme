package com.projectgoth.fusion.recommendation.generation;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

public class RecommendationGenerationService extends Application {
   private static final Logger log = Logger.getLogger(RecommendationGenerationService.class);
   public static ObjectAdapter rgsAdapter = null;
   public static Properties properties = null;
   public static long startTime = System.currentTimeMillis();
   private String hostName;
   private RecommendationGenerationServiceI rgsServant;

   public RecommendationGenerationService(RecommendationGenerationServiceI servant) {
      this.rgsServant = servant;
      log.info("Set servant ok");
   }

   private void configureServant(Properties properties) throws Exception {
      IcePrxFinder icePrxFinder = new IcePrxFinder(communicator(), RecommendationGenerationService.properties);
      this.rgsServant.setIcePrxFinder(icePrxFinder);
      if (log.isDebugEnabled()) {
         log.debug("Configured servant ok");
      }

   }

   public int run(String[] arg0) {
      String jdbcDriverClass = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.HIVE2_JDBC_DRIVER_CLASS);

      try {
         Class.forName(jdbcDriverClass);
      } catch (Exception var13) {
         log.error("App cannot start as unable to instantiate Hive2 JDBC driver: " + jdbcDriverClass + " It needs to be explicitly specified in the classpath. e=" + var13, var13);
         Runtime.getRuntime().exit(-1);
      }

      properties = communicator().getProperties();

      try {
         this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var12) {
         this.hostName = "UNKNOWN";
      }

      try {
         this.configureServant(properties);
         log.info("Configured endpoint [" + properties.getProperty("RecommendationGenerationServiceAdapter.Endpoints") + "]");
         if (log.isDebugEnabled()) {
            log.debug("Initialising RecommendationGenerationServiceAdapter interface");
         }

         rgsAdapter = communicator().createObjectAdapter("RecommendationGenerationServiceAdapter");
         rgsAdapter.add(this.rgsServant, Util.stringToIdentity("RecommendationGenerationService"));
         this.createAdminInterface();
         rgsAdapter.activate();
         log.info("Recommendation Generation Service started");
         this.rgsServant.initialize();
         if (log.isDebugEnabled()) {
            log.debug("Initialized servant");
         }

         while(!communicator().isShutdown()) {
            try {
               int refreshIntervalSecs = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RGS_INTERNAL_TIMER_REFRESH_INTERVAL_SECONDS);
               Thread.sleep((long)(refreshIntervalSecs * 1000));
               this.rgsServant.reinitialize();
            } catch (InterruptedException var11) {
            }
         }
      } catch (Exception var14) {
         log.error("failed to configure servant", var14);
      } finally {
         if (interrupted()) {
            log.fatal("RecommendationGenerationService " + this.hostName + ": terminating");
            this.rgsServant.shutdown();
         }

      }

      return 0;
   }

   private void createAdminInterface() throws Exception {
      log.info("Initialising RecommendationGenerationServiceAdmin interface");
      ObjectAdapter rgsAdminAdapter = communicator().createObjectAdapter("RecommendationGenerationServiceAdminAdapter");
      RecommendationGenerationServiceAdminI rgsAdmin = new RecommendationGenerationServiceAdminI(this.rgsServant);
      rgsAdminAdapter.add(rgsAdmin, Util.stringToIdentity("RecommendationGenerationServiceAdmin"));
      rgsAdminAdapter.activate();
   }
}
