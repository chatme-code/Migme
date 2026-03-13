package com.projectgoth.fusion.sessioncache;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.slice.ReputationServicePrx;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class SessionCache extends Application {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SessionCache.class));
   public static ObjectAdapter sessionCacheAdapter = null;
   public static Properties properties = null;
   public static String hostName = null;
   public static long startTime = System.currentTimeMillis();
   private SessionCacheI sessionCacheServant;
   private IcePrxFinder icePrxFinder;

   public SessionCache() {
      log.debug(this.getClass() + " INSTANTIATED!!");
   }

   @Required
   public void setSessionCacheServant(SessionCacheI sessionCacheServant) {
      this.sessionCacheServant = sessionCacheServant;
   }

   private void configureServant(Properties properties) throws Exception {
      this.icePrxFinder = new IcePrxFinder(communicator(), properties);
      ReputationServicePrx reputationServicePrx = this.icePrxFinder.waitForReputationServiceProxy();
      this.sessionCacheServant.setReputationServicePrx(reputationServicePrx);
      this.sessionCacheServant.createArchiveThread();
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();
      log.info("Configured endpoint [" + properties.getProperty("SessionCacheAdapter.Endpoints") + "]");

      try {
         hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var10) {
         hostName = "UNKNOWN";
      }

      try {
         this.configureServant(properties);
         log.debug("Initialising SessionCache interface");
         sessionCacheAdapter = communicator().createObjectAdapter("SessionCacheAdapter");
         sessionCacheAdapter.add(this.sessionCacheServant, Util.stringToIdentity("SessionCache"));
         IceStats.getInstance().setIceObjects(communicator(), sessionCacheAdapter, (ConfigurableExecutor)null);
         log.debug("Initialising SessionCacheAdmin interface");
         ObjectAdapter SessionCacheAdminAdapter = communicator().createObjectAdapter("SessionCacheAdminAdapter");
         SessionCacheAdminI SessionCacheAdmin = new SessionCacheAdminI(this.sessionCacheServant);
         SessionCacheAdminAdapter.add(SessionCacheAdmin, Util.stringToIdentity("SessionCacheAdmin"));
         SessionCacheAdminAdapter.activate();
         sessionCacheAdapter.activate();
         log.info("Service started");
         communicator().waitForShutdown();
      } catch (Exception var9) {
         log.error("failed to configure servant", var9);
      } finally {
         if (interrupted()) {
            log.fatal("SessionCache " + hostName + ": terminating");
            this.sessionCacheServant.terminating();
         }

      }

      return 0;
   }
}
