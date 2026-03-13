package com.projectgoth.fusion.userevent;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import com.projectgoth.fusion.userevent.store.StoreGeneratorEvent;
import com.projectgoth.fusion.userevent.store.StoreGeneratorEventDA;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class DumpTranslateGeneratorEvents {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DumpTranslateGeneratorEvents.class));
   private Environment environment;
   private EntityStore generatedStore;
   private StoreGeneratorEventDA storeGeneratorEventDA;
   private EventTextTranslator translator = new EventTextTranslator();
   private static Date TWO_WEEKS_AGO = DateTimeUtils.minusDays(new Date(), 14);

   public DumpTranslateGeneratorEvents(String path) {
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
         log.info("environment stats " + this.environment.getStats((StatsConfig)null).toString());
         this.storeGeneratorEventDA = new StoreGeneratorEventDA(this.generatedStore);
         log.info("Store opened successfully");
         log.info("Maximum cache memory: " + (int)((double)environmentConfig.getCachePercent() / 100.0D * (double)Runtime.getRuntime().maxMemory() / 1000000.0D) + " MB");
      } catch (DatabaseException var4) {
         log.fatal("Error opening environment and store, exiting... ", var4);
      }

   }

   public void dump() {
      EntityCursor cursor = null;

      try {
         log.info("getting cursor...");
         cursor = this.storeGeneratorEventDA.getPrimaryIndex().entities();
         StoreGeneratorEvent storeEvent = null;

         while(true) {
            do {
               if ((storeEvent = (StoreGeneratorEvent)cursor.next()) == null) {
                  return;
               }
            } while(storeEvent.getEvents() == null);

            Iterator i$ = storeEvent.getEvents().iterator();

            while(i$.hasNext()) {
               UserEvent event = (UserEvent)i$.next();
               String clazz = event.getClass().toString();
               if (event.getTimestamp() > TWO_WEEKS_AGO.getTime()) {
                  System.out.println(clazz.substring(46) + "," + new Date(event.getTimestamp()) + "," + event.getGeneratingUsername() + "," + this.translator.translate(event.toIceEvent(), ClientType.MIDP2, (String)null));
               }
            }
         }
      } catch (Exception var15) {
         log.error("failed to dump events", var15);
      } finally {
         try {
            cursor.close();
         } catch (DatabaseException var14) {
            log.error("failed to close cursor", var14);
         }

      }

   }

   public void closeDb() {
      log.error("closing db");
      if (this.environment != null) {
         try {
            this.environment.close();
         } catch (DatabaseException var2) {
            log.fatal("Error closing Environment, exiting... ", var2);
            return;
         }
      }

   }

   public static void main(String[] args) {
      DOMConfigurator.configureAndWatch("log4j.xml");
      if (args.length < 1) {
         System.err.println("Usage: DumpEvents <data directory>");
         System.exit(1);
      }

      DumpTranslateGeneratorEvents dumpEvents = new DumpTranslateGeneratorEvents(args[0]);

      try {
         dumpEvents.dump();
      } catch (Throwable var7) {
         log.error("problem dumping events ", var7);
      } finally {
         dumpEvents.closeDb();
      }

   }
}
