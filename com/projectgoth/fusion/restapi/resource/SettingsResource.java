package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.SecurityQuestion;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.eventqueue.EventQueue;
import com.projectgoth.fusion.eventqueue.events.UserDataUpdatedEvent;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.ChangePasswordData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.EmailInqueryData;
import com.projectgoth.fusion.restapi.data.ForgotPasswordData;
import com.projectgoth.fusion.restapi.data.ForgotUsernameData;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.RegistrationTokenData;
import com.projectgoth.fusion.restapi.data.SettingsAccountCommunicationData;
import com.projectgoth.fusion.restapi.data.SettingsAccountPictureData;
import com.projectgoth.fusion.restapi.data.SettingsEnums;
import com.projectgoth.fusion.restapi.data.SettingsProfileDetailsData;
import com.projectgoth.fusion.restapi.data.SettingsUserAliasData;
import com.projectgoth.fusion.restapi.data.UserActivationData;
import com.projectgoth.fusion.restapi.data.UserEmailData;
import com.projectgoth.fusion.restapi.data.UserSettingRestData;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import com.projectgoth.fusion.restapi.util.RedisDataUtil;
import com.projectgoth.fusion.thirdpartysites.ThirdPartySiteCredentialManager;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

@Provider
@Path("/settings")
public class SettingsResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SettingsResource.class));

   @GET
   @Path("/security_questions")
   @Produces({"application/json"})
   public DataHolder<List<SecurityQuestion>> getSecurityQuestions() throws FusionRestException {
      try {
         List<SecurityQuestion> questions = SecurityQuestion.getAllQuestions();
         return new DataHolder(questions);
      } catch (Exception var2) {
         log.error(String.format("Failed to retrieve security questions due to EJB CreateException: %s", var2.getMessage()));
         throw new FusionRestException(101, "Internal error while retrieving security questions");
      }
   }

   @GET
   @Path("/{userid}/account/profile")
   @Produces({"application/json"})
   public DataHolder<SettingsProfileDetailsData> getProfileDetails(@PathParam("userid") String useridStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = null;
         if (userid != -1) {
            userData = userEJB.loadUserFromID(userid);
         } else {
            userData = userEJB.loadUser(useridStr, false, false);
            if (userData == null) {
               throw new FusionRestException(102, String.format("Unable to find user '%s'", useridStr));
            }

            userid = userData.userID;
         }

         if (userData == null) {
            throw new FusionRestException(102, String.format("Unable to find user '%s'", useridStr));
         } else {
            String username = userData.username;
            UserProfileData upData = null;

            try {
               upData = userEJB.getUserProfile((String)null, username, false);
            } catch (EJBException var13) {
               log.error(String.format("Failed to retrieve user profile '%s' due to EJBException in UserBean.getUserProfile: %s", useridStr, var13.getMessage()), var13);
               throw new FusionRestException(101, "Internal error while retrieving user data");
            } catch (FusionEJBException var14) {
               log.error(String.format("Failed to retrieve user profile '%s' due to EJBFusionException in UserBean.getUserProfile: %s", useridStr, var14.getMessage()), var14);
               throw new FusionRestException(101, "Internal error while retrieving user data");
            }

            MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            CountryData countryData = misBean.getCountry(userData.countryID);
            Map<String, Integer> privacy = RedisDataUtil.getAccountProfilePrivacy(userData.userID);
            Map<String, Integer> privacyToWrite = new HashMap();
            SettingsEnums.EveryoneOrFollowerAndFriend feed = getFeedContentPrivacyWithMigration(userid, username, userEJB);
            if (!privacy.containsKey("DobPrivacy")) {
               SettingsEnums.Birthday dobNew = getMigratedBirthdayPrivacy(userid, username, feed);
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

            if (privacyToWrite.size() > 0 && !RedisDataUtil.setAccountPrivacy(userid, privacyToWrite)) {
               log.error(String.format("Failed to save user account communication privacy '%s'", username));
            }

            return new DataHolder(new SettingsProfileDetailsData(userData, upData, countryData, privacy));
         }
      } catch (CreateException var15) {
         log.error(String.format("Failed to retrieve user profile '%s' due to EJB CreateException: %s", useridStr, var15.getMessage()), var15);
         throw new FusionRestException(101, "Internal error while retrieving user data");
      }
   }

   @POST
   @Path("/{username}/account/profile")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response setProfileDetails(@PathParam("username") String username, DataHolder<SettingsProfileDetailsData> dataHolder) throws FusionRestException {
      SettingsProfileDetailsData data = (SettingsProfileDetailsData)dataHolder.data;
      if (!StringUtil.isBlank(username) && username.equalsIgnoreCase((String)data.username.value)) {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserProfileData upData = null;
            upData = new UserProfileData();
            upData.username = username;
            data.updateUserProfileData(upData);

            try {
               userEJB.updateUserProfile(upData);
               int userId = userEJB.getUserID(username, (Connection)null);
               UserData userData = userEJB.loadUser(username, false, false);
               UserEmailAddressData emailAddress = userEJB.getUserEmailAddressByType(userData.userID, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY);
               boolean sendEmailVerification;
               if (emailAddress == null) {
                  sendEmailVerification = data.externalEmail.value != null && userEJB.addUserEmailAddress(userId, (String)data.externalEmail.value, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY);
               } else if (data.externalEmail.value != null && !((String)data.externalEmail.value).equals(emailAddress.emailAddress)) {
                  boolean updatedUserEmailAddress = userEJB.updateUserEmailAddress(userId, emailAddress.emailAddress, (String)data.externalEmail.value, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY);
                  sendEmailVerification = updatedUserEmailAddress && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.SEND_EMAIL_VERIFICATION_AFTER_EMAIL_ADDRESS_UPDATE);
               } else {
                  sendEmailVerification = false;
               }

               if (sendEmailVerification) {
                  try {
                     userEJB.sendEmailVerification(userId, (String)data.externalEmail.value);
                  } catch (Exception var16) {
                     log.error("Error sending email verification to userId[" + userId + "] email[" + (String)data.externalEmail.value + "]." + var16, var16);
                  }
               }

               Map<String, Integer> oldPrivacy = RedisDataUtil.getAccountProfilePrivacy(userId);
               Map<String, Integer> newPrivacy = RedisDataUtil.removeUnchangedPrivacy(data.retrievePrivacy(), oldPrivacy);
               if (newPrivacy.size() > 0) {
                  if (!RedisDataUtil.setAccountPrivacy(userId, newPrivacy)) {
                     log.error(String.format("Failed to save user profile privacy '%s'", username));
                  }

                  boolean doInAsync = !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserProfileSettings.DO_SYNCHRONOUS_PROFILE_CACHE_INVALIDATION_ON_MIGBO_UPON_PROFILE_SETTING_UPDATE);
                  if (!doInAsync) {
                     try {
                        MigboApiUtil api = MigboApiUtil.getInstance();
                        JSONObject obj = api.delete(String.format("/user/%d/cache/profile", userId));
                        log.debug(String.format("Received JSON Response from migbo-datsvc : %s ", obj.toString()));
                     } catch (Exception var17) {
                        boolean rollback = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserProfileSettings.FAIL_PROFILE_SETTING_UPDATE_IF_FAIL_DO_SYNCHRONOUS_PROFILE_CACHE_INVALIDATION_ON_MIGBO);
                        log.error(String.format("Exception caught while invalidating profile cache on migbo for user [%d %s], %s", userId, username, rollback ? "rolling back" : "doing it in async"), var17);
                        if (rollback) {
                           Map<String, Integer> oldUpdatedPrivacy = RedisDataUtil.keepChangedPrivacy(oldPrivacy, newPrivacy);
                           if (!RedisDataUtil.setAccountPrivacy(userId, oldUpdatedPrivacy)) {
                              log.error(String.format("Failed to rollback user profile privacy changes '%s'", username));
                           }

                           throw new FusionRestException(101, "Internal error while saving user data");
                        }

                        doInAsync = true;
                     }
                  }

                  if (doInAsync) {
                     EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.PROFILE));
                  }
               }
            } catch (EJBException var18) {
               log.error(String.format("Failed to save user profile '%s' due to EJBException: %s", username, var18.getMessage()));
               throw new FusionRestException(101, "Error: " + var18.getMessage());
            } catch (FusionEJBException var19) {
               log.error(String.format("Failed to save user profile '%s' due to EJBException: %s", username, var19.getMessage()));
               throw new FusionRestException(101, "Error: " + var19.getMessage());
            }
         } catch (CreateException var20) {
            log.error(String.format("Failed to save user profile '%s' due to EJB CreateException: %s", username, var20.getMessage()));
            throw new FusionRestException(101, "Internal error while saving user data");
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      } else {
         log.error(String.format("Failed to save user profile: username '%s' in REST path does not match username '%s' in data", username, data.username.value));
         throw new FusionRestException(-1, "Incorrect username or profile data specified");
      }
   }

   @GET
   @Path("/{username}/account/communication")
   @Produces({"application/json"})
   public DataHolder<SettingsAccountCommunicationData> getAccountCommunication(@PathParam("username") String username) throws FusionRestException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         boolean var3 = true;

         int userId;
         try {
            userId = userEJB.getUserID(username, (Connection)null);
         } catch (EJBException var8) {
            log.error(String.format("Failed to retrieve account communication privacy '%s' due to EJBException in getting userid: %s", username, var8.getMessage()));
            throw new FusionRestException(101, "Internal error while retrieving communication settings");
         }

         Map<String, Integer> privacy = RedisDataUtil.getAccountCommunicationPrivacy(userId);
         Map<String, Integer> privacyToWrite = new HashMap();
         SettingsEnums.EveryoneOrFollowerAndFriend feed = null;
         if (!privacy.containsKey("FeedPv")) {
            feed = getOldFeedContentPrivacy(userId, username, userEJB);
            privacy.put("FeedPv", feed.value());
            privacyToWrite.put("FeedPv", feed.value());
         } else {
            feed = SettingsEnums.EveryoneOrFollowerAndFriend.fromValue((Integer)privacy.get("FeedPv"));
         }

         if (!privacy.containsKey("ChatPv")) {
            SettingsEnums.EveryoneFollowerFriend chatNew = getMigratedChatPrivacy(userId, username, userEJB, feed);
            privacy.put("ChatPv", chatNew.value());
            privacyToWrite.put("ChatPv", chatNew.value());
         }

         if (!privacy.containsKey("BuzzPv")) {
            SettingsEnums.OnOff buzzNew = getMigratedBuzzPrivacy(userId, username, userEJB);
            privacy.put("BuzzPv", buzzNew.value());
            privacyToWrite.put("BuzzPv", buzzNew.value());
         }

         if (privacyToWrite.size() > 0 && !RedisDataUtil.setAccountPrivacy(userId, privacyToWrite)) {
            log.error(String.format("Failed to save user account communication privacy '%s'", username));
         }

         return new DataHolder(new SettingsAccountCommunicationData(privacy));
      } catch (CreateException var9) {
         log.error(String.format("Failed to retrieve account communication privacy '%s' due to EJB CreateException: %s", username, var9.getMessage()));
         throw new FusionRestException(101, "Internal error while retrieving communication settings");
      }
   }

   public static SettingsEnums.EveryoneFollowerFriend getMigratedChatPrivacy(int userid, String username, UserLocal userEJB, SettingsEnums.EveryoneOrFollowerAndFriend feed) throws FusionRestException {
      log.info(String.format("migrating chat privacy '%d'", userid));
      SettingsEnums.EveryoneFollowerFriend chatNew = SettingsAccountCommunicationData.PRIVACY_DEFAULT_CHAT;

      try {
         UserSettingData.MessageEnum chat = userEJB.getMessageSetting(username);
         if (chat == UserSettingData.MessageEnum.FRIENDS_ONLY) {
            chatNew = SettingsEnums.EveryoneFollowerFriend.FRIEND_ONLY;
         } else if (chat == UserSettingData.MessageEnum.EVERYONE) {
            chatNew = SettingsEnums.EveryoneFollowerFriend.EVERYONE;
         }

         return chatNew;
      } catch (EJBException var6) {
         log.error(String.format("Failed to retrieve user privacy '%d' due to EJBException in getting chat setting: %s", userid, var6.getMessage()));
         throw new FusionRestException(101, "Internal error while retrieving communication settings");
      }
   }

   public static SettingsEnums.Birthday getMigratedBirthdayPrivacy(int userid, String username, SettingsEnums.EveryoneOrFollowerAndFriend feed) {
      log.info(String.format("migrating dob privacy from DB '%s'", username));
      SettingsEnums.Birthday dobNew = SettingsProfileDetailsData.PRIVACY_DEFAULT_BIRTHDAY;
      if (feed == SettingsEnums.EveryoneOrFollowerAndFriend.EVERYONE) {
         dobNew = SettingsEnums.Birthday.SHOW_FULL;
      } else if (feed == SettingsEnums.EveryoneOrFollowerAndFriend.FRIEND_OR_FOLLOWER) {
         dobNew = SettingsEnums.Birthday.HIDE;
      }

      return dobNew;
   }

   public static SettingsEnums.OnOff getMigratedBuzzPrivacy(int userid, String username, UserLocal userEJB) throws FusionRestException {
      log.info(String.format("getting buzz privacy from DB '%s'", username));
      SettingsEnums.OnOff buzzNew = SettingsAccountCommunicationData.PRIVACY_DEFAULT_BUZZ;

      try {
         if (username == null) {
            username = userEJB.getUsernameByUserid(userid, (Connection)null);
         }

         UserData userData = userEJB.loadUser(username, false, false);
         if (userData.allowBuzz != null) {
            buzzNew = userData.allowBuzz ? SettingsEnums.OnOff.ON : SettingsEnums.OnOff.OFF;
         }

         return buzzNew;
      } catch (EJBException var5) {
         log.error(String.format("Failed to retrieve account communication privacy '%s' due to EJBException in getting buzz setting: %s", username, var5.getMessage()));
         throw new FusionRestException(101, "Internal error while retrieving communication settings");
      }
   }

   public static SettingsEnums.EveryoneOrFollowerAndFriend getFeedContentPrivacyWithMigration(int userid, String username, UserLocal userEJB) throws FusionRestException {
      Map<String, Integer> privacy = RedisDataUtil.getAccountCommunicationPrivacy(userid);
      SettingsEnums.EveryoneOrFollowerAndFriend feed = null;
      if (!privacy.containsKey("FeedPv")) {
         log.info(String.format("migrating feed privacy from DB '%s'", username));
         feed = getOldFeedContentPrivacy(userid, username, userEJB);
         Map<String, Integer> privacyToWrite = new HashMap();
         privacyToWrite.put("FeedPv", feed.value());
         if (!RedisDataUtil.setAccountPrivacy(userid, privacyToWrite)) {
            log.error(String.format("Failed to save user account communication privacy '%s'", username));
         }
      } else {
         feed = SettingsEnums.EveryoneOrFollowerAndFriend.fromValue((Integer)privacy.get("FeedPv"));
      }

      return feed;
   }

   public static SettingsEnums.EveryoneOrFollowerAndFriend getOldFeedContentPrivacy(int userid, String username, UserLocal userEJB) throws FusionRestException {
      log.info(String.format("getting feed privacy from DB '%s'", username));
      SettingsEnums.EveryoneOrFollowerAndFriend feedNew = SettingsAccountCommunicationData.PRIVACY_DEFAULT_FEED;

      try {
         if (username == null) {
            username = userEJB.getUsernameByUserid(userid, (Connection)null);
         }

         UserProfileData upData = userEJB.getUserProfile((String)null, username, false);
         if (upData == null) {
            return feedNew;
         } else {
            if (upData.status != null) {
               feedNew = upData.status == UserProfileData.StatusEnum.PUBLIC ? SettingsEnums.EveryoneOrFollowerAndFriend.EVERYONE : SettingsEnums.EveryoneOrFollowerAndFriend.FRIEND_OR_FOLLOWER;
            }

            return feedNew;
         }
      } catch (EJBException var5) {
         log.error(String.format("Failed to retrieve account communication privacy '%s' due to EJBException in getting feed setting: %s", username, var5.getMessage()));
         throw new FusionRestException(101, "Internal error while retrieving communication settings");
      } catch (FusionEJBException var6) {
         log.error(String.format("Failed to retrieve account communication privacy '%s' due to EJBException in getting feed setting: %s", username, var6.getMessage()));
         throw new FusionRestException(101, "Internal error while retrieving communication settings");
      }
   }

   @POST
   @Path("/{username}/account/communication")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response setAccountCommunication(@PathParam("username") String username, DataHolder<SettingsAccountCommunicationData> dataHolder) throws FusionRestException {
      SettingsAccountCommunicationData data = (SettingsAccountCommunicationData)dataHolder.data;
      if (StringUtil.isBlank(username)) {
         log.error(String.format("Failed to save user account communication privacy: no username is specified"));
         throw new FusionRestException(-1, "Incorrect username specified");
      } else {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);

            try {
               int userId = userEJB.getUserID(username, (Connection)null);
               Map<String, Integer> oldPrivacy = RedisDataUtil.getAccountCommunicationPrivacy(userId);
               Map<String, Integer> newPrivacy = RedisDataUtil.removeUnchangedPrivacy(data.retrievePrivacy(), oldPrivacy);
               if (newPrivacy.size() > 0) {
                  this.syncChatPrivacyToFusionDB(data, username, userEJB);
                  if (!RedisDataUtil.setAccountPrivacy(userId, newPrivacy)) {
                     log.error(String.format("Failed to save user account communication privacy '%s'", username));
                  }

                  if (newPrivacy.containsKey("ChatPv") || newPrivacy.containsKey("FeedPv")) {
                     boolean doInAsync = !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserProfileSettings.DO_SYNCHRONOUS_PRIVACY_CACHE_INVALIDATION_ON_MIGBO_UPON_COMM_SETTING_UPDATE);
                     if (!doInAsync) {
                        try {
                           MigboApiUtil api = MigboApiUtil.getInstance();
                           JSONObject obj = api.delete(String.format("/user/%d/cache/privacy", userId));
                           log.debug(String.format("Received JSON Response from migbo-datsvc : %s ", obj.toString()));
                        } catch (Exception var12) {
                           boolean rollback = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserProfileSettings.FAIL_COMM_SETTING_UPDATE_IF_FAIL_DO_SYNCHRONOUS_PRIVACY_CACHE_INVALIDATION_ON_MIGBO);
                           log.error(String.format("Exception caught while invalidating privacy cache on migbo for user [%d %s], %s", userId, username, rollback ? "rolling back" : "doing it in async"), var12);
                           if (rollback) {
                              Map<String, Integer> oldUpdatedPrivacy = RedisDataUtil.keepChangedPrivacy(oldPrivacy, newPrivacy);
                              if (!RedisDataUtil.setAccountPrivacy(userId, oldUpdatedPrivacy)) {
                                 log.error(String.format("Failed to rollback user account communication privacy changes '%s'", username));
                              }

                              throw new FusionRestException(101, "Internal error while saving account communication privacy");
                           }

                           doInAsync = true;
                        }
                     }

                     if (doInAsync) {
                        EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.PROFILE));
                     }
                  }
               }
            } catch (EJBException var13) {
               log.error(String.format("Failed to save user account communication privacy '%s' due to EJBException: %s", username, var13.getMessage()));
               throw new FusionRestException(101, "Internal error while saving account communication privacy");
            }
         } catch (CreateException var14) {
            log.error(String.format("Failed to save user account communication privacy '%s' due to EJB CreateException: %s", username, var14.getMessage()));
            throw new FusionRestException(101, "Internal error while saving account communication privacy");
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   public void syncChatPrivacyToFusionDB(SettingsAccountCommunicationData data, String username, UserLocal userEJB) throws FusionRestException {
      Map<String, Integer> privacyOptions = data.retrievePrivacy();
      Integer chatPrivacyOption = (Integer)privacyOptions.get("ChatPv");
      if (chatPrivacyOption == null) {
         log.error("Invalid chat privacy for user = " + username);
         throw new FusionRestException(-1, "Incorrect chat privacy option specified");
      } else {
         if (chatPrivacyOption == SettingsEnums.EveryoneFollowerFriend.FOLLOWER_ONLY.value()) {
            chatPrivacyOption = SettingsEnums.EveryoneFollowerFriend.FRIEND_ONLY.value();
         }

         UserSettingData.MessageEnum chatSetting = UserSettingData.MessageEnum.fromValue(chatPrivacyOption);
         if (chatSetting == null) {
            log.error("Invalid chat setting for user = " + username);
            throw new FusionRestException(-1, "Incorrect chat setting specified");
         } else {
            userEJB.updateMessageSetting(username, chatSetting);
         }
      }
   }

   @GET
   @Path("/{userid}/account/picture")
   @Produces({"application/json"})
   public DataHolder<SettingsAccountPictureData> getProfilePicture(@PathParam("userid") String useridStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = null;
         if (userid != -1) {
            userData = userEJB.loadUserFromID(userid);
         } else {
            userData = userEJB.loadUser(useridStr, false, false);
            userid = userData.userID;
         }

         if (userData == null) {
            throw new FusionRestException(102, String.format("Unable to find user '%s'", useridStr));
         } else {
            return new DataHolder(new SettingsAccountPictureData(RedisDataUtil.getUserDisplayPictureSetting(userid)));
         }
      } catch (CreateException var6) {
         log.error(String.format("Failed to retrieve user display picture setting '%s' due to EJB CreateException: %s", useridStr, var6.getMessage()), var6);
         throw new FusionRestException(101, "Internal error while retrieving user data");
      }
   }

   @POST
   @Path("/{userid}/account/picture")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response setProfilePicture(@PathParam("userid") String useridStr, DataHolder<SettingsAccountPictureData> dataHolder) throws FusionRestException {
      SettingsAccountPictureData data = (SettingsAccountPictureData)dataHolder.data;
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Failed to save user account display picture setting: no user id is specified"));
         throw new FusionRestException(-1, "Incorrect user id specified");
      } else {
         if (!RedisDataUtil.setUserDisplayPictureSetting(userid, data.retrieveDisplayPictureSetting())) {
            log.error(String.format("Failed to save user account display picture setting '%s'", useridStr));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/{userid}/email/{emailtype}/notification/")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response setEmailNotification(@PathParam("userid") String useridStr, @PathParam("emailtype") String emailTypeStr, DataHolder<BooleanData> enabled) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      int emailTypeInt = StringUtil.toIntOrDefault(emailTypeStr, -1);
      UserSettingData.TypeEnum emailType = UserSettingData.TypeEnum.fromValue(emailTypeInt);
      if (userid == -1) {
         log.error(String.format("Failed to save user email notification setting: no user id is specified"));
         throw new FusionRestException(-1, "Incorrect user id specified");
      } else if (emailType == null) {
         log.error(String.format("Failed to save user email notification setting: invalid email type"));
         throw new FusionRestException(-1, "Incorrect email type specified");
      } else if (((BooleanData)enabled.data).value == null) {
         log.error(String.format("Failed to save user email notification setting: boolean value is null"));
         throw new FusionRestException(-1, "Incorrect boolean value specified");
      } else {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String username = userEJB.getUsernameByUserid(userid, (Connection)null);
            userEJB.updateEmailNotificationSetting(username, emailType, ((BooleanData)enabled.data).value ? UserSettingData.EmailSettingEnum.ENABLED : UserSettingData.EmailSettingEnum.DISABLED);
            return Response.ok().entity(new DataHolder("ok")).build();
         } catch (Exception var9) {
            log.error(String.format("Failed to save user email notification setting '%s': %s", useridStr, var9.getMessage()));
            throw new FusionRestException(101, "Internal error while saving user email notification setting");
         }
      }
   }

   @POST
   @Path("/{userid}/emailcategory/{emailtype}/notification/")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response setEmailCategoryNotification(@PathParam("userid") String useridStr, @PathParam("emailtype") String emailTypeStr, DataHolder<String> emailSettingValue) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      UserSettingData.TypeEnum emailType = UserSettingData.TypeEnum.fromName(emailTypeStr);
      if (userid == -1) {
         log.error(String.format("Failed to save user email notification setting: no user id is specified"));
         throw new FusionRestException(-1, "Incorrect user id specified");
      } else if (emailType == null) {
         log.error(String.format("Failed to save user email notification setting: invalid email type"));
         throw new FusionRestException(-1, "Incorrect email type specified");
      } else {
         UserSettingData.EmailSettingEnum emailSetting = UserSettingData.EmailSettingEnum.fromName((String)emailSettingValue.data);
         if (emailSetting == null) {
            log.error(String.format("Failed to save user email notification setting: invalid email setting value"));
            throw new FusionRestException(-1, "Incorrect email setting value specified");
         } else {
            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               String username = userEJB.getUsernameByUserid(userid, (Connection)null);
               userEJB.updateEmailNotificationSetting(username, emailType, emailSetting);
               return Response.ok().entity(new DataHolder("ok")).build();
            } catch (Exception var9) {
               log.error(String.format("Failed to save user email notification setting '%s': %s", useridStr, var9.getMessage()));
               throw new FusionRestException(101, "Internal error while saving user email notification setting");
            }
         }
      }
   }

   @GET
   @Path("/{userid}/account/alias")
   @Produces({"application/json"})
   public DataHolder<SettingsUserAliasData> getUserAlias(@PathParam("userid") String useridStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String alias = null;
         if (userid != -1) {
            alias = userEJB.getUserAliasByUserid(userid, (Connection)null);
         } else {
            alias = userEJB.getUserAliasByUsername(useridStr, (Connection)null);
         }

         return new DataHolder(new SettingsUserAliasData(alias));
      } catch (CreateException var6) {
         log.error(String.format("Failed to retrieve user alias '%s' due to EJB CreateException: %s", useridStr, var6.getMessage()), var6);
         throw new FusionRestException(101, "Internal error while retrieving user alias");
      }
   }

   @GET
   @Path("/{userid}/account/alias/check")
   @Produces({"application/json"})
   public DataHolder<? extends Object> checkUserAlias(@PathParam("userid") String useridStr, @QueryParam("alias") String alias) throws FusionRestException {
      if (StringUtil.isBlank(alias)) {
         throw new FusionRestException(103, String.format("alias is not specified"));
      } else {
         int userid = StringUtil.toIntOrDefault(useridStr, -1);

         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String username = null;
            if (userid == -1) {
               username = useridStr;
               userid = userEJB.getUserID(useridStr, (Connection)null, false);
            } else {
               username = userEJB.getUsernameByUserid(userid, (Connection)null);
            }

            if (userid == -1) {
               throw new FusionRestException(102, String.format("Invalid user '%s'", useridStr));
            } else {
               HashMap data = new HashMap();

               try {
                  userEJB.checkUserAliasByUserid(userid, alias, alias.equalsIgnoreCase(username), (Connection)null);
                  data.put("is_valid_alias", Boolean.TRUE);
               } catch (EJBException var8) {
                  data.put("is_valid_alias", Boolean.FALSE);
                  data.put("reason", var8.getMessage());
               }

               return new DataHolder(data);
            }
         } catch (CreateException var9) {
            log.error(String.format("Failed to check user alias '%s' for user '%s' due to EJB CreateException: %s", alias, useridStr, var9.getMessage()), var9);
            throw new FusionRestException(101, "Internal error while checking user alias");
         }
      }
   }

   @POST
   @Path("/{userid}/account/alias")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response setUserAlias(@PathParam("userid") String useridStr, DataHolder<SettingsUserAliasData> dataHolder) throws FusionRestException {
      SettingsUserAliasData data = (SettingsUserAliasData)dataHolder.data;
      log.info("set user alias");
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_USERDATA_DAO)) {
         log.info("DAO: set user alias");

         try {
            UserObject user = UserObject.createUserObject(useridStr);
            user.setAlias(data.alias);
            return Response.ok().entity(new DataHolder("ok")).build();
         } catch (DAOException var7) {
            log.error(String.format("Failed to save alias:[%s] for user:[%s] due to DAOException: %s", data.alias, useridStr, var7.getMessage()), var7);
            throw new FusionRestException(103, String.format("Unable to set alias: %s", var7.getMessage()));
         }
      } else {
         log.info("EJB: user alias");

         try {
            int userid = StringUtil.toIntOrDefault(useridStr, -1);
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            if (userid == -1) {
               userid = userEJB.getUserID(useridStr, (Connection)null, false);
            }

            if (userid == -1) {
               throw new FusionRestException(102, String.format("Invalid user '%s'", useridStr));
            } else {
               try {
                  userEJB.setUserAliasByUserid(userid, data.alias);
               } catch (EJBException var8) {
                  throw new FusionRestException(103, String.format("Unable to set alias: %s", var8.getMessage()));
               }

               return Response.ok().entity(new DataHolder("ok")).build();
            }
         } catch (CreateException var9) {
            log.error(String.format("Failed to save user alias '%s' due to EJB CreateException: %s", useridStr, var9.getMessage()), var9);
            throw new FusionRestException(101, "Internal error while saving user alias");
         }
      }
   }

   @POST
   @Path("/emailinquery")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response emailInquery(DataHolder<EmailInqueryData> dataHolder) throws FusionRestException {
      EmailInqueryData data = (EmailInqueryData)dataHolder.data;

      try {
         EJBIcePrxFinder.getUserNotificationServiceProxy().sendEmailFromNoReply(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.INQUERY_TO_EMAIL_ADDRESS), data.subject, data.content);
         return Response.ok().entity(new DataHolder("ok")).build();
      } catch (Exception var4) {
         log.error(String.format("Failed to send email inquery due toException: %s", var4.getMessage()), var4);
         throw new FusionRestException(101, "Internal error while saving user alias");
      }
   }

   /** @deprecated */
   @Deprecated
   public DataHolder<RegistrationTokenData> setUserAlias(@PathParam("userid") Integer userid, @PathParam("token") String token) throws FusionRestException {
      return this.verifyExternalEmailAddress(userid, token);
   }

   @GET
   @Path("/{userid}/email/verify/{token}")
   @Produces({"application/json"})
   public DataHolder<RegistrationTokenData> verifyExternalEmailAddress(@PathParam("userid") Integer userid, @PathParam("token") String token) throws FusionRestException {
      try {
         UserLocal user = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);

         try {
            return new DataHolder(user.verifyExternalEmailAddress(userid, token));
         } catch (EJBException var5) {
            log.error("Email verification failed for [" + userid + "] : " + var5.getMessage());
            throw new FusionRestException(102, "Invalid token provided.");
         }
      } catch (CreateException var6) {
         log.error("Email verification failed for [" + userid + "] : " + var6.getMessage());
         throw new FusionRestException(101, "Failed to verify email address - internal esrver error.");
      } catch (Exception var7) {
         log.error("Email verification failed for [" + userid + "] : " + var7.getMessage());
         throw new FusionRestException(102, "Failed to verify email address - " + var7.getMessage());
      }
   }

   @GET
   @Path("/{userid}/security_question")
   @Produces({"application/json"})
   public DataHolder<String> getUserSecurityQuestion(@PathParam("userid") String userIdStr) throws FusionRestException {
      try {
         int userid = StringUtil.toIntOrDefault(userIdStr, -1);
         if (userid == -1) {
            throw new FusionRestException(101, String.format("Invalid userid [%s]", userIdStr));
         } else {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            SecurityQuestion sq = userEJB.getSecurityQuestion(userid);
            if (sq == null) {
               throw new FusionRestException(101, "User has not set security question yet.");
            } else {
               return new DataHolder(sq.question);
            }
         }
      } catch (Exception var5) {
         log.error("Exception caught while retrieving user categories: " + var5.getMessage(), var5);
         throw new FusionRestException(101, "Internal error while retrieving user security question.");
      }
   }

   @GET
   @Path("/{userid}/email/activation_request")
   @Produces({"application/json"})
   public Response sendEmailVerification(@PathParam("userid") Integer userid) throws FusionRestException {
      String username = null;

      try {
         UserLocal user = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         username = user.getUsernameByUserid(userid, (Connection)null);
         if (null == username) {
            throw new FusionRestException(102, "User does not exist.");
         }

         String emailAddressToVerify = null;
         UserEmailAddressData emailAddress = user.getUserEmailAddressByType(userid, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY);
         if (emailAddress != null) {
            emailAddressToVerify = emailAddress.emailAddress;
         } else {
            UserData userData = user.loadUserFromID(userid);
            emailAddressToVerify = userData.emailAddress;
            user.addUserEmailAddress(userid, emailAddressToVerify, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY);
         }

         if (StringUtil.isBlank(emailAddressToVerify)) {
            throw new FusionRestException(101, "No email address detected.");
         }

         user.sendEmailVerification(userid, emailAddressToVerify);
      } catch (CreateException var7) {
         log.error(String.format("Failed to save user profile '%s' due to EJBException: %s", username, var7.getMessage()));
         throw new FusionRestException(101, "Internal error while saving user data");
      } catch (Exception var8) {
         log.error(String.format("Failed to save user profile '%s' due to EJBException: %s", username, var8.getMessage()));
         throw new FusionRestException(101, "Failed to send activation email - " + var8.getMessage());
      }

      return Response.ok().entity(new DataHolder("ok")).build();
   }

   @POST
   @Path("/email/activation_request/username/{username}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response sendEmailVerificationWithUsername(@PathParam("username") String username, DataHolder<UserEmailData> dataholder) throws FusionRestException {
      UserEmailData userEmailData = (UserEmailData)dataholder.data;

      try {
         UserLocal user = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         if (!user.isEmailRegistrationNotVerifiedForUsername(username, userEmailData.emailAddress)) {
            throw new FusionRestException(101, "Username and email address do not match");
         }

         UserActivationData userActivationData = user.getVerificationDataFromUserRegistrationTable(username);
         user.sendVerificationToken(username, userActivationData.emailAddress, userActivationData.token, userActivationData.registrationType);
      } catch (CreateException var6) {
         log.error(String.format("Failed to save user profile '%s' due to EJBException: %s", username, var6.getMessage()));
         throw new FusionRestException(101, "Internal error while saving user data");
      } catch (Exception var7) {
         log.error(String.format("Failed to save user profile '%s' due to EJBException: %s", username, var7.getMessage()));
         throw new FusionRestException(101, "Failed to send activation email - " + var7.getMessage());
      }

      return Response.ok().entity(new DataHolder("ok")).build();
   }

   @POST
   @Path("/{userid}/account/thirdpartysites/{siteid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response updateThirdPartySiteCredential(@PathParam("userid") String userIdStr, @PathParam("siteid") String siteIdStr, String credentialsJson) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, -1);
      if (userId == -1) {
         throw new FusionRestException(101, "Invalid User ID");
      } else {
         byte siteId = StringUtil.toByteOrDefault(siteIdStr, (byte)-1);
         if (siteId == -1) {
            throw new FusionRestException(101, "Invalid third party site ID");
         } else {
            log.info("Updating credential for User ID [" + userIdStr + "] and Site ID [" + siteIdStr + "] with Credential: " + credentialsJson);
            if (ThirdPartySiteCredentialManager.updateCredential(userId, siteId, credentialsJson)) {
               return Response.ok().entity(new DataHolder("ok")).build();
            } else {
               throw new FusionRestException(101, "Internal Server Error: Failed to update credential");
            }
         }
      }
   }

   @POST
   @Path("/account/thirdpartysites/{siteid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response updateThirdPartySiteCredentialUsingEmail(@QueryParam("emailAddress") String emailAddress, @PathParam("siteid") String siteIdStr, String credentialsJson) throws FusionRestException {
      try {
         UserLocal user = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int userId = user.getUserID(user.getUsernameByEmailAddress(emailAddress, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY), (Connection)null);
         return this.updateThirdPartySiteCredential(String.valueOf(userId), siteIdStr, credentialsJson);
      } catch (Exception var6) {
         throw new FusionRestException(101, "Unable to link email address");
      }
   }

   @DELETE
   @Path("/{userid}/account/thirdpartysites/{siteid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response deleteThirdPartySiteCredential(@PathParam("userid") String userIdStr, @PathParam("siteid") String siteIdStr) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, -1);
      if (userId == -1) {
         throw new FusionRestException(101, "Invalid User ID");
      } else {
         byte siteId = StringUtil.toByteOrDefault(siteIdStr, (byte)-1);
         if (siteId == -1) {
            throw new FusionRestException(101, "Invalid third party site ID");
         } else {
            log.info("Deleting credential for User ID [" + userIdStr + "] and Site ID [" + siteIdStr + "]");
            if (ThirdPartySiteCredentialManager.deleteCredential(userId, siteId)) {
               return Response.ok().entity(new DataHolder("ok")).build();
            } else {
               throw new FusionRestException(101, "Internal Server Error: Failed to delete credential");
            }
         }
      }
   }

   @GET
   @Path("/{userid}/account/thirdpartysites")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public String getThirdPartySiteCredentials(@PathParam("userid") String userIdStr) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, -1);
      if (userId == -1) {
         throw new FusionRestException(101, "Invalid User ID");
      } else {
         log.info("Retrieving third party site credentials for User ID [" + userIdStr + "]");
         String credentials = ThirdPartySiteCredentialManager.getCredentialsJsonStr(userId);
         if (credentials == null) {
            throw new FusionRestException(101, "Internal Server Error: Failed to get credential");
         } else {
            return credentials;
         }
      }
   }

   @GET
   @Path("/forgotpassword/{token}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Boolean> validateForgotPasswordToken(@PathParam("token") String tokenStr, @QueryParam("username") String usernameStr) throws FusionRestException {
      if (StringUtil.isBlank(usernameStr)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Please provide your username.");
      } else {
         String memcacheToken = MemCachedClientWrapper.getString(MemCachedKeySpaces.TokenKeySpace.FORGOT_PASSWORD, usernameStr);
         if (!StringUtil.isBlank(memcacheToken) && memcacheToken.equals(tokenStr)) {
            return new DataHolder(true);
         } else {
            log.warn("Invalid token entered: token [" + tokenStr + "] username [" + usernameStr + "].");
            throw new FusionRestException(FusionRestException.RestException.INVALID_TOKEN);
         }
      }
   }

   @GET
   @Path("/status/forgotpassword")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Map<Enums.ForgotPasswordEnum, Boolean>> validateForgotPasswordToken(@QueryParam("username") String usernameStr) throws FusionRestException {
      if (StringUtil.isBlank(usernameStr)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Please provide your username.");
      } else {
         Map<Enums.ForgotPasswordEnum, Boolean> result = new HashMap();
         Enums.ForgotPasswordEnum[] arr$ = Enums.ForgotPasswordEnum.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Enums.ForgotPasswordEnum type = arr$[i$];
            boolean check = true;

            try {
               check = MemCachedRateLimiter.checkWithoutHit(MemCachedRateLimiter.NameSpace.FORGOT_PASSWORD_REQUEST.toString(), StringUtil.join((Object[])(new String[]{usernameStr, type.name()}), "/"), Enums.ForgotPasswordEnum.getRatelimitPattern(type));
            } catch (Exception var10) {
               log.warn("Faield to check ratelimit for key pattern: [" + StringUtil.join((Object[])(new String[]{usernameStr, type.name()}), "/") + "], due to:" + var10 + ". ignored and set status to false.", var10);
               check = false;
            }

            if (check && type == Enums.ForgotPasswordEnum.VIA_SMS) {
               try {
                  UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  check = userEJB.allowForgotPasswordViaSMS(usernameStr);
               } catch (Exception var9) {
                  log.warn("Failed to check forgot password status for sms, due to:" + var9 + ", ignored and set status to false", var9);
                  check = false;
               }
            }

            result.put(type, check);
         }

         return new DataHolder(result);
      }
   }

   @POST
   @Path("/forgotpassword")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<String> forgotPassword(DataHolder<ForgotPasswordData> postRequest) throws FusionRestException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED)) {
         throw new FusionRestException(FusionRestException.RestException.DISABLED_FEATURE);
      } else {
         ForgotPasswordData postRequestData = (ForgotPasswordData)postRequest.data;
         Enums.ForgotPasswordEnum type = null;
         if (postRequestData.type == null) {
            if (!StringUtil.isBlank(postRequestData.emailAddress)) {
               type = Enums.ForgotPasswordEnum.VIA_EMAIL;
            }
         } else {
            type = Enums.ForgotPasswordEnum.fromValue(postRequestData.type);
         }

         if (type == null) {
            throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA, "You need to specify forgot password type");
         } else if (!Enums.ForgotPasswordEnum.isForgotPasswordTypeEnabled(type)) {
            throw new FusionRestException(FusionRestException.RestException.DISABLED_FEATURE);
         } else {
            this.checkRetrievePassworRatelimit(type, postRequestData);
            String token = null;

            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               if (type == Enums.ForgotPasswordEnum.VIA_EMAIL) {
                  token = userEJB.forgotPasswordViaEmail(postRequestData.username, postRequestData.emailAddress);
               } else if (type == Enums.ForgotPasswordEnum.VIA_SECURITY_QUESTION) {
                  token = userEJB.forgotPasswordViaSecurityQuestion(postRequestData.username, postRequestData.securityAnswer, postRequestData.securityQuestion);
               } else if (type == Enums.ForgotPasswordEnum.VIA_SMS) {
                  token = userEJB.forgotPasswordViaSMS(postRequestData.username, new AccountEntrySourceData(postRequestData.ipAddress, (String)null, postRequestData.mobileDevice, postRequestData.userAgent));
               }
            } catch (CreateException var6) {
               log.error(String.format("Forgot password request failed for request data:%s", postRequestData), var6);
               throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Unable to process your request at the moment. Please try again later.");
            } catch (FusionEJBException var7) {
               log.error(String.format("Forgot password request failed for request data:%s", postRequestData), var7);
               throw new FusionRestException(FusionRestException.RestException.ERROR, var7.getMessage());
            } catch (Exception var8) {
               log.error(String.format("Forgot password request failed for request data:%s", postRequestData), var8);
               throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Unable to process your request at the moment. Please try again later.");
            }

            return new DataHolder(token);
         }
      }
   }

   private void checkRetrievePassworRatelimit(Enums.ForgotPasswordEnum type, ForgotPasswordData postRequestData) throws FusionRestException {
      String forgotPasswordRateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ATTEMPT_RATE_LIMIT);

      try {
         MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.FORGOT_PASSWORD_REQUEST.toString(), postRequestData.username, forgotPasswordRateLimit);
      } catch (MemCachedRateLimiter.LimitExceeded var9) {
         log.warn(String.format("Rate limit exceeded for forgot password, request data:%s, ratelimit:%s", postRequestData, forgotPasswordRateLimit));
         throw new FusionRestException(FusionRestException.RestException.FORGOT_PASSWORD_OVERALL_RATE_LIMIT, FusionRestException.RestException.FORGOT_PASSWORD_OVERALL_RATE_LIMIT.getMessage());
      } catch (MemCachedRateLimiter.FormatError var10) {
         log.warn(String.format("Unable to trigger forgot password request due to invalid rate limit format error:, request data:%s, ratelimit:%s", postRequestData, forgotPasswordRateLimit));
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Sorry, we are unable to process your request at the moment. please try again later.");
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_RATELIMIT_PER_IP) && !StringUtils.isBlank(postRequestData.ipAddress)) {
         forgotPasswordRateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ATTEMPT_RATE_LIMIT_PER_IP);

         try {
            MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.FORGOT_PASSWORD_REQUEST.toString(), postRequestData.ipAddress, forgotPasswordRateLimit);
         } catch (MemCachedRateLimiter.LimitExceeded var7) {
            log.warn(String.format("Rate limit exceeded for forgot password, request data:%s, ratelimit:%s", postRequestData, forgotPasswordRateLimit));
            throw new FusionRestException(FusionRestException.RestException.FORGOT_PASSWORD_OVERALL_RATE_LIMIT, FusionRestException.RestException.FORGOT_PASSWORD_OVERALL_RATE_LIMIT.getMessage());
         } catch (MemCachedRateLimiter.FormatError var8) {
            log.warn(String.format("Unable to trigger forgot password request due to invalid rate limit format error:, request data:%s, ratelimit:%s", postRequestData, forgotPasswordRateLimit));
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Sorry, we are unable to process your request at the moment. please try again later.");
         }
      }

      forgotPasswordRateLimit = Enums.ForgotPasswordEnum.getRatelimitPattern(type);
      if (!StringUtil.isBlank(forgotPasswordRateLimit)) {
         try {
            MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.FORGOT_PASSWORD_REQUEST.toString(), StringUtil.join((Object[])(new String[]{postRequestData.username, type.name()}), "/"), forgotPasswordRateLimit);
         } catch (MemCachedRateLimiter.LimitExceeded var5) {
            log.warn(String.format("Rate limit exceeded for forgot password, request data:%s, ratelimit:%s", postRequestData, forgotPasswordRateLimit));
            throw new FusionRestException(Enums.ForgotPasswordEnum.getRatelimitRestException(type), FusionRestException.RestException.FORGOT_PASSWORD_OVERALL_RATE_LIMIT.getMessage());
         } catch (MemCachedRateLimiter.FormatError var6) {
            log.warn(String.format("Unable to trigger forgot password request due to invalid rate limit format error:, request data:%s, ratelimit:%s", postRequestData, forgotPasswordRateLimit));
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Sorry, we are unable to process your request at the moment. please try again later.");
         }
      }

   }

   @POST
   @Path("forgotpassword/{token}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Boolean> changePassword(@PathParam("token") String tokenStr, DataHolder<ChangePasswordData> postRequest) throws FusionRestException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED)) {
         throw new FusionRestException(FusionRestException.RestException.DISABLED_FEATURE);
      } else {
         ChangePasswordData postRequestData = (ChangePasswordData)postRequest.data;
         String responseRateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.RESPONSE_RATE_LIMIT);

         try {
            MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.FORGOT_PASSWORD_RESPONSE.toString(), StringUtil.join((Object[])(new String[]{postRequestData.username, postRequestData.ipAddress}), "/"), responseRateLimit);
         } catch (MemCachedRateLimiter.LimitExceeded var9) {
            log.warn("Rate limit exceeded for forgot password response :: user [" + postRequestData.username + "] token [" + tokenStr + "] ipAddress [" + postRequestData.ipAddress + "]: " + responseRateLimit);
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.THROW_CAPTCHA_ON_CHANGE_PASSWORD_RATE_LIMIT_HIT)) {
               throw new FusionRestException(FusionRestException.RestException.FORGOT_PASSWORD_VIA_EMAIL_RATE_LIMIT);
            }
         } catch (MemCachedRateLimiter.FormatError var10) {
            log.warn("Unable to trigger forgot password transaction due to invalid rate limit format error: user [" + postRequestData.username + "] token [" + tokenStr + "] ipAddress [" + postRequestData.ipAddress + "]: " + responseRateLimit);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Sorry, we are unable to process your request at the moment. please try again later.");
         }

         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            userEJB.changePassword(tokenStr, postRequestData.username, postRequestData.newPassword, postRequestData.ipAddress);
         } catch (CreateException var6) {
            log.error("Changing of password failed [" + postRequestData.username + "] ip [" + postRequestData.ipAddress + "]", var6);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Failed to change your password. Please try again later.");
         } catch (FusionEJBException var7) {
            log.error("Changing of password failed [" + postRequestData.username + "] ip [" + postRequestData.ipAddress + "]", var7);
            throw new FusionRestException(FusionRestException.RestException.ERROR, var7.getMessage());
         } catch (Exception var8) {
            log.error("Changing of password failed [" + postRequestData.username + "] ip [" + postRequestData.ipAddress + "]", var8);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Failed to change your password. Please try again later.");
         }

         return new DataHolder(true);
      }
   }

   @POST
   @Path("/forgotusername")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Boolean> forgotUsernameViaEmailAddress(DataHolder<ForgotUsernameData> postData) throws FusionRestException {
      ForgotUsernameData requestData = (ForgotUsernameData)postData.data;
      String forgotUsernameRateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotUsername.REQUEST_RATE_LIMIT);

      try {
         MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.FORGOT_USERNAME_REQUEST.toString(), StringUtil.join((Object[])(new String[]{requestData.emailAddress, requestData.ipAddress}), "/"), forgotUsernameRateLimit);
      } catch (MemCachedRateLimiter.LimitExceeded var7) {
         log.warn("Rate limit exceeded for forgot username :: email address [" + requestData.emailAddress + "]: ipAddress [" + requestData.ipAddress + "] : " + forgotUsernameRateLimit);
         throw new FusionRestException(FusionRestException.RestException.FORGOT_USERNAME_VIA_EMAIL_RATE_LIMIT);
      } catch (MemCachedRateLimiter.FormatError var8) {
         log.warn("Unable to trigger forgot password request due to invalid rate limit format error: ipAddress [" + requestData.ipAddress + "] email address [" + requestData.emailAddress + "]: " + forgotUsernameRateLimit);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Sorry, we are unable to process your request at the moment. please try again later.");
      }

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userEJB.forgotUsernameViaEmailAddress(requestData.emailAddress);
      } catch (CreateException var5) {
         log.error("Unable to process forgot username request for emailaddress [" + requestData.emailAddress + "] ipAddress [" + requestData.ipAddress + "]", var5);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Sorry, we are unable to process you request at the moment. Please try again later");
      } catch (FusionEJBException var6) {
         log.error("Unable to process forgot username request for emailaddress [" + requestData.emailAddress + "] ipAddress [" + requestData.ipAddress + "]", var6);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var6.getMessage());
      }

      return new DataHolder(true);
   }

   @POST
   @Path("/{userid}/usersettings")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Boolean> updateUserSettings(@PathParam("userid") String useridStr, DataHolder<UserSettingRestData> dataHolder) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         throw new FusionRestException(102, "Incorrect user id specified:" + useridStr);
      } else {
         UserSettingRestData data = (UserSettingRestData)dataHolder.data;
         UserSettingData.TypeEnum type = UserSettingData.TypeEnum.fromValue(data.type);
         if (type == null) {
            throw new FusionRestException(102, "Invalid type specified:" + data.type);
         } else {
            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               String username = userEJB.getUsernameByUserid(userid, (Connection)null);
               userEJB.updateUserSetting(username, type, data.value);
            } catch (CreateException var8) {
               log.error(String.format("Failed to update user setting data for user:%s with data:%s, due to:%s", useridStr, data, var8), var8);
               throw new FusionRestException(FusionRestException.RestException.ERROR, var8.getMessage());
            } catch (Exception var9) {
               log.error(String.format("Failed to update user setting data for user:%s with data:%s, due to:%s", useridStr, data, var9), var9);
               throw new FusionRestException(FusionRestException.RestException.ERROR, var9.getMessage());
            }

            return new DataHolder(true);
         }
      }
   }
}
