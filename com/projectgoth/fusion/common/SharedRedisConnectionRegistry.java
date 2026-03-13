package com.projectgoth.fusion.common;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class SharedRedisConnectionRegistry {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SharedRedisConnectionRegistry.class));
   private static SharedRedisConnectionRegistry theRegistry = new SharedRedisConnectionRegistry();
   final ConcurrentLinkedQueue<WeakReference<SharedRedisConnection>> connections = new ConcurrentLinkedQueue();
   final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
   private static final Object initLock = new Object();

   SharedRedisConnectionRegistry() {
   }

   void startMaintenanceThread(int delayInMs) {
      try {
         this.scheduler.schedule(new SharedRedisConnectionRegistry.MaintenanceThread(), (long)delayInMs, TimeUnit.MILLISECONDS);
      } catch (Exception var3) {
         log.error("Unable to shedule maintenance thread run : " + var3.getMessage(), var3);
      }

   }

   void registerConnection(SharedRedisConnection conn) {
      if (conn != null) {
         WeakReference<SharedRedisConnection> ref = new WeakReference(conn);
         this.connections.add(ref);
         log.info("SharedRedisConnection [" + conn.server + "] registered");
      } else {
         log.warn("registerConnection is called with a null SharedRedisConnection instance");
      }

   }

   public static SharedRedisConnectionRegistry getRegistry() {
      if (theRegistry != null) {
         return theRegistry;
      } else {
         synchronized(initLock) {
            if (theRegistry != null) {
               return theRegistry;
            }

            theRegistry = new SharedRedisConnectionRegistry();
            theRegistry.startMaintenanceThread(100);
         }

         return theRegistry;
      }
   }

   static void setRegistry(SharedRedisConnectionRegistry reg) {
      theRegistry = reg;
   }

   static {
      theRegistry.startMaintenanceThread(100);
   }

   class MaintenanceThread implements Runnable {
      private static final String SEPERATOR = ",";
      private static final String NEW_LINE = "\r\n";
      private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      private DecimalFormat decimalFormat = new DecimalFormat("0.00");
      private SimpleDateFormat instrumentationDateFormat = new SimpleDateFormat("yyyy-MM-dd");
      private String instrumentationLogFile;

      public MaintenanceThread() {
         String logDir = System.getProperty("log.dir") == null ? "/usr/fusion/logs" : System.getProperty("log.dir");
         String logFilename = System.getProperty("log.filename") == null ? "default" : System.getProperty("log.filename");
         this.instrumentationLogFile = logDir + File.separator + logFilename + ".redis.inst.summary.";
      }

      public void run() {
         try {
            SharedRedisConnectionRegistry.log.info("SharedRedisConnectionRegistry maintenance run. Found [" + SharedRedisConnectionRegistry.this.connections.size() + "] registered connections");
            Iterator<WeakReference<SharedRedisConnection>> iter = SharedRedisConnectionRegistry.this.connections.iterator();
            LinkedList forDeletion = new LinkedList();

            while(iter.hasNext()) {
               WeakReference<SharedRedisConnection> ref = (WeakReference)iter.next();
               SharedRedisConnection conn = (SharedRedisConnection)ref.get();
               if (conn == null) {
                  forDeletion.add(ref);
               } else {
                  this.recordMetrics(conn);
               }
            }

            SharedRedisConnectionRegistry.log.info("SharedRedisConnectionRegistry maintenance run. Found [" + forDeletion.size() + "] dead connections for removal");
            Iterator i$ = forDeletion.iterator();

            while(i$.hasNext()) {
               WeakReference<SharedRedisConnection> refx = (WeakReference)i$.next();
               SharedRedisConnectionRegistry.this.connections.remove(refx);
            }
         } catch (Exception var11) {
            SharedRedisConnectionRegistry.log.error("Unexpected exception during SharedRedisConnectionRegistry maintenance run: " + var11.getMessage(), var11);
         } finally {
            long runDelayInMs = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SHARED_REDIS_CONNECTION_MAINTENANCE_TASK_RUN_INTERVAL_IN_MS);
            SharedRedisConnectionRegistry.log.info("SharedRedisConnectionRegistry maintenance run complete. scheduling next run in [" + runDelayInMs + "] ms");
            SharedRedisConnectionRegistry.this.scheduler.schedule(SharedRedisConnectionRegistry.this.new MaintenanceThread(), runDelayInMs, TimeUnit.MILLISECONDS);
         }

      }

      private void recordMetrics(SharedRedisConnection conn) {
         try {
            List<Sampler.Summary> summaries = conn.getSampler().summarize();
            FileOutputStream out = new FileOutputStream(this.instrumentationLogFile + this.instrumentationDateFormat.format(new Date()), true);
            Iterator i$ = summaries.iterator();

            while(i$.hasNext()) {
               Sampler.Summary summary = (Sampler.Summary)i$.next();
               StringBuilder builder = new StringBuilder();
               builder.append(this.dateFormat.format(new Date())).append(",").append(summary.name).append(",").append(summary.count).append(",").append(summary.min).append(",").append(summary.median).append(",").append(summary.max).append(",").append(this.decimalFormat.format(summary.mean)).append(",").append(this.decimalFormat.format(summary.mean1Percentile)).append(",").append(this.decimalFormat.format(summary.mean5Percentile)).append(",").append(this.decimalFormat.format(summary.standardDeviation)).append("\r\n");
               out.write(builder.toString().getBytes());
            }

            out.close();
         } catch (Exception var7) {
            SharedRedisConnectionRegistry.log.error("Exception caught while recording shared redis connection metrics: " + var7.getMessage(), var7);
         }

      }
   }
}
