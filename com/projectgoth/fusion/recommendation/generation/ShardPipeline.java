package com.projectgoth.fusion.recommendation.generation;

import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class ShardPipeline {
   private static final Logger log = Logger.getLogger(ShardPipeline.class);
   private static final AtomicLong totalPipelinesUsed = new AtomicLong();
   final int MAX_PIPELINE_LENGTH;
   private final Jedis shard;
   private Pipeline pipeline;
   private int length;

   public ShardPipeline(Jedis shard) {
      this.MAX_PIPELINE_LENGTH = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RGS_MAX_PIPELINE_LENGTH);
      this.shard = shard;
      this.pipeline = shard.pipelined();
      this.length = 0;
      totalPipelinesUsed.incrementAndGet();
   }

   public Pipeline getPipeline() {
      return this.pipeline;
   }

   public void disconnect() {
      Redis.disconnect(this.shard, log);
   }

   public void syncIfNeeded(int lengthIncrement) {
      this.length += lengthIncrement;
      if (this.length > this.MAX_PIPELINE_LENGTH) {
         log.info("Doing intermediate sync of pipeline on shard =" + this.shard);
         this.sync();
      }

   }

   public void sync() {
      this.pipeline.sync();
      this.pipeline = this.shard.pipelined();
      this.length = 0;
      totalPipelinesUsed.incrementAndGet();
   }

   public void syncFinal() {
      this.pipeline.sync();
   }

   public static long getTotalPipelinesUsed() {
      return totalPipelinesUsed.get();
   }
}
