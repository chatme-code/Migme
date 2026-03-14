package com.projectgoth.fusion.userevent.store.buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ARCCache<K, V> {
   public static final int CACHE_SIZE = 5;
   public static final int DOUBLE_CACHE_SIZE = 10;
   private List<K> listOnceTop = new ArrayList();
   private List<K> listOnceBottom = new ArrayList();
   private List<K> listTwiceTop = new ArrayList();
   private List<K> listTwiceBottom = new ArrayList();
   private ReentrantLock onceTopLock = new ReentrantLock();
   private ReentrantLock onceBottomLock = new ReentrantLock();
   private ReentrantLock twiceTopLock = new ReentrantLock();
   private ReentrantLock twiceBottomLock = new ReentrantLock();
   private final Map<List<K>, ReentrantLock> locks = new HashMap();
   private ConcurrentMap<K, V> cache = new ConcurrentHashMap();
   private int pages = 0;
   private ReentrantLock pagesLock;

   public ARCCache() {
      this.locks.put(this.listOnceTop, this.onceTopLock);
      this.locks.put(this.listOnceBottom, this.onceBottomLock);
      this.locks.put(this.listTwiceTop, this.twiceTopLock);
      this.locks.put(this.listTwiceBottom, this.twiceBottomLock);
   }

   private K removeLRU(List<K> list) {
      ReentrantLock lock = (ReentrantLock)this.locks.get(list);

      Object var3;
      try {
         lock.lock();
         var3 = list.remove(list.size() - 1);
      } finally {
         lock.unlock();
      }

      return var3;
   }

   private void addMRU(List<K> list, K key) {
      ReentrantLock lock = (ReentrantLock)this.locks.get(list);

      try {
         lock.lock();
         list.add(0, key);
      } finally {
         lock.unlock();
      }

   }

   public abstract V getValueFromDB(K var1);

   public abstract void onCacheElementRemoval(K var1, V var2);

   private void replace(int p, boolean containedInBottomTwo) {
      int listOnceTopSize = this.listOnceTop.size();
      Object expiringItem;
      Object removedValue;
      if (listOnceTopSize < 1 || (!containedInBottomTwo || listOnceTopSize != p) && listOnceTopSize <= p) {
         expiringItem = this.removeLRU(this.listTwiceTop);
         this.addMRU(this.listTwiceBottom, expiringItem);
         removedValue = this.cache.remove(expiringItem);
         this.onCacheElementRemoval(expiringItem, removedValue);
      } else {
         expiringItem = this.removeLRU(this.listOnceTop);
         this.addMRU(this.listOnceBottom, expiringItem);
         removedValue = this.cache.remove(expiringItem);
         this.onCacheElementRemoval(expiringItem, removedValue);
      }

   }

   private int getListOnceSize() {
      return this.listOnceTop.size() + this.listOnceBottom.size();
   }

   private int getListTwiceSize() {
      return this.listTwiceTop.size() + this.listTwiceBottom.size();
   }

   private V case1(K key) {
      Object var2;
      try {
         this.onceTopLock.lock();
         if (this.listOnceTop.remove(key)) {
            this.addMRU(this.listTwiceTop, key);
            var2 = this.cache.get(key);
            return var2;
         }
      } finally {
         this.onceTopLock.lock();
      }

      try {
         this.twiceTopLock.lock();
         if (this.listTwiceTop.remove(key)) {
            this.addMRU(this.listTwiceTop, key);
            var2 = this.cache.get(key);
            return var2;
         }
      } finally {
         this.twiceTopLock.lock();
      }

      return null;
   }

   private V case2(K key) {
      boolean removed;
      try {
         this.onceBottomLock.lock();
         removed = this.listOnceBottom.remove(key);
      } finally {
         this.onceBottomLock.unlock();
      }

      if (removed) {
         int bottomRatio = this.listTwiceBottom.size() / this.listOnceBottom.size();
         if (bottomRatio < 1) {
            bottomRatio = 1;
         }

         try {
            this.pagesLock.lock();
            this.pages += bottomRatio;
            if (this.pages > 5) {
               this.pages = 5;
            }
         } finally {
            this.pagesLock.unlock();
         }

         this.replace(this.pages, false);
         this.addMRU(this.listTwiceTop, key);
         Object value = this.getValueFromDB(key);
         this.cache.put(key, value);
         return value;
      } else {
         return null;
      }
   }

   private V case3(K key) {
      try {
         this.twiceBottomLock.lock();
         this.listTwiceBottom.remove(key);
      } finally {
         this.twiceBottomLock.unlock();
      }

      if (this.listTwiceBottom.remove(key)) {
         int bottomRatio = this.listOnceBottom.size() / this.listTwiceBottom.size();
         if (bottomRatio < 1) {
            bottomRatio = 1;
         }

         try {
            this.pagesLock.lock();
            this.pages += -1 * bottomRatio;
            if (this.pages < 0) {
               this.pages = 0;
            }
         } finally {
            this.pagesLock.lock();
         }

         this.replace(this.pages, true);
         this.addMRU(this.listTwiceTop, key);
         Object value = this.getValueFromDB(key);
         this.cache.put(key, value);
         return value;
      } else {
         return null;
      }
   }

   private V case4(K key) {
      int listOnceSize = this.getListOnceSize();
      int listTwiceSize = false;
      Object removedKey;
      if (listOnceSize == 5) {
         if (this.listOnceTop.size() < 5) {
            this.removeLRU(this.listOnceBottom);
            this.replace(this.pages, false);
         } else {
            removedKey = this.removeLRU(this.listOnceTop);
            V removedValue = this.cache.remove(removedKey);
            this.onCacheElementRemoval(removedKey, removedValue);
         }
      } else {
         int listTwiceSize;
         if (listOnceSize < 5 && listOnceSize + (listTwiceSize = this.getListTwiceSize()) >= 5 && listOnceSize + listTwiceSize >= 10) {
            this.removeLRU(this.listTwiceBottom);
            this.replace(this.pages, false);
         }
      }

      removedKey = this.getValueFromDB(key);
      this.addMRU(this.listOnceTop, key);
      this.cache.put(key, removedKey);
      return removedKey;
   }

   public V get(K key) {
      if (this.cache.containsKey(key)) {
         return this.case1(key);
      } else {
         V returnValue = this.case2(key);
         if (returnValue == null) {
            returnValue = this.case3(key);
            if (returnValue == null) {
               returnValue = this.case4(key);
               return returnValue;
            } else {
               return returnValue;
            }
         } else {
            return returnValue;
         }
      }
   }
}
