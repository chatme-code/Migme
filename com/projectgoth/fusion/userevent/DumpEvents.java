package com.projectgoth.fusion.userevent;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import com.projectgoth.fusion.userevent.store.StoreUserEvent;
import com.projectgoth.fusion.userevent.store.StoreUserEventDA;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import java.io.File;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.util.StringUtils;

public class DumpEvents {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DumpEvents.class));
   private Environment environment;
   private EntityStore feedStore;
   private StoreUserEventDA storeUserEventDA;

   public DumpEvents(String path) {
      this.setup(new File(path));
   }

   private void setup(File environmentHome) {
      try {
         EnvironmentConfig environmentConfig = new EnvironmentConfig();
         StoreConfig storeConfig = new StoreConfig();
         environmentConfig.setAllowCreate(false);
         environmentConfig.setCachePercent(20);
         environmentConfig.setReadOnly(true);
         storeConfig.setAllowCreate(false);
         storeConfig.setReadOnly(true);
         this.environment = new Environment(environmentHome, environmentConfig);
         this.feedStore = new EntityStore(this.environment, "FeedStore", storeConfig);
         this.storeUserEventDA = new StoreUserEventDA(this.feedStore);
         this.log.info("Store opened successfully");
         this.log.info("Maximum cache memory: " + (int)((double)environmentConfig.getCachePercent() / 100.0D * (double)Runtime.getRuntime().maxMemory() / 1000000.0D) + " MB");
      } catch (DatabaseException var4) {
         this.log.fatal("Error opening environment and store, exiting... ", var4);
      }

   }

   public void dump(String filterUsername) {
      EntityCursor cursor = null;

      try {
         this.log.info("getting cursor...");
         StoreUserEvent storeEvent;
         Iterator i$;
         UserEvent event;
         if (StringUtils.hasLength(filterUsername) && !"*".equals(filterUsername)) {
            storeEvent = (StoreUserEvent)this.storeUserEventDA.getPrimaryIndex().get(filterUsername);
            System.out.println("username [" + storeEvent.getUsername() + "] mask [" + storeEvent.getReceivingMask());
            i$ = storeEvent.getEvents().iterator();

            while(i$.hasNext()) {
               event = (UserEvent)i$.next();
               System.out.println("username [" + storeEvent.getUsername() + "] " + event);
            }
         } else {
            cursor = this.storeUserEventDA.getPrimaryIndex().entities();
            this.log.info("moving cursor to last event");
            cursor.last();
            storeEvent = null;

            while((storeEvent = (StoreUserEvent)cursor.prev()) != null) {
               i$ = storeEvent.getEvents().iterator();

               while(i$.hasNext()) {
                  event = (UserEvent)i$.next();
                  System.out.println("username [" + storeEvent.getUsername() + "] " + event);
               }
            }
         }
      } catch (Exception var15) {
         this.log.error("failed to dump events", var15);
      } finally {
         try {
            if (cursor != null) {
               cursor.close();
            }
         } catch (DatabaseException var14) {
            this.log.error("failed to close cursor", var14);
         }

      }

   }

   public void closeDb() {
      this.log.error("closing db");
      if (this.environment != null) {
         try {
            this.environment.close();
         } catch (DatabaseException var2) {
            this.log.fatal("Error closing Environment, exiting... ", var2);
            return;
         }
      }

   }

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      if (args.length < 1) {
         System.err.println("Usage: DumpEvents <data directory> <optional filter user>");
         System.exit(1);
      }

      DumpEvents dumpEvents = new DumpEvents(args[0]);

      try {
         dumpEvents.dump(args[1]);
      } finally {
         dumpEvents.closeDb();
      }

   }
}
