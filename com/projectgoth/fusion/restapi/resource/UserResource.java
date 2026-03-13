package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.MerchantLocationData;
import com.projectgoth.fusion.data.RegistrationContextData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.data.StoreItemInventoryData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserDataAndRegistrationContextData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VirtualGiftReceivedData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.eventqueue.EventQueue;
import com.projectgoth.fusion.eventqueue.events.ThirdPartySiteCredentialUpdatedEvent;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.ContactLocal;
import com.projectgoth.fusion.interfaces.ContactLocalHome;
import com.projectgoth.fusion.interfaces.ContentLocal;
import com.projectgoth.fusion.interfaces.ContentLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.MerchantsLocal;
import com.projectgoth.fusion.interfaces.MerchantsLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.invitation.InvitationData;
import com.projectgoth.fusion.invitation.InvitationResponseData;
import com.projectgoth.fusion.invitation.InvitationUtils;
import com.projectgoth.fusion.invitation.restapi.data.InvitationDetailsData;
import com.projectgoth.fusion.invitation.restapi.data.SendingInvitationData;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FacebookCredentialData;
import com.projectgoth.fusion.restapi.data.FileUploadData;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.RSSFeedForUser;
import com.projectgoth.fusion.restapi.data.RSSFeedForUserList;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.data.SettingsEnums;
import com.projectgoth.fusion.restapi.data.SettingsProfileDetailsData;
import com.projectgoth.fusion.restapi.data.StringData;
import com.projectgoth.fusion.restapi.data.UpdateUnfundedBalanceData;
import com.projectgoth.fusion.restapi.data.UserAlerts;
import com.projectgoth.fusion.restapi.data.UserCreationData;
import com.projectgoth.fusion.restapi.data.UserEmoticonList;
import com.projectgoth.fusion.restapi.data.UserMigboProfileData;
import com.projectgoth.fusion.restapi.data.UserUserRelationshipData;
import com.projectgoth.fusion.restapi.data.UserVerificationData;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import com.projectgoth.fusion.restapi.util.RedisDataUtil;
import com.projectgoth.fusion.restapi.util.ResourceUtil;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.thirdpartysites.ThirdPartySiteCredentialManager;
import com.projectgoth.fusion.uns.UserNotificationServiceI;
import com.projectgoth.s3uploader.S3Uploader;
import com.projectgoth.s3uploader.S3UploaderConfiguration;
import com.projectgoth.s3uploader.S3UploaderContentTypeEnum;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

