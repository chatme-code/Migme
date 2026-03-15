package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.common.RequestCounter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class InstrumentedThreadPool implements Executor {
   private ThreadPoolExecutor pool;
   private ScheduledThreadPoolExecutor scheduler;
   private RequestCounter requestCounter;
   private int maxQueueSize;

   public InstrumentedThreadPool() {
      this.pool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
      this.scheduler = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
      this.requestCounter = new RequestCounter();
      this.maxQueueSize = Integer.MAX_VALUE;
   }

   public InstrumentedThreadPool(int nThreads, int maxQueueSize) {
      this.pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(nThreads);
      this.scheduler = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
      this.requestCounter = new RequestCounter();
      this.maxQueueSize = maxQueueSize;
   }

   public int getActiveCount() {
      return this.pool.getActiveCount();
   }

   public int getCorePoolSize() {
      return this.pool.getCorePoolSize();
   }

   public int getLargestPoolSize() {
      return this.pool.getLargestPoolSize();
   }

   public int getPoolSize() {
      return this.pool.getPoolSize();
   }

   public int getQueueSize() {
      return this.pool.getQueue().size();
   }

   public BlockingQueue<Runnable> getQueue() {
      return this.pool.getQueue();
   }

   public InstrumentedThreadPool.Task getEldestTaskOnQueue() {
      return (InstrumentedThreadPool.Task)this.pool.getQueue().peek();
   }

   public long getNumRequests() {
      return (long)this.requestCounter.getNumRequests();
   }

   public float getRequestsPerSecond() {
      return this.requestCounter.getRequestsPerSecond();
   }

   public float getMaxRequestsPerSecond() {
      return this.requestCounter.getMaxRequestsPerSecond();
   }

   public long getCompletedTaskCount() {
      return this.pool.getCompletedTaskCount();
   }

   public void execute(Runnable command) {
      if (this.pool.getQueue().size() >= this.maxQueueSize) {
         throw new RejectedExecutionException();
      } else {
         this.requestCounter.add();
         this.pool.execute(new InstrumentedThreadPool.Task(command));
      }
   }

   public void schedule(final Runnable command, long delay, TimeUnit unit) {
      if (this.pool.getQueue().size() >= this.maxQueueSize) {
         throw new RejectedExecutionException();
      } else {
         this.requestCounter.add();
         this.scheduler.schedule(new Runnable() {
            public void run() {
               InstrumentedThreadPool.this.pool.execute(new InstrumentedThreadPool.Task(command));
            }
         }, delay, unit);
      }
   }

   public void shutdown() {
      this.scheduler.shutdown();
      this.pool.shutdown();
   }

   public List<InstrumentedThreadPool.Task> shutdownNow() {
      List<InstrumentedThreadPool.Task> unexecutedTasks = new LinkedList();
      Iterator i$ = this.scheduler.shutdownNow().iterator();

      Runnable command;
      while(i$.hasNext()) {
         command = (Runnable)i$.next();
         unexecutedTasks.add(new InstrumentedThreadPool.Task(command));
      }

      i$ = this.pool.shutdownNow().iterator();

      while(i$.hasNext()) {
         command = (Runnable)i$.next();
         unexecutedTasks.add((InstrumentedThreadPool.Task)command);
      }

      return unexecutedTasks;
   }

   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return this.pool.awaitTermination(timeout, unit);
   }

   public static final class Task implements Runnable {
      private final Runnable command;
      private final long timeCreated;

      public Task(Runnable command) {
         this.command = command;
         this.timeCreated = System.currentTimeMillis();
      }

      public long getTimeCreated() {
         return this.timeCreated;
      }

      public void run() {
         this.command.run();
      }
   }
}
