package com.projectgoth.fusion.messageswitchboard;

import Ice.Application;
import Ice.LocalException;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.MessageSwitchboardAdminPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardPrxHelper;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class MessageSwitchboard extends Application {
   private static final LogFilter log;
   private static final String APP_NAME = "MessageSwitchboard";
   public static ObjectAdapter msgSwitchboardAdapter;
   public static Properties properties;
   public static long startTime;
   private String hostName;
   private MessageSwitchboardI msgSwitchboardServant;

   @Required
   public void setMessageSwitchboardServant(MessageSwitchboardI msgSwitchboardServant) {
      log.debug("Setting servant");
      this.msgSwitchboardServant = msgSwitchboardServant;
      log.debug("Set servant ok");
   }

   private void configureServant(Properties properties) throws Exception {
      IcePrxFinder icePrxFinder = new IcePrxFinder(communicator(), MessageSwitchboard.properties);
      this.msgSwitchboardServant.setIcePrxFinder(icePrxFinder);
      log.debug("Configured servant ok");
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var13) {
         this.hostName = "UNKNOWN";
      }

      byte var7;
      try {
         this.configureServant(properties);
         this.msgSwitchboardServant.initialize(communicator());
         log.debug("Initialized servant");
         log.info("Configured endpoint [" + properties.getProperty("MessageSwitchboardAdapter.Endpoints") + "]");
         log.debug("Initialising MessageSwitchboardAdapter interface");
         msgSwitchboardAdapter = communicator().createObjectAdapter("MessageSwitchboardAdapter");
         ObjectPrx basePrx = msgSwitchboardAdapter.add(this.msgSwitchboardServant, Util.stringToIdentity("MessageSwitchboard"));
         MessageSwitchboardPrx messageSwitchboardPrx = MessageSwitchboardPrxHelper.uncheckedCast(basePrx);
         MessageSwitchboardAdminPrx adminPrx = null;
         msgSwitchboardAdapter.activate();
         String uniqueID = messageSwitchboardPrx.ice_getEndpoints()[0].toString();
         RegistryPrx registryPrx = this.getRegistryPrx();
         if (registryPrx != null) {
            registryPrx.registerMessageSwitchboard(uniqueID, messageSwitchboardPrx, (MessageSwitchboardAdminPrx)adminPrx);
            log.info("Service started");
            communicator().waitForShutdown();
            return 0;
         }

         var7 = 1;
      } catch (Exception var14) {
         log.error("failed to configure servant", var14);
         return 0;
      } finally {
         if (interrupted()) {
            log.fatal("MessageSwitchboard " + this.hostName + ": terminating");
            this.msgSwitchboardServant.shutdown();
         }

      }

      return var7;
   }

   private RegistryPrx getRegistryPrx() {
      String registryStringifiedProxy = communicator().getProperties().getProperty("RegistryProxy");
      ObjectPrx basePrx = communicator().stringToProxy(registryStringifiedProxy);
      log.info("Connecting to [" + basePrx + "]");
      RegistryPrx registryPrx = null;

      try {
         registryPrx = RegistryPrxHelper.checkedCast(basePrx);
      } catch (LocalException var5) {
         log.fatal("Connection to [" + registryPrx + "] failed. ", var5);
         return null;
      }

      if (registryPrx == null) {
         log.fatal("Connection to [" + registryPrx + "] failed");
         return null;
      } else {
         return registryPrx;
      }
   }

   static {
      log = new LogFilter(Logger.getLogger(MessageSwitchboard.class), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
      msgSwitchboardAdapter = null;
      properties = null;
      startTime = System.currentTimeMillis();
   }
}
