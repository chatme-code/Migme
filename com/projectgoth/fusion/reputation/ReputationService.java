package com.projectgoth.fusion.reputation;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class ReputationService extends Application {
   private static Logger log = Logger.getLogger(ReputationService.class);
   public static ObjectAdapter ReputationServiceAdapter = null;
   public static Properties properties = null;
   public static long startTime = System.currentTimeMillis();
   private String hostName;
   private ReputationServiceI reputationServiceServant;

   @Required
   public void setReputationServiceServant(ReputationServiceI reputationServiceServant) {
      this.reputationServiceServant = reputationServiceServant;
   }

   private void configureServant(Properties properties) throws Exception {
      IcePrxFinder icePrxFinder = new IcePrxFinder(communicator(), ReputationService.properties);
      this.reputationServiceServant.setIcePrxFinder(icePrxFinder);
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var10) {
         this.hostName = "UNKNOWN";
      }

      try {
         this.configureServant(properties);
         log.info("Configured endpoint [" + properties.getProperty("ReputationServiceAdapter.Endpoints") + "]");
         log.debug("Initialising ReputationServiceAdapter interface");
         ReputationServiceAdapter = communicator().createObjectAdapter("ReputationServiceAdapter");
         ReputationServiceAdapter.add(this.reputationServiceServant, Util.stringToIdentity("ReputationService"));
         IceStats.getInstance().setIceObjects(communicator(), ReputationServiceAdapter, (ConfigurableExecutor)null);
         log.debug("Initialising ReputationServiceAdmin interface");
         ObjectAdapter reputationServiceAdminAdapter = communicator().createObjectAdapter("ReputationServiceAdminAdapter");
         ReputationServiceAdminI reputationServiceAdmin = new ReputationServiceAdminI(this.reputationServiceServant);
         reputationServiceAdminAdapter.add(reputationServiceAdmin, Util.stringToIdentity("ReputationServiceAdmin"));
         reputationServiceAdminAdapter.activate();
         ReputationServiceAdapter.activate();
         log.info("Service started");
         communicator().waitForShutdown();
      } catch (Exception var9) {
         log.error("failed to configure servant", var9);
      } finally {
         if (interrupted()) {
            log.fatal("ReputationService " + this.hostName + ": terminating");
            this.reputationServiceServant.shutdown();
         }

      }

      return 0;
   }
}
