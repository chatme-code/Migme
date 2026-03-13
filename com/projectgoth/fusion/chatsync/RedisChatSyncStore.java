package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.jedis.FusionPipelineBlock;
import com.projectgoth.fusion.slice.FusionException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

public class RedisChatSyncStore implements ChatSyncStore {
   private static final LogFilter log;
   private static AtomicInteger totalPipelineExceptions;
   private ChatSyncStore.StorePrimacy primacy;
   private ChatSyncEntity lastEntity;
   private Jedis lastMasterInstance;
   private Jedis lastSlaveInstance;

   public static int getTotalPipelineExceptions() {
      return totalPipelineExceptions.get();
   }

   protected void logError(String message, Throwable t) {
      log.error(message, t);
      if (log.isDebugEnabled()) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         t.printStackTrace(pw);
         log.error(sw.toString());
      }

   }

   public RedisChatSyncStore(ChatSyncStore.StorePrimacy primacy) {
      this.primacy = primacy;
   }

   public ChatSyncStore.StorePrimacy getStorePrimacy() {
      return this.primacy;
   }

   public void setMaster() {
      this.primacy = ChatSyncStore.StorePrimacy.MASTER;
   }

   public void setSlave() {
      this.primacy = ChatSyncStore.StorePrimacy.SLAVE;
   }

   public ChatSyncStore.StoreType getStoreType() {
      return ChatSyncStore.StoreType.REDIS;
   }

   protected Jedis getInstance(ChatSyncEntity entity) throws Exception {
      if (entity.getEntityType() == ChatSyncEntity.ChatSyncEntityType.MESSAGE_STATUS_EVENT) {
         MessageStatusEventKey mseKey;
         if (entity instanceof MessageStatusEvents) {
            MessageStatusEvents events = (MessageStatusEvents)entity;
            mseKey = events.getEventsKey();
         } else {
            if (!(entity instanceof MessageStatusEventKey)) {
               throw new FusionException("Unknown entity of type MESSAGE_STATUS_EVENT, entity=" + entity);
            }

            mseKey = (MessageStatusEventKey)entity;
         }

         return this.primacy.equals(ChatSyncStore.StorePrimacy.MASTER) ? Redis.getMasterInstanceForEntityID(Redis.KeySpace.CONVERSATION_ENTITY, mseKey.getChatKey().hashCode()) : Redis.getSlaveInstanceForEntityID(Redis.KeySpace.CONVERSATION_ENTITY, mseKey.getChatKey().hashCode());
      } else if (entity.getEntityType() != ChatSyncEntity.ChatSyncEntityType.CONVERSATION && entity.getEntityType() != ChatSyncEntity.ChatSyncEntityType.MESSAGE) {
         if (entity.getEntityType() == ChatSyncEntity.ChatSyncEntityType.USER) {
            if (this.primacy.equals(ChatSyncStore.StorePrimacy.MASTER)) {
               if (log.isDebugEnabled()) {
                  log.debug("Getting master Redis instance for user=" + entity.getKey());
               }

               return Redis.getMasterInstanceForUserID(Integer.parseInt(entity.getKey()));
            } else {
               return Redis.getSlaveInstanceForUserID(Integer.parseInt(entity.getKey()));
            }
         } else {
            throw new FusionException("Invalid ChatSyncEntity type in getInstance: type=" + entity.getEntityType().toString());
         }
      } else {
         return this.primacy.equals(ChatSyncStore.StorePrimacy.MASTER) ? Redis.getMasterInstanceForEntityID(Redis.KeySpace.CONVERSATION_ENTITY, entity.getKey().hashCode()) : Redis.getSlaveInstanceForEntityID(Redis.KeySpace.CONVERSATION_ENTITY, entity.getKey().hashCode());
      }
   }

   public static String getRedisKey(ChatSyncEntity entity) throws FusionException {
      if (entity instanceof ChatDefinition) {
         return Redis.getConversationDefinitionKey(entity.getKey());
      } else if (entity instanceof ChatMessage) {
         return Redis.getConversationMessageKey(entity.getKey());
      } else if (!(entity instanceof MessageStatusEvent) && !(entity instanceof MessageStatusEventKey)) {
         if (entity instanceof ParticipantList) {
            return Redis.getConversationParticipantsKey(entity.getKey());
         } else if (entity instanceof OldChatList) {
            OldChatList cl = (OldChatList)entity;
            return Redis.getOldChatListsKey(cl.getUserID());
         } else if (entity instanceof OldChatLists) {
            OldChatLists cl = (OldChatLists)entity;
            return Redis.getOldChatListsKey(cl.getUserID());
         } else if (entity instanceof CurrentChatList) {
            CurrentChatList cl = (CurrentChatList)entity;
            return Redis.getCurrentChatListKey(cl.getUserID());
         } else if (entity instanceof ChatListVersion) {
            ChatListVersion clv = (ChatListVersion)entity;
            return Redis.getChatListVersionKey(clv.getUserID());
         } else {
            throw new FusionException("Invalid ChatSyncEntity type in getRedisKey: entity=" + entity);
         }
      } else {
         MessageStatusEventKey mseKey = (MessageStatusEventKey)entity;
         return Redis.getMessageStatusEventKey(mseKey);
      }
   }

   public List<Object> pipelined(List<ChatSyncPipelineOp> pipelineOps) throws FusionException {
      ChatSyncPipelineOp[] opsArray = (ChatSyncPipelineOp[])pipelineOps.toArray(new ChatSyncPipelineOp[pipelineOps.size()]);
      return this.pipelined(opsArray);
   }

   public List<Object> pipelinedBinary(List<ChatSyncPipelineOp> pipelineOps) throws FusionException {
      ChatSyncPipelineOp[] opsArray = (ChatSyncPipelineOp[])pipelineOps.toArray(new ChatSyncPipelineOp[pipelineOps.size()]);
      return this.pipelinedBinary(opsArray);
   }

   public List<Object> pipelined(ChatSyncPipelineOp[] pipelineOps) throws FusionException {
      List<Object> results = this.pipelinedBinaryOps(pipelineOps);
      int startIndex = 0;
      ChatSyncPipelineOp[] arr$ = pipelineOps;
      int len$ = pipelineOps.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatSyncPipelineOp op = arr$[i$];
         startIndex = op.processResults(results, startIndex);
      }

      return results;
   }

   public List<Object> pipelinedBinary(ChatSyncPipelineOp[] pipelineOps) throws FusionException {
      List<Object> results = this.pipelinedBinaryOps(pipelineOps);
      int startIndex = 0;
      ChatSyncPipelineOp[] arr$ = pipelineOps;
      int len$ = pipelineOps.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ChatSyncPipelineOp op = arr$[i$];
         startIndex = op.processResults(results, startIndex);
      }

      return results;
   }

   private List<Object> pipelinedBinaryOps(final ChatSyncPipelineOp[] pipelineOps) throws FusionException {
      Jedis instance = null;

      List var5;
      try {
         if (pipelineOps.length == 0) {
            throw new FusionException("pipelineOps should not be empty");
         }

         boolean isStorage = this.primacy.equals(ChatSyncStore.StorePrimacy.MASTER);
         ChatSyncStats.getInstance().incrementRedisStats(pipelineOps[0].getEntity(), isStorage);
         instance = this.getInstance(pipelineOps[0].getEntity());
         List results;
         if (instance == null) {
            if (this.primacy.equals(ChatSyncStore.StorePrimacy.SLAVE)) {
               results = null;
               return results;
            }

            throw new FusionException("Couldn't retrieve shard for entity " + pipelineOps[0].getEntity());
         }

         results = instance.pipelined(new FusionPipelineBlock() {
            public void execute() {
               try {
                  RedisChatSyncPipelineStore wrapper = new RedisChatSyncPipelineStore(this);
                  ChatSyncPipelineOp[] arr$ = pipelineOps;
                  int len$ = arr$.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     ChatSyncPipelineOp op = arr$[i$];
                     ChatSyncEntity entity = op.getEntity();
                     if (op.isRead()) {
                        entity.retrievePipeline(wrapper);
                     } else {
                        entity.storePipeline(wrapper);
                     }
                  }

               } catch (FusionException var7) {
                  RedisChatSyncStore.this.logError("pipelined(ops)", var7);
                  RedisChatSyncStore.totalPipelineExceptions.incrementAndGet();
                  throw new RuntimeException(var7);
               }
            }
         });
         var5 = results;
      } catch (FusionException var11) {
         this.logError("pipelined(ops)", var11);
         throw var11;
      } catch (Exception var12) {
         this.logError("pipelined(ops)", var12);
         throw new FusionException(var12.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var5;
   }

   public List<Object> pipelined(ChatSyncEntity entity, ChatSyncStorePipeline pipeline) throws FusionException {
      List<Object> results = this.pipelinedBinary(entity, pipeline);
      return results;
   }

   public List<Object> pipelinedBinary(final ChatSyncEntity entity, final ChatSyncStorePipeline pipeline) throws FusionException {
      Jedis instance = null;

      List var6;
      try {
         boolean isStorage = this.primacy.equals(ChatSyncStore.StorePrimacy.MASTER);
         ChatSyncStats.getInstance().incrementRedisStats(entity, isStorage);
         instance = this.getInstance(entity);
         List results;
         if (instance == null) {
            if (this.primacy.equals(ChatSyncStore.StorePrimacy.SLAVE)) {
               results = null;
               return results;
            }

            throw new FusionException("Couldn't retrieve shard for entity " + entity);
         }

         results = instance.pipelined(new FusionPipelineBlock() {
            public void execute() {
               try {
                  RedisChatSyncPipelineStore wrapper = new RedisChatSyncPipelineStore(this);
                  pipeline.execute(entity, wrapper);
               } catch (FusionException var2) {
                  RedisChatSyncStore.this.logError("pipelined(entity)", var2);
                  RedisChatSyncStore.totalPipelineExceptions.incrementAndGet();
                  throw new RuntimeException(var2);
               }
            }
         });
         var6 = results;
      } catch (FusionException var12) {
         this.logError("pipelined(entity)", var12);
         throw var12;
      } catch (Exception var13) {
         this.logError("pipelined(entity)", var13);
         throw new FusionException(var13.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var6;
   }

   public int rpush(ChatSyncEntity entity, String value) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public int rpush(ChatSyncEntity entity, byte[] value) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public String lpop(ChatSyncEntity entityKey) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, true);
         instance = this.getInstance(entityKey);
         if (instance != null) {
            key = getRedisKey(entityKey);
            String var4 = instance.lpop(key);
            return var4;
         }

         key = null;
      } catch (Exception var9) {
         this.logError("lpop", var9);
         throw new FusionException(var9.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public Integer llen(ChatSyncEntity entityForKey) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityForKey, false);
         instance = this.getInstance(entityForKey);
         if (instance != null) {
            key = getRedisKey(entityForKey);
            Long result = instance.llen(key);
            Integer var5 = result == null ? null : result.intValue();
            return var5;
         }

         key = null;
      } catch (Exception var10) {
         this.logError("llen", var10);
         throw new FusionException(var10.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public List<String> lrange(ChatSyncEntity entityForKey, int start, int end) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityForKey, false);
         instance = this.getInstance(entityForKey);
         if (instance != null) {
            key = getRedisKey(entityForKey);
            List var6 = instance.lrange(key, (long)start, (long)end);
            return var6;
         }

         key = null;
      } catch (Exception var11) {
         this.logError("lrange", var11);
         throw new FusionException(var11.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public List<byte[]> lrangeBinary(ChatSyncEntity forKey, int start, int end) throws FusionException {
      Jedis instance = null;

      List var8;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(forKey);
         byte[] keyBytes = Redis.safeEncoderEncode(key);
         List<byte[]> results = instance.lrange(keyBytes, (long)start, (long)end);
         var8 = results;
      } catch (Exception var13) {
         this.logError("lrangeBinary", var13);
         throw new FusionException(var13.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var8;
   }

   public String get(ChatSyncEntity entityForKey) throws FusionException {
      Jedis instance = null;

      String var4;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityForKey, false);
         instance = this.getInstance(entityForKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(entityForKey);
         var4 = instance.get(key);
      } catch (Exception var9) {
         this.logError("get", var9);
         throw new FusionException(var9.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var4;
   }

   public Integer incr(ChatSyncEntity entityForKey) throws FusionException {
      Jedis instance = null;

      Integer var5;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityForKey, true);
         instance = this.getInstance(entityForKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(entityForKey);
         Long result = instance.incr(key);
         var5 = result == null ? null : result.intValue();
      } catch (Exception var10) {
         this.logError("incr", var10);
         throw new FusionException(var10.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var5;
   }

   public String hget(ChatSyncEntity entityForKey, String field) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityForKey, false);
         instance = this.getInstance(entityForKey);
         if (instance != null) {
            key = getRedisKey(entityForKey);
            String var5 = instance.hget(key, field);
            return var5;
         }

         key = null;
      } catch (Exception var10) {
         this.logError("hget", var10);
         throw new FusionException(var10.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public void hset(ChatSyncEntity entityKey, String field, String value) throws FusionException {
      Jedis instance = null;

      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, true);
         instance = this.getInstance(entityKey);
         String key = getRedisKey(entityKey);
         instance.hset(key, field, value);
      } catch (Exception var10) {
         this.logError("hset", var10);
         throw new FusionException(var10.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

   }

   public Integer hdel(ChatSyncEntity entityForKey, String field) throws FusionException {
      Jedis instance = null;

      Integer var7;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityForKey, false);
         instance = this.getInstance(entityForKey);
         String key = getRedisKey(entityForKey);
         Long resultLong = instance.hdel(key, new String[]{field});
         Integer result = resultLong == null ? null : resultLong.intValue();
         var7 = result;
      } catch (Exception var12) {
         this.logError("hdel", var12);
         throw new FusionException(var12.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var7;
   }

   public void lrem(ChatSyncEntity entityKey, int count, String value) throws FusionException {
      Jedis instance = null;

      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, true);
         instance = this.getInstance(entityKey);
         String key = getRedisKey(entityKey);
         instance.lrem(key, (long)count, value);
      } catch (Exception var10) {
         this.logError("lrem", var10);
         throw new FusionException(var10.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

   }

   public Integer sadd(ChatSyncEntity entityKey, String member) throws FusionException {
      Jedis instance = null;

      Integer var6;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, true);
         instance = this.getInstance(entityKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(entityKey);
         Long result = instance.sadd(key, new String[]{member});
         var6 = result == null ? null : result.intValue();
      } catch (Exception var11) {
         this.logError("sadd", var11);
         throw new FusionException(var11.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var6;
   }

   public Set<String> smembers(ChatSyncEntity entityKey) throws FusionException {
      Jedis instance = null;

      Set var4;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, false);
         instance = this.getInstance(entityKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(entityKey);
         var4 = instance.smembers(key);
      } catch (Exception var9) {
         this.logError("smembers", var9);
         throw new FusionException(var9.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var4;
   }

   public Integer srem(ChatSyncEntity entityKey, String member) throws FusionException {
      Jedis instance = null;

      Integer var6;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, true);
         instance = this.getInstance(entityKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(entityKey);
         Long result = instance.srem(key, new String[]{member});
         var6 = result == null ? null : result.intValue();
      } catch (Exception var11) {
         this.logError("srem", var11);
         throw new FusionException(var11.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var6;
   }

   public Integer zadd(ChatSyncEntity forKey, double score, String member) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, true);
         instance = this.getInstance(forKey);
         if (instance != null) {
            key = getRedisKey(forKey);
            if (log.isDebugEnabled()) {
               log.debug("Redis zadd with instance=" + instance + " key=" + key + " score=" + score);
            }

            Long resultLong = instance.zadd(key, score, member);
            Integer result = resultLong == null ? null : resultLong.intValue();
            if (log.isDebugEnabled()) {
               log.debug("Called Redis zadd with instance=" + instance + " key=" + key + " score=" + score + " result=" + result);
            }

            Integer var9 = result;
            return var9;
         }

         key = null;
      } catch (Exception var14) {
         this.logError("zadd", var14);
         throw new FusionException(var14.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public Integer zadd(ChatSyncEntity forKey, double score, byte[] member) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public Set<String> zrange(ChatSyncEntity forKey, int start, int end) throws FusionException {
      Jedis instance = null;

      Set var7;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrange with instance=" + instance + " key=" + key + " start=" + start + " end=" + end);
         }

         Set<String> results = instance.zrange(key, (long)start, (long)end);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrange with instance=" + instance + " key=" + key + " start=" + start + " end=" + end + " results size=" + results.size());
         }

         var7 = results;
      } catch (Exception var12) {
         this.logError("zrange", var12);
         throw new FusionException(var12.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var7;
   }

   public Set<byte[]> zrangeBinary(ChatSyncEntity forKey, int start, int end) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         if (instance != null) {
            key = getRedisKey(forKey);
            byte[] keyBytes = Redis.safeEncoderEncode(key);
            if (log.isDebugEnabled()) {
               log.debug("Redis zrangeBinary with instance=" + instance + " key=" + key + " start=" + start + " end=" + end);
            }

            Set<byte[]> results = instance.zrange(keyBytes, (long)start, (long)end);
            if (log.isDebugEnabled()) {
               log.debug("Redis zrangeBinary with instance=" + instance + " key=" + key + " start=" + start + " end=" + end + " results size=" + results.size());
            }

            Set var8 = results;
            return var8;
         }

         key = null;
      } catch (Exception var13) {
         this.logError("zrange", var13);
         throw new FusionException(var13.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public Set<String> zrangeByScore(ChatSyncEntity forKey, double start, double end) throws FusionException {
      Jedis instance = null;

      Set var9;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrangebyscore with instance=" + instance + " key=" + key + " start=" + start + " end=" + end);
         }

         Set<String> results = instance.zrangeByScore(key, start, end);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrangebyscore with instance=" + instance + " key=" + key + " start=" + start + " end=" + end + " results size=" + results.size());
         }

         var9 = results;
      } catch (Exception var14) {
         this.logError("zrangeByScore", var14);
         throw new FusionException(var14.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var9;
   }

   public Set<Tuple> zrangeByScoreWithScores(ChatSyncEntity forKey, double start, double end) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         if (instance != null) {
            key = getRedisKey(forKey);
            Set var8 = instance.zrangeByScoreWithScores(key, start, end);
            return var8;
         }

         key = null;
      } catch (Exception var13) {
         this.logError("zrangeByScoreWithScores", var13);
         throw new FusionException(var13.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public Set<String> zrangeByScore(ChatSyncEntity forKey, double start, double end, int offset, int count) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         if (instance != null) {
            key = getRedisKey(forKey);
            if (log.isDebugEnabled()) {
               log.debug("Redis zrangebyscore with instance=" + instance + " key=" + key + " start=" + start + " end=" + end + " offset=" + offset + " count=" + count);
            }

            Set<String> results = instance.zrangeByScore(key, start, end, offset, count);
            if (log.isDebugEnabled()) {
               log.debug("Redis zrangebyscore with instance=" + instance + " key=" + key + " start=" + start + " end=" + end + " offset=" + offset + " count=" + count + " results size=" + results.size());
            }

            Set var11 = results;
            return var11;
         }

         key = null;
      } catch (Exception var16) {
         this.logError("zrangeByScore with offset,count", var16);
         throw new FusionException(var16.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public Integer zrank(ChatSyncEntity forKey, String member) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         if (instance != null) {
            key = getRedisKey(forKey);
            if (log.isDebugEnabled()) {
               log.debug("Redis zrank with instance=" + instance + " key=" + key + " member=" + member);
            }

            Long resultLong = instance.zrank(key, member);
            Integer result = resultLong == null ? null : resultLong.intValue();
            if (log.isDebugEnabled()) {
               log.debug("Redis zrank with instance=" + instance + " key=" + key + " member=" + member + " rank=" + result);
            }

            Integer var7 = result;
            return var7;
         }

         key = null;
      } catch (Exception var12) {
         this.logError("zrank", var12);
         throw new FusionException(var12.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public Integer zrankBinary(ChatSyncEntity forKey, byte[] member) throws FusionException {
      Jedis instance = null;

      Integer var7;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, false);
         instance = this.getInstance(forKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(forKey);
         byte[] keyBytes = Redis.safeEncoderEncode(key);
         Long result = instance.zrank(keyBytes, member);
         var7 = result == null ? null : result.intValue();
      } catch (Exception var12) {
         this.logError("zrank", var12);
         throw new FusionException(var12.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var7;
   }

   public Integer expire(ChatSyncEntity entityKey, int seconds) throws FusionException {
      Jedis instance = null;

      Integer var6;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, true);
         instance = this.getInstance(entityKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(entityKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis expire with instance=" + instance + " key=" + key + " seconds=" + seconds);
         }

         Long result = instance.expire(key, seconds);
         var6 = result == null ? null : result.intValue();
      } catch (Exception var11) {
         this.logError("expire", var11);
         throw new FusionException(var11.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var6;
   }

   public Integer ttl(ChatSyncEntity entityKey) throws FusionException {
      Jedis instance = null;

      String key;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, false);
         instance = this.getInstance(entityKey);
         if (instance != null) {
            key = getRedisKey(entityKey);
            Long result = instance.ttl(key);
            Integer ttlSeconds = result == null ? null : result.intValue();
            if (log.isDebugEnabled()) {
               log.debug("Redis ttl with instance=" + instance + " key=" + key + " seconds=" + ttlSeconds);
            }

            Integer var6 = ttlSeconds;
            return var6;
         }

         key = null;
      } catch (Exception var11) {
         this.logError("ttl", var11);
         throw new FusionException(var11.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return key;
   }

   public Integer zrem(ChatSyncEntity forKey, String member) throws FusionException {
      Jedis instance = null;

      Integer var7;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, true);
         instance = this.getInstance(forKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(forKey);
         if (log.isDebugEnabled()) {
            log.debug("Redis zrem with instance=" + instance + " key=" + key + " member=" + member);
         }

         Long resultLong = instance.zrem(key, new String[]{member});
         Integer result = resultLong == null ? null : resultLong.intValue();
         if (log.isDebugEnabled()) {
            log.debug("Redis zrem with instance=" + instance + " key=" + key + " member=" + member + " result=" + result);
         }

         var7 = result;
      } catch (Exception var12) {
         this.logError("zrem", var12);
         throw new FusionException(var12.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var7;
   }

   public Integer zremrangeByRank(ChatSyncEntity forKey, int start, int end) throws FusionException {
      Jedis instance = null;

      Integer var7;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(forKey, true);
         instance = this.getInstance(forKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(forKey);
         Long resultLong = instance.zremrangeByRank(key, (long)start, (long)end);
         var7 = resultLong == null ? null : resultLong.intValue();
      } catch (Exception var12) {
         this.logError("zremrangeByRank", var12);
         throw new FusionException(var12.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var7;
   }

   public Set<String> zrevrangeByScore(ChatSyncEntity forKey, double max, double min, int offset, int count) throws FusionException {
      throw new FusionException("Only implemented in RedisChatSyncPipelineStore for now");
   }

   public Set<Tuple> zrevrangeByScoreWithScores(ChatSyncEntity forKey, double max, double min, int offset, int count) throws FusionException {
      throw new FusionException("Only implemented in RedisChatSyncPipelineStore for now");
   }

   public Integer zcount(ChatSyncEntity entityKey, double min, double max) throws FusionException {
      Jedis instance = null;

      Integer var9;
      try {
         ChatSyncStats.getInstance().incrementRedisStats(entityKey, true);
         instance = this.getInstance(entityKey);
         String key;
         if (instance == null) {
            key = null;
            return key;
         }

         key = getRedisKey(entityKey);
         Long resultLong = instance.zcount(key, min, max);
         var9 = resultLong == null ? null : resultLong.intValue();
      } catch (Exception var14) {
         this.logError("zcount", var14);
         throw new FusionException(var14.getMessage());
      } finally {
         Redis.disconnect(instance, log.getLogger());
      }

      return var9;
   }

   public static List<Tuple> getTupledList(List<byte[]> membersWithScores) throws FusionException {
      try {
         ArrayList<Tuple> list = new ArrayList();
         Iterator iterator = membersWithScores.iterator();

         while(iterator.hasNext()) {
            list.add((Tuple)iterator.next());
         }

         return list;
      } catch (Exception var3) {
         log.error(var3);
         throw new FusionException(var3.getMessage());
      }
   }

   public Transaction multi(ChatSyncEntity forKey) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public void execTxn(ChatSyncEntity forKey) throws FusionException {
      throw new FusionException("Not implemented");
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(RedisChatSyncStore.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
      totalPipelineExceptions = new AtomicInteger(0);
   }
}
