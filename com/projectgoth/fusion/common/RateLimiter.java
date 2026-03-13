package com.projectgoth.fusion.common;

public class RateLimiter {
   private long interval;
   private long maxHits;
   private long startTime;
   private long hits;

   public RateLimiter(long maxHits, long interval) {
      this.maxHits = maxHits;
      this.interval = interval;
   }

   public synchronized boolean hit() {
      if (System.currentTimeMillis() - this.startTime > this.interval) {
         this.startTime = System.currentTimeMillis();
         this.hits = 0L;
      }

      return ++this.hits <= this.maxHits;
   }
}
