package com.projectgoth.fusion.userevent;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import com.projectgoth.fusion.userevent.store.StoreGeneratorEvent;
import com.projectgoth.fusion.userevent.store.StoreGeneratorEventDA;
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

public class DumpGeneratorEvents {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DumpGeneratorEvents.class));
   private Environment environment;
   private EntityStore generatedStore;
   private StoreGeneratorEventDA storeGeneratorEventDA;

   public DumpGeneratorEvents(String path) {
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
         this.generatedStore = new EntityStore(this.environment, "GeneratedStore", storeConfig);
         this.storeGeneratorEventDA = new StoreGeneratorEventDA(this.generatedStore);
         this.log.info("Store opened successfully");
         this.log.info("Maximum cache memory: " + (int)((double)environmentConfig.getCachePercent() / 100.0D * (double)Runtime.getRuntime().maxMemory() / 1000000.0D) + " MB");
      } catch (DatabaseException var4) {
         this.log.fatal("Error opening environment and store, exiting... ", var4);
      }

   }

   public void dump(int requestedCount) {
      EntityCursor cursor = null;

      try {
         this.log.info("getting cursor...");
         cursor = this.storeGeneratorEventDA.getPrimaryIndex().entities();
         this.log.info("moving cursor to last event");
         cursor.last();
         int count = 0;
         StoreGeneratorEvent storeEvent = null;

         while((storeEvent = (StoreGeneratorEvent)cursor.prev()) != null && count++ < requestedCount) {
            Iterator i$ = storeEvent.getEvents().iterator();

            while(i$.hasNext()) {
               UserEvent event = (UserEvent)i$.next();
               System.out.println(event);
            }
         }
      } catch (Exception var16) {
         this.log.error("failed to dump events", var16);
      } finally {
         try {
            cursor.close();
         } catch (DatabaseException var15) {
            this.log.error("failed to close cursor", var15);
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
      if (args.length < 2) {
         System.err.println("Usage: DumpEvents <data directory> <event count>");
         System.exit(1);
      }

      DumpGeneratorEvents dumpEvents = new DumpGeneratorEvents(args[0]);

      try {
         dumpEvents.dump(Integer.parseInt(args[1]));
      } finally {
         dumpEvents.closeDb();
      }

   }
}
