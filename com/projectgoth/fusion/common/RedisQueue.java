package com.projectgoth.fusion.common;

import com.projectgoth.fusion.slice.FusionException;
import java.util.List;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class RedisQueue {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RedisQueue.class));
   private static final String queueKeySpace = "Queue:";
   Jedis redisQueuesMasterInstance = null;

   public static RedisQueue getInstance(Jedis redisConnection) {
      try {
         RedisQueue rq = new RedisQueue(redisConnection);
         return rq;
      } catch (Exception var2) {
         log.error("Unable to create RedisQueue instance: " + var2.getMessage(), var2);
         return null;
      }
   }

   public static RedisQueue getInstance() {
      try {
         RedisQueue rq = new RedisQueue();
         return rq;
      } catch (Exception var1) {
         log.error("Unable to create RedisQueue instance: " + var1.getMessage(), var1);
         return null;
      }
   }

   private RedisQueue(Jedis redisConnection) throws Exception {
      if (redisConnection != null) {
         this.redisQueuesMasterInstance = redisConnection;
      } else {
         throw new Exception("Invalid Initialization - redisConnection is null");
      }
   }

   private RedisQueue() throws Exception {
      this.redisQueuesMasterInstance = Redis.getQueuesMasterInstance();
   }

   public void disconnect() throws Exception {
      Redis.disconnect(this.redisQueuesMasterInstance, log);
   }

   public int push(String queueName, String value, boolean rightToLeft) throws Exception {
      Long resultLong;
      if (rightToLeft) {
         resultLong = this.redisQueuesMasterInstance.rpush("Queue:" + queueName, new String[]{value});
         if (resultLong == null) {
            throw new FusionException("push-rpush returns null.");
         } else {
            return resultLong.intValue();
         }
      } else {
         resultLong = this.redisQueuesMasterInstance.lpush("Queue:" + queueName, new String[]{value});
         if (resultLong == null) {
            throw new FusionException("push-lpush returns null.");
         } else {
            return resultLong.intValue();
         }
      }
   }

   public String pop(String queueName, boolean rightToLeft) throws Exception {
      return rightToLeft ? this.redisQueuesMasterInstance.lpop("Queue:" + queueName) : this.redisQueuesMasterInstance.rpop("Queue:" + queueName);
   }

   public String rpoplpush(String sourceQueueName, String targetQueueName) throws Exception {
      return this.redisQueuesMasterInstance.rpoplpush("Queue:" + sourceQueueName, "Queue:" + targetQueueName);
   }

   public String rpoplpushBlocking(String sourceQueueName, String targetQueueName, int retryInterval, int timeout) throws Exception {
      String value = this.redisQueuesMasterInstance.rpoplpush("Queue:" + sourceQueueName, "Queue:" + targetQueueName);
      if (value == null) {
         long startTime = System.currentTimeMillis();

         while(value == null) {
            try {
               Thread.sleep((long)retryInterval);
            } catch (InterruptedException var9) {
            }

            value = this.redisQueuesMasterInstance.rpoplpush("Queue:" + sourceQueueName, "Queue:" + targetQueueName);
            if (value == null && timeout > 0 && System.currentTimeMillis() - startTime >= (long)timeout) {
               return null;
            }
         }
      }

      return value;
   }

   public int remove(String queueName, int count, String value) throws Exception {
      Long resultLong = this.redisQueuesMasterInstance.lrem("Queue:" + queueName, (long)count, value);
      if (resultLong == null) {
         throw new FusionException("remove-lrem returns null.");
      } else {
         return resultLong.intValue();
      }
   }

   public String popBlocking(String queueName, int retryInterval, int timeout, boolean rightToLeft) throws Exception {
      String value = null;
      if (rightToLeft) {
         value = this.redisQueuesMasterInstance.lpop("Queue:" + queueName);
      } else {
         value = this.redisQueuesMasterInstance.rpop("Queue:" + queueName);
      }

      if (value == null) {
         long startTime = System.currentTimeMillis();

         while(value == null) {
            try {
               Thread.sleep((long)retryInterval);
            } catch (InterruptedException var9) {
            }

            if (rightToLeft) {
               value = this.redisQueuesMasterInstance.lpop("Queue:" + queueName);
            } else {
               value = this.redisQueuesMasterInstance.rpop("Queue:" + queueName);
            }

            if (value == null && timeout > 0 && System.currentTimeMillis() - startTime >= (long)timeout) {
               return null;
            }
         }
      }

      return value;
   }

   public String peek(String queueName, boolean rightToLeft) throws Exception {
      return rightToLeft ? this.redisQueuesMasterInstance.lindex("Queue:" + queueName, 0L) : this.redisQueuesMasterInstance.lindex("Queue:" + queueName, -1L);
   }

   public String peekBlocking(String queueName, int retryInterval, int timeout, boolean rightToLeft) throws Exception {
      String value = null;
      if (rightToLeft) {
         value = this.redisQueuesMasterInstance.lindex("Queue:" + queueName, 0L);
      } else {
         value = this.redisQueuesMasterInstance.lindex("Queue:" + queueName, -1L);
      }

      if (value == null) {
         long startTime = System.currentTimeMillis();

         while(value == null) {
            try {
               Thread.sleep((long)retryInterval);
            } catch (InterruptedException var9) {
            }

            value = this.redisQueuesMasterInstance.lindex("Queue:" + queueName, 0L);
            if (value == null && timeout > 0 && System.currentTimeMillis() - startTime >= (long)timeout) {
               return null;
            }
         }
      }

      return value;
   }

   public int size(String queueName) throws Exception {
      Long resultLong = this.redisQueuesMasterInstance.llen("Queue:" + queueName);
      if (resultLong == null) {
         throw new FusionException("size-llen returns null.");
      } else {
         return resultLong.intValue();
      }
   }

   public List<String> peekAll(String queueName) throws Exception {
      return this.redisQueuesMasterInstance.lrange("Queue:" + queueName, 0L, -1L);
   }

   public int purgeQueue(String queueName) throws Exception {
      Long resultLong = this.redisQueuesMasterInstance.del("Queue:" + queueName);
      if (resultLong == null) {
         throw new FusionException("purgeQueue del returns null.");
      } else {
         return resultLong.intValue();
      }
   }
}
