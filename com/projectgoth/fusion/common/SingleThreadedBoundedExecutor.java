package com.projectgoth.fusion.common;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadedBoundedExecutor extends AbstractExecutorService {
   public static final int DEFAULT_MAX_TASK_SIZE = 10;
   private final ThreadPoolExecutor executor;
   private final int maxTaskSize;
   private AtomicInteger currentInvokerCount;

   public SingleThreadedBoundedExecutor() {
      this(10);
   }

   public SingleThreadedBoundedExecutor(int maxTaskSize) {
      this.currentInvokerCount = new AtomicInteger(0);
      this.maxTaskSize = maxTaskSize;
      this.executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue());
   }

   public int getMaxTaskSize() {
      return this.maxTaskSize;
   }

   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return this.executor.awaitTermination(timeout, unit);
   }

   public boolean isShutdown() {
      return this.executor.isShutdown();
   }

   public boolean isTerminated() {
      return this.executor.isTerminated();
   }

   public void shutdown() {
      this.executor.shutdown();
   }

   public List<Runnable> shutdownNow() {
      return this.executor.shutdownNow();
   }

   public void execute(final Runnable command) {
      int waitingTaskSize = this.executor.getQueue().size();
      int maxTaskSize = this.getMaxTaskSize();
      if (waitingTaskSize + 1 > maxTaskSize) {
         throw new RejectedExecutionException("Pending task full. number of waiting tasks:" + waitingTaskSize + ".Max task size:" + maxTaskSize);
      } else {
         int currentSize = this.currentInvokerCount.incrementAndGet();
         if (currentSize <= maxTaskSize) {
            this.executor.execute(new Runnable() {
               public void run() {
                  try {
                     command.run();
                  } finally {
                     SingleThreadedBoundedExecutor.this.currentInvokerCount.decrementAndGet();
                  }

               }
            });
         } else {
            this.currentInvokerCount.decrementAndGet();
            throw new RejectedExecutionException("Too many task sender:" + currentSize + ".Max task size:" + maxTaskSize);
         }
      }
   }

   public int getCurrentInvokerCount() {
      return this.currentInvokerCount.intValue();
   }
}
