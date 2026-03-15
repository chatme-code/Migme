package com.projectgoth.fusion.sessioncache;

import java.util.concurrent.atomic.AtomicInteger;

public class TotalCountrySessions {
   private AtomicInteger totalAuthenticatedSessions;
   private AtomicInteger totalNonAuthenticatedSessions;

   public TotalCountrySessions() {
      this.totalAuthenticatedSessions = new AtomicInteger(0);
      this.totalNonAuthenticatedSessions = new AtomicInteger(0);
   }

   public TotalCountrySessions(int totalAuthenticatedSessions, int totalNonAuthenticatedSessions) {
      this.totalAuthenticatedSessions = new AtomicInteger(totalAuthenticatedSessions);
      this.totalNonAuthenticatedSessions = new AtomicInteger(totalNonAuthenticatedSessions);
   }

   public AtomicInteger getTotalAuthenticatedSessions() {
      return this.totalAuthenticatedSessions;
   }

   public void setTotalAuthenticatedSessions(AtomicInteger totalAuthenticatedSessions) {
      this.totalAuthenticatedSessions = totalAuthenticatedSessions;
   }

   public AtomicInteger getTotalNonAuthenticatedSessions() {
      return this.totalNonAuthenticatedSessions;
   }

   public void setTotalNonAuthenticatedSessions(AtomicInteger totalNonAuthenticatedSessions) {
      this.totalNonAuthenticatedSessions = totalNonAuthenticatedSessions;
   }
}
