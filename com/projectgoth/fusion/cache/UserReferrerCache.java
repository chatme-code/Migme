package com.projectgoth.fusion.cache;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.UserData;
import java.util.Set;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class UserReferrerCache {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserReferrerCache.class));

   public static boolean isWithinCapAllowed(UserData inviterUserData, UserData invitedUserData, RewardProgramData rewardProgramData) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER78_ENABLED)) {
         int[] rewardProgramType = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.EXCLUDE_TYPE_INVITEE_REWARDED_TRACKING);

         for(int i = 0; i < rewardProgramType.length; ++i) {
            if (rewardProgramType[i] == rewardProgramData.type.value()) {
               return true;
            }
         }

         int[] rewardProgramIds = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.EXCLUDE_REWARD_ID_INVITEE_REWARDED_TRACKING);

         for(int i = 0; i < rewardProgramIds.length; ++i) {
            if (rewardProgramIds[i] == rewardProgramData.id) {
               return true;
            }
         }
      }

      return isWithinCapAllowed(inviterUserData, invitedUserData);
   }

   public static boolean isWithinCapAllowed(UserData inviterUserData, UserData invitedUserData) {
      Jedis masterInstance = null;
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_INVITEE_REWARDED_TRACKING)) {
         return true;
      } else {
         boolean var7;
         try {
            masterInstance = Redis.getMasterInstanceForUserID(inviterUserData.userID);
            String member = String.valueOf(invitedUserData.userID);
            String userDetailKey = Redis.SubKeySpaceUserEntity.REWARDED_INVITEE_TIMESTAMP.getFullKey(inviterUserData.userID);
            Double timestamp = masterInstance.zscore(userDetailKey, member);
            if (timestamp == null && masterInstance.zcard(userDetailKey) <= (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.UNIQUE_INVITEE_REWARDED_REDIS_MAX_COUNT)) {
               masterInstance.zadd(userDetailKey, (double)System.currentTimeMillis(), member);
            }

            Set<String> ids = masterInstance.zrange(userDetailKey, 0L, (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.UNIQUE_INVITEE_REWARDED_MAX_COUNT));
            if (!ids.contains(member)) {
               return false;
            }

            var7 = true;
         } catch (Exception var12) {
            log.error("Unable to get the slave Redis instance for User ID [" + inviterUserData.userID + "]: " + var12);
            return false;
         } finally {
            Redis.disconnect(masterInstance, log);
         }

         return var7;
      }
   }
}
