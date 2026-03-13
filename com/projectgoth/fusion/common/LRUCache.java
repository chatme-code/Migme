package com.projectgoth.fusion.common;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LRUCache<K, V> {
   private static final float hashTableLoadFactor = 0.75F;
   private LinkedHashMap<K, LRUCache<K, V>.CacheItem> map;
   private int cacheSize;
   private long expiryTimeInMilliseconds = 0L;

   public LRUCache(int cacheSize) {
      this.cacheSize = cacheSize;
      int hashTableCapacity = (int)Math.ceil((double)((float)cacheSize / 0.75F)) + 1;
      this.map = new LinkedHashMap<K, LRUCache<K, V>.CacheItem>(hashTableCapacity, 0.75F, true) {
         private static final long serialVersionUID = 1L;

         protected boolean removeEldestEntry(Entry<K, LRUCache<K, V>.CacheItem> eldest) {
            return this.size() > LRUCache.this.cacheSize;
         }
      };
   }

   public LRUCache(int cacheSize, long expiryTimeInSeconds) {
      this.cacheSize = cacheSize;
      this.expiryTimeInMilliseconds = expiryTimeInSeconds * 1000L;
      int hashTableCapacity = (int)Math.ceil((double)((float)cacheSize / 0.75F)) + 1;
      this.map = new LinkedHashMap<K, LRUCache<K, V>.CacheItem>(hashTableCapacity, 0.75F, true) {
         private static final long serialVersionUID = 1L;

         protected boolean removeEldestEntry(Entry<K, LRUCache<K, V>.CacheItem> eldest) {
            return this.size() > LRUCache.this.cacheSize;
         }
      };
   }

   public synchronized V get(K key) {
      LRUCache<K, V>.CacheItem item = (LRUCache.CacheItem)this.map.get(key);
      return item == null ? null : item.getValue();
   }

   public synchronized void put(K key, V value) {
      this.map.put(key, new LRUCache.CacheItem(key, value));
   }

   public synchronized V remove(K key) {
      LRUCache<K, V>.CacheItem removedItem = (LRUCache.CacheItem)this.map.remove(key);
      return removedItem != null ? removedItem.getValue() : null;
   }

   public synchronized void clear() {
      this.map.clear();
   }

   public void setExpiryTimeInMilliseconds(long timeInMs) {
      this.expiryTimeInMilliseconds = timeInMs;
   }

   public synchronized int usedEntries() {
      return this.map.size();
   }

   private class CacheItem {
      private K key;
      private V value;
      private long timeWhenCached;

      public CacheItem(K key, V value) {
         this.key = key;
         this.value = value;
         this.timeWhenCached = System.currentTimeMillis();
      }

      public V getValue() {
         if (LRUCache.this.expiryTimeInMilliseconds != 0L && System.currentTimeMillis() - this.timeWhenCached >= LRUCache.this.expiryTimeInMilliseconds) {
            LRUCache.this.remove(this.key);
            return null;
         } else {
            return this.value;
         }
      }
   }
}
