package com.projectgoth.fusion.common;

import com.projectgoth.fusion.chatsync.MessageStatusEventKey;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.jedis.NonPooledJedis;
import com.projectgoth.fusion.slice.FusionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisException;

public class Redis {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Redis.class));
   private static final String PROPERTIES_FILENAME = "redis.properties";
   private static Properties properties;
   private static String shardDirectoryMasterServer;
   private static String shardDirectorySlaveServer;
   private static String leaderboardsMasterServer;
   private static String leaderboardsSlaveServer;
   private static String queuesMasterServer;
   private static String queuesSlaveServer;
   private static String gamesSlaveServer;
   private static String gamesMasterServer;
   private static String webResourcesSlaveServer;
   private static String webResourcesMasterServer;
   private static SecureRandom random = new SecureRandom();
   private static Redis.ShardSettingsCache shardSettingsCache;
   private static LRUCache<String, String> userShardMappingCache;
   private static int userShardMappingCacheMaxSize;
   private static int userShardMappingCacheExpiryInSeconds;
   private static SharedRedisConnection sharedDirectorySlaveConn;
   private static SharedRedisConnection sharedDirectoryMasterConn;
   public static final String KEYSPACE_SEPARATOR = ":";
   public static final String PROTOCOL_DOT_CHARSET = "UTF-8";

   public static int getUserShardMappingCacheMaxSize() {
      return userShardMappingCacheMaxSize;
   }

   public static int getUserShardMappingCacheExpiryInSeconds() {
      return userShardMappingCacheExpiryInSeconds;
   }

   static String getShardKeyForUserID(int userID) {
      return Redis.KeySpaceShardDirectory.REDIS_USER_ID.append(userID);
   }

   public static String getShardKeyForEntityID(Redis.KeySpace entity, int entityId) {
      return entity.getDirectoryKeySpace().append(entityId);
   }

   public static String getUserDetailKey(int userId) {
      return Redis.KeySpace.USER.append(userId);
   }

   public static String getOfflineMessageKey(int recipientID) {
      return getOfflineMessageKey(recipientID, new Date());
   }

   public static String getOfflineMessageKey(int recipientID, Date date) {
      String sDate = ((SimpleDateFormat)DateTimeUtils.OFFLINE_MSG_KEY_DATE_FORMAT.get()).format(date);
      return Redis.KeySpace.USER_ENTITY.toString() + recipientID + ":OLMSG:" + sDate;
   }

   public static String getConversationMessageKey(String conversationID) {
      return Redis.KeySpace.CONVERSATION_ENTITY.toString() + conversationID + ":M";
   }

   public static String getMessageStatusEventKey(MessageStatusEventKey mseKey) throws FusionException {
      if (mseKey.getMessageDestinationType() != MessageDestinationData.TypeEnum.INDIVIDUAL) {
         throw new FusionException("Tried to retrieve message status events for chatID=" + mseKey.getMessageDestination() + " chat type=" + mseKey.getMessageDestinationType() + " : Only supported for private messaging");
      } else {
         return Redis.KeySpace.CONVERSATION_ENTITY.toString() + mseKey.getKey() + ":E";
      }
   }

   public static String getConversationDefinitionKey(String conversationID) {
      return Redis.KeySpace.CONVERSATION_ENTITY.toString() + conversationID + ":D";
   }

   public static String getConversationParticipantsKey(String conversationID) {
      return Redis.KeySpace.CONVERSATION_ENTITY.toString() + conversationID + ":P";
   }

   public static String getOldChatListsKey(int userID) {
      if (log.isDebugEnabled()) {
         log.debug("getOldChatListsKey with userid=" + userID);
      }

      return Redis.KeySpace.USER_ENTITY.toString() + userID + ":CLO";
   }

   public static String getCurrentChatListKey(int userID) {
      if (log.isDebugEnabled()) {
         log.debug("getCurrentChatListKey with userid=" + userID);
      }

      return Redis.KeySpace.USER_ENTITY.toString() + userID + ":CLC";
   }

   public static String getChatListVersionKey(int userID) {
      if (log.isDebugEnabled()) {
         log.debug("getChatListVersionKey with userid=" + userID);
      }

      return Redis.KeySpace.USER_ENTITY.toString() + userID + ":CLV";
   }

   public static String getFailedAuthsPerIPKey(String ip) {
      return Redis.KeySpace.IP_ADDRESS.toString() + ip + ":F";
   }

   public static String getRecentWebOnlySidsKey(int userID) {
      if (log.isDebugEnabled()) {
         log.debug("getRecentSidsKey with userid=" + userID);
      }

      return Redis.KeySpace.USER_ENTITY.toString() + userID + ":RS";
   }

   public static Jedis getMasterInstanceForUserID(int userID) throws Exception {
      if (userID <= 0) {
         throw new Exception("Invalid User ID");
      } else {
         return getInstance(getShardKeyForUserID(userID), Redis.InstanceType.MASTER);
      }
   }

   public static Jedis getSlaveInstanceForUserID(int userID) throws Exception {
      if (userID <= 0) {
         throw new Exception("Invalid User ID");
      } else {
         return getInstance(getShardKeyForUserID(userID), Redis.InstanceType.SLAVE);
      }
   }

   public static Jedis getMasterInstanceForEntityID(Redis.KeySpace entity, int entityID) throws Exception {
      return getInstance(getShardKeyForEntityID(entity, entityID), Redis.InstanceType.MASTER);
   }

   public static Jedis getSlaveInstanceForEntityID(Redis.KeySpace entity, int entityID) throws Exception {
      return getInstance(getShardKeyForEntityID(entity, entityID), Redis.InstanceType.SLAVE);
   }

   public static Jedis getLeaderboardsMasterInstance() throws Exception {
      return getNonPooledRedisConnection(leaderboardsMasterServer);
   }

   public static Jedis getLeaderboardsSlaveInstance() throws Exception {
      return getNonPooledRedisConnection(leaderboardsSlaveServer);
   }

   public static Jedis getGamesMasterInstance() throws Exception {
      return getNonPooledRedisConnection(gamesMasterServer);
   }

   public static Jedis getGamesSlaveInstance() throws Exception {
      return getNonPooledRedisConnection(gamesSlaveServer);
   }

   public static Jedis getQueuesMasterInstance() throws Exception {
      return getNonPooledRedisConnection(queuesMasterServer);
   }

   public static Jedis getQueuesSlaveInstance() throws Exception {
      return getNonPooledRedisConnection(queuesSlaveServer);
   }

   public static Jedis getWebResourcesMasterInstance() throws Exception {
      return getNonPooledRedisConnection(webResourcesMasterServer);
   }

   public static Jedis getWebResourcesSlaveInstance() throws Exception {
      return getNonPooledRedisConnection(webResourcesSlaveServer);
   }

   public static String getShardID(String shardDirectoryKey, Jedis shardDirectoryConn) throws Exception {
      String shardID = (String)userShardMappingCache.get(shardDirectoryKey);
      if (StringUtil.isBlank(shardID)) {
         Jedis shardDirectorySlave;
         if (shardDirectoryConn == null) {
            try {
               shardDirectorySlave = getRedisConnection(shardDirectorySlaveServer);
            } catch (Exception var10) {
               log.error("Unable to connect to Redis shard directory slave server at " + shardDirectorySlaveServer, var10);
               throw new Exception("Unable to connect to shard directory slave");
            }
         } else {
            shardDirectorySlave = shardDirectoryConn;
         }

         try {
            shardID = shardDirectorySlave.get(shardDirectoryKey);
         } catch (Exception var9) {
            log.error("Unable to get shard ID or shard Instance: " + var9.getLocalizedMessage());
            throw var9;
         } finally {
            if (shardDirectoryConn == null) {
               disconnect(shardDirectorySlave, log);
            }

         }
      }

      return shardID;
   }

   static Jedis getInstance(String shardDirectoryKey, Redis.InstanceType instanceType) throws Exception {
      String shardID;
      Jedis sharedConn;
      long timeoutInMs;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SHARE_REDIS_DIRECTORY_SLAVE_CONNECTION_ENABLED)) {
         sharedConn = null;

         try {
            timeoutInMs = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SHARE_REDIS_DIRECTORY_SLAVE_CONNECTION_WAIT_IN_MS);
            sharedConn = sharedDirectorySlaveConn.getConnection(timeoutInMs);
            shardID = getShardID(shardDirectoryKey, sharedConn);
         } finally {
            if (sharedConn != null) {
               sharedDirectorySlaveConn.returnConnection();
            }

         }
      } else {
         shardID = getShardID(shardDirectoryKey, (Jedis)null);
      }

      String shardServer;
      if (shardID != null) {
         if (instanceType == Redis.InstanceType.MASTER) {
            shardServer = shardSettingsCache.getMasterServerForShardID(shardID);
         } else {
            shardServer = shardSettingsCache.getSlaveServerForShardID(shardID);
         }
      } else {
         if (instanceType != Redis.InstanceType.MASTER) {
            return null;
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SHARE_REDIS_DIRECTORY_MASTER_CONNECTION_ENABLED)) {
            sharedConn = null;

            try {
               timeoutInMs = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SHARE_REDIS_DIRECTORY_MASTER_CONNECTION_WAIT_IN_MS);
               sharedConn = sharedDirectoryMasterConn.getConnection(timeoutInMs);
               shardID = getNewShardID(shardDirectoryKey, sharedConn);
            } finally {
               if (sharedConn != null) {
                  sharedDirectoryMasterConn.returnConnection();
               }

            }
         } else {
            shardID = getNewShardID(shardDirectoryKey, (Jedis)null);
         }

         shardServer = shardSettingsCache.getMasterServerForShardID(shardID);
      }

      if (shardServer == null) {
         throw new Exception("Configuration for shard " + shardID + ":" + instanceType.value() + " not found");
      } else {
         if (!StringUtil.isBlank(shardID)) {
            userShardMappingCache.put(shardDirectoryKey, shardID);
         }

         return getRedisConnection(shardServer);
      }
   }

   public static String getNewShardID(String shardDirectoryKey, Jedis shardDirectoryConn) throws Exception {
      String lockID = "get_new_shard:" + shardDirectoryKey;
      RedisShardAssignmentMode shardAssignmentMode = (RedisShardAssignmentMode)SystemPropertyEntities.Temp.Cache.se426ShardAssignmentMode.getValue();
      if (shardAssignmentMode.isUsesDistLock()) {
         MemCachedDistributedLock.getDistributedLock(lockID);
      }

      Jedis shardDirectoryMaster;
      if (shardDirectoryConn == null) {
         try {
            shardDirectoryMaster = getRedisConnection(shardDirectoryMasterServer);
         } catch (Exception var21) {
            log.error("Unable to connect to Redis shard directory master server at " + shardDirectoryMasterServer, var21);
            if (shardAssignmentMode.isUsesDistLock()) {
               MemCachedDistributedLock.releaseDistributedLock(lockID);
            }

            throw new Exception("Unable to connect to to shard directory master");
         }
      } else {
         shardDirectoryMaster = shardDirectoryConn;
      }

      String var6;
      try {
         String shardID = shardDirectoryMaster.get(shardDirectoryKey);
         if (shardID == null) {
            List<Integer> shardWeights = shardSettingsCache.getShardWeights();
            int r = random.nextInt(shardSettingsCache.getSumOfShardWeights()) + 1;
            int offset = 0;

            for(int i = 0; i < shardWeights.size(); ++i) {
               offset += (Integer)shardWeights.get(i);
               if (r <= offset) {
                  shardID = Integer.toString(i + 1);
                  if (shardAssignmentMode.isUsesSetNXAndGetPipeline()) {
                     shardID = tryAssignAndGetShard(shardDirectoryMaster, shardDirectoryKey, shardID);
                  } else {
                     shardDirectoryMaster.set(shardDirectoryKey, shardID);
                  }

                  String var10 = shardID;
                  return var10;
               }
            }

            log.error("getNewShardID(" + shardDirectoryKey + ") could not locate a shard");
            throw new Exception("Unable to assign a new shard ID");
         }

         var6 = shardID;
      } catch (Exception var22) {
         throw var22;
      } finally {
         if (shardDirectoryConn == null) {
            disconnect(shardDirectoryMaster, log);
         }

         try {
            if (shardAssignmentMode.isUsesDistLock()) {
               MemCachedDistributedLock.releaseDistributedLock(lockID);
            }
         } catch (Exception var20) {
            log.warn("Unable to release distributed lock: " + var20.getLocalizedMessage());
         }

      }

      return var6;
   }

   private static String tryAssignAndGetShard(Jedis shardDirectoryMaster, String shardDirectoryKey, String shardID) throws Exception {
      Pipeline pipeline = shardDirectoryMaster.pipelined();
      pipeline.setnx(shardDirectoryKey, shardID);
      Response<String> actualShardIDResponse = pipeline.get(shardDirectoryKey);
      pipeline.sync();
      String storedShardID = (String)actualShardIDResponse.get();
      if (storedShardID == null) {
         throw new Exception("getNewShardID(" + shardDirectoryKey + ") Pipeline not returning the actual shard stored in the master directory");
      } else {
         return storedShardID;
      }
   }

   private static synchronized void loadProperties() throws Exception {
      properties = new Properties();
      String propertiesLocation = ConfigUtils.getConfigDirectory() + "redis.properties";
      log.info("Loading Redis configuration file " + propertiesLocation);

      try {
         InputStream inputStream = new FileInputStream(new File(propertiesLocation));
         properties.load(inputStream);
         shardDirectoryMasterServer = loadServerProperty("shardDirectory.master", "Shard directory master");
         shardDirectorySlaveServer = loadServerProperty("shardDirectory.slave", "Shard directory slave");
         leaderboardsMasterServer = loadServerProperty("leaderboards.master", "Leaderboards master");
         leaderboardsSlaveServer = loadServerProperty("leaderboards.slave", "Leaderboards slave");
         gamesMasterServer = loadServerProperty("games.master", "Games master");
         gamesSlaveServer = loadServerProperty("games.slave", "Games slave");
         queuesMasterServer = loadServerProperty("queues.master", "Queues master");
         queuesSlaveServer = loadServerProperty("queues.slave", "Queues slave");
         webResourcesMasterServer = loadServerProperty("webresources.master", "Web resources master");
         webResourcesSlaveServer = loadServerProperty("webresources.slave", "Web resources slave");
         userShardMappingCacheMaxSize = StringUtil.toIntOrDefault(properties.getProperty("cache.usershard.maxsize"), 5000);
         userShardMappingCacheExpiryInSeconds = StringUtil.toIntOrDefault(properties.getProperty("cache.usershard.expiry.seconds"), 0);
         log.info("Redis configuration successfully loaded");
      } catch (Exception var2) {
         log.error("Unable to load Redis configuration file: " + var2.getMessage());
      }

   }

   private static String loadServerProperty(String propName, String description) throws Exception {
      String server = properties.getProperty(propName);
      if (server == null) {
         throw new Exception(propName + " not specified");
      } else {
         log.info(description + ": " + server);
         return server;
      }
   }

   static Jedis getRedisConnection(String server) throws Exception {
      int colonPos = server.indexOf(58);
      String host = server.substring(0, colonPos);
      int port = Integer.parseInt(server.substring(colonPos + 1, server.length()));
      Jedis jedis = RedisConnectionManager.getInstance().getConnection(host, port);
      return jedis;
   }

   static Jedis getNonPooledRedisConnection(String server) throws Exception {
      int colonPos = server.indexOf(58);
      String host = server.substring(0, colonPos);
      int port = Integer.parseInt(server.substring(colonPos + 1, server.length()));
      NonPooledJedis jedis = new NonPooledJedis(host, port, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.JedisPoolConfig.JEDIS_NON_POOL_SOCKET_TIMEOUT));
      jedis.connect();
      return jedis;
   }

   public static void disconnect(Jedis connection, Logger callerLog) {
      if (connection != null) {
         try {
            if (connection instanceof NonPooledJedis) {
               connection.disconnect();
            } else {
               RedisConnectionManager.getInstance().closeConnection(connection);
            }
         } catch (Exception var4) {
            String msg = "Failed to close Redis connection, reason: " + var4;
            if (callerLog != null) {
               callerLog.error(msg, var4);
            } else {
               log.error(msg, var4);
            }
         }
      }

   }

   public static void appendToListAndTrim(Jedis redisConnection, String key, String value, int maxElements) throws Exception {
      redisConnection.rpush(key, new String[]{value});
      redisConnection.ltrim(key, (long)(-maxElements), -1L);
   }

   public static void addToSortedSetWithTimestampAndTrimByAge(Jedis redisConnection, String key, String value, int maxAgeSeconds, boolean setExpiry) throws Exception {
      long nowInSeconds = System.currentTimeMillis() / 1000L;
      log.debug(String.format("addToSortedSetWithTimestampAndTrimByAge called with key[%s] value [%s] maxAgeSeconds[%d] nowInSeconds[%d]", key, value, maxAgeSeconds, nowInSeconds));
      redisConnection.zremrangeByScore(key, -1.7976931348623157E308D, (double)(nowInSeconds - (long)maxAgeSeconds));
      redisConnection.zadd(key, (double)nowInSeconds, value);
      if (setExpiry) {
         redisConnection.expire(key, maxAgeSeconds);
      }

   }

   static boolean removeShardMappingForUserID(int userID) throws Exception {
      Jedis shardDirectoryMaster;
      try {
         shardDirectoryMaster = getRedisConnection(shardDirectoryMasterServer);
      } catch (Exception var10) {
         log.error("Unable to connect to Redis shard directory master server at " + shardDirectoryMasterServer, var10);
         throw new Exception("Unabel to connect to shard directory master");
      }

      boolean var3;
      try {
         String shardKey = Redis.KeySpaceShardDirectory.REDIS_USER_ID.append(userID);
         var3 = 1L == shardDirectoryMaster.del(shardKey);
      } catch (Exception var8) {
         throw var8;
      } finally {
         disconnect(shardDirectoryMaster, log);
      }

      return var3;
   }

   static boolean removeShardMapping(String shardKey) throws Exception {
      Jedis shardDirectoryMaster;
      try {
         shardDirectoryMaster = getRedisConnection(shardDirectoryMasterServer);
      } catch (Exception var9) {
         log.error("Unable to connect to Redis shard directory master server at " + shardDirectoryMasterServer, var9);
         throw new Exception("Unabel to connect to shard directory master");
      }

      boolean var2;
      try {
         var2 = 1L == shardDirectoryMaster.del(shardKey);
      } catch (Exception var7) {
         throw var7;
      } finally {
         disconnect(shardDirectoryMaster, log);
      }

      return var2;
   }

   static void clearShardMappingCache() {
      userShardMappingCache.clear();
   }

   static String getCachedShardID(String shardDirectoryKey) {
      return (String)userShardMappingCache.get(shardDirectoryKey);
   }

   public static String getPipelineResult(List<Object> pipelineResults, int index) throws FusionException {
      Object value = pipelineResults.get(index);
      return getPipelineResult(value);
   }

   public static String getPipelineResult(Object value) throws FusionException {
      if (value == null) {
         return null;
      } else if (value instanceof byte[]) {
         return safeEncoderEncode((byte[])((byte[])value));
      } else if (value instanceof String) {
         return (String)value;
      } else {
         throw new FusionException("Pipeline result is unexpected type=" + value.getClass());
      }
   }

   public static String safeEncoderEncode(byte[] b) {
      return b == null ? null : safeEncoderEncodeInner(b);
   }

   public static byte[] safeEncoderEncode(String str) {
      try {
         if (str == null) {
            throw new JedisException("value sent to redis cannot be null");
         } else {
            return str.getBytes("UTF-8");
         }
      } catch (UnsupportedEncodingException var2) {
         throw new JedisException(var2);
      }
   }

   private static String safeEncoderEncodeInner(byte[] data) {
      try {
         return new String(data, "UTF-8");
      } catch (UnsupportedEncodingException var2) {
         throw new JedisException(var2);
      }
   }

   static {
      try {
         loadProperties();
         shardSettingsCache = new Redis.ShardSettingsCache();
         sharedDirectoryMasterConn = new SharedRedisConnection(shardDirectoryMasterServer);
         sharedDirectorySlaveConn = new SharedRedisConnection(shardDirectorySlaveServer);
         userShardMappingCache = new LRUCache(userShardMappingCacheMaxSize, (long)userShardMappingCacheExpiryInSeconds);
      } catch (Exception var1) {
         log.error(var1.getMessage());
      }

   }

   private static class ShardSettingsCache {
      private final Semaphore refreshCacheSemaphore = new Semaphore(1);
      private int cacheTimeout = 600000;
      private volatile long timeOfNextUpdate = 0L;
      private volatile Redis.ShardSettingsCache.ShardSettings shardSettings;

      public ShardSettingsCache() {
         String cacheTimeoutString = Redis.properties.getProperty("shardingSettings.cacheTime");
         if (cacheTimeoutString != null) {
            try {
               this.cacheTimeout = Integer.parseInt(cacheTimeoutString);
            } catch (NumberFormatException var3) {
            }
         }

         this.refreshCacheIfNecessary();
      }

      private void refreshCacheIfNecessary() {
         long now = System.currentTimeMillis();
         if (now >= this.timeOfNextUpdate) {
            if (this.refreshCacheSemaphore.tryAcquire()) {
               try {
                  if (now < this.timeOfNextUpdate) {
                     return;
                  }

                  this.shardSettings = this.getShardSettings();
                  this.timeOfNextUpdate = System.currentTimeMillis() + (long)this.cacheTimeout;
               } catch (Exception var7) {
                  Redis.log.error("Unable to update Redis shard settings", var7);
               } finally {
                  this.refreshCacheSemaphore.release();
               }

            }
         }
      }

      private Redis.ShardSettingsCache.ShardSettings getShardSettings() throws Exception {
         Jedis shardDirectorySlave = null;
         shardDirectorySlave = Redis.getRedisConnection(Redis.shardDirectorySlaveServer);

         Redis.ShardSettingsCache.ShardSettings var5;
         try {
            Map<String, String> shardMasters = shardDirectorySlave.hgetAll(Redis.KeySpaceShardDirectory.REDIS_SHARD_MASTERS.value);
            Map<String, String> shardSlaves = shardDirectorySlave.hgetAll(Redis.KeySpaceShardDirectory.REDIS_SHARD_SLAVES.value);
            List<String> shardWeights = shardDirectorySlave.lrange(Redis.KeySpaceShardDirectory.REDIS_SHARD_WEIGHTS.value, 0L, -1L);
            var5 = new Redis.ShardSettingsCache.ShardSettings(shardMasters, shardSlaves, shardWeights);
         } finally {
            Redis.disconnect(shardDirectorySlave, Redis.log);
         }

         return var5;
      }

      public String getMasterServerForShardID(String shardID) throws Exception {
         this.refreshCacheIfNecessary();
         if (this.shardSettings == null) {
            throw new Exception("Shard settings have not been initialised");
         } else {
            return (String)this.shardSettings.getShardMasters().get(shardID);
         }
      }

      public String getSlaveServerForShardID(String shardID) throws Exception {
         this.refreshCacheIfNecessary();
         if (this.shardSettings == null) {
            throw new Exception("Shard settings have not been initialised");
         } else {
            return (String)this.shardSettings.getShardSlaves().get(shardID);
         }
      }

      public List<Integer> getShardWeights() throws Exception {
         this.refreshCacheIfNecessary();
         if (this.shardSettings == null) {
            throw new Exception("Shard settings have not been initialised");
         } else {
            return this.shardSettings.getShardWeights();
         }
      }

      public int getSumOfShardWeights() throws Exception {
         this.refreshCacheIfNecessary();
         if (this.shardSettings == null) {
            throw new Exception("Shard settings have not been initialised");
         } else {
            return this.shardSettings.getSumOfShardWeights();
         }
      }

      private class ShardSettings {
         private Map<String, String> shardMasters;
         private Map<String, String> shardSlaves;
         private List<Integer> shardWeights;
         private int sumOfShardWeights;

         public ShardSettings(Map<String, String> shardMasters, Map<String, String> shardSlaves, List<String> shardWeightsAsStrings) {
            this.shardMasters = shardMasters;
            this.shardSlaves = shardSlaves;
            this.shardWeights = new ArrayList(shardWeightsAsStrings.size());

            int weight;
            for(Iterator i$ = shardWeightsAsStrings.iterator(); i$.hasNext(); this.sumOfShardWeights += weight) {
               String weightAsString = (String)i$.next();
               weight = Integer.parseInt(weightAsString);
               this.shardWeights.add(weight);
            }

         }

         public Map<String, String> getShardMasters() {
            return this.shardMasters;
         }

         public Map<String, String> getShardSlaves() {
            return this.shardSlaves;
         }

         public List<Integer> getShardWeights() {
            return this.shardWeights;
         }

         public int getSumOfShardWeights() {
            return this.sumOfShardWeights;
         }
      }
   }

   public static enum FieldGroupDetails {
      TAGS("Tags");

      private String value;

      private FieldGroupDetails(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }
   }

   public static enum FieldUserDetails {
      AVATAR("Avatar"),
      AVATAR_VOTES_COUNTER("AvatarVotes"),
      AVATAR_COMMENTS_COUNTER("NumOfAvatarComments"),
      CONTACT_LIST_VERSION("ContactListVersion"),
      DISPLAY_PICTURE("DispPic"),
      GIFTS_RECEIVED_COUNTER("NumOfGiftsRecieved"),
      IS_VERIFIED("IsVerified"),
      USER_LIKES_SET(":LK:UL"),
      THIRD_PARTY_GAMES_PLAYED_COUNTER(":Application:INDEX"),
      VERIFIED_ABOUT_ME("VerifiedAbout");

      private String value;

      private FieldUserDetails(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }
   }

   public static enum SubKeySpaceUserEntity {
      CONFIG("Config"),
      REWARDED_INVITEE_TIMESTAMP("RewardedInviteeTimestamp");

      private String value;

      private SubKeySpaceUserEntity(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }

      public String getFullKey(int userId) {
         return String.format("%s%s%s", Redis.KeySpace.USER_ENTITY.append(userId), ":", this.value);
      }
   }

   public static enum KeySpace {
      USER("User:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      USER_ENTITY("U:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      USER_ACTIVITY("UserActivity:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      USER_LIKES("UserLikes:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      USER_FOOTPRINTS("UserFootprints:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      GROUP("Group:", Redis.KeySpaceShardDirectory.REDIS_GROUP_ID),
      GROUP_ACTIVITY("GroupActivity:", Redis.KeySpaceShardDirectory.REDIS_GROUP_ID),
      USER_NOTIFICATION("UserNotification:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      CAPTCHA("Captcha:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      EXTERNAL_EMAIL_VERIFICATION_TOKEN("ExternalEmailVerificationToken:", Redis.KeySpaceShardDirectory.REDIS_USER_ID),
      CONVERSATION_ENTITY("CV:", Redis.KeySpaceShardDirectory.REDIS_CONVERSATION_ID),
      COUNTRY_ENTITY("CO:", Redis.KeySpaceShardDirectory.REDIS_COUNTRY_ID),
      WEB_RESOURCE_ENTITY("WR:", (Redis.KeySpaceShardDirectory)null),
      IP_ADDRESS("I:", Redis.KeySpaceShardDirectory.REDIS_IP);

      private String value;
      private Redis.KeySpaceShardDirectory directoryKeySpace;

      private KeySpace(String value, Redis.KeySpaceShardDirectory directoryKeySpace) {
         this.value = value;
         this.directoryKeySpace = directoryKeySpace;
      }

      public String toString() {
         return this.value;
      }

      public String append(int i) {
         return this.value + i;
      }

      public String append(String s) {
         return this.value + s;
      }

      public Redis.KeySpaceShardDirectory getDirectoryKeySpace() {
         return this.directoryKeySpace;
      }
   }

   public static enum KeySpaceQueuesInstance {
      TOP_MERCHANT_TAG("TopMerchantTagTmp:");

      private String value;

      private KeySpaceQueuesInstance(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }

      public String append(int i) {
         return this.value + i;
      }

      public String append(String s) {
         return this.value + s;
      }
   }

   public static enum KeySpaceShardDirectory {
      REDIS_SHARD_MASTERS("R:MASTERS"),
      REDIS_SHARD_SLAVES("R:SLAVES"),
      REDIS_SHARD_WEIGHTS("R:WEIGHTS"),
      REDIS_USER_ID("R:U:"),
      REDIS_GROUP_ID("R:G:"),
      REDIS_CHAT_ROOM_ID("R:C:"),
      REDIS_CONVERSATION_ID("R:CV:"),
      REDIS_COUNTRY_ID("R:CO:"),
      REDIS_IP("R:I:");

      private String value;

      private KeySpaceShardDirectory(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }

      public String append(int i) {
         return this.value + i;
      }

      public String append(String s) {
         return this.value + s;
      }
   }

   private static enum InstanceType {
      MASTER('M'),
      SLAVE('S');

      private char value;

      private InstanceType(char value) {
         this.value = value;
      }

      public char value() {
         return this.value;
      }
   }
}
