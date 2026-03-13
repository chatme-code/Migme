package com.projectgoth.fusion.cache;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class GiftsReceivedCounter {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GiftsReceivedCounter.class));

   public static Integer getCacheCount(int userId) {
      Integer count = null;

      try {
         Jedis slaveInstance = Redis.getSlaveInstanceForUserID(userId);
         if (slaveInstance != null) {
            String userDetailKey = Redis.getUserDetailKey(userId);
            String countStr = slaveInstance.hget(userDetailKey, Redis.FieldUserDetails.GIFTS_RECEIVED_COUNTER.toString());
            if (countStr != null) {
               try {
                  count = Integer.parseInt(countStr);
               } catch (NumberFormatException var6) {
                  log.error("Unable to parse int from string [" + countStr + "]: " + var6);
               }
            }

            Redis.disconnect(slaveInstance, log);
         }
      } catch (Exception var7) {
         log.error("Unable to get the slave Redis instance for User ID [" + userId + "]: " + var7);
      }

      return count;
   }

   public static void setCacheCount(int userId, int count) {
      try {
         Jedis masterInstance = Redis.getMasterInstanceForUserID(userId);
         if (masterInstance != null) {
            String userDetailKey = Redis.getUserDetailKey(userId);
            masterInstance.hsetnx(userDetailKey, Redis.FieldUserDetails.GIFTS_RECEIVED_COUNTER.toString(), Integer.toString(count));
            Redis.disconnect(masterInstance, log);
         }
      } catch (Exception var4) {
         log.error("Unable to get the master redis instance for User ID [" + userId + "]: " + var4);
      }

   }

   public static void incrementCacheCount(int userId) {
      incrementCacheCountBy(userId, 1);
   }

   public static void decrementCacheCount(int userId) {
      incrementCacheCountBy(userId, -1);
   }

   private static void incrementCacheCountBy(int userId, int value) {
      Integer count = getCacheCount(userId);
      if (count != null) {
         if (value < 1 && count < 1) {
            return;
         }

         try {
            Jedis masterInstance = Redis.getMasterInstanceForUserID(userId);
            if (masterInstance != null) {
               String userDetailKey = Redis.getUserDetailKey(userId);
               masterInstance.hincrBy(userDetailKey, Redis.FieldUserDetails.GIFTS_RECEIVED_COUNTER.toString(), (long)value);
               Redis.disconnect(masterInstance, log);
            }
         } catch (Exception var5) {
            log.error("Unable to get the master redis instance for User ID [" + userId + "]: " + var5);
         }
      } else {
         log.info("Unable to increment gifts received counter because no cache value exists for User ID [" + userId + "]");
      }

   }
}
