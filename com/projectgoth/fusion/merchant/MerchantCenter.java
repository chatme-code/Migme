package com.projectgoth.fusion.merchant;

import com.projectgoth.fusion.common.Redis;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class MerchantCenter {
   private static final Logger log = Logger.getLogger(MerchantCenter.class);
   private static MerchantCenter instance = null;

   private MerchantCenter() {
   }

   public static MerchantCenter getInstance() {
      if (instance == null) {
         instance = new MerchantCenter();
      }

      return instance;
   }

   public void createTagEntry(long accountEntryId) {
      Jedis jedis = null;

      try {
         jedis = Redis.getQueuesMasterInstance();
         jedis.zadd(Redis.KeySpaceQueuesInstance.TOP_MERCHANT_TAG.toString(), (double)accountEntryId, String.valueOf(accountEntryId));
         log.debug("Creating redis entry for " + Redis.KeySpaceQueuesInstance.TOP_MERCHANT_TAG.toString() + ", value: " + accountEntryId);
      } catch (Exception var9) {
         log.error("Failed to create merchant tag entry for accountentryId [" + accountEntryId + "], reason: ", var9);
      } finally {
         Redis.disconnect(jedis, log);
      }

   }
}
