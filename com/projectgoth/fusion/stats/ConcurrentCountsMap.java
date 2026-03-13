package com.projectgoth.fusion.stats;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentCountsMap<K> {
   private ConcurrentHashMap<K, AtomicInteger> map = new ConcurrentHashMap();

   public AtomicInteger get(K key) {
      AtomicInteger count = (AtomicInteger)this.map.get(key);
      if (count == null) {
         this.map.putIfAbsent(key, new AtomicInteger());
         count = (AtomicInteger)this.map.get(key);
      }

      return count;
   }

   public void increment(K key) {
      this.get(key).incrementAndGet();
   }

   public void decrement(K key) {
      int test = this.get(key).decrementAndGet();
      if (test <= 0) {
         this.map.remove(key, new AtomicInteger(test));
      }

   }

   public int size() {
      return this.map.size();
   }
}
