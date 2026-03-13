package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.slice.FusionException;
import java.util.List;
import java.util.Set;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

public interface ChatSyncStore {
   ChatSyncStore.StoreType getStoreType();

   ChatSyncStore.StorePrimacy getStorePrimacy();

   void setMaster();

   void setSlave();

   List<Object> pipelined(ChatSyncPipelineOp[] var1) throws FusionException;

   List<Object> pipelined(List<ChatSyncPipelineOp> var1) throws FusionException;

   List<Object> pipelinedBinary(ChatSyncPipelineOp[] var1) throws FusionException;

   List<Object> pipelinedBinary(List<ChatSyncPipelineOp> var1) throws FusionException;

   List<Object> pipelined(ChatSyncEntity var1, ChatSyncStorePipeline var2) throws FusionException;

   List<Object> pipelinedBinary(ChatSyncEntity var1, ChatSyncStorePipeline var2) throws FusionException;

   String lpop(ChatSyncEntity var1) throws FusionException;

   Integer llen(ChatSyncEntity var1) throws FusionException;

   void lrem(ChatSyncEntity var1, int var2, String var3) throws FusionException;

   int rpush(ChatSyncEntity var1, String var2) throws FusionException;

   int rpush(ChatSyncEntity var1, byte[] var2) throws FusionException;

   List<String> lrange(ChatSyncEntity var1, int var2, int var3) throws FusionException;

   List<byte[]> lrangeBinary(ChatSyncEntity var1, int var2, int var3) throws FusionException;

   String get(ChatSyncEntity var1) throws FusionException;

   Integer incr(ChatSyncEntity var1) throws FusionException;

   String hget(ChatSyncEntity var1, String var2) throws FusionException;

   void hset(ChatSyncEntity var1, String var2, String var3) throws FusionException;

   Integer hdel(ChatSyncEntity var1, String var2) throws FusionException;

   Integer sadd(ChatSyncEntity var1, String var2) throws FusionException;

   Set<String> smembers(ChatSyncEntity var1) throws FusionException;

   Integer srem(ChatSyncEntity var1, String var2) throws FusionException;

   Integer zadd(ChatSyncEntity var1, double var2, String var4) throws FusionException;

   Integer zadd(ChatSyncEntity var1, double var2, byte[] var4) throws FusionException;

   Set<String> zrange(ChatSyncEntity var1, int var2, int var3) throws FusionException;

   Set<byte[]> zrangeBinary(ChatSyncEntity var1, int var2, int var3) throws FusionException;

   Set<String> zrangeByScore(ChatSyncEntity var1, double var2, double var4) throws FusionException;

   Set<Tuple> zrangeByScoreWithScores(ChatSyncEntity var1, double var2, double var4) throws FusionException;

   Set<String> zrangeByScore(ChatSyncEntity var1, double var2, double var4, int var6, int var7) throws FusionException;

   Set<String> zrevrangeByScore(ChatSyncEntity var1, double var2, double var4, int var6, int var7) throws FusionException;

   Set<Tuple> zrevrangeByScoreWithScores(ChatSyncEntity var1, double var2, double var4, int var6, int var7) throws FusionException;

   Integer zrank(ChatSyncEntity var1, String var2) throws FusionException;

   Integer zrankBinary(ChatSyncEntity var1, byte[] var2) throws FusionException;

   Integer zrem(ChatSyncEntity var1, String var2) throws FusionException;

   Integer zremrangeByRank(ChatSyncEntity var1, int var2, int var3) throws FusionException;

   Integer zcount(ChatSyncEntity var1, double var2, double var4) throws FusionException;

   Integer expire(ChatSyncEntity var1, int var2) throws FusionException;

   Integer ttl(ChatSyncEntity var1) throws FusionException;

   Transaction multi(ChatSyncEntity var1) throws FusionException;

   void execTxn(ChatSyncEntity var1) throws FusionException;

   public static enum StorePrimacy {
      MASTER,
      SLAVE;
   }

   public static enum StoreType {
      REDIS,
      MYSQL;
   }
}
