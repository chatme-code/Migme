package com.projectgoth.fusion.eventqueue;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.RequestAndRateLongCounter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.eventqueue.queues.EventQueueClient;
import com.projectgoth.fusion.slice._EventQueueWorkerDisp;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class WorkerI extends _EventQueueWorkerDisp {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(WorkerI.class));
   private ExecutorService executor;
   private int ID;
   private boolean isRunning;
   private static final long serialVersionUID = 1L;
   private RequestAndRateLongCounter totalCounter;
   private ConcurrentHashMap<Enums.EventTypeEnum, RequestAndRateLongCounter> eventCounters;
   private long queueSize;
   private long maxQueueSize;
   private long lastQueueCheckTS;
   private int poolSize;
   private CountDownLatch taskStartSignal;

   public WorkerI(int workerID) {
      this.ID = workerID;
      this.isRunning = false;
      this.taskStartSignal = new CountDownLatch(1);
      this.totalCounter = new RequestAndRateLongCounter(5);
      this.eventCounters = new ConcurrentHashMap();
      Iterator i$ = Enums.EventTypeEnum.getAllTypes().iterator();

      while(i$.hasNext()) {
         Enums.EventTypeEnum e = (Enums.EventTypeEnum)i$.next();
         this.eventCounters.put(e, new RequestAndRateLongCounter(5));
      }

      this.queueSize = 0L;
      this.maxQueueSize = 0L;
      this.lastQueueCheckTS = 0L;
      this.poolSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EventQueueSettings.WORKER_THREAD_POOL_SIZE);
      this.executor = Executors.newFixedThreadPool(this.poolSize);

      for(int i = 0; i < this.poolSize; ++i) {
         int threadID = workerID * 10000 + i;
         this.executor.execute(new WorkerI.WorkerTask(threadID));
      }

      this.taskStartSignal.countDown();
      this.setRunning(true);
      log.info(this.poolSize + " WorkerTask threads added to executor service");
   }

   public long getQueueSize() {
      return this.queueSize;
   }

   public long getMaxQueueSize() {
      return this.maxQueueSize;
   }

   public RequestAndRateLongCounter getTotalCounter() {
      return this.totalCounter;
   }

   public RequestAndRateLongCounter getEventCounter(Enums.EventTypeEnum type) {
      return (RequestAndRateLongCounter)this.eventCounters.get(type);
   }

   private synchronized void setRunning(boolean b) {
      this.isRunning = b;
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public void shutdownAllWorkerThreads() throws Exception {
      this.setRunning(false);
      this.executor.shutdown();
      this.executor.awaitTermination((long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EventQueueSettings.SHUTDOWN_WAIT_IN_SECONDS), TimeUnit.SECONDS);
      if (!this.executor.isTerminated()) {
         this.executor.shutdownNow();
      }

   }

   private class WorkerTask implements Runnable {
      int threadID;
      EventQueueClient client;

      public WorkerTask(int threadID) {
         this.threadID = threadID;
      }

      public void run() {
         int inProcess;
         try {
            WorkerI.log.info("WorkerTask thread [" + this.threadID + "]: thread starting up.");
            WorkerI.this.taskStartSignal.await();
            WorkerI.log.info("WorkerTask thread [" + this.threadID + "]: thread started.");
            this.client = EventQueue.getClient();
            if (this.client == null) {
               throw new Exception("WorkerTask thread [" + this.threadID + "]: Unable to get eventqueue client.");
            }

            this.client.setProcessingQueueID(this.threadID);

            while(WorkerI.this.isRunning) {
               WorkerI.log.info(String.format("WorkerTask thread [" + this.threadID + "]: waiting for events from queue"));

               for(inProcess = this.client.numberOfEventsInProcess(); inProcess > 0; inProcess = this.client.numberOfEventsInProcess()) {
                  WorkerI.log.info(String.format("WorkerTask thread [" + this.threadID + "]: Found %d events in processing queue. requeuing", inProcess));
                  this.client.requeueEventInProcess();
               }

               long totalPending = (long)this.client.numberOfEventsPending();
               WorkerI.log.info(String.format("WorkerTask thread [" + this.threadID + "]: There are %d events pending in queue", totalPending));
               if (totalPending > (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EventQueueSettings.MAXIMUM_WORKER_QUEUE_SIZE)) {
                  WorkerI.log.info("Attempting to auto purge events from queue");
                  int cleared = this.client.flushPendingQueue();
                  if (cleared > 0) {
                     WorkerI.log.info(String.format("WorkerTask thread [" + this.threadID + "]: Successfully cleared events from queue", cleared));
                  } else {
                     WorkerI.log.error("WorkerTask thread [" + this.threadID + "]: Unable to clear pending events.");
                  }
               }

               Event event = this.client.getForProcessing();
               if (System.currentTimeMillis() - WorkerI.this.lastQueueCheckTS > (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EventQueueSettings.QUEUE_CHECK_INTERVAL_IN_SECONDS) * 1000L) {
                  WorkerI.this.queueSize = (long)this.client.numberOfEventsPending();
                  if (WorkerI.this.queueSize > WorkerI.this.maxQueueSize) {
                     WorkerI.this.maxQueueSize = WorkerI.this.queueSize;
                  }
               }

               if (event == null) {
                  WorkerI.log.info(String.format("WorkerTask thread [" + this.threadID + "]: Received null message. Ignoring."));
               } else {
                  WorkerI.log.info(String.format("WorkerTask thread [" + this.threadID + "]: Processing event from queue: [%s] [%s] [%d]", event.type.description, event.eventSubject, event.timestamp));
                  if (WorkerI.this.eventCounters.containsKey(event.type)) {
                     WorkerI.this.totalCounter.add();
                     ((RequestAndRateLongCounter)WorkerI.this.eventCounters.get(event.type)).add();
                  }

                  if (event.execute()) {
                     WorkerI.log.info("WorkerTask thread [" + this.threadID + "]: Done. Acknowledging message.");
                     this.client.processingCompleted(true);
                     WorkerI.log.info("WorkerTask thread [" + this.threadID + "]: Acknowledged");
                  } else if (event.retryOnFailure()) {
                     WorkerI.log.error(String.format("WorkerTask thread [" + this.threadID + "]: Failed to execute event. requeueing it for retry [%s] [%s] [%d]", event.type.description, event.eventSubject, event.timestamp));
                     this.client.requeueEventInProcess();
                     WorkerI.log.info("WorkerTask thread [" + this.threadID + "]: ReQueued");
                  } else {
                     WorkerI.log.error(String.format("WorkerTask thread [" + this.threadID + "]: Failed to execute event. dropping it [%s] [%s] [%d]", event.type.description, event.eventSubject, event.timestamp));
                     this.client.processingCompleted(false);
                     WorkerI.log.info("WorkerTask thread [" + this.threadID + "]: Ignored");
                  }
               }

               if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EventQueueSettings.PERSISTENT_REDIS_CONNECTION_ENABLED)) {
                  this.client.disconnectClient();
                  this.client = EventQueue.getClient();
               }
            }
         } catch (Exception var27) {
            WorkerI.log.error("WorkerTask thread [" + this.threadID + "]: terminated due to exception - " + var27.getMessage(), var27);
         } finally {
            if (this.client != null) {
               try {
                  this.client.disconnectClient();
               } catch (Exception var24) {
                  this.client = null;
               }
            }

         }

         if (WorkerI.this.isRunning) {
            try {
               inProcess = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EventQueueSettings.RETRY_INTERVAL_IN_SECONDS);
               WorkerI.log.info("WorkerTask thread [" + this.threadID + "]: Attempting another restart in " + inProcess + " seconds");
               Thread.sleep((long)(inProcess * 1000));
            } catch (InterruptedException var25) {
            } finally {
               WorkerI.this.executor.execute(this);
            }
         }

      }
   }
}
