package com.projectgoth.fusion.common;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RequestCounter extends TimerTask {
   private int numRequests = 0;
   private long updateInterval;
   private float requestsPerSecond = 0.0F;
   private float maxRequestsPerSecond = 0.0F;
   private Date dateOfMaxRequestsPerSecond = new Date();

   public RequestCounter() {
      this.updateInterval = 10L;
      this.start();
   }

   public RequestCounter(long updateInterval) {
      this.updateInterval = updateInterval;
      this.start();
   }

   public void start() {
      Timer timer = new Timer(true);
      timer.schedule(this, this.updateInterval * 1000L, this.updateInterval * 1000L);
   }

   public synchronized void run() {
      if (this.numRequests == 0) {
         this.requestsPerSecond = 0.0F;
      } else {
         this.requestsPerSecond = (float)this.numRequests / (float)this.updateInterval;
      }

      this.numRequests = 0;
      if (this.requestsPerSecond > this.maxRequestsPerSecond) {
         this.maxRequestsPerSecond = this.requestsPerSecond;
         this.dateOfMaxRequestsPerSecond.setTime(System.currentTimeMillis());
      }

   }

   public synchronized void add() {
      ++this.numRequests;
   }

   public synchronized void add(int requests) {
      this.numRequests += requests;
   }

   public synchronized int getNumRequests() {
      return this.numRequests;
   }

   public synchronized float getRequestsPerSecond() {
      return this.requestsPerSecond;
   }

   public synchronized float getMaxRequestsPerSecond() {
      return this.maxRequestsPerSecond;
   }

   public synchronized Date getDateOfMaxRequestsPerSecond() {
      return this.dateOfMaxRequestsPerSecond;
   }
}
