package com.projectgoth.fusion.bothunter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.projectgoth.fusion.common.ConfigUtils;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

public class UsernameCountPerIP {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UsernameCountPerIP.class));
   private final Cache<String, AtomicInteger> data;
   private final ConcurrentMap<String, AtomicInteger> map;

   private UsernameCountPerIP() {
      this.data = CacheBuilder.newBuilder().concurrencyLevel(4).maximumSize(10000L).expireAfterWrite(30L, TimeUnit.MINUTES).build();
      this.map = this.data.asMap();
   }

   public static UsernameCountPerIP getInstance() {
      return UsernameCountPerIP.SingletonHolder.INSTANCE;
   }

   public int incrementAndGet(String ip) {
      if (this.map.get(ip) == null) {
         this.map.put(ip, new AtomicInteger(0));
      }

      return ((AtomicInteger)this.map.get(ip)).incrementAndGet();
   }

   public int get(String ip) {
      return this.map.get(ip) == null ? 0 : ((AtomicInteger)this.map.get(ip)).get();
   }

   // $FF: synthetic method
   UsernameCountPerIP(Object x0) {
      this();
   }

   private static class SingletonHolder {
      public static final UsernameCountPerIP INSTANCE = new UsernameCountPerIP();
   }
}
