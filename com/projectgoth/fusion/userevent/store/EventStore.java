package com.projectgoth.fusion.userevent.store;

import Ice.Application;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

public class EventStore extends Application {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EventStore.class));
   public static long startTime = System.currentTimeMillis();
   public static ObjectAdapter EventStoreAdapter = null;
   public static Properties properties = null;
   private String hostName;
   private BDBEventStoreI eventStoreServant;

   public void setEventStoreServant(BDBEventStoreI userEventCacheServant) {
      this.eventStoreServant = userEventCacheServant;
   }

   public int run(String[] arg0) {
      properties = communicator().getProperties();
      log.info("Configured endpoint [" + properties.getProperty("EventStoreAdapter.Endpoints") + "]");

      try {
         this.hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var4) {
         this.hostName = "UNKNOWN";
      }

      log.debug("Initialising EventStore interface");
      EventStoreAdapter = communicator().createObjectAdapter("EventStoreAdapter");
      EventStoreAdapter.add(this.eventStoreServant, Util.stringToIdentity("EventStore"));
      log.debug("Initialising EventStoreAdmin interface");
      ObjectAdapter EventStoreAdminAdapter = communicator().createObjectAdapter("EventStoreAdminAdapter");
      EventStoreAdminI EventStoreAdmin = new EventStoreAdminI(this.eventStoreServant);
      EventStoreAdminAdapter.add(EventStoreAdmin, Util.stringToIdentity("EventStoreAdmin"));
      EventStoreAdminAdapter.activate();
      EventStoreAdapter.activate();
      log.info("Service started");
      communicator().waitForShutdown();
      if (interrupted()) {
         log.fatal("EventStore " + this.hostName + ": terminating");
         this.eventStoreServant.shutdownStore();
      }

      return 0;
   }
}
