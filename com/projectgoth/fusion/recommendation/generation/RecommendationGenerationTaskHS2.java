package com.projectgoth.fusion.recommendation.generation;

import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.db.fusiondb.DBUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.recommendation.RecommendationTransform;
import com.projectgoth.fusion.recommendation.RecommendationTransformParam;
import com.projectgoth.fusion.recommendation.delivery.Enums;
import com.projectgoth.fusion.recommendation.delivery.RecommendationDeliveryUtils;
import com.projectgoth.fusion.slice.RecommendationGenerationServiceStats;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class RecommendationGenerationTaskHS2 extends TimerTask {
   private static final Logger log = Logger.getLogger(RecommendationGenerationTaskHS2.class);
   private static final String EXTRACT_PARAMS_REGEX = "\\{(.*?)\\}";
   private static final Pattern EXTRACT_PARAMS_PATTERN = Pattern.compile("\\{(.*?)\\}");
   private static ConcurrentHashMap<Integer, Semaphore> transformToSemaphoreMap = new ConcurrentHashMap();
   private static AtomicLong totalJobs = new AtomicLong();
   private static AtomicLong successfulJobs = new AtomicLong();
   private static AtomicLong failedJobs = new AtomicLong();
   private static AtomicLong failedJobsDueToAlreadyRunning = new AtomicLong();
   private static AtomicLong totalRecommendationsGenerated = new AtomicLong();
   private static AtomicLong totalGenerationTimeSeconds = new AtomicLong();
   private static AtomicLong shortestGenerationTimeSeconds = new AtomicLong(Long.MAX_VALUE);
   private static AtomicLong longestGenerationTimeSeconds = new AtomicLong();
   private final int transformID;

   public RecommendationGenerationTaskHS2(int transformID) {
      this.transformID = transformID;
   }

   private boolean tryAcquireTransformSemaphore() {
      return this.getTransformSemaphore().tryAcquire();
   }

   private void releaseTransformSemaphore() {
      this.getTransformSemaphore().release();
   }

   private Semaphore getTransformSemaphore() {
      Semaphore semaphore = (Semaphore)transformToSemaphoreMap.get(this.transformID);
      if (null == semaphore) {
         semaphore = new Semaphore(1);
         transformToSemaphoreMap.putIfAbsent(this.transformID, semaphore);
         semaphore = (Semaphore)transformToSemaphoreMap.get(this.transformID);
      }

      return semaphore;
   }

   public void run() {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.GENERATION_SERVICE_VIA_INTERNAL_TIMER_ENABLED)) {
         log.warn("Not running RecommendationGenerationTask via internal timer since disabled in system property");
      } else {
         this.transform();
      }
   }

   public void transform() {
      totalJobs.incrementAndGet();
      if (!this.tryAcquireTransformSemaphore()) {
         log.warn("Another invocation of RecommendationGenerationTask for transform " + this.transformID + " is already in progress. Exiting...");
         failedJobsDueToAlreadyRunning.incrementAndGet();
      } else {
         try {
            RecommendationTransform transform = DAOFactory.getInstance().getRecommendationDAO().getRecommendationTransform(this.transformID);
            if (transform.getStatus() == RecommendationGenerationTaskHS2.TransformStatusEnum.DISABLED.value()) {
               log.info("Not starting RecommendationGenerationTaskHS2 for transformation id=" + this.transformID + " as status==DISABLED");
               return;
            }

            long startTime = System.currentTimeMillis();
            this.transformInner(transform);
            this.setPostTransformStats(startTime);
         } catch (Exception var8) {
            log.error("Exception running transformation with id=" + this.transformID + " e=" + var8, var8);
            failedJobs.incrementAndGet();
         } finally {
            this.releaseTransformSemaphore();
         }

      }
   }

   private void setPostTransformStats(long startTime) {
      long genTime = (System.currentTimeMillis() - startTime) / 1000L;
      totalGenerationTimeSeconds.addAndGet(genTime);
      RecommendationGenerationTaskHS2.CasHelper chLongest;
      if (genTime < shortestGenerationTimeSeconds.get()) {
         chLongest = new RecommendationGenerationTaskHS2.CasHelper(shortestGenerationTimeSeconds, genTime) {
            public boolean stillNeedsSetting(long currentValue) {
               return this.newValue < currentValue;
            }
         };
         if (!chLongest.compareAndSet()) {
            log.warn("Unable to update shortestGenerationTimeSeconds stat due to CAS collisions");
         }
      }

      if (genTime > longestGenerationTimeSeconds.get()) {
         chLongest = new RecommendationGenerationTaskHS2.CasHelper(longestGenerationTimeSeconds, genTime) {
            public boolean stillNeedsSetting(long currentValue) {
               return this.newValue > currentValue;
            }
         };
         if (!chLongest.compareAndSet()) {
            log.warn("Unable to update longestGenerationTimeSeconds stat due to CAS collisions");
         }
      }

      successfulJobs.incrementAndGet();
   }

   private void transformInner(RecommendationTransform transform) throws Exception {
      log.info("Starting RecommendationGenerationTaskHS2 for transformation id=" + this.transformID);
      log.info("Got transform=" + transform);
      String precompScript = transform.getPrecompiledScript();
      if (log.isDebugEnabled()) {
         log.debug("retrieved precompiled hive script=" + precompScript);
      }

      if (StringUtil.isBlank(precompScript)) {
         log.info("Exiting RecommendationGenerationTaskHS2 for transformation id=" + this.transformID + " as precompiled script missing");
      } else {
         String driver = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.HIVE2_JDBC_DRIVER_CLASS);
         Class.forName(driver);
         log.info("Instantiated Hive JDBC driver=" + driver + "ok");
         String jdbcURL = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.HIVE2_JDBC_URL);
         String jdbcUser = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.HIVE2_JDBC_USERNAME);
         String jdbcPassword = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.HIVE2_JDBC_PASSWORD);
         Connection hiveConn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            log.info("Getting Hive connection with url=" + jdbcURL);
            hiveConn = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPassword);
            log.info("Got hive connection=" + hiveConn);
            String sql = transform.getPrecompiledScript();
            log.info("sql=" + sql);
            String replacedSQL = sql.replaceAll("\\{(.*?)\\}", "?");
            log.info("replacedSQL=" + replacedSQL);
            ps = hiveConn.prepareStatement(replacedSQL);
            this.setParams(ps, sql);
            if (log.isDebugEnabled()) {
               log.debug("Executing precompiled script");
            }

            int rows = ps.executeUpdate();
            log.info("Transform " + this.transformID + ": hive_precompile_script generated " + rows + " rows");
            ps.clearParameters();
            rs = ps.executeQuery(transform.getRetrievalQuery());
            log.info("Transform " + this.transformID + ": hive_retrieval_query executed ok");
            storeResultsInRedis(transform, rs);
            log.info("Transform " + this.transformID + ": hive_retrieval_query got " + rs.getRow() + " rows");
         } finally {
            DBUtils.closeResource(rs, ps, hiveConn, log);
         }

      }
   }

   private void setParams(PreparedStatement ps, String query) throws Exception {
      log.info("Set params with ps=" + ps + " query=" + query);
      Map<String, RecommendationTransformParam> paramsMap = DAOFactory.getInstance().getRecommendationDAO().getRecommendationTransformParamsByName(this.transformID);
      new ArrayList();
      Matcher m = EXTRACT_PARAMS_PATTERN.matcher(query);
      int paramIndex = 1;

      while(m.find()) {
         String paramNameBraces = m.group();
         String paramName = paramNameBraces.substring(1, paramNameBraces.length() - 1);
         log.info("Setting param=" + paramName + " to value=" + ((RecommendationTransformParam)paramsMap.get(paramName)).getValue());
         ps.setString(paramIndex++, ((RecommendationTransformParam)paramsMap.get(paramName)).getValue());
      }

      log.info("Transform " + this.transformID + ": set " + (paramIndex - 1) + " params");
   }

   public static void storeResultsInRedis(RecommendationTransform transform, ResultSet rs) throws Exception {
      Enums.RecommendationTypeEnum recType = Enums.RecommendationTypeEnum.fromValue(transform.getType());
      Enums.RecommendationTargetEnum target;
      if (transform.getDomain().equals("U")) {
         target = Enums.RecommendationTargetEnum.INDIVIDUAL;
      } else {
         if (!transform.getDomain().equals("CO")) {
            throw new Exception("Transform ID=" + transform.getID() + " has invalid domain=" + transform.getDomain());
         }

         target = Enums.RecommendationTargetEnum.COUNTRY;
      }

      HashMap shardPipelines = new HashMap();

      try {
         while(rs.next()) {
            String recommendation = rs.getString(1);
            float weight = rs.getFloat(2);
            int entityID = rs.getInt(3);
            String shardDirKey = Redis.getShardKeyForEntityID(target.getRedisKeySpace(), entityID);
            String shardID = Redis.getShardID(shardDirKey, (Jedis)null);
            if (null == shardID) {
               shardID = Redis.getNewShardID(shardDirKey, (Jedis)null);
               if (log.isDebugEnabled()) {
                  log.debug("assigned new shardID=" + shardID + " for entityID=" + entityID);
               }
            }

            if (log.isDebugEnabled()) {
               log.debug("entityID=" + entityID + ": shardDirKey=" + shardDirKey + " shardID=" + shardID);
            }

            ShardPipeline pipeline = (ShardPipeline)shardPipelines.get(shardID);
            if (null == pipeline) {
               Jedis shard = Redis.getMasterInstanceForEntityID(target.getRedisKeySpace(), entityID);
               pipeline = new ShardPipeline(shard);
               shardPipelines.put(shardID, pipeline);
            }

            String key = RecommendationDeliveryUtils.generateRecommendationKey(recType, target, entityID, transform.getSubType());
            pipeline.getPipeline().zadd(key, (double)weight, recommendation);
            pipeline.getPipeline().expire(key, (int)transform.getExpiry());
            pipeline.syncIfNeeded(2);
         }

         log.info("Doing final sync of " + shardPipelines.size() + " pipelines");
         Iterator i$ = shardPipelines.values().iterator();

         while(i$.hasNext()) {
            ShardPipeline pipeline = (ShardPipeline)i$.next();
            pipeline.syncFinal();
         }

         totalRecommendationsGenerated.addAndGet((long)rs.getRow());
      } catch (Exception var18) {
         log.error("Exception whilst storing recommendation results in redis: e=" + var18, var18);
         throw var18;
      } finally {
         Iterator i$ = shardPipelines.values().iterator();

         while(i$.hasNext()) {
            ShardPipeline shardPipeline = (ShardPipeline)i$.next();
            shardPipeline.disconnect();
         }

      }
   }

   public static void getStats(RecommendationGenerationServiceStats stats) {
      stats.totalJobs = totalJobs.get();
      stats.successfulJobs = successfulJobs.get();
      stats.failedJobs = failedJobs.get();
      stats.failedJobsDueToAlreadyRunning = failedJobsDueToAlreadyRunning.get();
      stats.totalRecommendationsGenerated = totalRecommendationsGenerated.get();
      stats.totalPipelinesUsed = ShardPipeline.getTotalPipelinesUsed();
      stats.totalGenerationTimeSeconds = totalGenerationTimeSeconds.get();
      stats.shortestGenerationTimeSeconds = shortestGenerationTimeSeconds.get();
      stats.longestGenerationTimeSeconds = longestGenerationTimeSeconds.get();
   }

   private abstract class CasHelper {
      protected AtomicLong target;
      protected long newValue;

      public CasHelper(AtomicLong target, long newValue) {
         this.target = target;
         this.newValue = newValue;
      }

      public abstract boolean stillNeedsSetting(long var1);

      public boolean compareAndSet() {
         long casStart = System.currentTimeMillis();

         boolean success;
         do {
            long current = this.target.get();
            if (!this.stillNeedsSetting(current)) {
               return false;
            }

            success = this.target.compareAndSet(current, this.newValue);
         } while(!success && System.currentTimeMillis() - casStart < 5000L);

         return success;
      }
   }

   public static enum TransformStatusEnum {
      DISABLED(0),
      ENABLED(1);

      final int status;

      private TransformStatusEnum(int status) {
         this.status = status;
      }

      public int value() {
         return this.status;
      }
   }
}
