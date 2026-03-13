package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.jedis.FusionPipelineBlock;
import com.projectgoth.fusion.slice.FusionException;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

public class RedisChatSyncPipelineStore extends RedisChatSyncStore {
   private static final LogFilter log;
   private FusionPipelineBlock client;

   public RedisChatSyncPipelineStore(FusionPipelineBlock client) {
      super((ChatSyncStore.StorePrimacy)null);
      this.client = client;
   }

   public ChatSyncStore.StoreType getStoreType() {
      return ChatSyncStore.StoreType.REDIS;
   }

   public List<Object> pipelined(ChatSyncEntity entity, ChatSyncStorePipeline pipeline) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public int rpush(ChatSyncEntity entity, String value) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entity, true);
         String key = getRedisKey(entity);
         this.client.rpush(key, new String[]{value});
         return 0;
      } catch (Exception var4) {
         this.logError("rpush (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public int rpush(ChatSyncEntity entity, byte[] value) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entity, true);
         byte[] key = Redis.safeEncoderEncode(getRedisKey(entity));
         this.client.rpush(key, new byte[][]{value});
         return 0;
      } catch (Exception var4) {
         this.logError("rpush (pipelined binary)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public String lpop(ChatSyncEntity entityKey) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, true);
         String key = getRedisKey(entityKey);
         this.client.lpop(key);
         return null;
      } catch (Exception var3) {
         this.logError("lpop (pipelined)", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public Integer llen(ChatSyncEntity entityForKey) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityForKey, false);
         String key = getRedisKey(entityForKey);
         this.client.llen(key);
         return null;
      } catch (Exception var3) {
         this.logError("llen (pipelined)", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public List<String> lrange(ChatSyncEntity entityForKey, int start, int end) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityForKey, false);
         String key = getRedisKey(entityForKey);
         this.client.lrange(key, (long)start, (long)end);
         return null;
      } catch (Exception var5) {
         this.logError("lrange (pipelined)", var5);
         throw new FusionException(var5.getMessage());
      }
   }

   public List<byte[]> lrangeBinary(ChatSyncEntity entityForKey, int start, int end) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityForKey, false);
         String key = getRedisKey(entityForKey);
         this.client.lrangeBinary(Redis.safeEncoderEncode(key), (long)start, (long)end);
         return null;
      } catch (Exception var5) {
         this.logError("lrange (pipelined)", var5);
         throw new FusionException(var5.getMessage());
      }
   }

   public String get(ChatSyncEntity entityForKey) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityForKey, false);
         String key = getRedisKey(entityForKey);
         this.client.get(key);
         return null;
      } catch (Exception var3) {
         this.logError("get (pipelined)", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public Integer incr(ChatSyncEntity entityForKey) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityForKey, true);
         String key = getRedisKey(entityForKey);
         this.client.incr(key);
         return null;
      } catch (Exception var3) {
         this.logError("incr (pipelined)", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public String hget(ChatSyncEntity entityForKey, String field) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityForKey, false);
         String key = getRedisKey(entityForKey);
         this.client.hget(key, field);
         return null;
      } catch (Exception var4) {
         this.logError("hget (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public void hset(ChatSyncEntity entityKey, String field, String value) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, true);
         String key = getRedisKey(entityKey);
         this.client.hset(key, field, value);
      } catch (Exception var5) {
         this.logError("hset (pipelined)", var5);
         throw new FusionException(var5.getMessage());
      }
   }

   public Integer hdel(ChatSyncEntity entityForKey, String field) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityForKey, false);
         String key = getRedisKey(entityForKey);
         this.client.hdel(key, new String[]{field});
         return null;
      } catch (Exception var4) {
         this.logError("hdel (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public void lrem(ChatSyncEntity entityKey, int count, String value) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, true);
         String key = getRedisKey(entityKey);
         this.client.lrem(key, (long)count, value);
      } catch (Exception var5) {
         this.logError("lrem (pipelined)", var5);
         throw new FusionException(var5.getMessage());
      }
   }

   public Integer sadd(ChatSyncEntity entityKey, String member) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, true);
         String key = getRedisKey(entityKey);
         this.client.sadd(key, new String[]{member});
         return null;
      } catch (Exception var4) {
         this.logError("sadd (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public Set<String> smembers(ChatSyncEntity entityKey) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, false);
         String key = getRedisKey(entityKey);
         this.client.smembers(key);
         return null;
      } catch (Exception var3) {
         this.logError("smembers (pipelined)", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public Integer srem(ChatSyncEntity entityKey, String member) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, true);
         String key = getRedisKey(entityKey);
         this.client.srem(key, new String[]{member});
         return null;
      } catch (Exception var4) {
         this.logError("srem (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public Integer zadd(ChatSyncEntity forKey, double score, String member) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, true);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zadd with client=" + this.client + " key=" + key + " score=" + score);
         }

         this.client.zadd(key, score, member);
         if (log.isDebugEnabled()) {
            log.debug("Called Redis zadd with client=" + this.client + " key=" + key + " score=" + score);
         }

         return null;
      } catch (Exception var6) {
         this.logError("zadd (pipelined)", var6);
         throw new FusionException(var6.getMessage());
      }
   }

   public Integer zadd(ChatSyncEntity forKey, double score, byte[] member) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, true);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zadd (binary) with client=" + this.client + " key=" + key + " score=" + score);
         }

         this.client.zadd(Redis.safeEncoderEncode(key), score, member);
         if (log.isDebugEnabled()) {
            log.debug("Called Redis zadd (binary) with client=" + this.client + " key=" + key + " score=" + score);
         }

         return null;
      } catch (Exception var6) {
         this.logError("zadd (pipelined, binary)", var6);
         throw new FusionException(var6.getMessage());
      }
   }

   public Set<String> zrange(ChatSyncEntity forKey, int start, int end) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrange with client=" + this.client + " key=" + key + " start=" + start + " end=" + end);
         }

         this.client.zrange(key, (long)start, (long)end);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrange with client=" + this.client + " key=" + key + " start=" + start + " end=" + end);
         }

         return null;
      } catch (Exception var5) {
         this.logError("zrange (pipelined)", var5);
         throw new FusionException(var5.getMessage());
      }
   }

   public Set<byte[]> zrangeBinary(ChatSyncEntity forKey, int start, int end) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrange with client=" + this.client + " key=" + key + " start=" + start + " end=" + end);
         }

         this.client.zrangeBinary(Redis.safeEncoderEncode(key), start, end);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrange with client=" + this.client + " key=" + key + " start=" + start + " end=" + end);
         }

         return null;
      } catch (Exception var5) {
         this.logError("zrange (pipelined)", var5);
         throw new FusionException(var5.getMessage());
      }
   }

   public Set<String> zrangeByScore(ChatSyncEntity forKey, double start, double end) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrangebyscore with client=" + this.client + " key=" + key + " start=" + start + " end=" + end);
         }

         this.client.zrangeByScore(key, start, end);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrangebyscore with client=" + this.client + " key=" + key + " start=" + start + " end=" + end);
         }

         return null;
      } catch (Exception var7) {
         this.logError("zrangeByScore (pipelined)", var7);
         throw new FusionException(var7.getMessage());
      }
   }

   public Set<Tuple> zrangeByScoreWithScores(ChatSyncEntity forKey, double start, double end) throws FusionException {
      Object instance = null;

      Object var8;
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         this.client.zrangeByScoreWithScores(Redis.safeEncoderEncode(key), start, end);
         var8 = null;
      } catch (Exception var13) {
         this.logError("zrangeByScoreWithScores (pipelined)", var13);
         throw new FusionException(var13.getMessage());
      } finally {
         Redis.disconnect((Jedis)instance, log.getLogger());
      }

      return (Set)var8;
   }

   public Set<String> zrangeByScore(ChatSyncEntity forKey, double start, double end, int offset, int count) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrangebyscore with key=" + key + " start=" + start + " end=" + end + " offset=" + offset + " count=" + count);
         }

         this.client.zrangeByScore(key, start, end, offset, count);
         return null;
      } catch (Exception var9) {
         this.logError("zrangeByScore (pipelined) with offset,count", var9);
         throw new FusionException(var9.getMessage());
      }
   }

   public Integer zrank(ChatSyncEntity forKey, String member) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrank with client=" + this.client + " key=" + key + " member=" + member);
         }

         this.client.zrank(key, member);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrank with client=" + this.client + " key=" + key + " member=" + member);
         }

         return null;
      } catch (Exception var4) {
         this.logError("zrank (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public Integer zrankBinary(ChatSyncEntity forKey, byte[] member) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public Integer expire(ChatSyncEntity entityKey, int seconds) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, true);
         String key = getRedisKey(entityKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis expire with client=" + this.client + " key=" + key + " seconds=" + seconds);
         }

         this.client.expire(key, seconds);
         return null;
      } catch (Exception var4) {
         this.logError("expire (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public Integer ttl(ChatSyncEntity entityKey) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, false);
         String key = getRedisKey(entityKey);
         this.client.ttl(key);
         if (log.isDebugEnabled()) {
            log.debug("Redis ttl with client=" + this.client + " key=" + key);
         }

         return null;
      } catch (Exception var3) {
         this.logError("ttl (pipelined)", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public Integer zrem(ChatSyncEntity forKey, String member) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, true);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrem with client=" + this.client + " key=" + key + " member=" + member);
         }

         this.client.zrem(key, new String[]{member});
         return null;
      } catch (Exception var4) {
         this.logError("zrem (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public Integer zremrangeByRank(ChatSyncEntity forKey, int start, int end) throws FusionException {
      Object var4 = null;

      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, true);
         String key = getRedisKey(forKey);
         this.client.zremrangeByRank(key, (long)start, (long)end);
         return null;
      } catch (Exception var6) {
         this.logError("zremrangeByRank (pipelined)", var6);
         throw new FusionException(var6.getMessage());
      }
   }

   public Integer zcount(ChatSyncEntity entityKey, double min, double max) throws FusionException {
      Object var6 = null;

      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(entityKey, true);
         String key = getRedisKey(entityKey);
         this.client.zcount(key, min, max);
         return null;
      } catch (Exception var8) {
         this.logError("zcount (pipelined)", var8);
         throw new FusionException(var8.getMessage());
      }
   }

   public Set<String> zrevrangeByScore(ChatSyncEntity forKey, double start, double end, int offset, int count) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrevrangebyscore with key=" + key + " start=" + start + " end=" + end + " offset=" + offset + " count=" + count);
         }

         this.zrevrangeByScore(this.client, key, start, end, offset, count, false);
         return null;
      } catch (Exception var9) {
         this.logError("zrevrangeByScore (pipelined) with offset,count", var9);
         throw new FusionException(var9.getMessage());
      }
   }

   public Set<Tuple> zrevrangeByScoreWithScores(ChatSyncEntity forKey, double start, double end, int offset, int count) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrevrangeByScoreWithScores with key=" + key + " start=" + start + " end=" + end + " offset=" + offset + " count=" + count);
         }

         this.zrevrangeByScore(this.client, key, start, end, offset, count, true);
         return null;
      } catch (Exception var9) {
         this.logError("zrevrangeByScoreWithScores (pipelined) with offset,count", var9);
         throw new FusionException(var9.getMessage());
      }
   }

   public Set<Tuple> zrevrangeByScoreWithScores(ChatSyncEntity forKey, double start, double end) throws FusionException {
      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, false);
         String key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrevrangeByScoreWithScores with key=" + key + " start=" + start + " end=" + end);
         }

         this.client.zrevrangeByScoreWithScores(key, start, end);
         return null;
      } catch (Exception var7) {
         this.logError("zrevrangeByScoreWithScores (pipelined) with offset,count", var7);
         throw new FusionException(var7.getMessage());
      }
   }

   private Object zrevrangeByScore(FusionPipelineBlock cli, String key, double max, double min, int offset, int count, boolean withScores) throws Exception {
      return withScores ? cli.zrevrangeByScoreWithScores(Redis.safeEncoderEncode(key), max, min, offset, count) : cli.zrevrangeByScoreBinary(Redis.safeEncoderEncode(key), max, min, offset, count);
   }

   public Transaction multi(ChatSyncEntity forKey) throws FusionException {
      Object var2 = null;

      try {
         ChatSyncStats.getInstance().incrementRedisStatsPipelined(forKey, true);
         this.client.multi();
         return null;
      } catch (Exception var4) {
         this.logError("multi (pipelined)", var4);
         throw new FusionException(var4.getMessage());
      }
   }

   public void execTxn(ChatSyncEntity forKey) throws FusionException {
      try {
         this.client.exec();
      } catch (Exception var3) {
         this.logError("execTxn (pipelined)", var3);
         throw new FusionException(var3.getMessage());
      }
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(RedisChatSyncPipelineStore.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
