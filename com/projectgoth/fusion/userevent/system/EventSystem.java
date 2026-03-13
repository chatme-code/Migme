package com.projectgoth.fusion.userevent.system;

import Ice.Application;
import Ice.LocalException;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

public class EventSystem extends Application {
   private static Logger log = Logger.getLogger(EventSystem.class);
   public static ObjectAdapter EventSystemAdapter = null;
   public static Properties properties = null;
   public static long startTime = System.currentTimeMillis();
   private RegistryPrx registryProxy = null;
   private String hostName;
   private EventSystemI eventSystemServant;

   public void setEventSystemServant(EventSystemI eventSystemServant) {
      this.eventSystemServant = eventSystemServant;
   }

   private void findRegistryProxy() {
      String registryStringifiedProxy = communicator().getProperties().getProperty("RegistryProxy");
      ObjectPrx basePrx = communicator().stringToProxy(registryStringifiedProxy);
      log.info("Connecting to [" + basePrx + "]");

      try {
         this.registryProxy = RegistryPrxHelper.checkedCast(basePrx);
      } catch (LocalException var4) {
         log.error("Registry " + this.hostName + ": Connection to [" + this.registryProxy + "] failed. ", var4);
         return;
      }

      if (this.registryProxy == null) {
         log.error("Registry " + this.hostName + ": Connection to [" + this.registryProxy + "] failed");
      }
   }

   public int run(String[] arg0) {
      this.eventSystemServant.createProxies(communicator());
      properties = communicator().getProperties();
      log.info("Configured endpoint [" + properties.getProperty("EventSystemAdapter.Endpoints") + "]");

      try {
         this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var6) {
         this.hostName = "UNKNOWN";
      }

      log.debug("Initialising EventSystem interface");
      EventSystemAdapter = communicator().createObjectAdapter("EventSystemAdapter");
      EventSystemAdapter.add(this.eventSystemServant, Util.stringToIdentity("EventSystem"));
      log.debug("Initialising EventSystemAdmin interface");
      ObjectAdapter EventSystemAdminAdapter = communicator().createObjectAdapter("EventSystemAdminAdapter");
      EventSystemAdminI EventSystemAdmin = new EventSystemAdminI(this.eventSystemServant);
      EventSystemAdminAdapter.add(EventSystemAdmin, Util.stringToIdentity("EventSystemAdmin"));
      EventSystemAdminAdapter.activate();
      this.findRegistryProxy();

      while(this.registryProxy == null) {
         try {
            log.warn("Still waiting for registry proxy to become available...");
            Thread.sleep(1000L);
            this.findRegistryProxy();
         } catch (InterruptedException var5) {
         }
      }

      if (this.registryProxy == null) {
         return -1;
      } else {
         this.eventSystemServant.setRegistryProxy(this.registryProxy);
         EventSystemAdapter.activate();
         log.info("Service started");
         communicator().waitForShutdown();
         if (interrupted()) {
            log.fatal("EventSystem " + this.hostName + ": terminating");
            this.eventSystemServant.shutdown();
         }

         return 0;
      }
   }
}
