package com.projectgoth.fusion.botservice.bot.migbot.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LimitTracker implements Serializable {
   private long expires;
   private ConcurrentMap<String, Double> amountSpent = new ConcurrentHashMap();
   private ConcurrentMap<String, Double> lastAmountSpent = new ConcurrentHashMap();

   public LimitTracker(String instanceID, long expires, double amount) {
      this.amountSpent.put(instanceID, amount);
      this.expires = expires;
   }

   public long getExpires() {
      return this.expires;
   }

   public double getTotalAmountSpent() {
      double total = 0.0D;
      Collection<Double> values = this.amountSpent.values();

      double value;
      for(Iterator i$ = values.iterator(); i$.hasNext(); total += value) {
         value = (Double)i$.next();
      }

      return total;
   }

   public void add(String instanceID, double amount) {
      double temp = 0.0D;
      if (this.amountSpent.containsKey(instanceID)) {
         temp = (Double)this.amountSpent.get(instanceID);
      }

      this.lastAmountSpent.put(instanceID, amount);
      this.amountSpent.put(instanceID, temp + amount);
   }

   public double revert(String instanceID) {
      double revert = 0.0D;
      if (this.lastAmountSpent.containsKey(instanceID)) {
         revert = (Double)this.lastAmountSpent.remove(instanceID);
         if (this.amountSpent.containsKey(instanceID)) {
            double temp = (Double)this.amountSpent.get(instanceID);
            this.amountSpent.put(instanceID, temp - revert);
            return revert;
         } else {
            return 0.0D;
         }
      } else {
         Double value = (Double)this.amountSpent.remove(instanceID);
         return value == null ? 0.0D : value;
      }
   }

   public boolean hasExpired(long currentTimeInMillis) {
      return this.expires < currentTimeInMillis;
   }
}
