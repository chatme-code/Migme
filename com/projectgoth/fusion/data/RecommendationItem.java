package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.Serializable;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class RecommendationItem implements Serializable, Comparable<RecommendationItem> {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecommendationItem.class));
   private String value;
   private int score;
   private String reason;
   private String source;
   private boolean feature;
   private String description;
   private String redisLocation;

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public RecommendationItem(String recommendation, int score) {
      this.value = recommendation;
      this.score = score;
   }

   public RecommendationItem() {
   }

   public RecommendationItem(Tuple tuple) {
      this.value = tuple.getElement();
      this.score = (int)tuple.getScore();
   }

   public RecommendationItem(Tuple tuple, Jedis redisInstance, String redisKey) {
      this(tuple);

      try {
         if (redisInstance != null && redisInstance.getClient() != null) {
            this.redisLocation = redisInstance.getClient().getHost() + ":" + redisInstance.getClient().getPort() + "//" + redisKey;
         }
      } catch (Exception var5) {
         log.warn("Unable to set RecommendationItem.redisLocation: e=" + var5, var5);
      }

   }

   public RecommendationItem(Tuple tuple, String reason, String source) {
      this.value = tuple.getElement();
      this.score = (int)tuple.getScore();
      this.reason = reason;
      this.source = source;
   }

   public String getReason() {
      return this.reason;
   }

   public void setReason(String reason) {
      this.reason = reason;
   }

   public String getSource() {
      return this.source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   public int getScore() {
      return this.score;
   }

   public void setScore(int score) {
      this.score = score;
   }

   public boolean isFeature() {
      return this.feature;
   }

   public void setFeature(boolean feature) {
      this.feature = feature;
   }

   public int compareTo(RecommendationItem other) {
      return (new Integer(this.score)).compareTo(new Integer(other.getScore()));
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("[value=");
      builder.append(this.value);
      builder.append(", score=");
      builder.append(this.score);
      builder.append("]");
      return builder.toString();
   }

   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (this == o) {
         return true;
      } else {
         return !(o instanceof RecommendationItem) ? false : this.value.equals(((RecommendationItem)o).getValue());
      }
   }

   public int hashCode() {
      return this.value == null ? -1 : this.value.hashCode();
   }

   public String getRedisLocation() {
      return this.redisLocation;
   }
}
