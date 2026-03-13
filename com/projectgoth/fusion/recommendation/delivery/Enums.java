package com.projectgoth.fusion.recommendation.delivery;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.RecommendationItem;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class Enums {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Enums.class));

   public static enum UserRecommendationEnum {
      FOLLOWINGS_FOLLOWING("FL"),
      FOLLOWERS_FOLLOWING("FLR"),
      FRIENDS_FOLLOWING("FR");

      private String value;

      private UserRecommendationEnum(String value) {
         this.value = value;
      }

      public String value() {
         return this.value;
      }

      public static Enums.UserRecommendationEnum fromValue(String value) {
         Enums.UserRecommendationEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.UserRecommendationEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum RecommendationTypeEnum implements EnumUtils.IEnumValueGetter<String> {
      HASH_TAGS("HT", new RecommendationDeliveryUtils.RecommendationTypeEvaluator<String>() {
         public String evaluate(RecommendationItem item) {
            return item.getValue();
         }
      }),
      CHATROOMS("CR", new RecommendationDeliveryUtils.RecommendationTypeEvaluator<ChatRoomData>() {
         public ChatRoomData evaluate(RecommendationItem item) {
            int chatroomId;
            try {
               chatroomId = Integer.parseInt(item.getValue());
            } catch (NumberFormatException var7) {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.REDIS_LOCATION_ERROR_LOGGING_ENABLED)) {
                  Enums.log.error("RecommendationItem contains invalid data! Value=" + item.getValue() + (item.getRedisLocation() != null ? " redis location=" + item.getRedisLocation() : "") + " ex=" + var7, var7);
               }

               throw var7;
            }

            try {
               Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               return messageEJB.getSimpleChatRoomData((Integer)chatroomId, (Connection)null);
            } catch (CreateException var5) {
               Enums.log.error("Unabled to create bean to extract chatroom details", var5);
               return null;
            } catch (RemoteException var6) {
               Enums.log.error("Unabled to get chatroom data for chatroom id " + item.getValue(), var6);
               return null;
            }
         }
      }),
      ADDRESSBOOKCONTACTS("ABC", new RecommendationDeliveryUtils.RecommendationTypeEvaluator<String>() {
         public String evaluate(RecommendationItem item) {
            return item.getValue();
         }
      }),
      USERRECOMMENDATION("UR", new RecommendationDeliveryUtils.RecommendationTypeEvaluator<String>() {
         public String evaluate(RecommendationItem item) {
            return item.getValue();
         }
      });

      String value;
      RecommendationDeliveryUtils.RecommendationTypeEvaluator evaluator;

      private RecommendationTypeEnum(String value, RecommendationDeliveryUtils.RecommendationTypeEvaluator transform) {
         this.value = value;
         this.evaluator = transform;
      }

      public String value() {
         return this.value;
      }

      public static Enums.RecommendationTypeEnum fromValue(String v) {
         return (Enums.RecommendationTypeEnum)Enums.RecommendationTypeEnum.SingletonHolder.getLookupMap().get(v);
      }

      public RecommendationDeliveryUtils.RecommendationTypeEvaluator getEvaluator() {
         return this.evaluator;
      }

      public String getEnumValue() {
         return this.value();
      }

      private static class SingletonHolder {
         private static final Map<String, Enums.RecommendationTypeEnum> LOOKUP_MAP = EnumUtils.buildLookUpMap(new HashMap(), Enums.RecommendationTypeEnum.class);

         public static Map<String, Enums.RecommendationTypeEnum> getLookupMap() {
            return LOOKUP_MAP;
         }
      }
   }

   public static enum RecommendationTargetEnum implements EnumUtils.IEnumValueGetter<String> {
      INDIVIDUAL("user", Redis.KeySpace.USER_ENTITY),
      COUNTRY("country", Redis.KeySpace.COUNTRY_ENTITY);

      String value;
      Redis.KeySpace redisKeySpace;

      private RecommendationTargetEnum(String value, Redis.KeySpace redisKeySpace) {
         this.value = value;
         this.redisKeySpace = redisKeySpace;
      }

      public String value() {
         return this.value;
      }

      public static Enums.RecommendationTargetEnum fromValue(String v) {
         return (Enums.RecommendationTargetEnum)Enums.RecommendationTargetEnum.SingletonHolder.getLookupMap().get(v);
      }

      public String getEnumValue() {
         return this.value();
      }

      public Redis.KeySpace getRedisKeySpace() {
         return this.redisKeySpace;
      }

      private static class SingletonHolder {
         private static final Map<String, Enums.RecommendationTargetEnum> LOOKUP_MAP = EnumUtils.buildLookUpMap(new HashMap(), Enums.RecommendationTargetEnum.class);

         public static Map<String, Enums.RecommendationTargetEnum> getLookupMap() {
            return LOOKUP_MAP;
         }
      }
   }
}
