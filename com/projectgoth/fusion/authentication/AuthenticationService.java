package com.projectgoth.fusion.authentication;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class AuthenticationService extends Application {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AuthenticationService.class));
   private static ObjectAdapter AuthenticationServiceAdapter = null;
   private static Properties properties = null;
   public static long startTime = System.currentTimeMillis();
   private String hostName;
   private AuthenticationServiceI authenticationServiceServant;

   @Required
   public void setAuthenticationServiceServant(AuthenticationServiceI authenticationServiceServant) {
      this.authenticationServiceServant = authenticationServiceServant;
   }

   private void configureServant(Properties properties) throws Exception {
      this.authenticationServiceServant.setSurgeMailPassword(properties.getPropertyWithDefault("SurgeMailPassword", "surgemail"));
      this.authenticationServiceServant.setMinimumAuthenticationsPerIP(properties.getPropertyAsIntWithDefault("MinimumAuthenticationsPerIP", 100));
      this.authenticationServiceServant.setBruteForceIPRatio(properties.getPropertyAsIntWithDefault("BruteForceIPRatio", 25000));
      this.authenticationServiceServant.setSuspectIPRatio(properties.getPropertyAsIntWithDefault("SuspectIPRatio", 5000));
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         this.configureServant(properties);
         log.info("Configured endpoint [" + properties.getProperty("AuthenticationServiceAdapter.Endpoints") + "]");

         try {
            this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
         } catch (UnknownHostException var9) {
            this.hostName = "UNKNOWN";
         }

         log.debug("Initialising AuthenticationServiceAdapter interface");
         AuthenticationServiceAdapter = communicator().createObjectAdapter("AuthenticationServiceAdapter");
         AuthenticationServiceAdapter.add(this.authenticationServiceServant, Util.stringToIdentity("AuthenticationService"));
         IceStats.getInstance().setIceObjects(communicator(), AuthenticationServiceAdapter, (ConfigurableExecutor)null);
         log.debug("Initialising AuthenticationServiceAdmin interface");
         ObjectAdapter authenticationServiceAdminAdapter = communicator().createObjectAdapter("AuthenticationServiceAdminAdapter");
         AuthenticationServiceAdminI authenticationServiceAdmin = new AuthenticationServiceAdminI(this.authenticationServiceServant);
         authenticationServiceAdminAdapter.add(authenticationServiceAdmin, Util.stringToIdentity("AuthenticationServiceAdmin"));
         authenticationServiceAdminAdapter.activate();
         AuthenticationServiceAdapter.activate();
         log.info("Service started");
         communicator().waitForShutdown();
      } catch (Exception var10) {
         log.error("failed to configure servant", var10);
      } finally {
         if (interrupted()) {
            log.fatal("AuthenticationService " + this.hostName + ": terminating");
            this.authenticationServiceServant.shutdown();
         }

      }

      return 0;
   }
}
