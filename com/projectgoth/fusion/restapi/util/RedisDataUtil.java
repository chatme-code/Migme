package com.projectgoth.fusion.restapi.util;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.data.SSOSessionMetrics;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.PipelineBlock;

public class RedisDataUtil {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RedisDataUtil.class));
   public static final String PRIVACY_KEY_MOBILENUMBER = "MobNumPrivacy";
   public static final String PRIVACY_KEY_BIRTHDAY = "DobPrivacy";
   public static final String PRIVACY_KEY_FIRSTLASTNAME = "FLNamePv";
   public static final String PRIVACY_KEY_EXTERNALEMAIL = "ExtEmPv";
   public static final String PRIVACY_KEY_CHAT = "ChatPv";
   public static final String PRIVACY_KEY_BUZZ = "BuzzPv";
   public static final String PRIVACY_KEY_LOOKOUT = "LOPv";
   public static final String PRIVACY_KEY_FOOTPRINTS = "FPPv";
   public static final String PRIVACY_KEY_FEED_CONTENT = "FeedPv";
   public static final String SSO_KEY_METRICS = "SSOM";
   public static final String SSO_KEY_METRICS_INDEX = "SSOM:INDEX";

   public static String getRedisKeyForSSOSessionMetric(SSOEnums.View view, String sessionID) {
      return "SSOM:" + view.value() + ":" + sessionID;
   }

   public static SSOEnums.View getViewFromSSOSessionMetricKey(String key) {
      String[] tokens = key.split(":");
      return tokens.length != 3 ? SSOEnums.View.UNKNOWN : SSOEnums.View.fromValue(Integer.parseInt(tokens[1]));
   }

   public static String getSessionIDFromSSOSessionMetricKey(String key) {
      String[] tokens = key.split(":");
      return tokens.length != 3 ? null : tokens[2];
   }

   public static void logSSOClientSession(final SSOSessionMetrics metrics) {
      Jedis masterInst = null;
      SSOEnums.View view = metrics.getView();
      String sessionID = metrics.getSessionID();
      final long timestamp = metrics.getTimestamp();
      boolean var6 = true;

      try {
         masterInst = Redis.getGamesMasterInstance();
         final String redisKey = getRedisKeyForSSOSessionMetric(view, sessionID);
         log.debug(String.format("logSSOClientSession: [%s]", redisKey));
         final boolean sessionMetricExists = masterInst.exists(redisKey);
         if (sessionMetricExists) {
            long stored_timestamp = Long.parseLong(masterInst.hget(redisKey, "timestamp"));
            if (stored_timestamp >= timestamp + 60L) {
               log.warn("Received outdated request to log SSO session metrics. Ignoring [" + sessionID + "] [" + view.toString() + "]");
               return;
            }
         }

         masterInst.pipelined(new PipelineBlock() {
            public void execute() {
               Iterator i$ = metrics.getCounters().entrySet().iterator();

               Entry entry;
               String field;
               while(i$.hasNext()) {
                  entry = (Entry)i$.next();
                  field = (String)entry.getKey();
                  Integer value = (Integer)entry.getValue();
                  RedisDataUtil.log.debug(String.format("incrementing [%s] [%s] [%d]", redisKey, field, value));
                  this.hincrBy(redisKey, field, (long)value);
               }

               i$ = metrics.getRecords().entrySet().iterator();

               while(i$.hasNext()) {
                  entry = (Entry)i$.next();
                  field = (String)entry.getKey();
                  String valuex = (String)entry.getValue();
                  RedisDataUtil.log.debug(String.format("setting [%s] [%s] [%s]", redisKey, field, valuex));
                  this.hset(redisKey, field, valuex);
               }

               if (!sessionMetricExists) {
                  RedisDataUtil.log.debug(String.format("setting [%s] [%s] [%s]", redisKey, "sessionStartTime", Long.toString(metrics.getSessionStartTime())));
                  this.hset(redisKey, "sessionStartTime", Long.toString(metrics.getSessionStartTime()));
                  this.hset(redisKey, "viewStartTime", Long.toString(System.currentTimeMillis()));
                  this.zadd("SSOM:INDEX", new Double((double)metrics.getSessionStartTime()), redisKey);
               }

               this.hset(redisKey, "timestamp", Long.toString(timestamp));
            }
         });
      } catch (Exception var15) {
         log.error(String.format("Unable to log SSO session [%s] [%s] : %s", sessionID, view.toString(), var15.getMessage()), var15);
      } finally {
         Redis.disconnect(masterInst, log);
      }

   }

   private static Map<String, Integer> getIntMapByFields(String key, int userId, String[] fields) {
      Map<String, Integer> ret = new HashMap();
      if (fields != null && fields.length != 0) {
         Jedis slaveInst = null;

         try {
            slaveInst = Redis.getSlaveInstanceForUserID(userId);
            List<String> vals = slaveInst.hmget(key, fields);
            int index = 0;

            for(Iterator i$ = vals.iterator(); i$.hasNext(); ++index) {
               String val = (String)i$.next();
               if (val != null) {
                  try {
                     ret.put(fields[index], Integer.parseInt(val));
                  } catch (NumberFormatException var15) {
                     log.error(String.format("Unexpected for key '%s' user id %d, unable to parse '%s' into an integer", key, userId, val));
                  }
               }
            }
         } catch (Exception var16) {
            log.error(String.format("Unable to get redis slave instance for user id %d", userId));
         } finally {
            Redis.disconnect(slaveInst, log);
         }

         return ret;
      } else {
         return ret;
      }
   }

   private static Map<String, String> getStringMapByFields(String key, int userId, String[] fields) {
      if (!(Boolean)SystemPropertyEntities.Temp.Cache.se552NPEFixEnabled.getValue()) {
         return getStringMapByFields_PreSE552(key, userId, fields);
      } else {
         Map<String, String> ret = new HashMap();
         if (fields != null && fields.length != 0) {
            Jedis slaveInst = null;

            try {
               slaveInst = Redis.getSlaveInstanceForUserID(userId);
               if (slaveInst != null) {
                  List<String> vals = slaveInst.hmget(key, fields);
                  int index = 0;

                  for(Iterator i$ = vals.iterator(); i$.hasNext(); ++index) {
                     String val = (String)i$.next();
                     if (val != null) {
                        ret.put(fields[index], val);
                     }
                  }
               } else if (log.isDebugEnabled()) {
                  log.debug("getStringMapByFields[" + key + "," + userId + "] has no configured shard instance");
               }
            } catch (Exception var13) {
               log.error(String.format("Unable to get redis slave instance for user id %d", userId), var13);
            } finally {
               Redis.disconnect(slaveInst, log);
            }

            return ret;
         } else {
            return ret;
         }
      }
   }

   /** @deprecated */
   private static Map<String, String> getStringMapByFields_PreSE552(String key, int userId, String[] fields) {
      Map<String, String> ret = new HashMap();
      if (fields != null && fields.length != 0) {
         Jedis slaveInst = null;

         try {
            slaveInst = Redis.getSlaveInstanceForUserID(userId);
            List<String> vals = slaveInst.hmget(key, fields);
            int index = 0;

            for(Iterator i$ = vals.iterator(); i$.hasNext(); ++index) {
               String val = (String)i$.next();
               if (val != null) {
                  ret.put(fields[index], val);
               }
            }
         } catch (Exception var13) {
            log.error(String.format("Unable to get redis slave instance for user id %d", userId), var13);
         } finally {
            Redis.disconnect(slaveInst, log);
         }

         return ret;
      } else {
         return ret;
      }
   }

   public static boolean setStringMap(String key, int userId, Map<String, String> values) {
      if (values != null && !values.isEmpty()) {
         Jedis masterInst = null;
         String ret = "Failed";

         try {
            masterInst = Redis.getMasterInstanceForUserID(userId);
            ret = masterInst.hmset(key, values);
            if (!"ok".equalsIgnoreCase(ret)) {
               log.error(String.format("Failed to save key '%s' to redis master instance for user id %d", key, userId));
            }
         } catch (Exception var10) {
            log.error(String.format("Unable to get redis master instance for user id %d", userId), var10);
         } finally {
            Redis.disconnect(masterInst, log);
         }

         return "ok".equalsIgnoreCase(ret);
      } else {
         return true;
      }
   }

   private static Map<String, Integer> getPrivacy(int userId, String[] fields) {
      return getIntMapByFields(Redis.SubKeySpaceUserEntity.CONFIG.getFullKey(userId), userId, fields);
   }

   public static Map<String, Integer> getAccountProfilePrivacy(int userId) {
      return getPrivacy(userId, new String[]{"DobPrivacy", "MobNumPrivacy", "FLNamePv", "ExtEmPv"});
   }

   public static boolean setAccountPrivacy(int userId, Map<String, Integer> values) {
      log.debug(String.format("writing privacy value for user %d", userId));
      Map<String, String> valueStrs = new HashMap();
      Iterator i$ = values.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Integer> e = (Entry)i$.next();
         valueStrs.put(e.getKey(), ((Integer)e.getValue()).toString());
         log.debug(String.format(" added %s %s", e.getKey(), ((Integer)e.getValue()).toString()));
      }

      return setStringMap(Redis.SubKeySpaceUserEntity.CONFIG.getFullKey(userId), userId, valueStrs);
   }

   public static Map<String, Integer> getAccountCommunicationPrivacy(int userId) {
      return getPrivacy(userId, new String[]{"ChatPv", "BuzzPv", "LOPv", "FPPv", "FeedPv"});
   }

   public static Map<String, Integer> removeUnchangedPrivacy(Map<String, Integer> newPrivacy, Map<String, Integer> oldPrivacy) {
      Map<String, Integer> updatedPrivacy = new HashMap(newPrivacy.size());
      Iterator i$ = newPrivacy.entrySet().iterator();

      while(true) {
         Entry entry;
         Integer oldValue;
         do {
            if (!i$.hasNext()) {
               return updatedPrivacy;
            }

            entry = (Entry)i$.next();
            oldValue = (Integer)oldPrivacy.get(entry.getKey());
         } while(oldValue != null && oldValue.equals(entry.getValue()));

         updatedPrivacy.put(entry.getKey(), entry.getValue());
      }
   }

   public static Map<String, Integer> keepChangedPrivacy(Map<String, Integer> newPrivacy, Map<String, Integer> oldPrivacy) {
      Map<String, Integer> updatedPrivacy = new HashMap(newPrivacy.size());
      Iterator i$ = newPrivacy.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Integer> entry = (Entry)i$.next();
         Integer oldValue = (Integer)oldPrivacy.get(entry.getKey());
         if (oldValue != null && !oldValue.equals(entry.getValue())) {
            updatedPrivacy.put(entry.getKey(), entry.getValue());
         }
      }

      return updatedPrivacy;
   }

   public static boolean setAdditionalUserInfo(int userId, Map<String, String> values) {
      return setStringMap(Redis.getUserDetailKey(userId), userId, values);
   }

   public static int getUserDisplayPictureSetting(int userId) {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se552NPEFixEnabled.getValue()) {
         Map<String, String> userDetMap = getStringMapByFields(Redis.getUserDetailKey(userId), userId, RedisDataUtil.UserDisplayPictureSettingKeys.KEY_AS_ARRAY);
         if (userDetMap != null && userDetMap.size() != 0) {
            try {
               String value = (String)userDetMap.get(RedisDataUtil.UserDisplayPictureSettingKeys.KEY);
               return !StringUtil.isBlank(value) ? Integer.parseInt(value) : -1;
            } catch (NumberFormatException var3) {
               log.warn("getUserDisplayPictureSetting:" + var3);
               return -1;
            }
         } else {
            return -1;
         }
      } else {
         return getUserDisplayPictureSetting_PreSE552(userId);
      }
   }

   /** @deprecated */
   private static int getUserDisplayPictureSetting_PreSE552(int userId) {
      String key = Redis.FieldUserDetails.DISPLAY_PICTURE.toString();
      String value = (String)getStringMapByFields(Redis.getUserDetailKey(userId), userId, new String[]{key}).get(key);

      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException var4) {
         return -1;
      }
   }

   public static boolean setUserDisplayPictureSetting(int userId, int displayPictureSetting) {
      Map<String, String> values = new HashMap();
      values.put(Redis.FieldUserDetails.DISPLAY_PICTURE.toString(), Integer.toString(displayPictureSetting));
      return setStringMap(Redis.getUserDetailKey(userId), userId, values);
   }

   public static Map<String, Integer> getAllAccountPrivacy(int userId) {
      return getPrivacy(userId, new String[]{"DobPrivacy", "MobNumPrivacy", "FLNamePv", "ExtEmPv", "ChatPv", "BuzzPv", "LOPv", "FPPv", "FeedPv"});
   }

   public static Map<RedisDataUtil.ProfileCounterType, Integer> getUserProfileCounters(final int userId) {
      Map<RedisDataUtil.ProfileCounterType, Integer> result = new HashMap();
      Jedis slaveInst = null;
      Jedis lbSlaveInst = null;

      try {
         slaveInst = Redis.getSlaveInstanceForUserID(userId);
         lbSlaveInst = Redis.getLeaderboardsSlaveInstance();
         List<Object> pipelineResult = slaveInst.pipelined(new PipelineBlock() {
            public void execute() {
               int footprintsMaxDuration = SystemProperty.getInt("footprintsMaxDurationInSeconds", 14515200);
               this.hget(Redis.getUserDetailKey(userId), Redis.FieldUserDetails.AVATAR_VOTES_COUNTER.toString());
               this.hget(Redis.getUserDetailKey(userId), Redis.FieldUserDetails.AVATAR_COMMENTS_COUNTER.toString());
               this.zcount("UserProfileViewedBy:" + userId, (double)(System.currentTimeMillis() / 1000L - (long)footprintsMaxDuration), 9.223372036854776E18D);
               this.scard(Redis.KeySpace.USER_ENTITY.toString() + userId + Redis.FieldUserDetails.USER_LIKES_SET.toString());
               this.scard(Redis.KeySpace.USER_ENTITY.toString() + userId + Redis.FieldUserDetails.THIRD_PARTY_GAMES_PLAYED_COUNTER.toString());
            }
         });
         result.put(RedisDataUtil.ProfileCounterType.AVATAR_VOTES, convertPipelinedCounterResultToInt(pipelineResult.get(0)));
         result.put(RedisDataUtil.ProfileCounterType.AVATAR_COMMENTS, convertPipelinedCounterResultToInt(pipelineResult.get(1)));
         result.put(RedisDataUtil.ProfileCounterType.FOOTPRINTS, convertPipelinedCounterResultToInt(pipelineResult.get(2)));
         result.put(RedisDataUtil.ProfileCounterType.USERLIKES, convertPipelinedCounterResultToInt(pipelineResult.get(3)));
         result.put(RedisDataUtil.ProfileCounterType.THIRD_PARTY_GAMES_PLAYED, convertPipelinedCounterResultToInt(pipelineResult.get(4)));
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         final String username = userEJB.getUsernameByUserid(userId, (Connection)null);
         pipelineResult = lbSlaveInst.pipelined(new PipelineBlock() {
            public void execute() {
               String userkey = username + ":" + userId;
               Leaderboard.Type[] arr$ = Leaderboard.Type.TOTAL_PLAYED_LEADERBOARDS;
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Leaderboard.Type type = arr$[i$];
                  this.zscore(type.append(Leaderboard.Period.WEEKLY.toString()), userkey);
               }

            }
         });
         int totalChatGamesPlayed = 0;

         for(int i = 0; i < Leaderboard.Type.TOTAL_PLAYED_LEADERBOARDS.length; ++i) {
            totalChatGamesPlayed += convertPipelinedCounterResultToInt(pipelineResult.get(i));
         }

         result.put(RedisDataUtil.ProfileCounterType.CHATROOM_GAMES_PLAYED, totalChatGamesPlayed);
      } catch (Exception var13) {
         log.error(String.format("Exception caught while retrieving profile counters for userid %d", userId), var13);
      } finally {
         Redis.disconnect(slaveInst, log);
         Redis.disconnect(lbSlaveInst, log);
      }

      return result;
   }

   public static int convertPipelinedCounterResultToInt(Object o) {
      if (o == null) {
         return 0;
      } else if (o instanceof Number) {
         return ((Number)o).intValue();
      } else if (o instanceof String) {
         return (int)StringUtil.toDoubleOrDefault((String)o, 0.0D);
      } else {
         log.error("[" + o + "] is not a Number or String");
         return 0;
      }
   }

   public static boolean recordProfileView(UserData viewedUserData, CountryData viewedCountryData, UserData viewingUserData, CountryData viewingCountryData) {
      Jedis mastInstA = null;
      Jedis mastInstB = null;
      int maxAgeSeconds = SystemProperty.getInt("footprintsMaxDurationInSeconds", 14515200);

      boolean var8;
      try {
         mastInstA = Redis.getMasterInstanceForUserID(viewingUserData.userID);
         String key = "UserProfilesViewed:" + viewingUserData.userID;
         JSONObject obj = new JSONObject();
         obj.put("UserID", "" + viewedUserData.userID);
         obj.put("Username", viewedUserData.username);
         obj.put("Country", viewedCountryData.name);
         Redis.addToSortedSetWithTimestampAndTrimByAge(mastInstA, key, obj.toString(), maxAgeSeconds, true);
         mastInstB = Redis.getMasterInstanceForUserID(viewedUserData.userID);
         key = "UserProfileViewedBy:" + viewedUserData.userID;
         obj = new JSONObject();
         obj.put("UserID", "" + viewingUserData.userID);
         obj.put("Username", viewingUserData.username);
         obj.put("Country", viewingCountryData.name);
         Redis.addToSortedSetWithTimestampAndTrimByAge(mastInstB, key, obj.toString(), maxAgeSeconds, true);
         boolean var9 = true;
         return var9;
      } catch (Exception var14) {
         log.error(String.format("Unable to record profile view user id %s vieweruserid", viewedUserData.userID, viewingUserData.userID), var14);
         var8 = false;
      } finally {
         Redis.disconnect(mastInstA, log);
         Redis.disconnect(mastInstB, log);
      }

      return var8;
   }

   private static final class UserDisplayPictureSettingKeys {
      public static final String KEY;
      public static final String[] KEY_AS_ARRAY;

      static {
         KEY = Redis.FieldUserDetails.DISPLAY_PICTURE.toString();
         KEY_AS_ARRAY = new String[]{KEY};
      }
   }

   public static enum ProfileCounterType {
      AVATAR_VOTES,
      AVATAR_COMMENTS,
      FOOTPRINTS,
      USERLIKES,
      THIRD_PARTY_GAMES_PLAYED,
      CHATROOM_GAMES_PLAYED;
   }
}
