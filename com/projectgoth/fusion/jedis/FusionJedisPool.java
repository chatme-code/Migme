package com.projectgoth.fusion.jedis;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import redis.clients.jedis.JedisPool;

public class FusionJedisPool extends JedisPool {
   private static final Logger log = Logger.getLogger(FusionJedisPool.class);
   private final AtomicInteger configVersion = new AtomicInteger(0);

   public FusionJedisPool(GenericObjectPoolConfig poolConfig, String host, int port) {
      super(poolConfig, host, port);
   }

   public FusionJedisPool(GenericObjectPoolConfig poolConfig, String host, int port, int timeout) {
      super(poolConfig, host, port, timeout);
   }

   public void ensureConfigUpToDate(int newVersion, GenericObjectPoolConfig updatedConfig) {
      int currentVer = this.configVersion.get();
      if (currentVer < newVersion && this.configVersion.compareAndSet(currentVer, newVersion)) {
         this.internalPool.setConfig(updatedConfig);
         log.info("JedisPool props refreshed for pool=" + this);
      }

   }
}
