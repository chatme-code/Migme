package com.projectgoth.fusion.datagrid;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedDistributedLock;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.objectcache.ChatUserState;
import com.projectgoth.fusion.slice.FusionException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import org.apache.log4j.Logger;

public abstract class LocalFauxDataGrid extends DataGrid {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LocalFauxDataGrid.class));
   private final Cache<String, Map> mapsData;
   private final ConcurrentMap<String, Map> mapsMap;
   private final ExecutorService execService;

   protected abstract int getMapExpirySeconds();

   protected abstract int getMapConcurrencyLevel();

   protected LocalFauxDataGrid() {
      this.mapsData = CacheBuilder.newBuilder().concurrencyLevel(this.getMapConcurrencyLevel()).expireAfterWrite((long)this.getMapExpirySeconds(), TimeUnit.SECONDS).build();
      this.mapsMap = this.mapsData.asMap();
      this.execService = Executors.newCachedThreadPool();
   }

   public void prepare() {
   }

   public ExecutorService getExecutorService(String name) {
      return this.execService;
   }

   public Lock getLock(Object key) {
      return new LocalFauxDataGrid.MemcachedLock(key);
   }

   public Map<String, ChatUserState> getUsersMap() throws FusionException {
      throw new FusionException("Not implemented");
   }

   public <K, V> void configMap(String name, int ttlSeconds) {
      Cache<K, V> data = CacheBuilder.newBuilder().concurrencyLevel(this.getMapConcurrencyLevel()).expireAfterWrite((long)ttlSeconds, TimeUnit.SECONDS).build();
      ConcurrentMap<K, V> map = data.asMap();
      this.mapsMap.put(name, map);
   }

   public Map<String, Integer> getStringIntMap(String name) {
      return (Map)this.mapsMap.get(name);
   }

   public void destroyLock(Lock lock) {
   }

   public void destroyMap(String name) {
      this.mapsMap.remove(name);
   }

   public String getStats() {
      try {
         int values = 0;
         Iterator it = this.mapsMap.values().iterator();

         while(it.hasNext()) {
            Map m = (Map)it.next();
            if (m != null) {
               values += m.size();
            }
         }

         return "LocalFauxDataGrid: map count=" + this.mapsMap.size() + " containing " + values + " total values";
      } catch (Exception var4) {
         log.error("Unable to get LocalFauxDataGrid stats: e=" + var4, var4);
         return "Unable to get LocalFauxDataGrid stats";
      }
   }

   private class MemcachedLock implements Lock {
      private final String key;

      public MemcachedLock(Object key) {
         this.key = key.toString();
      }

      private String getLockID() {
         return MemCachedKeySpaces.CommonKeySpace.FAUX_DATA_GRID + this.key;
      }

      public void lock() {
         MemCachedDistributedLock.getDistributedLock(this.getLockID());
      }

      public void lockInterruptibly() throws InterruptedException {
         throw new RuntimeException("Not supported");
      }

      public boolean tryLock() {
         return MemCachedDistributedLock.getDistributedLock(this.getLockID(), 0L);
      }

      public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
         return MemCachedDistributedLock.getDistributedLock(this.getLockID(), unit.toMillis(time));
      }

      public void unlock() {
         MemCachedDistributedLock.releaseDistributedLock(this.getLockID());
      }

      public Condition newCondition() {
         throw new RuntimeException("Not supported");
      }
   }
}
