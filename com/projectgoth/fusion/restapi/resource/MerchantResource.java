package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.FullMerchantTagDetailsData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.MerchantLocationData;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.MerchantsLocal;
import com.projectgoth.fusion.interfaces.MerchantsLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.interfaces.WebLocal;
import com.projectgoth.fusion.interfaces.WebLocalHome;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.MerchantUsernameColorData;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/merchant")
public class MerchantResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MerchantResource.class));

   @GET
   @Path("/{userid}/details")
   @Produces({"application/json"})
   public DataHolder<MerchantDetailsData> getMerchantDetails(@PathParam("userid") int userIdStr) throws FusionRestException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String username = userEJB.getUsernameByUserid(userIdStr, (Connection)null);
         MerchantDetailsData data = userEJB.getFullMerchantDetails(username);
         return new DataHolder(data);
      } catch (Exception var5) {
         log.error("Exception caught while fetching merchant details: " + var5.getMessage(), var5);
         throw new FusionRestException(101, var5.getMessage());
      }
   }

   @POST
   @Path("/{userid}/details/username_color")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<MerchantDetailsData> setUsernameColor(@PathParam("userid") Integer userId, DataHolder<MerchantUsernameColorData> jsonData) throws FusionRestException {
      try {
         MerchantDetailsData.UserNameColorTypeEnum colorType = MerchantDetailsData.UserNameColorTypeEnum.fromValue(((MerchantUsernameColorData)jsonData.data).color);
         if (colorType == null) {
            throw new Exception("Invalid value for color [" + ((MerchantUsernameColorData)jsonData.data).color + "]");
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String username = userEJB.getUsernameByUserid(userId, (Connection)null);
            if (username == null) {
               throw new Exception("Unknown user [" + userId + "]");
            } else {
               MerchantDetailsData merchantData = userEJB.getFullMerchantDetails(username);
               if (userEJB.setMerchantColorType(merchantData.id, colorType)) {
                  merchantData.usernameColorType = colorType;
                  return new DataHolder(merchantData);
               } else {
                  throw new Exception("Unable to update username color type for user " + merchantData.userName);
               }
            }
         }
      } catch (Exception var7) {
         log.error(var7.getMessage());
         throw new FusionRestException(101, var7.getMessage());
      }
   }

   @GET
   @Path("/{userid}/tags")
   @Produces({"application/json"})
   public DataHolder<Map<String, Object>> getMerchantTags(@PathParam("userid") String userIdStr, @QueryParam("page") String pageStr, @QueryParam("numRecords") String numRecordsStr) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, 0);
      if (userId <= 0) {
         throw new FusionRestException(101, String.format("Invalid userId provided."));
      } else {
         int page = StringUtil.toIntOrDefault(pageStr, 1);
         if (page < 0) {
            throw new FusionRestException(101, String.format("Invalid page parameter [%s]", pageStr));
         } else {
            int numRecords = StringUtil.toIntOrDefault(numRecordsStr, 50);
            if (numRecords < 0) {
               throw new FusionRestException(101, String.format("Invalid number of records parameter [%s]", numRecordsStr));
            } else {
               try {
                  UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  Map<String, Object> details = userEJB.getTaggedUsers(userId, page, numRecords);
                  return new DataHolder(details);
               } catch (Exception var9) {
                  log.error(var9.getMessage());
                  throw new FusionRestException(101, var9.getMessage());
               }
            }
         }
      }
   }

   @GET
   @Path("/{userid}/tags/expiring")
   @Produces({"application/json"})
   public DataHolder<Map<String, FullMerchantTagDetailsData>> getMerchantTags(@PathParam("userid") String userIdStr, @QueryParam("days") String daysStr) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, 0);
      if (userId <= 0) {
         throw new FusionRestException(101, String.format("Invalid userId provided."));
      } else {
         int days = StringUtil.toIntOrDefault(daysStr, 7);
         if (days < 0) {
            throw new FusionRestException(101, String.format("Invalid page parameter [%s]", daysStr));
         } else {
            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               Map<String, FullMerchantTagDetailsData> details = userEJB.getExpiringTaggedUsers(userId, days);
               return new DataHolder(details);
            } catch (Exception var7) {
               log.error(var7.getMessage());
               throw new FusionRestException(101, var7.getMessage());
            }
         }
      }
   }

   @GET
   @Path("/tag/minimum/{countryId}")
   @Produces({"application/json"})
   public DataHolder<Map<String, Object>> getMinimumNonTopMerchantTagDetailsInCountry(@PathParam("countryId") String countryIdStr, @QueryParam("currency") String currency) throws FusionRestException {
      boolean var3 = false;

      int countryId;
      try {
         countryId = Integer.parseInt(countryIdStr);
      } catch (Exception var8) {
         throw new FusionRestException(101, String.format("Invalid country id parameter [%s]", countryIdStr));
      }

      try {
         Map<String, Object> minTagDetails = new HashMap();
         minTagDetails.put("validity", MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         Map<String, Object> minimum = accountBean.getMinTopMerchantToNonTopMerchantTagAmount(countryId, currency);
         minTagDetails.put("amount", (Double)minimum.get("amount"));
         minTagDetails.put("currency", (String)minimum.get("currency"));
         return new DataHolder(minTagDetails);
      } catch (Exception var7) {
         log.error(var7.getMessage());
         throw new FusionRestException(101, var7.getMessage());
      }
   }

   @GET
   @Path("/tag/top/minimum")
   @Produces({"application/json"})
   public DataHolder<Map<String, Object>> getMinimumTopMerchantTagDetails(@QueryParam("currency") String currency) throws FusionRestException {
      try {
         Map<String, Object> minTagDetails = new HashMap();
         minTagDetails.put("validity", MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
         double minRequirementUSD = SystemProperty.getDouble("MinMerchantMerchantTagAmountUSD", 100.0D);
         if (currency != null && !currency.equals("USD")) {
            AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            minTagDetails.put("amount", accountBean.convertCurrency(minRequirementUSD, "USD", currency));
            minTagDetails.put("currency", currency);
         } else {
            minTagDetails.put("amount", minRequirementUSD);
            minTagDetails.put("currency", "USD");
         }

         return new DataHolder(minTagDetails);
      } catch (Exception var6) {
         log.error(var6.getMessage());
         throw new FusionRestException(101, var6.getMessage());
      }
   }

   @GET
   @Path("/tag/{username}")
   @Produces({"application/json"})
   public DataHolder<MerchantTagData> getTagFromUsername(@PathParam("username") String username) throws FusionRestException {
      try {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         MerchantTagData tag = accountBean.getMerchantTagFromUsername((Connection)null, username, true);
         return new DataHolder(tag);
      } catch (Exception var4) {
         log.error(var4.getMessage());
         throw new FusionRestException(101, var4.getMessage());
      }
   }

   @GET
   @Path("/country/{id}")
   @Produces({"application/json"})
   public DataHolder<List<MerchantLocationData>> getMerchantByCountryId(@QueryParam("requestingUserid") int requestingUserid, @PathParam("id") int id, @QueryParam("offset") int offset, @QueryParam("limit") int limit) throws FusionRestException {
      try {
         MerchantsLocal merchantsBean = (MerchantsLocal)EJBHomeCache.getLocalObject("MerchantsLocal", MerchantsLocalHome.class);
         List<MerchantLocationData> results = merchantsBean.getMerchantsByCountry(requestingUserid, id, offset, limit, true);
         return new DataHolder(results);
      } catch (Exception var7) {
         log.error("Exception caught while getting merchant location " + var7.getMessage(), var7);
         throw new FusionRestException(101, "Internal Server Error: Could not fetch merchant location.");
      }
   }

   @GET
   @Path("/country/search")
   @Produces({"application/json"})
   public DataHolder<List<MerchantLocationData>> getMerchantByCountry(@QueryParam("requestingUserid") int requestingUserid, @QueryParam("name") String name, @QueryParam("offset") int offset, @QueryParam("limit") int limit) throws FusionRestException {
      try {
         MerchantsLocal merchantsBean = (MerchantsLocal)EJBHomeCache.getLocalObject("MerchantsLocal", MerchantsLocalHome.class);
         List<MerchantLocationData> results = merchantsBean.getMerchantsByCountry(requestingUserid, name, offset, limit, true);
         return new DataHolder(results);
      } catch (Exception var7) {
         log.error("Exception caught while getting merchant location " + var7.getMessage(), var7);
         throw new FusionRestException(101, "Internal Server Error: Could not fetch merchant location.");
      }
   }

   @GET
   @Path("/countries")
   @Produces({"application/json"})
   public DataHolder<List> getCountries() throws FusionRestException {
      try {
         WebLocal webBean = (WebLocal)EJBHomeCache.getLocalObject("WebLocal", WebLocalHome.class);
         webBean.getCountriesWithMerchants();
         return new DataHolder(webBean.getCountriesWithMerchants());
      } catch (Exception var2) {
         log.error("Exception caught while removing user from blacklist: " + var2.getMessage());
         throw new FusionRestException(101, "Internal Server Error: Could not fetch merchant countries");
      }
   }

   @POST
   @Path("/{username}/resetmerchantpin")
   @Produces({"application/json"})
   public DataHolder<BooleanData> resetMerchantPin(@PathParam("username") String username) throws FusionRestException {
      try {
         Web webBean = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
         webBean.resetMerchantPin(username);
         return new DataHolder(new BooleanData(true));
      } catch (Exception var3) {
         log.error(String.format("Error in resetmerchantpin chatroom:%s", username), var3);
         throw new FusionRestException(101, "Unable to resetmerchantpin");
      }
   }
}
