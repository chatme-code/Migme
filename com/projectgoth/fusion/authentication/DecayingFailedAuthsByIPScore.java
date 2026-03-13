package com.projectgoth.fusion.authentication;

import com.migme.common.DecayingScoreData;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.FusionException;
import java.util.List;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class DecayingFailedAuthsByIPScore extends DecayingScoreData {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DecayingFailedAuthsByIPScore.class));

   public DecayingFailedAuthsByIPScore() {
   }

   public DecayingFailedAuthsByIPScore(double score) throws Exception {
      super(score);
   }

   public DecayingFailedAuthsByIPScore(String json) throws Exception {
      this.loadFromJson(json);
   }

   public double getHalfLifeMillis() {
      return (double)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FAILED_AUTHS_PER_IP_HALF_LIFE_SECS) * 1000.0D;
   }

   protected double getScoreScale() {
      return this.getHalfLifeMillis() / Math.log(2.0D);
   }

   protected double getInverseHalfLife() {
      return 1.0D / this.getHalfLifeMillis();
   }

   public static DecayingFailedAuthsByIPScore load(String clientIP) throws Exception {
      Jedis shard = null;
      String json = null;

      DecayingFailedAuthsByIPScore var8;
      try {
         shard = Redis.getSlaveInstanceForEntityID(Redis.KeySpace.IP_ADDRESS, clientIP.hashCode());
         if (null != shard) {
            String key = Redis.getFailedAuthsPerIPKey(clientIP);
            json = shard.get(key);
         }

         if (null == json) {
            var8 = new DecayingFailedAuthsByIPScore(0.0D);
            return var8;
         }

         var8 = new DecayingFailedAuthsByIPScore(json);
      } finally {
         Redis.disconnect(shard, log);
      }

      return var8;
   }

   public static boolean incrementScore(String clientIP, double scoreIncrement) throws FusionException {
      Jedis shard = null;

      try {
         shard = Redis.getMasterInstanceForEntityID(Redis.KeySpace.IP_ADDRESS, clientIP.hashCode());
         boolean timedOut = false;
         long startTime = System.currentTimeMillis();
         long timeout = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FAILED_AUTHS_PER_IP_CAS_TIMEOUT_MILLIS);

         boolean success;
         do {
            String key = Redis.getFailedAuthsPerIPKey(clientIP);
            shard.watch(new String[]{key});
            String json = shard.get(key);
            DecayingFailedAuthsByIPScore score;
            if (json != null) {
               score = new DecayingFailedAuthsByIPScore(json);
            } else {
               score = new DecayingFailedAuthsByIPScore();
            }

            score.addScore(scoreIncrement);
            Transaction t = shard.multi();
            t.set(key, score.toJson());
            if ((Boolean)SystemPropertyEntities.Temp.Cache.se445FailedAuthsExpiryFixEnabled.getValue()) {
               long millisUntilHalfScore = (long)score.getTimeForUnitScore() - System.currentTimeMillis() + (long)score.getHalfLifeMillis();
               t.expire(key, (int)(millisUntilHalfScore / 1000L));
            } else {
               t.expire(key, (int)(score.getHalfLifeMillis() * 2.0D * 1000.0D));
            }

            List<Object> results = t.exec();
            success = results != null;
            if (!success) {
               timedOut = System.currentTimeMillis() - startTime > timeout;
               if (timedOut) {
                  log.warn("CAS operation on key=" + key + " timed out");
               }
            }
         } while(!success && !timedOut);

         boolean var22 = success;
         return var22;
      } catch (Exception var20) {
         log.error("Exception loading failed auths for [IP:" + clientIP + "] from redis, shard=" + shard + " e=" + var20, var20);
         throw new FusionException(var20.getMessage());
      } finally {
         Redis.disconnect(shard, log);
      }
   }

   public long getLoginResponseResultDelayMillis(int scoreThreshold) {
      int delayUnit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FAILED_AUTHS_PER_IP_PACKET_DELAY_UNIT_SECS);
      double delayPower = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FAILED_AUTHS_PER_IP_PACKET_DELAY_POWER);
      double excess = this.score - (double)scoreThreshold;
      return (long)((double)delayUnit + Math.pow(excess, delayPower) * (double)delayUnit) * 1000L;
   }
}
