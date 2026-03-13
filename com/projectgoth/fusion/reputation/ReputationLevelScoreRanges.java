package com.projectgoth.fusion.reputation;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.jdbc.ConnectionCreator;
import com.projectgoth.fusion.common.jdbc.JdbcHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NavigableMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

public class ReputationLevelScoreRanges {
   private static final Logger log = Logger.getLogger(ReputationLevelScoreRanges.class);
   private static final int ROW_TYPE_LOWER_BOUND = 0;
   private static final int ROW_TYPE_UPPER_BOUND = 1;
   public static final int MIN_SCORE = 0;
   public static final int MAX_SCORE = Integer.MAX_VALUE;
   private final NavigableMap<Integer, ReputationLevelScoreRanges.LevelScoreRangeEntry> levelScoreRangeEntryCache = new ConcurrentSkipListMap();
   private final ReputationLevelScoreRanges.CacheStatsHolder cacheStatsHolder = new ReputationLevelScoreRanges.CacheStatsHolder();
   public static volatile ReputationLevelScoreRanges instanceOverride;

   protected Long getEntryTTLMillis() {
      return SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Reputation.SCORE_TO_LEVEL_CACHE_ENTRY_TTL_MILLIS);
   }

   protected ReputationLevelScoreRanges.LevelScoreRangeEntry getScoreToLevelRangeFromDB(Connection conn, final int score) throws Exception {
      String query = "select scoreRange.rowtype,scoreRange.score,r3.level from (  select 0 as rowtype ,max(r1.score) as score from reputationscoretolevel r1  where r1.score <= ?  union all  select 1 as rowtype ,min(r2.score) as score from reputationscoretolevel r2  where ? < r2.score) scoreRange join reputationscoretolevel r3 on scoreRange.score = r3.score";
      if (log.isDebugEnabled()) {
         log.debug("Query=[select scoreRange.rowtype,scoreRange.score,r3.level from (  select 0 as rowtype ,max(r1.score) as score from reputationscoretolevel r1  where r1.score <= ?  union all  select 1 as rowtype ,min(r2.score) as score from reputationscoretolevel r2  where ? < r2.score) scoreRange join reputationscoretolevel r3 on scoreRange.score = r3.score] score=[" + score + "]");
      }

      final ReputationLevelScoreRanges.LevelScoreRangeEntry[] result = new ReputationLevelScoreRanges.LevelScoreRangeEntry[]{null};
      JdbcHelper.execQuery((Connection)conn, "select scoreRange.rowtype,scoreRange.score,r3.level from (  select 0 as rowtype ,max(r1.score) as score from reputationscoretolevel r1  where r1.score <= ?  union all  select 1 as rowtype ,min(r2.score) as score from reputationscoretolevel r2  where ? < r2.score) scoreRange join reputationscoretolevel r3 on scoreRange.score = r3.score", new JdbcHelper.QueryHandlerAdapter() {
         public void setParamValues(PreparedStatement preparedStatement) throws SQLException {
            if (score >= 0) {
               preparedStatement.setInt(1, score);
               preparedStatement.setInt(2, score);
            } else {
               preparedStatement.setInt(1, 0);
               preparedStatement.setInt(2, 0);
            }

         }

         public void handle(ResultSet rs) throws SQLException {
            int scoreStarts = 0;
            int scoreEnds = Integer.MAX_VALUE;
            int currentLevel = 1;

            while(rs.next()) {
               int rowType = rs.getInt("rowtype");
               int scorex = rs.getInt("score");
               int level = rs.getInt("level");
               switch(rowType) {
               case 0:
                  scoreStarts = scorex;
                  currentLevel = level;
                  break;
               case 1:
                  scoreEnds = scorex;
               }
            }

            long now = System.currentTimeMillis();
            result[0] = new ReputationLevelScoreRanges.LevelScoreRangeEntry(scoreStarts, scoreEnds, currentLevel, now);
         }
      });
      return result[0];
   }

   private ReputationLevelScoreRanges.LevelScoreRangeEntry getScoreFromDBThenCacheIt(Connection conn, int score) throws Exception {
      ReputationLevelScoreRanges.LevelScoreRangeEntry dbScoreEntry = this.getScoreToLevelRangeFromDB(conn, score);
      if (dbScoreEntry != null) {
         this.levelScoreRangeEntryCache.put(dbScoreEntry.scoreStart, dbScoreEntry);
      }

      return dbScoreEntry;
   }

   public ReputationLevelScoreRanges.LevelScoreRangeEntry getLevelScoreRange(int requestedScore, Connection conn) throws Exception {
      int score = requestedScore <= 0 ? 0 : requestedScore;
      Entry<Integer, ReputationLevelScoreRanges.LevelScoreRangeEntry> e = this.levelScoreRangeEntryCache.floorEntry(score);
      if (e == null) {
         this.cacheStatsHolder.missedNonExist();
         return this.getScoreFromDBThenCacheIt(conn, score);
      } else {
         long now = System.currentTimeMillis();
         ReputationLevelScoreRanges.LevelScoreRangeEntry se = (ReputationLevelScoreRanges.LevelScoreRangeEntry)e.getValue();
         if (score < se.scoreEnd) {
            if (now - se.loadTime <= this.getEntryTTLMillis()) {
               this.cacheStatsHolder.hit();
               return se;
            } else {
               this.cacheStatsHolder.missedExpired();
               return this.getScoreFromDBThenCacheIt(conn, score);
            }
         } else {
            this.cacheStatsHolder.missedNonExist();
            return this.getScoreFromDBThenCacheIt(conn, score);
         }
      }
   }

   public ReputationLevelScoreRanges.LevelScoreRangeEntry getLevelScoreRange(int score, ConnectionCreator connectionCreator) throws Exception {
      Connection conn = connectionCreator.create();

      ReputationLevelScoreRanges.LevelScoreRangeEntry var4;
      try {
         var4 = this.getLevelScoreRange(score, conn);
      } finally {
         conn.close();
      }

      return var4;
   }

   public void invalidateCache() {
      this.levelScoreRangeEntryCache.clear();
   }

   public void resetCacheStats() {
      this.cacheStatsHolder.reset();
   }

   public ReputationLevelScoreRanges.CacheStats getStats() {
      return this.cacheStatsHolder.getSnapshot();
   }

   public static final ReputationLevelScoreRanges getInstance() {
      return instanceOverride != null ? instanceOverride : ReputationLevelScoreRanges.Instance.defaultInstance;
   }

   public static void setInstance(ReputationLevelScoreRanges instanceOverride) {
      ReputationLevelScoreRanges.instanceOverride = instanceOverride;
   }

   protected NavigableMap<Integer, ReputationLevelScoreRanges.LevelScoreRangeEntry> getLevelScoreRangeEntryCache() {
      return this.levelScoreRangeEntryCache;
   }

   public static class Instance {
      static final ReputationLevelScoreRanges defaultInstance = new ReputationLevelScoreRanges();
   }

   private static class CacheStatsHolder {
      private final AtomicLong hitCount;
      private final AtomicLong missedExpiredCount;
      private final AtomicLong missedNonExistCount;

      private CacheStatsHolder() {
         this.hitCount = new AtomicLong(0L);
         this.missedExpiredCount = new AtomicLong(0L);
         this.missedNonExistCount = new AtomicLong(0L);
      }

      public void reset() {
         this.hitCount.set(0L);
         this.missedExpiredCount.set(0L);
         this.missedNonExistCount.set(0L);
      }

      public void hit() {
         this.hitCount.incrementAndGet();
      }

      public void missedExpired() {
         this.missedExpiredCount.incrementAndGet();
      }

      public void missedNonExist() {
         this.missedNonExistCount.incrementAndGet();
      }

      public ReputationLevelScoreRanges.CacheStats getSnapshot() {
         return new ReputationLevelScoreRanges.CacheStats(this.hitCount.get(), this.missedExpiredCount.get(), this.missedNonExistCount.get());
      }

      // $FF: synthetic method
      CacheStatsHolder(Object x0) {
         this();
      }
   }

   public static class CacheStats {
      public final long hitCount;
      public final long missedExpired;
      public final long missedNonExist;

      public CacheStats(long hitCount, long missedExpired, long missedNonExist) {
         this.hitCount = hitCount;
         this.missedExpired = missedExpired;
         this.missedNonExist = missedNonExist;
      }
   }

   public static class LevelScoreRangeEntry {
      public final int scoreStart;
      public final int scoreEnd;
      public final int level;
      public final long loadTime;

      public LevelScoreRangeEntry(int scoreStart, int scoreEnd, int level, long loadTime) {
         this.scoreStart = scoreStart;
         this.scoreEnd = scoreEnd;
         this.level = level;
         this.loadTime = loadTime;
      }
   }
}
