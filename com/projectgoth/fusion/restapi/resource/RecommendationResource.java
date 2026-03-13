package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RecommendationData;
import com.projectgoth.fusion.recommendation.delivery.Enums;
import com.projectgoth.fusion.recommendation.delivery.RecommendationDeliveryUtils;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

@Provider
@Path("/recommendation")
public class RecommendationResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RecommendationResource.class));
   public static final String ERROR_MESSAGE_INVALID_LIMIT_PARAMETER = "please provide a valid limit";

   public static int parseLimit(String limitStr, Enums.RecommendationTypeEnum type, Enums.RecommendationTargetEnum target, SSOEnums.View viewType) {
      try {
         boolean useDefaultLimit = StringUtil.isBlank(limitStr);
         int limit = useDefaultLimit ? RecommendationDeliveryUtils.getDefaultResultSize(type, target, viewType) : Integer.parseInt(limitStr);
         if (limit < 0) {
            return -1;
         } else {
            int maxAllowedSize = RecommendationDeliveryUtils.getAllowedMaxResultSize(type, target, viewType);
            if (limit > maxAllowedSize) {
               if (useDefaultLimit) {
                  log.warn("default recommendation size [" + limit + "] is larger than max allowed [" + maxAllowedSize + "]");
               } else {
                  log.warn("receive get recommendation request with limit:[" + limit + "] but max allowed is [" + maxAllowedSize + "]");
               }

               return maxAllowedSize;
            } else {
               return limit;
            }
         }
      } catch (NumberFormatException var7) {
         return -1;
      }
   }

   @GET
   @Path("/{recommendation_type}")
   @Produces({"application/json"})
   public DataHolder<RecommendationData> getRecommendation(@PathParam("recommendation_type") String typeStr, @QueryParam("targetType") String targetTypeStr, @QueryParam("targetID") Integer targetId, @QueryParam("subtype") String subtype, @QueryParam("limit") String limitStr, @QueryParam("offset") String offsetStr, @QueryParam("refreshCache") String refreshCacheStr, @QueryParam("view") String strView) throws FusionRestException {
      log.info(String.format("getRecommendation(typeStr[%s],targetTypeStr=[%s],targetId=[%s],limitStr=[%s],offsetStr=[%s],refreshCacheStr=[%s],strView=[%s])", typeStr, targetTypeStr, targetId, limitStr, offsetStr, refreshCacheStr, strView));
      int offset = StringUtil.toIntOrDefault(offsetStr, 0);
      if (offset < 0) {
         throw new FusionRestException(101, "please provide a valid offset");
      } else if (StringUtil.isBlank(typeStr)) {
         throw new FusionRestException(101, "recommendation type can not be null");
      } else {
         Enums.RecommendationTypeEnum recommendationType = Enums.RecommendationTypeEnum.fromValue(typeStr.toUpperCase());
         if (null == recommendationType) {
            throw new FusionRestException(101, "invalid recommendation type : " + typeStr);
         } else if (StringUtil.isBlank(targetTypeStr)) {
            throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
         } else {
            Enums.RecommendationTargetEnum target = Enums.RecommendationTargetEnum.fromValue(targetTypeStr.toLowerCase());
            if (null == target) {
               throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
            } else {
               SSOEnums.View viewType = SSOEnums.View.fromValue(StringUtil.toIntOrDefault(strView, Integer.MIN_VALUE));
               int limit = parseLimit(limitStr, recommendationType, target, viewType);
               if (limit < 0) {
                  throw new FusionRestException(101, "please provide a valid limit");
               } else if (targetId == null) {
                  throw new FusionRestException(101, "please provide a valid targetID");
               } else {
                  boolean refreshCache = Boolean.parseBoolean(refreshCacheStr);

                  try {
                     RecommendationData recommendation = RecommendationDeliveryUtils.getRecommendation(recommendationType, target, targetId, subtype, limit, offset, refreshCache);
                     return new DataHolder(recommendation);
                  } catch (Exception var16) {
                     log.error(String.format("Unexpected error while retrieving recommendation for type %s targetType=%s, targetId=%d", typeStr, targetTypeStr, targetId), var16);
                     throw new FusionRestException(101, "Internal error while retrieving recommendations");
                  }
               }
            }
         }
      }
   }

   @POST
   @Path("/{recommendation_type}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response updateRecommendation(@PathParam("recommendation_type") String typeStr, @QueryParam("targetType") String targetTypeStr, @QueryParam("targetID") Integer targetId, @QueryParam("replaceExisting") String replaceExistingStr, RecommendationData recommendationData) throws FusionRestException {
      if (StringUtil.isBlank(typeStr)) {
         throw new FusionRestException(101, "recommendation type can not be null");
      } else {
         Enums.RecommendationTypeEnum recommendationType = Enums.RecommendationTypeEnum.fromValue(typeStr.toUpperCase());
         if (null == recommendationType) {
            throw new FusionRestException(101, "invalid recommendation type : " + typeStr);
         } else if (StringUtil.isBlank(targetTypeStr)) {
            throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
         } else {
            Enums.RecommendationTargetEnum target = Enums.RecommendationTargetEnum.fromValue(targetTypeStr.toLowerCase());
            if (null == target) {
               throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
            } else if (targetId == null) {
               throw new FusionRestException(101, "please provide a valid targetID");
            } else if (recommendationData == null) {
               throw new FusionRestException(101, "post body missing");
            } else {
               boolean replaceExisting = Boolean.parseBoolean(replaceExistingStr);
               if (recommendationData.getRecommendations() != null && recommendationData.getRecommendations().size() != 0) {
                  log.info(String.format("Updating recommendations for type %s targetType=%s, targetId=%d replaceExisting=%s", typeStr, targetTypeStr, targetId, replaceExisting ? "true" : "false"));

                  try {
                     RecommendationDeliveryUtils.updateRecommendation(recommendationData, recommendationType, target, targetId, replaceExisting);
                     return Response.ok().entity(new DataHolder("ok")).build();
                  } catch (Exception var10) {
                     log.error(String.format("Unexpected error while updating recommendation for type %s targetType=%s, targetId=%d", typeStr, targetTypeStr, targetId), var10);
                     throw new FusionRestException(101, "Internal error while updating recommendations");
                  }
               } else {
                  throw new FusionRestException(101, "no recommendations found in post body");
               }
            }
         }
      }
   }

   @GET
   @Path("/{recommendation_type}/featured")
   @Produces({"application/json"})
   public DataHolder<RecommendationData> getFeaturedRecommendation(@PathParam("recommendation_type") String typeStr, @QueryParam("targetType") String targetTypeStr, @QueryParam("targetID") Integer targetId, @QueryParam("limit") String limitStr, @QueryParam("offset") String offsetStr, @QueryParam("view") String strView) throws FusionRestException {
      log.info(String.format("getFeaturedRecommendation(typeStr[%s],targetTypeStr=[%s],targetId=[%s],limitStr=[%s],offsetStr=[%s],strView=[%s])", typeStr, targetTypeStr, targetId, limitStr, offsetStr, strView));
      int offset = StringUtil.toIntOrDefault(offsetStr, 0);
      if (offset < 0) {
         throw new FusionRestException(101, "please provide a valid offset and limit");
      } else if (StringUtil.isBlank(typeStr)) {
         throw new FusionRestException(101, "recommendation type can not be null");
      } else {
         Enums.RecommendationTypeEnum recommendationType = Enums.RecommendationTypeEnum.fromValue(typeStr.toUpperCase());
         if (null == recommendationType) {
            throw new FusionRestException(101, "invalid recommendation type : " + typeStr);
         } else if (StringUtil.isBlank(targetTypeStr)) {
            throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
         } else {
            Enums.RecommendationTargetEnum target = Enums.RecommendationTargetEnum.fromValue(targetTypeStr.toLowerCase());
            if (null == target) {
               throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
            } else {
               SSOEnums.View viewType = SSOEnums.View.fromValue(StringUtil.toIntOrDefault(strView, Integer.MIN_VALUE));
               int limit = parseLimit(limitStr, recommendationType, target, viewType);
               if (limit < 0) {
                  throw new FusionRestException(101, "please provide a valid limit");
               } else if (targetId == null) {
                  throw new FusionRestException(101, "please provide a valid targetID");
               } else {
                  try {
                     RecommendationData recommendation = RecommendationDeliveryUtils.getFeaturedRecommendation(recommendationType, target, targetId, limit, offset);
                     return new DataHolder(recommendation);
                  } catch (Exception var13) {
                     log.error(String.format("Unexpected error while retrieving featured recommendation for type %s targetType=%s, targetId=%d", typeStr, targetTypeStr, targetId), var13);
                     throw new FusionRestException(101, "Internal error while retrieving featured recommendations");
                  }
               }
            }
         }
      }
   }

   @POST
   @Path("/{recommendation_type}/featured")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response updateFeaturedRecommendation(@PathParam("recommendation_type") String typeStr, @QueryParam("targetType") String targetTypeStr, @QueryParam("targetID") Integer targetId, @QueryParam("replaceExisting") String replaceExistingStr, String jsonBodyStr) throws FusionRestException {
      if (StringUtil.isBlank(typeStr)) {
         throw new FusionRestException(101, "recommendation type can not be null");
      } else {
         Enums.RecommendationTypeEnum recommendationType = Enums.RecommendationTypeEnum.fromValue(typeStr.toUpperCase());
         if (null == recommendationType) {
            throw new FusionRestException(101, "invalid recommendation type : " + typeStr);
         } else if (StringUtil.isBlank(targetTypeStr)) {
            throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
         } else {
            Enums.RecommendationTargetEnum target = Enums.RecommendationTargetEnum.fromValue(targetTypeStr.toLowerCase());
            if (null == target) {
               throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
            } else if (targetId == null) {
               throw new FusionRestException(101, "please provide a valid targetID");
            } else if (StringUtil.isBlank(jsonBodyStr)) {
               throw new FusionRestException(101, "post body missing");
            } else {
               boolean replaceExisting = Boolean.parseBoolean(replaceExistingStr);
               ArrayList items = new ArrayList();

               try {
                  JSONObject jsonBody = new JSONObject(jsonBodyStr);
                  JSONArray jsonArr = jsonBody.getJSONArray("data");

                  for(int i = 0; i < jsonArr.length(); ++i) {
                     items.add(jsonArr.getString(i));
                  }
               } catch (Exception var14) {
                  log.error("Unable to parse JSON Body: " + var14.getMessage(), var14);
                  throw new FusionRestException(101, "Unable to parse JSON Body :" + var14.getMessage());
               }

               try {
                  RecommendationDeliveryUtils.updateFeaturedRecommendation(items, recommendationType, target, targetId, replaceExisting);
                  return Response.ok().entity(new DataHolder("ok")).build();
               } catch (Exception var13) {
                  log.error(String.format("Unexpected error while updating recommendation for type %s targetType=%s, targetId=%d", typeStr, targetTypeStr, targetId), var13);
                  throw new FusionRestException(101, "Internal error while updating recommendations");
               }
            }
         }
      }
   }

   @GET
   @Path("/{recommendation_type}/blacklisted")
   @Produces({"application/json"})
   public DataHolder<RecommendationData> getBlacklistedRecommendation(@PathParam("recommendation_type") String typeStr, @QueryParam("targetType") String targetTypeStr, @QueryParam("targetID") Integer targetId, @QueryParam("limit") String limitStr, @QueryParam("offset") String offsetStr, @QueryParam("view") String strView) throws FusionRestException {
      int offset = StringUtil.toIntOrDefault(offsetStr, 0);
      if (offset < 0) {
         throw new FusionRestException(101, "please provide a valid offset");
      } else if (StringUtil.isBlank(typeStr)) {
         throw new FusionRestException(101, "recommendation type can not be null");
      } else {
         Enums.RecommendationTypeEnum recommendationType = Enums.RecommendationTypeEnum.fromValue(typeStr.toUpperCase());
         if (null == recommendationType) {
            throw new FusionRestException(101, "invalid recommendation type : " + typeStr);
         } else if (StringUtil.isBlank(targetTypeStr)) {
            throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
         } else {
            Enums.RecommendationTargetEnum target = Enums.RecommendationTargetEnum.fromValue(targetTypeStr.toLowerCase());
            if (null == target) {
               throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
            } else {
               SSOEnums.View viewType = SSOEnums.View.fromValue(StringUtil.toIntOrDefault(strView, Integer.MIN_VALUE));
               int limit = parseLimit(limitStr, recommendationType, target, viewType);
               if (limit < 0) {
                  throw new FusionRestException(101, "please provide a valid limit");
               } else if (targetId == null) {
                  throw new FusionRestException(101, "please provide a valid targetID");
               } else {
                  try {
                     RecommendationData recommendation = RecommendationDeliveryUtils.getBlacklistedRecommendation(recommendationType, target, targetId, limit, offset);
                     return new DataHolder(recommendation);
                  } catch (Exception var13) {
                     log.error(String.format("Unexpected error while retrieving blacklisted recommendation for type %s targetType=%s, targetId=%d", typeStr, targetTypeStr, targetId), var13);
                     throw new FusionRestException(101, "Internal error while retrieving blacklisted recommendations");
                  }
               }
            }
         }
      }
   }

   @POST
   @Path("/{recommendation_type}/blacklisted")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response updateBlacklistedRecommendations(@PathParam("recommendation_type") String typeStr, @QueryParam("targetType") String targetTypeStr, @QueryParam("targetID") Integer targetId, @QueryParam("replaceExisting") String replaceExistingStr, String jsonBodyStr) throws FusionRestException {
      if (StringUtil.isBlank(typeStr)) {
         throw new FusionRestException(101, "recommendation type can not be null");
      } else {
         Enums.RecommendationTypeEnum recommendationType = Enums.RecommendationTypeEnum.fromValue(typeStr.toUpperCase());
         if (null == recommendationType) {
            throw new FusionRestException(101, "invalid recommendation type : " + typeStr);
         } else if (StringUtil.isBlank(targetTypeStr)) {
            throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
         } else {
            Enums.RecommendationTargetEnum target = Enums.RecommendationTargetEnum.fromValue(targetTypeStr.toLowerCase());
            if (null == target) {
               throw new FusionRestException(101, "invalid target type : " + targetTypeStr);
            } else if (targetId == null) {
               throw new FusionRestException(101, "please provide a valid targetID");
            } else if (StringUtil.isBlank(jsonBodyStr)) {
               throw new FusionRestException(101, "post body missing");
            } else {
               boolean replaceExisting = Boolean.parseBoolean(replaceExistingStr);
               ArrayList items = new ArrayList();

               try {
                  JSONObject jsonBody = new JSONObject(jsonBodyStr);
                  JSONArray jsonArr = jsonBody.getJSONArray("data");

                  for(int i = 0; i < jsonArr.length(); ++i) {
                     items.add(jsonArr.getString(i));
                  }
               } catch (Exception var14) {
                  log.error("Unable to parse JSON Body: " + var14.getMessage(), var14);
                  throw new FusionRestException(101, "Unable to parse JSON Body :" + var14.getMessage());
               }

               try {
                  RecommendationDeliveryUtils.updateBlacklistedRecommendation(items, recommendationType, target, targetId, replaceExisting);
                  return Response.ok().entity(new DataHolder("ok")).build();
               } catch (Exception var13) {
                  log.error(String.format("Unexpected error while updating recommendation for type %s targetType=%s, targetId=%d", typeStr, targetTypeStr, targetId), var13);
                  throw new FusionRestException(101, "Internal error while updating recommendations");
               }
            }
         }
      }
   }
}