@Provider
@Path("/user")
public class UserResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserResource.class));

   @GET
   @Path("/{userid}/profile")
   @Produces({"application/json"})
   public DataHolder<UserMigboProfileData> getMigboUserProfile(@PathParam("userid") String useridOrUsername, @QueryParam("useUsername") String useUsernameStr, @QueryParam("relatedUserid") String relatedUseridStr) throws FusionRestException {
      boolean useUsername = StringUtil.toBooleanOrDefault(useUsernameStr, false);

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int userid = -1;
         if (!useUsername) {
            userid = StringUtil.toIntOrDefault(useridOrUsername, -1);
            if (userid == -1) {
               log.warn(String.format("useUsername is false '%s', but userid '%s' is invalid, treating it as username anyway", useUsername, useridOrUsername));
               useUsername = true;
            }
         }

         UserMigboProfileData profile = useUsername ? UserMigboProfileData.createUserMigboProfileData(useridOrUsername, userEJB, relatedUseridStr) : UserMigboProfileData.createUserMigboProfileData(userid, userEJB, relatedUseridStr);
         return new DataHolder(profile);
      } catch (CreateException var8) {
         log.error(String.format("Failed to retrieve user profile '%s' due to EJB CreateException: %s", useridOrUsername, var8.getMessage()), var8);
         throw new FusionRestException(101, "Internal error while retrieving user data");
      } catch (FusionRestException var9) {
         throw var9;
      } catch (Exception var10) {
         log.error(String.format("Unexpected error during retreival of user profile %s", useridOrUsername), var10);
         throw new FusionRestException(101, "Internal error while retrieving user data");
      }
   }

   @POST
   @Path("/profile/fetchlist")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<List<UserMigboProfileData>> getProfileList(String jsonData) throws FusionRestException {
      try {
         JSONObject containerObject = new JSONObject(jsonData);
         JSONObject dataObject = containerObject.getJSONObject("data");
         JSONArray userIdArray = dataObject.getJSONArray("userIds");
         int numberOfUserIds = userIdArray.length();
         ArrayList<UserMigboProfileData> resultList = new ArrayList(numberOfUserIds);
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);

         for(int i = 0; i < numberOfUserIds; ++i) {
            int userId = userIdArray.getInt(i);

            try {
               UserMigboProfileData profile = UserMigboProfileData.createUserMigboProfileData(userId, userEJB, (String)null);
               resultList.add(profile);
            } catch (Exception var11) {
               log.error(String.format("Unexpected error during retreival of user profile %d during fetchlist", userId), var11);
            }
         }

         if (resultList.size() == 0) {
            throw new FusionRestException(101, "Internal error while retrieving user data");
         } else {
            return new DataHolder(resultList);
         }
      } catch (FusionRestException var12) {
         throw var12;
      } catch (Exception var13) {
         log.error("Unexpected error during retreival of profile list", var13);
         throw new FusionRestException(101, "Internal error while retrieving user data");
      }
   }

   @GET
   @Path("/{userid}/miglevel")
   @Produces({"application/json"})
   public DataHolder<String> getMiglevel(@PathParam("userid") String useridOrUsername, @QueryParam("useUsername") String useUsernameStr) throws FusionRestException {
      boolean useUsername = StringUtil.toBooleanOrDefault(useUsernameStr, false);

      try {
         int userid = -1;
         String username = null;
         if (!useUsername) {
            userid = StringUtil.toIntOrDefault(useridOrUsername, -1);
            if (userid == -1) {
               log.warn(String.format("useUsername is false '%s', but userid '%s' is invalid, treating it as username anyway", useUsername, useridOrUsername));
               username = useridOrUsername;
            }
         }

         int migLevel = true;
         int migLevel;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
            if (userid == -1) {
               migLevel = (new UserObject(username)).getReputationLevel().level;
            } else {
               migLevel = (new UserObject(userid)).getReputationLevel().level;
            }
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            if (userid == -1) {
               userid = userEJB.getUserID(useridOrUsername, (Connection)null, false);
               if (userid == -1) {
                  throw new FusionRestException(101, String.format("Invalid username %s", useridOrUsername));
               }

               migLevel = MemCacheOrEJB.getUserReputationLevel(useridOrUsername);
            } else {
               migLevel = userEJB.getReputationLevelByUserid(userid).level;
            }
         }

         return new DataHolder(Integer.toString(migLevel));
      } catch (CreateException var8) {
         log.error(String.format("Failed to retrieve user miglevel '%s' due to EJB CreateException: %s", useridOrUsername, var8.getMessage()), var8);
         throw new FusionRestException(101, "Internal error while retrieving user miglevel");
      } catch (FusionRestException var9) {
         throw var9;
      } catch (Exception var10) {
         log.error(String.format("Unexpected error during retreival of user miglevel %s", useridOrUsername), var10);
         throw new FusionRestException(101, "Internal error while retrieving user miglevel");
      }
   }

   @POST
   @Path("/{userid}/profile/{viewerUserid}/view")
   @Produces({"application/json"})
   public DataHolder<BooleanData> recordFootprints(@PathParam("userid") String userIdStr, @PathParam("viewerUserid") String viewerUseridStr) throws FusionRestException {
      if (!SystemProperty.getBool("recordFootprintsEnabled", false)) {
         return new DataHolder(new BooleanData(false));
      } else {
         int userId = this.getAndCheckUserid(userIdStr);
         int viewerUserid = StringUtil.toIntOrDefault(viewerUseridStr, -1);
         if (viewerUserid == -1) {
            throw new FusionRestException(101, String.format("Invalid requesting user ID [%s]", viewerUseridStr));
         } else {
            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
               UserData viewedUserData = userEJB.loadUserFromID(userId);
               if (viewedUserData == null) {
                  throw new FusionRestException(101, String.format("Invalid user ID [%s] - not found", userIdStr));
               } else {
                  CountryData viewedCountryData = misEJB.getCountry(viewedUserData.countryID);
                  UserData viewerUserData = userEJB.loadUserFromID(viewerUserid);
                  if (viewerUserData == null) {
                     throw new FusionRestException(101, String.format("Invalid viewer user ID [%s] - not found", viewerUseridStr));
                  } else {
                     CountryData viewerCountryData = misEJB.getCountry(viewerUserData.countryID);
                     BooleanData data = new BooleanData(RedisDataUtil.recordProfileView(viewedUserData, viewedCountryData, viewerUserData, viewerCountryData));
                     return new DataHolder(data);
                  }
               }
            } catch (Exception var12) {
               log.error("Exception caught while recording footprints for user: " + var12.getMessage());
               BooleanData data = new BooleanData(false);
               return new DataHolder(data);
            }
         }
      }
   }

   @GET
   @Path("/{userid}/friends")
   @Produces({"application/json"})
   public DataHolder<Map<String, List<Integer>>> getUserFriends(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userID = -1;
      LinkedList userIDList = new LinkedList();

      try {
         userID = Integer.parseInt(userIdStr);
      } catch (Exception var11) {
      }

      if (userID == -1) {
         log.warn(String.format("Failed to retrieve user friends for '%d'", userIdStr));
         throw new FusionRestException(101, "Incorrect userid specified");
      } else {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String username = userEJB.getUsernameByUserid(userID, (Connection)null);
            Set<String> bcl = userEJB.loadBroadcastList(username, (Connection)null);
            Iterator iter = bcl.iterator();

            while(iter.hasNext()) {
               int friendID = userEJB.getUserID((String)iter.next(), (Connection)null, false);
               if (friendID > 0) {
                  userIDList.add(friendID);
               }
            }

            HashMap<String, List<Integer>> map = new HashMap();
            map.put("friends", userIDList);
            return new DataHolder(map);
         } catch (SQLException var9) {
            log.error(String.format("Failed to retrieve user friends '%s' due to SQLException: %s", userIdStr, var9.getMessage()), var9);
            throw new FusionRestException(101, "Internal error while retrieving user friends");
         } catch (CreateException var10) {
            log.error(String.format("Failed to retrieve user friends '%s' due to EJB CreateException: %s", userIdStr, var10.getMessage()), var10);
            throw new FusionRestException(101, "Internal error while retrieving user friends");
         }
      }
   }

   @GET
   @Path("/{userid}/user/{relatedUserid}")
   @Produces({"application/json"})
   public DataHolder<UserUserRelationshipData> getUserRelationship(@PathParam("userid") String useridStr, @PathParam("relatedUserid") String relatedUseridStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      int relatedUserid = StringUtil.toIntOrDefault(relatedUseridStr, -1);

      try {
         ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserUserRelationshipData data = new UserUserRelationshipData();

         try {
            if (userid == -1 || relatedUserid == -1) {
               log.error(String.format("Invalid input provided to the getUserRelationship() API userid[%s] relatedUserid[%s]", useridStr, relatedUseridStr));
               throw new FusionRestException(101, "Internal error while retrieving user relationship data");
            }

            String username = userEJB.getUsernameByUserid(userid, (Connection)null);
            String relatedUsername = userEJB.getUsernameByUserid(relatedUserid, (Connection)null);
            data.isFriend = contactEJB.isFriend(userid, relatedUserid);
            data.isBlockedBy = contactEJB.isBlocking(relatedUsername, username);
            data.isBlocking = contactEJB.isBlocking(username, relatedUsername);
         } catch (FusionEJBException var10) {
            log.error(String.format("Failed to retrieve user relationship '%s' '%s': %s", useridStr, relatedUseridStr, var10.getMessage()));
            throw new FusionRestException(101, "Internal error while retrieving user relationship data");
         }

         return new DataHolder(data);
      } catch (CreateException var11) {
         log.error(String.format("Failed to retrieve user relationship '%s' '%s' due to EJB CreateException: %s", useridStr, relatedUseridStr, var11.getMessage()), var11);
         throw new FusionRestException(101, "Internal error while retrieving user relationship data");
      }
   }

   @POST
   @Path("/{userid}/user/{relatedUserid}")
   @Produces({"application/json"})
   public Response updateUserRelationship(@PathParam("userid") String useridStr, @PathParam("relatedUserid") String relatedUseridStr, @QueryParam("action") String actionStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      int relatedUserid = StringUtil.toIntOrDefault(relatedUseridStr, -1);

      try {
         ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String requestorUsername = userEJB.getUsernameByUserid(userid, (Connection)null);
         String relatedUsername = userEJB.getUsernameByUserid(relatedUserid, (Connection)null);
         boolean isBlocking = contactEJB.isBlocking(requestorUsername, relatedUsername);
         if ("@block".equals(actionStr)) {
            if (!isBlocking) {
               contactEJB.blockContact(userid, requestorUsername, relatedUsername);
            } else {
               log.warn(String.format("Ignoring block request as [%d][%s] is already blocking [%d][%s]", userid, requestorUsername, relatedUserid, relatedUsername));
            }
         } else {
            if (!"@unblock".equals(actionStr)) {
               log.error(String.format("Invalid action [%s] current_status[%s]", actionStr, isBlocking ? "blocked" : "unblocked"));
               throw new FusionRestException(101, String.format("Invalid action [%s] current_status[%s]", actionStr, isBlocking ? "blocked" : "unblocked"));
            }

            if (isBlocking) {
               boolean shareMobilePhone = false;
               contactEJB.unblockContact(requestorUsername, relatedUsername, shareMobilePhone);
            } else {
               log.warn(String.format("Ignoring unblock request as [%d][%s] is currently not blocking [%d][%s]", userid, requestorUsername, relatedUserid, relatedUsername));
            }
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      } catch (FusionEJBException var12) {
         log.error(String.format("Failed to [%s] user '%s' by '%s' due to FusionEJBException: %s", actionStr, relatedUseridStr, useridStr, var12.getMessage()), var12);
      } catch (CreateException var13) {
         log.error(String.format("Failed to [%s] user '%s' by '%s' due to EJB CreateException: %s", actionStr, relatedUseridStr, useridStr, var13.getMessage()), var13);
      }

      throw new FusionRestException(101, "Internal error while retrieving user relationship data");
   }

   public static Map<String, Integer> getAllPrivacy(int userid) throws FusionRestException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         Map<String, Integer> privacy = RedisDataUtil.getAllAccountPrivacy(userid);
         String username = null;
         Map<String, Integer> privacyToWrite = new HashMap();
         SettingsEnums.EveryoneOrFollowerAndFriend feed = null;
         if (!privacy.containsKey("FeedPv")) {
            username = userEJB.getUsernameByUserid(userid, (Connection)null);
            feed = SettingsResource.getOldFeedContentPrivacy(userid, username, userEJB);
            privacy.put("FeedPv", feed.value());
            privacyToWrite.put("FeedPv", feed.value());
         } else {
            feed = SettingsEnums.EveryoneOrFollowerAndFriend.fromValue((Integer)privacy.get("FeedPv"));
         }

         if (!privacy.containsKey("DobPrivacy")) {
            SettingsEnums.Birthday dobNew = SettingsResource.getMigratedBirthdayPrivacy(userid, username, feed);
            privacy.put("DobPrivacy", dobNew.value());
            privacyToWrite.put("DobPrivacy", dobNew.value());
         }

         if (!privacy.containsKey("FLNamePv")) {
            SettingsEnums.ShowHide privacyNew = SettingsProfileDetailsData.PRIVACY_DEFAULT_FIRSTLASTNAME;
            privacy.put("FLNamePv", privacyNew.value());
            privacyToWrite.put("FLNamePv", privacyNew.value());
         }

         if (!privacy.containsKey("ExtEmPv")) {
            SettingsEnums.EveryoneFollowerFriendHide privacyNew = SettingsProfileDetailsData.PRIVACY_DEFAULT_EXTERNALEMAIL;
            privacy.put("ExtEmPv", privacyNew.value());
            privacyToWrite.put("ExtEmPv", privacyNew.value());
         }

         if (!privacy.containsKey("MobNumPrivacy")) {
            SettingsEnums.EveryoneFriendHide privacyNew = SettingsProfileDetailsData.PRIVACY_DEFAULT_MOBILEPHONE;
            privacy.put("MobNumPrivacy", privacyNew.value());
            privacyToWrite.put("MobNumPrivacy", privacyNew.value());
         }

         if (!privacy.containsKey("ChatPv")) {
            if (username == null) {
               username = userEJB.getUsernameByUserid(userid, (Connection)null);
            }

            SettingsEnums.EveryoneFollowerFriend chatNew = SettingsResource.getMigratedChatPrivacy(userid, username, userEJB, feed);
            privacy.put("ChatPv", chatNew.value());
            privacyToWrite.put("ChatPv", chatNew.value());
         }

         if (!privacy.containsKey("BuzzPv")) {
            SettingsEnums.OnOff buzzNew = SettingsResource.getMigratedBuzzPrivacy(userid, username, userEJB);
            privacy.put("BuzzPv", buzzNew.value());
            privacyToWrite.put("BuzzPv", buzzNew.value());
         }

         if (privacyToWrite.size() > 0 && !RedisDataUtil.setAccountPrivacy(userid, privacyToWrite)) {
            log.error(String.format("Failed to save user user privacy '%d'", userid));
         }

         return privacy;
      } catch (CreateException var7) {
         log.error(String.format("Failed to retrieve user privacy '%d' due to EJB CreateException: %s", userid, var7.getMessage()));
         throw new FusionRestException(101, "Internal error while retrieving user privacy");
      }
   }

   @GET
   @Path("/{userid}/privacy")
   @Produces({"application/json"})
   public DataHolder<Map<String, Integer>> getPrivacy(@PathParam("userid") String useridStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to retrieve user privacy due to invalid userid '%s'", useridStr));
         throw new FusionRestException(101, "Internal error while retrieving user privacy");
      } else {
         return new DataHolder(getAllPrivacy(userid));
      }
   }

   @GET
   @Path("/{userid}/alerts/unread/count")
   @Produces({"application/json"})
   public DataHolder<Map<Integer, Integer>> getAlertsUnreadCount(@PathParam("userid") String useridStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to retrieve user alerts unread count due to invalid userid '%s'", useridStr));
         throw new FusionRestException(101, "Internal error while retrieving user alerts unread count");
      } else {
         try {
            UserNotificationServicePrx unsPrx = EJBIcePrxFinder.getUserNotificationServiceProxy();
            if (unsPrx != null) {
               try {
                  Map<Integer, Integer> data = unsPrx.getUnreadNotificationCountForUser(userid);
                  return new DataHolder(data);
               } catch (FusionException var6) {
                  log.error(String.format("Failed to retrieve user alerts unread count: FusionException '%s'", var6.message));
                  throw new FusionRestException(102, "Internal error while retrieving user alerts unread count");
               }
            } else {
               log.error(String.format("Failed to retrieve user alerts unread count: unable to find UserNotificationService"));
               throw new FusionRestException(101, "Internal error while retrieving user alerts unread count");
            }
         } catch (EJBException var7) {
            log.error(String.format("Failed to retrieve user alerts unread count: EJBException '%s'", var7.getMessage()));
            throw new FusionRestException(101, "Internal error while retrieving user alerts unread count");
         }
      }
   }

   @GET
   @Path("/{userid}/alerts/unread")
   @Produces({"application/json"})
   public DataHolder<UserAlerts> getAlertsUnread(@PathParam("userid") String useridStr, @QueryParam("limit") String limitStr, @QueryParam("offset") String offsetStr) throws FusionRestException {
      int offset = StringUtil.toIntOrDefault(offsetStr, 0);
      int limit = StringUtil.toIntOrDefault(limitStr, 33);
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to retrieve user alerts due to invalid userid '%s'", useridStr));
         throw new FusionRestException(101, "Internal error while retrieving user alerts");
      } else {
         try {
            UserNotificationServicePrx unsPrx = EJBIcePrxFinder.getUserNotificationServiceProxy();
            if (unsPrx != null) {
               try {
                  Map<Integer, Integer> unreadCountMap = unsPrx.getUnreadNotificationCountForUser(userid);
                  Map<Integer, Map<String, Map<String, String>>> data = unsPrx.getUnreadPendingNotificationDataForUser(userid);
                  int unreadCount = 0;
                  UserAlerts alerts = new UserAlerts();
                  Iterator i$ = data.keySet().iterator();

                  while(i$.hasNext()) {
                     Integer key = (Integer)i$.next();
                     if (unreadCountMap.containsKey(key)) {
                        unreadCount += (Integer)unreadCountMap.get(key);
                     }
                  }

                  alerts.alerts = data;
                  alerts.unread = unreadCount;
                  return new DataHolder(alerts.retrievePage(offset, limit));
               } catch (FusionException var14) {
                  log.error(String.format("Failed to retrieve user alerts: FusionException '%s'", var14.message));
                  throw new FusionRestException(102, "Internal error while retrieving user alerts");
               }
            } else {
               log.error(String.format("Failed to retrieve user alerts: unable to find UserNotificationService"));
               throw new FusionRestException(101, "Internal error while retrieving user alerts");
            }
         } catch (EJBException var15) {
            log.error(String.format("Failed to retrieve user alerts: EJBException '%s'", var15.getMessage()));
            throw new FusionRestException(101, "Internal error while retrieving user alerts");
         }
      }
   }

   @GET
   @Path("/{userid}/alerts")
   @Produces({"application/json"})
   public DataHolder<UserAlerts> getAlerts(@PathParam("userid") String useridStr, @QueryParam("limit") String limitStr, @QueryParam("offset") String offsetStr) throws FusionRestException {
      int offset = StringUtil.toIntOrDefault(offsetStr, 0);
      int limit = StringUtil.toIntOrDefault(limitStr, 33);
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to retrieve user alerts due to invalid userid '%s'", useridStr));
         throw new FusionRestException(101, "Internal error while retrieving user alerts");
      } else {
         try {
            UserNotificationServicePrx unsPrx = EJBIcePrxFinder.getUserNotificationServiceProxy();
            if (unsPrx == null) {
               log.error(String.format("Failed to retrieve user alerts: unable to find UserNotificationService"));
               throw new FusionRestException(101, "Internal error while retrieving user alerts");
            } else {
               try {
                  Map<Integer, Integer> unreadCountMap = unsPrx.getUnreadNotificationCountForUser(userid);
                  Map<Integer, Map<String, Map<String, String>>> data = unsPrx.getPendingNotificationDataForUser(userid);
                  Iterator i$;
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.PERSISTENT_ALERT_RECOUNT_ENABLED)) {
                     Jedis masterInstance = null;

                     try {
                        masterInstance = Redis.getMasterInstanceForUserID(userid);
                        i$ = Enums.NotificationTypeEnum.PERSISTENT_SET.iterator();

                        while(i$.hasNext()) {
                           Enums.NotificationTypeEnum typeEnum = (Enums.NotificationTypeEnum)i$.next();
                           int type = typeEnum.getType();
                           if (unreadCountMap.containsKey(type)) {
                              int size = data.get(type) == null ? 0 : ((Map)data.get(type)).size();
                              if ((Integer)unreadCountMap.get(type) != size) {
                                 log.debug(String.format("Unread alert count was out of sync for User [%s], Type [%s], Old [%s], New [%s]. Resyncing...", userid, typeEnum, unreadCountMap.get(type), size));
                                 masterInstance.set(UserNotificationServiceI.getUnreadCountUnsKey(userid, type), "" + size);
                                 unreadCountMap.put(type, size);
                              }
                           }
                        }
                     } catch (Exception var21) {
                        log.error("Failed to set unread persistent alert count for User ID: " + userid, var21);
                     } finally {
                        Redis.disconnect(masterInstance, log);
                     }
                  }

                  int unreadCount = 0;
                  i$ = data.keySet().iterator();

                  while(i$.hasNext()) {
                     Integer key = (Integer)i$.next();
                     if (unreadCountMap.containsKey(key)) {
                        unreadCount += (Integer)unreadCountMap.get(key);
                     }
                  }

                  UserAlerts alerts = new UserAlerts();
                  alerts.alerts = data;
                  alerts.unread = unreadCount;
                  return new DataHolder(alerts.retrievePage(offset, limit));
               } catch (FusionException var23) {
                  log.error(String.format("Failed to retrieve user alerts: FusionException '%s'", var23.message));
                  throw new FusionRestException(102, "Internal error while retrieving user alerts");
               }
            }
         } catch (EJBException var24) {
            log.error(String.format("Failed to retrieve user alerts: EJBException '%s'", var24.getMessage()));
            throw new FusionRestException(101, "Internal error while retrieving user alerts");
         }
      }
   }

   @POST
   @Path("/{userid}/alert_response")
   @Produces({"application/json"})
   public Response alertResponse(@PathParam("userid") String useridStr, @QueryParam("alertType") String alertTypeStr, @QueryParam("alertKey") String alertKey, @QueryParam("responseType") String responseTypeStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to retrieve user alerts due to invalid userid '%s'", useridStr));
         throw new FusionRestException(101, "Internal error while retrieving user alerts");
      } else {
         int alertType = StringUtil.toIntOrDefault(alertTypeStr, -1);
         if (alertType == -1) {
            try {
               Enums.NotificationTypeEnum type = Enums.NotificationTypeEnum.valueOf(alertTypeStr);
               alertType = type.getType();
            } catch (Exception var12) {
            }
         } else if (!Enums.NotificationTypeEnum.isForPersistent(alertType)) {
            log.error(String.format("Can not response to a non-important alert, alertTypeStr '%s'", alertType));
            throw new FusionRestException(101, "Internal error while retrieving alert type");
         }

         int responseTypeInt = StringUtil.toIntOrDefault(responseTypeStr, -1);
         if (responseTypeInt == -1) {
            log.error(String.format("Failed to retrieve alert responseType due to invalid responseTypeStr '%s'", responseTypeStr));
            throw new FusionRestException(101, "Internal error while retrieving alert type");
         } else {
            InvitationResponseData.ResponseType responseType = InvitationResponseData.ResponseType.fromTypeCode(responseTypeInt);
            if (responseType == null) {
               log.error(String.format("Failed to retrieve alert responseType due to invalid responseTypeStr '%s'", responseType));
               throw new FusionRestException(101, "Internal error while retrieving alert type");
            } else if (responseType == InvitationResponseData.ResponseType.SIGN_UP_VERIFIED) {
               log.error(String.format("Invalid responseTypeStr '%s', should not be handled in alertResponse", responseType));
               throw new FusionRestException(101, "Internal error while retrieving alert type");
            } else {
               try {
                  UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  if (alertType == -1) {
                     Iterator i$ = Enums.NotificationTypeEnum.ACCUMULATED_SET.iterator();

                     while(i$.hasNext()) {
                        Enums.NotificationTypeEnum t = (Enums.NotificationTypeEnum)i$.next();
                        userBean.responseToAlert(t.getType(), alertKey, userid, responseType);
                     }
                  } else {
                     userBean.responseToAlert(alertType, alertKey, userid, responseType);
                  }

                  return Response.ok().entity(new DataHolder("ok")).build();
               } catch (CreateException var13) {
                  log.error(String.format("Failed to response to alert, userid:%s alertType:%s, alertKey:%s: EJBException '%s'", userid, alertTypeStr, alertKey, var13.getMessage()));
               } catch (FusionEJBException var14) {
                  log.error(String.format("Failed to response to alert, userid:%s alertType:%s, alertKey:%s: EJBException '%s'", userid, alertTypeStr, alertKey, var14.getMessage()));
               } catch (EJBException var15) {
                  log.error(String.format("Failed to response to alert, userid:%s alertType:%s, alertKey:%s: EJBException '%s'", userid, alertTypeStr, alertKey, var15.getMessage()));
               }

               throw new FusionRestException(101, "Internal error while retrieving user alerts");
            }
         }
      }
   }

   @GET
   @Path("/{userid}/response")
   @Produces({"application/json"})
   public DataHolder<UserAlerts> responseAlert(@PathParam("userid") String useridStr, @QueryParam("limit") String limitStr, @QueryParam("offset") String offsetStr) throws FusionRestException {
      int offset = StringUtil.toIntOrDefault(offsetStr, 0);
      int limit = StringUtil.toIntOrDefault(limitStr, 33);
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to retrieve user alerts due to invalid userid '%s'", useridStr));
         throw new FusionRestException(101, "Internal error while retrieving user alerts");
      } else {
         try {
            UserNotificationServicePrx unsPrx = EJBIcePrxFinder.getUserNotificationServiceProxy();
            if (unsPrx != null) {
               try {
                  Map<Integer, Integer> unreadCountMap = unsPrx.getUnreadNotificationCountForUser(userid);
                  Map<Integer, Map<String, Map<String, String>>> data = unsPrx.getPendingNotificationDataForUser(userid);
                  int unreadCount = 0;
                  UserAlerts alerts = new UserAlerts();
                  Iterator i$ = data.keySet().iterator();

                  while(i$.hasNext()) {
                     Integer key = (Integer)i$.next();
                     if (unreadCountMap.containsKey(key)) {
                        unreadCount += (Integer)unreadCountMap.get(key);
                     }
                  }

                  alerts.alerts = data;
                  alerts.unread = unreadCount;
                  return new DataHolder(alerts.retrievePage(offset, limit));
               } catch (FusionException var14) {
                  log.error(String.format("Failed to retrieve user alerts: FusionException '%s'", var14.message));
                  throw new FusionRestException(102, "Internal error while retrieving user alerts");
               }
            } else {
               log.error(String.format("Failed to retrieve user alerts: unable to find UserNotificationService"));
               throw new FusionRestException(101, "Internal error while retrieving user alerts");
            }
         } catch (EJBException var15) {
            log.error(String.format("Failed to retrieve user alerts: EJBException '%s'", var15.getMessage()));
            throw new FusionRestException(101, "Internal error while retrieving user alerts");
         }
      }
   }

   @DELETE
   @Path("/{userid}/alert/{alerttype}/{alertkey}")
   @Produces({"application/json"})
   public Response deleteAlert(@PathParam("userid") String useridStr, @PathParam("alerttype") String alertTypeStr, @PathParam("alertkey") String alertKey) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to delete user alert due to invalid userid '%s'", useridStr));
         throw new FusionRestException(101, "Internal error while deleting user alert");
      } else {
         int alertType = StringUtil.toIntOrDefault(alertTypeStr, -1);
         if (alertType == -1) {
            log.error(String.format("Failed to delete user alert due to invalid alert type '%s'", alertTypeStr));
            throw new FusionRestException(101, "Internal error while deleting user alert");
         } else if (Enums.NotificationTypeEnum.fromType(alertType) == null) {
            log.error(String.format("Failed to delete user alert due to unknown alert type '%s'", alertTypeStr));
            throw new FusionRestException(101, "Internal error while deleting user alert");
         } else {
            try {
               UserNotificationServicePrx unsPrx = EJBIcePrxFinder.getUserNotificationServiceProxy();
               if (unsPrx != null) {
                  try {
                     if (alertKey.equals("@all")) {
                        unsPrx.clearAllNotificationsByTypeForUser(userid, alertType);
                     } else {
                        unsPrx.clearNotificationsForUser(userid, alertType, new String[]{alertKey});
                     }

                     return Response.ok().entity(new DataHolder("ok")).build();
                  } catch (FusionException var8) {
                     log.error(String.format("Failed to retrieve user alerts: FusionException '%s'", var8.message));
                     throw new FusionRestException(102, "Internal error while retrieving user alerts");
                  }
               } else {
                  log.error(String.format("Failed to retrieve user alerts: unable to find UserNotificationService"));
                  throw new FusionRestException(101, "Internal error while retrieving user alerts");
               }
            } catch (EJBException var9) {
               log.error(String.format("Failed to retrieve user alerts: EJBException '%s'", var9.getMessage()));
               throw new FusionRestException(101, "Internal error while retrieving user alerts");
            }
         }
      }
   }

   @DELETE
   @Path("/{userid}/alert/unread/count")
   @Produces({"application/json"})
   public Response deleteAlertCount(@PathParam("userid") String useridStr, @QueryParam("resetAll") String resetAllStr) throws FusionRestException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Alert.ENABLED_MANDATORY_RESET_UNREAD_COUNT)) {
         throw new FusionRestException(101, "Feature disabled");
      } else {
         int userid = StringUtil.toIntOrDefault(useridStr, -1);
         if (userid == -1) {
            log.error(String.format("Failed to reset user unread alert count due to invalid userid '%s'", useridStr));
            throw new FusionRestException(101, "Internal error while deleting user alert");
         } else {
            String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Alert.MANDATORY_RESET_UNREAD_COUNT_RATE_LIMIT);
            if (!StringUtil.isBlank(rateLimit)) {
               try {
                  MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.MANDATORY_RESET_UNREAD_ALERT_COUNT.toString(), useridStr, rateLimit);
               } catch (MemCachedRateLimiter.LimitExceeded var8) {
                  throw new FusionRestException(FusionRestException.RestException.RATE_LIMIT);
               } catch (MemCachedRateLimiter.FormatError var9) {
                  log.error("Formatting error in rate limiter expression", var9);
                  throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
               }
            }

            try {
               boolean resetAll = StringUtil.toBooleanOrDefault(resetAllStr, false);
               UserNotificationServicePrx unsPrx = EJBIcePrxFinder.getUserNotificationServiceProxy();
               if (unsPrx != null) {
                  unsPrx.clearAllUnreadNotificationCountForUser(userid, resetAll);
                  return Response.ok().entity(new DataHolder("ok")).build();
               } else {
                  log.error(String.format("Failed to reset user[%s] unread alert counts: unable to find UserNotificationService", useridStr));
                  throw new FusionRestException(101, "Internal error while retrieving user alerts");
               }
            } catch (Exception var7) {
               log.error(String.format("Failed to reset user[%s] unread alert counts: EJBException '%s'", useridStr, var7.getMessage()), var7);
               throw new FusionRestException(101, "Internal error while retrieving user alerts");
            }
         }
      }
   }

   @GET
   @Path("/{userid}/emoticons")
   @Produces({"application/json"})
   public DataHolder<UserEmoticonList> getEmoticons(@PathParam("userid") String userIdStr) throws FusionRestException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         List<EmoticonData> emoticonPack = contentBean.getEmoticonPack(1);
         UserEmoticonList data = new UserEmoticonList(emoticonPack);
         return new DataHolder(data);
      } catch (CreateException var5) {
         log.error("Unable to create content bean: " + var5.getMessage());
      } catch (EJBException var6) {
         log.error("Unable to retrieve emoticon pack: " + var6.getMessage());
      } catch (FusionEJBException var7) {
         log.error("Unable to retrieve emoticon pack: " + var7.getMessage());
      }

      throw new FusionRestException(101, "Internal error while retrieving emoticons");
   }

   @GET
   @Path("/{userid}/rss")
   @Produces({"application/json"})
   public DataHolder<RSSFeedForUserList> getRSSFeedForUser(@PathParam("userid") String userIdStr) throws FusionRestException {
      try {
         int userid = StringUtil.toIntOrDefault(userIdStr, -1);
         if (userid == -1) {
            throw new FusionRestException(101, String.format("Invalid userid [%s]", userIdStr));
         } else {
            RSSFeedForUserList rssForUserList = new RSSFeedForUserList();
            MigboApiUtil api = MigboApiUtil.getInstance();
            log.info(String.format("Retrieving list of RSSFeedForUsers from Migbo Dataservice for userid [%d]", userid));
            JSONObject obj = api.get(String.format("/user/%d/rss", userid));
            JSONObject data = obj.getJSONObject("data");
            JSONArray rssList = data.getJSONArray("rss");

            for(int i = 0; i < rssList.length(); ++i) {
               JSONObject rssFeedForUser = rssList.getJSONObject(i);
               String feedURL = rssFeedForUser.getString("url");
               RSSFeedForUser r = new RSSFeedForUser(userid, feedURL);
               rssForUserList.addRSSFeedForUser(r);
            }

            log.debug(String.format("Received JSON Response fro migbo-datsvc : %s ", obj.toString()));
            return new DataHolder(rssForUserList);
         }
      } catch (Exception var12) {
         log.error(String.format("Exception caught while retrieving RSS Feeds for user [%s]", userIdStr));
         throw new FusionRestException(101, "Internal error while retrieving RSS Feeds for user");
      }
   }

   @PUT
   @Path("/{userid}/rss")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<Map<String, String>> addRSSFeedForUser(@PathParam("userid") String userIdStr, String jsonData) throws FusionRestException {
      try {
         int userid = StringUtil.toIntOrDefault(userIdStr, -1);
         if (userid == -1) {
            throw new FusionRestException(101, String.format("Invalid userid [%s]", userIdStr));
         } else {
            MigboApiUtil api = MigboApiUtil.getInstance();
            JSONObject obj = api.put(String.format("/user/%d/rss", userid), jsonData);
            JSONObject data = obj.getJSONObject("data");
            log.debug(String.format("Received JSON Response from migbo-datsvc : %s ", obj.toString()));
            String feedID = data.getString("id");
            Map<String, String> map = new HashMap();
            map.put("id", feedID);
            return new DataHolder(map);
         }
      } catch (Exception var9) {
         log.error(String.format("Exception caught while creating RSS Feed for user [%s] %s", userIdStr, jsonData));
         throw new FusionRestException(101, "Internal error while registering RSS Feeds for user");
      }
   }

   @DELETE
   @Path("/{userid}/rss/{rssid}")
   @Produces({"application/json"})
   public Response removeRSSFeedForUser(@PathParam("userid") String userIdStr, @PathParam("rssid") String rssIdStr) throws FusionRestException {
      try {
         int userid = StringUtil.toIntOrDefault(userIdStr, -1);
         if (userid == -1) {
            throw new FusionRestException(101, String.format("Invalid userid [%s]", userIdStr));
         } else {
            MigboApiUtil api = MigboApiUtil.getInstance();
            JSONObject obj = api.delete(String.format("/user/%d/rss/%s", userid, rssIdStr));
            log.debug(String.format("Received JSON Response from migbo-datsvc : %s ", obj.toString()));
            return Response.ok().entity(new DataHolder("ok")).build();
         }
      } catch (Exception var6) {
         log.error(String.format("Exception caught while deleting RSS Feed for user [%s] feed [%s]", userIdStr, rssIdStr));
         throw new FusionRestException(101, "Internal error while removing RSS Feeds for user");
      }
   }

   @GET
   @Path("/{userid}/categories")
   @Produces({"application/json"})
   public DataHolder<Map<Integer, String[]>> getUserCategories(@PathParam("userid") String userIdStr) throws FusionRestException {
      try {
         int userid = StringUtil.toIntOrDefault(userIdStr, -1);
         if (userid == -1) {
            throw new FusionRestException(101, String.format("Invalid userid [%s]", userIdStr));
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            Map<Integer, String[]> categories = userEJB.getUserCategoryNames(userid);
            return new DataHolder(categories);
         }
      } catch (Exception var5) {
         log.error("Exception caught while retrieving user categories: " + var5.getMessage());
         throw new FusionRestException(101, "Internal error while retrieving user categories.");
      }
   }

   @GET
   @Path("/category/{categoryid}")
   @Produces({"application/json"})
   public DataHolder<String[]> getUsernamesInCategory(@PathParam("categoryid") String strCategoryId) throws FusionRestException {
      try {
         int categoryId = StringUtil.toIntOrDefault(strCategoryId, -1);
         if (categoryId <= 0) {
            throw new FusionRestException(101, "Invalid category id: [" + categoryId + "]");
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String[] usernames = userEJB.getUsersInUserCategory(categoryId);
            return new DataHolder(usernames);
         }
      } catch (Exception var5) {
         log.error("Exception caught while retrieving user categories: " + var5.getMessage());
         throw new FusionRestException(101, "Internal error while retrieving users in category.");
      }
   }

   @POST
   @Path("/{userid}/blacklist")
   @Produces({"application/json"})
   public DataHolder<BooleanData> blacklistUser(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);

      BooleanData data;
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         data = new BooleanData(userEJB.blacklistUser(userId));
         return new DataHolder(data);
      } catch (Exception var5) {
         log.error("Exception caught while blacklisting user: " + var5.getMessage());
         data = new BooleanData(false);
         return new DataHolder(data);
      }
   }

   @DELETE
   @Path("/{userid}/blacklist")
   @Produces({"application/json"})
   public DataHolder<BooleanData> removeUserFromBlacklist(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);

      BooleanData data;
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         data = new BooleanData(userEJB.removeUserFromBlacklist(userId));
         return new DataHolder(data);
      } catch (Exception var5) {
         log.error("Exception caught while removing user from blacklist: " + var5.getMessage());
         data = new BooleanData(false);
         return new DataHolder(data);
      }
   }

   @POST
   @Path("/{userid}/ban")
   @Produces({"application/json"})
   public DataHolder<BooleanData> banUser(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);

      BooleanData data;
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         data = new BooleanData(userEJB.banUser(userId));
         return new DataHolder(data);
      } catch (Exception var5) {
         log.error("Exception caught while banning user: " + var5.getMessage());
         data = new BooleanData(false);
         return new DataHolder(data);
      }
   }

   @POST
   @Path("/{userid}/suspend")
   @Produces({"application/json"})
   public DataHolder<BooleanData> suspendUser(@PathParam("userid") String userIdStr, @QueryParam("duration") String durationStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);
      int duration = StringUtil.toIntOrDefault(durationStr, -1);
      if (duration < 0) {
         throw new FusionRestException(101, String.format("Invalid duration parameter [%s]", durationStr));
      } else {
         BooleanData data;
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            data = new BooleanData(userEJB.suspendUser(userId, duration));
            return new DataHolder(data);
         } catch (Exception var7) {
            log.error("Exception caught while suspending user: " + var7.getMessage());
            data = new BooleanData(false);
            return new DataHolder(data);
         }
      }
   }

   private UserData getUserData(UserLocal userEJB, String useridOrUsername, boolean useUsername) throws FusionRestException {
      int userid = -1;
      String username = null;
      if (!useUsername) {
         userid = StringUtil.toIntOrDefault(useridOrUsername, -1);
         if (userid == -1) {
            log.warn(String.format("useUsername is false '%s', but userid '%s' is invalid, treating it as username anyway", useUsername, useridOrUsername));
         }
      }

      UserData userData = null;
      if (userid == -1) {
         userData = userEJB.loadUserByUsernameOrAlias(useridOrUsername, false, false);
      } else {
         userData = userEJB.loadUserFromID(userid);
      }

      if (userData == null) {
         throw new FusionRestException(102, String.format("Unable to find user '%s'", useridOrUsername));
      } else {
         if (userid != -1) {
            if (!useridOrUsername.equalsIgnoreCase(userData.username)) {
               log.info(String.format("user data loaded by alias '%s' for userid '%d'", useridOrUsername, userData.userID));
            } else {
               log.info(String.format("user data loaded by username '%s' for userid '%d'", useridOrUsername, userData.userID));
            }
         } else {
            userid = userData.userID;
         }

         return userData;
      }
   }

   @GET
   @Path("/{userid}/registrationcontext/campaign")
   @Produces({"application/json"})
   public DataHolder<String> getRegistrationContextCampaign(@PathParam("userid") String useridOrUsername, @QueryParam("useUsername") String useUsernameStr) throws FusionRestException {
      boolean useUsername = StringUtil.toBooleanOrDefault(useUsernameStr, false);

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = this.getUserData(userEJB, useridOrUsername, useUsername);
         if (userData == null) {
            throw new FusionRestException(102, String.format("Unable to find user '%s'", useridOrUsername));
         } else {
            RegistrationContextData regContextData = userEJB.getRegistrationContextData(userData.userID);
            return new DataHolder(regContextData.campaign);
         }
      } catch (CreateException var7) {
         log.error(String.format("Failed to retrieve registration context campaign for user '%s' due to EJB CreateException: %s", useridOrUsername, var7.getMessage()), var7);
         throw new FusionRestException(101, "Internal error while retrieving user registration context data");
      } catch (Exception var8) {
         log.error(String.format("Unexpected error during registration context campaign for user %s", useridOrUsername), var8);
         throw new FusionRestException(101, "Internal error while retrieving user registration context data");
      }
   }

   @POST
   @Path("/{userid}/statusmessage")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<BooleanData> updateStatusMessage(@PathParam("userid") String userIdStr, @QueryParam("view") String ssoViewStr, DataHolder<StringData> dataholder) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);
      if (dataholder != null && dataholder.data != null && ((StringData)dataholder.data).value != null) {
         String statusMessage = ((StringData)dataholder.data).value;
         log.info(String.format("updating status message of userid %s, msg '%s', view %s", userIdStr, statusMessage, ssoViewStr));

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String username = userBean.getUsernameByUserid(userId, (Connection)null);
            userBean.updateStatusMessage(userId, username, statusMessage, (ClientType)null, SSOEnums.View.fromString(ssoViewStr));
         } catch (CreateException var8) {
            throw new FusionRestException(101, var8.getMessage());
         } catch (EJBException var9) {
            throw new FusionRestException(101, var9.getMessage());
         } catch (Exception var10) {
            throw new FusionRestException(101, var10.getMessage());
         }

         return new DataHolder(new BooleanData(true));
      } else {
         throw new FusionRestException(101, "Invalid status message");
      }
   }

   @POST
   @Path("")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<BooleanData> createUser(DataHolder<UserCreationData> dataholder) throws FusionRestException {
      UserCreationData userCreationData = (UserCreationData)dataholder.data;
      if (log.isDebugEnabled()) {
         log.debug("createUser:UserCreationData:username=[" + userCreationData.username + "],invitationToken=[" + userCreationData.invitationToken + "]" + ",campaign=[" + userCreationData.campaign + "]" + ",userAgent=[" + userCreationData.userAgent + "]" + ",emailAddress=[" + userCreationData.emailAddress + "]" + ",fbid=[" + userCreationData.fbid + "]" + ",accessToken=[" + userCreationData.accessToken + "]" + ",countryISOCode=[" + userCreationData.countryISOCode + "]" + ",registrationIPAddress=[" + userCreationData.registrationIPAddress + "]" + ",registrationDevice=[" + userCreationData.registrationDevice + "]" + ",registrationToken=[" + userCreationData.registrationToken + "]" + ",registrationType=[" + userCreationData.registrationType + "]");
      }

      Integer invitationID = null;
      if (InvitationUtils.isInvitationEngineEnabled((InvitationData.ChannelType)null)) {
         String invitationToken = userCreationData.invitationToken;
         if (!StringUtil.isBlank(invitationToken)) {
            invitationID = InvitationUtils.decryptReferralInvitation(invitationToken);
            if (invitationID < 0) {
               throw new FusionRestException(FusionRestException.RestException.INVALID_REFERRAL_TOKEN);
            }
         }
      }

      userCreationData.initializeToDefaultValues();
      UserData userData = userCreationData.getUserData();
      UserProfileData userProfileData = new UserProfileData();
      String registrationType = userCreationData.registrationType;
      String campaign = userCreationData.campaign;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         RegistrationType registrationTypeEnum = RegistrationType.fromValue(registrationType);
         if (registrationTypeEnum == null) {
            throw new FusionRestException(101, "Registration failed:Please provide a registrationType(email1/email2/mobile)");
         }

         UserRegistrationContextData userRegContextData = new UserRegistrationContextData(campaign, true, registrationTypeEnum, invitationID);
         if (registrationTypeEnum == RegistrationType.EMAIL_REGISTRATION_PATH1 || registrationTypeEnum == RegistrationType.EMAIL_REGISTRATION_PATH2) {
            UserVerificationData userVerificationData = userBean.getVerificationDataFromToken(userCreationData.registrationToken);
            if (!StringUtil.isBlank(userCreationData.username) && !userCreationData.username.equals(userVerificationData.username)) {
               log.error(String.format("registration username passed in '%s' is different from that in userregistration table '%s'", userCreationData.username, userVerificationData.username));
               throw new Exception("Token is not valid");
            }

            if (!StringUtil.isBlank(userCreationData.emailAddress) && !userCreationData.emailAddress.equals(userVerificationData.emailAddress)) {
               log.error(String.format("registration email passed in '%s' is different from that in userregistration table '%s'", userCreationData.emailAddress, userVerificationData.emailAddress));
               throw new Exception("Token is not valid");
            }

            if (RegistrationType.fromValue(userVerificationData.registrationType) != registrationTypeEnum) {
               log.error(String.format("registration type passed in '%s' is different from that in userregistration table '%s'", registrationType, userVerificationData.registrationType));
               throw new Exception("Token is not valid");
            }

            if (registrationTypeEnum == RegistrationType.EMAIL_REGISTRATION_PATH1 && !userBean.validateRegistrationToken(userVerificationData)) {
               throw new Exception("Token is not valid");
            }

            if (registrationTypeEnum == RegistrationType.EMAIL_REGISTRATION_PATH2 && !userVerificationData.isVerified) {
               throw new FusionRestException(101, "Registration failed:Invalid or unverified token");
            }

            UserDataAndRegistrationContextData data = userBean.getUserDataFromUserRegistrationTable(userVerificationData.username, userData);
            if (registrationTypeEnum == RegistrationType.EMAIL_REGISTRATION_PATH1) {
               userData = data.userData;
            }

            userRegContextData = data.regContextData.updateUserRegistrationContextData(userRegContextData);
         }

         AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(userData.registrationIPAddress, (String)null, userData.registrationDevice, userData.userAgent);
         userBean.createUser(userData, userProfileData, true, userRegContextData, accountEntrySourceData);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.FacebookConnect.ENABLED)) {
            if (registrationTypeEnum == RegistrationType.FACEBOOK_CONNECT) {
               int userId = userBean.getUserID(userCreationData.username, (Connection)null);
               if (userId != -1) {
                  this.updateFacebookDetails(userId, userCreationData.fbid, userCreationData.accessToken);
               }
            } else {
               log.error("Unable to find User ID for Username: " + userCreationData.username);
            }
         }
      } catch (CreateException var13) {
         throw new FusionRestException(101, var13.getMessage());
      } catch (EJBException var14) {
         throw new FusionRestException(101, var14.getMessage());
      } catch (Exception var15) {
         throw new FusionRestException(101, var15.getMessage());
      }

      return new DataHolder(new BooleanData(true));
   }

   private void updateFacebookDetails(int userId, String facebookId, String accessToken) throws FusionRestException, JSONException {
      FacebookCredentialData credentialData = new FacebookCredentialData(facebookId, accessToken);
      if (ThirdPartySiteCredentialManager.updateCredential(userId, PasswordType.FACEBOOK_IM.value(), credentialData.toJSONString())) {
         EventQueue.enqueueSingleEvent(new ThirdPartySiteCredentialUpdatedEvent(userId));
      }

   }

   @POST
   @Path("/{userid}/contact/{contactUserid}")
   @Produces({"application/json"})
   public DataHolder<ContactData> addFusionContact(@PathParam("userid") String useridStr, @PathParam("contactUserid") String contactUseridStr, @QueryParam("followOnMiniblog") String followOnMiniblogStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid < 1) {
         throw new FusionRestException(101, String.format("Invalid userid provided: %s", useridStr));
      } else {
         int contactUserid = StringUtil.toIntOrDefault(contactUseridStr, -1);
         if (contactUserid < 1) {
            throw new FusionRestException(101, String.format("Invalid contactUserid provided: %s", contactUseridStr));
         } else {
            boolean followOnMiniblog = StringUtil.toBooleanOrDefault(followOnMiniblogStr, false);
            ContactData contactData = new ContactData();
            ContactData returnContactData = null;

            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
               String username = userEJB.getUsernameByUserid(userid, (Connection)null);
               if (StringUtil.isBlank(username)) {
                  throw new FusionRestException(101, String.format("Invalid userid provided: %s - user not found", useridStr));
               }

               String contactUsername = userEJB.getUsernameByUserid(contactUserid, (Connection)null);
               if (StringUtil.isBlank(username)) {
                  throw new FusionRestException(101, String.format("Invalid contactUserid provided: %s - user not found", contactUseridStr));
               }

               contactData.username = username;
               contactData.fusionUsername = contactUsername;
               contactData.displayOnPhone = true;
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.FOLLOW_ON_ADD_CONTACT_ENABLED)) {
                  returnContactData = contactEJB.addFusionUserAsContact(userid, contactData, followOnMiniblog);
               } else {
                  returnContactData = contactEJB.addPendingFusionContact(userid, contactData);
               }

               if (returnContactData != null && returnContactData.id != null) {
                  return new DataHolder(returnContactData);
               }
            } catch (Exception var13) {
               log.error(String.format("Exception caught while adding %s to contact list of %s - followOnMiniblog %s", contactUseridStr, useridStr, followOnMiniblog ? "true" : "false"), var13);
            }

            throw new FusionRestException(101, "Internal system error - unable to fulfill add contact request");
         }
      }
   }

   @DELETE
   @Path("/{userid}/contact/{contactUserid}")
   @Produces({"application/json"})
   public Response removeFusionContact(@PathParam("userid") String useridStr, @PathParam("contactUserid") String contactUseridStr, @QueryParam("unfollowOnMiniblog") String unfollowOnMiniblogStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid < 1) {
         throw new FusionRestException(101, String.format("Invalid userid provided: %s", useridStr));
      } else {
         int contactUserid = StringUtil.toIntOrDefault(contactUseridStr, -1);
         if (contactUserid < 1) {
            throw new FusionRestException(101, String.format("Invalid contactUserid provided: %s", contactUseridStr));
         } else {
            boolean unfollowOnMiniblog = StringUtil.toBooleanOrDefault(unfollowOnMiniblogStr, false);

            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
               String username = userEJB.getUsernameByUserid(userid, (Connection)null);
               if (StringUtil.isBlank(username)) {
                  throw new FusionRestException(101, String.format("Invalid userid provided: %s - user not found", useridStr));
               } else {
                  String contactUsername = userEJB.getUsernameByUserid(contactUserid, (Connection)null);
                  if (StringUtil.isBlank(username)) {
                     throw new FusionRestException(101, String.format("Invalid contactUserid provided: %s - user not found", contactUseridStr));
                  } else {
                     ContactData contactData = contactEJB.getContact(username, contactUsername);
                     if (contactData != null && contactData.id != null) {
                        if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.UNFOLLOW_ON_REMOVE_CONTACT_ENABLED)) {
                           contactEJB.removeFusionUserFromContact(userid, username, contactData.id, unfollowOnMiniblog);
                        } else {
                           contactEJB.removeContact(userid, username, contactData.id);
                        }

                        return Response.ok().entity(new DataHolder("ok")).build();
                     } else {
                        throw new FusionRestException(101, String.format("%s does not have %s on contact list", username, contactUsername));
                     }
                  }
               }
            } catch (Exception var12) {
               log.error(String.format("Exception caught while remove %s to contact list of %s - unfollowOnMiniblog %s", contactUseridStr, useridStr, unfollowOnMiniblog ? "true" : "false"), var12);
               throw new FusionRestException(101, "Internal system error - unable to fulfill remove contact request");
            }
         }
      }
   }

   private int getAndCheckUserid(String userIdStr) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, -1);
      if (userId == -1) {
         throw new FusionRestException(101, String.format("Invalid user ID [%s]", userIdStr));
      } else {
         return userId;
      }
   }

   @GET
   @Path("/{userid}/payments")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<List<PaymentData>> getPendingPayments(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String username = userBean.getUsernameByUserid(userId, (Connection)null);
         if (username == null) {
            throw new FusionRestException(FusionRestException.RestException.UNKNOWN_USER_ID, "Unknown user id [" + userIdStr + "]");
         } else {
            AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            List<PaymentData> paymentTransactions = accountEJB.getPaymentTransactions(username, (Integer)null, PaymentData.StatusEnum.PENDING.getEnumValue(), (Integer)null);
            return new DataHolder(paymentTransactions);
         }
      } catch (CreateException var7) {
         log.error("Error in retrieving user transactions", var7);
         throw new FusionRestException(101, "Unable to retrieve pending payments.");
      } catch (EJBException var8) {
         log.error("Error in retrieving user transactions", var8);
         throw new FusionRestException(101, var8.getMessage());
      }
   }

   @POST
   @Path("/{userid}/balance/unfunded")
   @Produces({"application/json"})
   public DataHolder<BooleanData> updateUnfundedBalance(@PathParam("userid") String useridStr, DataHolder<UpdateUnfundedBalanceData> dataHolder) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid < 1) {
         throw new FusionRestException(101, String.format("Invalid userid provided: %s", useridStr));
      } else {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String username = userEJB.getUsernameByUserid(userid, (Connection)null);
            if (StringUtil.isBlank(username)) {
               throw new FusionRestException(101, String.format("Invalid userid provided: %s - user not found", useridStr));
            }

            AccountEntryData accountEntry = new AccountEntryData();
            accountEntry.username = username;
            AccountEntryData.TypeEnum type = AccountEntryData.TypeEnum.fromValue(((UpdateUnfundedBalanceData)dataHolder.data).type);
            accountEntry.type = AccountEntryData.TypeEnum.fromValue(((UpdateUnfundedBalanceData)dataHolder.data).type);
            accountEntry.reference = ((UpdateUnfundedBalanceData)dataHolder.data).reference;
            accountEntry.description = ((UpdateUnfundedBalanceData)dataHolder.data).description;
            accountEntry.currency = ((UpdateUnfundedBalanceData)dataHolder.data).currency;
            accountEntry.amount = ((UpdateUnfundedBalanceData)dataHolder.data).amount;
            accountEntry.fundedAmount = 0.0D;
            accountEntry.tax = 0.0D;
            accountEntry.wholesaleCost = 0.0D;
            AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(((UpdateUnfundedBalanceData)dataHolder.data).ipAddress, (String)null, (String)null, ((UpdateUnfundedBalanceData)dataHolder.data).userAgent);
            if (type == AccountEntryData.TypeEnum.BONUS_CREDIT) {
               accountEJB.giveUnfundedCredits(username, ((UpdateUnfundedBalanceData)dataHolder.data).reference, ((UpdateUnfundedBalanceData)dataHolder.data).description, ((UpdateUnfundedBalanceData)dataHolder.data).amount, ((UpdateUnfundedBalanceData)dataHolder.data).currency, accountEntrySourceData);
            } else {
               if (type != AccountEntryData.TypeEnum.DEDUCT_UNFUNDED_BALANCE) {
                  throw new FusionRestException(FusionRestException.RestException.INVALID_ACCOUNTENTRY_TYPE);
               }

               accountEJB.deductUnfundedCredits(username, ((UpdateUnfundedBalanceData)dataHolder.data).reference, ((UpdateUnfundedBalanceData)dataHolder.data).description, ((UpdateUnfundedBalanceData)dataHolder.data).amount, ((UpdateUnfundedBalanceData)dataHolder.data).currency, accountEntrySourceData);
            }
         } catch (CreateException var10) {
            log.error("CreateException on updateUnfundedBalance while creating UserBean/AccountBean", var10);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (EJBException var11) {
            log.error("CreateException on updateUnfundedBalance", var11);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (Exception var12) {
            log.error("Unhandled exception on updateUnfundedBalance", var12);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         }

         return new DataHolder(new BooleanData(true));
      }
   }

   @GET
   @Path("/{userid}/invitation_response/{response}/{invitationToken}")
   @Produces({"application/json"})
   public DataHolder<InvitationDetailsData> logInvitationResponse(@PathParam("userid") String userIdStr, @PathParam("response") String responseStr, @PathParam("invitationToken") String invitationTokenCode, @QueryParam("fetchExtraParameters") String fetchExtraParametersStr) throws FusionRestException {
      if (!InvitationUtils.isInvitationEngineEnabled((InvitationData.ChannelType)null)) {
         throw new FusionRestException(FusionRestException.RestException.INVITATION_DISABLED);
      } else {
         int userId = this.getAndCheckUserid(userIdStr);

         try {
            InvitationResponseData.ResponseType invitationResponseType = InvitationResponseData.ResponseType.fromTypeCode(Integer.valueOf(responseStr));
            if (invitationResponseType != null && invitationResponseType != InvitationResponseData.ResponseType.SIGN_UP_UNVERIFIED && invitationResponseType != InvitationResponseData.ResponseType.SIGN_UP_VERIFIED) {
               boolean fetchExtraParameters = StringUtil.toBooleanOrDefault(fetchExtraParametersStr, false);
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               String username = userBean.getUsernameByUserid(userId, (Connection)null);
               int invitationId = InvitationUtils.decryptReferralInvitation(invitationTokenCode);
               if (invitationId == -1) {
                  throw new FusionRestException(FusionRestException.RestException.INVALID_REFERRAL_TOKEN);
               } else {
                  Timestamp actionTime = new Timestamp(System.currentTimeMillis());
                  InvitationData invitationData = null;

                  try {
                     invitationData = userBean.getInvitationData(invitationId, fetchExtraParameters, (Connection)null);
                     InvitationUtils.getInvitationStatus(invitationData, actionTime);
                     if (invitationData == null) {
                        log.error(String.format("Failed to retrieve invitationData for invitationID:%s", invitationId));
                        throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                     }

                     if (invitationData.status == InvitationData.StatusFieldValue.NO_RESPONSE) {
                        userBean.logInvitationResponse((Connection)null, actionTime, invitationData, invitationResponseType, username, InvitationData.StatusFieldValue.CLOSED);
                     } else if (invitationData.status == InvitationData.StatusFieldValue.EXPIRED) {
                        log.info(String.format("Inivtation is already EXPIRED, invitationId:%s, expireTime:%s, responseTime:%s", invitationId, invitationData.expireTime, actionTime));
                     } else {
                        log.info(String.format("Invitation is already CLOSED, Can only reponse to a invitation once, invitationId:%s, invitationStatus:%s", invitationId, invitationData.status));
                     }
                  } catch (EJBException var14) {
                     log.error(String.format("Failed to response to invitation:%s, userid:%s, resonseType:%s, root cause:%s", invitationId, userId, invitationResponseType, var14.getMessage()));
                  }

                  return new DataHolder(userBean.convertInvitationDataToInvitationDetailsData(invitationData, fetchExtraParameters, actionTime));
               }
            } else {
               throw new FusionRestException(FusionRestException.RestException.INVALID_INVITATION_RESPONSE);
            }
         } catch (CreateException var15) {
            log.error("CreateException on getInvitationTokenData", var15);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (EJBException var16) {
            log.error("CreateException on getInvitationTokenData", var16);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (Exception var17) {
            log.error("Unhandled exception on getInvitationTokenData", var17);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         }
      }
   }

   @POST
   @Path("/{userid}/sent_invitation")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<Map<String, Integer>> sendInvitation(@PathParam("userid") String userIdStr, DataHolder<SendingInvitationData> dataHolder) throws FusionRestException {
      try {
         int userId = this.getAndCheckUserid(userIdStr);
         if (userId < 1) {
            throw new FusionRestException(FusionRestException.RestException.UNKNOWN_USER_ID, String.format("Invalid userid provided: %s", userIdStr));
         } else {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUserFromID(userId);
            if (userData == null) {
               throw new FusionRestException(FusionRestException.RestException.UNKNOWN_USER_ID, String.format("Invalid userid provided: %s", userIdStr));
            } else if (!InvitationUtils.isUserMobileOrEmailVerified(userData)) {
               throw new FusionRestException(FusionRestException.RestException.UNAUTHENTICATED_MOBILE_USER, String.format("You need to verify your account at %s before you can proceed.", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL)));
            } else {
               String username = null;
               username = userData.username;
               SendingInvitationData sendingInvitationData = (SendingInvitationData)dataHolder.data;
               InvitationData.ChannelType channelType = InvitationData.ChannelType.fromTypeCode(sendingInvitationData.channel);
               if (channelType == null) {
                  throw new FusionRestException(FusionRestException.RestException.INVALID_REFERRAL_CHANNEL);
               } else if (sendingInvitationData.destinations != null && sendingInvitationData.destinations.size() != 0) {
                  InvitationData.ActivityType activityType = InvitationData.ActivityType.fromTypeCode(sendingInvitationData.type);
                  if (activityType == null) {
                     throw new FusionRestException(FusionRestException.RestException.INVALID_REFERRAL_ACTIVITY_TYPE);
                  } else if (channelType == InvitationData.ChannelType.EMAIL && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.INVITATION_EMAIL_REFERRAL))) && !userBean.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.INVITATION_EMAIL_REFERRAL.value())) {
                     throw new FusionRestException(FusionRestException.RestException.INVITATION_DENIED);
                  } else if (channelType == InvitationData.ChannelType.FB && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.FB_INVITATION_REFERRAL))) && !userBean.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.FB_INVITATION_REFERRAL.value())) {
                     throw new FusionRestException(FusionRestException.RestException.INVITATION_DENIED);
                  } else if (channelType == InvitationData.ChannelType.INTERNAL && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.INTERNAL_INVITATION_REFERRAL))) && !userBean.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.INTERNAL_INVITATION_REFERRAL.value())) {
                     throw new FusionRestException(FusionRestException.RestException.INVITATION_DENIED);
                  } else {
                     InvitationUtils.deduplicateDestinations(sendingInvitationData);
                     int needToExecuteCount = 0;

                     try {
                        String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.REFERRAL_RATE_LIMIT);
                        if (channelType == InvitationData.ChannelType.FB) {
                           rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.FACEBOOK_REFERRAL_RATE_LIMIT);
                        } else if (channelType == InvitationData.ChannelType.INTERNAL) {
                           rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.INTERNAL_REFERRAL_RATE_LIMIT);
                        }

                        for(int i = 0; i < sendingInvitationData.destinations.size(); ++i) {
                           MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.USER_REFERRAL.toString(), MemCachedKeyUtils.getFullKeyFromStrings(activityType.toString(), userIdStr), rateLimit);
                           ++needToExecuteCount;
                        }
                     } catch (MemCachedRateLimiter.LimitExceeded var13) {
                        log.warn(String.format("user:%s has reached the number of referrals he can send today", userId));
                        if (needToExecuteCount == 0) {
                           throw new FusionRestException(FusionRestException.RestException.REACH_REFERRAL_RATE_LIMIT);
                        }
                     } catch (MemCachedRateLimiter.FormatError var14) {
                        log.error("Formatting error in rate limiter expression", var14);
                        throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                     }

                     sendingInvitationData.destinations = sendingInvitationData.destinations.subList(0, needToExecuteCount);
                     log.info(String.format("Creating invitation for User:%s at Channel:%s - Type:%s", userIdStr, channelType, activityType));
                     Map<String, Integer> result = userBean.createInvitation(userId, (SendingInvitationData)dataHolder.data, (Connection)null).getSendInvitationStatusSummary();
                     return new DataHolder(result);
                  }
               } else {
                  throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "destinations are not empty or not specified");
               }
            }
         }
      } catch (CreateException var15) {
         log.error("Error in retrieving user transactions", var15);
         throw new FusionRestException(101, "Unable to retrieve pending payments.");
      } catch (EJBException var16) {
         log.error("EJBException in sendInvitation: " + var16.getMessage());
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (FusionRestException var17) {
         throw var17;
      } catch (Exception var18) {
         log.error("Unhandled exception", var18);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      }
   }

   @GET
   @Path("/{userid}/stickers")
   @Produces({"application/json"})
   @Consumes({"application/json"})
   public DataHolder<List<EmoticonData>> getStickersForUser(@PathParam("userid") String userIdStr, @QueryParam("limit") String limitStr, @QueryParam("offset") String offsetStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);
      int offset = StringUtil.toIntOrDefault(offsetStr, 0);
      int limit = StringUtil.toIntOrDefault(limitStr, 33);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String username = userBean.getUsernameByUserid(userId, (Connection)null);
         if (username == null) {
            throw new FusionRestException(FusionRestException.RestException.UNKNOWN_USER_ID, "Unknown user id [" + userIdStr + "]");
         } else {
            ContentLocal contentEJB = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
            List<EmoticonData> emoticons = contentEJB.getStickerDataListForUser(username);
            if (offset > emoticons.size()) {
               return new DataHolder();
            } else {
               if (offset + limit > emoticons.size()) {
                  limit = emoticons.size() - offset;
               }

               return new DataHolder(emoticons.subList(offset, offset + limit));
            }
         }
      } catch (CreateException var11) {
         log.error("Error in retrieving user stickers", var11);
         throw new FusionRestException(101, "Unable to retrieve user stickers.");
      } catch (EJBException var12) {
         log.error("Error in retrieving user stickers", var12);
         throw new FusionRestException(101, var12.getMessage());
      }
   }

   @GET
   @Path("/{userid}/ip")
   @Produces({"application/json"})
   public DataHolder<Set<String>> getIpAddress(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String username = userBean.getUsernameByUserid(userId, (Connection)null);
         if (username == null) {
            log.error("Error in retrieving username to get IP for user ID: " + userIdStr);
            throw new FusionRestException(101, "Unable to retrieve user IP");
         } else {
            UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
            if (userPrx == null) {
               return new DataHolder(new HashSet(0));
            } else {
               SessionPrx[] sessions = userPrx.getSessions();
               Set<String> ipAddresses = new HashSet(sessions.length);

               for(int i = 0; i < sessions.length; ++i) {
                  ipAddresses.add(sessions[i].getRemoteIPAddress());
               }

               return new DataHolder(ipAddresses);
            }
         }
      } catch (Exception var9) {
         log.warn("Error in retrieving user IP", var9);
         throw new FusionRestException(101, "Unable to retrieve user IP");
      }
   }

   @POST
   @Path("/{userid}/disconnect")
   @Produces({"application/json"})
   public DataHolder<BooleanData> disconnectUser(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String username = userBean.getUsernameByUserid(userId, (Connection)null);
         if (username == null) {
            log.error("Error in retrieving username to disconnect user ID: " + userIdStr);
            throw new FusionRestException(101, "Unable to disconnect user");
         } else {
            if (log.isDebugEnabled()) {
               log.debug(String.format("disconnect user:%s", userId));
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CAPTCHA_REQUIRED_ENABLED)) {
               MemCachedClientWrapper.add(MemCachedKeySpaces.CommonKeySpace.CAPTCHA_REQUIRED, StringUtil.normalizeUsername(username), 1);
            }

            UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
            if (userPrx == null) {
               return new DataHolder(new BooleanData(true));
            } else {
               userPrx.disconnect(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.REASON_FOR_DISCONNECTING_SPAMMER));
               return new DataHolder(new BooleanData(true));
            }
         }
      } catch (Exception var6) {
         log.warn(String.format("Error in disconnecting user:%s", userId), var6);
         throw new FusionRestException(101, "Unable to disconnect user");
      }
   }

   @GET
   @Path("/{username}/store/received/")
   @Produces({"application/json"})
   public DataHolder<? extends Serializable> getStoreItemsReceived(@QueryParam("sessionId") String sessionId, @PathParam("username") String username, @QueryParam("type") int type, @QueryParam("offset") int offset, @QueryParam("limit") int limit) throws FusionRestException {
      ConnectionPrx prx = ResourceUtil.getConnectionProxy(sessionId);
      UserData userData = new UserData(prx.getUserObject().getUserData());

      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         switch(StoreItemData.TypeEnum.fromValue(type)) {
         case VIRTUAL_GIFT:
            return new DataHolder(contentBean.getVirtualGiftsReceived(userData.username, username, offset, limit));
         case AVATAR:
         case STICKER:
         case EMOTICON:
         case SUPER_EMOTICON:
         case THEME:
         default:
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Type not supported.");
         }
      } catch (FusionRestException var9) {
         throw var9;
      } catch (Exception var10) {
         log.error(String.format("Failed to retrieve store item received for: %s", userData.username), var10);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var10.getMessage());
      }
   }

   @GET
   @Path("/{username}/store/received/{itemid}")
   @Produces({"application/json"})
   public DataHolder<? extends Serializable> getStoreItemReceived(@QueryParam("sessionId") String sessionId, @PathParam("username") String username, @QueryParam("type") int type, @PathParam("itemid") int itemid) throws FusionRestException {
      ConnectionPrx prx = ResourceUtil.getConnectionProxy(sessionId);
      UserData userData = new UserData(prx.getUserObject().getUserData());

      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         switch(StoreItemData.TypeEnum.fromValue(type)) {
         case VIRTUAL_GIFT:
            return new DataHolder(contentBean.getVirtualGiftReceived(userData.username, username, itemid));
         case AVATAR:
         case STICKER:
         case EMOTICON:
         case SUPER_EMOTICON:
         case THEME:
         default:
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Type not supported.");
         }
      } catch (FusionRestException var8) {
         throw var8;
      } catch (Exception var9) {
         log.error(String.format("Failed to retrieve store item received id: %d", type), var9);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var9.getMessage());
      }
   }

   @GET
   @Path("/{username}/store/inventory/")
   @Produces({"application/json"})
   public DataHolder<List<StoreItemInventoryData>> getStoreItemsInventoryByType(@PathParam("username") String username, @QueryParam("type") int type) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int requestingId = userBean.getUserID(username, (Connection)null);
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         return new DataHolder(contentBean.getStoreItemsInventoryByType(requestingId, StoreItemData.TypeEnum.fromValue(type)));
      } catch (Exception var6) {
         log.error(String.format("Failed to retrieve store item received type: %d", type), var6);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var6.getMessage());
      }
   }

   @GET
   @Path("/{username}/store/inventory/{storeitemid}")
   @Produces({"application/json"})
   public DataHolder<StoreItemInventoryData> getStoreItemInventory(@PathParam("username") String username, @PathParam("storeitemid") int storeitemid) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int requestingId = userBean.getUserID(username, (Connection)null);
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         return new DataHolder(contentBean.getStoreItemInventory(requestingId, storeitemid));
      } catch (Exception var6) {
         log.error(String.format("Failed to retrieve store item id: %d", storeitemid), var6);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var6.getMessage());
      }
   }

   @POST
   @Path("/{username}/store/inventory/{storeitemid}/give")
   @Produces({"application/json"})
   public DataHolder<StoreItemData> giveInventory(@QueryParam("sessionId") String sessionId, @QueryParam("requestingUserid") int requestingUserId, @PathParam("storeitemid") int storeitemid, String jsonStr) throws FusionRestException {
      ConnectionPrx prx = ResourceUtil.getConnectionProxy(sessionId);
      UserData userData = new UserData(prx.getUserObject().getUserData());
      if (!userData.userID.equals(requestingUserId)) {
         log.error(String.format("User session id for %s is not the same with user %s", userData.username, requestingUserId));
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid session id for user %s", requestingUserId));
      } else {
         try {
            ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
            StoreItemData sid = contentBean.getStoreItem(userData.username, storeitemid);
            if (sid == null) {
               log.error(String.format("Failed to retrieved store item id for %d", storeitemid));
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find the store item id for " + storeitemid);
            } else {
               switch(sid.type) {
               case VIRTUAL_GIFT:
                  JSONObject json = new JSONObject(jsonStr);
                  String[] recepients = !StringUtil.isBlank(json.getString("to")) ? json.getString("to").split(",") : null;
                  String message = json.getString("message");
                  boolean privateGifts = json.getBoolean("private");
                  VirtualGiftData vgd = (VirtualGiftData)sid.referenceData;
                  Map<String, Integer> virtualGiftReceivedIdMap = contentBean.giveVirtualGiftForMultipleUsers(userData.username, Arrays.asList(recepients), sid, VirtualGiftReceivedData.PurchaseLocationEnum.STORE.value(), privateGifts, message);
                  UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  Map<Integer, UserData> recipientUserDataList = new HashMap();
                  ClientType deviceEnum = ClientType.fromValue(prx.getDeviceTypeAsInt());
                  String receiverStr = "";
                  Iterator i$ = virtualGiftReceivedIdMap.entrySet().iterator();

                  while(i$.hasNext()) {
                     Entry<String, Integer> entry = (Entry)i$.next();
                     String recipient = (String)entry.getKey();
                     UserData recipientUserData = userBean.loadUser(recipient, false, false);
                     if (recipientUserData != null) {
                        receiverStr = receiverStr + " @" + recipient;
                        recipientUserDataList.put(entry.getValue(), recipientUserData);
                     }
                  }

                  if (json.getBoolean("postToMiniblog") && virtualGiftReceivedIdMap.keySet().size() != 0) {
                     String msg;
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.WW422_ENABLED) && !StringUtil.isBlank(message)) {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGE_CUSTOM), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)), message);
                     } else if (virtualGiftReceivedIdMap.keySet().size() == 1) {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGE), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                     } else {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGES), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                     }

                     contentBean.createMigboTextPostForUser(userData.userID, msg, (String)null, (String)null, "1", deviceEnum, SSOEnums.View.fromFusionDeviceEnum(deviceEnum));
                  }

                  contentBean.onPurchaseVirtualGift(userData.username, recipientUserDataList, vgd, privateGifts, message, (String)null, true);
               case AVATAR:
               case STICKER:
               case EMOTICON:
               case SUPER_EMOTICON:
               case THEME:
               default:
                  return new DataHolder(sid);
               }
            }
         } catch (FusionRestException var23) {
            throw var23;
         } catch (Exception var24) {
            log.error("Failed to purchase store item.", var24);
            throw new FusionRestException(FusionRestException.RestException.ERROR, var24.getMessage());
         }
      }
   }

   @GET
   @Path("/{username}/merchant")
   @Produces({"application/json"})
   public DataHolder<List<MerchantLocationData>> getMerchantById(@QueryParam("requestingUserid") int requestingUserid, @PathParam("username") String username, @QueryParam("offset") int offset, @QueryParam("limit") int limit) throws FusionRestException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int userId = userEJB.getUserID(username, (Connection)null);
         UserData userData = userEJB.loadUserFromID(userId);
         MerchantsLocal merchantBean = (MerchantsLocal)EJBHomeCache.getLocalObject("MerchantsLocal", MerchantsLocalHome.class);
         List<MerchantLocationData> results = merchantBean.getMerchantsByCountry(requestingUserid, userData.countryID, offset, limit, true);
         return new DataHolder(results);
      } catch (Exception var10) {
         log.error("Exception caught while getting merchant location " + var10.getMessage(), var10);
         throw new FusionRestException(101, "Internal Server Error: Could not fetch merchant location");
      }
   }

   @POST
   @Path("/{userid}/file/upload")
   @Consumes({"image/jpeg", "image/gif", "image/png"})
   @Produces({"application/json"})
   public DataHolder<FileUploadData> uploadUserFile(@PathParam("userid") String userIdStr, @QueryParam("requestingUserid") int requestingUserid, @HeaderParam("Content-Type") MediaType contentType, @HeaderParam("Content-Length") int fileSize, InputStream is) throws FusionRestException {
      int userId = this.getAndCheckUserid(userIdStr);
      if (fileSize > SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.MAX_UPLOAD_FILE_SIZE)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to upload file. Max upload file size exceeded.");
      } else {
         try {
            if (log.isDebugEnabled()) {
               log.debug("user file upload content type: " + contentType + "; file size: " + fileSize);
            }

            BufferedImage image = ImageIO.read(is);
            if (null == image) {
               log.error(String.format("Error in reading image from input stream for user:%s", userId));
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to upload file. Please try again later.");
            } else {
               String photoId = Long.toString(System.currentTimeMillis());
               String s3Key = S3Uploader.calculatePath(userIdStr, photoId);
               S3UploaderContentTypeEnum s3ContentType = S3UploaderContentTypeEnum.fromMimeType(contentType.toString());
               String url = S3Uploader.uploadBufferedImage(new UserResource.FusionS3UploaderConfiguration(), s3Key, s3ContentType, image);
               if (StringUtil.isBlank(url)) {
                  log.error(String.format("Error in uploading file to s3 for user:%s, empty URL!", userId));
                  throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to upload file. Please try again later.");
               } else {
                  FileUploadData uploadData = new FileUploadData(url);
                  String thumbnailUrl = null;
                  int thumbnailWidth = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MimeDataSettings.IMAGE_THUMBNAIL_WIDTH);
                  if (image.getWidth() <= thumbnailWidth) {
                     thumbnailUrl = url;
                  } else {
                     BufferedImage thumbnail = Scalr.resize(image, thumbnailWidth, new BufferedImageOp[0]);
                     if (null != thumbnail) {
                        thumbnailUrl = S3Uploader.uploadBufferedImage(new UserResource.FusionS3UploaderConfiguration(), String.format("%s_%dx", s3Key, thumbnailWidth), s3ContentType, thumbnail);
                     }
                  }

                  if (!StringUtil.isBlank(thumbnailUrl)) {
                     uploadData.thumbnailUrl = thumbnailUrl;
                  } else {
                     log.error(String.format("Error in generating or uploading image thumbnail to s3 for user:%s", userId));
                  }

                  return new DataHolder(uploadData);
               }
            }
         } catch (FusionRestException var16) {
            throw var16;
         } catch (Exception var17) {
            log.error(String.format("Error in uploading file to s3 for user:%s", userId), var17);
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to upload file. Please try again later.");
         }
      }
   }

   @GET
   @Path("/test/DAO/userdata")
   @Produces({"application/json"})
   public DataHolder<UserData> testDAOUserData(@QueryParam("username") String username, @QueryParam("fullUserData") String fullUserDataStr) throws FusionRestException {
      try {
         UserObject user = new UserObject(username);
         log.info("DAO -> BCL:" + user.getBroadcastList());
         log.info("DAO -> contact list:" + user.getContactList());
         log.info("DAO -> group list:" + user.getGroupList());
         log.info("DAO -> usersetting:" + user.getUserSettings());
         log.info("DAO -> user id:" + user.getUserID());
         log.info("DAO -> ReputationScoreAndLevel:" + user.getReputationScoreAndLevel());
         log.info("DAO -> ReputationLevel:" + user.getReputationLevel());
         log.info("DAO -> BasicMerchantDetails:" + user.getBasicMerchantDetails());
         log.info("DAO -> AccountBalance:" + user.getAccountBalance());
         log.info("DAO -> GroupMember:" + user.getGroupMember(400393));
         log.info("DAO -> Emoticon IDS:" + user.getEmoticonPacks());
         log.info("DAO -> Emoticons:" + user.getEmoticons());
         log.info("DAO -> All Emoticons:" + DAOFactory.getInstance().getEmoAndStickerDAO().loadEmoticons());
         log.info("DAO -> All Emoticon packs:" + DAOFactory.getInstance().getEmoAndStickerDAO().loadEmoticonPacks());
         log.info("DAO -> getEmoticonPack 1:" + DAOFactory.getInstance().getEmoAndStickerDAO().getEmoticonPack(1));
         log.info("DAO -> get GroupData for groupd:400393:" + DAOFactory.getInstance().getGroupDAO().getGroup(400393));
         log.info("DAO -> get group chat rooms for group:400393:" + DAOFactory.getInstance().getChatRoomDAO().getGroupChatRooms(400393));
         log.info("DAO -> get isUserBlackListedInGroup for group:400393:" + user.isUserBlackListedInGroup(400393));
         log.info("DAO -> get ModeratorUserNames for group:400393:" + DAOFactory.getInstance().getGroupDAO().getModeratorUserNames(400393, false));
         log.info("DAO -> latest msg:" + DAOFactory.getInstance().getMessageDAO().getLatestAlertMessage(5, AlertMessageData.TypeEnum.CHAT_ROOM_WELCOME_MESSAGE, 119, new Date(), (AlertContentType)null, 7));
         log.info("System Properties: " + SystemProperty.getAllSystemProperties());
         log.info("DAO -> get ChatroomNamesPerCategory:" + DAOFactory.getInstance().getChatRoomDAO().getChatroomNamesPerCategory(true));
         log.info("DAO -> get Chatrooms:" + DAOFactory.getInstance().getChatRoomDAO().getChatRooms(199, "chat"));
         log.info("DAO -> get Chatrooms:" + DAOFactory.getInstance().getChatRoomDAO().getChatRooms(199, "test"));
         log.info("DAO -> get FavouriteChatRooms:" + user.getFavouriteChatRooms());
         log.info("DAO -> get RecentChatRooms:" + user.getRecentChatRooms());
         log.info("DAO -> get chat room data:clickers:" + DAOFactory.getInstance().getChatRoomDAO().getChatRoom("clickers"));
         log.info("DAO -> get bot data:1:" + DAOFactory.getInstance().getBotDAO().getBot(1));
         log.info("DAO -> gloadEmoticonHeights:" + DAOFactory.getInstance().getEmoAndStickerDAO().loadEmoticonHeights());
         log.info("DAO -> get opt height for height: 50 :" + DAOFactory.getInstance().getEmoAndStickerDAO().getOptimalEmoticonHeight(50));
         log.info("DAO -> load info text :" + DAOFactory.getInstance().getMessageDAO().loadInfoTexts());
         log.info("DAO -> load help text :" + DAOFactory.getInstance().getMessageDAO().loadHelpTexts());
         log.info("DAO -> load info text for 1 :" + DAOFactory.getInstance().getMessageDAO().getInfoText(1));
         return new DataHolder(user.getUserData(StringUtil.toBooleanOrDefault(fullUserDataStr, false), false));
      } catch (Exception var4) {
         log.error("Failed to retrieve DAO data", var4);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var4.getMessage());
      }
   }

   @GET
   @Path("/test/DAO/guardset")
   @Produces({"application/json"})
   public DataHolder<Short> testDAOGuardset(@QueryParam("clientType") String clientTypeStr, @QueryParam("guardCapability") String guardCapabilityStr) throws FusionRestException {
      try {
         int clientType = Integer.parseInt(clientTypeStr);
         int guardCapability = Integer.parseInt(guardCapabilityStr);
         return new DataHolder(DAOFactory.getInstance().getGuardsetDAO().getMinimumClientVersionForAccess(clientType, guardCapability));
      } catch (Exception var5) {
         log.error("Failed to retrieve DAO data", var5);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var5.getMessage());
      }
   }

   @POST
   @Path("/{username}/changepassword")
   @Produces({"application/json"})
   public DataHolder<BooleanData> changePassword(@PathParam("username") String username, @QueryParam("oldpassword") String oldPassword, @QueryParam("newpassword") String newPassword) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.changePassword(username, oldPassword, newPassword);
         return new DataHolder(new BooleanData(true));
      } catch (Exception var5) {
         log.error(String.format("Error in changepassword username:%s", username), var5);
         throw new FusionRestException(101, "Unable to changepassword");
      }
   }

   @POST
   @Path("/{username}/changemobilephone")
   @Produces({"application/json"})
   public DataHolder<BooleanData> changeMobilePhone(@PathParam("username") String username, @QueryParam("mobilenumber") String mobileNumber, @QueryParam("ipaddress") String ipAddress, @QueryParam("sessionid") String sessionID, @QueryParam("mobiledevice") String mobileDevice, @QueryParam("useragent") String userAgent) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.changeMobilePhone(username, mobileNumber, new AccountEntrySourceData(ipAddress, sessionID, mobileDevice, userAgent));
         return new DataHolder(new BooleanData(true));
      } catch (Exception var8) {
         log.error(String.format("Error in changemobilephone username:%s", username), var8);
         throw new FusionRestException(101, "Unable to changemobilephone");
      }
   }

   @POST
   @Path("/{username}/updateuserdisplaypicture")
   @Produces({"application/json"})
   public DataHolder<BooleanData> updateUserDisplayPicture(@PathParam("username") String username, @QueryParam("displaypictureid") String displayPictureId) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userBean.updateDisplayPicture(username, displayPictureId);
         return new DataHolder(new BooleanData(true));
      } catch (Exception var4) {
         log.error(String.format("Error in updateuserdisplaypicture username:%s", username), var4);
         throw new FusionRestException(101, "Unable to updateuserdisplaypicture");
      }
   }

   @POST
   @Path("/{username}/updateuserdetailsice")
   @Produces({"application/json"})
   public DataHolder<BooleanData> updateUserDetailsIce(@PathParam("username") String username) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(username, false, true);
         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(userData.username);
         userPrx.userDetailChanged(userData.toIceObject());
         return new DataHolder(new BooleanData(true));
      } catch (Exception var5) {
         log.error(String.format("Error in updateuserdetailsice username:%s", username), var5);
         throw new FusionRestException(101, "Unable to updateuserdetailsice");
      }
   }

   @GET
   @Path("/{username}/getuserownedchatrooms")
   @Produces({"application/json"})
   public DataHolder<Hashtable> getUserOwnedChatrooms(@PathParam("username") String username, @QueryParam("page") int page, @QueryParam("numentries") int numEntries) throws FusionRestException {
      try {
         Web webBean = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
         return new DataHolder(webBean.getUserOwnedChatrooms(username, page, numEntries));
      } catch (Exception var5) {
         log.error(String.format("Error in getuserownedchatrooms username:%s", username), var5);
         throw new FusionRestException(101, "Unable to getuserownedchatrooms");
      }
   }

   private static class FusionS3UploaderConfiguration implements S3UploaderConfiguration {
      private FusionS3UploaderConfiguration() {
      }

      public String getAccessKey() {
         return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.ACCESS_KEY);
      }

      public String getSecretKey() {
         return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.SECRET_KEY);
      }

      public String getS3BaseDomain() {
         return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.S3_BASE_DOMAIN);
      }

      public String getPhotoBucketName() {
         return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.BUCKET_NAME);
      }

      public int getMaxAgeForCache() {
         return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.MAX_AGE_FOR_CACHE);
      }

      public boolean useCdnDomain() {
         return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.USE_CDN_DOMAIN);
      }

      public String getCdnDomain() {
         return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.CDN_DOMAIN);
      }

      public int getAWSRefresh() {
         return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.AWS_REFRESH);
      }

      public int getMaxBucketFailures() {
         return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.S3UploaderSettings.MAX_BUCKET_FAILURES);
      }

      // $FF: synthetic method
      FusionS3UploaderConfiguration(Object x0) {
         this();
      }
   }
}
