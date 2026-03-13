package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserProfileLabelsData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.interfaces.ContactLocal;
import com.projectgoth.fusion.interfaces.ContactLocalHome;
import com.projectgoth.fusion.interfaces.ContentLocal;
import com.projectgoth.fusion.interfaces.ContentLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.restapi.resource.UserResource;
import com.projectgoth.fusion.restapi.util.RedisDataUtil;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.log4j.Logger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "user"
)
public class UserMigboProfileData {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserMigboProfileData.class));
   public int id;
   public String username;
   public String dateRegistered;
   public String country;
   public String aboutMe;
   public String alias;
   public String firstName;
   public String lastName;
   public String dateOfBirth;
   public String gender;
   public int migLevel;
   public String migBotImage;
   public int numEmoticonPacks;
   public String statusMessage;
   public String state;
   public String city;
   public Integer relationship;
   public String companies;
   public String schools;
   public String interests;
   public String hometown;
   public String externalEmail;
   public boolean externalEmailVerified;
   public int displayPictureType;
   public UserProfileLabelsData labels;
   public String aboutMeVerified;
   public int accountType;
   public int accountVerified;
   public SettingsEnums.EveryoneOrFollowerAndFriend postPrivacy;
   public SettingsEnums.EveryoneFollowerFriend chatPrivacy;
   public SettingsEnums.Birthday dateOfBirthPrivacy;
   public SettingsEnums.ShowHide firstLastNamePrivacy;
   public SettingsEnums.EveryoneFollowerFriendHide externalEmailPrivacy;
   public SettingsEnums.EveryoneFriendHide mobilePhonePrivacy;
   public UserUserRelationshipData userRelationship;
   public String mobilePhone;
   public boolean mobileVerified;
   public String avatarHeadUuid;
   public String avatarBodyUuid;
   public int numOfGroupsJoined;
   public int numOfGiftsReceived;
   public int numOfMigPhotosUploaded;
   public int numOfChatroomsOwned;
   public int numOfAvatarVotesReceived;
   public int numOfAvatarCommentsReceived;
   public int numOfFootprintsReceived;
   public int numOfUserLikesReceived;
   public int numOfGamesPlayed;
   public int emailAllSetting = UserSettingData.EmailSettingEnum.defaultValue().value();
   public int emailMentionSetting = UserSettingData.EmailSettingEnum.defaultValue().value();
   public int emailReplyToPostSetting = UserSettingData.EmailSettingEnum.defaultValue().value();
   public int emailReceiveGiftSetting = UserSettingData.EmailSettingEnum.defaultValue().value();
   public int emailNewFollowerSetting = UserSettingData.EmailSettingEnum.defaultValue().value();

   public UserMigboProfileData() {
   }

   public static UserMigboProfileData createUserMigboProfileData(int userId, UserLocal userEJB, String relatedUseridStr) throws FusionRestException, CreateException {
      UserData userData = userEJB.loadUserFromID(userId);
      return loadFromEJB(userId, userId + "", userData, userEJB, relatedUseridStr);
   }

   public static UserMigboProfileData createUserMigboProfileData(String username, UserLocal userEJB, String relatedUseridStr) throws FusionRestException, CreateException {
      UserData userData = userEJB.loadUserByUsernameOrAlias(username, false, false);
      return loadFromEJB(-1, username, userData, userEJB, relatedUseridStr);
   }

   private static UserMigboProfileData loadFromEJB(int userid, String useridOrUsername, UserData userData, UserLocal userEJB, String relatedUseridStr) throws FusionRestException, CreateException {
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

         UserProfileData upData = null;

         try {
            upData = userEJB.getUserProfile((String)null, userData.username, false);
         } catch (EJBException var23) {
            log.error(String.format("Failed to retrieve user profile '%s'/'%s' due to EJBException in UserBean.getUserProfile: %s", useridOrUsername, userData.username, var23.getMessage()), var23);
            throw new FusionRestException(101, "Internal error while retrieving user data");
         } catch (FusionEJBException var24) {
            log.error(String.format("Failed to retrieve user profile '%s'/'%s' due to EJBFusionException in UserBean.getUserProfile: %s", useridOrUsername, userData.username, var24.getMessage()), var24);
            throw new FusionRestException(101, "Internal error while retrieving user data");
         }

         if (upData == null) {
            log.warn(String.format("Failed to retrieve user profile '%s'/'%s': user profile does not exist", useridOrUsername, userData.username));
            upData = new UserProfileData(userData);
         }

         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misBean.getCountry(userData.countryID);
         Integer migLevel = null;

         try {
            migLevel = MemCacheOrEJB.getUserReputationLevel(userData.userID);
         } catch (Exception var22) {
            String err = "Exception retrieving mig level for user=" + userData.username + " e=" + var22;
            log.error(err, var22);
            throw new FusionRestException(-1, err);
         }

         UserMigboProfileData.AdminEnum adminType = UserMigboProfileData.AdminEnum.NOT_ADMIN;

         try {
            adminType = userEJB.getAdminLabel(userData.username);
         } catch (FusionEJBException var21) {
            throw new FusionRestException(101, "Internal error while retrieving user data");
         }

         UserMigboProfileData.MerchantEnum merchantType = UserMigboProfileData.MerchantEnum.NOT_MERCHANT;

         try {
            if (userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
               if (userEJB.isMerchantMentor(userData.username)) {
                  merchantType = UserMigboProfileData.MerchantEnum.MERCHANT_MENTOR;
               } else {
                  merchantType = UserMigboProfileData.MerchantEnum.MERCHANT_SUBMENTOR;
               }
            }
         } catch (FusionEJBException var25) {
            throw new FusionRestException(101, "Internal error while retrieving user data");
         }

         String migBotImage = null;

         try {
            ReputationLevelData reputationLevelData = MemCacheOrEJB.getUserReputationLevelData(userData, userEJB);
            migBotImage = reputationLevelData.image;
         } catch (Exception var20) {
            log.error("Failed to retrieve reputation level data: " + var20);
         }

         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         int numEmoticonPacks = contentBean.getEmoticonPackCountForUser(userData.username);
         UserMigboProfileData profile = new UserMigboProfileData(userData, upData, countryData, migLevel, adminType, merchantType, RedisDataUtil.getUserDisplayPictureSetting(userid), UserResource.getAllPrivacy(userid), migBotImage, numEmoticonPacks);
         int relatedUserid = StringUtil.toIntOrDefault(relatedUseridStr, -1);
         if (relatedUserid > 0) {
            profile.userRelationship = new UserUserRelationshipData();
            if (relatedUserid == userid) {
               profile.userRelationship.isSelf = true;
            } else {
               try {
                  ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);

                  try {
                     profile.userRelationship.isFriend = contactEJB.isFriend(userid, relatedUserid);
                  } catch (FusionEJBException var18) {
                     log.error(String.format("Failed to retrieve user relationship '%d' '%s': %s", userid, relatedUseridStr, var18.getMessage()));
                     throw new FusionRestException(101, "Internal error while retrieving user relationship data");
                  }
               } catch (CreateException var19) {
                  log.error(String.format("Failed to retrieve user relationship '%d' '%s' due to EJB CreateException: %s", userid, relatedUseridStr, var19.getMessage()), var19);
                  throw new FusionRestException(101, "Internal error while retrieving user relationship data");
               }
            }
         }

         if (SystemProperty.getBool("avatarProfileCountersEnabled", false)) {
            profile.numOfChatroomsOwned = userEJB.getChatroomsOwnedCount(userid);
            profile.numOfGiftsReceived = userEJB.getGiftsReceivedCount(userid);
            profile.numOfGroupsJoined = userEJB.getGroupsJoinedCount(userid);
            profile.numOfMigPhotosUploaded = userEJB.getPhotosUploadedCount(userid);
            Map<RedisDataUtil.ProfileCounterType, Integer> counters = RedisDataUtil.getUserProfileCounters(userid);
            profile.numOfAvatarVotesReceived = (Integer)counters.get(RedisDataUtil.ProfileCounterType.AVATAR_VOTES);
            profile.numOfAvatarCommentsReceived = (Integer)counters.get(RedisDataUtil.ProfileCounterType.AVATAR_COMMENTS);
            profile.numOfFootprintsReceived = (Integer)counters.get(RedisDataUtil.ProfileCounterType.FOOTPRINTS);
            profile.numOfUserLikesReceived = (Integer)counters.get(RedisDataUtil.ProfileCounterType.USERLIKES);
            profile.numOfGamesPlayed = (Integer)counters.get(RedisDataUtil.ProfileCounterType.THIRD_PARTY_GAMES_PLAYED) + (Integer)counters.get(RedisDataUtil.ProfileCounterType.CHATROOM_GAMES_PLAYED);
         }

         return profile;
      }
   }

   public UserMigboProfileData(UserData userData, UserProfileData upData, CountryData countryData, int migLevel, UserMigboProfileData.AdminEnum adminType, UserMigboProfileData.MerchantEnum merchantType, int displayPictureSetting, Map<String, Integer> privacySettings, String migBotImage, int numEmoticonPacks) {
      this.id = userData.userID;
      this.username = userData.username;
      this.dateRegistered = DateTimeUtils.getStringForMigbo(userData.dateRegistered);
      this.alias = userData.alias;
      this.firstName = upData.firstName;
      this.lastName = upData.lastName;
      this.dateOfBirth = DateTimeUtils.getStringForMigbo(upData.dateOfBirth);
      this.gender = upData.gender == null ? null : upData.gender.value();
      this.aboutMe = upData.aboutMe;
      this.migLevel = migLevel;
      this.migBotImage = migBotImage;
      this.numEmoticonPacks = numEmoticonPacks;
      this.statusMessage = userData.statusMessage;
      this.relationship = upData.relationshipStatus == null ? null : upData.relationshipStatus.value();
      this.country = countryData.name;
      this.state = upData.state;
      this.city = upData.city;
      this.companies = upData.jobs;
      this.schools = upData.schools;
      this.interests = upData.hobbies;
      this.hometown = upData.homeTown;
      this.externalEmail = userData.emailAddress;
      this.externalEmailVerified = userData.emailVerified != null && userData.emailVerified;
      this.mobilePhone = userData.mobilePhone;
      this.mobileVerified = userData.mobileVerified;
      this.aboutMeVerified = userData.aboutMeVerified;
      this.accountVerified = userData.accountVerified.value();
      this.accountType = userData.accountType.value();
      this.avatarHeadUuid = userData.avatar;
      this.avatarBodyUuid = userData.fullbodyAvatar;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.WW519_EMAIL_NOTIFICATION_USER_SETTINGS_ENABLED)) {
         this.emailAllSetting = userData.emailAllSetting.value();
      }

      this.emailMentionSetting = userData.emailMentionSetting.value();
      this.emailNewFollowerSetting = userData.emailNewFollowerSetting.value();
      this.emailReceiveGiftSetting = userData.emailReceiveGiftSetting.value();
      this.emailReplyToPostSetting = userData.emailReplyToPostSetting.value();
      SettingsEnums.DisplayPictureChoice displayPictureEnum = SettingsEnums.DisplayPictureChoice.fromValue(displayPictureSetting);
      if (displayPictureEnum == null) {
         displayPictureEnum = SettingsAccountPictureData.DEFAULT_DISPLAY_PICTURE;
      }

      this.displayPictureType = displayPictureEnum.value();
      this.labels = new UserProfileLabelsData();
      this.labels.isVerified = userData.accountVerified == UserData.AccountVerifiedEnum.VERIFIED_OK;
      this.labels.admin = adminType.value();
      this.labels.merchant = merchantType.value();
      this.postPrivacy = privacySettings.containsKey("FeedPv") ? SettingsEnums.EveryoneOrFollowerAndFriend.fromValue((Integer)privacySettings.get("FeedPv")) : SettingsAccountCommunicationData.PRIVACY_DEFAULT_FEED;
      this.chatPrivacy = privacySettings.containsKey("ChatPv") ? SettingsEnums.EveryoneFollowerFriend.fromValue((Integer)privacySettings.get("ChatPv")) : SettingsAccountCommunicationData.PRIVACY_DEFAULT_CHAT;
      this.dateOfBirthPrivacy = privacySettings.containsKey("DobPrivacy") ? SettingsEnums.Birthday.fromValue((Integer)privacySettings.get("DobPrivacy")) : SettingsProfileDetailsData.PRIVACY_DEFAULT_BIRTHDAY;
      this.firstLastNamePrivacy = privacySettings.containsKey("FLNamePv") ? SettingsEnums.ShowHide.fromValue((Integer)privacySettings.get("FLNamePv")) : SettingsProfileDetailsData.PRIVACY_DEFAULT_FIRSTLASTNAME;
      this.externalEmailPrivacy = privacySettings.containsKey("ExtEmPv") ? SettingsEnums.EveryoneFollowerFriendHide.fromValue((Integer)privacySettings.get("ExtEmPv")) : SettingsProfileDetailsData.PRIVACY_DEFAULT_EXTERNALEMAIL;
      this.mobilePhonePrivacy = privacySettings.containsKey("MobNumPrivacy") ? SettingsEnums.EveryoneFriendHide.fromValue((Integer)privacySettings.get("MobNumPrivacy")) : SettingsProfileDetailsData.PRIVACY_DEFAULT_MOBILEPHONE;
   }

   public static enum MerchantEnum {
      NOT_MERCHANT("N"),
      MERCHANT_SUBMENTOR("M"),
      MERCHANT_MENTOR("MT");

      private String value;

      private MerchantEnum(String value) {
         this.value = value;
      }

      public String value() {
         return this.value;
      }

      public static UserMigboProfileData.MerchantEnum fromValue(String value) {
         UserMigboProfileData.MerchantEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserMigboProfileData.MerchantEnum e = arr$[i$];
            if (value.equals(e.value())) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum AdminEnum {
      NOT_ADMIN("N"),
      GLOBAL_ADMIN("A"),
      GROUP_ADMIN("GA"),
      CHATROOM_ADMIN("CA");

      private String value;

      private AdminEnum(String value) {
         this.value = value;
      }

      public String value() {
         return this.value;
      }

      public static UserMigboProfileData.AdminEnum fromValue(String value) {
         UserMigboProfileData.AdminEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserMigboProfileData.AdminEnum e = arr$[i$];
            if (value.equals(e.value())) {
               return e;
            }
         }

         return null;
      }
   }
}
