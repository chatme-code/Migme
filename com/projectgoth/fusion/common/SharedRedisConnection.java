package com.projectgoth.fusion.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class SharedRedisConnection {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SharedRedisConnection.class));
   private MultiSampler sampler = new MultiSampler();
   long lastConnectionAcquiredTimeNS = 0L;
   private final Semaphore semaphore = new Semaphore(1);
   Jedis sharedConnection;
   String server;

   public SharedRedisConnection(String server) {
      this.server = server;
      this.sharedConnection = null;
      SharedRedisConnectionRegistry.getRegistry().registerConnection(this);
   }

   public Jedis getConnection() {
      this.semaphore.acquireUninterruptibly();
      return this.validatedSharedConnection();
   }

   public Jedis getConnection(long timeoutInMs) {
      try {
         if (!this.semaphore.tryAcquire(timeoutInMs, TimeUnit.MILLISECONDS)) {
            return null;
         }
      } catch (InterruptedException var4) {
         return null;
      }

      return this.validatedSharedConnection();
   }

   MultiSampler getSampler() {
      return this.sampler;
   }

   public void returnConnection() {
      try {
         StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
         String methodName = stackTraceElements[2].getMethodName();
         String className = stackTraceElements[2].getClassName();
         this.sampler.add(String.format("%s:%s:%s", this.server, className, methodName), System.nanoTime() - this.lastConnectionAcquiredTimeNS);
      } catch (Exception var4) {
         log.warn("Unexpected exception while recording metrics for SharedRedisConnection: " + var4.getMessage(), var4);
      }

      this.semaphore.release();
   }

   public void shutdown() {
      Redis.disconnect(this.sharedConnection, log);
   }

   private Jedis validatedSharedConnection() {
      try {
         if (this.sharedConnection == null || !"pong".equalsIgnoreCase(this.sharedConnection.ping())) {
            Redis.disconnect(this.sharedConnection, log);
            Jedis conn = Redis.getRedisConnection(this.server);
            this.sharedConnection = conn;
         }

         this.lastConnectionAcquiredTimeNS = System.nanoTime();
         return this.sharedConnection;
      } catch (Exception var2) {
         log.error("Unable to create shared redis connection to server " + this.server + " :" + var2.getMessage(), var2);
         return null;
      }
   }
}
