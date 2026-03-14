package com.projectgoth.fusion.common;

import java.util.LinkedList;
import java.util.Queue;

public class SingleRateLimiter {
   private Queue<Long> eventHistory = new LinkedList();
   private long rateLimitTime;
   private long rateLimitAmount;

   public SingleRateLimiter(long rateLimitTime, long rateLimitAmount) {
      this.rateLimitTime = rateLimitTime;
      this.rateLimitAmount = rateLimitAmount;
   }

   public boolean onEvent(Long now) {
      synchronized(this.eventHistory) {
         if ((long)this.eventHistory.size() > this.rateLimitAmount) {
            this.eventHistory.poll();
         }

         this.eventHistory.offer(now);

         while(now - (Long)this.eventHistory.peek() > this.rateLimitTime) {
            this.eventHistory.poll();
         }

         return (long)this.eventHistory.size() > this.rateLimitAmount;
      }
   }
}
