package com.projectgoth.fusion.registry;

import Ice.ObjectPrx;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ProxyMap<T extends ObjectPrx> {
   private ConcurrentMap<String, T> proxies = new ConcurrentHashMap();
   private AtomicInteger maxProxies = new AtomicInteger();

   public ConcurrentMap<String, T> getProxies() {
      return this.proxies;
   }

   public T register(String name, T prx) {
      T proxy = (ObjectPrx)this.proxies.put(name, prx);
      this.updateStats();
      return proxy;
   }

   public T deregister(String name) {
      return (ObjectPrx)this.proxies.remove(name);
   }

   public T remove(String name) {
      return (ObjectPrx)this.proxies.remove(name);
   }

   public T find(String name) {
      return (ObjectPrx)this.proxies.get(name);
   }

   public HashMap<String, T> getClone() {
      return new HashMap(this.proxies);
   }

   public int getMaxProxies() {
      return this.maxProxies.get();
   }

   public int size() {
      return this.proxies.size();
   }

   private void updateStats() {
      int users = this.proxies.size();

      int current;
      do {
         current = this.maxProxies.get();
         if (users <= current) {
            return;
         }
      } while(!this.maxProxies.compareAndSet(current, users));

   }

   public void purgeExpired() {
      HashMap<String, T> clone = new HashMap(this.proxies);
      this.purgeExpired(clone.entrySet());
   }

   public abstract void purgeExpired(Set<Entry<String, T>> var1);
}
