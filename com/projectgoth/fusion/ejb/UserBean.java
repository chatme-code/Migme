package com.projectgoth.fusion.ejb;

import Ice.LocalException;
import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlParameter;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.cache.BroadcastList;
import com.projectgoth.fusion.cache.BroadcastListPersisted;
import com.projectgoth.fusion.cache.GiftsReceivedCounter;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.cache.SurgeMail;
import com.projectgoth.fusion.cache.UserReferrerCache;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.EmailUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.HashUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedHelper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.common.NotificationUtils;
import com.projectgoth.fusion.common.PasswordUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.ThirdPartyAppHelper;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.common.UsernameUtils;
import com.projectgoth.fusion.common.UsernameValidationException;
import com.projectgoth.fusion.common.WebCommon;
import com.projectgoth.fusion.common.jdbc.ConnectionCreator;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.CampaignData;
import com.projectgoth.fusion.data.CampaignParticipantData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.DisplayPictureAndStatusMessage;
import com.projectgoth.fusion.data.FullMerchantTagDetailsData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.data.RegistrationContextData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.SecurityQuestion;
import com.projectgoth.fusion.data.ServiceData;
import com.projectgoth.fusion.data.SubscriptionData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.ThirdPartyApplicationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserDataAndRegistrationContextData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserProfileLabelsData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserReputationScoreData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.data.ValidateCredentialResult;
import com.projectgoth.fusion.data.VoucherData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.eventqueue.Event;
import com.projectgoth.fusion.eventqueue.EventQueue;
import com.projectgoth.fusion.eventqueue.events.StatusUpdateEvent;
import com.projectgoth.fusion.eventqueue.events.ThirdPartySiteCredentialUpdatedEvent;
import com.projectgoth.fusion.eventqueue.events.UserDataUpdatedEvent;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.ContactLocal;
import com.projectgoth.fusion.interfaces.ContactLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.VoucherLocal;
import com.projectgoth.fusion.interfaces.VoucherLocalHome;
import com.projectgoth.fusion.invitation.CreateInvitationsResult;
import com.projectgoth.fusion.invitation.InvitationData;
import com.projectgoth.fusion.invitation.InvitationMetadata;
import com.projectgoth.fusion.invitation.InvitationResponseData;
import com.projectgoth.fusion.invitation.InvitationStatusEnum;
import com.projectgoth.fusion.invitation.InvitationUtils;
import com.projectgoth.fusion.invitation.restapi.data.InvitationDetailsData;
import com.projectgoth.fusion.invitation.restapi.data.SendingInvitationData;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.reputation.ReputationLevelScoreRanges;
import com.projectgoth.fusion.restapi.data.RegistrationTokenData;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.data.UserActivationData;
import com.projectgoth.fusion.restapi.data.UserMigboProfileData;
import com.projectgoth.fusion.restapi.data.UserVerificationData;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.ConsecutiveLoginTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ExternalEmailVerifiedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.InvitationRespondedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.LastLoginTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserFirstAuthenticatedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserReferralActivationTrigger;
import com.projectgoth.fusion.slice.AuthenticationServiceCredentialResponse;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.slice.FusionBusinessException;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.smsengine.SMSControl;
import com.projectgoth.fusion.uns.UserNotificationServiceI;
import com.projectgoth.leto.common.event.CampaignParticipation;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisException;

