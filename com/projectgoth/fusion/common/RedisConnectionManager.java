package com.projectgoth.fusion.common;

import com.projectgoth.fusion.jedis.FusionJedisPool;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionManager {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RedisConnectionManager.class));
   private static final ConcurrentHashMap<String, FusionJedisPool> pools = new ConcurrentHashMap();
   private final JedisPoolConfig jedisPoolConfig;
   private final ExecutorService ex;
   private AtomicInteger configVersion;

   private RedisConnectionManager() {
      this.jedisPoolConfig = new JedisPoolConfig();
      this.ex = Executors.newSingleThreadExecutor();
      this.configVersion = new AtomicInteger(0);
      this.ex.execute(new Runnable() {
         public void run() {
            while(true) {
               if (!Thread.currentThread().isInterrupted()) {
                  RedisConnectionManager.this.refreshConfig();

                  try {
                     Thread.sleep((long)(1000 * SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.PROPS_REFRESH_INTERVAL_SECS)));
                     continue;
                  } catch (InterruptedException var2) {
                     RedisConnectionManager.log.info("JedisPool PropsRefreshThread interrupted and shutting down");
                     Thread.currentThread().interrupt();
                  }
               }

               RedisConnectionManager.log.info("JedisPool PropsRefreshThread exiting");
               return;
            }
         }
      });
   }

   public Jedis getConnection(String host, int port) {
      Jedis jedis;
      if ((Boolean)SystemPropertyEntities.JedisPoolConfig.Cache.enabled.getValue()) {
         String key = host + ":" + port;
         int timeout = (Integer)SystemPropertyEntities.JedisPoolConfig.Cache.poolSocketTimeout.getValue();
         FusionJedisPool jedisPool = (FusionJedisPool)pools.get(key);
         if (jedisPool == null) {
            log.info("adding key in pool cache: " + key + " Current number of JedisPool created: " + pools.size() + pools);
            jedisPool = (FusionJedisPool)pools.putIfAbsent(key, new FusionJedisPool(this.jedisPoolConfig, host, port, timeout));
            if (jedisPool == null) {
               jedisPool = (FusionJedisPool)pools.get(key);
            }
         }

         jedisPool.ensureConfigUpToDate(this.configVersion.get(), this.jedisPoolConfig);
         jedis = jedisPool.getResource();
         if (jedis.getClient().getTimeout() != timeout) {
            log.info("jedisPool timeout refreshed.");
            jedis.getClient().setTimeout(timeout);
            jedis.getClient().rollbackTimeout();
         }

         return jedis;
      } else {
         jedis = new Jedis(host, port, (Integer)SystemPropertyEntities.JedisPoolConfig.Cache.nonPoolSocketTimeout.getValue());
         jedis.connect();
         return jedis;
      }
   }

   public void closeConnection(Jedis jedis) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.JEDIS_POOL_ENABLED)) {
         String key = jedis.getClient().getHost() + ":" + jedis.getClient().getPort();
         JedisPool jedisPool = (JedisPool)pools.get(key);
         if (jedisPool != null) {
            jedisPool.returnResource(jedis);
         }
      } else {
         jedis.disconnect();
      }

   }

   private void refreshConfig() {
      boolean blockWhenExhausted = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.BLOCK_WHEN_EXHAUSTED);
      int maxTotal = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.MAX_TOTAL);
      int maxIdle = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.MAX_IDLE);
      int minIdle = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.MIN_IDLE);
      boolean testOnBorrow = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.TEST_ON_BORROW);
      boolean testOnReturn = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.TEST_ON_RETURN);
      boolean testWhileIdle = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.TEST_WHILE_IDLE);
      int numTestsPerEvictionRun = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.NUM_TESTS_PER_EVICTION_RUN);
      long timeBetweenEvictionRunsMillis = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.TIME_BETWEEN_EVICTION_RUN_MILLIS);
      long minEvictableIdleTime = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.MIN_EVICTABLE_IDLE_TIME_MILLIS);
      long maxWait = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.MAX_WAIT);
      boolean propertyChanged = false;
      if (this.jedisPoolConfig.getBlockWhenExhausted() != blockWhenExhausted) {
         this.jedisPoolConfig.setBlockWhenExhausted(blockWhenExhausted);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getMaxTotal() != maxTotal) {
         this.jedisPoolConfig.setMaxTotal(maxTotal);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getMaxIdle() != maxIdle) {
         this.jedisPoolConfig.setMaxIdle(maxIdle);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getMinIdle() != minIdle) {
         this.jedisPoolConfig.setMinIdle(minIdle);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getTestOnBorrow() != testOnBorrow) {
         this.jedisPoolConfig.setTestOnBorrow(testOnBorrow);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getTestOnReturn() != testOnReturn) {
         this.jedisPoolConfig.setTestOnReturn(testOnReturn);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getTestWhileIdle() != testWhileIdle) {
         this.jedisPoolConfig.setTestWhileIdle(testWhileIdle);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getNumTestsPerEvictionRun() != numTestsPerEvictionRun) {
         this.jedisPoolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getTimeBetweenEvictionRunsMillis() != timeBetweenEvictionRunsMillis) {
         this.jedisPoolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getMinEvictableIdleTimeMillis() != minEvictableIdleTime) {
         this.jedisPoolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTime);
         propertyChanged = true;
      }

      if (this.jedisPoolConfig.getMaxWaitMillis() != maxWait) {
         this.jedisPoolConfig.setMaxWaitMillis(maxWait);
         propertyChanged = true;
      }

      if (propertyChanged) {
         this.configVersion.incrementAndGet();
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.PROPS_LOG_ENABLED)) {
         StringBuilder sb = new StringBuilder();
         sb.append("\n maxTotal=").append(this.jedisPoolConfig.getMaxTotal());
         sb.append("\n maxIdle=").append(this.jedisPoolConfig.getMaxIdle());
         sb.append("\n minIdle=").append(this.jedisPoolConfig.getMinIdle());
         sb.append("\n testOnBorrow=").append(this.jedisPoolConfig.getTestOnBorrow());
         sb.append("\n testOnReturn=").append(this.jedisPoolConfig.getTestOnReturn());
         sb.append("\n testWhileIdle=").append(this.jedisPoolConfig.getTestWhileIdle());
         sb.append("\n numTestsPerEvictionRun=").append(this.jedisPoolConfig.getNumTestsPerEvictionRun());
         sb.append("\n timeBetweenEvictionRunMillis=").append(this.jedisPoolConfig.getTimeBetweenEvictionRunsMillis());
         sb.append("\n MinEvictableIdleTimeMillis=").append(this.jedisPoolConfig.getMinEvictableIdleTimeMillis());
         sb.append("\n maxWait=").append(this.jedisPoolConfig.getMaxWaitMillis());
         log.info("Refreshed jedis pool config: " + sb.toString());
      }

   }

   public static RedisConnectionManager getInstance() {
      return RedisConnectionManager.RedisConnectionManagerHolder.INSTANCE;
   }

   public void shutdown() {
      this.ex.shutdown();
      Iterator i$ = pools.values().iterator();

      while(i$.hasNext()) {
         JedisPool jedisPool = (JedisPool)i$.next();
         jedisPool.destroy();
      }

   }

   // $FF: synthetic method
   RedisConnectionManager(Object x0) {
      this();
   }

   private static class RedisConnectionManagerHolder {
      public static final RedisConnectionManager INSTANCE = new RedisConnectionManager();
   }
}
