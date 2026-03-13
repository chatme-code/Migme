package com.projectgoth.fusion.uns;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import com.projectgoth.fusion.stats.IceStats;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class UserNotificationService extends Application {
   private static Logger log = Logger.getLogger(UserNotificationService.class);
   public static ObjectAdapter UserNotificationServiceAdapter = null;
   public static Properties properties = null;
   public static long startTime = System.currentTimeMillis();
   private String hostName;
   private UserNotificationServiceI userNotificationServiceServant;
   private String registryProxyLocation;
   private RegistryPrx registryProxy;

   @Required
   public void setUserNotificationServiceServant(UserNotificationServiceI userNotificationlServiceServant) {
      this.userNotificationServiceServant = userNotificationlServiceServant;
   }

   private synchronized RegistryPrx findUserNotificationProxy() {
      synchronized(this.registryProxyLocation) {
         try {
            if (this.registryProxy == null) {
               ObjectPrx basePrx = communicator().stringToProxy(this.registryProxyLocation);
               if (basePrx == null) {
                  throw new Exception("communicator().stringToProxy() returned null");
               }

               this.registryProxy = RegistryPrxHelper.checkedCast(basePrx);
               if (this.registryProxy == null) {
                  throw new Exception("RegistryPrxHelper.checkedCast() returned null");
               }
            }
         } catch (Exception var4) {
            log.warn("failed to locate Registry at endpoint(s) " + this.registryProxyLocation + "]", var4);
            this.registryProxy = null;
         }

         return this.registryProxy;
      }
   }

   private void configureServant(Properties properties) throws Exception {
      this.registryProxyLocation = properties.getProperty("RegistryProxy");
      this.userNotificationServiceServant.setRegistryProxy(this.findUserNotificationProxy());
      this.userNotificationServiceServant.initializeQueues();
      log.info("found registry [" + this.registryProxy + "]");
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         this.configureServant(properties);
         log.info("Configured endpoint [" + properties.getProperty("UserNotificationServiceAdapter.Endpoints") + "]");

         try {
            this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
         } catch (UnknownHostException var9) {
            this.hostName = "UNKNOWN";
         }

         log.debug("Initialising UserNotificationService interface");
         UserNotificationServiceAdapter = communicator().createObjectAdapter("UserNotificationServiceAdapter");
         UserNotificationServiceAdapter.add(this.userNotificationServiceServant, Util.stringToIdentity("UserNotificationService"));
         IceStats.getInstance().setIceObjects(communicator(), UserNotificationServiceAdapter, (ConfigurableExecutor)null);
         log.debug("Initialising UserNotificationServiceAdmin interface");
         ObjectAdapter userNotificationServiceAdminAdapter = communicator().createObjectAdapter("UserNotificationServiceAdminAdapter");
         UserNotificationServiceAdminI blueLabelServiceAdmin = new UserNotificationServiceAdminI(this.userNotificationServiceServant);
         userNotificationServiceAdminAdapter.add(blueLabelServiceAdmin, Util.stringToIdentity("UserNotificationServiceAdmin"));
         userNotificationServiceAdminAdapter.activate();
         UserNotificationServiceAdapter.activate();
         log.info("Service started");
         communicator().waitForShutdown();
      } catch (Exception var10) {
         log.error("problem initializing UNS?", var10);
      } finally {
         if (interrupted()) {
            log.fatal("UserNotificationService " + this.hostName + ": terminating");
            this.userNotificationServiceServant.shutdown();
         }

      }

      return 0;
   }
}
