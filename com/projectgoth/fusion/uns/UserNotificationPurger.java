package com.projectgoth.fusion.uns;

import com.google.gson.Gson;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.Message;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class UserNotificationPurger {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserNotificationPurger.class));
   private static ConcurrentHashMap<Integer, Long> userMutexMap = new ConcurrentHashMap();

   public static void trimUserNotificationItemsIfNeededOnWrite(Jedis jedisRead, Jedis jedisMasterPassedIn, int userID) {
      long mutexTimeout = (long)(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.USER_NOTIFICATION_USER_MUTEX_TIMEOUT_MINS) * 60 * 1000);
      Long userMutex = (Long)userMutexMap.get(userID);
      if (userMutex == null || System.currentTimeMillis() - userMutex >= mutexTimeout) {
         boolean setIt;
         if (userMutex != null) {
            setIt = userMutexMap.replace(userID, userMutex, System.currentTimeMillis());
         } else {
            Long oldValue = (Long)userMutexMap.putIfAbsent(userID, System.currentTimeMillis());
            setIt = oldValue == null;
         }

         if (setIt) {
            try {
               trimUserNotificationItemsIfNeeded(jedisRead, jedisMasterPassedIn, userID);
            } finally {
               userMutexMap.remove(userID);
            }

         }
      }
   }

   public static void trimUserNotificationItemsIfNeeded(Jedis jedisRead, Jedis jedisMasterPassedIn, int userID) {
      Jedis jedisMaster = null;

      try {
         if (log.isDebugEnabled()) {
            log.debug("trimUserNotificationItemsIfNeeded for userID=" + userID);
         }

         int count = countUserNotificationItems(jedisRead, userID);
         int limit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_NOTIFICATIONS_TO_SEND_TO_USER);
         if (log.isDebugEnabled()) {
            log.debug("trimUserNotificationItemsIfNeeded for userID=" + userID + " count=" + count + " limit=" + limit);
         }

         if (count > limit) {
            if (jedisMasterPassedIn != null) {
               jedisMaster = jedisMasterPassedIn;
            } else {
               jedisMaster = Redis.getMasterInstanceForUserID(userID);
            }

            trimUserNotifications(jedisMaster, userID, count, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.MAX_NOTIFICATIONS_TO_SEND_TO_USER_TRUNCATION));
         }
      } catch (Exception var9) {
         log.error("Warning: failed to trim user notifications for userID=" + userID + ": cause=" + var9, var9);
      } finally {
         if (jedisMasterPassedIn == null) {
            Redis.disconnect(jedisMaster, log);
         }

      }

   }

   private static int countUserNotificationItems(Jedis jedis, int userID) {
      int count = 0;

      String redisKey;
      for(Iterator i$ = Enums.NotificationTypeEnum.MIGBO_SET.iterator(); i$.hasNext(); count = (int)((long)count + jedis.hlen(redisKey))) {
         Enums.NotificationTypeEnum notfnType = (Enums.NotificationTypeEnum)i$.next();
         redisKey = UserNotificationServiceI.getUnsKey(userID, notfnType.getType());
      }

      return count;
   }

   private static void trimUserNotifications(Jedis jedis, int userID, int hashItemCount, int maxCountToTruncateTo) {
      TreeMap<Long, String[]> userHKeyFieldPairsByDate = new TreeMap();
      Long oldestTimestamp = null;
      Iterator i$ = Enums.NotificationTypeEnum.MIGBO_SET.iterator();

      while(i$.hasNext()) {
         Enums.NotificationTypeEnum notfnType = (Enums.NotificationTypeEnum)i$.next();
         String key = UserNotificationServiceI.getUnsKey(userID, notfnType.getType());
         List<String> hfields = new ArrayList(jedis.hkeys(key));
         Iterator i$ = hfields.iterator();

         while(i$.hasNext()) {
            String hfield = (String)i$.next();
            String hvalue = jedis.hget(key, hfield);
            long dateCreated = getDateCreated(hvalue);
            String[] pair = new String[]{key, hfield};
            Long timestampToDelete = null;
            String[] keyAndFieldToDelete = null;
            if (oldestTimestamp != null && dateCreated < oldestTimestamp) {
               timestampToDelete = dateCreated;
               keyAndFieldToDelete = pair;
            } else {
               userHKeyFieldPairsByDate.put(dateCreated, pair);
               if (userHKeyFieldPairsByDate.size() > maxCountToTruncateTo) {
                  timestampToDelete = (Long)userHKeyFieldPairsByDate.firstKey();
                  keyAndFieldToDelete = (String[])userHKeyFieldPairsByDate.get(timestampToDelete);
                  userHKeyFieldPairsByDate.remove(timestampToDelete);
                  oldestTimestamp = (Long)userHKeyFieldPairsByDate.firstKey();
               }
            }

            if (keyAndFieldToDelete != null) {
               if (log.isDebugEnabled()) {
                  log.debug("HDEL " + keyAndFieldToDelete[0] + " " + keyAndFieldToDelete[1] + " with dateCreated=" + timestampToDelete);
               }

               jedis.hdel(keyAndFieldToDelete[0], new String[]{keyAndFieldToDelete[1]});
            }
         }
      }

   }

   private static long getDateCreated(String jsonMessage) {
      int x = StringUtil.toIntOrDefault(jsonMessage, -1);
      if (x != -1) {
         return System.currentTimeMillis();
      } else {
         try {
            Message message = (Message)(new Gson()).fromJson(jsonMessage, Message.class);
            return message.dateCreated;
         } catch (Exception var3) {
            if (log.isDebugEnabled()) {
               log.debug("Couldn't parse dateCreated from hvalue=" + jsonMessage);
            }

            return System.currentTimeMillis();
         }
      }
   }
}
