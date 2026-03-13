package com.projectgoth.fusion.eventqueue;

import Ice.Application;
import Ice.LocalException;
import Ice.ObjectAdapter;
import Ice.Properties;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;

public class Worker extends Application {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Worker.class));
   private int ID;
   public static String WORKER_ADMIN_ICE_APP_NAME = "EventQueueWorkerAdmin";
   public static String WORKER_ICE_APP_NAME = "EventQueueWorker";
   public static ObjectAdapter workerAdapter = null;
   public static ObjectAdapter workerAdminAdapter = null;
   public static Properties properties = null;
   public static String hostName;
   public static long startTime = System.currentTimeMillis();
   private WorkerI worker;
   private WorkerAdminI workerAdmin;
   private static final String CONFIG_FILE = "EventQueue.cfg";

   public static void main(String[] args) {
      String configFile = args.length > 0 ? args[0] : "EventQueue.cfg";
      Worker worker = new Worker();
      int status = worker.main("EventQueue", args, configFile);
      System.exit(status);
   }

   public boolean isRunning() {
      return this.worker != null && this.worker.isRunning();
   }

   public synchronized void shutdown() {
      try {
         if (this.isRunning()) {
            this.worker.shutdownAllWorkerThreads();
         }
      } catch (Exception var2) {
         log.error("Exception caught while shutting down worker threads: " + var2.getMessage(), var2);
      }

   }

   public int run(String[] args) {
      int returnStatus = 0;
      log.info("Starting worker application");
      shutdownOnInterrupt();
      log.info(String.format("Starting eventqueue worker ..."));

      try {
         log.info("Retrieving configuration properties from Communicator object");
         properties = communicator().getProperties();
         log.info("Configured endpoint [" + properties.getProperty("EventQueueWorkerAdapter.Endpoints") + "]");
         log.info("Configured endpoint [" + properties.getProperty("EventQueueWorkerAdminAdapter.Endpoints") + "]");
         log.info("Configured worked ID [" + properties.getProperty("EventQueueWorkerAdapter.WorkerID") + "]");

         int workerID;
         try {
            workerID = Integer.parseInt(properties.getProperty("EventQueueWorkerAdapter.WorkerID"));
         } catch (Exception var6) {
            log.error(String.format("Unable to determine workerID from arguments: %s. Please check your configuration file. Terminating worker.", var6.getMessage()));
            return 1;
         }

         try {
            log.info("Getting hostName");
            hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
         } catch (UnknownHostException var5) {
            log.error("Unable to determine hostname for Worker", var5);
            hostName = "UNKNOWN";
         }

         log.info(String.format("Hostname found [%s]", hostName));
         log.info("Initialising EventQueueWorker interface");
         workerAdapter = communicator().createObjectAdapter(WORKER_ICE_APP_NAME + "Adapter");
         this.worker = new WorkerI(workerID);
         workerAdapter.add(this.worker, Util.stringToIdentity(WORKER_ICE_APP_NAME));
         log.info("Initialising WorkerAdmin interface");
         workerAdminAdapter = communicator().createObjectAdapter(WORKER_ADMIN_ICE_APP_NAME + "Adapter");
         this.workerAdmin = new WorkerAdminI(this.worker);
         workerAdminAdapter.add(this.workerAdmin, Util.stringToIdentity(WORKER_ADMIN_ICE_APP_NAME));
         workerAdminAdapter.activate();
         workerAdapter.activate();
         log.info("Service started");
         setInterruptHook(new Worker.ShutdownHook());
         communicator().waitForShutdown();
      } catch (LocalException var7) {
         var7.printStackTrace();
         returnStatus = 1;
      } catch (Exception var8) {
         System.err.println(var8.getMessage());
         returnStatus = 1;
      }

      return returnStatus;
   }

   private class ShutdownHook extends Thread {
      private ShutdownHook() {
      }

      public void run() {
         Worker.this.shutdown();
      }

      // $FF: synthetic method
      ShutdownHook(Object x1) {
         this();
      }
   }
}
