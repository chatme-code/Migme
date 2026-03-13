package com.projectgoth.fusion.userevent.system.loadbalancing;

import com.projectgoth.fusion.common.ConfigUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;

public class ConsistentHash<T> {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ConsistentHash.class));
   private final HashFunction hashFunction;
   private final int numberOfReplicas;
   private final SortedMap<Long, T> circle = new TreeMap();

   public ConsistentHash(HashFunction hashFunction, int numberOfReplicas, Collection<T> nodes) {
      this.hashFunction = hashFunction;
      this.numberOfReplicas = numberOfReplicas;
      Iterator i$ = nodes.iterator();

      while(i$.hasNext()) {
         T node = i$.next();
         this.add(node);
      }

   }

   public void add(T node) {
      for(int i = 0; i < this.numberOfReplicas; ++i) {
         if (log.isDebugEnabled()) {
            log.debug("Adding node [" + node + i + "] as key [" + this.hashFunction.hash(node.toString() + i) + "]");
         }

         this.circle.put(this.hashFunction.hash(node.toString() + i), node);
      }

   }

   public void remove(T node) {
      for(int i = 0; i < this.numberOfReplicas; ++i) {
         this.circle.remove(this.hashFunction.hash(node.toString() + i));
      }

   }

   public T get(String key) {
      if (this.circle.isEmpty()) {
         return null;
      } else {
         long hash = this.hashFunction.hash(key);
         log.debug("key hash is [" + hash + "]");
         if (!this.circle.containsKey(hash)) {
            SortedMap<Long, T> tailMap = this.circle.tailMap(hash);
            hash = tailMap.isEmpty() ? (Long)this.circle.firstKey() : (Long)tailMap.firstKey();
            log.debug("circle did not contain the exact hash, the closest hash is [" + hash + "]");
         }

         return this.circle.get(hash);
      }
   }
}