public class UserBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserBean.class));
   private static final Integer UNKNOWN_EMAIL_ADDRESS = -1;
   private static final Integer HALFWAY_REGISTERED_EMAIL_ADDRESS = 0;
   private static MemCachedClient broadcastListMemcache;
   private static MemCachedClient bclPersistedMemcache;
   private static MemCachedClient displayPictureAndStatusMessageMemcache;
   private static MemCachedClient surgeMailMemcache;
   private static final DateFormat dateFormate;
   private static final int INITIAL_SCORE = 0;
   private static final int INITIAL_LEVEL = 1;
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private DataSource userRegistrationMaster;
   private DataSource userRegistrationSlave;
   private SecureRandom randomGen;
   private SessionContext context;
   private static final Pattern USERALIAS_PATTERN;

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         this.userRegistrationMaster = LookupUtil.getRegistrationMasterDataSource();
         this.userRegistrationSlave = LookupUtil.getRegistrationSlaveDataSource();
         this.randomGen = new SecureRandom();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create User EJB", var2);
         throw new CreateException("Unable to create User EJB: " + var2.getMessage());
      }
   }

   private void checkUserAlias(String alias, boolean sameAsUsername) throws FusionEJBException {
      if (StringUtil.isBlank(alias)) {
         throw new FusionEJBException("Empty alias");
      } else if (alias.length() >= (sameAsUsername ? 3 : 6) && USERALIAS_PATTERN.matcher(alias).matches()) {
         int maxUserAliasLength = SystemProperty.getInt((String)"MaxUserAliasLength", 20);
         if (alias.length() > maxUserAliasLength) {
            throw new FusionEJBException("The alias must not contain more than " + maxUserAliasLength + " characters");
         } else {
            if (!sameAsUsername) {
               String[] bannedUsernames = SystemProperty.get("BannedUsernames", "").split(";");
               String[] arr$ = bannedUsernames;
               int len$ = bannedUsernames.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  String bannedUsername = arr$[i$];
                  if (alias.indexOf(bannedUsername) != -1) {
                     throw new FusionEJBException("You cannot use " + bannedUsername + " in the alias");
                  }
               }
            }

         }
      } else {
         throw new FusionEJBException("The alias must start with a letter, and contain at least 6 letters, numbers, periods (.), hyphens (-), or underscores (_)");
      }
   }

   public void checkUsername(String username) throws EJBException, NoSuchFieldException {
      try {
         String normalizedUsername = UsernameUtils.validateUsernameCharacters(username, false);
         UsernameUtils.validateAgainstBannedWords(normalizedUsername, SystemProperty.get("BannedUsernames").split(";"));
      } catch (UsernameValidationException var4) {
         throw new EJBExceptionWithErrorCause(var4.getErrorCause(), var4.getErrorMsgArgs());
      }
   }

   private void checkPassword(String username, String password) throws EJBException, NoSuchFieldException {
      ValidateCredentialResult result = PasswordUtils.validatePassword(username, password);
      if (null == result) {
         throw new EJBException("Unable to validate username/password");
      } else if (!result.valid) {
         throw new EJBException(result.reason);
      }
   }

   private String newVerificationCode() {
      return String.format("%1$05d", this.randomGen.nextInt(90000) + 10000);
   }

   private void addUserToDB(Connection conn, UserData userData, UserRegistrationContextData userRegContextData, AccountEntrySourceData accountEntrySourceData) throws SQLException {
      PreparedStatement ps = null;

      try {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.ALLOW_OVERRIDING_USER_REGISTRATION_DATE_FOR_UNIT_TESTING)) {
            userData.dateRegistered = userData.dateRegistered != null ? userData.dateRegistered : new Date();
         } else {
            userData.dateRegistered = new Date();
         }

         userData.statusMessage = null;
         userData.statusTimeStamp = userData.dateRegistered;
         userData.chatRoomAdmin = false;
         userData.chatRoomBans = 0;
         userData.firstLoginDate = null;
         userData.lastLoginDate = null;
         userData.failedLoginAttempts = 0;
         userData.failedActivationAttempts = 0;
         userData.mobileVerified = false;
         userData.verificationCode = this.newVerificationCode();
         userData.emailActivated = false;
         userData.emailAlert = false;
         userData.emailAlertSent = false;
         userData.allowBuzz = true;
         userData.emailActivationDate = null;
         userData.bonusProgramID = null;
         userData.balance = 0.0D;
         userData.fundedBalance = 0.0D;
         userData.status = UserData.StatusEnum.ACTIVE;
         String statement = "insert into user ";
         statement = statement + "(username, dateregistered, password, displayname, displaypicture, statusmessage, statustimestamp, countryid, language, emailaddress, onmailinglist, chatroomadmin, chatroombans, registrationipaddress, registrationdevice, firstlogindate, lastlogindate, failedloginattempts, failedactivationattempts, mobilephone, mobiledevice, useragent, mobileverified, verificationcode, emailActivated, emailAlert, emailAlertSent, emailActivationDate, allowBuzz, utcoffset, type, affiliateid, merchantCreated, referredby, bonusprogramid, currency, balance, notes, status) ";
         statement = statement + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
         ps = conn.prepareStatement(statement);
         ps.setString(1, userData.username);
         ps.setTimestamp(2, new Timestamp(userData.dateRegistered.getTime()));
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT73368964_ENABLED)) {
            ps.setString(3, userData.password);
         } else {
            ps.setString(3, "");
         }

         ps.setString(4, userData.displayName);
         ps.setString(5, userData.displayPicture);
         ps.setString(6, userData.statusMessage);
         ps.setTimestamp(7, new Timestamp(userData.statusTimeStamp.getTime()));
         ps.setObject(8, userData.countryID);
         ps.setString(9, userData.language);
         ps.setString(10, userData.emailAddress);
         ps.setObject(11, userData.onMailingList == null ? null : userData.onMailingList ? 1 : 0);
         ps.setObject(12, userData.chatRoomAdmin == null ? null : userData.chatRoomAdmin ? 1 : 0);
         ps.setObject(13, userData.chatRoomBans);
         ps.setString(14, userData.registrationIPAddress);
         ps.setString(15, userData.registrationDevice);
         ps.setTimestamp(16, userData.firstLoginDate == null ? null : new Timestamp(userData.firstLoginDate.getTime()));
         ps.setTimestamp(17, userData.lastLoginDate == null ? null : new Timestamp(userData.lastLoginDate.getTime()));
         ps.setObject(18, userData.failedLoginAttempts);
         ps.setObject(19, userData.failedActivationAttempts);
         ps.setString(20, userData.mobilePhone);
         ps.setString(21, DataUtils.truncateMobileDevice(userData.mobileDevice, true, String.format("in create new user '%s'", userData.username)));
         ps.setString(22, DataUtils.truncateUserAgent(userData.userAgent, true, String.format("in create new user '%s'", userData.username)));
         ps.setObject(23, userData.mobileVerified == null ? null : userData.mobileVerified ? 1 : 0);
         ps.setString(24, userData.verificationCode);
         ps.setObject(25, userData.emailActivated == null ? null : userData.emailActivated ? 1 : 0);
         ps.setObject(26, userData.emailAlert == null ? null : userData.emailAlert ? 1 : 0);
         ps.setObject(27, userData.emailAlertSent == null ? null : userData.emailAlertSent ? 1 : 0);
         ps.setTimestamp(28, userData.emailActivationDate == null ? null : new Timestamp(userData.emailActivationDate.getTime()));
         ps.setObject(29, userData.allowBuzz == null ? null : userData.allowBuzz ? 1 : 0);
         ps.setObject(30, userData.UTCOffset);
         ps.setObject(31, userData.type == null ? null : userData.type.value());
         ps.setObject(32, userData.affiliateID);
         ps.setString(33, userData.merchantCreated);
         ps.setString(34, userData.referredBy);
         ps.setObject(35, userData.bonusProgramID);
         ps.setString(36, userData.currency);
         ps.setObject(37, userData.balance);
         ps.setString(38, userData.notes);
         ps.setObject(39, userData.status == null ? null : userData.status.value());
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to add a new user '" + userData.username + "' to database");
         }

         ps.close();
         ps = conn.prepareStatement("insert into userid (username) values (?)", 1);
         ps.setString(1, userData.username);
         ps.executeUpdate();
         ResultSet rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new SQLException("Unable to create user account");
         }

         userData.userID = rs.getInt(1);
         rs.close();
         ps.close();
         ps = conn.prepareStatement("insert into registrationdevice (userid, mobiledevice, useragent, imei) values (?,?,?,?)");
         ps.setInt(1, userData.userID);
         ps.setString(2, accountEntrySourceData.mobileDevice);
         ps.setString(3, accountEntrySourceData.userAgent);
         ps.setString(4, accountEntrySourceData.imei);
         if (ps.executeUpdate() != 1) {
            throw new SQLException("Unable to record registration device");
         }

         ps.close();
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_CONTEXT_ENABLED)) {
            RegistrationContextData regContextData = new RegistrationContextData(userData, userRegContextData, accountEntrySourceData);
            Map<Integer, String> regContextValues = regContextData.toIntegerAndStringMap();
            if (!regContextValues.isEmpty()) {
               ps = conn.prepareStatement("insert into registrationcontext (userid, type, value) values (?,?,?)");
               Iterator i$ = regContextValues.entrySet().iterator();

               while(true) {
                  if (!i$.hasNext()) {
                     int[] batchResults = ps.executeBatch();
                     if (batchResults == null || batchResults.length != regContextValues.size()) {
                        throw new SQLException("Unable to record registration context");
                     }

                     int[] arr$ = batchResults;
                     int len$ = batchResults.length;

                     for(int i$ = 0; i$ < len$; ++i$) {
                        int batchResult = arr$[i$];
                        if (batchResult != 1) {
                           throw new SQLException("Unable to record registration context");
                        }
                     }

                     ps.close();
                     break;
                  }

                  Entry<Integer, String> entry = (Entry)i$.next();
                  ps.setInt(1, regContextData.userid);
                  ps.setInt(2, (Integer)entry.getKey());
                  ps.setString(3, (String)entry.getValue());
                  ps.addBatch();
               }
            }
         }

         if (!StringUtil.isBlank(userData.emailAddress)) {
            statement = "INSERT INTO useremailaddress(userid, emailaddress, type) VALUES (?,?,?)";
            if (userRegContextData.verified) {
               statement = "INSERT INTO useremailaddress(userid, emailaddress, type, verified, dateverified) VALUES (?,?,?, 1, NOW())";
            }

            ps = conn.prepareStatement(statement);
            ps.setInt(1, userData.userID);
            ps.setString(2, userData.emailAddress);
            ps.setInt(3, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
            if (ps.executeUpdate() != 1) {
               throw new SQLException("Unable to record email address");
            }

            userData.emailVerified = true;
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

      }

   }

   private int addUserToUserRegistrationTable(Connection conn, UserData userData, UserRegistrationContextData userRegContextData, AccountEntrySourceData accountEntrySourceData) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var10;
      try {
         userData.dateRegistered = new Date();
         userData.failedActivationAttempts = 0;
         userData.emailActivationDate = null;
         RegistrationContextData regContextData = new RegistrationContextData(userData, userRegContextData, accountEntrySourceData);
         JSONObject contextJson = null;

         try {
            contextJson = regContextData.toJSONObject();
         } catch (JSONException var22) {
         }

         String statement = "insert into userRegistration (username, password, countryid, language, emailaddress, registrationipaddress, registrationdevice,  useragent, type, referredby, currency, registrationType,mobiledevice,imei,context) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
         ps = conn.prepareStatement(statement, 1);
         ps.setString(1, userData.username);
         ps.setString(2, userData.password);
         ps.setObject(3, userData.countryID);
         ps.setString(4, userData.language);
         ps.setString(5, userData.emailAddress);
         ps.setString(6, userData.registrationIPAddress);
         ps.setString(7, userData.registrationDevice);
         ps.setString(8, DataUtils.truncateUserAgent(accountEntrySourceData.userAgent, true, String.format("in create new user '%s'", userData.username)));
         ps.setObject(9, userData.type == null ? null : userData.type.value());
         ps.setString(10, userData.referredBy);
         ps.setString(11, userData.currency);
         ps.setString(12, userRegContextData.registrationType.value());
         ps.setString(13, accountEntrySourceData.mobileDevice);
         ps.setString(14, accountEntrySourceData.imei);
         if (contextJson != null && contextJson.length() >= 1) {
            ps.setString(15, contextJson.toString());
         } else {
            ps.setNull(15, 12);
         }

         if (ps.executeUpdate() != 1) {
            log.error("Unable to add new user [" + userData.username + "] to the userregistration table");
            throw new SQLException("Unable to add new user. Please try again later");
         }

         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new SQLException("Failed to add a new user '" + userData.username + "' to userRegistration table");
         }

         var10 = rs.getInt(1);
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

      }

      return var10;
   }

   private void updateUserRegistrationWithToken(Connection conn, int id, String token) throws SQLException {
      PreparedStatement ps = null;

      try {
         String statement = "update userRegistration set verificationToken=? where id=?";
         ps = conn.prepareStatement(statement);
         ps.setString(1, token);
         ps.setInt(2, id);
         if (ps.executeUpdate() != 1) {
            log.error("Unable to updated userregistration table for id [" + id + "] with token [" + token + "]");
            throw new SQLException("Unable to create user. Please try again later");
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var12) {
            ps = null;
         }

      }

   }

   private boolean isEmailRegistrationV2Enabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_PATH1_ENABLED) || SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_PATH2_ENABLED);
   }

   public UserDataAndRegistrationContextData getUserDataFromUserRegistrationTable(String username, UserData oldUserData) throws EJBException {
      if (!this.isEmailRegistrationV2Enabled()) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         UserDataAndRegistrationContextData var9;
         try {
            conn = this.userRegistrationSlave.getConnection();
            ps = conn.prepareStatement("SELECT * FROM userregistration WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMax() + ",now()) AND username=?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new SQLException("Failed to find user in registration table: " + username);
            }

            UserData userData = new UserData(oldUserData.toIceObject());
            userData.username = rs.getString("username");
            userData.password = rs.getString("password");
            userData.countryID = rs.getInt("countryid");
            userData.language = rs.getString("language");
            userData.emailAddress = rs.getString("emailaddress");
            userData.registrationIPAddress = rs.getString("registrationipaddress");
            userData.registrationDevice = rs.getString("registrationdevice");
            userData.userAgent = rs.getString("useragent");
            userData.UTCOffset = rs.getDouble("utcoffset");
            userData.type = UserData.TypeEnum.fromValue(rs.getInt("type"));
            userData.referredBy = rs.getString("referredby");
            userData.referralLevel = rs.getInt("referrallevel");
            userData.currency = rs.getString("currency");
            userData.mobileDevice = rs.getString("mobiledevice");
            String contextJson = rs.getString("context");
            RegistrationContextData regContextData = null;

            try {
               regContextData = new RegistrationContextData(contextJson);
            } catch (JSONException var22) {
               log.error(String.format("Unable to parse json string of userregistration.context column for '%s', e: %s", contextJson, var22));
            }

            var9 = new UserDataAndRegistrationContextData(userData, regContextData);
         } catch (SQLException var23) {
            log.error("Unable to get userregistration data from table: " + var23.getMessage());
            throw new EJBException("Unable to get user registration data");
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var21) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var20) {
               conn = null;
            }

         }

         return var9;
      }
   }

   public UserActivationData getVerificationDataFromUserRegistrationTable(String username) throws EJBException {
      if (!this.isEmailRegistrationV2Enabled()) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         UserActivationData var6;
         try {
            conn = this.userRegistrationSlave.getConnection();
            ps = conn.prepareStatement("SELECT * FROM userregistration WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMax() + ",now()) AND username=?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new SQLException("Failed to find user in registration table: " + username);
            }

            UserActivationData userActivationData = new UserActivationData();
            userActivationData.emailAddress = rs.getString("emailaddress");
            userActivationData.token = rs.getString("verificationtoken");
            userActivationData.registrationType = RegistrationType.fromValue(rs.getString("registrationtype"));
            var6 = userActivationData;
         } catch (SQLException var21) {
            log.error("Unable to get userregistration data from table: " + var21.getMessage());
            throw new EJBException("Unable to get user registration data");
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var20) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var19) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var18) {
               conn = null;
            }

         }

         return var6;
      }
   }

   private void deleteUserFromRegistrationDB(Connection conn, int userRegistrationId) throws SQLException {
      PreparedStatement ps = null;

      try {
         if (userRegistrationId > 0) {
            ps = conn.prepareStatement("delete from userregistration where id = ?");
            ps.setInt(1, userRegistrationId);
            if (ps.executeUpdate() != 1) {
               log.info(String.format("Failed to delete userRegistrationId:%s from regdb.userregistration table, we might not have the record yet, check \"select * from userregistration where id = %s\" for more details, ingoring...", userRegistrationId, userRegistrationId));
            }
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var10) {
            ps = null;
         }

      }

   }

   private void deleteUserFromFusionDB(Connection conn, UserData userData) throws SQLException {
      PreparedStatement ps = null;

      try {
         if (userData.userID != null) {
            ps = conn.prepareStatement("delete ignore from useremailaddress where userid = ?");
            ps.setInt(1, userData.userID);
            ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement("delete from registrationdevice where userid = ?");
            ps.setInt(1, userData.userID);
            ps.executeUpdate();
            ps.close();
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_CONTEXT_ENABLED)) {
               ps = conn.prepareStatement("delete from registrationcontext where userid = ?");
               ps.setInt(1, userData.userID);
               ps.executeUpdate();
               ps.close();
            }

            ps = conn.prepareStatement("delete ignore from credential where userID = ?");
            ps.setInt(1, userData.userID);
            ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement("delete from userid where username = ?");
            ps.setString(1, userData.username);
            if (ps.executeUpdate() != 1) {
               log.info(String.format("Failed to delete user:%s from userid table, we might not have the record yet, check \"select * from userid where username = %s\" for more details, ingoring...", userData.username, userData.username));
            }

            ps.close();
         }

         ps = conn.prepareStatement("delete from userprofile where username = ?");
         ps.setString(1, userData.username);
         if (ps.executeUpdate() != 1) {
            log.info(String.format("Failed to delete user:%s from userprofile table, we might not have the record yet, check \"select * from userprofile where username = %s\" for more details, ingoring...", userData.username, userData.username));
         }

         ps.close();
         ps = conn.prepareStatement("delete from user where username = ?");
         ps.setString(1, userData.username);
         if (ps.executeUpdate() != 1) {
            log.info(String.format("Failed to delete user:%s from user table, we might not have the record yet, check \"select * from user where user = %s\" for more details, ingoring...", userData.username, userData.username));
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var10) {
            ps = null;
         }

      }

   }

   private int getMobilePhoneCount(Connection conn, String mobilePhone) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         ps = conn.prepareStatement("select count(*) from user where mobilephone=?");
         ps.setString(1, mobilePhone);
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.error("Unable to determine mobile phone count for " + mobilePhone);
            throw new SQLException("Unable to determine mobile phone count");
         }

         if (log.isDebugEnabled()) {
            log.debug("====== UserBean.GETMOBILEPHONECOUNT: Number: " + mobilePhone + ", MOBILE count: " + rs.getInt(1));
         }

         if (rs.getInt(1) <= 0) {
            var5 = this.getActivatedNumberCount(conn, mobilePhone);
            return var5;
         }

         var5 = rs.getInt(1);
      } catch (Exception var19) {
         throw new SQLException("Unable to determine mobile phone count");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var18) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
            ps = null;
         }

      }

      return var5;
   }

   private int getActivatedNumberCount(Connection conn, String mobilePhone) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         ps = conn.prepareStatement("select count(*) from activation where mobilephone=? and datecreated >=date_sub(curdate(), interval ? day)");
         ps.setString(1, mobilePhone);
         ps.setInt(2, SystemProperty.getInt("ActivationDisabledInterval"));
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.error("Unable to determine mobile phone count for " + mobilePhone);
            throw new SQLException("Unable to determine mobile phone count");
         }

         if (log.isDebugEnabled()) {
            log.debug("====== UserBean.GETMOBILEPHONECOUNT: Number: " + mobilePhone + ", ACTIVATION count: " + rs.getInt(1));
         }

         var5 = rs.getInt(1);
      } catch (Exception var17) {
         throw new SQLException("Unable to determine mobile phone count");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var16) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var15) {
            ps = null;
         }

      }

      return var5;
   }

   public UserData createPrepaidCardUser(String didNumber, String voucherNumber, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      UserData var14;
      try {
         VoucherLocal voucherEJB = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         VoucherData voucherData = voucherEJB.getVoucher(voucherNumber);
         if (voucherData == null || voucherData.status != VoucherData.StatusEnum.ACTIVE) {
            throw new EJBException("Invalid voucher number " + voucherNumber);
         }

         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         Integer iddCode = messageEJB.getIDDCode(didNumber);
         if (iddCode == null) {
            throw new EJBException("Unable to determine IDD code from DID number " + didNumber);
         }

         MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misEJB.getCountryByIDDCode(iddCode, didNumber);
         if (countryData == null) {
            throw new EJBException("Unable to determine country from IDD code " + iddCode);
         }

         UserData userData = new UserData();
         userData.type = UserData.TypeEnum.MIG33_PREPAID_CARD;
         userData.username = userData.type.toString().toLowerCase() + "_" + voucherNumber;
         userData.password = this.generatePassword();
         userData.countryID = countryData.id;
         userData.currency = countryData.currency;
         userData.language = "ENG";
         conn = this.dataSourceMaster.getConnection();
         this.addUserToDB(conn, userData, new UserRegistrationContextData((String)null, false, RegistrationType.MOBILE_REGISTRATION), accountEntrySourceData);
         voucherEJB.redeemVoucher(userData.username, voucherNumber, accountEntrySourceData);
         var14 = userData;
      } catch (SQLException var30) {
         throw new EJBException(var30.getMessage());
      } catch (CreateException var31) {
         throw new EJBException(var31.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (ps != null) {
               ((PreparedStatement)ps).close();
            }
         } catch (SQLException var28) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var27) {
            conn = null;
         }

      }

      return var14;
   }

   public Map<String, Integer> getRecentMobileActivationCountsWithMobilePrefixByIPAddress(String mobilePrefix, int recentActivationsToCheck) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Map<String, Integer> result = new HashMap();
      if (log.isDebugEnabled()) {
         log.debug("getRecentMobileActivationCountsWithMobilePrefixByIPAddress called with mobilePrefix[" + mobilePrefix + "] recentActivationsToCheck[" + recentActivationsToCheck + "]");
      }

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select max(id) as maxid from activation");
         rs = ps.executeQuery();
         int maxActivationID = false;
         if (!rs.next()) {
            throw new EJBException("Unable to determine activation max ID");
         } else {
            int maxActivationID = rs.getInt("maxid");
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select count(*) as numActivations, IPAddress from activation where id > ? and substring(mobilephone,1,?) = ? group by ipaddress");
            ps.setInt(1, Math.max(0, maxActivationID - recentActivationsToCheck));
            ps.setInt(2, mobilePrefix.length());
            ps.setString(3, mobilePrefix);
            rs = ps.executeQuery();

            while(rs.next()) {
               String IPAddress = rs.getString("IPAddress");
               int numActivations = rs.getInt("numActivations");
               result.put(IPAddress, numActivations);
            }

            HashMap var27 = result;
            return var27;
         }
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }
   }

   public boolean checkRecentCommonPrefixActivations(Integer iddCode, String mobilePhone) {
      boolean result = true;

      try {
         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         int minimumLengthForMobilePhone = messageEJB.getMinimumMobileNumberLength(iddCode);
         if (minimumLengthForMobilePhone == -1) {
            log.warn(String.format("Unable to determine minimum length of mobile number iddCode[%d] mobilephone[%s] - check skipped", iddCode, mobilePhone));
         } else {
            int maxCommonPrefixActivationsAllowed = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MAX_COMMON_PREFIX_RECENT_ACTIVATIONS_ALLOWED);
            int recentActivationsToCheck = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.NUMBER_OF_RECENT_ACTIVATIONS_FOR_COMMON_PREFIX_CHECK);
            String commonPrefix = mobilePhone.substring(0, minimumLengthForMobilePhone);
            Map<String, Integer> commonPrefixActivations = this.getRecentMobileActivationCountsWithMobilePrefixByIPAddress(commonPrefix, recentActivationsToCheck);
            if (commonPrefixActivations.size() == 1) {
               String IPAddress = (String)commonPrefixActivations.keySet().iterator().next();
               Integer commonPrefixActivationsFromSingleIPAddress = (Integer)commonPrefixActivations.values().iterator().next();
               String[] ipWhitelistPrefixes = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.IPWHITELIST_PREFIX_FOR_COMMON_PREFIX_CHECK);
               boolean ipWhitelisted = false;
               String[] arr$ = ipWhitelistPrefixes;
               int len$ = ipWhitelistPrefixes.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  String ipWhitelistPrefix = arr$[i$];
                  if (IPAddress.startsWith(ipWhitelistPrefix)) {
                     ipWhitelisted = true;
                     break;
                  }
               }

               if (!ipWhitelisted && commonPrefixActivationsFromSingleIPAddress >= maxCommonPrefixActivationsAllowed) {
                  log.warn(String.format("getMinimumMobileNumberLength failed - multy-ID suspected mobilenumber [%s] IPAddress [%s] commonPrefixActivations[%d] maxCommonPrefixActivationsAllowed[%d]", mobilePhone, IPAddress, commonPrefixActivationsFromSingleIPAddress, maxCommonPrefixActivationsAllowed));
                  result = false;
               }
            }

            log.info(String.format("checkRecentCommonPrefixActivations result[%s] setsize[%d] IPAddresses[%s] mobilenumber [%s]  commonPrefix[%s] maxCommonPrefixActivationsAllowed[%d] recentActivationsToCheck[%d]", result ? "PASS" : "BLOCKED", commonPrefixActivations.size(), commonPrefixActivations, mobilePhone, commonPrefix, maxCommonPrefixActivationsAllowed, recentActivationsToCheck));
         }
      } catch (Exception var18) {
         log.warn("Unexpected exception while checking common prefix activations for mobile registration - check skipped: " + var18.getMessage(), var18);
      }

      return result;
   }

   public UserData createUser(UserData userData, UserProfileData userProfileData, boolean sendVerificationCode, UserRegistrationContextData regContextData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      return this.createUser(userData, userProfileData, sendVerificationCode, regContextData, accountEntrySourceData, true, true, true);
   }

   public UserData createUser(UserData userData, UserProfileData userProfileData, boolean sendVerificationCode, UserRegistrationContextData regContextData, AccountEntrySourceData accountEntrySourceData, boolean performUsernameCharValidation, boolean performEmailRegRateLimitCheck, boolean performSendEmailVerificationToken) throws EJBException {
      Connection connFusion = null;
      Connection connRegistrationDB = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean userAddedToDB = false;
      boolean userAddedToRegistrationDB = false;
      int idUserRegistration = -1;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED)) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_PATH1_ENABLED) && regContextData.registrationType == RegistrationType.EMAIL_REGISTRATION_PATH1) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_PATH2_ENABLED) && regContextData.registrationType == RegistrationType.EMAIL_REGISTRATION_PATH2) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else if (StringUtil.isBlank(userData.mobilePhone) && StringUtil.isBlank(userData.emailAddress)) {
         throw new EJBException("Mobile number or email address is mandatory for registration");
      } else {
         if (regContextData.isEmailBased()) {
            EmailUtils.EmailValidatationResult evr = EmailUtils.externalEmailIsValid(userData.emailAddress);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.STRIP_PERIODS_FROM_GMAIL_ADDRESS_ENABLED) && evr.result == EmailUtils.EmailValidatationEnum.PERIODS_EXCEED_IN_GMAIL) {
               userData.emailAddress = EmailUtils.stripPeriodsFromGmailAddress(userData.emailAddress);
            } else if (evr.result != EmailUtils.EmailValidatationEnum.VALID) {
               throw new EJBException(evr.reason);
            }
         }

         if (regContextData.verified || regContextData.registrationType != RegistrationType.EMAIL_REGISTRATION_PATH2) {
            userData.password = userData.password.trim();

            try {
               this.checkPassword(userData.username, userData.password);
            } catch (NoSuchFieldException var53) {
               throw new EJBException(var53.getMessage(), var53);
            }
         }

         if (regContextData.isEmailBased()) {
            if (performEmailRegRateLimitCheck) {
               this.checkEmailRegistrationRateLimit(userData.emailAddress, userData.registrationIPAddress, regContextData.verified);
            } else {
               log.info("createUser:[" + userData.username + "] performEmailRegRateLimitCheck disabled");
            }
         } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MOBILE_REGISTRATION_DISABLED) && regContextData.registrationType == RegistrationType.MOBILE_REGISTRATION) {
            throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MOBILE_REGISTRATION_DISABLED_MESSAGE));
         }

         UserData var22;
         try {
            if (userData.username != null) {
               userData.username = userData.username.toLowerCase();
            }

            if (performUsernameCharValidation) {
               this.checkUsername(userData.username);
            } else {
               log.info("createUser:[" + userData.username + "] performUsernameCharValidation disabled.");
            }

            connFusion = this.dataSourceMaster.getConnection();
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            if (!regContextData.verified || regContextData.registrationType == RegistrationType.MOBILE_REGISTRATION) {
               if (this.usernameExists(connFusion, userData.username)) {
                  throw new EJBException("Username has already been registered");
               }

               if (!StringUtil.isBlank(userData.emailAddress) && this.emailAddressExists(connFusion, userData.emailAddress)) {
                  throw new EJBException("The email address you entered is already linked to an existing migme account! Please enter a different email address.");
               }
            }

            CountryData countryData;
            try {
               countryData = misEJB.getCountryFromIPNumber(WebCommon.toIPNumber(userData.registrationIPAddress));
            } catch (Exception var52) {
               countryData = null;
            }

            if (regContextData.isEmailBased()) {
               if (userData.countryID != null) {
                  CountryData userSelectedCountry = misEJB.getCountry(userData.countryID);
                  if (userSelectedCountry != null) {
                     countryData = userSelectedCountry;
                  }
               }
            } else {
               userData.mobilePhone = messageEJB.cleanPhoneNumber(userData.mobilePhone);

               Integer iddCode;
               try {
                  iddCode = messageEJB.getIDDCode(userData.mobilePhone);
               } catch (Exception var51) {
                  iddCode = null;
               }

               if ((iddCode == null || !messageEJB.isMobileNumber(userData.mobilePhone, true)) && userData.registrationIPAddress != null && countryData != null && countryData.id != 169 && !countryData.iddCode.equals(iddCode)) {
                  String mobilePhone = countryData.iddCode.toString() + userData.mobilePhone;
                  if (messageEJB.isMobileNumber(mobilePhone, true)) {
                     iddCode = countryData.iddCode;
                     userData.mobilePhone = mobilePhone;
                  }
               }

               userData.mobilePhone = messageEJB.cleanAndValidatePhoneNumber(userData.mobilePhone, true);
               countryData = misEJB.getCountryByIDDCode(iddCode, userData.mobilePhone);
               if (countryData == null) {
                  throw new EJBException("Unable to determine the country for mobile phone " + userData.mobilePhone);
               }

               if (userData.mobilePhone != null && this.getMobilePhoneCount(connFusion, userData.mobilePhone) > 0) {
                  throw new EJBException("The mobile number you entered, " + userData.mobilePhone + ",  is already linked to an existing migme account! Please enter another mobile number.");
               }

               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.COMMON_PREFIX_CHECK_FOR_MOBILE_REGISTRATION_ENABLED) && !this.checkRecentCommonPrefixActivations(iddCode, userData.mobilePhone)) {
                  log.warn(String.format("Blocking registration for user[%s] mobilenumber[%s] iddCode[%d]", userData.username, userData.mobilePhone, iddCode));
                  throw new EJBException("Unable to register your account right now. Please try again later");
               }
            }

            if ((userData.countryID == null || userData.countryID == 0) && countryData != null) {
               userData.countryID = countryData.id;
            }

            if (userData.currency == null) {
               userData.currency = countryData.currency;
            }

            if (userData.language == null) {
               userData.language = "ENG";
            }

            if (!regContextData.verified && (regContextData.registrationType == RegistrationType.EMAIL_REGISTRATION_PATH1 || regContextData.registrationType == RegistrationType.EMAIL_REGISTRATION_PATH2)) {
               String token = this.generateEmailVerificationToken(userData.emailAddress);
               connRegistrationDB = this.userRegistrationMaster.getConnection();
               int idUserRegistration = this.addUserToUserRegistrationTable(connRegistrationDB, userData, regContextData, accountEntrySourceData);
               userAddedToRegistrationDB = true;
               if (token == null) {
                  log.error("Unable to generate email verification token for user: " + userData.username);
                  throw new EJBException("Internal server error. Please try again later");
               }

               this.updateUserRegistrationWithToken(connRegistrationDB, idUserRegistration, token);
               this.sendVerificationToken(userData.username, userData.emailAddress, token, regContextData.registrationType, performSendEmailVerificationToken);
               UserData var59 = userData;
               return var59;
            }

            this.addUserToDB(connFusion, userData, regContextData, accountEntrySourceData);
            userAddedToDB = true;
            Credential userCredential = new Credential(userData.userID, userData.username, userData.password, PasswordType.FUSION.value());
            AuthenticationServiceResponseCodeEnum response = EJBIcePrxFinder.getAuthenticationServiceProxy().createCredential(userCredential);
            if (response != AuthenticationServiceResponseCodeEnum.Success) {
               log.error("Failed to register user [" + userData.username + "] with id [" + userData.userID + "] response " + response);
               throw new Exception("Unable to complete user registration");
            }

            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            var22 = userEJB.postCreateUser(userData, userProfileData, sendVerificationCode, accountEntrySourceData, regContextData, performSendEmailVerificationToken);
         } catch (Exception var54) {
            log.error("Failed to create user [" + userData.username + "]", var54);
            if (userAddedToDB) {
               try {
                  this.deleteUserFromFusionDB(connFusion, userData);
               } catch (Exception var50) {
                  log.error("failed to delete user from DB: " + userData.username, var50);
               }
            }

            if (userAddedToRegistrationDB) {
               try {
                  this.deleteUserFromRegistrationDB(connRegistrationDB, idUserRegistration);
               } catch (Exception var49) {
                  log.error("failed to delete user from Registration DB: " + userData.username + " id:" + idUserRegistration, var49);
               }
            }

            if (var54 instanceof EJBException) {
               throw (EJBException)var54;
            }

            throw new EJBException(var54.getMessage(), var54);
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var48) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ((PreparedStatement)ps).close();
               }
            } catch (SQLException var47) {
               ps = null;
            }

            try {
               if (connFusion != null) {
                  connFusion.close();
               }
            } catch (SQLException var46) {
               connFusion = null;
            }

            try {
               if (connRegistrationDB != null) {
                  connRegistrationDB.close();
               }
            } catch (SQLException var45) {
               connRegistrationDB = null;
            }

         }

         return var22;
      }
   }

   private void checkEmailRegistrationRateLimit(String emailAddress, String registrationIPAddress, boolean isStep2) throws EJBException {
      String rateLimitPattern = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_IP);
      if (!StringUtil.isBlank(rateLimitPattern)) {
         if (!SystemProperty.isValueInArray(registrationIPAddress, SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_IP_WHITELIST)) {
            try {
               MemCachedRateLimiter.NameSpace rateLimitNamespace = isStep2 ? MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_IP_STEP_2 : MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_IP;
               MemCachedRateLimiter.hit(rateLimitNamespace, registrationIPAddress, rateLimitPattern);
            } catch (MemCachedRateLimiter.LimitExceeded var19) {
               log.info(String.format("rate limit of email registration exceeded for IP %s: %s", registrationIPAddress, rateLimitPattern));
               throw new EJBException("You have exceeded the number of registration attempts for this session. Please come back and try again later.");
            } catch (MemCachedRateLimiter.FormatError var20) {
               log.error("Formatting error in rate limiter expression when checking rate limit for email registration by IP: " + var20.getMessage());
               throw new EJBException("Internal error. Please try again later.");
            }
         } else {
            log.info(String.format("rate limit of email registration IP %s skipped due to whitelisting", registrationIPAddress));
         }
      }

      String emailDomain = "";

      try {
         emailDomain = EmailUtils.getDomain(emailAddress);
      } catch (Exception var18) {
         throw new EJBException(var18.getMessage());
      }

      rateLimitPattern = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_DOMAIN);
      if (!StringUtil.isBlank(rateLimitPattern)) {
         if (!SystemProperty.isValueInArray(emailDomain, SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_DOMAIN_WHITELIST)) {
            try {
               MemCachedRateLimiter.NameSpace rateLimitNamespace = isStep2 ? MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_DOMAIN_STEP_2 : MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_DOMAIN;
               MemCachedRateLimiter.hit(rateLimitNamespace, emailDomain, rateLimitPattern);
            } catch (MemCachedRateLimiter.LimitExceeded var16) {
               log.info(String.format("rate limit of email registration exceeded for domain %s: %s", emailDomain, rateLimitPattern));
               throw new EJBException("You have exceeded the number of registration attempts for this session. Please come back and try again later.");
            } catch (MemCachedRateLimiter.FormatError var17) {
               log.error(String.format("Formatting error in rate limiter expression when checking rate limit for email registration by domain [%s]: %s", emailDomain, var17.getMessage()));
               throw new EJBException("Internal error. Please try again later.");
            }
         } else {
            log.info(String.format("rate limit of email registration domain %s skipped due to whitelisting", emailDomain));
         }
      }

      MemCachedRateLimiter.NameSpace rateLimitNamespace;
      String topLevelDomain;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.SANITIZED_EMAIL_LOCAL_PART_AND_DOMAIN_RATE_LIMIT_ENABLED)) {
         rateLimitPattern = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_SANITIZIED_LOCAL_AND_DOMAIN);
         topLevelDomain = EmailUtils.getSanitizedEmailAddressLocalPart(emailAddress) + "/" + emailDomain;
         if (!StringUtil.isBlank(rateLimitPattern)) {
            try {
               rateLimitNamespace = isStep2 ? MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_DOMAIN_STEP_2 : MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_DOMAIN;
               MemCachedRateLimiter.hit(rateLimitNamespace, topLevelDomain, rateLimitPattern);
            } catch (MemCachedRateLimiter.LimitExceeded var14) {
               log.info(String.format("rate limit of email registration (sanitized local email part / domain) exceeded for email address [%s] key [%s] : %s", emailAddress, topLevelDomain, rateLimitPattern));
               throw new EJBException("You have exceeded the number of registration attempts for this session. Please come back and try again later.");
            } catch (MemCachedRateLimiter.FormatError var15) {
               log.error(String.format("Formatting error in rate limiter expression when checking rate limit for sanitized email local part and domain [%s]: %s", topLevelDomain, var15.getMessage()));
               throw new EJBException("We are unable to process your request at the moment. Please try again later.");
            }
         }
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.SANITIZED_EMAIL_LOCAL_PART_AND_IP_RATE_LIMIT_ENABLED)) {
         rateLimitPattern = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_SANITIZIED_LOCAL_AND_IP);
         topLevelDomain = EmailUtils.getSanitizedEmailAddressLocalPart(emailAddress) + "/" + registrationIPAddress;
         if (!StringUtil.isBlank(rateLimitPattern)) {
            try {
               rateLimitNamespace = isStep2 ? MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_AND_IP_STEP_2 : MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_SANITIZED_EMAIL_LOCAL_AND_IP;
               MemCachedRateLimiter.hit(rateLimitNamespace, topLevelDomain, rateLimitPattern);
            } catch (MemCachedRateLimiter.LimitExceeded var12) {
               log.info(String.format("rate limit of email registration (sanitized local email part/ip address) exceeded for email address [%s] key [%s] : %s", emailAddress, topLevelDomain, rateLimitPattern));
               throw new EJBException("You have exceeded the number of registration attempts for this session. Please come back and try again later.");
            } catch (MemCachedRateLimiter.FormatError var13) {
               log.error(String.format("Formatting error in rate limiter expression when checking rate limit for sanitized email local part and domain [%s]: %s", topLevelDomain, var13.getMessage()));
               throw new EJBException("We are unable to process your request at the moment. Please try again later.");
            }
         }
      }

      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.NON_WHITELISTED_DOMAINS_TOP_LEVEL_PART_RATE_LIMIT_ENABLED) && !SystemProperty.isValueInArray(emailDomain, SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_DOMAIN_WHITELIST)) {
         rateLimitPattern = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_RATE_LIMIT_BY_NONWHITELISTED_DOMAINS_TOP_LEVEL_PART);
         topLevelDomain = "";

         try {
            topLevelDomain = EmailUtils.getTopLevelDomain(emailAddress);
         } catch (Exception var11) {
            throw new EJBException(var11.getMessage());
         }

         String key = topLevelDomain;
         if (!StringUtil.isBlank(rateLimitPattern)) {
            try {
               MemCachedRateLimiter.NameSpace rateLimitNamespace = isStep2 ? MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_NON_WHITELISTED_DOMAIN_TOP_LEVEL_PART_STEP_2 : MemCachedRateLimiter.NameSpace.EMAIL_REG_RATE_LIMIT_NON_WHITELISTED_DOMAIN_TOP_LEVEL_PART;
               MemCachedRateLimiter.hit(rateLimitNamespace, key, rateLimitPattern);
            } catch (MemCachedRateLimiter.LimitExceeded var9) {
               log.info(String.format("rate limit of email registration (non whitelisted domain top level part) exceeded for email address [%s] key [%s] : %s", emailAddress, topLevelDomain, rateLimitPattern));
               throw new EJBException("You have exceeded the number of registration attempts for this session. Please come back and try again later.");
            } catch (MemCachedRateLimiter.FormatError var10) {
               log.error(String.format("Formatting error in rate limiter expression when checking rate limit for non whitelisted domain top level part [%s]: %s", topLevelDomain, var10.getMessage()));
               throw new EJBException("We are unable to process your request at the moment. Please try again later.");
            }
         }
      }

   }

   /** @deprecated */
   public UserData postCreateUser(UserData userData, UserProfileData userProfileData, boolean sendVerificationCode, AccountEntrySourceData accountEntrySourceData, UserRegistrationContextData userRegContextData) throws EJBException {
      return this.postCreateUser(userData, userProfileData, sendVerificationCode, accountEntrySourceData, userRegContextData, true);
   }

   public UserData postCreateUser(UserData userData, UserProfileData userProfileData, boolean sendVerificationCode, AccountEntrySourceData accountEntrySourceData, UserRegistrationContextData userRegContextData, boolean performSendEmailVerification) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserData var41;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         userProfileData.id = null;
         userProfileData.username = userData.username;
         this.updateUserProfile(userProfileData);
         if (!StringUtil.isBlank(userData.mobilePhone)) {
            ps = connMaster.prepareStatement("select userid.id userid, user.username, user.displayname, user.mobilephone,userreferral.paid paid from userreferral, user, userid  where  userreferral.username = user.username  and user.username = userid.username  and userreferral.mobilephone = ?  order by userreferral.id desc limit 1");
            ps.setString(1, userData.mobilePhone);
            rs = ps.executeQuery();
            String messagePattern;
            if (rs.next()) {
               int referrerUserID = rs.getInt("userid");
               messagePattern = rs.getString("displayname");
               String referrerUsername = rs.getString("username");
               String referrerMobilePhone = rs.getString("mobilephone");
               int paid = rs.getInt("paid");
               if (paid == 0) {
                  ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
                  contactEJB.makeReferrerAndReferreeFriends(userData.userID, userData.username, userData.mobilePhone, referrerUserID, referrerUsername, messagePattern, referrerMobilePhone);
               }
            }

            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            if (sendVerificationCode && messageEJB.isMobileNumber(userData.mobilePhone, true)) {
               messagePattern = "";
               if (userData.registrationDevice != null && userData.registrationDevice.equals("Web")) {
                  messagePattern = SystemProperty.get("VerificationCodeWebSMS");
               } else {
                  messagePattern = SystemProperty.get("VerificationCodeSMS");
               }

               this.sendVerificationCode(userData.username, userData.password, userData.registrationIPAddress, userData.mobilePhone, userData.verificationCode, messagePattern, accountEntrySourceData);
            }
         } else if (userRegContextData.registrationType == RegistrationType.EMAIL_LEGACY && !StringUtil.isBlank(userData.emailAddress)) {
            String token = this.generateEmailVerificationToken(userData.emailAddress);
            if (token == null) {
               throw new EJBException("Unable to generate email verification token");
            }

            if (!this.cacheEmailVerificationTokenDetails(token, userData.userID, userData.emailAddress)) {
               throw new EJBException("Unable to store email verification token");
            }

            this.sendVerificationToken(userData.username, userData.emailAddress, token, userRegContextData.registrationType, performSendEmailVerification);
         }

         if (userRegContextData.verified) {
            try {
               RewardCentre.getInstance().sendTrigger(new UserFirstAuthenticatedTrigger(userData, new RegistrationContextData(userData, userRegContextData, accountEntrySourceData)));
            } catch (Exception var33) {
               log.warn("Unable to notify reward system to send UserFirstAuthenticatedTrigger", var33);
            }

            Integer invitationID = userRegContextData.invitationID;
            if (invitationID != null) {
               Timestamp actionTime = new Timestamp(System.currentTimeMillis());
               InvitationData invitationData = this.getAndValidateSignUpInvitationData(connMaster, invitationID, actionTime);
               if (invitationData != null && InvitationUtils.isInvitationEngineEnabled(invitationData.channel)) {
                  InvitationResponseData invitationResponseData = this.logInvitationResponse(connMaster, actionTime, invitationData, InvitationResponseData.ResponseType.SIGN_UP_VERIFIED, userData.username, InvitationData.StatusFieldValue.CLOSED);
                  log.info(String.format("Invitation Response: invitaionID:%s, inviteeID:%s, inviterID:%s, response:%s, activity:%s", invitationData.id, userData.userID, invitationData.inviterUserId, invitationResponseData.responseType, invitationData.type));
               }
            }
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.BCLPERSISTED_ENABLED)) {
            BroadcastListPersisted.setBroadcastListPersisted(bclPersistedMemcache, userData.username, 1);
         }

         MemCachedHelper.setUsernameIdMapping(userData.username, userData.userID);
         this.sendMigAlertsToNewUser(userData, (String)null);
         var41 = userData;
      } catch (NoSuchFieldException var34) {
         throw new EJBException(var34.getMessage());
      } catch (CreateException var35) {
         throw new EJBException(var35.getMessage());
      } catch (SQLException var36) {
         throw new EJBException(var36.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var32) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var31) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var30) {
            connMaster = null;
         }

      }

      return var41;
   }

   private InvitationData getAndValidateSignUpInvitationData(Connection conn, int invitationID, Date actionTime) {
      if (invitationID <= 0) {
         log.error("processing invitation id will be ignored. Invitation ID value is " + invitationID);
         return null;
      } else {
         InvitationData invitationData = this.getInvitationData(invitationID, true, conn);
         if (invitationData == null) {
            log.error("processing invitation id will be ignored.Invitation ID " + invitationID + " does not exist");
            return null;
         } else {
            InvitationData.StatusFieldValue invitationStatus = InvitationUtils.getInvitationStatus(invitationData, actionTime);
            if (invitationStatus != InvitationData.StatusFieldValue.NO_RESPONSE) {
               log.error("processing invitation id will be ignored.Invitation ID " + invitationID + " invitationStatusEnum is " + invitationStatus);
               return null;
            } else {
               return invitationData;
            }
         }
      }
   }

   public void sendVerificationToken(String username, String emailAddress, String token, RegistrationType type) throws EJBException {
      this.sendVerificationToken(username, emailAddress, token, type, true);
   }

   private void sendVerificationToken(String username, String emailAddress, String token, RegistrationType type, boolean sendVerificationToken) throws EJBException {
      try {
         if (!StringUtil.isBlank(emailAddress)) {
            String urlParams = String.format("userName=%s&email=%s", URLEncoder.encode(username, "UTF-8"), URLEncoder.encode(emailAddress, "UTF-8"));
            String emailVerifyLink;
            String content;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.ENABLED_SEND_VERIFICATION_TOKEN_WITH_TEMPLATE)) {
               Map<String, String> params = new HashMap();
               params.put("username", username);
               emailVerifyLink = SystemProperty.get("VerifyEmailActivateAccountLink", "http://www.mig33.com/sites/corporate/registration/email_verification?token=");
               content = "&";
               if (type == RegistrationType.EMAIL_REGISTRATION_PATH1) {
                  emailVerifyLink = SystemProperty.get("VerifyEmailActivateAccountLinkPath1", "https://register.mig.me/verify/");
                  content = "?";
               } else if (type == RegistrationType.EMAIL_REGISTRATION_PATH2) {
                  emailVerifyLink = SystemProperty.get("VerifyEmailActivateAccountLinkPath2", "https://register.mig.me/verify/step2/");
                  content = "?";
               }

               params.put("verification_link", emailVerifyLink + token + content + urlParams);
               if (sendVerificationToken) {
                  UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                  unsProxy.sendTemplatizedEmailFromNoReply(emailAddress, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.VERIFICATION_EMAIL_TEMPLATE_ID), params);
               } else {
                  log.info("sendVerificationToken(with template) [" + username + "],[" + emailAddress + "],token[" + token + "],regType:[" + type + "],params:[" + params + "] disabled.");
               }
            } else if (!StringUtil.isBlank(emailAddress)) {
               String emailVerifyLink = SystemProperty.get("VerifyEmailActivateAccountLink", "http://mig.me/sites/corporate/registration/email_verification?token=");
               emailVerifyLink = SystemProperty.get("VerifyEmailActivateAccountSubject", "Activate your migme account");
               content = SystemProperty.get("VerifyEmailActivateAccountContent", "Hi {0},\n\nThank you for signing up with migme! \n\n You are one step closer to being part of our awesome community. Please click on this link to activate your account now: {1}\n\n--The migme Team\n\n(if clicking the link in this message does not work, copy and paste it into the address bar of your browser)");
               content = MessageFormat.format(content, username, emailVerifyLink + token + "&" + urlParams);
               if (sendVerificationToken) {
                  log.info("Sending verification email to [" + username + "] : subject: [" + emailVerifyLink + "] content: [" + content + "]");
                  MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                  messageEJB.sendEmailFromNoReply(emailAddress, emailVerifyLink, content);
               } else {
                  log.info("sendVerificationToken(without template) [" + username + "],[" + emailAddress + "],token[" + token + "],regType:[" + type + "] disabled.");
               }
            }
         }

      } catch (Exception var11) {
         log.error("could not create email verification token for: " + username, var11);
         throw new EJBException(var11.getMessage());
      }
   }

   public boolean isUsernameAvailable(String username) {
      Connection conn = null;

      boolean var4;
      try {
         this.checkUsername(username);
         conn = this.dataSourceSlave.getConnection();
         boolean var3 = !this.usernameExists(conn, username);
         return var3;
      } catch (EJBException var18) {
         log.debug("Username [" + username + "] is not available for registration: " + var18);
         var4 = false;
         return var4;
      } catch (SQLException var19) {
         log.debug(var19);
         var4 = false;
         return var4;
      } catch (NoSuchFieldException var20) {
         log.debug("Username [" + username + "] is not valid: " + var20);
         var4 = false;
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return var4;
   }

   private void sendMigAlertsToNewUser(UserData userData, String alerts_message) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Alert.MIGBO_SYS_ALERTS_FOR_NEW_USER_ENABLED)) {
         String alerts_str = null;

         try {
            UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
            alerts_str = alerts_message == null ? SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Alert.MIGBO_SYS_ALERTS_FOR_NEW_USER) : alerts_message;
            if (StringUtils.isEmpty(alerts_str)) {
               return;
            }

            log.info("Sending migalerts to new user:" + alerts_str);
            JSONArray alerts_array = new JSONArray(alerts_str);

            for(int i = 0; i < alerts_array.length(); ++i) {
               JSONObject alert = alerts_array.getJSONObject(i);
               if (alert.has("alert_key") && alert.has("alert_content")) {
                  log.info("Sending migalerts to new user");
                  unsProxy.notifyFusionUser(new Message(alert.getString("alert_key"), userData.userID, userData.username, Enums.NotificationTypeEnum.SYS_ALERT.getType(), System.currentTimeMillis(), this.parseMigAlertsFromJSONArrayToMap(alert)));
               } else {
                  log.warn(String.format("Failed to parse mig alerts for new user: %s, alert: %s, should at lease have alert_key and alert_content", userData.userID, alert));
               }
            }
         } catch (JSONException var8) {
            log.warn(String.format("Failed to parse mig alerts for new user: %s, alert: %s", userData.userID, alerts_str), var8);
         } catch (Exception var9) {
            log.error(String.format("Failed to create mig alerts for new user: %s", userData.userID), var9);
         }

      }
   }

   private void sendMigAlertToInviterWhenInviteeRegister(UserData inviterUserData, UserData inviteeUserData) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Alert.MIGBO_SYS_ALERTS_FOR_INVITER_ENABLED)) {
         if (inviterUserData != null && inviteeUserData != null && !StringUtil.isBlank(inviteeUserData.emailAddress)) {
            try {
               UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
               Map<String, String> parameters = new HashMap();
               parameters.put("alert_key", "inform_inviter_with_invitee_email");
               parameters.put("alert_content", inviteeUserData.emailAddress + " has just joined migme as Username:%{invitee_username}.");
               parameters.put("invitee_username_linktype", "invitee_username");
               parameters.put("invitee_username_label", inviteeUserData.username);
               unsProxy.notifyFusionUser(new Message((String)parameters.get("alert_key"), inviterUserData.userID, inviterUserData.username, Enums.NotificationTypeEnum.SYS_ALERT.getType(), System.currentTimeMillis(), parameters));
               log.info(String.format("Sending sys alert to inviter:%s, informing inviter that invitee:%s with emailaddress:%s has joined migme", inviterUserData.username, inviteeUserData.username, inviteeUserData.emailAddress));
            } catch (Exception var5) {
               log.error(String.format("Failed to create mig alerts for inviter: %s", inviterUserData.userID), var5);
            }

         }
      }
   }

   private void sendMigAlertToInviterWhenInviteeAcceptInvitation(UserData inviterUserData, UserData inviteeUserData) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Alert.MIGBO_SYS_ALERTS_FOR_INVITER_ENABLED_WHEN_INVITEE_ACCEPT_INVITATION)) {
         if (inviterUserData != null && inviteeUserData != null) {
            try {
               UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
               Map<String, String> parameters = new HashMap();
               parameters.put("alert_key", "invitee_accpet_invitation");
               parameters.put("alert_content", "%{invitee_username} has already accepted your invitation.");
               parameters.put("invitee_username_linktype", "invitee_username");
               parameters.put("invitee_username_label", inviteeUserData.username);
               unsProxy.notifyFusionUser(new Message((String)parameters.get("alert_key"), inviterUserData.userID, inviterUserData.username, Enums.NotificationTypeEnum.SYS_ALERT.getType(), System.currentTimeMillis(), parameters));
               log.info(String.format("Sending sys alert to inviter:%s, informing inviter that invitee:%s has already accepted invitation", inviterUserData.username, inviteeUserData.username));
            } catch (Exception var5) {
               log.error(String.format("Failed to create mig alerts for inviter: %s", inviterUserData.userID), var5);
            }

         }
      }
   }

   private Map<String, String> parseMigAlertsFromJSONArrayToMap(JSONObject alert) throws JSONException {
      Map<String, String> parameters = new HashMap();
      parameters.put("alert_content", alert.getString("alert_content"));
      Iterator keys = alert.keys();

      while(keys.hasNext()) {
         String key = (String)keys.next();
         if (!key.equals("alert_key") && !key.equals("alert_content")) {
            parameters.put(key, alert.getString(key));
         }
      }

      return parameters;
   }

   private boolean usernameExists(Connection conn, String username) throws EJBException {
      if (StringUtil.isBlank(username)) {
         return true;
      } else {
         PreparedStatement ps = null;
         ResultSet rs = null;
         Connection userRegistrationConn = null;

         boolean var7;
         try {
            String statement = "select username from user where username = ? or username=(select distinct username from useralias where alias= ?)";
            ps = conn.prepareStatement(statement);
            ps.setString(1, username);
            ps.setString(2, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               if (this.isEmailRegistrationV2Enabled()) {
                  userRegistrationConn = this.userRegistrationSlave.getConnection();
                  statement = "SELECT DISTINCT username FROM (SELECT * FROM userregistration WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMax() + ",now()) AND username=?) AS t " + "WHERE verified=1 OR updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMin() + ",now())";
                  ps = userRegistrationConn.prepareStatement(statement);
                  ps.setString(1, username);
                  rs = ps.executeQuery();
                  if (rs.next()) {
                     var7 = true;
                     return var7;
                  }
               }

               var7 = false;
               return var7;
            }

            var7 = true;
         } catch (SQLException var28) {
            log.info(var28);
            throw new EJBException("Unable to check if username exists");
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var27) {
               ps = null;
            }

            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var26) {
               rs = null;
            }

            try {
               if (userRegistrationConn != null) {
                  userRegistrationConn.close();
               }
            } catch (SQLException var25) {
               userRegistrationConn = null;
            }

         }

         return var7;
      }
   }

   public boolean emailAddressExists(String emailAddress) throws EJBException {
      Connection conn = null;

      boolean var3;
      try {
         conn = this.dataSourceSlave.getConnection();
         var3 = this.emailAddressExists(conn, emailAddress);
      } catch (SQLException var12) {
         log.warn("Unable to check if email address exists: " + var12);
         throw new EJBException("Unable to check if email address exists");
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var11) {
            conn = null;
         }

      }

      return var3;
   }

   private boolean emailAddressExists(Connection conn, String emailAddress) throws EJBException {
      if (StringUtil.isBlank(emailAddress)) {
         throw new EJBException("Email address is blank");
      } else {
         PreparedStatement ps = null;
         ResultSet rs = null;
         Connection userRegistrationConn = null;

         boolean var7;
         try {
            String statement = "select emailaddress from useremailaddress where emailaddress = ?";
            ps = conn.prepareStatement(statement);
            ps.setString(1, emailAddress);
            rs = ps.executeQuery();
            if (!rs.next()) {
               if (this.isEmailRegistrationV2Enabled()) {
                  userRegistrationConn = this.userRegistrationSlave.getConnection();
                  statement = "SELECT DISTINCT emailaddress FROM (SELECT * FROM userregistration WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMax() + ",now()) AND emailaddress=?) AS t " + "WHERE verified=1 OR updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMin() + ",now())";
                  ps = userRegistrationConn.prepareStatement(statement);
                  ps.setString(1, emailAddress);
                  rs = ps.executeQuery();
                  if (rs.next()) {
                     var7 = true;
                     return var7;
                  }
               }

               var7 = false;
               return var7;
            }

            var7 = true;
         } catch (SQLException var28) {
            log.info(var28);
            throw new EJBException("Unable to check if email address exists");
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var27) {
               ps = null;
            }

            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var26) {
               rs = null;
            }

            try {
               if (userRegistrationConn != null) {
                  userRegistrationConn.close();
               }
            } catch (SQLException var25) {
               userRegistrationConn = null;
            }

         }

         return var7;
      }
   }

   public UserVerificationData getVerificationDataFromToken(String token, boolean includingExpiredToken) throws EJBException {
      if (!this.isEmailRegistrationV2Enabled()) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         UserVerificationData var33;
         try {
            conn = this.userRegistrationSlave.getConnection();
            String statement = null;
            if (!includingExpiredToken) {
               statement = "SELECT username,emailaddress,updatedtime,registrationtype,verified,context FROM (SELECT * FROM userregistration WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMax() + ",now()) AND verificationtoken=?) AS t " + "WHERE verified=1 OR updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMin() + ",now())";
            } else {
               statement = "SELECT username,emailaddress,updatedtime,registrationtype,verified,context FROM userregistration where verificationtoken=? LIMIT 1";
            }

            ps = conn.prepareStatement(statement);
            ps.setString(1, token);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Token does not exist");
            }

            String username = rs.getString(1);
            String emailAddress = rs.getString(2);
            Long updatedTime = rs.getTimestamp(3).getTime();
            String type = rs.getString(4);
            Boolean isVerified = rs.getBoolean(5);
            String registrationContext = rs.getString(6);
            String campaign = null;
            if (!StringUtil.isBlank(registrationContext)) {
               try {
                  JSONObject jsonObj = new JSONObject(registrationContext);
                  campaign = jsonObj.optString(Integer.toString(RegistrationContextData.RegistrationContextTypeEnum.CAMPAIGN.value()), (String)null);
               } catch (JSONException var30) {
                  log.warn("Unable to parse JSON string: " + var30);
               }
            }

            var33 = new UserVerificationData(username, emailAddress, type, isVerified, updatedTime, campaign);
         } catch (SQLException var31) {
            throw new EJBException(var31.getMessage());
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var29) {
               ps = null;
            }

            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var28) {
               rs = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var27) {
               conn = null;
            }

         }

         return var33;
      }
   }

   public UserVerificationData getVerificationDataFromToken(String token) throws EJBException {
      return this.getVerificationDataFromToken(token, false);
   }

   public boolean validateRegistrationToken(UserVerificationData data) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.add(12, -1 * SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_TIMELIMIT_IN_MINUTES));
      Date verificationTimeLimit = calendar.getTime();
      if (data.updatedTime > verificationTimeLimit.getTime()) {
         try {
            this.verifyEmailRegistration(data.username, data.emailAddress);
         } catch (EJBException var5) {
            log.error("Unable to update verification status in the DB: " + var5);
         }

         return true;
      } else {
         return false;
      }
   }

   public void insertPartnerUser(int partnerId, int userId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("insert into ussdpartneruser (userid, ussdpartnerid) values (?, ?)");
         ps.setInt(1, userId);
         ps.setInt(2, partnerId);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Creating partner user record failed");
         }
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public UserData loadStaff(String username) throws EJBException {
      UserData userData = null;
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from staff where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            userData = new UserData();
            userData.username = rs.getString("username");
            userData.password = rs.getString("password");
            ps.close();
            rs.close();
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return userData;
   }

   private Set<String> loadStringListForUser(String username, String field, String tableName, Connection connection) throws SQLException {
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;

      try {
         preparedStatement = connection.prepareStatement("select " + field + " from " + tableName + " where username = ?");
         preparedStatement.setString(1, username);
         resultSet = preparedStatement.executeQuery();
         HashSet set = new HashSet();

         while(resultSet.next()) {
            set.add(resultSet.getString(field));
         }

         HashSet var8 = set;
         return var8;
      } finally {
         try {
            if (resultSet != null) {
               resultSet.close();
            }
         } catch (SQLException var18) {
            resultSet = null;
         }

         try {
            if (preparedStatement != null) {
               preparedStatement.close();
            }
         } catch (SQLException var17) {
            preparedStatement = null;
         }

      }
   }

   public Set<String> loadBroadcastList(String username, Connection connection) throws SQLException {
      Set<String> broadcastList = BroadcastList.getBroadcastList(broadcastListMemcache, username);
      if (broadcastList == null) {
         ConnectionHolder ch = null;

         try {
            ch = new ConnectionHolder(this.dataSourceSlave, connection);
            broadcastList = this.loadStringListForUser(username, "broadcastUsername", "broadcastlist", ch.getConnection());
            if (!broadcastList.isEmpty()) {
               BroadcastList.setBroadcastList(broadcastListMemcache, username, broadcastList);
            }
         } finally {
            if (ch != null) {
               ch.close();
            }

         }
      }

      if (log.isDebugEnabled()) {
         log.debug("loaded " + broadcastList.size() + " entries for broadcast list");
      }

      return broadcastList;
   }

   public Set<String> checkAndLoadBroadcastList(String username, Connection connection) throws SQLException, CreateException, EJBException, NoSuchFieldException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHECK_AND_POPULATEBCL)) {
         ContactLocal contactEJB = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
         Set<String> broadcastList = contactEJB.checkAndPopulateBCL(username, connection);
         if (log.isDebugEnabled()) {
            log.debug("found " + broadcastList.size() + " BCL entries for user [" + username + "] after checkAndPopulate");
         }

         return broadcastList;
      } else {
         return this.loadBroadcastList(username, connection);
      }
   }

   private Set<String> loadBlockList(String username, Connection connection) throws SQLException {
      Set<String> blockList = (Set)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.BLOCK_LIST, username);
      if (blockList == null) {
         blockList = this.loadStringListForUser(username, "blockUsername", "blocklist", connection);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.BLOCK_LIST, username, blockList);
      }

      return blockList;
   }

   private Set<String> loadPendingContacts(String username, Connection connection) throws SQLException {
      Set<String> pendingContacts = this.loadStringListForUser(username, "pendingContact", "pendingcontact", connection);
      if (log.isDebugEnabled()) {
         log.debug("loaded " + pendingContacts.size() + " entries for pending contact list");
      }

      return pendingContacts;
   }

   public List<UserSettingData> getUserSettings(String username) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Object var6;
      try {
         List<UserSettingData> settings = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.USER_SETTING, username);
         if (settings == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select * from usersetting where username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            settings = new ArrayList();

            while(rs.next()) {
               ((List)settings).add(new UserSettingData(rs));
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_SETTING, username, settings);
         }

         var6 = settings;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return (List)var6;
   }

   private void populateUserSettings(UserData userData, Connection conn) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         List<UserSettingData> settings = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.USER_SETTING, userData.username);
         if (settings == null) {
            ps = conn.prepareStatement("select * from usersetting where username = ?");
            ps.setString(1, userData.username);
            rs = ps.executeQuery();
            settings = new ArrayList();

            while(rs.next()) {
               ((List)settings).add(new UserSettingData(rs));
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_SETTING, userData.username, settings);
         }

         Iterator i$ = ((List)settings).iterator();

         while(i$.hasNext()) {
            UserSettingData setting = (UserSettingData)i$.next();
            if (setting.type != null) {
               switch(setting.type) {
               case MESSAGE:
                  userData.messageSetting = UserSettingData.MessageEnum.fromValue(setting.value);
                  break;
               case ANONYMOUS_CALL:
                  userData.anonymousCallSetting = UserSettingData.AnonymousCallEnum.fromValue(setting.value);
                  break;
               case EMAIL_ALL:
                  userData.emailAllSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
                  break;
               case EMAIL_MENTION:
                  userData.emailMentionSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
                  break;
               case EMAIL_NEW_FOLLOWER:
                  userData.emailNewFollowerSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
                  break;
               case EMAIL_RECEIVE_GIFT:
                  userData.emailReceiveGiftSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
                  break;
               case EMAIL_REPLY_TO_POST:
                  userData.emailReplyToPostSetting = UserSettingData.EmailSettingEnum.fromValue(setting.value);
               }
            }
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var17) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

      }

   }

   public void checkUserAliasByUserid(int userid, String alias, boolean sameAsUsername, Connection conn) throws EJBException {
      ConnectionHolder ch = null;

      try {
         try {
            this.checkUserAlias(alias, sameAsUsername);
         } catch (FusionEJBException var16) {
            throw new EJBException("Invalid alias");
         }

         ch = new ConnectionHolder(this.dataSourceSlave, (Connection)null);
         int aliasUserid = this.getUseridByAlias(alias, ch.getConnection());
         if (aliasUserid != -1) {
            if (aliasUserid != userid) {
               throw new EJBException("Not available");
            }

            throw new EJBException("Already set");
         }

         if (!sameAsUsername) {
            aliasUserid = this.getUserID(alias, ch.getConnection(), false);
            if (aliasUserid != -1 && aliasUserid != userid) {
               throw new EJBException("Not available");
            }
         }
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var15) {
            ch = null;
         }

      }

   }

   public void setUserAliasByUserid(int userid, String alias) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         ch = new ConnectionHolder(this.dataSourceMaster, (Connection)null);
         String username = this.getUsernameByUserid(userid, ch.getConnection());
         if (StringUtil.isBlank(username)) {
            throw new EJBException("Invalid user");
         }

         this.checkUserAliasByUserid(userid, alias, username.equalsIgnoreCase(alias), ch.getConnection());
         if (this.getUserAliasByUserid(userid, ch.getConnection()) != null) {
            throw new EJBException("Already set");
         }

         ps = ch.getConnection().prepareStatement("insert into useralias (username, alias, dateupdated) values (?, ?, now())");
         ps.setString(1, username);
         ps.setString(2, alias);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to set alias");
         }

         MemCachedHelper.setUserAlias(username, userid, alias);
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var18) {
            ch = null;
         }

      }

   }

   public String getUserAliasByUsername(String username, Connection conn) throws EJBException {
      if (StringUtil.isBlank(username)) {
         return null;
      } else {
         ConnectionHolder ch = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            String alias = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.USER_ALIAS_BY_USERNAME, username.toLowerCase());
            String var27;
            if (alias == null) {
               ch = new ConnectionHolder(this.dataSourceSlave, conn);
               ps = ch.getConnection().prepareStatement("select id,alias from userid uid, useralias ua where uid.username = ua.username and ua.username = ?");
               ps.setString(1, username);
               rs = ps.executeQuery();
               int userid = -1;
               if (rs.next()) {
                  userid = rs.getInt("id");
                  alias = rs.getString("alias");
               }

               MemCachedHelper.setUserAlias(username, userid, alias);
            } else if (StringUtil.isBlank(alias)) {
               var27 = null;
               return var27;
            }

            var27 = alias;
            return var27;
         } catch (SQLException var25) {
            throw new EJBException(var25.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var24) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var23) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var22) {
               ch = null;
            }

         }
      }
   }

   public String getUsernameByAlias(String alias, Connection conn) throws EJBException {
      if (StringUtil.isBlank(alias)) {
         return null;
      } else {
         ConnectionHolder ch = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         String var27;
         try {
            String username = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.USER_NAME_BY_ALIAS, alias.toLowerCase());
            if (username == null) {
               ch = new ConnectionHolder(this.dataSourceSlave, conn);
               ps = ch.getConnection().prepareStatement("select ua.username as username,id from userid uid, useralias ua where uid.username = ua.username and ua.alias = ?");
               ps.setString(1, alias);
               rs = ps.executeQuery();
               int userid = -1;
               if (rs.next()) {
                  userid = rs.getInt("id");
                  username = rs.getString("username");
               }

               MemCachedHelper.setUserAlias(username, userid, alias);
            } else if (StringUtil.isBlank(username)) {
               var27 = null;
               return var27;
            }

            var27 = username;
         } catch (SQLException var25) {
            throw new EJBException(var25.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var24) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var23) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var22) {
               ch = null;
            }

         }

         return var27;
      }
   }

   public int getUseridByAlias(String alias, Connection conn) throws EJBException {
      if (StringUtil.isBlank(alias)) {
         return -1;
      } else {
         ConnectionHolder ch = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         int var24;
         try {
            Integer userid = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.USER_ID_BY_ALIAS, alias.toLowerCase());
            if (userid == null) {
               ch = new ConnectionHolder(this.dataSourceSlave, conn);
               ps = ch.getConnection().prepareStatement("select ua.username as username,id from userid uid, useralias ua where uid.username = ua.username and ua.alias = ?");
               ps.setString(1, alias);
               rs = ps.executeQuery();
               String username = null;
               userid = -1;
               if (rs.next()) {
                  userid = rs.getInt("id");
                  username = rs.getString("username");
               }

               MemCachedHelper.setUserAlias(username, userid, alias);
            }

            var24 = userid;
         } catch (SQLException var22) {
            throw new EJBException(var22.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var21) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var20) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var19) {
               ch = null;
            }

         }

         return var24;
      }
   }

   public SecurityQuestion getSecurityQuestion(int userid) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      SecurityQuestion sq = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select sq.ID as id, sq.question as question from usersetting us join userid on us.username = userid.username join securityquestion sq on us.value = sq.id where userid.id = ? and us.type = 3 ");
         ps.setInt(1, userid);
         rs = ps.executeQuery();
         if (rs.next()) {
            sq = new SecurityQuestion(rs);
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return sq;
   }

   public String getUserAliasByUserid(int userid, Connection conn) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String username;
      try {
         String alias = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.USER_ALIAS_BY_USERID, "" + userid);
         if (alias == null) {
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
            ps = ch.getConnection().prepareStatement("select ua.username as username,alias from userid uid, useralias ua where uid.username = ua.username and uid.id = ?");
            ps.setInt(1, userid);
            rs = ps.executeQuery();
            username = null;
            if (rs.next()) {
               username = rs.getString("username");
               alias = rs.getString("alias");
            }

            MemCachedHelper.setUserAlias(username, userid, alias);
         } else if (StringUtil.isBlank(alias)) {
            username = null;
            return username;
         }

         username = alias;
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var22) {
            ch = null;
         }

      }

      return username;
   }

   public String getUsernameByUserid(int userid, Connection conn) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var7;
      try {
         String username = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.USER_NAME_BY_ID, "" + userid);
         if (username == null) {
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
            ps = ch.getConnection().prepareStatement("select username from userid where id = ?");
            ps.setInt(1, userid);
            rs = ps.executeQuery();
            if (!rs.next()) {
               var7 = null;
               return var7;
            }

            username = rs.getString("username");
            MemCachedHelper.setUsernameIdMapping(username, userid);
         }

         var7 = username;
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var22) {
            ch = null;
         }

      }

      return var7;
   }

   public boolean isBounceEmailAddress(String recipient) throws FusionEJBException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.CHECK_EMAIL_BOUNCEDB_ENABLED)) {
         return false;
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         boolean var7;
         try {
            conn = this.dataSourceSlave.getConnection();
            String sql = "SELECT count(*) FROM bouncedb WHERE emailaddress = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, recipient);
            rs = ps.executeQuery();
            int result = 0;
            if (rs.next()) {
               result = rs.getInt(1);
            }

            var7 = result != 0;
         } catch (Exception var22) {
            throw new FusionEJBException(var22.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var21) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var20) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var19) {
               conn = null;
            }

         }

         return var7;
      }
   }

   public Map<String, Integer> getEmailUserIdMapping(Collection<String> emailAddresses) throws FusionEJBException {
      Map<String, Integer> emailUserIDMap = new HashMap();
      Iterator ps = emailAddresses.iterator();

      String rs;
      while(ps.hasNext()) {
         rs = (String)ps.next();
         emailUserIDMap.put(StringUtil.normalizeEmailAddress(rs), UNKNOWN_EMAIL_ADDRESS);
      }

      ps = null;
      rs = null;
      Connection conn = null;
      int emailAddressesCount = emailUserIDMap.size();
      int foundExistingUserCount = 0;

      try {
         conn = this.dataSourceSlave.getConnection();

         PreparedStatement ps;
         ResultSet rs;
         try {
            String sql = "select userid, emailaddress from useremailaddress where emailaddress in (%s) group by emailaddress";
            StringBuilder builder = new StringBuilder();

            int paramIdx;
            for(paramIdx = 0; paramIdx < emailAddressesCount; ++paramIdx) {
               if (paramIdx > 0) {
                  builder.append(",");
               }

               builder.append("?");
            }

            ps = conn.prepareStatement(String.format(sql, builder.toString()));

            try {
               paramIdx = 1;

               for(Iterator i$ = emailUserIDMap.keySet().iterator(); i$.hasNext(); ++paramIdx) {
                  String emailAddress = (String)i$.next();
                  ps.setString(paramIdx, emailAddress);
               }

               rs = ps.executeQuery();

               try {
                  while(rs.next()) {
                     emailUserIDMap.put(StringUtil.normalizeEmailAddress(rs.getString("emailaddress")), rs.getInt("userid"));
                     ++foundExistingUserCount;
                  }
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }
         } finally {
            conn.close();
         }

         if (foundExistingUserCount < emailAddressesCount) {
            conn = this.userRegistrationSlave.getConnection();

            try {
               int timelimit = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_TIMELIMIT_IN_MINUTES);
               int gracePeriod = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_GRACE_PERIOD_IN_MINUTES);
               String sql = "(select emailaddress, 1 as type from userregistration where verified = 1 and updatedTime > TIMESTAMPADD(minute," + -1 * (timelimit + gracePeriod) + ",now()) and emailaddress in (%s) ) " + "union" + " (select emailaddress,2 as type from userregistration " + "where verified = 0 and updatedTime > TIMESTAMPADD(minute," + -1 * timelimit + ",now()) and emailaddress in (%s) )";
               StringBuilder builder = new StringBuilder();
               int remainingUnknownEmailAddressCount = emailAddressesCount - foundExistingUserCount;

               int paramIdx;
               for(paramIdx = 0; paramIdx < remainingUnknownEmailAddressCount; ++paramIdx) {
                  if (paramIdx > 0) {
                     builder.append(",");
                  }

                  builder.append("?");
               }

               ps = conn.prepareStatement(String.format(sql, builder.toString(), builder.toString()));

               try {
                  paramIdx = 1;
                  Iterator i$ = emailUserIDMap.entrySet().iterator();

                  Entry emailToUserIDEntry;
                  while(i$.hasNext()) {
                     emailToUserIDEntry = (Entry)i$.next();
                     if (emailToUserIDEntry.getValue() == UNKNOWN_EMAIL_ADDRESS) {
                        ps.setString(paramIdx, (String)emailToUserIDEntry.getKey());
                        ++paramIdx;
                     }
                  }

                  i$ = emailUserIDMap.entrySet().iterator();

                  while(i$.hasNext()) {
                     emailToUserIDEntry = (Entry)i$.next();
                     if (emailToUserIDEntry.getValue() == UNKNOWN_EMAIL_ADDRESS) {
                        ps.setString(paramIdx, (String)emailToUserIDEntry.getKey());
                        ++paramIdx;
                     }
                  }

                  rs = ps.executeQuery();

                  try {
                     while(rs.next()) {
                        emailUserIDMap.put(StringUtil.normalizeEmailAddress(rs.getString("emailaddress")), HALFWAY_REGISTERED_EMAIL_ADDRESS);
                     }
                  } finally {
                     rs.close();
                  }
               } finally {
                  ps.close();
               }
            } finally {
               conn.close();
            }
         }

         return emailUserIDMap;
      } catch (SQLException var73) {
         throw new FusionEJBException(var73.getMessage());
      }
   }

   public CreateInvitationsResult createInvitation(int userId, SendingInvitationData data, Connection conn) throws EJBException, FusionEJBException {
      UserData inviterUserData = this.loadUserFromID(userId);
      if (inviterUserData == null) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNKNOWN_USER, new Object[0]);
      } else if (!InvitationUtils.isUserMobileOrEmailVerified(inviterUserData)) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNVERIFIED_USER, new Object[0]);
      } else {
         InvitationData.ActivityType activityType = InvitationData.ActivityType.fromTypeCode(data.type);
         if (activityType == null) {
            throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_ACTIVITY_TYPE, new Object[]{data.type});
         } else {
            InvitationData.ChannelType channelType = InvitationData.ChannelType.fromTypeCode(data.channel);
            if (channelType == null) {
               throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_CHANNEL_TYPE, new Object[]{data.channel});
            } else if ((activityType == InvitationData.ActivityType.PLAY_A_GAME || activityType == InvitationData.ActivityType.GAME_HELP) && (StringUtil.isBlank(data.invitationMetadata.gameId) || StringUtil.isBlank(data.invitationMetadata.returnUrl))) {
               throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.LACK_OF_INFORMATION_FOR_GAME_INVITATION, new Object[]{data.invitationMetadata.gameId, data.invitationMetadata.returnUrl});
            } else if (activityType == InvitationData.ActivityType.SHARE_PROFILE && StringUtil.isBlank(data.invitationMetadata.sharedUserID)) {
               throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.LACK_OF_INFORMATION_FOR_SHARE_PROFILE_INVITATION, new Object[]{data.invitationMetadata.sharedUserID});
            } else if (channelType == InvitationData.ChannelType.EMAIL) {
               return this.handleEmailChannelForInvitationEngine(activityType, userId, data, conn);
            } else if (channelType == InvitationData.ChannelType.FB) {
               return this.handleFBChannelForInvitationEngine(activityType, userId, data, conn);
            } else if (channelType == InvitationData.ChannelType.INTERNAL) {
               return this.handleInternalChannelForInvitationEngine(activityType, userId, data, conn);
            } else if (channelType == InvitationData.ChannelType.MIGBO) {
               return this.handleMigboChannelForInvitationEngine(activityType, userId, data, conn);
            } else if (channelType == InvitationData.ChannelType.CHAT) {
               return this.handleChatChannelForInvitationEngine(activityType, userId, data, conn);
            } else {
               throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_CHANNEL_TYPE, new Object[]{data.channel});
            }
         }
      }
   }

   private CreateInvitationsResult handleEmailChannelForInvitationEngine(InvitationData.ActivityType activityType, int userId, SendingInvitationData data, Connection conn) throws EJBExceptionWithErrorCause, FusionEJBException {
      if (activityType != InvitationData.ActivityType.JOIN_MIG33 && activityType != InvitationData.ActivityType.PLAY_A_GAME && activityType != InvitationData.ActivityType.GAME_HELP && activityType != InvitationData.ActivityType.SHARE_PROFILE) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_ACTIVITY_TYPE, new Object[]{data.type});
      } else {
         Set<String> invalidEmails = new HashSet();
         CreateInvitationsResult invalidEmailMap = new CreateInvitationsResult();
         Iterator i$ = data.destinations.iterator();

         String emailAddr;
         while(i$.hasNext()) {
            emailAddr = (String)i$.next();
            if (EmailUtils.externalEmailIsValid(emailAddr).result != EmailUtils.EmailValidatationEnum.VALID) {
               invalidEmails.add(emailAddr);
               invalidEmailMap.put(emailAddr, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.INVALID_DESTINATION));
               log.error(String.format("Invalid Email address, Failed to send invitation from userid:%s to %s,", userId, emailAddr));
            }
         }

         i$ = invalidEmails.iterator();

         while(i$.hasNext()) {
            emailAddr = (String)i$.next();
            data.destinations.remove(emailAddr);
         }

         if (data.destinations.size() == 0) {
            return invalidEmailMap;
         } else {
            CreateInvitationsResult result = new CreateInvitationsResult();
            result.putAll(invalidEmailMap);
            Map<String, Integer> emailUserIdMap = this.getEmailUserIdMapping(data.destinations);
            ArrayList<String> externalUsersEmails = new ArrayList();
            ArrayList<String> existingUsersEmails = new ArrayList();
            ArrayList<String> existingUserIds = new ArrayList();
            ArrayList<String> halfWayRegisterUserEmails = new ArrayList();
            Iterator i$ = emailUserIdMap.entrySet().iterator();

            String inviteeId;
            while(i$.hasNext()) {
               Entry<String, Integer> emailUserIDEntry = (Entry)i$.next();
               inviteeId = (String)emailUserIDEntry.getKey();
               Integer existingUserId = (Integer)emailUserIDEntry.getValue();
               if (existingUserId > 0) {
                  existingUserIds.add(existingUserId + "");
                  existingUsersEmails.add(inviteeId);
                  result.put(inviteeId, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.SEND_FOLLOWING_ME_REQUEST_TO_EXISTING_USER, existingUserId));
               } else if (existingUserId == HALFWAY_REGISTERED_EMAIL_ADDRESS) {
                  halfWayRegisterUserEmails.add(inviteeId);
                  result.put(inviteeId, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.NOTHING_HAPPENS_TO_HALFWAY_REGISTERED_USER));
               } else if (existingUserId == UNKNOWN_EMAIL_ADDRESS) {
                  externalUsersEmails.add(inviteeId);
                  result.put(inviteeId, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.SUCCESS_SEND_INVITATION));
               }
            }

            Map emailIdToInvitationIdMap;
            String inviteeId;
            Iterator i$;
            if (activityType == InvitationData.ActivityType.JOIN_MIG33) {
               i$ = existingUserIds.iterator();

               while(i$.hasNext()) {
                  inviteeId = (String)i$.next();
                  this.triggerFollowingAnUserAndSendingFollowingMeRequest(userId, Integer.valueOf(inviteeId));
               }
            } else if (activityType != InvitationData.ActivityType.PLAY_A_GAME && activityType != InvitationData.ActivityType.GAME_HELP) {
               if (activityType == InvitationData.ActivityType.SHARE_PROFILE) {
                  i$ = existingUserIds.iterator();

                  while(i$.hasNext()) {
                     inviteeId = (String)i$.next();
                     this.triggerFollowingAnUserAndSendingFollowingMeRequest(userId, Integer.valueOf(inviteeId));
                  }

                  externalUsersEmails.addAll(existingUsersEmails);
               }
            } else {
               emailIdToInvitationIdMap = this.doCreateInvitation(existingUserIds, userId, data.type, InvitationData.ChannelType.INTERNAL.getTypeCode());
               this.doCreateMetadataForInvitation(emailIdToInvitationIdMap.values(), data.invitationMetadata);
               i$ = existingUserIds.iterator();

               label98:
               while(true) {
                  while(true) {
                     if (!i$.hasNext()) {
                        break label98;
                     }

                     inviteeId = (String)i$.next();
                     if (emailIdToInvitationIdMap.containsKey(inviteeId)) {
                        this.triggerSendGameInvitationNotification(userId, Integer.valueOf(inviteeId), data, (Integer)emailIdToInvitationIdMap.get(inviteeId), activityType);
                     } else {
                        int inviteeIdInt = Integer.valueOf(inviteeId);
                        Iterator i$ = emailUserIdMap.keySet().iterator();

                        while(i$.hasNext()) {
                           String key = (String)i$.next();
                           if ((Integer)emailUserIdMap.get(key) == inviteeIdInt) {
                              result.put(key, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.FAILED_TO_SEND_INVITATION_TO_DESTINATION));
                              this.triggerSendGameInvitationNotification(userId, Integer.valueOf(inviteeId), data, -1, activityType);
                              break;
                           }
                        }
                     }
                  }
               }
            }

            if (externalUsersEmails.size() == 0) {
               return result;
            } else {
               emailIdToInvitationIdMap = this.doCreateInvitation(externalUsersEmails, userId, data.type, data.channel);
               this.doCreateMetadataForInvitation(emailIdToInvitationIdMap.values(), data.invitationMetadata);
               i$ = externalUsersEmails.iterator();

               while(i$.hasNext()) {
                  inviteeId = (String)i$.next();
                  CreateInvitationsResult.CreateInvitationDetails createInvitationDetails = (CreateInvitationsResult.CreateInvitationDetails)result.get(inviteeId);
                  if (emailIdToInvitationIdMap.containsKey(inviteeId)) {
                     createInvitationDetails.invitationID = (Integer)emailIdToInvitationIdMap.get(inviteeId);
                     createInvitationDetails.sendInvitationResult = InvitationUtils.SendInvitationResultEnum.SUCCESS_SEND_INVITATION;
                  } else {
                     createInvitationDetails.invitationID = -1;
                     createInvitationDetails.sendInvitationResult = InvitationUtils.SendInvitationResultEnum.FAILED_TO_SEND_INVITATION_TO_DESTINATION;
                  }
               }

               InvitationUtils.sendInvitationEmails(userId, emailIdToInvitationIdMap, data, activityType);
               return result;
            }
         }
      }
   }

   private CreateInvitationsResult handleFBChannelForInvitationEngine(InvitationData.ActivityType activityType, int userId, SendingInvitationData data, Connection conn) throws EJBExceptionWithErrorCause, FusionEJBException {
      if (activityType != InvitationData.ActivityType.SHARE_PROFILE && activityType != InvitationData.ActivityType.JOIN_MIG33) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_ACTIVITY_TYPE, new Object[]{activityType});
      } else {
         CreateInvitationsResult result = new CreateInvitationsResult();
         Map<String, Integer> facebookUserIdToInvitationIdMap = this.doCreateInvitation(data.destinations, userId, data.type, data.channel);
         this.doCreateMetadataForInvitation(facebookUserIdToInvitationIdMap.values(), data.invitationMetadata);
         Iterator i$ = data.destinations.iterator();

         while(true) {
            while(i$.hasNext()) {
               String facebookUserId = (String)i$.next();
               if (facebookUserIdToInvitationIdMap.containsKey(facebookUserId) && (Integer)facebookUserIdToInvitationIdMap.get(facebookUserId) > 0) {
                  result.put(facebookUserId, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.SUCCESS_SEND_INVITATION));
               } else {
                  result.put(facebookUserId, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.FAILED_TO_SEND_INVITATION_TO_DESTINATION));
               }
            }

            return result;
         }
      }
   }

   private CreateInvitationsResult handleInternalChannelForInvitationEngine(InvitationData.ActivityType activityType, int userId, SendingInvitationData data, Connection conn) throws EJBExceptionWithErrorCause, FusionEJBException {
      if (activityType != InvitationData.ActivityType.PLAY_A_GAME && activityType != InvitationData.ActivityType.GAME_HELP) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_ACTIVITY_TYPE, new Object[]{activityType});
      } else {
         CreateInvitationsResult result = new CreateInvitationsResult();
         Map<String, Integer> existingUserIdToInvitationIdMap = this.doCreateInvitation(data.destinations, userId, data.type, data.channel);
         this.doCreateMetadataForInvitation(existingUserIdToInvitationIdMap.values(), data.invitationMetadata);
         Iterator i$ = data.destinations.iterator();

         while(true) {
            while(i$.hasNext()) {
               String des = (String)i$.next();
               if (existingUserIdToInvitationIdMap.containsKey(des) && (Integer)existingUserIdToInvitationIdMap.get(des) > 0) {
                  result.put(des, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.SUCCESS_SEND_INVITATION));
                  this.triggerSendGameInvitationNotification(userId, Integer.valueOf(des), data, (Integer)existingUserIdToInvitationIdMap.get(des), activityType);
               } else {
                  result.put(des, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.FAILED_TO_SEND_INVITATION_TO_DESTINATION));
                  this.triggerSendGameInvitationNotification(userId, Integer.valueOf(des), data, -1, activityType);
               }
            }

            return result;
         }
      }
   }

   private CreateInvitationsResult handleMigboChannelForInvitationEngine(InvitationData.ActivityType activityType, int userId, SendingInvitationData data, Connection conn) throws EJBExceptionWithErrorCause, FusionEJBException {
      if (activityType != InvitationData.ActivityType.SHARE_PROFILE) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_ACTIVITY_TYPE, new Object[]{activityType});
      } else {
         Map<String, Integer> destinationToInvitationIdMap = this.doCreateInvitation(data.destinations, userId, data.type, data.channel);
         this.doCreateMetadataForInvitation(destinationToInvitationIdMap.values(), data.invitationMetadata);
         CreateInvitationsResult result = new CreateInvitationsResult();
         Iterator i$ = data.destinations.iterator();

         while(true) {
            while(i$.hasNext()) {
               String des = (String)i$.next();
               if (destinationToInvitationIdMap.containsKey(des) && (Integer)destinationToInvitationIdMap.get(des) > 0) {
                  result.put(des, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.SUCCESS_SEND_INVITATION));
               } else {
                  result.put(des, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.FAILED_TO_SEND_INVITATION_TO_DESTINATION));
               }
            }

            return result;
         }
      }
   }

   private CreateInvitationsResult handleChatChannelForInvitationEngine(InvitationData.ActivityType activityType, int userId, SendingInvitationData data, Connection conn) throws EJBExceptionWithErrorCause, FusionEJBException {
      if (activityType != InvitationData.ActivityType.SHARE_PROFILE) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_ACTIVITY_TYPE, new Object[]{activityType});
      } else {
         Map<String, Integer> destinationToInvitationIdMap = this.doCreateInvitation(data.destinations, userId, data.type, data.channel);
         this.doCreateMetadataForInvitation(destinationToInvitationIdMap.values(), data.invitationMetadata);
         CreateInvitationsResult result = new CreateInvitationsResult();
         Iterator i$ = data.destinations.iterator();

         while(true) {
            while(i$.hasNext()) {
               String des = (String)i$.next();
               if (destinationToInvitationIdMap.containsKey(des) && (Integer)destinationToInvitationIdMap.get(des) > 0) {
                  result.put(des, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.SUCCESS_SEND_INVITATION));
               } else {
                  result.put(des, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.FAILED_TO_SEND_INVITATION_TO_DESTINATION));
               }
            }

            return result;
         }
      }
   }

   public CreateInvitationsResult createInvitationForFBInvite(int userId, SendingInvitationData data, Connection conn) {
      if (InvitationData.ChannelType.fromTypeCode(data.channel) != InvitationData.ChannelType.FB) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_CHANNEL_TYPE, new Object[]{data.channel});
      } else if (InvitationData.ActivityType.fromTypeCode(data.type) != InvitationData.ActivityType.JOIN_MIG33) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNSUPPORTED_ACTIVITY_TYPE, new Object[]{data.type});
      } else {
         UserData inviterUserData = this.loadUserFromID(userId);
         if (inviterUserData == null) {
            throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNKNOWN_USER, new Object[0]);
         } else if (!InvitationUtils.isUserMobileOrEmailVerified(inviterUserData)) {
            throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.UNVERIFIED_USER, new Object[0]);
         } else {
            CreateInvitationsResult result = new CreateInvitationsResult();
            Map<String, Integer> facebookUserIdToInvitationIdMap = this.doCreateInvitation(data.destinations, userId, data.type, data.channel);
            this.doCreateMetadataForInvitation(facebookUserIdToInvitationIdMap.values(), data.invitationMetadata);
            Iterator i$ = data.destinations.iterator();

            while(true) {
               while(i$.hasNext()) {
                  String facebookUserId = (String)i$.next();
                  if (facebookUserIdToInvitationIdMap.containsKey(facebookUserId) && (Integer)facebookUserIdToInvitationIdMap.get(facebookUserId) > 0) {
                     result.put(facebookUserId, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.SUCCESS_SEND_INVITATION));
                  } else {
                     result.put(facebookUserId, new CreateInvitationsResult.CreateInvitationDetails(InvitationUtils.SendInvitationResultEnum.FAILED_TO_SEND_INVITATION_TO_DESTINATION));
                  }
               }

               return result;
            }
         }
      }
   }

   private Map<String, Integer> doCreateInvitation(List<String> destinations, int userId, int type, int channel) {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HashMap var57;
      try {
         Map<String, Integer> destinationToInvitationIdMap = new HashMap();
         List<Integer> generatedInvitationIDs = new ArrayList();
         ch = new ConnectionHolder(this.dataSourceMaster, (Connection)null);
         ps = ch.getConnection().prepareStatement("insert into invitation(inviterUserId,type,channel,destination,status,expireTime) values(?,?,?,?,?,?)", new String[]{"id"});

         String destination;
         try {
            int expireDays = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.REFERRAL_EXPIRE_PERIOD_IN_DAY);
            Date expireDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expireDate);
            calendar.add(5, expireDays);
            expireDate = calendar.getTime();
            Iterator i$ = destinations.iterator();

            while(i$.hasNext()) {
               destination = (String)i$.next();
               ps.setInt(1, userId);
               ps.setInt(2, type);
               ps.setInt(3, channel);
               ps.setString(4, destination);
               ps.setInt(5, InvitationData.StatusFieldValue.NO_RESPONSE.getTypeCode());
               ps.setString(6, dateFormate.format(expireDate));
               ps.addBatch();
            }

            ps.executeBatch();
            rs = ps.getGeneratedKeys();

            try {
               while(rs.next()) {
                  generatedInvitationIDs.add(rs.getInt(1));
               }
            } finally {
               rs.close();
            }
         } finally {
            ps.close();
         }

         boolean needDataBaseLookUp = false;
         if (generatedInvitationIDs.size() == destinations.size()) {
            for(int i = 0; i < destinations.size(); ++i) {
               if ((Integer)generatedInvitationIDs.get(i) <= 0) {
                  destinationToInvitationIdMap.clear();
                  needDataBaseLookUp = true;
                  break;
               }

               destinationToInvitationIdMap.put(destinations.get(i), generatedInvitationIDs.get(i));
            }
         } else {
            needDataBaseLookUp = true;
         }

         if (needDataBaseLookUp) {
            String retrieveIdToDestinationSQL = "select id, destination from invitation where id in (%s)";
            StringBuilder retrieveIdToDestinationSQLParamPlaceHolder = new StringBuilder();
            int generatedInvitationIDCount = generatedInvitationIDs.size();

            int i;
            for(i = 0; i < generatedInvitationIDCount; ++i) {
               if (i > 0) {
                  retrieveIdToDestinationSQLParamPlaceHolder.append(",");
               }

               retrieveIdToDestinationSQLParamPlaceHolder.append("?");
            }

            ps = ch.getConnection().prepareStatement(String.format(retrieveIdToDestinationSQL, retrieveIdToDestinationSQLParamPlaceHolder.toString()));

            for(i = 0; i < generatedInvitationIDCount; ++i) {
               ps.setInt(i + 1, (Integer)generatedInvitationIDs.get(i));
            }

            rs = ps.executeQuery();

            while(rs.next()) {
               destination = rs.getString("destination");
               Integer invitationId = rs.getInt("id");
               destinationToInvitationIdMap.put(destination, invitationId);
            }
         }

         var57 = destinationToInvitationIdMap;
      } catch (SQLException var51) {
         throw new EJBException(var51.getMessage(), var51);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var48) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var47) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var46) {
            ch = null;
         }

      }

      return var57;
   }

   private void doCreateMetadataForInvitation(Collection<Integer> invitationIds, InvitationMetadata invitationMetadata) {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      if (invitationMetadata != null) {
         try {
            ch = new ConnectionHolder(this.dataSourceMaster, (Connection)null);
            ps = ch.getConnection().prepareStatement("insert into invitationparameters(invitationId,type,value) values(?,?,?)");
            Iterator i$ = invitationIds.iterator();

            while(i$.hasNext()) {
               Integer invitationId = (Integer)i$.next();
               if (!StringUtil.isBlank(invitationMetadata.returnUrl)) {
                  ps.setInt(1, invitationId);
                  ps.setInt(2, InvitationData.ParamType.RETURN_URL.getTypeCode());
                  ps.setString(3, invitationMetadata.returnUrl);
                  ps.addBatch();
               }

               if (!StringUtil.isBlank(invitationMetadata.facebookRequestId)) {
                  ps.setInt(1, invitationId);
                  ps.setInt(2, InvitationData.ParamType.FACEBOOK_REQUEST_ID.getTypeCode());
                  ps.setString(3, invitationMetadata.facebookRequestId);
                  ps.addBatch();
               }

               if (!StringUtil.isBlank(invitationMetadata.gameId)) {
                  ps.setInt(1, invitationId);
                  ps.setInt(2, InvitationData.ParamType.GAMEID.getTypeCode());
                  ps.setString(3, invitationMetadata.gameId);
                  ps.addBatch();
               }
            }

            ps.executeBatch();
         } catch (SQLException var18) {
            throw new EJBException(var18.getMessage(), var18);
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var17) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var16) {
               ch = null;
            }

         }
      }
   }

   public InvitationData getInvitationData(int invitationID, boolean loadExtraParameters, Connection conn) {
      if (invitationID <= 0) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.INVITATION_ID_OUT_OF_RANGE, new Object[0]);
      } else {
         ConnectionHolder ch = new ConnectionHolder(this.dataSourceSlave, conn);

         InvitationData var38;
         try {
            String sql = loadExtraParameters ? "SELECT inv.channel, inv.createdTime,  inv.destination, inv.expireTime, inv.id, inv.inviterUserId, inv.status, inv.type,  invparam.invitationId as invparamInvitationId,  invparam.type as invparamType,  invparam.value as invparamValue  FROM invitation inv LEFT JOIN invitationparameters invparam ON (inv.id = invparam.invitationId)  WHERE inv.id = ?" : "SELECT inv.channel, inv.createdTime,  inv.destination, inv.expireTime, inv.id, inv.inviterUserId, inv.status, inv.type FROM Invitation inv  WHERE inv.id = ?";
            PreparedStatement ps = ch.getConnection().prepareStatement(sql);

            try {
               ps.setInt(1, invitationID);
               ResultSet rs = ps.executeQuery();

               try {
                  InvitationData invitationData = null;
                  if (rs.next()) {
                     invitationData = new InvitationData();
                     invitationData.initializeMainData(rs);
                     if (loadExtraParameters) {
                        Object invparamInvitationIdObj = rs.getObject("invparamInvitationId");
                        if (invparamInvitationIdObj != null) {
                           invitationData.addParameters(rs);
                        }

                        while(rs.next()) {
                           invitationData.addParameters(rs);
                        }
                     }
                  }

                  var38 = invitationData;
               } finally {
                  rs.close();
               }
            } finally {
               ps.close();
            }
         } catch (SQLException var36) {
            throw new EJBException("getInvitation with loadExtraParameters=" + loadExtraParameters, var36);
         } finally {
            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var33) {
               ch = null;
            }

         }

         return var38;
      }
   }

   public ThirdPartyApplicationData getThirdPartyApplicationData(int thirdPartyID, Connection conn) {
      if (thirdPartyID <= 0) {
         throw new EJBExceptionWithErrorCause(ErrorCause.ThirdPartyApplicationErrorReasonType.THIRD_PARTY_ID_OUT_OF_RANGE, new Object[0]);
      } else {
         ConnectionHolder ch = new ConnectionHolder(this.dataSourceSlave, conn);
         Jedis handle = null;

         ThirdPartyApplicationData var7;
         try {
            handle = Redis.getGamesMasterInstance();
            String redisKey = ThirdPartyAppHelper.getThirdPartyAppKey(thirdPartyID);
            String jsonStr = handle.get(redisKey);
            if (jsonStr == null) {
               String sql = "select t.*, g.picture, tv.view from thirdpartyapplication as t join groups as g on t.groupid = g.id join thirdpartyapplicationview as tv on t.id = tv.ThirdPartyApplicationID where t.id = ? and t.status = 1 and tv.status = 1;";
               PreparedStatement ps = ch.getConnection().prepareStatement("select t.*, g.picture, tv.view from thirdpartyapplication as t join groups as g on t.groupid = g.id join thirdpartyapplicationview as tv on t.id = tv.ThirdPartyApplicationID where t.id = ? and t.status = 1 and tv.status = 1;");

               try {
                  ps.setInt(1, thirdPartyID);
                  ResultSet rs = ps.executeQuery();

                  try {
                     ThirdPartyApplicationData thirdPartyApplicationData = null;
                     if (rs.next()) {
                        thirdPartyApplicationData = new ThirdPartyApplicationData();
                        thirdPartyApplicationData.initializeMainData(rs);
                        thirdPartyApplicationData.addView(rs);

                        while(rs.next()) {
                           thirdPartyApplicationData.addView(rs);
                        }

                        handle.set(redisKey, ThirdPartyAppHelper.toJsonString(thirdPartyApplicationData));
                     }

                     ThirdPartyApplicationData var11 = thirdPartyApplicationData;
                     return var11;
                  } finally {
                     rs.close();
                  }
               } finally {
                  ps.close();
               }
            }

            var7 = ThirdPartyAppHelper.fromJson(jsonStr);
         } catch (SQLException var42) {
            log.error(String.format("Failed to retrieve ThirdPartyApplicationData for ThirdPartyApplicationData ID:%s", thirdPartyID), var42);
            throw new EJBException(String.format("Failed to retrieve ThirdPartyApplicationData for ThirdPartyApplicationData ID:%s", thirdPartyID), var42);
         } catch (Exception var43) {
            log.error(String.format("Failed to retrieve ThirdPartyApplicationData for ThirdPartyApplicationData ID:%s", thirdPartyID), var43);
            throw new EJBException(String.format("Failed to retrieve ThirdPartyApplicationData for ThirdPartyApplicationData ID:%s", thirdPartyID), var43);
         } finally {
            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (Exception var39) {
               handle = null;
            }

            Redis.disconnect(handle, log);
         }

         return var7;
      }
   }

   public InvitationData getInvitationDataForFBInvite(String facebookRequestId, String facebookUserId, boolean fetchExtraParams, Connection conn) {
      int invitationId = this.getInvitationIdForFBInvite(facebookRequestId, facebookUserId, conn);
      return this.getInvitationData(invitationId, fetchExtraParams, conn);
   }

   private int getInvitationIdForFBInvite(String facebookRequestId, String facebookUserId, Connection conn) {
      try {
         if (Long.valueOf(facebookRequestId) <= 0L || Long.valueOf(facebookUserId) <= 0L) {
            throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.INVALID_FACEBOOK_REQUEST_ID_OR_FACEBOOKUSERID, new Object[0]);
         }
      } catch (Exception var41) {
         throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.INVALID_FACEBOOK_REQUEST_ID_OR_FACEBOOKUSERID, new Object[0]);
      }

      ConnectionHolder ch = new ConnectionHolder(this.dataSourceSlave, conn);

      int var9;
      try {
         String sql = "SELECT invitation.id as id FROM invitation JOIN invitationparameters on invitation.id = invitationparameters.invitationId WHERE invitation.destination = ? AND invitationparameters.type = ? AND invitationparameters.value = ?";
         PreparedStatement ps = ch.getConnection().prepareStatement("SELECT invitation.id as id FROM invitation JOIN invitationparameters on invitation.id = invitationparameters.invitationId WHERE invitation.destination = ? AND invitationparameters.type = ? AND invitationparameters.value = ?");

         try {
            ps.setString(1, facebookUserId);
            ps.setInt(2, InvitationData.ParamType.FACEBOOK_REQUEST_ID.getEnumValue());
            ps.setString(3, facebookRequestId);
            ResultSet rs = ps.executeQuery();

            try {
               int invitationId = -1;
               if (rs.next()) {
                  invitationId = rs.getInt("id");
               }

               var9 = invitationId;
            } finally {
               rs.close();
            }
         } finally {
            ps.close();
         }
      } catch (SQLException var39) {
         throw new EJBException(var39.getMessage(), var39);
      } finally {
         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var36) {
            ch = null;
         }

      }

      return var9;
   }

   public InvitationResponseData logInvitationResponse(Connection conn, Date actionTime, InvitationData invitationData, InvitationResponseData.ResponseType responseType, String username, InvitationData.StatusFieldValue newStatusFieldValue) {
      if (responseType == null) {
         throw new IllegalArgumentException("responseType cannot be null");
      } else if (StringUtil.isBlank(username)) {
         throw new IllegalArgumentException("username cannot be null");
      } else {
         int invitationId = invitationData.id;
         Timestamp actionTimestamp = new Timestamp(actionTime.getTime());
         InvitationResponseData invitationResponseData = new InvitationResponseData();
         invitationResponseData.id = -1L;
         invitationResponseData.invitationId = invitationId;
         invitationResponseData.responseTime = actionTime;
         invitationResponseData.responseType = responseType;
         invitationResponseData.username = username;

         try {
            ConnectionHolder ch = new ConnectionHolder(this.dataSourceMaster, conn);

            try {
               String insertSql;
               PreparedStatement ps;
               int i;
               if (newStatusFieldValue != null) {
                  insertSql = "UPDATE invitation SET status = ? WHERE id=? AND status = ? AND (expireTime IS NULL OR expireTime > ?)";
                  ps = ch.getConnection().prepareStatement("UPDATE invitation SET status = ? WHERE id=? AND status = ? AND (expireTime IS NULL OR expireTime > ?)");

                  try {
                     ps.setInt(1, newStatusFieldValue.getTypeCode());
                     ps.setInt(2, invitationId);
                     ps.setInt(3, InvitationData.StatusFieldValue.NO_RESPONSE.getTypeCode());
                     ps.setTimestamp(4, actionTimestamp);
                     int updateCount = ps.executeUpdate();
                     if (updateCount != 1) {
                        throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.INVITATION_STATUS_CHANGE_NOT_ALLOWED, new Object[]{invitationId});
                     }

                     invitationData.status = newStatusFieldValue;
                     this.postLogInvitationResponse(invitationData, username, invitationResponseData);
                  } finally {
                     ps.close();
                  }
               } else {
                  insertSql = "SELECT status FROM invitation WHERE id=? AND status = ? AND (expireTime IS NULL OR expireTime > ?)";
                  ps = ch.getConnection().prepareStatement("SELECT status FROM invitation WHERE id=? AND status = ? AND (expireTime IS NULL OR expireTime > ?)");

                  try {
                     ps.setInt(1, invitationId);
                     ps.setInt(2, InvitationData.StatusFieldValue.NO_RESPONSE.getTypeCode());
                     ps.setTimestamp(3, actionTimestamp);
                     ResultSet rs = ps.executeQuery();

                     try {
                        if (!rs.next()) {
                           throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.LOG_INVITATION_RESPONSE_NOT_ALLOWED, new Object[]{invitationId});
                        }

                        i = rs.getInt(1);
                        if (InvitationData.StatusFieldValue.fromTypeCode(i) != InvitationData.StatusFieldValue.NO_RESPONSE) {
                           throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.LOG_INVITATION_RESPONSE_NOT_ALLOWED, new Object[]{invitationId});
                        }
                     } finally {
                        rs.close();
                     }
                  } finally {
                     ps.close();
                  }
               }

               insertSql = "INSERT INTO INVITATIONRESPONSE(invitationId,responseTime,responseType,username) VALUES (?,?,?,?)";
               ps = ch.getConnection().prepareStatement("INSERT INTO INVITATIONRESPONSE(invitationId,responseTime,responseType,username) VALUES (?,?,?,?)", 1);

               try {
                  ps.setInt(1, invitationId);
                  ps.setTimestamp(2, actionTimestamp);
                  ps.setInt(3, responseType.getTypeCode());
                  ps.setString(4, username);
                  ps.addBatch();
                  if (responseType == InvitationResponseData.ResponseType.SIGN_UP_VERIFIED && invitationData.type != InvitationData.ActivityType.JOIN_MIG33) {
                     ps.setInt(1, invitationId);
                     ps.setTimestamp(2, actionTimestamp);
                     ps.setInt(3, InvitationResponseData.ResponseType.ACCEPT_INVITATION.getTypeCode());
                     ps.setString(4, username);
                     ps.addBatch();
                  }

                  int[] batchResult = ps.executeBatch();

                  for(i = 0; i < batchResult.length; ++i) {
                     if (batchResult[i] != 1) {
                        throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.LOG_INVITATION_RESPONSE_FAILED, new Object[]{responseType, username, invitationId});
                     }
                  }

                  ResultSet rs = ps.getGeneratedKeys();

                  try {
                     if (!rs.next()) {
                        throw new EJBExceptionWithErrorCause(ErrorCause.InvitationErrorReasonType.LOG_INVITATION_RESPONSE_FAILED, new Object[]{responseType, username, invitationId});
                     }

                     invitationResponseData.id = rs.getLong(1);
                  } finally {
                     rs.close();
                  }
               } finally {
                  ps.close();
               }
            } finally {
               ch.close();
            }

            return invitationResponseData;
         } catch (SQLException var74) {
            throw new EJBExceptionWithErrorCause(var74, ErrorCause.InvitationErrorReasonType.LOG_INVITATION_RESPONSE_FAILED, new Object[]{responseType, username, invitationId});
         }
      }
   }

   private void postLogInvitationResponse(InvitationData invitationData, String inviteeUserName, InvitationResponseData invitationResponseData) {
      int inviterId = invitationData.inviterUserId;
      UserData inviterUserData = this.loadUserFromID(inviterId);
      boolean loadInviteeFromMaster = (Boolean)SystemPropertyEntities.Temp.Cache.se423LoadInviteeUsesMasterDB.getValue();
      if (log.isDebugEnabled()) {
         log.debug("postLogInvitationResponse:loadInviteeFromMasterDB:" + loadInviteeFromMaster);
      }

      UserData inviteeUserData = this.loadUser(inviteeUserName, false, loadInviteeFromMaster);
      if (invitationResponseData.responseType == InvitationResponseData.ResponseType.ACCEPT_INVITATION) {
         this.sendMigAlertToInviterWhenInviteeAcceptInvitation(inviterUserData, inviteeUserData);
      } else if (invitationResponseData.responseType == InvitationResponseData.ResponseType.SIGN_UP_VERIFIED || invitationResponseData.responseType == InvitationResponseData.ResponseType.LOGIN_USING_EXISTING_ACCOUNT) {
         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.ENABLE_REFERRAL_ACK_EMAIL)) {
               MigboApiUtil apiUtil = MigboApiUtil.getInstance();
               String pathPrefix = String.format("/user/%s/email/%s?follower=%s", inviterUserData.userID, Enums.EmailTypeEnum.REFERRAL_ACK.value(), inviteeUserData.username);
               String postData = "{}";
               if (!apiUtil.postAndCheckOk(pathPrefix, postData)) {
                  log.warn(String.format("Failed to send referral ack email to inviter:%s, invitee:%s", inviterUserData.userID, inviteeUserData.userID));
               }
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.ENABLE_MUTUAL_FOLLOWING_FOR_ALL_INVITERS)) {
               this.addMutualFollowingAndTriggerMigAlertsToAllInviters(invitationData, inviteeUserData);
            } else {
               this.addMutualFollowingAndTriggerMigAlerts(inviterUserData, inviteeUserData);
            }
         } catch (Exception var11) {
            log.error("addMutualFollowingAndTriggerMigAlerts failed for inviter userId:[" + invitationData.inviterUserId + "], invitee username [" + inviteeUserData.username + "].Error message:" + var11.getMessage(), var11);
         }
      }

      this.triggerMarketingMechanicsForInvitation(inviterUserData, inviteeUserData, invitationData, invitationResponseData);
   }

   private void triggerMarketingMechanicsForInvitation(UserData inviterUserData, UserData inviteeUserData, InvitationData invitationData, InvitationResponseData invitationResponseData) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.MARKETING_MECHANICS_ENABLED)) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER78_ENABLED) || UserReferrerCache.isWithinCapAllowed(inviterUserData, inviteeUserData)) {
            if (invitationResponseData.responseType == InvitationResponseData.ResponseType.SIGN_UP_VERIFIED && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.MARKETING_MECHANICS_FOR_REFERRAL_ENABLED)) {
               InvitationRespondedTrigger trigger;
               try {
                  trigger = new InvitationRespondedTrigger(inviteeUserData, false, invitationData, invitationResponseData, inviterUserData);
                  this.populateTriggerWithCampaignParticipationInfo(trigger, CampaignData.TypeEnum.INVITE_FRIENDS_TO_SIGN_UP);
                  RewardCentre.getInstance().sendTrigger(trigger);
               } catch (Exception var13) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var13, var13);
               }

               try {
                  trigger = new InvitationRespondedTrigger(inviterUserData, true, invitationData, invitationResponseData, inviteeUserData);
                  this.populateTriggerWithCampaignParticipationInfo(trigger, CampaignData.TypeEnum.INVITE_FRIENDS_TO_SIGN_UP);
                  RewardCentre.getInstance().sendTrigger(trigger);
               } catch (Exception var12) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var12, var12);
               }
            }

            if (invitationResponseData.responseType == InvitationResponseData.ResponseType.ACCEPT_INVITATION && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.MARKETING_MECHANICS_FOR_ACCEPT_INVITATION_ENABLED)) {
               try {
                  RewardCentre.getInstance().sendTrigger(new InvitationRespondedTrigger(inviteeUserData, false, invitationData, invitationResponseData, inviterUserData));
               } catch (Exception var11) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var11, var11);
               }

               try {
                  RewardCentre.getInstance().sendTrigger(new InvitationRespondedTrigger(inviterUserData, true, invitationData, invitationResponseData, inviteeUserData));
               } catch (Exception var10) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var10, var10);
               }
            }

            if (invitationResponseData.responseType == InvitationResponseData.ResponseType.LOGIN_USING_EXISTING_ACCOUNT && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.MARKETING_MECHANICS_FOR_LOGIN_USING_EXISTING_ACCOUNT_ENABLED)) {
               try {
                  RewardCentre.getInstance().sendTrigger(new InvitationRespondedTrigger(inviteeUserData, false, invitationData, invitationResponseData, inviterUserData));
               } catch (Exception var9) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var9, var9);
               }

               try {
                  RewardCentre.getInstance().sendTrigger(new InvitationRespondedTrigger(inviterUserData, true, invitationData, invitationResponseData, inviteeUserData));
               } catch (Exception var8) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var8, var8);
               }
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.MARKETING_MECHANICS_CHAIN_ENABLED) && invitationResponseData.responseType == InvitationResponseData.ResponseType.SIGN_UP_VERIFIED && invitationData.type != InvitationData.ActivityType.JOIN_MIG33) {
               try {
                  RewardCentre.getInstance().sendTrigger(new InvitationRespondedTrigger(inviteeUserData, false, invitationData, invitationResponseData, inviterUserData));
               } catch (Exception var7) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var7, var7);
               }

               try {
                  RewardCentre.getInstance().sendTrigger(new InvitationRespondedTrigger(inviterUserData, true, invitationData, invitationResponseData, inviteeUserData));
               } catch (Exception var6) {
                  log.error("Unable to notify reward system to send InvitationRespondedTrigger." + var6, var6);
               }
            }

         }
      }
   }

   private void populateTriggerWithCampaignParticipationInfo(InvitationRespondedTrigger trigger, CampaignData.TypeEnum campaignType) throws DAOException {
      int userId;
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se504Enabled.getValue()) {
         userId = trigger.userData.userID;
         List<CampaignParticipantData> participationDataList = DAOFactory.getInstance().getCampaignDAO().getActiveCampaignParticipantDataByType(userId, campaignType.getEnumValue());
         if (log.isDebugEnabled()) {
            log.debug("UserId=[" + userId + "];isInviter=[" + trigger.isInviter() + "];participationDataList.size=[" + participationDataList.size() + "];participationDataList=[" + participationDataList + "]");
         }

         Map<Integer, CampaignParticipation> participatedCampaigns = trigger.getParticipatedCampaigns();
         if (participationDataList != null) {
            Iterator i$ = participationDataList.iterator();

            while(i$.hasNext()) {
               CampaignParticipantData participantData = (CampaignParticipantData)i$.next();
               participatedCampaigns.put(participantData.getCampaignId(), participantData);
            }
         }
      } else if (log.isDebugEnabled()) {
         userId = trigger.userData.userID;
         log.debug("Campaign participation data extraction disabled for userId [" + userId + "]");
      }

   }

   public int getUserID(String username, Connection conn) throws EJBException {
      return this.getUserID(username, conn, true);
   }

   public int getUserID(String username, Connection conn, boolean throwExceptionWhenNotFound) throws EJBException {
      if (StringUtil.isBlank(username)) {
         if (throwExceptionWhenNotFound) {
            throw new EJBException("Invalid username " + username);
         } else {
            return -1;
         }
      } else {
         ConnectionHolder ch = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            Integer userID = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.USER_ID, username.toLowerCase());
            if (userID == null) {
               ch = new ConnectionHolder(this.dataSourceSlave, conn);
               ps = ch.getConnection().prepareStatement("select id from userid where username = ?");
               ps.setString(1, username);
               rs = ps.executeQuery();
               if (!rs.next()) {
                  if (throwExceptionWhenNotFound) {
                     throw new EJBException("Invalid username " + username);
                  }

                  byte var28 = -1;
                  return var28;
               }

               userID = rs.getInt("id");
               MemCachedHelper.setUsernameIdMapping(username, userID);
            }

            int var8 = userID;
            return var8;
         } catch (SQLException var26) {
            throw new EJBException(var26.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var25) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var24) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var23) {
               ch = null;
            }

         }
      }
   }

   public Map<String, Integer> getLastLoggedInUsersWithVerifiedEmail(UserEmailAddressData.UserEmailAddressTypeEnum emailType, Timestamp minimum, Timestamp maximum, Connection conn) throws EJBException {
      if (minimum.after(maximum)) {
         throw new EJBException(String.format("Minimum Date [%s] should be less than Maximum Date [%s]", minimum, maximum));
      } else {
         ConnectionHolder ch = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
            ps = ch.getConnection().prepareStatement("select uid.username as username, uid.id as id from userid uid, user u, useremailaddress ue where u.status=1 and u.username = uid.username and u.lastlogindate >= ? and u.lastlogindate < ? and ue.type = ? and ue.verified=1");
            log.info(String.format("select uid.username as username, uid.id as id from userid uid, user u, useremailaddress ue where u.username = uid.username and u.lastlogindate >= %s and u.lastlogindate < %s and ue.type = %d and ue.verified=1", minimum.toString(), maximum.toString(), emailType.value));
            ps.setTimestamp(1, minimum);
            ps.setTimestamp(2, maximum);
            ps.setInt(3, emailType.value);
            rs = ps.executeQuery();
            HashMap result = new HashMap();

            while(rs.next()) {
               result.put(rs.getString("username"), rs.getInt("id"));
            }

            HashMap var9 = result;
            return var9;
         } catch (SQLException var24) {
            throw new EJBException(var24.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var23) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var21) {
               ch = null;
            }

         }
      }
   }

   public UserData loadUserByUsernameOrAlias(String usernameOrAlias, boolean fullyLoadUserObject, boolean loadFromMasterDB) throws EJBException {
      UserData userData = this.loadUser(usernameOrAlias, fullyLoadUserObject, loadFromMasterDB);
      if (userData == null) {
         log.info(String.format("could not load user by username '%s', trying alias", usernameOrAlias));
         Connection conn = null;
         String username = null;

         try {
            conn = loadFromMasterDB ? this.dataSourceMaster.getConnection() : this.dataSourceSlave.getConnection();
            username = this.getUsernameByAlias(usernameOrAlias, conn);
         } catch (SQLException var16) {
            throw new EJBException(var16.getMessage());
         } finally {
            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var15) {
               conn = null;
            }

         }

         if (username == null) {
            return null;
         } else {
            log.info(String.format("loading user by username '%s' from alias '%s'", username, usernameOrAlias));
            return this.loadUser(username, fullyLoadUserObject, loadFromMasterDB);
         }
      } else {
         return userData;
      }
   }

   public UserData loadUser(String username, boolean fullyLoadUserObject, boolean loadFromMasterDB) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserData userData;
      try {
         conn = loadFromMasterDB ? this.dataSourceMaster.getConnection() : this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select u.*, uid.id as uid, a.headuuid as avatar, a.bodyuuid as fullbodyavatar, ua.alias,uv.type accountType, uv.verified accountVerified, uv.description verifiedProfile, uea.emailaddress primaryEmail, uea.verified emailVerified from user u, userid uid LEFT OUTER JOIN avataruserbody a ON uid.id = a.userid and a.used = 1 LEFT OUTER JOIN useralias ua ON uid.username=ua.username LEFT OUTER JOIN userverified uv ON uv.userid = uid.id LEFT OUTER JOIN useremailaddress uea ON uea.userid = uid.id and uea.type = ? where u.username = uid.username and u.username = ? ");
         ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (rs.next() && username.toLowerCase().equals(rs.getString("u.username"))) {
            userData = new UserData(rs);
            if (fullyLoadUserObject) {
               rs.close();
               ps.close();
               conn.close();
               conn = this.dataSourceSlave.getConnection();
               userData.pendingContacts = this.loadPendingContacts(userData.username, conn);
               userData.blockList = this.loadBlockList(userData.username, conn);
               userData.broadcastList = this.checkAndLoadBroadcastList(userData.username, conn);
            }

            this.populateUserSettings(userData, conn);
            MemCachedHelper.setUserAlias(username, userData.userID, userData.alias);
            UserData var8 = userData;
            return var8;
         }

         userData = null;
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } catch (CreateException var29) {
         throw new EJBException(var29.getMessage());
      } catch (NoSuchFieldException var30) {
         throw new EJBException(var30.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return userData;
   }

   public UserData loadUserFromID(int id) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserData userData;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select u.*, uid.id as uid, a.headuuid as avatar, a.bodyuuid as fullbodyavatar, ua.alias, uv.type accountType, uv.verified accountVerified, uv.description verifiedProfile, uea.emailaddress primaryEmail, uea.verified emailVerified from user u, userid uid LEFT OUTER JOIN avataruserbody a ON uid.id = a.userid and a.used = 1 LEFT OUTER JOIN useralias ua ON uid.username=ua.username LEFT OUTER JOIN userverified uv ON uv.userid = uid.id LEFT OUTER JOIN useremailaddress uea ON uea.userid = uid.id and uea.type = ? where u.username = uid.username and uid.id = ? ");
         ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
         ps.setInt(2, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            userData = new UserData(rs);
            this.populateUserSettings(userData, conn);
            MemCachedHelper.setUserAlias(userData.username, userData.userID, userData.alias);
            UserData var6 = userData;
            return var6;
         }

         userData = null;
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return userData;
   }

   public UserData loadUserFromMobilePhone(String mobilePhone) throws EJBException {
      if (StringUtil.isBlank(mobilePhone)) {
         throw new EJBException("Blank mobile phone not allowed");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         UserData userData;
         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select u.*, uid.id as uid, a.headuuid as avatar, a.bodyuuid as fullbodyavatar, ua.alias, uv.type accountType, uv.verified accountVerified, uv.description verifiedProfile, uea.emailaddress primaryEmail, uea.verified emailVerified from user u, userid uid LEFT OUTER JOIN avataruserbody a ON uid.id = a.userid and a.used = 1 LEFT OUTER JOIN useralias ua ON uid.username=ua.username LEFT OUTER JOIN userverified uv ON uv.userid = uid.id LEFT OUTER JOIN useremailaddress uea ON uea.userid = uid.id and uea.type = ? where u.username = uid.username and u.mobilephone = ? LIMIT 0,1");
            ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
            ps.setString(2, mobilePhone);
            rs = ps.executeQuery();
            if (rs.next()) {
               userData = new UserData(rs);
               MemCachedHelper.setUserAlias(userData.username, userData.userID, userData.alias);
               UserData var6 = userData;
               return var6;
            }

            userData = null;
         } catch (SQLException var24) {
            throw new EJBException(var24.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var23) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var21) {
               conn = null;
            }

         }

         return userData;
      }
   }

   public UserData loadUserFromVoucherNumber(String voucherNumber) throws EJBException {
      try {
         VoucherLocal voucherEJB = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
         VoucherData voucherData = voucherEJB.getVoucher(voucherNumber);
         if (voucherData == null) {
            throw new EJBException("Invalid voucher number " + voucherNumber);
         } else {
            AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            AccountEntryData accountEntryData = accountEJB.getAccountEntryFromReference(AccountEntryData.TypeEnum.VOUCHER_RECHARGE, voucherData.id.toString());
            return accountEntryData == null ? null : this.loadUser(accountEntryData.username, false, false);
         }
      } catch (CreateException var6) {
         throw new EJBException(var6.getMessage());
      }
   }

   public void updateUserDetail(UserData userData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      UserPrx userPrx = EJBIcePrxFinder.findUserPrx(userData.username);

      try {
         UserData oldUserData = this.loadUser(userData.username, false, false);
         String statement = "update user set ";
         statement = statement + "emailaddress=?, onmailinglist=?, utcoffset=?, notes=?, type=?, chatroomadmin=?, chatroombans=?, emailactivated=?, emailalert=?, emailactivationdate=?, allowBuzz=?, bonusprogramid=? ";
         statement = statement + "where username=?";
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement(statement);
         ps.setString(1, userData.emailAddress);
         ps.setObject(2, userData.onMailingList == null ? null : userData.onMailingList ? 1 : 0);
         ps.setObject(3, userData.UTCOffset);
         ps.setString(4, userData.notes);
         ps.setInt(5, userData.type.value());
         ps.setObject(6, userData.chatRoomAdmin == null ? null : userData.chatRoomAdmin ? 1 : 0);
         ps.setInt(7, userData.chatRoomBans);
         ps.setObject(8, userData.emailActivated == null ? null : userData.emailActivated ? 1 : 0);
         ps.setObject(9, userData.emailAlert == null ? null : userData.emailAlert ? 1 : 0);
         ps.setTimestamp(10, userData.emailActivationDate == null ? null : new Timestamp(userData.emailActivationDate.getTime()));
         ps.setObject(11, userData.allowBuzz == null ? null : userData.allowBuzz ? 1 : 0);
         ps.setObject(12, userData.bonusProgramID);
         ps.setString(13, userData.username);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to update user detail for " + userData.username);
         }

         if ((userData.displayPicture != null || oldUserData.displayPicture == null) && (userData.displayPicture == null || userData.displayPicture.equals(oldUserData.displayPicture))) {
            log.info("No change in displayPicture, old: [" + oldUserData.displayPicture + "] new: [" + userData.displayPicture + "]... skipping update");
         } else {
            log.info("Updating displayPicture from [" + oldUserData.displayPicture + "] to [" + userData.displayPicture + "]");
            this.updateDisplayPicture(userData.username, userData.displayPicture);
         }

         if (userPrx != null) {
            userPrx.userDetailChanged(userData.toIceObject());
         }

         EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(userData.username, UserDataUpdatedEvent.TypeEnum.USER_DETAIL));
      } catch (LocalException var21) {
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

   }

   public void updateOtherIMDetail(String username, ImType imType, String imUsername, String imPassword) throws EJBException {
      Connection conn = null;
      PreparedStatement psGetIDs = null;
      PreparedStatement psDeleteRow = null;
      ResultSet rs = null;

      try {
         if (imUsername == null || imUsername.trim().length() == 0) {
            imUsername = null;
            imPassword = null;
         }

         UserData userData = this.loadUser(username, false, false);
         if (userData == null) {
            throw new EJBException("Invalid username " + username);
         }

         AuthenticationServicePrx authenticationServicePrx = EJBIcePrxFinder.getAuthenticationServiceProxy();
         PasswordType passwordType = PasswordType.forIMEnum(imType);
         Credential newCredential = new Credential(userData.userID, imUsername, imPassword, passwordType.value());
         AuthenticationServiceCredentialResponse response = authenticationServicePrx.getCredential(userData.userID, passwordType.value());
         if (response.code == AuthenticationServiceResponseCodeEnum.Success) {
            authenticationServicePrx.removeCredential(response.userCredential);
            if (imUsername != null) {
               authenticationServicePrx.createCredential(newCredential);
            }
         } else {
            if (response.code != AuthenticationServiceResponseCodeEnum.UnknownCredential) {
               throw new EJBException("Authentication service error " + response.code);
            }

            if (imUsername != null) {
               authenticationServicePrx.createCredential(newCredential);
            }
         }

         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
         if (userPrx != null) {
            userPrx.userDetailChanged(userData.toIceObject());
         }

         if (imUsername == null) {
            String sqlGetIDs;
            switch(imType) {
            case MSN:
               sqlGetIDs = "select id from contact where username = ? and msnusername is not null and fusionusername is null and aimusername is null and yahoousername is null and icqusername is null and jabberusername is null and emailaddress is null and mobilephone is null and homephone is null and officephone is null";
               break;
            case YAHOO:
               sqlGetIDs = "select id from contact where username = ? and yahoousername is not null and fusionusername is null and aimusername is null and msnusername is null and icqusername is null and jabberusername is null and emailaddress is null and mobilephone is null and homephone is null and officephone is null";
               break;
            case AIM:
               sqlGetIDs = "select id from contact where username = ? and aimusername is not null and fusionusername is null and yahoousername is null and msnusername is null and icqusername is null and jabberusername is null and emailaddress is null and mobilephone is null and homephone is null and officephone is null";
               break;
            case GTALK:
               sqlGetIDs = "select id from contact where username = ? and jabberusername is not null and fusionusername is null and yahoousername is null and msnusername is null and icqusername is null and aimusername is null and emailaddress is null and mobilephone is null and homephone is null and officephone is null";
               break;
            case FACEBOOK:
               sqlGetIDs = "select id from contact where username = ? and icqusername is not null and fusionusername is null and yahoousername is null and msnusername is null and  jabberusername is null and aimusername is null and emailaddress is null and mobilephone is null and homephone is null and officephone is null";
               break;
            default:
               throw new EJBException("Unknown IM type " + imType);
            }

            conn = this.dataSourceMaster.getConnection();
            psGetIDs = conn.prepareStatement(sqlGetIDs);
            psGetIDs.setString(1, username);
            rs = psGetIDs.executeQuery();
            psDeleteRow = conn.prepareStatement("delete from contact where id = ?");

            while(true) {
               if (!rs.next()) {
                  if (userPrx != null) {
                     userPrx.otherIMRemoved(imType.value());
                  }
                  break;
               }

               psDeleteRow.setInt(1, rs.getInt(1));
               if (psDeleteRow.executeUpdate() == 0) {
                  throw new EJBException("Unable to remove IM contact");
               }
            }
         }

         if (imType == ImType.FACEBOOK) {
            log.info("Adding ThirdPartySiteCredentialUpdatedEvent to clear migbo dataservice credential cache");
            EventQueue.enqueueSingleEvent(new ThirdPartySiteCredentialUpdatedEvent(userData.userID));
         }
      } catch (SQLException var35) {
         throw new EJBException(var35.getMessage());
      } catch (FusionException var36) {
         throw new EJBException(var36.message);
      } catch (LocalException var37) {
         throw new EJBException(var37.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var34) {
            rs = null;
         }

         try {
            if (psGetIDs != null) {
               psGetIDs.close();
            }
         } catch (SQLException var33) {
            psGetIDs = null;
         }

         try {
            if (psDeleteRow != null) {
               psDeleteRow.close();
            }
         } catch (SQLException var32) {
            psDeleteRow = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

   }

   public void updateStatusMessage(int userID, String username, String statusMessage, ClientType deviceType, SSOEnums.View ssoView) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);

      try {
         conn = this.dataSourceMaster.getConnection();
         statusMessage = StringUtil.stripHTML(statusMessage);
         long timeStamp = System.currentTimeMillis();
         ps = conn.prepareStatement("update user set statusmessage=?, statustimestamp=? where username=? and !(statusmessage<=>?)");
         ps.setString(1, statusMessage);
         ps.setTimestamp(2, new Timestamp(timeStamp));
         ps.setString(3, username);
         ps.setString(4, statusMessage);
         if (ps.executeUpdate() != 1) {
            return;
         }

         ps.close();
         List<Event> eventList = new LinkedList();
         eventList.add(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.STATUS_MESSAGE));
         if (!StringUtil.isBlank(statusMessage) && SystemProperty.getBool("migboPostFromStatusUpdateEnabled", false)) {
            eventList.add(new StatusUpdateEvent(username, statusMessage, deviceType == null ? -1 : deviceType.value(), ssoView == null ? -1 : ssoView.value()));
         }

         EventQueue.enqueueMultipleEvents(eventList);
         DisplayPictureAndStatusMessage.deleteDisplayPictureAndStatusMessage(displayPictureAndStatusMessageMemcache, username);
         if (userPrx != null) {
            userPrx.userStatusMessageChanged(statusMessage, timeStamp);
         }
      } catch (LocalException var28) {
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var27) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var26) {
            conn = null;
         }

      }

   }

   public void updateDisplayPicture(String username, String displayPicture) throws EJBException {
      if (displayPicture != null && !Pattern.matches("^[0-9a-zA-Z._-]*$", displayPicture)) {
         throw new EJBException("Invalid display picture [" + displayPicture + "]");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);

         try {
            conn = this.dataSourceMaster.getConnection();
            long timeStamp = System.currentTimeMillis();
            ps = conn.prepareStatement("set foreign_key_checks = 0");
            ps.execute();
            ps.close();
            ps = conn.prepareStatement("update user set displaypicture=?, statustimestamp=? where username=?");
            ps.setString(1, displayPicture);
            ps.setTimestamp(2, new Timestamp(timeStamp));
            ps.setString(3, username);
            int rowsUpdated = ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement("set foreign_key_checks = 1");
            ps.execute();
            ps.close();
            if (rowsUpdated != 1) {
               throw new EJBException("Failed to update display picture for " + username);
            }

            DisplayPictureAndStatusMessage.deleteDisplayPictureAndStatusMessage(displayPictureAndStatusMessageMemcache, username);
            URLUtil.purgeVanishCache("/u/" + username);
            if (userPrx != null) {
               userPrx.userDisplayPictureChanged(displayPicture, timeStamp);
            }

            EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.DISPLAY_PICTURE));
         } catch (LocalException var23) {
         } catch (SQLException var24) {
            throw new EJBException(var24.getMessage());
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var21) {
               conn = null;
            }

         }

      }
   }

   public void removeDisplayPicture(String displayPicture) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select username from user where displaypicture = ?");
         ps.setString(1, displayPicture);
         rs = ps.executeQuery();

         while(rs.next()) {
            this.updateDisplayPicture(rs.getString("username"), (String)null);
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public UserProfileData.StatusEnum getUserProfileStatus(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserProfileData.StatusEnum var23;
      try {
         UserProfileData.StatusEnum status = (UserProfileData.StatusEnum)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE_STATUS, username);
         if (status == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select status from userprofile where username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
               Integer intVal = (Integer)rs.getObject("status");
               if (intVal != null) {
                  status = UserProfileData.StatusEnum.fromValue(intVal);
               }
            }

            if (status == null) {
               status = UserProfileData.StatusEnum.PRIVATE;
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE_STATUS, username, status);
         }

         var23 = status;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var23;
   }

   public UserProfileData getUserProfile(String requestingUsername, String targetUsername, boolean checkAccessRight) throws EJBException, FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         UserProfileData targetUserProfileData = (UserProfileData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE, targetUsername);
         UserProfileData var8;
         if (targetUserProfileData == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("select * from userprofile where username = ?");
            ps.setString(1, targetUsername);
            rs = ps.executeQuery();
            if (!rs.next()) {
               var8 = null;
               return var8;
            }

            targetUserProfileData = new UserProfileData(rs);
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE, targetUsername, targetUserProfileData);
            rs.close();
            ps.close();
            conn.close();
         }

         if (checkAccessRight && !requestingUsername.equals(targetUsername)) {
            if (UserProfileData.StatusEnum.PRIVATE.equals(targetUserProfileData.status)) {
               log.info("Private profile of [" + targetUsername + "] viewed by [" + requestingUsername + "]");
               throw new FusionEJBException("The profile you have selected is 'Private'. Only the user can view this profile.");
            }

            if (UserProfileData.StatusEnum.CONTACTS_ONLY.equals(targetUserProfileData.status)) {
               conn = this.dataSourceSlave.getConnection();
               ps = conn.prepareStatement("select id from contact where username = ? and fusionusername = ?");
               ps.setString(1, targetUsername);
               ps.setString(2, requestingUsername);
               rs = ps.executeQuery();
               if (!rs.next()) {
                  rs.close();
                  ps.close();
                  ps = conn.prepareStatement("select username from pendingcontact where username = ? and pendingcontact = ?");
                  ps.setString(1, requestingUsername);
                  ps.setString(2, targetUsername);
                  rs = ps.executeQuery();
                  if (!rs.next()) {
                     log.info("FriendsOnly profile of [" + targetUsername + "] viewed by [" + requestingUsername + "]");
                     throw new FusionEJBException("The profile you have selected is 'Friends Only'. Only the user's friends can view this profile.");
                  }
               }
            }
         }

         var8 = targetUserProfileData;
         return var8;
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }
   }

   private void onProfileUpdated(String username) {
      try {
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE, username);
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE_STATUS, username);
      } catch (Exception var3) {
         log.error("Unable to delete profile status from memcached user [" + username + "]", var3);
      }

      EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.PROFILE));
   }

   public boolean updateUserProfile(UserProfileData userProfileData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var31;
      try {
         conn = this.dataSourceMaster.getConnection();
         UserProfileData oldProfileData = null;

         try {
            oldProfileData = this.getUserProfile(userProfileData.username, userProfileData.username, true);
         } catch (Exception var28) {
            oldProfileData = null;
         }

         boolean needToUpdate = true;
         if (oldProfileData == null) {
            ps = conn.prepareStatement("insert into userprofile (firstname, lastname, hometown, city, state, dateofbirth, gender, jobs, schools, hobbies, likes, dislikes, aboutme, relationshipstatus, status, username) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);
         } else if (userProfileData.isDifferent(oldProfileData)) {
            ps = conn.prepareStatement("update userprofile set firstname=?, lastname=?, hometown=?, city=?, state=?, dateofbirth=?, gender=?, jobs=?, schools=?, hobbies=?, likes=?, dislikes=?, aboutme=?, relationshipstatus=?, status=? where username=?");
         } else {
            needToUpdate = false;
         }

         if (needToUpdate) {
            if (userProfileData.status == null) {
               userProfileData.status = UserProfileData.StatusEnum.PUBLIC;
            }

            ps.setString(1, userProfileData.firstName);
            ps.setString(2, userProfileData.lastName);
            ps.setString(3, userProfileData.homeTown);
            ps.setString(4, userProfileData.city);
            ps.setString(5, userProfileData.state);
            ps.setTimestamp(6, userProfileData.dateOfBirth == null ? null : new Timestamp(userProfileData.dateOfBirth.getTime()));
            ps.setString(7, userProfileData.gender == null ? null : userProfileData.gender.value());
            ps.setString(8, userProfileData.jobs);
            ps.setString(9, userProfileData.schools);
            ps.setString(10, userProfileData.hobbies);
            ps.setString(11, userProfileData.likes);
            ps.setString(12, userProfileData.dislikes);
            ps.setString(13, userProfileData.aboutMe);
            ps.setObject(14, userProfileData.relationshipStatus == null ? null : userProfileData.relationshipStatus.value());
            ps.setObject(15, userProfileData.status == null ? null : userProfileData.status.value());
            ps.setString(16, userProfileData.username);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Failed to update user profile for " + userProfileData.username);
            }

            if (oldProfileData == null) {
               rs = ps.getGeneratedKeys();
               if (!rs.next()) {
                  throw new EJBException("Failed to create user profile for " + userProfileData.username);
               }

               userProfileData.id = rs.getInt(1);
               rs.close();
            } else {
               userProfileData.id = oldProfileData.id;
            }

            ps.close();
            this.onProfileUpdated(userProfileData.username);
            Enums.UserProfileKeywordEnum[] types = new Enums.UserProfileKeywordEnum[]{Enums.UserProfileKeywordEnum.JOBS, Enums.UserProfileKeywordEnum.SCHOOLS, Enums.UserProfileKeywordEnum.HOBBIES, Enums.UserProfileKeywordEnum.LIKES, Enums.UserProfileKeywordEnum.DISLIKES};
            String[] newKeywords = new String[]{userProfileData.jobs, userProfileData.schools, userProfileData.hobbies, userProfileData.likes, userProfileData.dislikes};
            String[] oldKeywords;
            if (oldProfileData == null) {
               oldKeywords = new String[]{"", "", "", "", ""};
            } else {
               oldKeywords = new String[]{oldProfileData.jobs, oldProfileData.schools, oldProfileData.hobbies, oldProfileData.likes, oldProfileData.dislikes};
            }

            ps = conn.prepareStatement("delete from userprofilekeyword where userprofileid = ? and type = ?");
            ps.setInt(1, userProfileData.id);

            for(int i = 0; i < types.length; ++i) {
               String oldKeyword = oldKeywords[i] == null ? "" : oldKeywords[i];
               String newKeyword = newKeywords[i] == null ? "" : newKeywords[i];
               if (!oldKeyword.equalsIgnoreCase(newKeyword)) {
                  if (oldKeyword.length() > 0) {
                     ps.setInt(2, types[i].value());
                     ps.executeUpdate();
                  }

                  if (newKeyword.length() > 0) {
                     this.addUserProfileKeywords(conn, userProfileData.id, types[i], newKeyword.split(","));
                  }
               }
            }
         }

         var31 = needToUpdate;
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return var31;
   }

   private void addUserProfileKeywords(Connection conn, int profileID, Enums.UserProfileKeywordEnum type, String[] keywords) throws SQLException, EJBException {
      PreparedStatement psGetKeyword = null;
      PreparedStatement psAddKeyword = null;
      PreparedStatement psAddKeywordLink = null;
      ResultSet rs = null;

      try {
         psGetKeyword = conn.prepareStatement("select id from keyword where keyword = ?");
         psAddKeyword = conn.prepareStatement("insert into keyword (keyword) values (?)", 1);
         psAddKeywordLink = conn.prepareStatement("insert into userprofilekeyword (userprofileid, keywordid, type) values (?,?,?)");
         psAddKeywordLink.setInt(1, profileID);
         Set<String> keywordSet = new HashSet();
         keywordSet.addAll(Arrays.asList(keywords));
         Iterator i$ = keywordSet.iterator();

         while(i$.hasNext()) {
            String keyword = (String)i$.next();
            if (keyword.length() > 0) {
               keyword = keyword.trim();
               int len = keyword.length();
               if (len != 0) {
                  if (len > 64) {
                     keyword = keyword.substring(0, 64);
                  }

                  psGetKeyword.setString(1, keyword);
                  rs = psGetKeyword.executeQuery();
                  int keywordID;
                  if (rs.next()) {
                     keywordID = rs.getInt("id");
                     rs.close();
                  } else {
                     psAddKeyword.setString(1, keyword);
                     psAddKeyword.executeUpdate();
                     rs.close();
                     rs = psAddKeyword.getGeneratedKeys();
                     if (!rs.next()) {
                        throw new EJBException("Failed to add keyword " + keyword + " to database");
                     }

                     keywordID = rs.getInt(1);
                     rs.close();
                  }

                  psAddKeywordLink.setInt(2, keywordID);
                  psAddKeywordLink.setInt(3, type.value());
                  if (psAddKeywordLink.executeUpdate() != 1) {
                     throw new EJBException("Failed to link keyword " + keyword + " to user profile");
                  }
               }
            }
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (psGetKeyword != null) {
               psGetKeyword.close();
            }
         } catch (SQLException var28) {
            psGetKeyword = null;
         }

         try {
            if (psAddKeyword != null) {
               psAddKeyword.close();
            }
         } catch (SQLException var27) {
            psAddKeyword = null;
         }

         try {
            if (psAddKeywordLink != null) {
               psAddKeywordLink.close();
            }
         } catch (SQLException var26) {
            psAddKeywordLink = null;
         }

      }

   }

   public void activateAccount(String username, String verificationCode, boolean loadUserFromMasterDb, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);

      try {
         UserData userData = this.loadUser(username, false, loadUserFromMasterDb);
         if (userData == null) {
            throw new EJBException("Invalid user " + username);
         }

         if (userData.mobilePhone == null) {
            throw new EJBException("User does not have a mobile phone number");
         }

         if (userData.mobileVerified) {
            return;
         }

         if (userData.failedActivationAttempts >= SystemProperty.getInt("MaxActivationAttempts")) {
            throw new EJBException("Too many failed authentication attempts");
         }

         if (!userData.verificationCode.equals(verificationCode)) {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            userEJB.activateAccountFailed(username);
            throw new EJBException("Incorrect code entered");
         }

         conn = this.dataSourceMaster.getConnection();
         Timestamp affiliateReferralDate = new Timestamp(0L);
         ps = conn.prepareStatement("select affiliateid, datecreated from affiliatereferral where mobilephone = ? order by datecreated desc");
         ps.setString(1, userData.mobilePhone);
         rs = ps.executeQuery();
         if (rs.next()) {
            userData.affiliateID = rs.getInt("affiliateId");
            userData.referralLevel = 1;
            affiliateReferralDate = rs.getTimestamp("datecreated");
         }

         rs.close();
         ps.close();
         Integer referrerUserID = null;
         ps = conn.prepareStatement("select uid.id, u.username, u.countryid, u.affiliateid, u.referrallevel from userreferral ur inner join user u on (ur.username = u.username) inner join userid uid on (u.username = uid.username) where ur.mobilephone = ? and ur.datecreated > ? order by ur.datecreated desc");
         ps.setString(1, userData.mobilePhone);
         ps.setTimestamp(2, affiliateReferralDate);
         rs = ps.executeQuery();
         if (rs.next()) {
            referrerUserID = rs.getInt("id");
            userData.affiliateID = (Integer)rs.getObject("affiliateid");
            userData.referredBy = rs.getString("username");
            userData.referralLevel = (Integer)rs.getObject("referrallevel");
            if (userData.referralLevel == null) {
               userData.referralLevel = 1;
            } else {
               userData.referralLevel = userData.referralLevel + 1;
            }
         }

         userData.mobileVerified = true;
         ps = conn.prepareStatement("update user set mobileverified = 1, affiliateId = ?, referredBy = ?, referralLevel = ? where username = ? and verificationcode = ? and mobileverified = 0 and failedactivationattempts < ?");
         ps.setObject(1, userData.affiliateID);
         ps.setString(2, userData.referredBy);
         ps.setObject(3, userData.referralLevel);
         ps.setString(4, userData.username);
         ps.setString(5, verificationCode);
         ps.setInt(6, SystemProperty.getInt("MaxActivationAttempts"));
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Too many failed authentication attempts");
         }

         ps.close();
         ps = conn.prepareStatement("select id from activation where username = ? or mobilephone = ? limit 1");
         ps.setString(1, userData.username);
         ps.setString(2, userData.mobilePhone);
         rs = ps.executeQuery();
         boolean firstActivation = !rs.next();
         rs.close();
         ps.close();
         ps = conn.prepareStatement("insert into activation (username, mobilephone, datecreated, ipaddress) values (?,?,?,?)", 1);
         ps.setString(1, username);
         ps.setString(2, userData.mobilePhone);
         ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
         ps.setString(4, accountEntrySourceData.ipAddress);
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Failed to log authentication attempt");
         }

         int activationID = rs.getInt(1);
         rs.close();
         ps.close();
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         if (firstActivation) {
            if (!this.isUserEmailVerified(userData.username)) {
               accountEJB.giveActivationCredit(userData.username, userData.countryID, Integer.toString(activationID), accountEntrySourceData);
            }

            if (referrerUserID != null) {
               try {
                  UserData referrerUserData = this.loadUserFromID(referrerUserID);
                  UserReferralActivationTrigger trigger = new UserReferralActivationTrigger(referrerUserData);
                  trigger.quantityDelta = 1;
                  trigger.amountDelta = 0.0D;
                  RewardCentre.getInstance().sendTrigger(trigger);
                  Leaderboard.updateReferrerLeaderboards(userData.referredBy, referrerUserID);
               } catch (Exception var40) {
                  log.warn("Unable to notify reward system to send UserReferallActivationTrigger", var40);
               }
            }
         }

         try {
            accountEJB.activatePendingMerchantTag(conn, userData.username);
         } catch (EJBException var39) {
            log.warn("Unable to activate merchant tag for user: " + username);
         }

         conn.close();
         if (userPrx != null) {
            userPrx.userDetailChanged(userData.toIceObject());
         }

         boolean doInAsync = !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.DO_SYNCHRONOUS_PROFILE_CACHE_INVALIDATION_ON_MIGBO_UPON_MOBILE_AUTH);
         if (!doInAsync) {
            try {
               MigboApiUtil api = MigboApiUtil.getInstance();
               JSONObject obj = api.delete(String.format("/user/%d/cache/profile", userData.userID));
               log.debug(String.format("Received JSON Response from migbo-datsvc : %s ", obj.toString()));
            } catch (Exception var38) {
               log.error(String.format("Exception caught while invalidating profile cache on migbo for user [%s] due to mobile authentication, falling back to async via EventQueueWorker", username), var38);
               doInAsync = true;
            }
         }

         if (doInAsync) {
            EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.PROFILE));
         }
      } catch (Exception var41) {
         throw new EJBException(var41.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var37) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var36) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var35) {
            conn = null;
         }

      }

   }

   private boolean mobileActivated(Connection conn, String mobilePhone) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var5;
      try {
         ps = conn.prepareStatement("select id from activation where mobilephone = ?");
         ps.setString(1, mobilePhone);
         rs = ps.executeQuery();
         var5 = rs.next();
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var15) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var14) {
            ps = null;
         }

      }

      return var5;
   }

   public void inviteFriend(String username, String displayName, String mobilePhone, Integer groupID, String groupName, String gameName, String hashKey, AccountEntrySourceData accountEntrySourceData) throws EJBException, FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         if (displayName != null && displayName.length() != 0) {
            username = username.toLowerCase().trim();
            UserData userData = this.loadUser(username, false, false);
            if (userData == null) {
               log.error(String.format("Unable to invite friend - inviter user '%s' does not exist, displayName '%s' mobilePhone %s", username, displayName, mobilePhone));
               throw new FusionEJBException("Invalid referrer username");
            } else if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.INVITE_FRIEND, userData)) {
               throw new FusionEJBException("You must authenticate your account before inviting friends");
            } else {
               MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               mobilePhone = messageEJB.cleanAndValidatePhoneNumber(mobilePhone, true);
               Integer iddCode = messageEJB.getIDDCode(mobilePhone);
               if (iddCode == null) {
                  throw new FusionEJBException("Unable to determine IDD code for phone number " + mobilePhone);
               } else if (!messageEJB.isMobileNumber(mobilePhone, true)) {
                  throw new FusionEJBException("Invalid mobile number " + mobilePhone);
               } else {
                  MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
                  CountryData countryData = misEJB.getCountryByIDDCode(iddCode, mobilePhone);
                  if (countryData == null) {
                     throw new FusionEJBException("Unable to determine country for IDD code " + iddCode);
                  } else {
                     double referralCredit = countryData.referralCredit == null ? 0.0D : countryData.referralCredit;
                     conn = this.dataSourceMaster.getConnection();
                     ps = conn.prepareStatement("select username from user where mobilephone=?");
                     ps.setString(1, mobilePhone);
                     rs = ps.executeQuery();
                     if (rs.next()) {
                        throw new FusionEJBException("The mobile number belongs to a registered migme user, " + rs.getString("username") + ". Please go back to Contacts and add " + rs.getString("username") + " as a contact");
                     } else {
                        rs.close();
                        ps.close();
                        ps = conn.prepareStatement("select mobilephone,registrationdevice from user where username=?");
                        ps.setString(1, username);
                        rs = ps.executeQuery();
                        if (!rs.next()) {
                           throw new FusionEJBException("Invalid user " + username);
                        } else {
                           String inviterMobilePhone = rs.getString("mobilephone");
                           if (inviterMobilePhone != null && inviterMobilePhone.equals(mobilePhone)) {
                              throw new FusionEJBException("You cannot refer yourself");
                           } else {
                              String registrationdevice = rs.getString("registrationdevice");
                              rs.close();
                              ps.close();
                              if (this.getMobilePhoneCount(conn, mobilePhone) <= 0) {
                                 boolean referredViaGame = false;
                                 String smsText = null;
                                 String inviterContact = inviterMobilePhone;
                                 if (inviterMobilePhone == null) {
                                    inviterContact = userData.emailAddress;
                                 }

                                 int smsCount;
                                 if (accountEntrySourceData != null) {
                                    if (!StringUtil.isBlank(gameName) && !StringUtil.isBlank(hashKey)) {
                                       referredViaGame = true;
                                       smsText = MessageFormat.format(SystemProperty.get("GameReferralSMS"), StringUtil.truncateWithEllipsis(username, 18), StringUtil.truncateWithEllipsis(displayName, 30), StringUtil.truncateWithEllipsis(gameName, 30), hashKey);
                                    } else if (groupID != null && groupName != null) {
                                       ps = conn.prepareStatement("select referralsms from groups where id=?");
                                       ps.setInt(1, groupID);
                                       rs = ps.executeQuery();
                                       if (rs.next() && org.springframework.util.StringUtils.hasLength(rs.getString("referralsms"))) {
                                          smsText = rs.getString("referralsms");
                                       }

                                       rs.close();
                                       ps.close();
                                       if (smsText == null) {
                                          smsText = SystemProperty.get("GroupReferralSMS").replaceAll("%4", groupName).replaceAll("%5", groupID.toString());
                                       }
                                    } else {
                                       smsText = SystemProperty.get("ReferralSMS");
                                       if (registrationdevice != null) {
                                          ps = conn.prepareStatement("select id,smsmsg from partnerbuild where useragent=?");
                                          ps.setString(1, registrationdevice);
                                          rs = ps.executeQuery();
                                          if (rs.next() && org.springframework.util.StringUtils.hasLength(rs.getString("smsmsg"))) {
                                             smsText = rs.getString("smsmsg");
                                             smsText = smsText.replaceAll("%4", String.valueOf(rs.getInt("id")));
                                          }
                                       }
                                    }

                                    smsText = smsText.replaceAll("%1", displayName).replaceAll("%2", inviterContact).replaceAll("%3", mobilePhone);
                                    if (smsText.length() > 160) {
                                       throw new FusionEJBException("The name you entered is too long");
                                    }

                                    smsCount = messageEJB.getSystemSMSCount(SystemSMSData.SubTypeEnum.USER_REFERRAL, username, mobilePhone);
                                    if (smsCount >= SystemProperty.getInt("MaxUserReferralRetryPerDay")) {
                                       throw new FusionEJBException("You have reached the limit for referring the same number for today. We have sent an SMS to your friend telling them about migme, and you will receive bonus credit when they authenticate their migme account");
                                    }

                                    ps = conn.prepareStatement("select count(*) from userreferral where username = ? and datecreated > curdate()");
                                    ps.setString(1, username);
                                    rs = ps.executeQuery();
                                    rs.next();
                                    if (rs.getInt(1) >= SystemProperty.getInt("MaxUserReferralPerDay")) {
                                       throw new FusionEJBException("You have reached the limit of referrals for today. We have sent SMS to your friends telling them about migme, and you will receive bonus credit when they authenticate their migme accounts");
                                    }

                                    rs.close();
                                    ps.close();
                                    ps = conn.prepareStatement("select count(*) from systemsms where subtype = ? and destination = ? and datecreated > curdate()");
                                    ps.setInt(1, SystemSMSData.SubTypeEnum.USER_REFERRAL.value());
                                    ps.setString(2, mobilePhone);
                                    rs = ps.executeQuery();
                                    rs.next();
                                    if (rs.getInt(1) >= SystemProperty.getInt("MaxUserReferralPerMobilePhone")) {
                                       throw new FusionEJBException("The mobile number " + mobilePhone + " cannot receive any more invitation today");
                                    }

                                    rs.close();
                                    ps.close();
                                    int longerLimitPerMobile = SystemProperty.getInt((String)"MaxUserReferralPerMobilePhone90Days", -1);
                                    if (longerLimitPerMobile > 0) {
                                       ps = conn.prepareStatement("select count(*) from (select destination from systemsms where subtype = ? and destination = ? and datecreated > now() - interval 1 month limit ?) s");
                                       ps.setInt(1, SystemSMSData.SubTypeEnum.USER_REFERRAL.value());
                                       ps.setString(2, mobilePhone);
                                       ps.setInt(3, longerLimitPerMobile);
                                       rs = ps.executeQuery();
                                       rs.next();
                                       if (rs.getInt(1) >= longerLimitPerMobile) {
                                          throw new FusionEJBException("The mobile number " + mobilePhone + " has already been invited and cannot receive further invitations");
                                       }

                                       rs.close();
                                       ps.close();
                                    }
                                 }

                                 ps = conn.prepareStatement("select id from userreferral where username = ? and mobilephone = ?");
                                 ps.setString(1, username);
                                 ps.setString(2, mobilePhone);
                                 rs = ps.executeQuery();
                                 if (rs.next()) {
                                    smsCount = rs.getInt("id");
                                    rs.close();
                                    ps.close();
                                    ps = conn.prepareStatement("update userreferral set datecreated=?, amount=? where id=?");
                                    ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                                    ps.setDouble(2, referralCredit);
                                    ps.setInt(3, smsCount);
                                    if (ps.executeUpdate() != 1) {
                                       throw new EJBException("Failed to update user referral for " + mobilePhone);
                                    }
                                 } else {
                                    rs.close();
                                    ps.close();
                                    ps = conn.prepareStatement("insert into userreferral (username, referrername, datecreated, mobilephone, amount, paid) values (?, ?, ?, ?, ?, ?)");
                                    ps.setString(1, username);
                                    ps.setString(2, displayName);
                                    ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                                    ps.setString(4, mobilePhone);
                                    ps.setDouble(5, referralCredit);
                                    ps.setInt(6, 0);
                                    if (ps.executeUpdate() != 1) {
                                       throw new EJBException("Failed to insert user referral for " + mobilePhone);
                                    }
                                 }

                                 if (accountEntrySourceData != null && SMSControl.isSendEnabledForSubtype(referredViaGame ? SystemSMSData.SubTypeEnum.USER_REFERRAL_VIA_GAMES : SystemSMSData.SubTypeEnum.USER_REFERRAL, username)) {
                                    SystemSMSData systemSMSData = new SystemSMSData();
                                    systemSMSData.username = username;
                                    systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
                                    systemSMSData.subType = referredViaGame ? SystemSMSData.SubTypeEnum.USER_REFERRAL_VIA_GAMES : SystemSMSData.SubTypeEnum.USER_REFERRAL;
                                    systemSMSData.source = inviterContact;
                                    systemSMSData.destination = mobilePhone;
                                    systemSMSData.messageText = smsText;
                                    messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
                                 }

                                 try {
                                    this.getUserReferralSuccessRate(username, true);
                                 } catch (Exception var43) {
                                    log.warn("Unable to update user referral success rate for user : [" + username + "]", var43);
                                 }

                              } else {
                                 throw new FusionEJBException("Mobile number cannot be used now.");
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            throw new FusionEJBException("Invalid referrer name");
         }
      } catch (CreateException var44) {
         throw new EJBException(var44.getMessage());
      } catch (SQLException var45) {
         throw new EJBException(var45.getMessage());
      } catch (NoSuchFieldException var46) {
         throw new EJBException(var46.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var42) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var41) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var40) {
            conn = null;
         }

      }
   }

   public void sendVerificationCode(String username, String password, String registrationIP, String mobilePhone, String verificationCode, String messagePattern, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.ACTIVATION_CODE, username)) {
         if (StringUtil.isBlank(messagePattern)) {
            throw new EJBException("Empty messagePattern provided for verification code SMS");
         } else {
            try {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.ACTIVATION_CODE;
               systemSMSData.username = username;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = mobilePhone;
               systemSMSData.messageText = messagePattern.replaceAll("%1", verificationCode).replaceAll("%2", username).replaceAll("%3", password);
               systemSMSData.registrationIP = registrationIP;
               MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
            } catch (CreateException var10) {
               throw new EJBException(var10.getMessage());
            } catch (NoSuchFieldException var11) {
               throw new EJBException(var11.getMessage());
            }
         }
      }
   }

   public void resendVerificationCode(String username, String mobilePhone, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         mobilePhone = messageEJB.cleanAndValidatePhoneNumber(mobilePhone, true);
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select password, mobilephone, mobileverified, verificationcode, registrationipaddress from user where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid user " + username);
         }

         if (rs.getInt("mobileverified") == 1) {
            throw new EJBException("Account is already authenticated");
         }

         int smsCount = messageEJB.getSystemSMSCount(SystemSMSData.SubTypeEnum.ACTIVATION_CODE, username);
         if (smsCount >= SystemProperty.getInt("MaxVerificationCodeRequestPerDay")) {
            throw new EJBException("You have already requested your authentication code today. If you do not receive the SMS containing your code, please email contact@mig.me");
         }

         if (!mobilePhone.equals(rs.getString("mobilephone"))) {
            this.changeMobilePhone(username, mobilePhone, accountEntrySourceData);
         } else {
            String verificationCode = rs.getString("verificationcode");
            if (verificationCode == null) {
               throw new EJBException("No verification code available");
            }

            if (!messageEJB.isMobileNumber(mobilePhone, true)) {
               throw new EJBException("Invalid mobile number " + mobilePhone);
            }

            String messagePattern = SystemProperty.get("ResendVerificationCodeSMS");
            this.sendVerificationCode(username, rs.getString("password"), rs.getString("registrationipaddress"), mobilePhone, verificationCode, messagePattern, accountEntrySourceData);
         }
      } catch (CreateException var27) {
         throw new EJBException(var27.getMessage());
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } catch (NoSuchFieldException var29) {
         throw new EJBException(var29.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

   }

   public void sendMerchantActivatedUserSMS(String username, String password, String mobilePhone, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      try {
         if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.MERCHANT_USER_ACTIVATION, username)) {
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            if (messageEJB.isMobileNumber(mobilePhone, true)) {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.MERCHANT_USER_ACTIVATION;
               systemSMSData.username = username;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = mobilePhone;
               systemSMSData.messageText = SystemProperty.get("MerchantActivatedUserSMS").replaceAll("%1", username).replaceAll("%2", password);
               messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
            }

         }
      } catch (CreateException var7) {
         throw new EJBException(var7.getMessage());
      } catch (NoSuchFieldException var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   public void sendEmailVerification(int userId, String emailAddress) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT verified FROM useremailaddress WHERE userid = ? AND emailaddress = ?");
         ps.setInt(1, userId);
         ps.setString(2, emailAddress);
         rs = ps.executeQuery();
         if (rs.next()) {
            if (rs.getBoolean("verified")) {
               throw new FusionEJBException("Email address " + emailAddress + " has already been verified.");
            } else {
               EmailUtils.EmailValidatationResult evr = EmailUtils.externalEmailIsValid(emailAddress);
               if (evr.result != EmailUtils.EmailValidatationEnum.VALID) {
                  log.error("Sending of email verification failed for [" + userId + "]: invalid email address [" + emailAddress + "].");
                  throw new EJBException(evr.reason);
               } else {
                  String token = this.generateEmailVerificationToken(emailAddress);
                  if (token == null) {
                     throw new EJBException("Unable to generate email verification token");
                  } else if (this.cacheEmailVerificationTokenDetails(token, userId, emailAddress)) {
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.ENABLED_EMAIL_ADDRESS_VRIFICATION_WITH_TEMPLATE)) {
                        UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                        Map<String, String> params = new HashMap();
                        params.put("base_url", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL));
                        params.put("verify_link", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.EXTERNAL_EMAIL_ADDRESS_VERIFICATION_LINK) + token);
                        unsProxy.sendTemplatizedEmailFromNoReply(emailAddress, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.EMAIL_ADDRESS_VRIFICATION_TEMPLATE_ID), params);
                     } else {
                        String emailVerifyLink = SystemProperty.get("ExternalEmailAddressVerificationLink", "http://www.mig33.com/sites/ajax/settings/account_email_verify?token=");
                        String subject = SystemProperty.get("ExternalEmailAddressVerificationEmailSubject", "Activate your email in mig33");
                        String content = "Hi,\nThank you for providing your email address. Please click the link below to verify your email:\n%l%s\n\n--The mig33 Team\n(if clicking the link in this message does not work, copy and paste it into the address bar of your browser.)";
                        content = content.replace("%l", emailVerifyLink);
                        content = SystemProperty.get("ExternalEmailAddressVerificationEmailContent", content);
                        content = content.replace("%s", token);
                        log.info("Sending verification email to [" + userId + "];email[" + emailAddress + "];token[" + token + "]: subject: [" + subject + "] content: [" + content + "]");
                        MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                        messageEJB.sendEmailFromNoReply(emailAddress, subject, content);
                     }

                  } else {
                     throw new EJBException("Unable to store email verification token");
                  }
               }
            }
         } else {
            log.error(userId + " tried to request for email verification on email address " + emailAddress);
            throw new FusionEJBException("Unknown email address " + emailAddress);
         }
      } catch (CreateException var28) {
         throw new EJBException(var28.getMessage());
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
      } catch (Exception var30) {
         throw new EJBException(var30.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }
   }

   private String generateEmailVerificationToken(String emailAddress) {
      String tokenSalt = SystemProperty.get("ExternalEmailVerificationTokenSalt", "asd783jsok3@1%sdf%klsdfgsklsdfer");
      String token = new String(Base64.encodeBase64(HashUtils.sha256(emailAddress + tokenSalt + System.currentTimeMillis() % 59999L)));
      return token.replaceAll("[^A-Za-z0-9]", "");
   }

   private boolean cacheEmailVerificationTokenDetails(String token, int userId, String emailAddress) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EMAIL_VERIFICATION_WITHOUT_USERNAME_ENABLED)) {
         if (!MemCachedClientWrapper.add(MemCachedKeySpaces.CommonKeySpace.EXTERNAL_EMAIL_VERIFICATION_TOKEN, token, String.format("%s|%d", emailAddress, userId), (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EMAIL_VERIFICATION_TOKEN_EXPIRY_IN_SECONDS) * 1000L)) {
            log.error(String.format("Unable to store email verification token for userid %s email %s to memcached: key (token) is already in use '%s'", userId, emailAddress, token));
            return false;
         } else {
            return true;
         }
      } else {
         String key = getRedisKeyForEmailVerificationToken(userId, token);
         Jedis masterInstance = null;

         boolean var7;
         try {
            masterInstance = Redis.getMasterInstanceForUserID(userId);
            masterInstance.connect();
            masterInstance.setex(key, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EMAIL_VERIFICATION_TOKEN_EXPIRY_IN_SECONDS), emailAddress);
            return true;
         } catch (Exception var12) {
            log.error(String.format("Unable to store email verification token for userid %d email %s with token %s to redis, key is %s", userId, emailAddress, token, key));
            var7 = false;
         } finally {
            Redis.disconnect(masterInstance, log);
         }

         return var7;
      }
   }

   public void changeMobilePhone(String username, String mobilePhone, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      this.changeMobilePhone(username, mobilePhone, false, accountEntrySourceData);
   }

   public void changeMobilePhone(String username, String mobilePhone, boolean skipSendVerificationCode, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      log.info("Change Mobilephone request: IP[" + accountEntrySourceData.ipAddress + "] username [" + username + "] User-agent [" + accountEntrySourceData.userAgent + "] to [" + mobilePhone + "]");
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);
      UserData userData = null;
      boolean ok = false;
      boolean mobilePhoneUpdated = false;

      try {
         userData = this.loadUser(username, false, false);
         if (userData == null) {
            throw new EJBException("Invalid username " + username);
         }

         conn = this.dataSourceMaster.getConnection();
         if (mobilePhone != null) {
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            mobilePhone = messageEJB.cleanAndValidatePhoneNumber(mobilePhone, true);
            if (this.getMobilePhoneCount(conn, mobilePhone) > 0) {
               throw new EJBException("Mobile phone " + mobilePhone + " already in use");
            }
         }

         if (mobilePhone == null && userData.type != UserData.TypeEnum.MIG33_PREPAID_CARD) {
            throw new EJBException("Not allowed to deregister mobile phone");
         }

         String currency;
         int countryID;
         if (mobilePhone != null && userData.type != UserData.TypeEnum.MIG33_PREPAID_CARD) {
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            CountryData countryData = misEJB.getCountryByIDDCode(messageEJB.getIDDCode(mobilePhone), mobilePhone);
            if (countryData == null) {
               throw new EJBException("Unable to determine the country for mobile phone " + mobilePhone);
            }

            if (!userData.countryID.equals(countryData.id)) {
               ps = conn.prepareStatement("select * from activation where username = ? limit 1");
               ps.setString(1, username);
               rs = ps.executeQuery();
               boolean hasMobileVerified = rs.next();
               rs.close();
               ps.close();
               if (hasMobileVerified || this.isUserMobileOrEmailVerified(username)) {
                  Integer iddCodeOld = null;
                  if (StringUtil.isBlank(userData.mobilePhone)) {
                     CountryData countryDataUser = misEJB.getCountry(userData.countryID);
                     if (countryDataUser == null) {
                        throw new EJBException("Unable to determine the country of the user");
                     }

                     iddCodeOld = countryDataUser.iddCode;
                  } else {
                     iddCodeOld = messageEJB.getIDDCode(userData.mobilePhone);
                  }

                  throw new EJBException("You can only change to a number starting with " + iddCodeOld);
               }
            }

            countryID = countryData.id;
            currency = countryData.currency;
         } else {
            countryID = userData.countryID;
            currency = userData.currency;
         }

         String verificationCode = this.newVerificationCode();
         boolean mobileVerified = userData.type == UserData.TypeEnum.MIG33_PREPAID_CARD && mobilePhone != null;
         ps = conn.prepareStatement("update user set mobilephone=?, mobileverified=?, verificationcode=?, countryid=? where username=?");
         ps.setString(1, mobilePhone);
         ps.setInt(2, mobileVerified ? 1 : 0);
         ps.setString(3, verificationCode);
         ps.setInt(4, countryID);
         ps.setString(5, username);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to change mobile phone for " + username);
         }

         mobilePhoneUpdated = true;
         if (mobilePhone != null && this.getMobilePhoneCount(conn, mobilePhone) > 1) {
            throw new EJBException("Mobile phone already in use");
         }

         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         userEJB.postChangeMobilePhone(userData, username, mobilePhone, verificationCode, currency, skipSendVerificationCode, accountEntrySourceData);
         ok = true;
         if (userPrx != null) {
            userPrx.userDetailChanged(this.loadUser(username, false, true).toIceObject());
         }
      } catch (CreateException var43) {
         throw new EJBException(var43.getMessage());
      } catch (LocalException var44) {
      } catch (SQLException var45) {
         throw new EJBException(var45.getMessage());
      } finally {
         if (!ok && mobilePhoneUpdated) {
            try {
               ps.setString(1, userData.mobilePhone);
               ps.setObject(2, userData.mobileVerified == null ? null : userData.mobileVerified ? 1 : 0);
               ps.setString(3, userData.verificationCode);
               ps.setObject(4, userData.countryID);
               ps.setString(5, username);
               ps.executeUpdate();
            } catch (Exception var42) {
            }
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var41) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var40) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var39) {
            conn = null;
         }

      }

   }

   public void postChangeMobilePhone(UserData userData, String username, String mobilePhone, String verificationCode, String currency, boolean skipSendVerificationCode, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      Object ps = null;

      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountEJB.setUsersLocalCurrency(username, currency, accountEntrySourceData);
         if (!skipSendVerificationCode && mobilePhone != null && userData.type != UserData.TypeEnum.MIG33_PREPAID_CARD) {
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            if (messageEJB.isMobileNumber(mobilePhone, true)) {
               this.resendVerificationCode(username, mobilePhone, accountEntrySourceData);
            }
         }
      } catch (CreateException var23) {
         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (ps != null) {
               ((PreparedStatement)ps).close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               ((Connection)conn).close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

   }

   public void cancelChangeMobilePhoneRequest(String username, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select activation.mobilephone, user.mobileverified, user.verificationcode from user left outer join activation on (user.username = activation.username) where user.username = ? order by id desc limit 1");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid username " + username);
         }

         String originalMobilePhone = rs.getString("mobilephone");
         if (originalMobilePhone == null || rs.getInt("mobileverified") == 1) {
            return;
         }

         rs.close();
         ps.close();
         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misEJB.getCountryByIDDCode(messageEJB.getIDDCode(originalMobilePhone), originalMobilePhone);
         if (countryData == null) {
            throw new EJBException("Unable to determine the country for mobile phone " + originalMobilePhone);
         }

         if (this.getMobilePhoneCount(conn, originalMobilePhone) > 0) {
            throw new EJBException("Mobile phone already in use");
         }

         ps = conn.prepareStatement("update user set mobilephone = ?, mobileverified = ?, countryid = ? where username = ?");
         ps.setString(1, originalMobilePhone);
         ps.setInt(2, 1);
         ps.setObject(3, countryData.id);
         ps.setString(4, username);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to cancel mobile phone change request for user " + username);
         }

         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         accountEJB.setUsersLocalCurrency(username, countryData.currency, accountEntrySourceData);
         if (userPrx != null) {
            userPrx.userDetailChanged(this.loadUser(username, false, true).toIceObject());
         }
      } catch (CreateException var30) {
         throw new EJBException(var30.getMessage());
      } catch (SQLException var31) {
         throw new EJBException(var31.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var28) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var27) {
            conn = null;
         }

      }

   }

   public String forgotPasswordViaEmail(String username, String emailAddress) throws FusionEJBException {
      log.info("A request to change password has been triggered: username [" + username + "] emailAddress [" + emailAddress + "]");
      if (StringUtil.isBlank(username)) {
         throw new FusionEJBException("Please provide a valid username.");
      } else {
         EmailUtils.EmailValidatationResult evr = EmailUtils.externalEmailIsValid(emailAddress);
         if (evr.result != EmailUtils.EmailValidatationEnum.VALID) {
            throw new FusionEJBException(evr.reason);
         } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String changePasswordLink;
            try {
               conn = this.dataSourceSlave.getConnection();
               String sql = "SELECT COUNT(*) `match` FROM useremailaddress uea, \t   userid ui, \t   user u WHERE ui.username = u.username AND uea.userid = ui.id AND ui.username = ? AND uea.type = ? AND uea.verified = 1 AND u.status = ? AND uea.emailAddress = ? ";
               ps = conn.prepareStatement(sql);
               ps.setString(1, username);
               ps.setInt(2, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
               ps.setInt(3, UserData.StatusEnum.ACTIVE.value());
               ps.setString(4, emailAddress);
               rs = ps.executeQuery();
               if (!rs.next() || rs.next() && rs.getInt("match") < 1) {
                  throw new FusionEJBException("Sorry, we cannot find your account. Please fill in the correct information below to help us identify your account.");
               }

               String token = StringUtil.generateRandomWord(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_CHARSET), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.MINIMUM_LENGTH));
               MemCachedClientWrapper.set(MemCachedKeySpaces.TokenKeySpace.FORGOT_PASSWORD, username, token, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.TOKEN_EXPIRATION_IN_MILLIS));
               log.info("Created token for reset password - user [" + username + "] email [" + emailAddress + "] token [" + token + "]");
               String content;
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.ENABLED_FORGOT_PASSWORD_EMAIL_WITH_TEMPLATE)) {
                  UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                  Map<String, String> params = new HashMap();
                  content = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.MIGME_CHANGE_PASSWORD_URL).replaceAll("%1", username).replaceAll("%2", token);
                  params.put("base_url", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL));
                  params.put("change_password_link", content);
                  params.put("username", username);
                  unsProxy.sendTemplatizedEmailFromNoReply(emailAddress, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.FORGOT_PASSWORD_EMAIL_TEMPLATE_ID), params);
               } else {
                  changePasswordLink = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.CHANGE_PASSWORD_URL).replaceAll("%1", username).replaceAll("%2", token);
                  String subject = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.EMAIL_SUBJECT);
                  content = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.EMAIL_CONTENT).replaceAll("%1", username).replaceAll("%2", changePasswordLink);
                  if (log.isDebugEnabled()) {
                     log.debug("Sending token via email: [" + emailAddress + "] subject [" + subject + "] content [" + content + "]");
                  }

                  MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                  messageEJB.sendEmailFromNoReply(emailAddress, subject, content);
               }

               this.logForgotPassword(this.getUserID(username, (Connection)null), Enums.Mig33UserActionMisLogEnum.FORGOT_PASSWORD, "EMAIL");
               changePasswordLink = token;
            } catch (SQLException var28) {
               log.error("Unable to process forgot password request", var28);
               throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
            } catch (Exception var29) {
               log.error("Unable to process forgot password request", var29);
               throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var27) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var26) {
                  ps = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var25) {
                  conn = null;
               }

            }

            return changePasswordLink;
         }
      }
   }

   private void logForgotPassword(int userID, Enums.Mig33UserActionMisLogEnum action, String desc) {
      try {
         MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misEJB.logMig33UserAction(userID, action, String.format(Enums.Mig33UserActionMisLogEnum.FORGOT_PASSWORD.getDescriptionPattern(), desc));
      } catch (Exception var5) {
         log.warn("Unable to log change in password for user [" + userID + "], due to:" + var5, var5);
      }

   }

   public String forgotPasswordViaSecurityQuestion(String username, String answer, int sqID) throws FusionEJBException, EJBException {
      log.info("A request to change password has been triggered: username [" + username + "] security answer [" + answer + "]");
      if (StringUtil.isBlank(username)) {
         throw new FusionEJBException("Please provide a valid username.");
      } else if (StringUtil.isBlank(answer)) {
         throw new FusionEJBException("Please provide a valid answer.");
      } else if (sqID < 1) {
         throw new FusionEJBException("Please provide a valid sequrity question.");
      } else {
         boolean var4 = false;

         int userID;
         try {
            userID = this.getUserID(username, (Connection)null);
         } catch (EJBException var8) {
            log.warn("Attempt to change password for username [" + username + "] but no userid detected. error msg: " + var8, var8);
            throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
         }

         SecurityQuestion sq = this.getSecurityQuestion(userID);
         if (sq == null) {
            throw new FusionEJBException("Can not find security question");
         } else if (sq.id != sqID) {
            throw new FusionEJBException("Please check that you have provided the correct security question and answer in order to proceed.");
         } else {
            try {
               AuthenticationServiceCredentialResponse authSvcCredResp = EJBIcePrxFinder.getAuthenticationServiceProxy().getCredential(userID, PasswordType.SECURITY_QUESTION.value());
               if (authSvcCredResp != null && authSvcCredResp.userCredential != null && authSvcCredResp.userCredential.password != null) {
                  if (!answer.equalsIgnoreCase(authSvcCredResp.userCredential.password)) {
                     if (log.isDebugEnabled()) {
                        log.debug(String.format("Expected security answer:%s, actual answer:%s", authSvcCredResp.userCredential.password, answer));
                     }

                     throw new FusionEJBException("Please check that you have provided the correct security question and answer in order to proceed.");
                  } else {
                     String token = StringUtil.generateRandomWord(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_CHARSET), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.MINIMUM_LENGTH));
                     MemCachedClientWrapper.set(MemCachedKeySpaces.TokenKeySpace.FORGOT_PASSWORD, username, token, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.TOKEN_EXPIRATION_IN_MILLIS));
                     log.info("Created token for reset password - user [" + username + "] security answer [" + answer + "] token [" + token + "]");
                     this.logForgotPassword(userID, Enums.Mig33UserActionMisLogEnum.FORGOT_PASSWORD, "SECURITY QUESTION");
                     return token;
                  }
               } else {
                  log.warn("Can not find security question");
                  throw new FusionEJBException("Can not find security question");
               }
            } catch (Exception var9) {
               log.error("Unable to process forgot password request:" + var9, var9);
               if (var9 instanceof FusionEJBException) {
                  throw (FusionEJBException)var9;
               } else {
                  throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
               }
            }
         }
      }
   }

   public boolean changePassword(String token, String username, String newPassword, String ipAddress) throws FusionEJBException {
      log.info("initiated password change with the following info: username [" + username + "] token [" + token + "] ipAddress [" + ipAddress + "]");
      if (StringUtil.isBlank(token)) {
         throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
      } else if (StringUtil.isBlank(newPassword)) {
         throw new FusionEJBException("Please provide a valid password.");
      } else {
         ValidateCredentialResult validationResult = PasswordUtils.validatePassword(username, newPassword);
         if (!validationResult.valid) {
            throw new FusionEJBException(validationResult.reason);
         } else if (StringUtil.isBlank(ipAddress)) {
            throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
         } else if (StringUtil.isBlank(username)) {
            throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
         } else {
            String memcacheToken = MemCachedClientWrapper.getString(MemCachedKeySpaces.TokenKeySpace.FORGOT_PASSWORD, username);
            if (!StringUtil.isBlank(memcacheToken) && memcacheToken.equals(token)) {
               boolean var7 = false;

               int userID;
               try {
                  userID = this.getUserID(username, (Connection)null);
               } catch (EJBException var12) {
                  log.warn("Attempt to change password for username [" + username + "] but no userid detected.");
                  throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
               }

               try {
                  AuthenticationServiceCredentialResponse authSvcCredResp = EJBIcePrxFinder.getAuthenticationServiceProxy().getCredential(userID, PasswordType.FUSION.value());
                  if (authSvcCredResp.userCredential == null) {
                     log.warn("Attempted to change password for user [" + username + "] but no fusion password detected");
                     throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
                  } else {
                     String oldPassword = authSvcCredResp.userCredential.password;
                     this.changePassword(username, oldPassword, newPassword);
                     MemCachedClientWrapper.delete(MemCachedKeySpaces.TokenKeySpace.FORGOT_PASSWORD, username);
                     return true;
                  }
               } catch (FusionException var10) {
                  log.warn("Unable to retrieve user credential", var10);
                  throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
               } catch (EJBException var11) {
                  log.warn("Unable to change user password", var11);
                  throw new FusionEJBException("Unable to process you request at the moment. Please try again later.");
               }
            } else {
               log.warn("Invalid token entered: token [" + token + "] username [" + username + "] ipAddress [" + ipAddress + "]");
               throw new FusionEJBException("Invalid token detected.");
            }
         }
      }
   }

   public String forgotPasswordViaSMS(String username, AccountEntrySourceData accountEntrySourceData) throws FusionEJBException {
      if (StringUtil.isBlank(username)) {
         throw new FusionEJBException("Please provide a valid username.");
      } else {
         log.info("A request to change password via SMS has been triggered: username [" + username + "]");
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         String var12;
         try {
            conn = this.dataSourceSlave.getConnection();
            String sql = "select mobilephone from user where username = ?;";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               log.error(String.format("Failed to retrieve password via sms, due to unable to locate user:%s", username));
               throw new FusionEJBException("Sorry, we cannot find your account. Please fill in the correct information below to help us identify your account.");
            }

            String mobile = rs.getString("mobilephone");
            if (StringUtil.isBlank(mobile)) {
               log.warn(String.format("Failed to retrieve password via sms, due to User:%s, does not have a mobile number", username));
               throw new FusionEJBException("Sorry, there is no mobilephone attached to the user");
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_CHECK_FORGOT_PASSWORD_VIA_SMS_MAX_RETRIEVE_TIME)) {
               rs.close();
               ps.close();
               sql = "select retrieveTimes from SMSRetrievePWStatus where username = ?;";
               ps = conn.prepareStatement(sql);
               ps.setString(1, username);
               rs = ps.executeQuery();
               if (rs.next() && rs.getInt("retrieveTimes") > SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.FORGOT_PASSWORD_VIA_SMS_MAX_RETRIEVE_TIME)) {
                  log.error(String.format("Exceed max retrieve time, user:%s with mobile phone:%s", username, mobile));
                  throw new FusionEJBException("Sorry, you have exceeded the max retrieve time, please contact our customer service");
               }
            }

            String token = StringUtil.generateRandomWord(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.DEFAULT_CHARSET), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.TokenizerSettings.MINIMUM_LENGTH));
            token = token.substring(token.length() - 4, token.length());
            MemCachedClientWrapper.set(MemCachedKeySpaces.TokenKeySpace.FORGOT_PASSWORD, username, token, SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.TOKEN_EXPIRATION_IN_MILLIS));
            log.info("Created token for reset password - user [" + username + "] mobile [" + mobile + "] token [" + token + "]");
            String content = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.FORGOT_PASSWORD_SMS_MESSAGE), token);
            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.FORGOT_PASSWORD;
            systemSMSData.username = username;
            systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
            systemSMSData.destination = mobile;
            systemSMSData.messageText = content;
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            messageEJB.sendSystemSMS(systemSMSData, (long)SystemProperty.getInt("ForgotPasswordSMSDelay"), accountEntrySourceData);
            this.logForgotPasswordViaSMS(username);
            this.logForgotPassword(this.getUserID(username, (Connection)null), Enums.Mig33UserActionMisLogEnum.FORGOT_PASSWORD, "SMS");
            var12 = token;
         } catch (SQLException var28) {
            log.error("Unable to process forgot password request", var28);
            throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
         } catch (Exception var29) {
            log.error("Unable to process forgot password request", var29);
            throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var27) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var26) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var25) {
               conn = null;
            }

         }

         return var12;
      }
   }

   public boolean allowForgotPasswordViaSMS(String username) throws FusionEJBException {
      if (StringUtil.isBlank(username)) {
         throw new FusionEJBException("Please provide a valid username.");
      } else if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_CHECK_COUNTRY_FOR_FORGOT_PASSWORD_VIA_SMS) && !SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_CHECK_RETRIEVE_TIME_FOR_FORGOT_PASSWORD_VIA_SMS)) {
         return true;
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         boolean var41;
         try {
            conn = this.dataSourceSlave.getConnection();
            String sql;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_CHECK_COUNTRY_FOR_FORGOT_PASSWORD_VIA_SMS)) {
               sql = "select CountryID from user where username = ?;";
               ps = conn.prepareStatement(sql);
               ps.setString(1, username);
               rs = ps.executeQuery();
               if (!rs.next()) {
                  log.error(String.format("Unable to retrive country id for user:%s", username));
                  throw new FusionEJBException("Unable to retrive country id.");
               }

               Integer countryIDInteger = (Integer)rs.getObject("CountryID");
               if (countryIDInteger == null) {
                  log.error(String.format("NO country information for user:%s", username));
                  throw new FusionEJBException("NO country information.");
               }

               int countryID = countryIDInteger;
               boolean isWhitelisted = false;
               int[] arr$ = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.WHITELIST_COUNTRIES_FOR_SMS);
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  int cid = arr$[i$];
                  if (cid == countryID) {
                     isWhitelisted = true;
                     break;
                  }
               }

               if (!isWhitelisted) {
                  boolean var43 = false;
                  return var43;
               }
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.ENABLED_CHECK_FORGOT_PASSWORD_VIA_SMS_MAX_RETRIEVE_TIME)) {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var37) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var36) {
                  ps = null;
               }

               sql = "select retrieveTimes from SMSRetrievePWStatus where username = ?;";
               ps = conn.prepareStatement(sql);
               ps.setString(1, username);
               rs = ps.executeQuery();
               if (rs.next() && rs.getInt("retrieveTimes") > SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotPassword.FORGOT_PASSWORD_VIA_SMS_MAX_RETRIEVE_TIME)) {
                  boolean var42 = false;
                  return var42;
               }
            }

            var41 = true;
         } catch (SQLException var38) {
            log.error("Unable to process forgot password request", var38);
            throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
         } catch (Exception var39) {
            log.error("Unable to process forgot password request", var39);
            throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var35) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var34) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var33) {
               conn = null;
            }

         }

         return var41;
      }
   }

   private void logForgotPasswordViaSMS(String username) {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "insert into SMSRetrievePWStatus(username) values(?) on duplicate key update retrieveTimes = retrieveTimes+1, retrieveDate = now();";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.executeUpdate();
      } catch (Exception var18) {
         log.error("Failed to log forgot password via SMS, due to: " + var18, var18);
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var16) {
            conn = null;
         }

      }

   }

   /** @deprecated */
   @Deprecated
   public void forgotPasswordWithMobileNumberOrEmail(String mobileOrEmail, boolean email, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         if (email) {
            ps = conn.prepareStatement("select u.username as username from userid u, useremailaddress ue where ue.emailaddress = ? and u.id = ue.userid and ue.type = ?");
            ps.setString(1, mobileOrEmail);
            ps.setInt(2, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Invalid email address: " + mobileOrEmail);
            }
         } else {
            mobileOrEmail = messageEJB.cleanAndValidatePhoneNumber(mobileOrEmail, true);
            if (!messageEJB.isMobileNumber(mobileOrEmail, true)) {
               throw new EJBException("Invalid mobile number: " + mobileOrEmail);
            }

            ps = conn.prepareStatement("select username from user where mobilephone = ? and status = 1");
            ps.setString(1, mobileOrEmail);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Invalid mobile number: " + mobileOrEmail);
            }
         }

         String username = rs.getString("username");
         rs.close();
         ps.close();
         if (!email) {
            int smsCount = messageEJB.getSystemSMSCount(SystemSMSData.SubTypeEnum.FORGOT_PASSWORD, username);
            if (smsCount >= SystemProperty.getInt("MaxPasswordRequestPerDay")) {
               throw new EJBException("You have already requested your password today. If you do not receive the SMS containing your password, please email contact@mig.me");
            }
         }

         Integer attempts = MemCachedClientWrapper.getInt(MemCachedKeySpaces.RateLimitKeySpace.FORGOT_PASSWORD_ATTEMPTS, MemCachedKeyUtils.getFullKeyFromStrings(mobileOrEmail, accountEntrySourceData.ipAddress));
         if (attempts != null && attempts > SystemProperty.getInt((String)"MaxForgotPasswordAttempts", 5)) {
            throw new EJBException("You have already exceeded the maximum number of reset passwords today. If you have not requested for your password to be reset, please email contact@mig.me.");
         }

         ps = conn.prepareStatement("select username from usersetting where username = ? AND type = ?");
         ps.setString(1, username);
         ps.setInt(2, UserSettingData.TypeEnum.SECURITY_QUESTION.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            Crypter crypter = new Crypter(ConfigUtils.getConfigDirectory() + "/aeskeys/");
            String token = PasswordUtils.encrypt(UUID.randomUUID().toString(), crypter);
            token = token.substring(token.length() - 8, token.length());
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.SECURITY_QUESTION, token, mobileOrEmail, System.currentTimeMillis() + SystemProperty.getLong("SecurityTokenExpiryInSeconds", 86400L) * 1000L);
            if (MemCachedClientWrapper.incr(MemCachedKeySpaces.RateLimitKeySpace.FORGOT_PASSWORD_ATTEMPTS, MemCachedKeyUtils.getFullKeyFromStrings(mobileOrEmail, accountEntrySourceData.ipAddress)) < 0L) {
               MemCachedClientWrapper.set(MemCachedKeySpaces.RateLimitKeySpace.FORGOT_PASSWORD_ATTEMPTS, MemCachedKeyUtils.getFullKeyFromStrings(mobileOrEmail, accountEntrySourceData.ipAddress), 1);
            }

            String forgotPasswordSMSPassword = SystemProperty.get("ForgotPasswordSMSPassword", "");
            if (forgotPasswordSMSPassword.length() > 0) {
               if (email) {
                  log.debug("Sending token via email: [" + mobileOrEmail + "] " + token);
                  messageEJB.sendEmailFromNoReply(mobileOrEmail, "migme account password reset token", forgotPasswordSMSPassword.replaceAll("%1", token));
               } else {
                  SystemSMSData systemSMSData = new SystemSMSData();
                  systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
                  systemSMSData.subType = SystemSMSData.SubTypeEnum.FORGOT_PASSWORD;
                  systemSMSData.username = username;
                  systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
                  systemSMSData.destination = mobileOrEmail;
                  systemSMSData.messageText = forgotPasswordSMSPassword.replaceAll("%1", token);
                  messageEJB.sendSystemSMS(systemSMSData, (long)SystemProperty.getInt("ForgotPasswordSMSDelay"), accountEntrySourceData);
               }
            }
         }
      } catch (CreateException var31) {
         throw new EJBException(var31.getMessage());
      } catch (SQLException var32) {
         throw new EJBException(var32.getMessage());
      } catch (NoSuchFieldException var33) {
         throw new EJBException(var33.getMessage());
      } catch (KeyczarException var34) {
         throw new EJBException(var34.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var28) {
            conn = null;
         }

      }

   }

   public AuthenticationServiceResponseCodeEnum validateUserCredential(String username, String password, PasswordType passwordType) {
      AuthenticationServicePrx authPrx = EJBIcePrxFinder.getAuthenticationServiceProxy();
      return authPrx.checkCredentialByUsername(username, password, passwordType.value());
   }

   public void changePassword(String username, String oldPassword, String newPassword) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);
      AuthenticationServicePrx authPrx = EJBIcePrxFinder.getAuthenticationServiceProxy();
      log.info("attempting to update password for user [" + username + "]");

      try {
         newPassword = newPassword.trim();
         this.checkPassword(username, newPassword);
         AuthenticationServiceResponseCodeEnum responseCode;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT73368964_UseAuthForChangePwd_ENABLED, true)) {
            responseCode = authPrx.checkCredentialByUsername(username, oldPassword, PasswordType.FUSION.value());
            if (responseCode != AuthenticationServiceResponseCodeEnum.Success) {
               throw new EJBException("Incorrect username or password");
            }
         } else {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("select username from user where username=? and password=?");
            ps.setString(1, username);
            ps.setString(2, oldPassword);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Incorrect username or password");
            }

            rs.close();
            ps.close();
         }

         SurgeMail.deleteSurgeMailPassword(surgeMailMemcache, username);

         try {
            responseCode = authPrx.updateFusionCredential(new Credential(0, username, newPassword, PasswordType.FUSION.value()), oldPassword);
            if (responseCode != AuthenticationServiceResponseCodeEnum.Success) {
               log.error("Failed to change password with authentication service " + responseCode.toString());
               throw new EJBException("Unable to update password for " + username);
            }
         } catch (FusionBusinessException var35) {
            throw new EJBException("Unable to update password for " + username + " : " + var35.message);
         } catch (FusionException var36) {
            log.error("failed to update password for user [" + username + "]", var36);
            throw new EJBException("Unable to update password for " + username);
         }

         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.PT73368964_UseAuthForChangePwd_ENABLED, true)) {
            if (conn == null) {
               conn = this.dataSourceMaster.getConnection();
            }

            ps = conn.prepareStatement("update user set password=? where username=?");
            ps.setString(1, newPassword);
            ps.setString(2, username);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to update password for " + username);
            }
         }

         if (userPrx != null) {
            userPrx.userDetailChanged(this.loadUser(username, false, true).toIceObject());
         }

         try {
            int userID = this.getUserID(username, (Connection)null);
            Enums.Mig33UserActionMisLogEnum action = Enums.Mig33UserActionMisLogEnum.CHANGE_PASSWORD;
            MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            misEJB.logMig33UserAction(userID, action, action.getDescriptionPattern());
         } catch (Exception var34) {
            log.warn("Unable to log change in password for user [" + username + "]", var34);
         }
      } catch (LocalException var37) {
      } catch (SQLException var38) {
         throw new EJBException(var38.getMessage());
      } catch (NoSuchFieldException var39) {
         throw new EJBException(var39.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var33) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

   }

   public void loginSucceeded(String username, String mobileDevice, String userAgent, String language) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Timestamp currentTime = new Timestamp(System.currentTimeMillis());

      try {
         UserData userData = this.loadUser(username, false, false);
         ConsecutiveLoginTrigger clTrigger = new ConsecutiveLoginTrigger(userData);
         clTrigger.amountDelta = 0.0D;
         clTrigger.quantityDelta = 1;
         clTrigger.currency = userData.currency;
         clTrigger.lastLoginDate = userData.lastLoginDate;
         RewardCentre.getInstance().sendTrigger(clTrigger);
         LastLoginTrigger llTrigger = new LastLoginTrigger(userData, currentTime);
         llTrigger.amountDelta = 0.0D;
         llTrigger.quantityDelta = 1;
         llTrigger.currency = userData.currency;
         llTrigger.lastLoginDate = userData.lastLoginDate;
         RewardCentre.getInstance().sendTrigger(llTrigger);
      } catch (Exception var25) {
         log.warn("Unable to send reward program trigger for login [" + username + "] :" + var25.getMessage(), var25);
      }

      try {
         try {
            Locale locale = new Locale(language.substring(0, 2));
            language = locale.getISO3Language().toUpperCase();
         } catch (Exception var24) {
            language = Locale.ENGLISH.getISO3Language().toUpperCase();
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set mobiledevice = ?, useragent = ?, language = ?, firstlogindate = if(firstlogindate is null and lastlogindate is null, now(), firstlogindate), lastlogindate = now() where username = ?");
         ps.setString(1, mobileDevice);
         ps.setString(2, userAgent);
         ps.setString(3, language);
         ps.setString(4, username);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("Failed to update user's successful login attempt");
         }
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

   }

   public void loginFailed(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set failedloginattempts = failedloginattempts + 1 where username = ?");
         ps.setString(1, username);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("Failed to update user's failed login attempt");
         }
      } catch (SQLException var16) {
         throw new EJBException(var16.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var15) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            conn = null;
         }

      }

   }

   public void activateAccountFailed(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set failedactivationattempts = failedactivationattempts + 1 where username = ?");
         ps.setString(1, username);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("Failed to update user's failed authentication attempt");
         }
      } catch (SQLException var16) {
         throw new EJBException(var16.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var15) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            conn = null;
         }

      }

   }

   public AlertMessageData getLatestAlertMessage(int midletVersion, AlertMessageData.TypeEnum type, int countryId, Date minimumDate, AlertContentType alertContentType, int clientType) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String sql;
      try {
         String key = null;
         if (alertContentType == null) {
            key = clientType + "/" + midletVersion + "/" + type + "/" + countryId;
         } else {
            key = clientType + "/" + midletVersion + "/" + type + "/" + countryId + "/" + alertContentType.value();
         }

         List<AlertMessageData> alertMessages = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.ALERT_MESSAGE, key);
         if (alertMessages == null) {
            sql = "select * from alertmessage where MinMidletVersion <= ? and MaxMidletVersion >= ? and Type = ? and (CountryID = ? or CountryID is null) and StartDate <= now() and ExpiryDate > now() and Status = ? and clientType = ?";
            if (alertContentType != null) {
               sql = sql + " and ContentType = ?";
            }

            sql = sql + " order by CountryID";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, midletVersion);
            ps.setInt(2, midletVersion);
            ps.setInt(3, type.value());
            ps.setInt(4, countryId);
            ps.setInt(5, AlertMessageData.StatusEnum.ACTIVE.value());
            ps.setInt(6, clientType);
            if (alertContentType != null) {
               ps.setInt(7, alertContentType.value());
            }

            rs = ps.executeQuery();
            alertMessages = new ArrayList();

            while(rs.next()) {
               ((List)alertMessages).add(new AlertMessageData(rs));
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.ALERT_MESSAGE, key, alertMessages);
         }

         if (((List)alertMessages).size() != 0) {
            List<Double> accumWeightList = new ArrayList();
            double totalWeight = 0.0D;

            Iterator i$;
            for(i$ = ((List)alertMessages).iterator(); i$.hasNext(); accumWeightList.add(totalWeight)) {
               AlertMessageData alertMessage = (AlertMessageData)i$.next();
               boolean expired = alertMessage.expiryDate.before(new Date());
               boolean neverSeen = minimumDate == null || alertMessage.dateCreated.after(minimumDate);
               if (!expired && (!alertMessage.onceOnly || neverSeen)) {
                  totalWeight += alertMessage.weighting;
               }
            }

            if (totalWeight > 0.0D) {
               double pick = this.randomGen.nextDouble();

               for(int i = 0; i < accumWeightList.size(); ++i) {
                  double accumWeight = (Double)accumWeightList.get(i);
                  if (accumWeight > 0.0D && accumWeight / totalWeight >= pick) {
                     AlertMessageData var20 = (AlertMessageData)((List)alertMessages).get(i);
                     return var20;
                  }
               }
            }

            i$ = null;
            return i$;
         }

         sql = null;
      } catch (SQLException var41) {
         throw new EJBException(var41.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var40) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var39) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var38) {
            conn = null;
         }

      }

      return sql;
   }

   public void setAccountStatus(String username, boolean status) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set status = ? where username = ?");
         if (status) {
            ps.setInt(1, UserData.StatusEnum.ACTIVE.value());
         } else {
            ps.setInt(1, UserData.StatusEnum.INACTIVE.value());
         }

         ps.setString(2, username);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Could not update user status: User does not exist");
         }
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public boolean getAccountStatus(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT status from user where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            Integer result = rs.getInt("status");
            rs.close();
            rs = null;
            if (result != null) {
               boolean var6 = result.equals(UserData.StatusEnum.ACTIVE.value());
               return var6;
            }
         }

         throw new EJBException("Unable to load account status from the DB for the user " + username);
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }
   }

   public void setFailedActivationAttempts(String username, int failedActivationAttempts) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set failedactivationattempts = ? where username = ?");
         ps.setInt(1, failedActivationAttempts);
         ps.setString(2, username);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to set " + username + "'s failed activation attempts to " + failedActivationAttempts);
         }
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public void bannedFromChatRoom(String room, String instigator, String target, String reason) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      UserPrx targetUserPrx = EJBIcePrxFinder.findOnewayUserPrx(target);

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set chatroombans = chatroombans + 1 where username = ?");
         ps.setString(1, target);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to log chat room ban");
         }

         UserData userData = this.loadUser(target, false, true);

         try {
            EmailUserNotification note = null;
            int bansBeforeSuspension = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BANS_BEFORE_SUSPENSION);
            if (bansBeforeSuspension > 0 && userData.chatRoomBans == bansBeforeSuspension) {
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOM_BAN, target, "1", (long)(SystemProperty.getInt((String)"GlobalChatRoomSuspensionDuration", 24) * 60 * 60 * 1000));
            }

            int maxChatRoomBans = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CHATROOM_BANS);
            if (maxChatRoomBans > 0 && userData.chatRoomBans >= maxChatRoomBans) {
               note = new EmailUserNotification();
               note.message = target + " banned from chat room " + room + " by " + instigator + ". Permanent suspension from entering chat rooms";
            }

            if (note != null) {
               note.subject = "[BANNED FROM CHATROOM] " + target;
               note.emailAddress = SystemProperty.get("ReportChatRoomBanEmail");
               if (note.emailAddress.length() > 0) {
                  EJBIcePrxFinder.getUserNotificationServiceProxy().notifyUserViaEmail(note);
               }
            }
         } catch (Exception var24) {
            log.warn("Unable to send chat room ban notification email", var24);
         }

         if (targetUserPrx != null) {
            targetUserPrx.userDetailChanged(userData.toIceObject());
         }
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

   }

   public void decrementChatroomBanCounter(String target) {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("UPDATE user SET chatroombans=chatroombans-1 WHERE username=? AND chatroombans > 0");
         ps.setString(1, target);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed update chatroomban counter");
         }

         UserData userData = this.loadUser(target, false, true);
         UserPrx targetUserPrx = EJBIcePrxFinder.findOnewayUserPrx(target);
         if (targetUserPrx != null) {
            targetUserPrx.userDetailChanged(userData.toIceObject());
         }
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public String processEmailNotification(String username, String from, String subject, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      String password = null;
      String mobilePhone = null;
      boolean emailAlert = false;
      boolean emailAlertSent = false;
      double smsEmailAlertCost = 0.0D;
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select u.password, u.mobilephone, u.emailalert, u.emailalertsent, c.smsemailalertcost from user u, country c where u.username = ? and c.id = u.countryid");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to load details from the DB for the user " + username);
         }

         password = rs.getString("password");
         mobilePhone = rs.getString("mobilephone");
         Integer intVal = (Integer)rs.getObject("emailAlert");
         if (intVal != null) {
            emailAlert = intVal != 0;
         }

         intVal = (Integer)rs.getObject("emailAlertSent");
         if (intVal != null) {
            emailAlertSent = intVal != 0;
         }

         smsEmailAlertCost = rs.getDouble("smsemailalertcost");
         rs.close();
         rs = null;
         if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.EMAIL_ALERT, username) && emailAlert && !emailAlertSent) {
            try {
               MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.EMAIL_ALERT;
               systemSMSData.username = username;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = mobilePhone;
               systemSMSData.cost = smsEmailAlertCost;
               String smsText = SystemProperty.get("EmailAlertSMS").replaceAll("%1", username).replaceAll("%2", subject);
               if (smsText.length() > 160) {
                  smsText = smsText.substring(0, 159);
               }

               systemSMSData.messageText = smsText;
               messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
            } catch (CreateException var33) {
               throw new EJBException("Unable to send an SMS email alert to the user " + username + ": " + var33.getMessage());
            }

            this.setEmailAlertSent(username, true);
         }
      } catch (Exception var34) {
         throw new EJBException(var34.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var32) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var31) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var30) {
            conn = null;
         }

      }

      return password;
   }

   public void setEmailAlertSent(String username, boolean emailAlertSent) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set emailAlertSent = ? where username = ?");
         if (emailAlertSent) {
            ps.setInt(1, 1);
         } else {
            ps.setInt(1, 0);
         }

         ps.setString(2, username);
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not update EmailAlertSent: User does not exist");
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

   }

   public UserData createUserMerchant(UserData userData, UserProfileData userProfileData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED)) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else {
         try {
            UserData merchantUserData = this.loadUser(userData.merchantCreated, false, false);
            if (merchantUserData == null) {
               throw new EJBException("Invalid merchant username provided");
            } else if (merchantUserData.type.value() == UserData.TypeEnum.MIG33.value()) {
               throw new EJBException("You must be a migme merchant to create user accounts");
            } else if (merchantUserData.status.value() == UserData.StatusEnum.INACTIVE.value()) {
               throw new EJBException("Your account must be active to create users");
            } else {
               userData.password = this.generatePassword();
               userData = this.createUser(userData, userProfileData, false, new UserRegistrationContextData((String)null, false, RegistrationType.MOBILE_REGISTRATION), accountEntrySourceData);
               if (merchantUserData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                  AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
                  accountBean.tagMerchantPending((Connection)null, userData.userID, userData.username, merchantUserData.userID, merchantUserData.username);
               }

               this.sendMerchantActivatedUserSMS(userData.username, userData.password, userData.mobilePhone, accountEntrySourceData);
               return userData;
            }
         } catch (Exception var6) {
            throw new EJBException(var6.getMessage());
         }
      }
   }

   private String generatePassword() {
      String randomPassword = String.format("%1$04d", this.randomGen.nextInt(9000) + 1000);
      String charSet = "abcdefghijklmnopqrstuvwxyz";

      for(int i = 0; i < 2; ++i) {
         randomPassword = randomPassword + charSet.charAt(this.randomGen.nextInt(charSet.length()));
      }

      return this.scrambleString(randomPassword);
   }

   private String scrambleString(String word) {
      StringBuilder builder = new StringBuilder(word.length());
      boolean[] used = new boolean[word.length()];

      for(int i = 0; i < word.length(); ++i) {
         int rndIndex;
         do {
            rndIndex = (new Random()).nextInt(word.length());
         } while(used[rndIndex]);

         used[rndIndex] = true;
         builder.append(word.charAt(rndIndex));
      }

      return builder.toString();
   }

   public void sendLookouts(String username, AccountEntrySourceData accountEntrySourceData) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.LOOKOUT, username)) {
         Connection connMaster = null;
         Connection connSlave = null;
         PreparedStatement psSlave = null;
         PreparedStatement psMaster = null;
         ResultSet rs = null;

         try {
            connSlave = this.dataSourceSlave.getConnection();
            psSlave = connSlave.prepareStatement("select lookout.username, user.mobilephone, country.smslookoutcost from lookout, user, contact, currency, country where lookout.contactusername=? and lookout.username=user.username and lookout.contactusername=contact.username and lookout.username=contact.fusionusername and user.currency=currency.code and country.id = user.countryid and user.balance/currency.exchangerate> country.smslookoutcost and lookout.username not in (select blockusername from blocklist where username=?) and (lookout.datelastsent is null or lookout.datelastsent < DATE_SUB(now(), INTERVAL ? SECOND))");
            psSlave.setString(1, username);
            psSlave.setString(2, username);
            psSlave.setInt(3, SystemProperty.getInt("MinSecondsBetweenLookoutSMS"));
            rs = psSlave.executeQuery();
            HashMap stalkers = new HashMap();

            String stalkerUsername;
            while(rs.next()) {
               String stalkerUsername = rs.getString(1);
               stalkerUsername = rs.getString(2);
               double smsLookoutCost = rs.getDouble(3);
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.LOOKOUT;
               systemSMSData.username = stalkerUsername;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = stalkerUsername;
               systemSMSData.messageText = SystemProperty.get("LookoutSMS").replaceAll("%1", username).replaceAll("%2", stalkerUsername);
               systemSMSData.cost = smsLookoutCost;
               stalkers.put(stalkerUsername, systemSMSData);
            }

            rs.close();
            rs = null;
            psSlave.close();
            psSlave = null;
            connSlave.close();
            connSlave = null;
            if (stalkers.size() > 0) {
               connMaster = this.dataSourceMaster.getConnection();
               psMaster = connMaster.prepareStatement("update lookout set DateLastSent=now() where username=? and contactusername=?");
               Iterator i$ = stalkers.keySet().iterator();

               while(i$.hasNext()) {
                  stalkerUsername = (String)i$.next();
                  SystemSMSData systemSMSData = (SystemSMSData)stalkers.get(stalkerUsername);

                  try {
                     MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                     messageEJB.sendSystemSMS(systemSMSData, accountEntrySourceData);
                     psMaster.setString(1, stalkerUsername);
                     psMaster.setString(2, username);
                     psMaster.executeUpdate();
                  } catch (Exception var35) {
                  }
               }
            }
         } catch (Exception var36) {
            throw new EJBException(var36.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var34) {
               rs = null;
            }

            try {
               if (psSlave != null) {
                  psSlave.close();
               }
            } catch (SQLException var33) {
               psSlave = null;
            }

            try {
               if (psMaster != null) {
                  psMaster.close();
               }
            } catch (SQLException var32) {
               psMaster = null;
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var31) {
               connMaster = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var30) {
               connSlave = null;
            }

         }

      }
   }

   public boolean midletRegistrationDisabled(String username, String password, String registrationIPAddress) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var7;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select ip from blockedregistrationip where ? like ip limit 1");
         ps.setString(1, registrationIPAddress);
         rs = ps.executeQuery();
         if (rs.next()) {
            var7 = true;
            return var7;
         }

         rs.close();
         ps.close();
         ps = connSlave.prepareStatement("select password from blockedregistrationpassword where password = ?");
         ps.setString(1, password);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var7 = SystemProperty.getBool("midletRegistrationDisabled", false);
            return var7;
         }

         var7 = true;
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var25) {
            connSlave = null;
         }

      }

      return var7;
   }

   public GroupMemberData getGroupMember(String username, int groupID) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      GroupMemberData memberData;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "select m.*, u.displaypicture, sub.id vipsubscriptionid from groupmember m inner join user u on m.username=u.username inner join groups g on m.groupid=g.id left outer join service on (g.vipserviceid=service.id and service.status=?) left outer join subscription sub on (sub.serviceid=service.id and sub.status=? and sub.username=u.username) where m.username=? and m.groupid=? and m.status in (?, ?)";
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, ServiceData.StatusEnum.ACTIVE.value());
         ps.setInt(2, SubscriptionData.StatusEnum.ACTIVE.value());
         ps.setString(3, username);
         ps.setInt(4, groupID);
         ps.setInt(5, GroupMemberData.StatusEnum.ACTIVE.value());
         ps.setInt(6, GroupMemberData.StatusEnum.BANNED.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            memberData = new GroupMemberData(rs);
            memberData.displayPicture = rs.getString("displayPicture");
            GroupMemberData var8 = memberData;
            return var8;
         }

         memberData = null;
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var23) {
            connSlave = null;
         }

      }

      return memberData;
   }

   public GroupData getGroup(int id) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      GroupData groupData;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select groups.*, service.status vipservicestatus from groups LEFT OUTER JOIN service ON (groups.vipserviceid=service.id and service.status=1) where groups.id=? and groups.status=1");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            groupData = new GroupData(rs);
            rs.close();
            ps.close();
            GroupData var6 = groupData;
            return var6;
         }

         groupData = null;
      } catch (SQLException var24) {
         log.warn("Exception: " + var24.getMessage(), var24);
         throw new EJBException(var24.getMessage(), var24);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return groupData;
   }

   public boolean isUserMobileVerified(String username) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var5;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select mobileverified from user where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unknown user " + username);
         }

         var5 = rs.getBoolean(1);
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }

      return var5;
   }

   public boolean isUserMobileOrEmailVerified(String username) throws EJBException {
      return this.checkIsUserMobileOrEmailVerifiedFromDB(username);
   }

   private boolean checkIsUserMobileOrEmailVerifiedFromDB(String username) {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var5;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select u.mobileverified mobile, uea.verified email from user u, userid uid LEFT OUTER JOIN useremailaddress uea ON uid.id = uea.userid and uea.type = ? where u.username = uid.username and u.username = ?");
         ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unknown user " + username);
         }

         var5 = rs.getBoolean(1) || rs.getBoolean(2);
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }

      return var5;
   }

   public boolean isUserMobileOrEmailVerifiedWithTxSupported(String username) throws EJBException {
      return this.checkIsUserMobileOrEmailVerifiedFromDB(username);
   }

   public boolean isUserEmailVerified(String username) throws FusionEJBException {
      return this.checkIsUserEmailVerified(username);
   }

   private boolean checkIsUserEmailVerified(String username) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var5;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select uea.verified emailVerified from userid uid LEFT OUTER JOIN useremailaddress uea ON uid.id = uea.userid and uea.type = ? and uea.verified = true where uid.username = ? limit 1");
         ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new FusionEJBException("Unknown user " + username);
         }

         var5 = rs.getBoolean(1);
      } catch (SQLException var20) {
         throw new FusionEJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var17) {
            connSlave = null;
         }

      }

      return var5;
   }

   public boolean isUserEmailVerifiedWithTxSupport(String username) throws FusionEJBException {
      return this.checkIsUserEmailVerified(username);
   }

   public AuthenticatedAccessControlParameter getUserAuthenticatedAccessControlParameter(String username) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AuthenticatedAccessControlParameter var5;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select u.mobileverified mobile, uea.verified email from user u, userid uid LEFT OUTER JOIN useremailaddress uea ON uid.id = uea.userid and uea.type = ? where u.username = uid.username and u.username = ?");
         ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = null;
            return var5;
         }

         var5 = new AuthenticatedAccessControlParameter(username, rs.getBoolean("mobile"), rs.getBoolean("email"));
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage(), var24);
      } catch (IllegalArgumentException var25) {
         throw new EJBException(var25.getMessage(), var25);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return var5;
   }

   public boolean userAttemptedVerification(String username, int hours) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select if (count(*)<>0,1,0) from systemsms where username=? and type=1 and subtype=1 and datecreated > DATE_SUB(now(), INTERVAL ? HOUR)");
         ps.setString(1, username);
         ps.setInt(2, hours);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unknown user " + username);
         }

         var6 = rs.getBoolean(1);
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var18) {
            connSlave = null;
         }

      }

      return var6;
   }

   private UserSettingData getUserSetting(String username, UserSettingData.TypeEnum type) throws EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.WW519_EMAIL_NOTIFICATION_USER_SETTINGS_ENABLED)) {
         List userSettings = null;

         try {
            userSettings = this.getUserSettings(username);
         } catch (SQLException var6) {
            throw new EJBException(var6.getMessage());
         }

         if (null != userSettings) {
            Iterator i$ = userSettings.iterator();

            while(i$.hasNext()) {
               UserSettingData setting = (UserSettingData)i$.next();
               if (setting.type == type) {
                  return setting;
               }
            }
         }

         return null;
      } else {
         return this.getUserSetting_beforeWW519(username, type);
      }
   }

   private UserSettingData getUserSetting_beforeWW519(String username, UserSettingData.TypeEnum type) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserSettingData var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from usersetting where username = ? and type = ?");
         ps.setString(1, username);
         ps.setInt(2, type.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = null;
            return var6;
         }

         var6 = new UserSettingData(rs);
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return var6;
   }

   public void updateUserSetting(String username, UserSettingData.TypeEnum type, int value) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         String sql = "insert into usersetting (username, type, value) values (?,?,?) ON DUPLICATE KEY UPDATE value = ?";
         ps = connMaster.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, type.value());
         ps.setInt(3, value);
         ps.setInt(4, value);
         if (ps.executeUpdate() == 0) {
            throw new EJBException("Unable to update user setting for " + username);
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.USER_SETTING, username);
         EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.SETTINGS));
      } catch (SQLException var18) {
         throw new EJBException(var18.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var16) {
            connMaster = null;
         }

      }

   }

   public UserSettingData.AnonymousCallEnum getAnonymousCallSetting(String username) throws EJBException {
      UserSettingData userSettingData = this.getUserSetting(username, UserSettingData.TypeEnum.ANONYMOUS_CALL);
      return userSettingData == null ? UserSettingData.AnonymousCallEnum.defaultValue() : UserSettingData.AnonymousCallEnum.fromValue(userSettingData.value);
   }

   public void updateAnonymousCallSetting(String username, UserSettingData.AnonymousCallEnum setting) throws EJBException {
      this.updateUserSetting(username, UserSettingData.TypeEnum.ANONYMOUS_CALL, setting.value());
      UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);
      if (userPrx != null) {
         userPrx.anonymousCallSettingChanged(setting.value());
      }

   }

   public UserSettingData.MessageEnum getMessageSetting(String username) throws EJBException {
      UserSettingData userSettingData = this.getUserSetting(username, UserSettingData.TypeEnum.MESSAGE);
      return userSettingData == null ? UserSettingData.MessageEnum.defaultValue() : UserSettingData.MessageEnum.fromValue(userSettingData.value);
   }

   public void updateMessageSetting(String username, UserSettingData.MessageEnum setting) throws EJBException {
      this.updateUserSetting(username, UserSettingData.TypeEnum.MESSAGE, setting.value());
      UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);
      if (userPrx != null) {
         userPrx.messageSettingChanged(setting.value());
      }

   }

   public UserSettingData.EmailSettingEnum getEmailNotificationSetting(String username, UserSettingData.TypeEnum emailType) throws EJBException {
      UserSettingData userSettingData = this.getUserSetting(username, emailType);
      return userSettingData == null ? UserSettingData.EmailSettingEnum.defaultValue() : UserSettingData.EmailSettingEnum.fromValue(userSettingData.value);
   }

   public void updateEmailNotificationSetting(String username, UserSettingData.TypeEnum emailType, UserSettingData.EmailSettingEnum setting) throws EJBException {
      if (UserSettingData.isValidEmailTypeEnum(emailType)) {
         this.updateUserSetting(username, emailType, setting.value());
      }

   }

   public ReputationLevelData getReputationLevelByUserid(int userId) throws EJBException {
      return this.getReputationLevelByUserid(userId, false);
   }

   public ReputationLevelData getReputationLevelByUserid(int userId, boolean skipCacheCheck) throws EJBException {
      try {
         UserReputationScoreAndLevelData data = this.getReputationScoreAndLevel(userId, (Connection)null, skipCacheCheck);
         return MemCacheOrEJB.getReputationLevelDataForLevel(data.level, skipCacheCheck);
      } catch (Exception var4) {
         throw new EJBException(var4.getMessage());
      }
   }

   public ReputationLevelData getReputationLevel(String username) throws EJBException {
      return this.getReputationLevel(username, false);
   }

   public ReputationLevelData getReputationLevel(String username, boolean skipCacheCheck) throws EJBException {
      int userid = this.getUserID(username, (Connection)null);
      return this.getReputationLevelByUserid(userid, skipCacheCheck);
   }

   /** @deprecated */
   public void addReputationScore(int userid, int score, Connection suppliedConn) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         ch = new ConnectionHolder(this.dataSourceMaster, suppliedConn);
         Connection c = ch.getConnection();

         try {
            this.updateReputationScore(c, userid, score, true);
            this.invalidateCacheAndNotifyReputationScoreUpdated(userid);
         } finally {
            c.close();
         }
      } catch (Exception var32) {
         throw new EJBException(var32.getMessage(), var32);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ((PreparedStatement)ps).close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var28) {
            ch = null;
         }

      }

   }

   private void updateReputationScore(Connection conn, int userid, int score, boolean isScoreIncrement) throws SQLException, EJBException, Exception {
      this.updateReputationScoreAndGet(conn, userid, score, isScoreIncrement, false);
   }

   private UserReputationScoreAndLevelData updateReputationScoreAndGet(Connection conn, int userid, int score, boolean isScoreIncrement, boolean getScoreAfterUpdate) throws SQLException, EJBException, Exception {
      String sql = isScoreIncrement ? "INSERT INTO score VALUES(?,?,now()) ON DUPLICATE KEY UPDATE SCORE = SCORE + ?, lastUpdated=now()" : "INSERT INTO score VALUES(?,?,now()) ON DUPLICATE KEY UPDATE SCORE = ?, lastUpdated=now()";
      PreparedStatement ps = conn.prepareStatement(sql);

      try {
         ps.setInt(1, userid);
         ps.setInt(2, score);
         ps.setInt(3, score);
         int rowsAffected = ps.executeUpdate();
         if (rowsAffected == 1) {
            log.info("Awarded new " + score + " reputation score for userid: " + userid);
         } else {
            if (rowsAffected <= 1) {
               throw new EJBException("Unable to update score for userid: " + userid + " rowsAffected:" + rowsAffected);
            }

            if (isScoreIncrement) {
               log.info("Awarded additional " + score + " reputation score for userid: " + userid);
            } else {
               log.info("Awarded with " + score + " reputation score for userid: " + userid);
            }
         }
      } finally {
         ps.close();
      }

      if (getScoreAfterUpdate) {
         UserReputationScoreData userReputationScoreData = this.getUserReputationScoreFromDB(conn, userid);
         ReputationLevelScoreRanges.LevelScoreRangeEntry entry = ReputationLevelScoreRanges.getInstance().getLevelScoreRange(userReputationScoreData.score, (ConnectionCreator)(new ConnectionCreator.FromDataSource(this.dataSourceSlave)));
         UserReputationScoreAndLevelData result;
         if (entry == null) {
            log.warn("Unable to get LevelScoreRangeEntry for user [ " + userid + "]");
            result = null;
         } else {
            result = new UserReputationScoreAndLevelData(userid, userReputationScoreData.score, entry.level, userReputationScoreData.lastUpdated, true);
         }

         if (log.isDebugEnabled()) {
            log.debug("Update score userid:[" + userid + "],score:[" + score + "],isIncrement:[" + isScoreIncrement + "],current score and leveldata:[" + result + "]");
         }

         return result;
      } else {
         return null;
      }
   }

   public void updateReputationScore(int userid, int score, boolean isScoreIncrement) throws EJBException {
      try {
         Connection conn = this.dataSourceMaster.getConnection();

         try {
            this.updateReputationScore(conn, userid, score, isScoreIncrement);
         } finally {
            conn.close();
         }

         invalidateReputationScore(userid);
      } catch (EJBException var12) {
         throw var12;
      } catch (SQLException var13) {
         throw new EJBException("Failed to update score for userid: " + userid + " score:" + score + ". Exception:" + var13, var13);
      } catch (Exception var14) {
         throw new EJBException("Unhandled exception:" + var14, var14);
      }
   }

   public UserReputationScoreAndLevelData updateReputationScoreAndGet(int userid, int score, boolean isScoreIncrement) throws EJBException {
      try {
         Connection conn = this.dataSourceMaster.getConnection();

         UserReputationScoreAndLevelData result;
         try {
            result = this.updateReputationScoreAndGet(conn, userid, score, isScoreIncrement, true);
         } finally {
            conn.close();
         }

         invalidateReputationScore(userid);
         return result;
      } catch (EJBException var13) {
         throw var13;
      } catch (SQLException var14) {
         throw new EJBException("Failed to update score for userid: " + userid + " score:" + score + ". Exception:" + var14, var14);
      } catch (Exception var15) {
         throw new EJBException("Unhandled exception:" + var15, var15);
      }
   }

   private static void invalidateReputationScore(int userid) {
      String userIdStr = Integer.toString(userid);

      try {
         if (log.isDebugEnabled()) {
            log.debug("Invalidating reputation score cache [" + MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, userIdStr) + "]");
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, userIdStr);
      } catch (Exception var3) {
         log.error("unable to invalidate reputation score cache [" + MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, userIdStr) + "].Exception:" + var3, var3);
      }

   }

   public void invalidateCacheAndNotifyReputationScoreUpdated(int userid) {
      invalidateReputationScore(userid);
      String username = this.getUsernameByUserid(userid, (Connection)null);

      try {
         UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);
         if (null != userPrx) {
            userPrx.userReputationChanged();
         }
      } catch (Exception var4) {
         log.error("unable to notify a reputation score change for user object proxy for [" + username + "].Exception:" + var4, var4);
      }

   }

   private UserReputationScoreData getUserReputationScoreFromDB(Connection conn, int userid) throws SQLException {
      PreparedStatement pstmt = conn.prepareStatement("select * from score s where s.userid=?");

      UserReputationScoreData userReputationScoreData;
      try {
         pstmt.setInt(1, userid);
         ResultSet rs = pstmt.executeQuery();

         try {
            if (rs.next()) {
               userReputationScoreData = UserReputationScoreData.fromResultSet(rs);
               if (log.isDebugEnabled()) {
                  log.debug("User [" + userid + "] score [" + userReputationScoreData + "]");
               }
            } else {
               if (log.isDebugEnabled()) {
                  log.debug("User [" + userid + "] has no entries in the score table. Assigning with initial score");
               }

               userReputationScoreData = new UserReputationScoreData(userid, 0, new Timestamp(0L));
            }
         } finally {
            rs.close();
         }
      } finally {
         pstmt.close();
      }

      return userReputationScoreData;
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel(int userid, Connection conn) {
      return this.getReputationScoreAndLevel(userid, conn, false);
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel(int userid, Connection conn, boolean skipCacheCheck) throws EJBException {
      return this.getReputationScoreAndLevel(false, userid, skipCacheCheck);
   }

   public UserReputationScoreAndLevelData getReputationScoreAndLevel(boolean mustUseMaster, int userid, boolean skipCacheCheck) throws EJBException {
      try {
         UserReputationScoreAndLevelData cachedScoreData;
         boolean needToRefreshCache;
         if (skipCacheCheck) {
            cachedScoreData = null;
            needToRefreshCache = true;
         } else {
            cachedScoreData = (UserReputationScoreAndLevelData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(userid));
            if (log.isDebugEnabled()) {
               log.debug("ScoreData from cache:" + cachedScoreData);
            }

            needToRefreshCache = cachedScoreData == null || !cachedScoreData.isCompatible(mustUseMaster);
         }

         UserReputationScoreAndLevelData result;
         if (needToRefreshCache) {
            Connection conn = (mustUseMaster ? this.dataSourceMaster : this.dataSourceSlave).getConnection();

            UserReputationScoreAndLevelData refreshedScoreData;
            try {
               refreshedScoreData = this.getReputationScoreAndLevelFromDB(conn, userid, mustUseMaster);
            } finally {
               conn.close();
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(userid), refreshedScoreData);
            result = refreshedScoreData;
         } else {
            result = cachedScoreData;
         }

         if (log.isDebugEnabled()) {
            log.debug((needToRefreshCache ? "(DB)" : "(Cached)") + " reputation data " + result);
         }

         return result;
      } catch (Exception var14) {
         throw new EJBException(var14.getMessage());
      }
   }

   private UserReputationScoreAndLevelData getReputationScoreAndLevelFromDB(Connection conn, int userid, boolean fromMaster) throws SQLException {
      String sql = "select s.*, max(r.level) as level from score s, reputationscoretolevel r where s.userid = ? and s.score >= r.score;";
      PreparedStatement ps = conn.prepareStatement("select s.*, max(r.level) as level from score s, reputationscoretolevel r where s.userid = ? and s.score >= r.score;");

      UserReputationScoreAndLevelData var8;
      try {
         ps.setInt(1, userid);
         ResultSet rs = ps.executeQuery();

         try {
            UserReputationScoreAndLevelData scoreAndLevelData;
            if (rs.next() && rs.getInt("level") != 0) {
               scoreAndLevelData = new UserReputationScoreAndLevelData(rs, fromMaster);
               if (log.isDebugEnabled()) {
                  log.debug("Found score data for user: " + userid + " score: " + scoreAndLevelData.score);
               }
            } else {
               scoreAndLevelData = new UserReputationScoreAndLevelData(userid, 0, 1, new Date(0L), fromMaster);
               if (log.isDebugEnabled()) {
                  log.debug("No score data found for user : " + userid + " using default values");
               }
            }

            var8 = scoreAndLevelData;
         } finally {
            rs.close();
         }
      } finally {
         ps.close();
      }

      return var8;
   }

   public Map<Integer, UserReputationScoreAndLevelData> getReputationScoreAndLevelForUsers(List<Integer> userIDs, Connection conn) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      HashMap<Integer, UserReputationScoreAndLevelData> resultMap = new HashMap();
      LinkedList<String> uncachedKeys = new LinkedList();
      String[] keys = new String[userIDs.size()];

      for(int i = 0; i < keys.length; ++i) {
         keys[i] = ((Integer)userIDs.get(i)).toString();
      }

      Map<String, Object> map = MemCachedClientWrapper.getMulti(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, keys);
      Iterator i$ = map.keySet().iterator();

      while(i$.hasNext()) {
         String s = (String)i$.next();
         UserReputationScoreAndLevelData scoreData = (UserReputationScoreAndLevelData)map.get(s);
         if (scoreData == null) {
            uncachedKeys.add(s);
         } else {
            resultMap.put(Integer.parseInt(s), scoreData);
         }
      }

      if (log.isDebugEnabled()) {
         log.debug("Found [" + resultMap.size() + "] cached entries for getReputationScoreAndLevelForUsers()");
         log.debug("Found [" + uncachedKeys.size() + "] uncached entries for getReputationScoreAndLevelForUsers()");
      }

      HashMap var32;
      try {
         if (uncachedKeys.size() > 0) {
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
            StringBuilder sql = new StringBuilder();
            sql.append("select s.*, max(r.level) as level from score s, reputationscoretolevel r where s.userid in (");

            int i;
            for(i = 0; i < uncachedKeys.size(); ++i) {
               sql.append('?');
               if (i < uncachedKeys.size() - 1) {
                  sql.append(',');
               }
            }

            sql.append(") and s.score >= r.score group by s.userid;");
            ps = ch.getConnection().prepareStatement(sql.toString());

            for(i = 0; i < uncachedKeys.size(); ++i) {
               ps.setInt(i + 1, Integer.parseInt((String)uncachedKeys.get(i)));
            }

            rs = ps.executeQuery();

            while(rs.next()) {
               if (rs.getInt("level") != 0) {
                  UserReputationScoreAndLevelData scoreData = new UserReputationScoreAndLevelData(rs, false);
                  if (log.isDebugEnabled()) {
                     log.debug("Found score data in the database for user: " + scoreData.userID + " score: " + scoreData.score);
                  }

                  resultMap.put(scoreData.userID, scoreData);
                  MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(scoreData.userID), scoreData);
               }
            }

            Iterator i$ = uncachedKeys.iterator();

            while(i$.hasNext()) {
               String s = (String)i$.next();
               if (!resultMap.containsKey(Integer.parseInt(s))) {
                  UserReputationScoreAndLevelData scoreData = new UserReputationScoreAndLevelData(Integer.parseInt(s), 0, 1, new Date(0L), false);
                  if (log.isDebugEnabled()) {
                     log.debug("No score data found for user : " + s + " using default values");
                  }

                  resultMap.put(scoreData.userID, scoreData);
                  MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.REPUTATION_SCORE, Integer.toString(scoreData.userID), scoreData);
               }
            }
         }

         var32 = resultMap;
      } catch (Exception var28) {
         throw new EJBException(var28.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var25) {
            ch = null;
         }

      }

      return var32;
   }

   public void createLookout(String creatorUsername, String contactUsername) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select username from lookout where username=? and contactusername=?");
         ps.setString(1, creatorUsername);
         ps.setString(2, contactUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("insert into lookout (username, contactusername) values (?, ?)");
            ps.setString(1, creatorUsername);
            ps.setString(2, contactUsername);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Failed to create Lookout");
            }
         }
      } catch (SQLException var21) {
         throw new EJBException("Sorry, an internal error occurred: " + var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

   }

   public double getPriceInUserCurrency(double originalPrice, String originalCurrencyCode, String username) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      double var9;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT ? / original_currency.ExchangeRate * currency_user.ExchangeRate Price, user.Currency UserCurrency FROM user, currency currency_user, currency original_currency WHERE user.Username=? and user.Currency=currency_user.Code and original_currency.Code=?";
         ps = connSlave.prepareStatement(sql);
         ps.setDouble(1, originalPrice);
         ps.setString(2, username);
         ps.setString(3, originalCurrencyCode);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new FusionEJBException(String.format("Unknown user '%s' or incorrect currency '%s'", username, originalCurrencyCode));
         }

         var9 = rs.getDouble(1);
      } catch (SQLException var25) {
         log.error("Exception occured in getPriceInUserCurrency: ", var25);
         throw new FusionEJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }

      return var9;
   }

   public UserMigboProfileData.AdminEnum getAdminLabel(String username) throws FusionEJBException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserProfileSettings.GET_ADMIN_LABELS_VERSION_1) ? this.getAdminLabelV1(username) : this.getAdminLabelV2(username);
   }

   private UserMigboProfileData.AdminEnum getAdminLabelV1(String username) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserMigboProfileData.AdminEnum var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT chatroomadmin FROM user WHERE username=?";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next() && rs.getInt(1) == 1) {
            var6 = UserMigboProfileData.AdminEnum.GLOBAL_ADMIN;
            return var6;
         }

         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var44) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var43) {
            ps = null;
         }

         sql = "SELECT createdby FROM groups WHERE createdby=? LIMIT 1";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var42) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var41) {
               ps = null;
            }

            sql = "SELECT username FROM chatroommoderator WHERE username=? LIMIT 1";
            ps = connSlave.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
               var6 = UserMigboProfileData.AdminEnum.CHATROOM_ADMIN;
               return var6;
            }

            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var40) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var39) {
               ps = null;
            }

            sql = "SELECT creator FROM chatroom WHERE creator=? LIMIT 1";
            ps = connSlave.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
               var6 = UserMigboProfileData.AdminEnum.CHATROOM_ADMIN;
               return var6;
            }

            var6 = UserMigboProfileData.AdminEnum.NOT_ADMIN;
            return var6;
         }

         var6 = UserMigboProfileData.AdminEnum.GROUP_ADMIN;
      } catch (SQLException var45) {
         log.error("SQLException occurred in getAdminLabel: " + var45);
         throw new FusionEJBException(var45.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var38) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var37) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var36) {
            connSlave = null;
         }

      }

      return var6;
   }

   private UserMigboProfileData.AdminEnum getAdminLabelV2(String username) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserMigboProfileData.AdminEnum var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT CASE WHEN u.chatroomadmin = 1 THEN 'A' WHEN (SELECT 1 FROM groups g WHERE g.createdby = u.username LIMIT 1) IS NOT NULL THEN 'GA' WHEN (SELECT 1 FROM chatroommoderator cm WHERE cm.username = u.username LIMIT 1) IS NOT NULL THEN 'CA' WHEN (SELECT 1 FROM chatroom c WHERE c.creator = u.username LIMIT 1) IS NOT NULL THEN 'CA' ELSE 'N' END adminType FROM user u WHERE username = ?";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.error("Unable to get admin label for user [" + username + "].");
            throw new FusionEJBException("Unable to get user label");
         }

         var6 = UserMigboProfileData.AdminEnum.fromValue(rs.getString("adminType"));
      } catch (SQLException var21) {
         log.error("SQLException occurred in getAdminLabel: " + var21);
         throw new FusionEJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var18) {
            connSlave = null;
         }

      }

      return var6;
   }

   public UserProfileLabelsData getUserLabels(String username) throws Exception {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String sql = null;

      UserProfileLabelsData var7;
      try {
         UserProfileLabelsData userProfileLabels = new UserProfileLabelsData();
         userProfileLabels.admin = this.getAdminLabel(username).value();
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserProfileSettings.GET_USER_MERCHANT_LABELS_ENABLED)) {
            userProfileLabels.merchant = this.getMerchantLabel(username).value();
         }

         sql = "SELECT IFNULL(verified, 0) verified FROM userid u      , userverified uv WHERE u.id = uv.userid AND u.username = ?";
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next()) {
            userProfileLabels.isVerified = rs.getBoolean("verified");
         } else {
            userProfileLabels.isVerified = false;
         }

         var7 = userProfileLabels;
      } catch (SQLException var22) {
         log.error("SQLException occurred in getUserLabels: " + var22);
         throw new FusionEJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var19) {
            connSlave = null;
         }

      }

      return var7;
   }

   public UserMigboProfileData.MerchantEnum getMerchantLabel(String username) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String sql = null;

      UserMigboProfileData.MerchantEnum var6;
      try {
         sql = "SELECT IFNULL(CASE WHEN m.mentor = 'mentor' THEN 'MT' \t      ELSE 'M' END, 'N') merchantType FROM userid u      , merchantdetails m WHERE u.id = m.id AND u.username = ?";
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = UserMigboProfileData.MerchantEnum.NOT_MERCHANT;
            return var6;
         }

         var6 = UserMigboProfileData.MerchantEnum.fromValue(rs.getString("merchantType"));
      } catch (SQLException var24) {
         log.error("SQLException occurred in getUserLabels: " + var24);
         throw new FusionEJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return var6;
   }

   public MerchantDetailsData getBasicMerchantDetails(String username) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MerchantDetailsData var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT * FROM merchantdetails WHERE id=(SELECT id FROM userid WHERE username=?)";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.debug("MerchantDetails not found for username [" + username + "]");
            var6 = null;
            return var6;
         }

         log.debug("MerchantDetails found for username [" + username + "]");
         var6 = new MerchantDetailsData(rs, false);
      } catch (SQLException var24) {
         log.error("SQLException occurred in getBasicMerchantDetails: " + var24);
         throw new FusionEJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return var6;
   }

   public boolean isMerchantMentor(String username) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT mentor FROM merchantdetails join userid on merchantdetails.id = userid.id where userid.username = ?";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (rs.next() && !rs.wasNull()) {
            String value = rs.getString(1);
            if (value != null && value.equals("mentor")) {
               boolean var7 = true;
               return var7;
            }
         }

         boolean var27 = false;
         return var27;
      } catch (SQLException var25) {
         log.error("SQLException occurred in getMerchantLabel: " + var25);
         throw new FusionEJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }
   }

   public double getUserReferralSuccessRate(String username, boolean refreshCache) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      int minSystemSMSID = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIN_SYSTEMSMS_ID_FOR_30_DAY_SCAN);
      int minAccountEntryID = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIN_ACCOUNTENTRY_ID_FOR_30_DAY_SCAN);

      double var33;
      try {
         Double result = MemCachedClientWrapper.getDouble(MemCachedKeySpaces.CommonKeySpace.USER_REFERRAL_SUCCESS_RATE, username);
         if (refreshCache || result == null) {
            int windowDurationInDays = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.REFERRAL_SUCCESS_RATIO_EVALUATION_WINDOW_IN_DAYS);
            connSlave = this.dataSourceSlave.getConnection();
            String getNumReferralsSQL = "select count(*) from systemsms s where s.type=? and s.subtype=? and s.username=? and s.datecreated > date_sub(now(), interval ? day) and s.id > ?";
            int numReferrals = 0;
            ps = connSlave.prepareStatement(getNumReferralsSQL);
            ps.setInt(1, SystemSMSData.TypeEnum.STANDARD.value());
            ps.setInt(2, SystemSMSData.SubTypeEnum.USER_REFERRAL.value());
            ps.setString(3, username);
            ps.setInt(4, windowDurationInDays);
            ps.setInt(5, minSystemSMSID);
            rs = ps.executeQuery();
            if (rs.next()) {
               numReferrals = rs.getInt(1);
            }

            rs.close();
            ps.close();
            if (numReferrals < 1) {
               double var34 = -1.0D;
               return var34;
            }

            String getNumReferralsAwardedSQL = "select count(*) from accountentry ae where ae.type=? and ae.username=? and ae.datecreated > date_sub(now(), interval ? day) and ae.id > ?";
            int numReferralsAwarded = 0;
            ps = connSlave.prepareStatement(getNumReferralsAwardedSQL);
            ps.setInt(1, AccountEntryData.TypeEnum.REFERRAL_CREDIT.value());
            ps.setString(2, username);
            ps.setInt(3, windowDurationInDays);
            ps.setInt(4, minAccountEntryID);
            rs = ps.executeQuery();
            if (rs.next()) {
               numReferralsAwarded = rs.getInt(1);
            }

            result = new Double(1.0D * (double)numReferralsAwarded / (double)numReferrals);
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_REFERRAL_SUCCESS_RATE, username, result.toString());
         }

         if (log.isDebugEnabled()) {
            log.debug("referral success rate [" + username + "] [" + result + "]");
         }

         var33 = result;
      } catch (Exception var31) {
         log.error("Unexpected exception: " + var31.getMessage(), var31);
         throw new FusionEJBException(var31.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var28) {
            connSlave = null;
         }

      }

      return var33;
   }

   public boolean isUserInMigboAccessList(int userId, int accessListType, int guardCapabilityType) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT COUNT(*) ctr_capability FROM guardcapability gc, guardsetcapability gsc, guardsetmember gsm, guardset gs WHERE gc.id = gsc.guardcapabilityid AND gsm.guardsetid = gsc.guardsetid AND gsm.guardsetid = gs.id AND gsm.membertype = ? AND gc.id = ? AND gsm.memberid = ?";
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, accessListType);
         ps.setInt(2, guardCapabilityType);
         ps.setInt(3, userId);
         rs = ps.executeQuery();
         if (rs.next()) {
            int value = rs.getInt(1);
            if (value > 0) {
               boolean var9 = true;
               return var9;
            }
         }

         boolean var29 = false;
         return var29;
      } catch (SQLException var27) {
         log.error("SQLException occurred in isUserLevelAllowedMigboAccess: " + var27);
         throw new FusionEJBException(var27.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var24) {
            connSlave = null;
         }

      }
   }

   public boolean isUserLevelAllowedMigboAccess(int userId, int guardCapabilityId) throws FusionEJBException {
      int MEMBERSHIP_TYPE = 3;
      String username = "";

      try {
         username = this.getUsernameByUserid(userId, (Connection)null);
      } catch (EJBException var32) {
         log.error("Unable to retrieve username: " + var32);
         throw new FusionEJBException(var32.getMessage());
      }

      boolean var5 = true;

      int userLevel;
      try {
         userLevel = this.getReputationLevel(username).level;
      } catch (EJBException var31) {
         log.error("Unable to retrieve user level: " + var31);
         throw new FusionEJBException(var31.getMessage());
      }

      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT gsm.memberid FROM guardcapability gc, guardsetcapability gsc, guardsetmember gsm, guardset gs WHERE gc.id = gsc.guardcapabilityid AND gsm.guardsetid = gsc.guardsetid AND gsm.guardsetid = gs.id AND gsm.membertype = ? AND gc.id = ?";
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, MEMBERSHIP_TYPE);
         ps.setInt(2, guardCapabilityId);
         rs = ps.executeQuery();
         if (rs.next()) {
            int value = rs.getInt(1);
            if (userLevel >= value) {
               boolean var11 = true;
               return var11;
            }
         }

         boolean var36 = false;
         return var36;
      } catch (SQLException var33) {
         log.error("SQLException occurred in isUserLevelAllowedMigboAccess: " + var33);
         throw new FusionEJBException(var33.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var28) {
            connSlave = null;
         }

      }
   }

   public boolean addUserEmailAddress(int userId, String emailAddress, UserEmailAddressData.UserEmailAddressTypeEnum type) throws FusionEJBException {
      if (emailAddress == null) {
         throw new FusionEJBException("Please provie a valid email address.");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         EmailUtils.EmailValidatationResult evr = EmailUtils.externalEmailIsValid(emailAddress);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.STRIP_PERIODS_FROM_GMAIL_ADDRESS_ENABLED) && evr.result == EmailUtils.EmailValidatationEnum.PERIODS_EXCEED_IN_GMAIL) {
            emailAddress = EmailUtils.stripPeriodsFromGmailAddress(emailAddress);
         } else if (evr.result != EmailUtils.EmailValidatationEnum.VALID) {
            throw new EJBException(evr.reason);
         }

         String username = "";

         try {
            username = this.getUsernameByUserid(userId, (Connection)null);
         } catch (EJBException var29) {
            log.error("Invalid userId in setting email address: " + userId);
            throw new FusionEJBException(var29.getMessage());
         }

         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("SELECT userid FROM useremailaddress WHERE emailaddress = ? ");
            ps.setString(1, emailAddress);
            rs = ps.executeQuery();
            if (rs.next()) {
               log.error(username + " is trying to register an email address that's already in use: " + emailAddress);
               throw new FusionEJBException("The email address " + emailAddress + " is already in use.");
            }

            rs.close();
            ps = conn.prepareStatement("INSERT INTO useremailaddress(userid, emailaddress, type) VALUES (?,?,?)");
            ps.setInt(1, userId);
            ps.setString(2, emailAddress);
            ps.setInt(3, type.value);
            if (ps.executeUpdate() != 1) {
               log.error("Unable to add emailaddress " + emailAddress + " to " + username);
               throw new FusionEJBException("Unable to add emailaddress " + emailAddress);
            }

            EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.USER_DETAIL));

            try {
               Enums.Mig33UserActionMisLogEnum action = Enums.Mig33UserActionMisLogEnum.ADD_EMAIL_ADDRESS;
               MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
               misEJB.logMig33UserAction(userId, action, String.format(action.getDescriptionPattern(), emailAddress));
            } catch (Exception var28) {
               log.warn(String.format("Unable to log adding of email address for user [%s] email address: [%s]", username, emailAddress), var28);
            }
         } catch (SQLException var30) {
            throw new EJBException(var30.getMessage());
         } catch (Exception var31) {
            throw new EJBException(var31.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var27) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var26) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var25) {
               conn = null;
            }

         }

         return true;
      }
   }

   public boolean removeUserEmailAddressByType(int userId, UserEmailAddressData.UserEmailAddressTypeEnum type) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (type == UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY) {
         log.error("Attempt to remove primary meail address detected for [" + userId + "]");
         throw new FusionEJBException("You are not allowed to remove your primary email address. Try modifying it instead.");
      } else {
         String username = "";

         try {
            username = this.getUsernameByUserid(userId, (Connection)null);
         } catch (EJBException var27) {
            log.error("Invalid id detected in removing email address: " + userId);
            throw new FusionEJBException(var27.getMessage());
         }

         try {
            log.info("Removing all emailaddress of user [" + username + "] of type " + type.value + "]");
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("DELETE FROM useremailaddress WHERE userid = ? AND type = ?");
            ps.setInt(1, userId);
            ps.setInt(2, type.value);
            ps.executeUpdate();
            EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.USER_DETAIL));
         } catch (SQLException var24) {
            throw new EJBException(var24.getMessage());
         } catch (Exception var25) {
            throw new EJBException(var25.getMessage());
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var23) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var21) {
               conn = null;
            }

         }

         return true;
      }
   }

   public boolean removeUserEmailAddress(int userId, String emailAddress) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String username = "";

      try {
         username = this.getUsernameByUserid(userId, (Connection)null);
      } catch (EJBException var27) {
         log.error("Invalid userId in setting email address: " + userId);
         throw new FusionEJBException(var27.getMessage());
      }

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("SELECT userid FROM useremailaddress WHERE emailaddress = ? AND type = 1");
         ps.setString(1, emailAddress);
         rs = ps.executeQuery();
         if (rs.next()) {
            log.error(username + " is trying to remove promary email address: " + emailAddress);
            throw new FusionEJBException("You are not allowed to remove your primary email address. Try modifying it instead.");
         }

         log.info("Removing emailaddress of user [" + username + "] " + emailAddress + "]");
         ps = conn.prepareStatement("DELETE FROM useremailaddress WHERE userid = ? AND emailAddress = ?");
         ps.setInt(1, userId);
         ps.setString(2, emailAddress);
         ps.executeUpdate();
         EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.USER_DETAIL));

         try {
            Enums.Mig33UserActionMisLogEnum action = Enums.Mig33UserActionMisLogEnum.DELETE_EMAIL_ADDRESS;
            MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            misEJB.logMig33UserAction(userId, action, String.format(action.getDescriptionPattern(), emailAddress));
         } catch (Exception var26) {
            log.warn(String.format("Unable to log delete  email address [%s] for user [%s]", emailAddress, username), var26);
         }
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } catch (Exception var29) {
         throw new EJBException(var29.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return true;
   }

   public boolean updateUserEmailAddress(int userId, String oldEmailAddress, String newEmailAddress, UserEmailAddressData.UserEmailAddressTypeEnum type) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String username = "";

      try {
         username = this.getUsernameByUserid(userId, (Connection)null);
      } catch (EJBException var30) {
         log.error("Invalid userId in setting email address: " + userId);
         throw new FusionEJBException(var30.getMessage());
      }

      if (StringUtil.isBlank(oldEmailAddress)) {
         throw new FusionEJBException("Please provide the email address to be modified.");
      } else if (type == null) {
         throw new FusionEJBException("Unable to modify email address - unrecognized email.");
      } else {
         EmailUtils.EmailValidatationResult evr = EmailUtils.externalEmailIsValid(newEmailAddress);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.STRIP_PERIODS_FROM_GMAIL_ADDRESS_ENABLED) && evr.result == EmailUtils.EmailValidatationEnum.PERIODS_EXCEED_IN_GMAIL) {
            newEmailAddress = EmailUtils.stripPeriodsFromGmailAddress(newEmailAddress);
         } else if (evr.result != EmailUtils.EmailValidatationEnum.VALID) {
            throw new EJBException(evr.reason);
         }

         try {
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("SELECT userid FROM useremailaddress WHERE emailaddress = ? AND type = 1");
            ps.setString(1, newEmailAddress);
            rs = ps.executeQuery();
            if (rs.next()) {
               log.error(username + " is trying to register an email address that's already in use: " + newEmailAddress);
               throw new FusionEJBException("The email address " + newEmailAddress + " is already in use.");
            }

            ps = conn.prepareStatement("UPDATE useremailaddress SET emailaddress = ?,verified = 0,dateverified = NULL WHERE userid = ? AND type = ? AND emailaddress = ?");
            ps.setString(1, newEmailAddress);
            ps.setInt(2, userId);
            ps.setInt(3, type.value);
            ps.setString(4, oldEmailAddress);
            if (ps.executeUpdate() == 0) {
               throw new EJBException("Unable to modify user email address.");
            }

            EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.USER_DETAIL));

            try {
               Enums.Mig33UserActionMisLogEnum action = Enums.Mig33UserActionMisLogEnum.CHANGE_EMAIL_ADDRESS;
               MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
               misEJB.logMig33UserAction(userId, action, String.format(action.getDescriptionPattern(), oldEmailAddress, newEmailAddress));
            } catch (Exception var29) {
               log.warn(String.format("Unable to log change in email address for user [%s] from [%s] to [%s]", username, oldEmailAddress, newEmailAddress), var29);
            }
         } catch (SQLException var31) {
            throw new EJBException(var31.getMessage());
         } catch (Exception var32) {
            throw new EJBException(var32.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var28) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var27) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var26) {
               conn = null;
            }

         }

         return true;
      }
   }

   public RegistrationTokenData verifyExternalEmailAddress(Integer requestorUserID, String token) throws FusionEJBException {
      String requestLogStmt = "verifyExternalEmailAddress:UserID [" + requestorUserID + "] token [" + token + "]:";
      log.info(requestLogStmt + "attempting to verify token");
      Jedis masterInstance = null;
      boolean useMemcached = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EMAIL_VERIFICATION_WITHOUT_USERNAME_ENABLED);

      RegistrationTokenData var34;
      try {
         String emailAddress = null;
         String keyRedis = null;
         int tokenOwnerUserID;
         String username;
         if (useMemcached) {
            username = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.EXTERNAL_EMAIL_VERIFICATION_TOKEN, token);
            if (username == null) {
               log.error("External email verification failed for requestorUserID [" + requestorUserID + "]: invalid token [" + token + "] - value not found in memcached");
               throw new EJBExceptionWithErrorCause(ErrorCause.EmailVerificationErrorReasonType.TOKEN_DOES_NOT_EXIST, new Object[0]);
            }

            String[] values = username.split("\\|");
            if (values.length != 2) {
               log.error("External email verification failed for requestorUserID [" + requestorUserID + "]: invalid token [" + token + "] - invalid value found in memcached");
               throw new EJBExceptionWithErrorCause(ErrorCause.EmailVerificationErrorReasonType.TOKEN_REFERS_TO_AN_INVALID_VALUE, new Object[0]);
            }

            emailAddress = values[0];

            try {
               tokenOwnerUserID = Integer.parseInt(values[1]);
            } catch (NumberFormatException var27) {
               log.error("External email verification failed for requestorUserID [" + requestorUserID + "]: invalid token [" + token + "] - invalid userId in value found in memcached");
               throw new EJBExceptionWithErrorCause(ErrorCause.EmailVerificationErrorReasonType.TOKEN_REFERS_TO_UNPARSEABLE_USERID, new Object[0]);
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.ENABLE_USER_ID_CHECK_WHEN_DEREFERENCING_TOKENS)) {
               log.error("External email verification failed for requestorUserID [" + requestorUserID + "]: invalid token [" + token + "] - owner of token is [" + tokenOwnerUserID + "]");
               if (tokenOwnerUserID != requestorUserID) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.EmailVerificationErrorReasonType.TOKEN_REFERS_TO_MISMATCHED_USERID, new Object[0]);
               }
            }
         } else {
            tokenOwnerUserID = requestorUserID;
            keyRedis = getRedisKeyForEmailVerificationToken(tokenOwnerUserID, token);
            masterInstance = Redis.getMasterInstanceForUserID(tokenOwnerUserID);
            emailAddress = masterInstance.get(keyRedis);
            if (emailAddress == null) {
               log.error("External email verification failed for requestorUserID [" + requestorUserID + "] tokenOwnerUserID [" + tokenOwnerUserID + "] invalid token [" + token + "] - value not found in redis");
               throw new EJBExceptionWithErrorCause(ErrorCause.EmailVerificationErrorReasonType.TOKEN_DOES_NOT_EXIST, new Object[0]);
            }
         }

         if (null == emailAddress) {
            log.error("External email verification failed for userid [" + requestorUserID + "]: invalid token [" + token + "]. tokenOwnerUserID [" + tokenOwnerUserID + "]");
            throw new EJBException("Invalid token provided.");
         }

         this.saveVerifiedUserPrimaryEmailAddressOnDatabase(tokenOwnerUserID, emailAddress);
         log.info(requestLogStmt + "saved to database");

         try {
            if (useMemcached) {
               purgeEmailVerificationTokenFromMemcache(tokenOwnerUserID, token);
            } else {
               purgeEmailVerificationTokenFromRedis(masterInstance, tokenOwnerUserID, token);
            }
         } catch (Exception var26) {
            log.error("Unable to delete verification token userid [" + tokenOwnerUserID + "]: invalid token [" + token + "]." + var26, var26);
         }

         username = this.getUsernameByUserid(tokenOwnerUserID, (Connection)null);
         EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(username, UserDataUpdatedEvent.TypeEnum.USER_DETAIL));

         try {
            Enums.Mig33UserActionMisLogEnum action = Enums.Mig33UserActionMisLogEnum.VERIFY_EMAIL_ADDRESS;
            MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            misEJB.logMig33UserAction(tokenOwnerUserID, action, String.format(action.getDescriptionPattern(), emailAddress));
         } catch (Exception var25) {
            log.warn(String.format("Unable to log verify  email address [%s] for user [%s]", emailAddress, username), var25);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.ENABLE_EXTERNAL_EMAIL_VERIFIED_EVENT_TRIGGER)) {
            UserData userData = this.loadUser(username, false, false);
            RegistrationContextData regContextData = this.getRegistrationContextData(tokenOwnerUserID);
            ExternalEmailVerifiedTrigger externalEmailVerifiedTrigger = new ExternalEmailVerifiedTrigger(userData, regContextData, emailAddress);

            try {
               RewardCentre.getInstance().sendTrigger(externalEmailVerifiedTrigger);
            } catch (Exception var24) {
               log.error("Unable to notify reward system to send ExternalEmailVerifiedTrigger.Trigger:[" + externalEmailVerifiedTrigger + "].Exception:" + var24, var24);
            }
         }

         log.info(requestLogStmt + "completed");
         var34 = new RegistrationTokenData("ok", username, emailAddress);
      } catch (EJBException var28) {
         throw var28;
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
      } catch (Exception var30) {
         throw new EJBException(var30.getMessage());
      } finally {
         Redis.disconnect(masterInstance, log);
      }

      return var34;
   }

   public static String getRedisKeyForEmailVerificationToken(int requestorUserID, String token) {
      return Redis.KeySpace.EXTERNAL_EMAIL_VERIFICATION_TOKEN.toString() + requestorUserID + ":" + token;
   }

   public static void purgeEmailVerificationTokenFromMemcache(int requestorUserID, String token) {
      MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.EXTERNAL_EMAIL_VERIFICATION_TOKEN, token);
   }

   public static void purgeEmailVerificationTokenFromRedis(Jedis masterInstance, int requestorUserID, String token) {
      masterInstance.del(getRedisKeyForEmailVerificationToken(requestorUserID, token));
   }

   public void saveVerifiedUserPrimaryEmailAddressOnDatabase(int userid, String emailAddress) {
      try {
         Connection conn = this.dataSourceMaster.getConnection();

         try {
            PreparedStatement ps = conn.prepareStatement("UPDATE useremailaddress SET verified = 1, dateverified = NOW() WHERE userid = ? AND emailaddress = ? AND verified = ?");

            try {
               ps.setInt(1, userid);
               ps.setString(2, emailAddress);
               ps.setInt(3, 0);
               if (ps.executeUpdate() < 1) {
                  throw new EJBException("Unable to verify email address.");
               }
            } finally {
               ps.close();
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.ENABLE_UPDATE_EMAIL_ADDRESS_FIELD_ON_USER_TABLE)) {
               try {
                  ps = conn.prepareStatement("UPDATE user u, userid ui SET u.emailaddress = ? WHERE u.username = ui.username AND ui.id = ?");

                  try {
                     ps.setString(1, emailAddress);
                     ps.setInt(2, userid);
                     ps.executeUpdate();
                  } finally {
                     ps.close();
                  }
               } catch (Exception var28) {
                  log.error("Partial failure updating the database.Unable to update user.emailaddress field for userid [" + userid + "] and emailaddress [" + emailAddress + "]." + var28, var28);
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.THROW_ON_FAILED_UPDATE_EMAIL_ADDRESS_FIELD_ON_USER_TABLE)) {
                     throw new EJBException("Partial failure updating the database", var28);
                  }
               }
            }
         } finally {
            conn.close();
         }

      } catch (SQLException var30) {
         throw new EJBException("Database update error." + var30, var30);
      }
   }

   private boolean verifyEmailRegistration(String username, String emailAddress) {
      if (!this.isEmailRegistrationV2Enabled()) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         Object rs = null;

         boolean var7;
         try {
            conn = this.userRegistrationMaster.getConnection();
            String statement = "UPDATE userregistration SET verified=1, verificationtime=NOW() WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMin() + ",now()) AND username=? AND emailaddress=?";
            ps = conn.prepareStatement(statement);
            ps.setString(1, username);
            ps.setString(2, emailAddress);
            if (ps.executeUpdate() < 1) {
               log.warn("Unable to update verification time of email");
            }

            var7 = true;
         } catch (SQLException var23) {
            throw new EJBException(var23.getMessage());
         } catch (Exception var24) {
            throw new EJBException(var24.getMessage());
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var22) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var21) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var20) {
               conn = null;
            }

         }

         return var7;
      }
   }

   private int getEmailRegistrationTimelimitMin() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_TIMELIMIT_IN_MINUTES);
   }

   private int getEmailRegistrationTimelimitMax() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_TIMELIMIT_IN_MINUTES) + SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.EMAIL_REGISTRATION_GRACE_PERIOD_IN_MINUTES);
   }

   public boolean isEmailRegistrationNotVerifiedForUsername(String username) {
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection conn = null;

      boolean var5;
      try {
         conn = this.userRegistrationSlave.getConnection();
         ps = conn.prepareStatement("SELECT * FROM userregistration WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMin() + ",now()) AND username=? AND verified=0");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = false;
            return var5;
         }

         var5 = true;
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } catch (Exception var25) {
         throw new EJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return var5;
   }

   public boolean isEmailRegistrationNotVerifiedForUsername(String username, String emailAddress) {
      if (!this.isEmailRegistrationV2Enabled()) {
         throw new EJBException(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.REGISTRATION_DISABLED_ERROR_MESSAGE));
      } else {
         PreparedStatement ps = null;
         ResultSet rs = null;
         Connection conn = null;

         boolean var6;
         try {
            conn = this.userRegistrationSlave.getConnection();
            ps = conn.prepareStatement("SELECT * FROM userregistration WHERE updatedtime > timestampadd(MINUTE," + -1 * this.getEmailRegistrationTimelimitMin() + ",now()) AND username=? AND emailAddress=? AND verified=0");
            ps.setString(1, username);
            ps.setString(2, emailAddress);
            rs = ps.executeQuery();
            if (rs.next()) {
               var6 = true;
               return var6;
            }

            var6 = false;
         } catch (SQLException var25) {
            throw new EJBException(var25.getMessage());
         } catch (Exception var26) {
            throw new EJBException(var26.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var24) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var23) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var22) {
               conn = null;
            }

         }

         return var6;
      }
   }

   public boolean isExternalEmailVerified(int userId, String emailAddress) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT verified FROM useremailaddress WHERE userid = ? AND emailaddress = ?");
         ps.setInt(1, userId);
         ps.setString(2, emailAddress);
         rs = ps.executeQuery();
         boolean emailVerified = false;
         if (!rs.next()) {
            throw new FusionEJBException("Unknown email address.");
         }

         emailVerified = rs.getBoolean("verified");
         var7 = emailVerified;
      } catch (SQLException var22) {
         throw new FusionEJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var7;
   }

   public UserEmailAddressData getUserEmailAddressByType(int userId, UserEmailAddressData.UserEmailAddressTypeEnum type) throws FusionEJBException {
      if (userId <= 0) {
         throw new FusionEJBException("Unknown user.");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         UserEmailAddressData var8;
         try {
            String sql = "SELECT * FROM useremailaddress WHERE userid = ? AND type = ? ";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, type.value);
            rs = ps.executeQuery();
            UserEmailAddressData emailAddress = null;
            if (rs.next()) {
               emailAddress = new UserEmailAddressData(rs);
            }

            var8 = emailAddress;
         } catch (SQLException var23) {
            throw new FusionEJBException(var23.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var22) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var21) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var20) {
               conn = null;
            }

         }

         return var8;
      }
   }

   public String getUsernameByEmailAddress(String emailAddress, UserEmailAddressData.UserEmailAddressTypeEnum type) throws FusionEJBException {
      EmailUtils.EmailValidatationResult evr = EmailUtils.externalEmailIsValid(emailAddress);
      if (evr.result != EmailUtils.EmailValidatationEnum.VALID) {
         throw new FusionEJBException(evr.reason);
      } else if (type == null) {
         log.warn("User email address type is null. email address [" + emailAddress + "]");
         throw new FusionEJBException("Please provide the email address type.");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         String var9;
         try {
            conn = this.dataSourceSlave.getConnection();
            String sql = "SELECT ui.username FROM userid ui, \t   useremailaddress uea WHERE uea.userid = ui.id AND uea.type = ? AND uea.verified = 1 AND uea.emailaddress = ? ";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, type.value);
            ps.setString(2, emailAddress);
            rs = ps.executeQuery();
            String username = null;
            if (rs.next()) {
               username = rs.getString("username");
            }

            var9 = username;
         } catch (SQLException var24) {
            throw new FusionEJBException(var24.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var23) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var21) {
               conn = null;
            }

         }

         return var9;
      }
   }

   public void forgotUsernameViaEmailAddress(String emailAddress) throws FusionEJBException {
      EmailUtils.EmailValidatationResult evr = EmailUtils.externalEmailIsValid(emailAddress);
      if (evr.result != EmailUtils.EmailValidatationEnum.VALID) {
         throw new FusionEJBException(evr.reason);
      } else {
         try {
            String username = this.getUsernameByEmailAddress(emailAddress, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY);
            if (StringUtil.isBlank(username)) {
               throw new FusionEJBException("Sorry, we cannot find a migme account with email address " + emailAddress + ". Please provide the email address you have registered with us.");
            } else {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.ENABLED_FORGOT_USERNAME_EMAIL_WITH_TEMPLATE)) {
                  UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                  Map<String, String> params = new HashMap();
                  params.put("base_url", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL));
                  params.put("username", username);
                  unsProxy.sendTemplatizedEmailFromNoReply(emailAddress, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.FORGOT_USERNAME_EMAIL_TEMPLATE_ID), params);
               } else {
                  String subject = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotUsername.EMAIL_SUBJECT).replaceAll("%1", username);
                  String content = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ForgotUsername.EMAIL_CONTENT).replaceAll("%1", username).replaceAll("%2", username);
                  if (log.isDebugEnabled()) {
                     log.debug("Sending token via email: [" + emailAddress + "] subject [" + subject + "] content [" + content + "]");
                  }

                  MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                  messageEJB.sendEmailFromNoReply(emailAddress, subject, content);
               }

            }
         } catch (Exception var7) {
            log.error("Unable to send forgot username request emailaddress [" + emailAddress + "]", var7);
            throw new FusionEJBException("Sorry, we are unable to process your request at the moment. please try again later.");
         }
      }
   }

   public String[] getUsersInUserCategory(int categoryId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList usernames = new ArrayList();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT ui.username FROM usertousercategory uc, userid ui WHERE uc.userid = ui.id AND uc.usercategoryid = ?");
         ps.setInt(1, categoryId);
         rs = ps.executeQuery();

         while(rs.next()) {
            usernames.add(rs.getString("username"));
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return (String[])usernames.toArray(new String[usernames.size()]);
   }

   public Map<Integer, String[]> getUserCategoryNames(int userId) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      HashMap categories = new HashMap();

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT uc.id, uc.name, uc.type FROM usertousercategory u2uc, userid ui, usercategory uc WHERE u2uc.userid = ui.id AND uc.id = u2uc.usercategoryid AND u2uc.userid = ?");
         ps.setInt(1, userId);
         rs = ps.executeQuery();
         new ArrayList();

         while(rs.next()) {
            if (!categories.containsKey(rs.getInt("type"))) {
               categories.put(rs.getInt("type"), new ArrayList());
            }

            ArrayList<String> currentCategoryList = (ArrayList)categories.get(rs.getInt("type"));
            currentCategoryList.add(rs.getString("name"));
            categories.put(rs.getInt("type"), currentCategoryList);
         }
      } catch (SQLException var21) {
         throw new FusionEJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      Map<Integer, String[]> returnCategories = new HashMap();
      Iterator i$ = categories.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Integer, ArrayList<String>> categoryType = (Entry)i$.next();
         returnCategories.put(categoryType.getKey(), ((ArrayList)categoryType.getValue()).toArray(new String[((ArrayList)categoryType.getValue()).size()]));
      }

      return returnCategories;
   }

   public boolean blacklistUser(int userId) {
      Connection connSlave = null;
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      label270: {
         boolean var6;
         try {
            connSlave = this.dataSourceSlave.getConnection();
            ps = connSlave.prepareStatement("SELECT * FROM guardsetmember WHERE (guardsetid=(SELECT guardsetid FROM guardsetcapability WHERE guardcapabilityid=?) AND memberid=? AND membertype=?)");
            ps.setInt(1, GuardCapabilityEnum.MIGBO_ACCESS.value());
            ps.setInt(2, userId);
            ps.setInt(3, MigboAccessMemberTypeEnum.BLACKLIST.value());
            rs = ps.executeQuery();
            if (!rs.next()) {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var35) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var34) {
                  ps = null;
               }

               connMaster = this.dataSourceMaster.getConnection();
               ps = connMaster.prepareStatement("INSERT INTO guardsetmember (guardsetid, memberid, membertype) VALUES((SELECT guardsetid FROM guardsetcapability WHERE guardcapabilityid=?), ?, ?)");
               ps.setInt(1, GuardCapabilityEnum.MIGBO_ACCESS.value());
               ps.setInt(2, userId);
               ps.setInt(3, MigboAccessMemberTypeEnum.BLACKLIST.value());
               if (ps.executeUpdate() == 1) {
                  break label270;
               }

               var6 = false;
               return var6;
            }

            var6 = true;
         } catch (SQLException var36) {
            throw new EJBException(var36.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var33) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var32) {
               ps = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var31) {
               connSlave = null;
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var30) {
               connMaster = null;
            }

         }

         return var6;
      }

      new UserData();
      UserData userData = this.loadUserFromID(userId);
      this.disconnectUser(userData.username, "blacklist user");
      return true;
   }

   public boolean removeUserFromBlacklist(int userId) {
      Connection connMaster = null;
      PreparedStatement ps = null;

      boolean var4;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("DELETE FROM guardsetmember WHERE (guardsetid=(SELECT guardsetid FROM guardsetcapability WHERE guardcapabilityid=?) AND memberid=? AND membertype=?)");
         ps.setInt(1, GuardCapabilityEnum.MIGBO_ACCESS.value());
         ps.setInt(2, userId);
         ps.setInt(3, MigboAccessMemberTypeEnum.BLACKLIST.value());
         if (ps.executeUpdate() == 1) {
            var4 = true;
            return var4;
         }

         var4 = false;
      } catch (SQLException var18) {
         throw new EJBException(var18.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var16) {
            connMaster = null;
         }

      }

      return var4;
   }

   public boolean banUser(int userId) {
      Connection connMaster = null;
      PreparedStatement ps = null;

      label116: {
         boolean var4;
         try {
            connMaster = this.dataSourceMaster.getConnection();
            ps = connMaster.prepareStatement("update user join userid on user.username = userid.username set user.status = 0 where userid.id = ?");
            ps.setInt(1, userId);
            if (ps.executeUpdate() == 1) {
               break label116;
            }

            var4 = false;
         } catch (SQLException var18) {
            throw new EJBException(var18.getMessage());
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var17) {
               ps = null;
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var16) {
               connMaster = null;
            }

         }

         return var4;
      }

      new UserData();
      UserData userData = this.loadUserFromID(userId);
      this.disconnectUser(userData.username, "ban user");
      return true;
   }

   public boolean suspendUser(int userId, int durationHours) {
      new UserData();
      UserData userData = this.loadUserFromID(userId);
      int durationMilliseconds = durationHours * 60 * 60 * 1000;
      MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.LOGIN_BAN, userData.username, "1", (long)durationMilliseconds);
      this.disconnectUser(userData.username, "suspend user");
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.LOG_USER_SUSPENSION)) {
         try {
            Enums.Mig33UserActionMisLogEnum action = Enums.Mig33UserActionMisLogEnum.SUSPEND_USER;
            MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            misEJB.logMig33UserAction(userId, action, String.format(action.getDescriptionPattern(), userData.username + "," + durationHours));
         } catch (Exception var7) {
            log.warn(String.format("Unable to log suspension of user [%s] ", userData.username), var7);
         }
      }

      return true;
   }

   public boolean disconnectUser(String username, String comment) {
      UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
      if (userPrx != null) {
         try {
            userPrx.disconnect(comment);
            return true;
         } catch (Exception var5) {
            log.error("Failed to disconnect user: " + var5.getMessage());
            return false;
         }
      } else {
         return false;
      }
   }

   public int getChatroomsOwnedCount(int userId) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var6;
      try {
         Integer count = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.CHATROOMS_OWNED_COUNT, "" + userId);
         if (count == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) as c from userid u, chatroom ch WHERE ch.creator=u.username and u.id=?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
               count = rs.getInt("c");
            }

            if (count == null) {
               count = new Integer(0);
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.CHATROOMS_OWNED_COUNT, "" + userId, count);
         }

         var6 = count;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public int getGiftsReceivedCount(int userId) {
      boolean useRedis = SystemProperty.getBool("UseRedisDataStore", true);
      Integer count = useRedis ? GiftsReceivedCounter.getCacheCount(userId) : MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.GIFTS_RECEIVED_COUNT, "" + userId);
      if (count != null) {
         return count;
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) as c FROM userid u, virtualgiftreceived vgr WHERE vgr.username=u.username and u.id=?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
               count = rs.getInt("c");
            }

            if (count == null) {
               count = new Integer(0);
            }
         } catch (SQLException var22) {
            throw new EJBException(var22.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var21) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var20) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var19) {
               conn = null;
            }

         }

         if (useRedis) {
            GiftsReceivedCounter.setCacheCount(userId, count);
         } else {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GIFTS_RECEIVED_COUNT, "" + userId, count);
         }

         return count;
      }
   }

   public int getGroupsJoinedCount(int userId) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var6;
      try {
         Integer count = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.GROUPS_JOINED_COUNT, "" + userId);
         if (count == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("SELECT count(*) as c FROM userid u, groupmember gm WHERE gm.username=u.username and u.id=?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
               count = rs.getInt("c");
            }

            if (count == null) {
               count = new Integer(0);
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GROUPS_JOINED_COUNT, "" + userId, count);
         }

         var6 = count;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public int getPhotosUploadedCount(int userId) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var6;
      try {
         Integer count = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.PHOTOS_UPLOADED_COUNT, "" + userId);
         if (count == null) {
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement("SELECT count(*) as c from userid u, scrapbook sb where sb.username=u.username and u.id=?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
               count = rs.getInt("c");
            }

            if (count == null) {
               count = new Integer(0);
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.PHOTOS_UPLOADED_COUNT, "" + userId, count);
         }

         var6 = count;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public RegistrationContextData getRegistrationContextData(int userId) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      RegistrationContextData var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT * from registrationcontext where userid = ?");
         ps.setInt(1, userId);
         rs = ps.executeQuery();
         var5 = new RegistrationContextData(rs);
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return var5;
   }

   public Map<String, Object> getTaggedUsers(int merchantUserId, int page, int numRecords) throws FusionEJBException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MerchantTagSettings.GET_MERCHANT_TAG_DETAILS_ENABLED)) {
         return null;
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         Map<String, Object> tags = new HashMap();
         tags.put("totalTags", 0);
         tags.put("tags", (Object)null);

         try {
            conn = this.dataSourceSlave.getConnection();
            String qry = "";
            Integer totalTags = MemCachedClientWrapper.getInt(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG_COUNT, "" + merchantUserId);
            if (totalTags == null) {
               qry = "SELECT COUNT(DISTINCT userid) totalTags FROM merchanttag WHERE merchantuserid = ?";
               ps = conn.prepareStatement(qry);
               ps.setInt(1, merchantUserId);
               rs = ps.executeQuery();
               if (rs.next()) {
                  totalTags = rs.getInt("totalTags");
                  MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG_COUNT, "" + merchantUserId, totalTags);
               }
            }

            tags.put("totalTags", totalTags);
            tags.put("tags", new ArrayList());
            if (totalTags > 0) {
               String taggerUsername = this.getUsernameByUserid(merchantUserId, (Connection)null);
               int offset = page * numRecords - numRecords;
               qry = "SELECT mt.id id FROM merchanttag mt     , ( SELECT MAX(id) id          FROM merchanttag          WHERE merchantuserid = ?          GROUP BY userid) mtOrd WHERE mt.id = mtOrd.id ORDER BY status DESC, mt.id DESC LIMIT ?, ?";
               ps = conn.prepareStatement(qry);
               ps.setInt(1, merchantUserId);
               ps.setInt(2, offset);
               ps.setInt(3, numRecords);
               rs = ps.executeQuery();
               ArrayList ids = new ArrayList();

               while(rs.next()) {
                  ids.add(rs.getString("id"));
               }

               Map<String, FullMerchantTagDetailsData> tmpTags = this.getMerchantTagDetailsData(ids, taggerUsername);
               tags.put("tags", tmpTags);
            }
         } catch (SQLException var29) {
            throw new FusionEJBException(var29.getMessage());
         } catch (Exception var30) {
            throw new FusionEJBException(var30.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var28) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var27) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var26) {
               conn = null;
            }

         }

         return tags;
      }
   }

   public Map<String, Object> getExpiringTaggedUsers(int merchantUserId, int daysToExpire) throws FusionEJBException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MerchantTagSettings.GET_MERCHANT_TAG_DETAILS_ENABLED)) {
         return null;
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         Map<String, Object> tags = new HashMap();
         tags.put("totalTags", 0);
         tags.put("tags", (Object)null);

         try {
            Calendar nonTopMerchantTagOffset = Calendar.getInstance();
            nonTopMerchantTagOffset.add(5, -(MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity() / 1440 - daysToExpire));
            Calendar topMerchantTagOffset = Calendar.getInstance();
            topMerchantTagOffset.add(5, -(MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity() / 1440 - daysToExpire));
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            conn = this.dataSourceSlave.getConnection();
            String qry = "SELECT mt.id id        , CASE WHEN u.type = ? THEN DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE)          ELSE DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE) END expirydate FROM merchanttag mt, userid uid, user u WHERE mt.userid = uid.id AND uid.username = u.username AND mt.merchantuserid = ? AND mt.status = ? AND ((u.type = ? AND mt.lastsalesdate < ?)      OR (u.type < ? AND mt.lastsalesdate < ?)) ORDER BY expirydate";
            ps = conn.prepareStatement(qry);
            ps.setInt(1, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setInt(2, MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
            ps.setInt(3, MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
            ps.setInt(4, merchantUserId);
            ps.setInt(5, MerchantTagData.StatusEnum.ACTIVE.value());
            ps.setInt(6, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setString(7, df.format(topMerchantTagOffset.getTime()));
            ps.setInt(8, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setString(9, df.format(nonTopMerchantTagOffset.getTime()));
            rs = ps.executeQuery();
            List<String> ids = new ArrayList();

            Integer rowCount;
            for(rowCount = 0; rs.next(); rowCount = rowCount + 1) {
               ids.add(rs.getString("id"));
            }

            tags.put("totalTags", rowCount);
            String taggerUsername = this.getUsernameByUserid(merchantUserId, (Connection)null);
            Map<String, FullMerchantTagDetailsData> tmpTags = this.getMerchantTagDetailsData(ids, taggerUsername);
            tags.put("tags", tmpTags);
            return tags;
         } catch (SQLException var30) {
            throw new FusionEJBException(var30.getMessage());
         } catch (Exception var31) {
            throw new FusionEJBException(var31.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var29) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var28) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var27) {
               conn = null;
            }

         }
      }
   }

   private Map<String, FullMerchantTagDetailsData> getMerchantTagDetailsData(List<String> tagsIds, String taggerUsername) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HashMap var7;
      try {
         Map<String, FullMerchantTagDetailsData> tmpTags = new HashMap();
         if (tagsIds.size() != 0) {
            int maxTaggedUsersToReturn = SystemProperty.getInt((String)"MaxTaggedUsersToReturn", 50);
            List<String> ids = new LinkedList(tagsIds);
            if (tagsIds.size() > maxTaggedUsersToReturn) {
               ids = ((List)ids).subList(0, maxTaggedUsersToReturn);
            }

            String query = "SELECT mt.id        ,ui.username        ,ui.id userID        ,mt.dateCreated        ,mt.lastSalesDate        ,CASE WHEN u.type = ? THEN ADDDATE(mt.lastsalesdate, INTERVAL ? MINUTE)         WHEN u.type < ? THEN ADDDATE(mt.lastsalesdate, INTERVAL ? MINUTE) END expiry        ,CASE WHEN u.type = ? AND ADDDATE(mt.lastsalesdate, INTERVAL ? MINUTE) > NOW() AND mt.status = 1 THEN 1         WHEN u.type < ? AND ADDDATE(mt.lastsalesdate, INTERVAL ? MINUTE) > NOW() AND mt.status = 1 THEN 1         ELSE 0 END status        ,u.type usertype        ,u.displaypicture        ,mt.merchantUserID        ,a.amount        ,a.currency        ,mu.username merchantusername \t   ,c.name country \t   ,up.gender \t   ,(COUNT(ct.username) > 0) isContact \t   ,up.status profileStatus \t   ,up.aboutMe FROM merchanttag mt LEFT JOIN accountentry a ON mt.accountentryid = a.id \t ,userid ui \t ,user u\t LEFT JOIN contact ct ON ct.username = u.username AND ct.fusionusername = ? \t LEFT JOIN userprofile up ON u.username = up.username \t ,userid mu \t ,country c WHERE mt.userid = ui.id AND mt.merchantuserid = mu.id AND ui.username = u.username AND u.countryid = c.id AND mt.id IN (" + StringUtil.join((Collection)ids, ",") + ") " + "GROUP BY u.username " + "ORDER BY status";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setInt(2, MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
            ps.setInt(3, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setInt(4, MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
            ps.setInt(5, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setInt(6, MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
            ps.setInt(7, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setInt(8, MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
            ps.setString(9, taggerUsername);
            rs = ps.executeQuery();
            boolean getUserLabelsEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserProfileSettings.GET_USER_LABELS_ENABLED);

            while(rs.next()) {
               String username = rs.getString("username");
               ReputationLevelData reputationLevelData = null;
               UserProfileLabelsData userProfileLabelsData = null;

               try {
                  reputationLevelData = this.getReputationLevel(username);
               } catch (EJBException var34) {
                  log.error("Failed to retrieve reputation level data: " + var34);
               }

               if (getUserLabelsEnabled) {
                  userProfileLabelsData = this.getUserLabels(username);
               }

               FullMerchantTagDetailsData tagDetails = new FullMerchantTagDetailsData(rs, reputationLevelData, userProfileLabelsData);
               tmpTags.put(username, tagDetails);
            }

            HashMap var39 = tmpTags;
            return var39;
         }

         var7 = tmpTags;
      } catch (SQLException var35) {
         throw new FusionEJBException(var35.getMessage());
      } catch (Exception var36) {
         throw new FusionEJBException(var36.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var33) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

      return var7;
   }

   public MerchantDetailsData getFullMerchantDetails(String username) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MerchantDetailsData var7;
      try {
         conn = this.dataSourceSlave.getConnection();
         String query = "SELECT  user.username        ,user.dateregistered AS dateregistered        ,user.displaypicture        ,userid.id        ,merchantdetails.mentor        ,merchantdetails.referrer        ,merchantdetails.username_color_type        ,user.mobilephone        ,CASE WHEN user.type=2 THEN 1         WHEN user.type=3 THEN 2 END type FROM  user      ,merchantdetails      ,userid WHERE user.username = userid.username AND merchantdetails.id = userid.id AND userid.username=? AND user.type IN (?, ?)";
         ps = conn.prepareStatement(query);
         ps.setString(1, username);
         ps.setInt(2, UserData.TypeEnum.MIG33_MERCHANT.value());
         ps.setInt(3, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new Exception("User " + username + " is not a merchant.");
         }

         MerchantDetailsData merchant = new MerchantDetailsData(rs, true);
         rs.close();
         ps.close();
         query = "SELECT datecreated AS lasttransfer FROM accountentry WHERE username = ? AND type = ? AND amount < 0 ORDER BY id DESC LIMIT 1";
         ps = conn.prepareStatement(query);
         ps.setString(1, username);
         ps.setInt(2, AccountEntryData.TypeEnum.USER_TO_USER_TRANSFER.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            merchant.lastTransfer = DateTimeUtils.getStringForMigcore(rs.getDate("lasttransfer"));
         }

         var7 = merchant;
      } catch (Exception var22) {
         throw new FusionEJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var7;
   }

   public boolean setMerchantColorType(Integer merchantId, MerchantDetailsData.UserNameColorTypeEnum color) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      boolean var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         String query = "UPDATE merchantdetails SET username_color_type = ? WHERE id = ?";
         ps = conn.prepareStatement(query);
         ps.setString(1, color.toString());
         ps.setInt(2, merchantId);
         if (ps.executeUpdate() != 1) {
            throw new Exception("Unable to set username color for user id [" + merchantId + "]");
         }

         var7 = true;
      } catch (Exception var22) {
         throw new FusionEJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var7;
   }

   public void responseToAlert(int notificationTypeCode, String alertKey, int userId, InvitationResponseData.ResponseType responseType) throws FusionEJBException {
      if (!Enums.NotificationTypeEnum.isForPersistent(notificationTypeCode)) {
         log.error(String.format("Can not response to a not-persistant alert, notification type:%s, userid:%s, key:%s", notificationTypeCode, userId, alertKey));
         throw new FusionEJBException(String.format("Can not response to a not-persistant alert, notification type:%s, userid:%s, key:%s", notificationTypeCode, userId, alertKey));
      } else {
         Jedis handle = null;
         Enums.NotificationTypeEnum notificationTypeEnum = Enums.NotificationTypeEnum.fromType(notificationTypeCode);

         try {
            handle = Redis.getMasterInstanceForUserID(userId);
            String unReadCountkey = UserNotificationServiceI.getUnreadCountUnsKey(userId, notificationTypeCode);
            String hashMapName = UserNotificationServiceI.getUnsKey(userId, notificationTypeCode);
            log.info(String.format("Response to alert, userid:%s, alertType:%s, alertKey:%s, responseType:%s", userId, hashMapName, alertKey, responseType));
            if (StringUtil.isBlank(alertKey)) {
               if (!Enums.NotificationTypeEnum.ACCUMULATED_SET.contains(notificationTypeEnum)) {
                  log.error(String.format("Can not response to reset a non-accumulated alert, notification type:%s, userid:%s, key:%s", notificationTypeCode, userId, alertKey));
                  throw new FusionEJBException(String.format("Can not response to reset a non-accumulated alert, notification type:%s, userid:%s, key:%s", notificationTypeCode, userId, alertKey));
               }

               handle.del(unReadCountkey);
               return;
            }

            Pipeline pipeline = handle.pipelined();
            Response<String> rawAlertResponse = pipeline.hget(hashMapName, alertKey);
            pipeline.decr(unReadCountkey);
            pipeline.sync();
            if (rawAlertResponse != null && !StringUtil.isBlank((String)rawAlertResponse.get())) {
               InvitationData.ActivityType activityType = null;
               if (notificationTypeEnum == Enums.NotificationTypeEnum.GAME_INVITE) {
                  activityType = InvitationData.ActivityType.PLAY_A_GAME;
               } else {
                  if (notificationTypeEnum != Enums.NotificationTypeEnum.GAME_HELP) {
                     log.info("Do not need to response to invitation");
                     return;
                  }

                  activityType = InvitationData.ActivityType.GAME_HELP;
               }

               Message message = NotificationUtils.getMessageFromString((String)rawAlertResponse.get());
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.ENABLE_LOG_INVITATION_RESPONSE_FOR_ALL_INVITERS_OF_COLLAPSE_ALERT) && message.parameters.containsKey("collapseInviterUserIdKey")) {
                  String rawInvitersStr = (String)message.parameters.get("collapseInviterUserIdKey");
                  String[] inviters = rawInvitersStr.split(":");
                  this.doResponseToInvitationForAlert((Connection)null, userId, activityType.getTypeCode(), responseType, inviters);
               } else if (message.parameters.containsKey("inviterUserId")) {
                  this.doResponseToInvitationForAlert((Connection)null, userId, activityType.getTypeCode(), responseType, (String)message.parameters.get("inviterUserId"));
               }
            }
         } catch (JedisException var21) {
            log.error(String.format("Failed to decrease unread count for user:%s, notification type:%s, notification key:%s", userId, notificationTypeCode, alertKey), var21);
            throw new FusionEJBException(String.format("Failed to decrease unread count for user:%s, notification type:%s, notification key:%s", userId, notificationTypeCode, alertKey), var21);
         } catch (Exception var22) {
            if (var22 instanceof FusionEJBException) {
               throw (FusionEJBException)var22;
            }

            log.error(String.format("Failed to decrease unread count for user:%s, notification type:%s, notification key:%s", userId, notificationTypeCode, alertKey), var22);
            throw new FusionEJBException(String.format("Failed to decrease unread count for user:%s, notification type:%s, notification key:%s", userId, notificationTypeCode, alertKey), var22);
         } finally {
            Redis.disconnect(handle, log);
         }

      }
   }

   private void doResponseToInvitationForAlert(Connection conn, int invitee, int activityType, InvitationResponseData.ResponseType responseType, String... inviters) {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (InvitationUtils.isInvitationEngineEnabled(InvitationData.ChannelType.INTERNAL)) {
         try {
            Timestamp actionTime = new Timestamp(System.currentTimeMillis());
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
            String sql = "select id from invitation where destination = ? and type = ? and channel = 4 and status = 1 and inviterUserId in (%s)";
            StringBuilder builder = new StringBuilder();

            int paramIndex;
            for(paramIndex = 0; paramIndex < inviters.length; ++paramIndex) {
               if (paramIndex > 0) {
                  builder.append(",");
               }

               builder.append("?");
            }

            ps = ch.getConnection().prepareStatement(String.format(sql, builder.toString()));
            ps.setString(1, invitee + "");
            ps.setInt(2, activityType);
            paramIndex = 3;

            for(int i = 0; i < inviters.length; ++i) {
               ps.setInt(paramIndex, Integer.valueOf(inviters[i]));
               ++paramIndex;
            }

            rs = ps.executeQuery();
            UserData userData = this.loadUserFromID(invitee);

            while(rs.next()) {
               InvitationData invitationData = this.getAndValidateSignUpInvitationData((Connection)null, rs.getInt("id"), actionTime);
               if (invitationData != null) {
                  InvitationResponseData invitationResponseData = this.logInvitationResponse((Connection)null, actionTime, invitationData, responseType, userData.username, InvitationData.StatusFieldValue.CLOSED);
                  log.info(String.format("Invitation Response: invitaionID:%s, inviteeID:%s, inviterID:%s, response:%s, activity:%s", invitationData.id, userData.userID, invitationData.inviterUserId, invitationResponseData.responseType, invitationData.type));
               }
            }

         } catch (SQLException var30) {
            throw new EJBException(var30.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var29) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var28) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var27) {
               ch = null;
            }

         }
      }
   }

   public void addMutualFollowing(int inviter, int invitee) throws FusionEJBException {
      try {
         MigboApiUtil api = MigboApiUtil.getInstance();
         String pathPrefix = String.format("/user/%s/following_request/%s?requestingUserid=%s&action=mutualfollow", inviter, invitee, inviter);
         api.postOneWay(pathPrefix, "");
      } catch (Exception var5) {
         throw new FusionEJBException("addMutualFollowing failed. " + var5.getMessage(), var5);
      }
   }

   public void addMutualFollowingAndTriggerMigAlerts(UserData inviterUserData, UserData inviteeUserData) throws FusionEJBException {
      this.addMutualFollowing(inviterUserData.userID, inviteeUserData.userID);
      this.sendMigAlertToInviterWhenInviteeRegister(inviterUserData, inviteeUserData);
   }

   public void addMutualFollowingAndTriggerMigAlertsToAllInviters(InvitationData invitationData, UserData inviteeUserData) throws FusionEJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         ch = new ConnectionHolder(this.dataSourceSlave, (Connection)null);
         ps = ch.getConnection().prepareStatement("select distinct inviteruserid from invitation where destination = ?");
         ps.setString(1, invitationData.destination);
         rs = ps.executeQuery();

         while(rs.next()) {
            int inviterUserID = rs.getInt("inviteruserid");
            UserData inviterUserData = this.loadUserFromID(inviterUserID);
            this.addMutualFollowingAndTriggerMigAlerts(inviterUserData, inviteeUserData);
         }
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage(), var22);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var19) {
            ch = null;
         }

      }

   }

   public void triggerFollowingAnUserAndSendingFollowingMeRequest(int inviter, int invitee) throws FusionEJBException {
      try {
         MigboApiUtil api = MigboApiUtil.getInstance();
         String pathPrefix = String.format("/user/%s/following_request/%s?requestingUserid=%s&action=follow", inviter, invitee, inviter);
         api.postOneWay(pathPrefix, "");
         UserData inviteeUserData = this.loadUserFromID(invitee);
         UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
         Map<String, String> parameters = new HashMap();
         long timestamp = System.currentTimeMillis();
         parameters.put("requestedTimestamp", String.valueOf(timestamp));
         String key = Integer.toString(inviter);
         unsProxy.notifyFusionUser(new Message(key, inviteeUserData.userID, inviteeUserData.username, Enums.NotificationTypeEnum.FOLLOWING_REQUEST.getType(), timestamp, parameters));
      } catch (Exception var11) {
         log.error("Failed to triggerFollowingAnUserAndSendingFollowingMeRequest", var11);
         throw new FusionEJBException(var11.getMessage());
      }
   }

   public void triggerSendGameInvitationNotification(int inviter, int invitee, SendingInvitationData data, int invitationId, InvitationData.ActivityType activityType) throws FusionEJBException {
      try {
         Enums.NotificationTypeEnum notificationTypeEnum = null;
         if (activityType == InvitationData.ActivityType.PLAY_A_GAME) {
            notificationTypeEnum = Enums.NotificationTypeEnum.GAME_INVITE;
         } else {
            if (activityType != InvitationData.ActivityType.GAME_HELP) {
               throw new FusionEJBException(String.format("ActivityType:%s is not supported in triggerSendGameInvitationNotification, expect PLAY_A_GAME or PLAY_A_GAME", activityType));
            }

            notificationTypeEnum = Enums.NotificationTypeEnum.GAME_HELP;
         }

         Map<String, String> parameters = new HashMap();
         long timestamp = System.currentTimeMillis();
         parameters.put("timestamp", String.valueOf(timestamp));
         parameters.put("inviterUserId", inviter + "");
         parameters.put("invitationId", invitationId + "");
         if (!StringUtil.isBlank(data.invitationMetadata.gameId)) {
            parameters.put("gameId", data.invitationMetadata.gameId);
            if (!StringUtil.isBlank(data.invitationMetadata.returnUrl)) {
               parameters.put("returnURL", data.invitationMetadata.returnUrl);
               UserData inviteeUserData = this.loadUserFromID(invitee);
               UserNotificationServicePrx var11 = EJBIcePrxFinder.getUserNotificationServiceProxy();
               var11.notifyFusionUser(new Message(data.invitationMetadata.gameId, inviteeUserData.userID, inviteeUserData.username, notificationTypeEnum.getType(), timestamp, parameters));
               log.info(String.format("Sending game invite invitation to user:%s, from inviter:%s, with invitationID:%s, gameId:%s, returnURL:%s", invitee, inviter, invitationId, data.invitationMetadata.gameId, data.invitationMetadata.returnUrl));
            } else {
               throw new FusionEJBException("SendingInvitationData.invitationMetadata.returnUrl can not be empty");
            }
         } else {
            throw new FusionEJBException("SendingInvitationData.invitationMetadata.gameId can not be empty");
         }
      } catch (Exception var12) {
         throw new FusionEJBException(var12.getMessage());
      }
   }

   public InvitationDetailsData getInvitationDetailsData(String invitationTokenCode, boolean fetchExtraParameters, Date timeOfAction) throws FusionEJBException {
      InvitationDetailsData invitationDetailsData = new InvitationDetailsData();
      int invitationId = InvitationUtils.decryptReferralInvitation(invitationTokenCode);
      if (invitationId < 0) {
         invitationDetailsData.invitationID = invitationId;
         invitationDetailsData.status = InvitationStatusEnum.INVALID.getTypeCode();
      } else {
         InvitationData invitationData = this.getInvitationData(invitationId, fetchExtraParameters, (Connection)null);
         invitationDetailsData = this.convertInvitationDataToInvitationDetailsData(invitationData, fetchExtraParameters, timeOfAction);
      }

      return invitationDetailsData;
   }

   public InvitationDetailsData getInvitationDetailsDataForFBInvite(String facebookRequestId, String facebookUserId, boolean fetchExtraParameters, Date timeOfAction) throws FusionEJBException {
      InvitationData invitationData = this.getInvitationDataForFBInvite(facebookRequestId, facebookUserId, fetchExtraParameters, (Connection)null);
      return this.convertInvitationDataToInvitationDetailsData(invitationData, fetchExtraParameters, timeOfAction);
   }

   public InvitationDetailsData convertInvitationDataToInvitationDetailsData(InvitationData invitationData, boolean fetchExtraParameters, Date timeOfAction) throws FusionEJBException {
      InvitationDetailsData invitationDetailsData = new InvitationDetailsData();
      if (invitationData == null) {
         invitationDetailsData.invitationID = -1;
      } else {
         invitationDetailsData.invitationID = invitationData.id;

         try {
            invitationDetailsData.invitationToken = InvitationUtils.encryptReferralInvitation(invitationData.id);
         } catch (Exception var7) {
            log.error(String.format("Failed to encrypt invitation id into invitation token, invitation id : %s", invitationData.id), var7);
            throw new FusionEJBException("Failed to encrypt invitation id into invitationToken: ", var7);
         }

         invitationDetailsData.activityType = invitationData.status.getTypeCode();
         invitationDetailsData.channelType = invitationData.channel.getTypeCode();
         invitationDetailsData.destination = invitationData.destination;
         if (fetchExtraParameters) {
            invitationDetailsData.extraParameters = new HashMap();
            Iterator i$ = invitationData.getParameterEntries().iterator();

            while(i$.hasNext()) {
               Entry<InvitationData.ParamType, String> paramEntry = (Entry)i$.next();
               invitationDetailsData.extraParameters.put(((InvitationData.ParamType)paramEntry.getKey()).getTypeCode(), paramEntry.getValue());
            }
         } else {
            invitationDetailsData.extraParameters = null;
         }

         invitationDetailsData.inviterUserID = invitationData.inviterUserId;
         invitationDetailsData.assignCreatedTime(invitationData.createdTime);
         invitationDetailsData.assignExpiredTime(invitationData.expireTime);
      }

      invitationDetailsData.status = InvitationUtils.getInvitationStatus(invitationData, timeOfAction).getTypeCode();
      return invitationDetailsData;
   }

   public String getDisplayPicture(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT displaypicture from user where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = null;
            return var5;
         }

         var5 = rs.getString(1);
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var5;
   }

   private UserData getMobileInviter(int userid) {
      UserData inviteeUserData = this.loadUserFromID(userid);
      if (inviteeUserData == null) {
         return null;
      } else {
         return !StringUtil.isBlank(inviteeUserData.referredBy) ? this.loadUser(inviteeUserData.referredBy, false, false) : null;
      }
   }

   private UserData getJoinMig33Inviter(int userid) {
      String var2 = " select inv.inviteruserid as inviteruserid from invitation inv join invitationresponse invres on  (inv.id = invres.invitationid  and inv.type=?  and invres.responsetype=?  and invres.username=?);";

      try {
         Connection conn = this.dataSourceSlave.getConnection();

         PreparedStatement pstmt;
         try {
            String inviteeUserName = this.getUsernameByUserid(userid, conn);
            if (!StringUtil.isBlank(inviteeUserName)) {
               pstmt = conn.prepareStatement(" select inv.inviteruserid as inviteruserid from invitation inv join invitationresponse invres on  (inv.id = invres.invitationid  and inv.type=?  and invres.responsetype=?  and invres.username=?);");

               try {
                  pstmt.setInt(1, InvitationData.ActivityType.JOIN_MIG33.getTypeCode());
                  pstmt.setInt(2, InvitationResponseData.ResponseType.SIGN_UP_VERIFIED.getTypeCode());
                  pstmt.setString(3, inviteeUserName);
                  ResultSet rs = pstmt.executeQuery();

                  try {
                     Object inviterUserID;
                     if (rs.next()) {
                        inviterUserID = rs.getInt("inviteruserid");
                        UserData var8 = this.loadUserFromID((int)inviterUserID);
                        return var8;
                     }

                     inviterUserID = null;
                     return (UserData)inviterUserID;
                  } finally {
                     rs.close();
                  }
               } finally {
                  pstmt.close();
               }
            }

            pstmt = null;
         } finally {
            conn.close();
         }

         return pstmt;
      } catch (SQLException var30) {
         throw new EJBException(var30.getMessage(), var30);
      }
   }

   public UserData getInviterForSignUp(int userid) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Invitation.ENABLE_GET_INVITER_FROM_MOBILE_REFERRER)) {
         UserData mobileInviter = this.getMobileInviter(userid);
         if (mobileInviter != null) {
            return mobileInviter;
         }
      }

      return this.getJoinMig33Inviter(userid);
   }

   static {
      broadcastListMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
      bclPersistedMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.bclPersisted);
      displayPictureAndStatusMessageMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.userDisplayPictureAndStatus);
      surgeMailMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.surgeMail);
      dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      USERALIAS_PATTERN = Pattern.compile("^[a-zA-Z](\\.?[\\w-])+$");
   }
}
