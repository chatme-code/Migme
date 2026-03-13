package com.projectgoth.fusion.recommendation.delivery;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LRUCache;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.SystemPropertyEntryWithParent;
import com.projectgoth.fusion.data.RecommendationData;
import com.projectgoth.fusion.data.RecommendationItem;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.hashtag.data.HashTagData;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.CreateException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

public class RecommendationDeliveryUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecommendationDeliveryUtils.class));
   private static final Logger spamLog = Logger.getLogger("SpamLog");
   private static final String SPAM_LOG_PATTERN = "|%s|%s";
   public static final String RECOMMENDATION_NAME_SPACE = "Rec";
   private static final int GLOBAL_ID = -1;
   public static final String SOURCE_HASHMAP_NAME_SPACE = "S";
   private static LRUCache<String, List<RecommendationItem>> recommendationsCache;
   private static int recommendationCacheMaxSize = 5000;
   private static LazyLoader<Boolean> redisLocationLoggingEnabled = new LazyLoader<Boolean>("REDIS_LOCATION_ERROR_LOGGING_ENABLED", 60000L) {
      protected Boolean fetchValue() throws Exception {
         return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.REDIS_LOCATION_ERROR_LOGGING_ENABLED);
      }
   };
   private static final Comparator<RecommendationItem> ORDER_BY_SCORE_DESC_COMPARATOR;

   public static String generateRecommendationKey(Enums.RecommendationTypeEnum type, Enums.RecommendationTargetEnum target, int targetId, String subtype) {
      return subtype == null ? String.format("%s:%s:%s", target.getRedisKeySpace().append(targetId), "Rec", type.value()) : String.format("%s:%s:%s:%s", target.getRedisKeySpace().append(targetId), "Rec", type.value(), subtype);
   }

   public static String generateRecommendationKey(Enums.RecommendationTypeEnum type, Enums.RecommendationTargetEnum target, int targetId) {
      return generateRecommendationKey(type, target, targetId, (String)null);
   }

   public static RecommendationData getRecommendation(Enums.RecommendationTypeEnum typeEnum, Enums.RecommendationTargetEnum target, int targetId, String subtype, int limit, int offset, boolean refreshCache) {
      if (typeEnum == Enums.RecommendationTypeEnum.USERRECOMMENDATION) {
         return handleUserRecommendation(typeEnum, target, targetId, limit, offset, refreshCache);
      } else {
         Jedis handle = null;
         List<RecommendationItem> recommendations = getCachedRecommendations(typeEnum, target, targetId, subtype);
         if (!refreshCache && recommendations == null) {
            recommendations = getCachedRecommendations(typeEnum, target, -1, subtype);
         }

         StringBuilder logStrBuilder;
         if (!refreshCache && recommendations != null) {
            if (log.isDebugEnabled()) {
               StringBuilder logStrBuilder = append(typeEnum, target, targetId, limit, offset, refreshCache);
               logStrBuilder.append("recommendations from cache:[").append(recommendations + "]");
               log.debug(logStrBuilder);
            }
         } else {
            try {
               recommendations = new ArrayList();
               handle = Redis.getSlaveInstanceForEntityID(target.getRedisKeySpace(), targetId);
               String key = generateRecommendationKey(typeEnum, target, targetId, subtype);
               boolean fallBackToGlobal = false;
               if (null == handle || !handle.exists(key)) {
                  StringBuilder logStrBuilder;
                  if (log.isDebugEnabled()) {
                     logStrBuilder = append(typeEnum, target, targetId, limit, offset, refreshCache);
                     logStrBuilder.append("null == handle || 0 == handle.exists( key ):key=[").append(key).append("].");
                     log.debug(logStrBuilder);
                  }

                  if (null != handle) {
                     Redis.disconnect(handle, log);
                  }

                  handle = Redis.getSlaveInstanceForEntityID(target.getRedisKeySpace(), -1);
                  key = generateRecommendationKey(typeEnum, target, -1, subtype);
                  fallBackToGlobal = true;
                  if (log.isDebugEnabled()) {
                     logStrBuilder = append(typeEnum, target, targetId, limit, offset, refreshCache);
                     logStrBuilder.append("rediskeyspace=[").append(target.getRedisKeySpace()).append("].handle=[").append(handle).append("].");
                     logStrBuilder.append("key:[").append(key).append("].");
                     log.debug(logStrBuilder);
                  }
               }

               if (null != handle) {
                  Set<Tuple> resultSet = handle.zrevrangeWithScores(key, 0L, -1L);
                  Iterator i$ = resultSet.iterator();

                  while(i$.hasNext()) {
                     Tuple tuple = (Tuple)i$.next();
                     if ((Boolean)redisLocationLoggingEnabled.getValue()) {
                        ((List)recommendations).add(new RecommendationItem(tuple, handle, key));
                     } else {
                        ((List)recommendations).add(new RecommendationItem(tuple));
                     }
                  }

                  if (log.isDebugEnabled()) {
                     logStrBuilder = append(typeEnum, target, targetId, limit, offset, refreshCache);
                     logStrBuilder.append("redis key:[").append(key).append("].");
                     logStrBuilder.append("fallBackToGlobal:[").append(fallBackToGlobal).append("].");
                     logStrBuilder.append("recommendations from redis:[").append(recommendations + "]");
                     log.debug(logStrBuilder);
                  }

                  if (fallBackToGlobal) {
                     setCachedRecommendations((List)recommendations, typeEnum, target, -1, subtype);
                  } else {
                     setCachedRecommendations((List)recommendations, typeEnum, target, targetId, subtype);
                  }

                  refreshCacheExpiry();
               } else {
                  log.warn("Unable to retrieve any recomendation for Type=" + typeEnum.toString() + " TargetType=" + target.toString() + " targetID=" + targetId);
               }
            } catch (Exception var17) {
               log.error("Failed to get recommendations: " + var17.getMessage(), var17);
            } finally {
               Redis.disconnect(handle, log);
            }
         }

         List<RecommendationItem> recommendations = combineFeaturedRecommendation((List)recommendations, typeEnum, target, targetId);
         recommendations = filterBlackListedItemsFromRecommendation(recommendations, typeEnum, target, targetId);
         recommendations = applyCustomFilterForRecommendation(recommendations, typeEnum, target, targetId);
         LinkedHashSet<RecommendationItem> set = new LinkedHashSet(recommendations);
         recommendations.clear();
         recommendations.addAll(set);
         RecommendationData extractedData = extractRequestedPageFromRecommendation(recommendations, offset, limit);
         if (log.isDebugEnabled()) {
            logStrBuilder = append(typeEnum, target, targetId, limit, offset, refreshCache);
            logStrBuilder.append("Final Recommendations result:[").append(extractedData.getRecommendations() + "]");
            log.debug(logStrBuilder);
         }

         return extractedData;
      }
   }

   private static RecommendationData handleUserRecommendation(Enums.RecommendationTypeEnum typeEnum, Enums.RecommendationTargetEnum target, int targetId, int limit, int offset, boolean refreshCache) {
      Jedis handle = null;
      List<RecommendationItem> recommendations = getCachedRecommendations(typeEnum, target, targetId);
      List<RecommendationItem> globalRecommendations = null;
      List<RecommendationItem> regionalRecommendations = null;
      boolean fallBack = false;
      if (refreshCache || recommendations == null) {
         try {
            recommendations = new ArrayList();
            handle = Redis.getSlaveInstanceForEntityID(target.getRedisKeySpace(), targetId);
            String parentKey = generateRecommendationKey(typeEnum, target, targetId);
            if (!userRecommendationKeyexists(handle, parentKey)) {
               fallBack = true;
            } else {
               if (log.isDebugEnabled()) {
                  log.debug(String.format("Retriving customzied user recommendation for user:%s", targetId));
               }

               Map<String, RecommendationItem> recUsersMap = new HashMap();
               Map<String, Integer> recUsersWeightMap = new HashMap();
               Enums.UserRecommendationEnum[] arr$ = Enums.UserRecommendationEnum.values();
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Enums.UserRecommendationEnum e = arr$[i$];
                  String key = getUserRecommendationKey(parentKey, e);
                  String sourceKey = getUserRecommendationSourceKey(parentKey, e);
                  if (checkAllKeysExist(handle, key, sourceKey)) {
                     Pipeline pipeline = handle.pipelined();
                     pipeline.zrevrangeWithScores(key, 0L, -1L);
                     pipeline.hgetAll(sourceKey);
                     List<Object> pipelineResult = pipeline.syncAndReturnAll();
                     Set<Tuple> resultSet = (Set)pipelineResult.get(0);
                     Map<String, String> sourceMap = (Map)pipelineResult.get(1);
                     if (resultSet != null && resultSet.size() != 0 && sourceMap != null && sourceMap.size() != 0) {
                        Iterator i$ = resultSet.iterator();

                        while(i$.hasNext()) {
                           Tuple tuple = (Tuple)i$.next();
                           String tupleKey = tuple.getElement();
                           if (!sourceMap.containsKey(tupleKey)) {
                              if (log.isDebugEnabled()) {
                                 log.debug(String.format("Can not find key:%s in sourceMap for, RecommendationTypeEnum:%s, RecommendationTargetEnum:%s, targetId:%s, ingore it", tupleKey, typeEnum, target, targetId));
                              }
                           } else {
                              RecommendationItem item;
                              if (recUsersMap.containsKey(tupleKey)) {
                                 item = (RecommendationItem)recUsersMap.get(tupleKey);
                                 item.setScore(item.getScore() + getForUserRecommendationWeight(e, tuple));
                                 recUsersMap.put(tupleKey, item);
                                 double weight = tuple.getScore();
                                 if (recUsersWeightMap.containsKey(tupleKey)) {
                                    weight += (double)(Integer)recUsersWeightMap.get(tupleKey);
                                 }

                                 recUsersWeightMap.put(tupleKey, (int)weight);
                              } else {
                                 if ((Boolean)redisLocationLoggingEnabled.getValue()) {
                                    item = new RecommendationItem(tuple, handle, key);
                                 } else {
                                    item = new RecommendationItem(tuple);
                                 }

                                 item.setScore(getForUserRecommendationWeight(e, tuple));
                                 item.setSource((String)sourceMap.get(tupleKey));
                                 recUsersMap.put(tupleKey, item);
                                 recUsersWeightMap.put(tupleKey, (int)tuple.getScore());
                              }
                           }
                        }
                     }
                  }
               }

               String reasonDetailP1 = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.INDIVIDUAL_USER_RECOMMENDATION_REASON_DETAILS_PART1);
               String reasonDetailP2 = reasonDetailP1 + SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.INDIVIDUAL_USER_RECOMMENDATION_REASON_DETAILS_PART2);
               Iterator i$ = recUsersMap.keySet().iterator();

               while(i$.hasNext()) {
                  String k = (String)i$.next();
                  if (recUsersWeightMap.containsKey(k)) {
                     RecommendationItem item = (RecommendationItem)recUsersMap.get(k);
                     int sourceCount = (Integer)recUsersWeightMap.get(k);
                     if (sourceCount > 1) {
                        item.setReason(String.format(reasonDetailP2, sourceCount));
                     } else {
                        item.setReason(reasonDetailP1);
                     }

                     ((List)recommendations).add(item);
                  }
               }

               if (recommendations != null && ((List)recommendations).size() > 0) {
                  setCachedRecommendations((List)recommendations, typeEnum, target, targetId);
                  refreshCacheExpiry();
               }
            }
         } catch (Exception var33) {
            log.error("Failed to get recommendations: " + var33.getMessage(), var33);
         } finally {
            Redis.disconnect(handle, log);
         }
      }

      if (fallBack || SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.MIX_USER_RECOMMENDATION_ENABLE)) {
         globalRecommendations = getGlobalVerifiedUserRecommendation();
         regionalRecommendations = getRegionalActiveUserRecommendation(targetId);
         mixGlobalRegionalUserRecommendation((List)recommendations, globalRecommendations, regionalRecommendations);
      }

      List<RecommendationItem> recommendations = combineFeaturedRecommendation((List)recommendations, typeEnum, target, targetId);
      recommendations = filterBlackListedItemsFromRecommendation(recommendations, typeEnum, target, targetId);
      recommendations = applyCustomFilterForRecommendation(recommendations, typeEnum, target, targetId);
      LinkedHashSet<RecommendationItem> set = new LinkedHashSet(recommendations);
      recommendations.clear();
      recommendations.addAll(set);
      RecommendationData extractedData = extractRequestedPageFromRecommendation(recommendations, offset, limit);
      if (log.isDebugEnabled()) {
         StringBuilder logStrBuilder = append(typeEnum, target, targetId, limit, offset, refreshCache);
         logStrBuilder.append("Final Recommendations result:[").append(extractedData.getRecommendations() + "]");
         log.debug(logStrBuilder);
      }

      return extractedData;
   }

   public static void mixGlobalRegionalUserRecommendation(List<RecommendationItem> recommendation, List<RecommendationItem> globalRecommendations, List<RecommendationItem> regionalRecommendations) {
      double weight = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.MIX_USER_RECOMMENDATION_WEIGHT);
      Iterator i$ = recommendation.iterator();

      while(i$.hasNext()) {
         RecommendationItem item = (RecommendationItem)i$.next();
         item.setScore((int)((double)item.getScore() * weight));
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.BOOST_GLOBAL_VERIFIED_USER_RECOMMENDATION_ENABLE) && recommendation.size() <= SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.USER_RECOMMENDATION_MIN_THRESHOLD) && globalRecommendations != null && globalRecommendations.size() > 0) {
         double globalBoostWeight = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.GLOBAL_VERIFIED_USER_RECOMMENDATION_BOOST_WEIGHT);
         Iterator i$ = globalRecommendations.iterator();

         while(i$.hasNext()) {
            RecommendationItem item = (RecommendationItem)i$.next();
            item.setScore((int)((double)item.getScore() * globalBoostWeight));
         }
      }

      if (globalRecommendations != null && globalRecommendations.size() > 0) {
         recommendation.addAll(globalRecommendations);
      }

      if (regionalRecommendations != null && regionalRecommendations.size() > 0) {
         recommendation.addAll(regionalRecommendations);
      }

   }

   private static List<RecommendationItem> getGlobalVerifiedUserRecommendation() {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.GLOBAL_VERIFIED_USER_RECOMMENDATION_ENABLE)) {
         return Collections.emptyList();
      } else {
         List<RecommendationItem> recommendations = getCachedRecommendations(Enums.RecommendationTypeEnum.USERRECOMMENDATION, Enums.RecommendationTargetEnum.INDIVIDUAL, -1);
         if (recommendations != null && recommendations.size() > 0) {
            return recommendations;
         } else {
            List<RecommendationItem> recommendations = new ArrayList();
            Jedis handle = null;

            try {
               handle = Redis.getSlaveInstanceForEntityID(Redis.KeySpace.USER_ENTITY, -1);
               String key;
               if (handle == null) {
                  log.warn("Failed to get redis handle");
                  key = null;
                  return key;
               }

               key = generateRecommendationKey(Enums.RecommendationTypeEnum.USERRECOMMENDATION, Enums.RecommendationTargetEnum.INDIVIDUAL, -1);
               String globalUserRecommendationReason;
               if (!handle.exists(key)) {
                  if (log.isDebugEnabled()) {
                     log.debug(String.format("Global recommendation:%s does not exist!, check pyric's log for more details", key));
                  }

                  globalUserRecommendationReason = null;
                  return globalUserRecommendationReason;
               }

               globalUserRecommendationReason = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.GLOBAL_USER_RECOMMENDATION_REASON);
               Set<Tuple> resultSet = handle.zrevrangeWithScores(key, 0L, -1L);
               Iterator i$ = resultSet.iterator();

               while(i$.hasNext()) {
                  Tuple tuple = (Tuple)i$.next();
                  RecommendationItem item;
                  if ((Boolean)redisLocationLoggingEnabled.getValue()) {
                     item = new RecommendationItem(tuple, handle, key);
                  } else {
                     item = new RecommendationItem(tuple);
                  }

                  item.setReason(globalUserRecommendationReason);
                  recommendations.add(item);
               }

               setCachedRecommendations(recommendations, Enums.RecommendationTypeEnum.USERRECOMMENDATION, Enums.RecommendationTargetEnum.INDIVIDUAL, -1);
            } catch (Exception var11) {
               log.error("Failed to get recommendations: " + var11.getMessage(), var11);
            } finally {
               if (handle != null) {
                  Redis.disconnect(handle, log);
               }

            }

            return recommendations;
         }
      }
   }

   private static List<RecommendationItem> getRegionalActiveUserRecommendation(int userid) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.REGIONAL_MOST_ACTIVE_USER_RECOMMENDATION_ENABLE)) {
         return Collections.emptyList();
      } else {
         List<RecommendationItem> recommendations = null;
         UserLocal userBean = null;
         Jedis handle = null;

         String regionalUserRecommendationReason;
         try {
            userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUserFromID(userid);
            String key;
            if (userData == null) {
               log.error(String.format("Failed to get UserData to userid:%s, skipping regional active user recommendtion", userid));
               key = null;
               return key;
            }

            if (userData.countryID == null) {
               log.error(String.format("Failed to get country id (countryid is null) for user:%s, skipping regional active user recommendtion", userid));
               key = null;
               return key;
            }

            List<RecommendationItem> recommendations = getCachedRecommendations(Enums.RecommendationTypeEnum.USERRECOMMENDATION, Enums.RecommendationTargetEnum.COUNTRY, userData.countryID);
            if (recommendations != null && recommendations.size() > 0) {
               List var19 = recommendations;
               return var19;
            }

            recommendations = new ArrayList();
            handle = Redis.getSlaveInstanceForEntityID(Redis.KeySpace.COUNTRY_ENTITY, userData.countryID);
            if (handle == null) {
               log.warn(String.format("Failed to get redis handle, skipping regional active user recommendtion for user:%s", userid));
               key = null;
               return key;
            }

            key = generateRecommendationKey(Enums.RecommendationTypeEnum.USERRECOMMENDATION, Enums.RecommendationTargetEnum.COUNTRY, userData.countryID);
            if (handle.exists(key)) {
               regionalUserRecommendationReason = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.REGIONAL_USER_RECOMMENDATION_REASON);
               Set<Tuple> resultSet = handle.zrevrangeWithScores(key, 0L, -1L);
               double threshold = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.REGIONAL_MOST_ACTIVE_USER_SPAM_THRESHOLD);
               Iterator i$ = resultSet.iterator();

               while(i$.hasNext()) {
                  Tuple tuple = (Tuple)i$.next();
                  if (tuple.getScore() >= threshold) {
                     spamLog.info(String.format("|%s|%s", tuple.getElement(), tuple.getScore()));
                  } else {
                     RecommendationItem item;
                     if ((Boolean)redisLocationLoggingEnabled.getValue()) {
                        item = new RecommendationItem(tuple, handle, key);
                     } else {
                        item = new RecommendationItem(tuple);
                     }

                     item.setReason(regionalUserRecommendationReason);
                     recommendations.add(item);
                  }
               }

               setCachedRecommendations(recommendations, Enums.RecommendationTypeEnum.USERRECOMMENDATION, Enums.RecommendationTargetEnum.COUNTRY, userData.countryID);
               return recommendations;
            }

            if (log.isDebugEnabled()) {
               log.debug(String.format("Regional recommendation:%s does not exist!, check pyric's log for more details", key));
            }

            regionalUserRecommendationReason = null;
         } catch (Exception var16) {
            log.error(String.format("Failed to get reginal active user recommendation to userid:%s, EJBException '%s'", userid, var16.getMessage()));
            return recommendations;
         } finally {
            Redis.disconnect(handle, log);
         }

         return regionalUserRecommendationReason;
      }
   }

   private static int getForUserRecommendationWeight(Enums.UserRecommendationEnum e, Tuple tuple) {
      double weight = 1.0D;
      if (e == Enums.UserRecommendationEnum.FRIENDS_FOLLOWING) {
         weight = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.USER_RECOMMENDATION_WEIGHT_FOR_FRIENDS);
      } else if (e == Enums.UserRecommendationEnum.FOLLOWERS_FOLLOWING) {
         weight = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.USER_RECOMMENDATION_WEIGHT_FOR_FOLLOWER);
      } else if (e == Enums.UserRecommendationEnum.FOLLOWINGS_FOLLOWING) {
         weight = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.USER_RECOMMENDATION_WEIGHT_FOR_FOLLOWING);
      }

      return (int)(weight * tuple.getScore());
   }

   private static String getUserRecommendationKey(String parentKey, Enums.UserRecommendationEnum userRecType) {
      return String.format("%s:%s", parentKey, userRecType.value());
   }

   private static String getUserRecommendationSourceKey(String parentKey, Enums.UserRecommendationEnum userRecType) {
      return String.format("%s:%s:%s", parentKey, userRecType.value(), "S");
   }

   private static boolean userRecommendationKeyexists(Jedis handle, String parentKey) {
      if (handle == null) {
         return false;
      } else {
         Pipeline pipeline = handle.pipelined();
         Enums.UserRecommendationEnum[] arr$ = Enums.UserRecommendationEnum.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.UserRecommendationEnum e = arr$[i$];
            pipeline.exists(getUserRecommendationKey(parentKey, e));
         }

         List<Object> pipelineResult = pipeline.syncAndReturnAll();
         Iterator i$ = pipelineResult.iterator();

         Object obj;
         do {
            if (!i$.hasNext()) {
               return false;
            }

            obj = i$.next();
         } while(!(Boolean)obj);

         return true;
      }
   }

   private static boolean checkAllKeysExist(Jedis handle, String... keys) {
      if (handle == null) {
         return false;
      } else {
         Pipeline pipeline = handle.pipelined();
         String[] arr$ = keys;
         int len$ = keys.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String key = arr$[i$];
            pipeline.exists(key);
         }

         List<Object> pipelineResult = pipeline.syncAndReturnAll();
         Iterator i$ = pipelineResult.iterator();

         Object obj;
         do {
            if (!i$.hasNext()) {
               return true;
            }

            obj = i$.next();
         } while((Boolean)obj);

         return false;
      }
   }

   private static StringBuilder append(Enums.RecommendationTypeEnum typeEnum, Enums.RecommendationTargetEnum target, int targetId, int limit, int offset, boolean refreshCache) {
      StringBuilder stb = new StringBuilder();
      stb.append("typeEnum=[").append(typeEnum).append("].");
      stb.append("target=[").append(target).append("].");
      stb.append("targetId=[").append(targetId).append("].");
      stb.append("limit=[").append(limit).append("].");
      stb.append("offset=[").append(offset).append("]");
      stb.append("refreshCache=[").append(refreshCache).append("].");
      return stb;
   }

   private static RecommendationData extractRequestedPageFromRecommendation(List<RecommendationItem> recommendations, int requestedOffset, int requestedLimit) {
      RecommendationData result = new RecommendationData();
      result.setRecommendations(sortAndSliceWithSideEffect(recommendations, ORDER_BY_SCORE_DESC_COMPARATOR, requestedOffset, requestedLimit));
      return result;
   }

   protected static List<RecommendationItem> getCachedRecommendations(Enums.RecommendationTypeEnum typeEnum, Enums.RecommendationTargetEnum target, int targetId, String subtype) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RECOMMENDATIONS_CACHE_ENABLED)) {
         return null;
      } else if (typeEnum != null && target != null) {
         String cacheKey = subtype == null ? String.format("%s:%s:%d", typeEnum.toString(), target.toString(), targetId) : String.format("%s:%s:%d:%s", typeEnum.toString(), target.toString(), targetId, subtype);
         List<RecommendationItem> cachedItems = (List)recommendationsCache.get(cacheKey);
         return cachedItems == null ? null : new ArrayList(cachedItems);
      } else {
         return null;
      }
   }

   protected static List<RecommendationItem> getCachedRecommendations(Enums.RecommendationTypeEnum typeEnum, Enums.RecommendationTargetEnum target, int targetId) {
      return getCachedRecommendations(typeEnum, target, targetId, (String)null);
   }

   protected static void clearCachedRecommendations() {
      recommendationsCache.clear();
   }

   protected static boolean setCachedRecommendations(List<RecommendationItem> items, Enums.RecommendationTypeEnum typeEnum, Enums.RecommendationTargetEnum target, int targetId, String subtype) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RECOMMENDATIONS_CACHE_ENABLED)) {
         return false;
      } else if (items != null && items.size() != 0) {
         String cacheKey = subtype == null ? String.format("%s:%s:%d", typeEnum.toString(), target.toString(), targetId) : String.format("%s:%s:%d:%s", typeEnum.toString(), target.toString(), targetId, subtype);
         recommendationsCache.put(cacheKey, new ArrayList(items));
         return true;
      } else {
         return false;
      }
   }

   protected static boolean setCachedRecommendations(List<RecommendationItem> items, Enums.RecommendationTypeEnum typeEnum, Enums.RecommendationTargetEnum target, int targetId) {
      return setCachedRecommendations(items, typeEnum, target, targetId, (String)null);
   }

   protected static boolean refreshCacheExpiry() {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RECOMMENDATIONS_CACHE_ENABLED)) {
         return false;
      } else {
         long expiryInSeconds = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RECOMMENDATIONS_CACHE_EXPIRY_IN_SECONDS);
         recommendationsCache.setExpiryTimeInMilliseconds(expiryInSeconds * 1000L);
         return true;
      }
   }

   private static List<RecommendationItem> combineFeaturedRecommendation(List<RecommendationItem> original, Enums.RecommendationTypeEnum type, Enums.RecommendationTargetEnum target, int targetId) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.FEATURED_RECOMMENDATIONS_ENABLED)) {
         return original;
      } else {
         Collection<String> featuredRecommendations = getFeaturedRecommendations(type, target, targetId);
         String reason = null;
         if (type == Enums.RecommendationTypeEnum.USERRECOMMENDATION) {
            reason = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.GLOBAL_USER_RECOMMENDATION_REASON);
         }

         boolean isReasonEmpty = StringUtils.isEmpty(reason);
         Collection<RecommendationItem> featuredItems = new ArrayList(featuredRecommendations.size());
         Iterator i$ = featuredRecommendations.iterator();

         while(i$.hasNext()) {
            String featuredItemName = (String)i$.next();
            RecommendationItem featuredItem = new RecommendationItem(featuredItemName, Integer.MAX_VALUE);
            if (!isReasonEmpty) {
               featuredItem.setReason(reason);
            }

            featuredItem.setFeature(true);
            featuredItems.add(featuredItem);
         }

         original.addAll(0, featuredItems);
         return original;
      }
   }

   private static Collection<String> getFeaturedRecommendations(Enums.RecommendationTypeEnum type, Enums.RecommendationTargetEnum target, int targetId) {
      String[] featuredRecommendations = Enums.RecommendationTargetEnum.COUNTRY == target ? SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.FEATURED.forRecommendationType(type).forTargetType(target).forCountryId(targetId)) : SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.FEATURED.forRecommendationType(type));
      return new LinkedHashSet(Arrays.asList(featuredRecommendations));
   }

   protected static List<RecommendationItem> applyCustomFilterForRecommendation(List<RecommendationItem> original, Enums.RecommendationTypeEnum type, Enums.RecommendationTargetEnum target, int targetId) {
      if (type == Enums.RecommendationTypeEnum.ADDRESSBOOKCONTACTS) {
         return applyFilterForAddressBookContactsRecommendation(original);
      } else {
         if (type == Enums.RecommendationTypeEnum.HASH_TAGS && target == Enums.RecommendationTargetEnum.COUNTRY) {
            try {
               Iterator i$ = original.iterator();

               while(i$.hasNext()) {
                  RecommendationItem recommendationItem = (RecommendationItem)i$.next();
                  MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                  HashTagData htd = misEJB.getHashTagData(recommendationItem.getValue(), targetId);
                  if (htd != null) {
                     recommendationItem.setDescription(htd.getDescription());
                  }
               }
            } catch (Exception var8) {
               log.warn("Unable to get description for hashtag", var8);
            }
         }

         return original;
      }
   }

   private static List<RecommendationItem> applyFilterForAddressBookContactsRecommendation(List<RecommendationItem> original) {
      ArrayList filteredResult = new ArrayList();

      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         Iterator i$ = original.iterator();

         while(i$.hasNext()) {
            RecommendationItem item = (RecommendationItem)i$.next();
            Integer userid = Integer.valueOf(item.getValue());
            String username = userEJB.getUsernameByUserid(userid, (Connection)null);
            if (username != null) {
               UserProfileData.StatusEnum status = userEJB.getUserProfileStatus(username);
               if (status != null && status == UserProfileData.StatusEnum.PUBLIC) {
                  filteredResult.add(item);
               }
            }
         }

         return filteredResult;
      } catch (Exception var8) {
         log.error("Exception while calling applyFilterForAddressBookContactsRecommendation:" + var8.toString(), var8);
         return null;
      }
   }

   protected static List<RecommendationItem> filterBlackListedItemsFromRecommendation(List<RecommendationItem> original, Enums.RecommendationTypeEnum type, Enums.RecommendationTargetEnum target, int targetId) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RECOMMENDATIONS_BLACKLIST_FILTER_ENABLED)) {
         return original;
      } else {
         Set<String> blacklistedSet = getBlackListedRecommendations(type);
         List<RecommendationItem> itemsToRemove = new ArrayList();
         int i;
         RecommendationItem item;
         if (blacklistedSet.size() > 0) {
            for(i = 0; i < original.size(); ++i) {
               item = (RecommendationItem)original.get(i);
               if (blacklistedSet.contains(normalizeItemName(item.getValue()))) {
                  itemsToRemove.add(item);
               }
            }
         }

         for(i = 0; i < itemsToRemove.size(); ++i) {
            item = (RecommendationItem)itemsToRemove.get(i);
            original.remove(item);
         }

         return original;
      }
   }

   private static String normalizeItemName(String string) {
      return StringUtil.trimmedLowerCase(string);
   }

   private static Set<String> getBlackListedRecommendations(Enums.RecommendationTypeEnum type) {
      List<String> blacklistedRecommendations = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.BLACKLISTED.forRecommendationType(type)));
      LinkedHashSet<String> blackListedSet = new LinkedHashSet(blacklistedRecommendations.size());
      Iterator i$ = blacklistedRecommendations.iterator();

      while(i$.hasNext()) {
         String blacklistedName = (String)i$.next();
         blackListedSet.add(normalizeItemName(blacklistedName));
      }

      return blackListedSet;
   }

   public static boolean updateRecommendation(RecommendationData recommendationData, Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum target, Integer targetId, boolean replaceExisting) {
      String key = generateRecommendationKey(recommendationType, target, targetId);
      Jedis handle = null;

      boolean var8;
      try {
         handle = Redis.getMasterInstanceForEntityID(target.getRedisKeySpace(), targetId);
         if (replaceExisting) {
            handle.del(key);
         }

         Iterator i$ = recommendationData.getRecommendations().iterator();

         while(i$.hasNext()) {
            RecommendationItem item = (RecommendationItem)i$.next();
            handle.zadd(key, (double)item.getScore(), item.getValue());
         }

         boolean var14 = true;
         return var14;
      } catch (Exception var12) {
         log.error("Exception Caught while populating redis data:" + var12.getMessage(), var12);
         var8 = false;
      } finally {
         Redis.disconnect(handle, log);
      }

      return var8;
   }

   public static RecommendationData getFeaturedRecommendation(Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum target, Integer targetId, int limit, int offset) {
      List<RecommendationItem> recommendations = combineFeaturedRecommendation(new ArrayList(), recommendationType, target, targetId);
      return extractRequestedPageFromRecommendation(recommendations, offset, limit);
   }

   public static RecommendationData getBlacklistedRecommendation(Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum target, Integer targetId, int limit, int offset) {
      List<String> blacklistedRecommendations = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.BLACKLISTED.forRecommendationType(recommendationType)));
      List<RecommendationItem> items = new ArrayList();

      for(int i = 0; i < blacklistedRecommendations.size(); ++i) {
         items.add(new RecommendationItem((String)blacklistedRecommendations.get(i), Integer.MAX_VALUE));
      }

      return extractRequestedPageFromRecommendation(items, offset, limit);
   }

   public static boolean updateFeaturedRecommendation(List<String> items, Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum target, Integer targetId, boolean replaceExisting) {
      LinkedHashSet<String> featuredItems = new LinkedHashSet();
      if (!replaceExisting) {
         String[] currentfeaturedRecommendations = target == Enums.RecommendationTargetEnum.COUNTRY ? SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.FEATURED.forRecommendationType(recommendationType).forTargetType(target).forCountryId(targetId)) : SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.FEATURED.forRecommendationType(recommendationType));
         featuredItems.addAll(Arrays.asList(currentfeaturedRecommendations));
      }

      featuredItems.addAll(items);

      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         if (target == Enums.RecommendationTargetEnum.COUNTRY) {
            misEJB.updateSystemProperty(SystemPropertyEntities.RecommendationServiceSettings.FEATURED.forRecommendationType(recommendationType).forTargetType(target).forCountryId(targetId).getName(), StringUtil.join((Collection)featuredItems, ";"));
         } else {
            misEJB.updateSystemProperty(SystemPropertyEntities.RecommendationServiceSettings.FEATURED.forRecommendationType(recommendationType).getName(), StringUtil.join((Collection)featuredItems, ";"));
         }

         SystemProperty.resetCachedProperties();
         return true;
      } catch (CreateException var7) {
         log.error("EJB Create Exception caught: " + var7.getMessage(), var7);
         return false;
      } catch (RemoteException var8) {
         log.error("Remote Exception caught: " + var8.getMessage(), var8);
         return false;
      }
   }

   public static boolean updateBlacklistedRecommendation(List<String> items, Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum target, Integer targetId, boolean replaceExisting) {
      LinkedHashSet<String> blacklistedItems = new LinkedHashSet();
      if (!replaceExisting) {
         String[] currentfeaturedRecommendations = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.BLACKLISTED.forRecommendationType(recommendationType));
         blacklistedItems.addAll(Arrays.asList(currentfeaturedRecommendations));
      }

      blacklistedItems.addAll(items);

      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         misEJB.updateSystemProperty(SystemPropertyEntities.RecommendationServiceSettings.BLACKLISTED.forRecommendationType(recommendationType).getName(), StringUtil.join((Collection)blacklistedItems, ";"));
         SystemProperty.resetCachedProperties();
         return true;
      } catch (CreateException var7) {
         log.error("EJB Create Exception caught: " + var7.getMessage(), var7);
         return false;
      } catch (RemoteException var8) {
         log.error("Remote Exception caught: " + var8.getMessage(), var8);
         return false;
      }
   }

   private static SystemPropertyEntryWithParent forView(SSOEnums.View viewType, SystemPropertyEntities.RecommendationServiceSettings.RecommendationTargetSpecificSetting targetSpecificSettingProp) {
      return (SystemPropertyEntryWithParent)(viewType == null ? targetSpecificSettingProp : targetSpecificSettingProp.forViewType(viewType));
   }

   private static int getSetting(SystemPropertyEntities.RecommendationServiceSettings rootProperty, Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum targetType, SSOEnums.View viewType) {
      if (recommendationType == null) {
         throw new IllegalArgumentException("recommendationType is null");
      } else if (targetType == null) {
         throw new IllegalArgumentException("targetType is null");
      } else {
         SystemPropertyEntities.SystemPropertyEntryInterface sysProp = forView(viewType, rootProperty.forRecommendationType(recommendationType).forTargetType(targetType));
         return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)sysProp);
      }
   }

   public static int getDefaultResultSize(Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum targetType, SSOEnums.View viewType) {
      return getSetting(SystemPropertyEntities.RecommendationServiceSettings.DEFAULT_RESULT_SIZE, recommendationType, targetType, viewType);
   }

   public static int getAllowedMaxResultSize(Enums.RecommendationTypeEnum recommendationType, Enums.RecommendationTargetEnum targetType, SSOEnums.View viewType) {
      return getSetting(SystemPropertyEntities.RecommendationServiceSettings.MAX_ALLOWED_RESULT_SIZE, recommendationType, targetType, viewType);
   }

   public static <T> List<T> sortAndSliceWithSideEffect(List<T> sourceList, Comparator<T> comparator, int requestedStartIndex, int requestedCount) {
      if (sourceList == null) {
         return null;
      } else if (requestedCount <= 0) {
         return Collections.emptyList();
      } else if (requestedStartIndex >= sourceList.size()) {
         return Collections.emptyList();
      } else {
         int requestedToIndexExcl = requestedStartIndex + requestedCount;
         if (requestedToIndexExcl < 0) {
            return Collections.emptyList();
         } else {
            int toIndexExcl = requestedToIndexExcl > sourceList.size() ? sourceList.size() : requestedToIndexExcl;
            int startIndex = requestedStartIndex < 0 ? 0 : requestedStartIndex;
            if (comparator != null) {
               Collections.sort(sourceList, comparator);
            }

            return sourceList.subList(startIndex, toIndexExcl);
         }
      }
   }

   static {
      try {
         long recommendationCacheExpiryInSeconds = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RecommendationServiceSettings.RECOMMENDATIONS_CACHE_EXPIRY_IN_SECONDS);
         recommendationsCache = new LRUCache(recommendationCacheMaxSize, recommendationCacheExpiryInSeconds);
      } catch (Exception var2) {
         log.error("Unexpected exception while initializing LRUCache :" + var2.getMessage(), var2);
      }

      ORDER_BY_SCORE_DESC_COMPARATOR = new Comparator<RecommendationItem>() {
         public final int compare(RecommendationItem o1, RecommendationItem o2) {
            return o2.getScore() - o1.getScore();
         }
      };
   }

   public abstract static class RecommendationTypeEvaluator<T> {
      public abstract T evaluate(RecommendationItem var1);
   }
}
