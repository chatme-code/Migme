package com.projectgoth.fusion.rewardsystem;

public class ExcecutorMetrics {
   public final int queueSize;
   public final int activeThreads;
   public final int coreSize;
   public final int maxThreads;
   public final long keepAliveSecs;
   public final int largestNumThreads;

   public ExcecutorMetrics() {
      this.queueSize = 0;
      this.activeThreads = 0;
      this.coreSize = 0;
      this.maxThreads = 0;
      this.keepAliveSecs = 0L;
      this.largestNumThreads = 0;
   }

   public ExcecutorMetrics(int activeThreads, int queueSize, int coreSize, int maxThreads, long keepAliveSecs, int largestNumThreads) {
      this.activeThreads = activeThreads;
      this.queueSize = queueSize;
      this.coreSize = coreSize;
      this.maxThreads = maxThreads;
      this.keepAliveSecs = keepAliveSecs;
      this.largestNumThreads = largestNumThreads;
   }

   public String toString() {
      return this.getClass().getSimpleName() + "<" + "Active:" + this.activeThreads + " Queue:" + this.queueSize + " Core:" + this.coreSize + " Max:" + this.maxThreads + " KAlive(sec):" + this.keepAliveSecs + " Largest Threads:" + this.largestNumThreads + ">";
   }
}
