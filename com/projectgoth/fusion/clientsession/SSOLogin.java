package com.projectgoth.fusion.clientsession;

import Ice.LocalException;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.HashUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedDistributedLock;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.UsernameUtils;
import com.projectgoth.fusion.common.UsernameValidationException;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.exception.IceLocalExceptionAndContext;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.data.SSOSessionMetrics;
import com.projectgoth.fusion.restapi.util.RedisDataUtil;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.FusionCredential;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectExistsException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class SSOLogin {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SSOLogin.class));
   private static final Logger vasLog = Logger.getLogger("vaslog");
   private static final int[] DEFAULT_FUSION_SESSION_ALLOWED_DEVICE_TYPES;
   private static final int[] DEFAULT_SSO_SESSION_ALLOWED_VIEWS;
   public static final String LOGIN_FAILED_INTERNAL_ERROR_MESSAGE = "Login failed - Internal server error, please try again";
   public static final String INTERNAL_ERROR_MESSAGE = "Internal server error, please try again";
   private static final String INCORRECT_CREDENTIAL = "Username and password do not match";
   public static final String EMAIL_REGISTERED_USER_NOT_VERIFIED = "Please activate your account first or go to m.mig.me/resend to resend token";
   private static final long pow2_31 = 2147483648L;
   private static final long pow2_32 = 4294967296L;

   public static boolean isSSOLoginEnabled() {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.SSO_LOGIN_ENABLED);
   }

   public static boolean isSSOLoginEnabledClient(int deviceType, int clientVersion) {
      ClientType clientTypeEnum = ClientType.fromValue(deviceType);
      if (clientTypeEnum == null) {
         return false;
      } else {
         switch(clientTypeEnum) {
         case AJAX1:
         case AJAX2:
         case WAP:
         case BLAAST:
         case BLACKBERRY:
         case IOS:
            return true;
         case ANDROID:
            int androidSupportedVersion = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.SUPPORT_ANDROID_VERSION);
            return androidSupportedVersion > 0 && clientVersion >= androidSupportedVersion;
         case MIDP1:
         case MIDP2:
            int midletSupportedVersion = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.SUPPORT_MIDLET_VERSION);
            return midletSupportedVersion > 0 && clientVersion >= midletSupportedVersion;
         case MRE:
            return true;
         default:
            return false;
         }
      }
   }

   public static boolean isSSOLoginEnabledView(SSOEnums.View view, int clientVersion) {
      if (view == null) {
         return false;
      } else {
         int[] SSO_SESSION_ALLOWED_VIEWS = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.SSO_SESSION_ALLOWED_VIEWS, DEFAULT_SSO_SESSION_ALLOWED_VIEWS);
         int[] arr$ = SSO_SESSION_ALLOWED_VIEWS;
         int len$ = SSO_SESSION_ALLOWED_VIEWS.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            int allowedView = arr$[i$];
            if (view.value() == allowedView) {
               return true;
            }
         }

         ClientType clientTypeEnum = view.toFusionDeviceEnum();
         return isSSOLoginEnabledClient(clientTypeEnum == null ? 0 : clientTypeEnum.value(), clientVersion);
      }
   }

   public static boolean isFusionSessionAllowedForDeviceType(int deviceType) {
      int[] FUSION_SESSION_ALLOWED_DEVICE_TYPES = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FUSION_SESSION_ALLOWED_VIEWS, DEFAULT_FUSION_SESSION_ALLOWED_DEVICE_TYPES);
      int[] arr$ = FUSION_SESSION_ALLOWED_DEVICE_TYPES;
      int len$ = FUSION_SESSION_ALLOWED_DEVICE_TYPES.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         int type = arr$[i$];
         if (deviceType == type) {
            return true;
         }
      }

      return false;
   }

   public static boolean isEncryptedSessionIDSupported(int deviceType, int clientVersion) {
      ClientType clientTypeEnum = ClientType.fromValue(deviceType);
      if (clientTypeEnum == null) {
         return false;
      } else {
         switch(clientTypeEnum) {
         case AJAX1:
         case AJAX2:
         case WAP:
         case BLAAST:
         case BLACKBERRY:
         case IOS:
            return true;
         case ANDROID:
            int androidSupportedVersion = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FUSION_EID_ANDROID_VERSION);
            return androidSupportedVersion > 0 && clientVersion >= androidSupportedVersion;
         case MIDP1:
         case MIDP2:
            int midletSupportedVersion = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FUSION_EID_MIDLET_VERSION);
            return midletSupportedVersion > 0 && clientVersion >= midletSupportedVersion;
         case MRE:
            return true;
         default:
            return false;
         }
      }
   }

   public static byte[] getSSOEIDEncryptionKeyInBytes() {
      String encryptionKey = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.EID_ENCRPYTION_KEY);
      String encryptionKeyHex = HashUtils.asHex(HashUtils.md5(encryptionKey));
      log.debug(String.format("encryption key: %s , after hash: %s", encryptionKey, encryptionKeyHex));
      return encryptionKeyHex.getBytes();
   }

   public static String generateEncryptedSessionID(SSOEncryptedSessionIDInfo eidInfo) {
      return CodeIgniterEncryptionUtil.encryptToBase64(eidInfo.toJSON());
   }

   public static String getSessionIDFromEncryptedSessionID(String encryptedSessionID) {
      SSOEncryptedSessionIDInfo eidInfo = SSOEncryptedSessionIDInfo.fromJSON(CodeIgniterEncryptionUtil.decryptFromBase64(encryptedSessionID));
      if (eidInfo != null) {
         return eidInfo.sid;
      } else {
         log.error(String.format("Unable to decode encrypted session id %s", encryptedSessionID));
         return null;
      }
   }

   public static boolean isEncryptedSessionID(String encryptedSessionID) {
      return !StringUtil.isBlank(encryptedSessionID) && encryptedSessionID.length() > 70;
   }

   public static boolean setLoginDataInMemcache(String sid, SSOLoginData ld) {
      if (StringUtil.isBlank(sid)) {
         log.warn(String.format("Trying to set login data to memcache for empty sid"));
         return false;
      } else if (ld == null) {
         log.warn(String.format("Trying to set empty login data to memcache for sid", sid));
         return false;
      } else {
         boolean result;
         try {
            result = MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.LOGIN_DATA, sid, ld.toString(), SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.SESSION_EXPIRY_IN_SECONDS) * 1000L);
         } catch (Exception var4) {
            result = false;
         }

         log.info("Setting login data [" + MemCachedKeySpaces.CommonKeySpace.LOGIN_DATA + "/" + sid + "] [" + ld.username + "] [" + ld + "] success?[" + result + "]");
         return result;
      }
   }

   public static SSOLoginData getLoginDataFromMemcache(String sid) {
      if (StringUtil.isBlank(sid)) {
         log.warn(String.format("Trying to get login data from memcache for empty sid"));
         return null;
      } else {
         try {
            String data = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.LOGIN_DATA, sid);
            if (!StringUtil.isBlank(data)) {
               return new SSOLoginData(data);
            }
         } catch (Exception var2) {
            log.error("Unexpected exception while retrieving session data for [" + sid + "] from memcache: " + var2.getMessage());
         }

         return null;
      }
   }

   public static Map<String, String> getMultipleLoginDataStrFromMemcache(String[] sids) {
      return MemCachedClientWrapper.getMultiString(MemCachedKeySpaces.CommonKeySpace.LOGIN_DATA, sids);
   }

   public static boolean sessionIsActive(String sid) throws Exception {
      try {
         SSOLoginData sessionData = getLoginDataFromMemcache(sid);
         log.info("Getting session for [" + sid + "] data " + sessionData);
         return sessionData != null;
      } catch (Exception var2) {
         throw new Exception("Unable to retrieve session status: " + var2.getMessage());
      }
   }

   public static boolean removeDataFromMemcache(String sid) throws Exception {
      try {
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.LOGIN_DATA, sid);
         log.info("Cleared session for [" + sid + "]");
         return true;
      } catch (Exception var2) {
         throw new Exception("Unable to clear session: " + var2.getMessage());
      }
   }

   public static void logSSOClientSession(String sid, SSOEnums.View view, String username, Long sessionStartTimeInSeconds, String jsonStrPost) throws Exception {
      SSOSessionMetrics metrics = null;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.SESSION_ARCHIVE_ENABLED)) {
         try {
            log.debug(String.format("Logging session sid[%s] view [%s] username[%s] sessionStart [%s] jsondata [%s]", sid, view.toString(), username, (new Date(sessionStartTimeInSeconds * 1000L)).toString(), jsonStrPost));
            metrics = SSOSessionMetrics.fromJSONString(sid, view, username, sessionStartTimeInSeconds, jsonStrPost);
         } catch (Exception var7) {
            throw new Exception(String.format("Error Parsing Input [%s]"));
         }

         if (metrics != null) {
            RedisDataUtil.logSSOClientSession(metrics);
            log.info(String.format("Session logging successful for sid[%s] view [%s] username[%s] sessionStart [%s]", sid, view.toString(), username, (new Date(sessionStartTimeInSeconds * 1000L)).toString()));
         } else {
            log.warn(String.format("Error logging session sid[%s] view [%s] username[%s] sessionStart [%s] jsondata [%s]", sid, view.toString(), username, (new Date(sessionStartTimeInSeconds * 1000L)).toString(), jsonStrPost));
         }
      }

   }

   public static boolean doLogout(SSOLogoutSessionInfo sessionInfo, SSOLogoutClientContext context) {
      try {
         removeDataFromMemcache(sessionInfo.sessionID);
      } catch (Exception var4) {
         log.error(String.format("Unable to remove memcached entry to logout session %s, still proceeding to logout", sessionInfo.sessionID));
      }

      context.postLogout(sessionInfo);
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se501ClearMemcachedOnSsoDisconnectWapEnabled.getValue()) {
         try {
            removeSessionFromRecentWebOnlySidsKey(sessionInfo.connection.getUserID(), sessionInfo.sessionID);
         } catch (Exception var3) {
            log.error("Failed to clear session [sid:" + sessionInfo.sessionID + "] from recent web only sids key e=" + var3, var3);
         }
      }

      return true;
   }

   private static void removeSessionFromRecentWebOnlySidsKey(int userID, String sessionId) throws Exception {
      Jedis masterInstance = null;

      try {
         if (log.isDebugEnabled()) {
            log.debug(String.format("calling removeSessionFromRecentWebOnlySidsKey for userid: [%s] member:[%s]", userID, sessionId));
         }

         masterInstance = Redis.getMasterInstanceForUserID(userID);
         String key = Redis.getRecentWebOnlySidsKey(userID);
         long count = masterInstance.zrem(key, new String[]{sessionId});
         if (log.isDebugEnabled()) {
            log.debug(String.format("removeSessionFromRecentWebOnlySidsKey: removed count: [%s]", count));
         }
      } finally {
         Redis.disconnect(masterInstance, log);
      }

   }

   public static boolean getLoginSerializationLock(String username, long waitTimeMillis) {
      return MemCachedDistributedLock.getDistributedLock(MemCachedKeyUtils.getFullKeyFromStrings("LS", username), waitTimeMillis);
   }

   public static void releaseLoginSerializationLock(String username) {
      MemCachedDistributedLock.releaseDistributedLock(MemCachedKeyUtils.getFullKeyFromStrings("LS", username));
   }

   public static SSOLogin.SSOLoginResult doLogin(String username, String loginChallenge, Integer passwordHash, SSOLoginSessionInfo sessionInfo, boolean skipCaptchaCheck, SSOLoginClientContext context, boolean skipAuthenticationCheck) {
      if (!isSSOLoginEnabled()) {
         context.errorOccurred("Login is disabled.");
         return SSOLogin.SSOLoginResult.ERROR;
      } else {
         String exFLAKey;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.ENABLE_PRELOGIN_USERNAME_CHAR_VALIDATION)) {
            try {
               UsernameUtils.validateUsernameAsSearchKeyword(username);
            } catch (UsernameValidationException var37) {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.LOG_INVALID_USERNAME_ON_PRELOGIN)) {
                  exFLAKey = var37.getErrorCause() != null ? var37.getErrorCause().getCode() : "";
                  log.info("Received an invalid username.RemoteAddress:[" + sessionInfo.remoteAddress + "].ErrorCode:[" + exFLAKey + "].Username[" + MemCachedKeyUtils.getFullKeyFromStrings(username) + "]");
               }

               context.errorOccurred(FusionPktError.Code.INCORRECT_CREDENTIAL, "Username and password do not match");
               return SSOLogin.SSOLoginResult.ERROR;
            }
         }

         if (!StringUtil.isBlank(sessionInfo.sessionID)) {
            try {
               if (sessionIsActive(sessionInfo.sessionID)) {
                  return SSOLogin.SSOLoginResult.SESSION_ACTIVE;
               }
            } catch (Exception var36) {
               log.error(String.format("Unable to check sso session - %s", var36.getMessage()));
               context.errorOccurred("Login failed - Internal server error, please try again", var36);
               return SSOLogin.SSOLoginResult.ERROR;
            }
         } else {
            sessionInfo.sessionID = ConnectionI.newSessionID();
            log.info(String.format("Created new session id %s for user %s", sessionInfo.sessionID, username));
         }

         boolean loginLockAcquired = false;

         try {
            if (username == null || username.length() == 0) {
               context.errorOccurred(FusionPktError.Code.INCORRECT_CREDENTIAL, "Username and password do not match");
               SSOLogin.SSOLoginResult var49 = SSOLogin.SSOLoginResult.ERROR;
               return var49;
            } else {
               exFLAKey = getFailedLoginAttemptKey(username, sessionInfo.remoteAddress);
               SSOLogin.SSOLoginResult var54;
               if (!skipCaptchaCheck) {
                  long failedLoginAttempts = MemCachedClientWrapper.getCounter(MemCachedKeySpaces.RateLimitKeySpace.FAILED_LOGIN_ATTEMPTS, exFLAKey);
                  if (failedLoginAttempts > (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.MAX_FAILED_LOGIN_ATTEMPTS)) {
                     context.captchaRequired(sessionInfo);
                     var54 = SSOLogin.SSOLoginResult.REQUIRE_CAPTCHA;
                     return var54;
                  }

                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CAPTCHA_REQUIRED_ENABLED) && MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.CAPTCHA_REQUIRED, StringUtil.normalizeUsername(username)) != null) {
                     context.captchaRequired(sessionInfo);
                     var54 = SSOLogin.SSOLoginResult.REQUIRE_CAPTCHA;
                     return var54;
                  }
               }

               SSOLogin.SSOLoginResult var50;
               if (!skipAuthenticationCheck) {
                  try {
                     if (!authenticateUser(context, username, loginChallenge, passwordHash, sessionInfo)) {
                        var50 = SSOLogin.SSOLoginResult.ERROR;
                        return var50;
                     }
                  } catch (EJBException var38) {
                     log.error("Unable to find authentication service proxy to perform login", var38);
                     context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var38);
                     SSOLogin.SSOLoginResult var10 = SSOLogin.SSOLoginResult.ERROR;
                     return var10;
                  }
               } else {
                  log.info("Login requested by " + username + " without authentication. [sid: " + sessionInfo.sessionID + ", remoteIp: " + sessionInfo.remoteAddress + ", view: " + sessionInfo.view.name() + "]");
               }

               loginLockAcquired = getLoginSerializationLock(username, 0L);
               if (!loginLockAcquired) {
                  log.warn(String.format("Disallowed login from user '%s' due to concurrent login sessions.", username));
                  context.errorOccurred("Unable to login now. Please try again later.");
                  var50 = SSOLogin.SSOLoginResult.ERROR;
                  return var50;
               } else {
                  User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                  UserData userData = userEJB.loadUser(username, false, false);
                  if (userData == null) {
                     context.errorOccurred(FusionPktError.Code.INCORRECT_CREDENTIAL, "Username and password do not match");
                     var54 = SSOLogin.SSOLoginResult.ERROR;
                     return var54;
                  } else if (userData.status != UserData.StatusEnum.ACTIVE) {
                     context.errorOccurred("Account is not active");
                     var54 = SSOLogin.SSOLoginResult.ERROR;
                     return var54;
                  } else {
                     if (!userEJB.userAttemptedVerification(username, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.ALLOWED_VERIFICATION_WINDOW_IN_HOURS)) && !AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.LOGIN_AFTER_90DAYS, userData)) {
                        Date today = new Date();
                        Date dateRegistered = userData.dateRegistered;
                        long dateDiffInMsec = today.getTime() - dateRegistered.getTime();
                        if (dateDiffInMsec / DateTimeUtils.TWENTY_FOUR_HOURS_IN_MS > (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.UNAUTH_USER_ALLOW_LOGIN_IN_DAYS)) {
                           String message = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.UNAUTH_USER_LOGIN_BLOCK_MESSAGE);
                           context.errorOccurred(message);
                           SSOLogin.SSOLoginResult var58 = SSOLogin.SSOLoginResult.ERROR;
                           return var58;
                        }
                     }

                     if (userData.mobilePhone == null && !StringUtil.isBlank(userData.emailAddress) && !userData.emailVerified && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.EMAIL_UNAUTH_USER_LOGIN_DISABLED)) {
                        log.warn(String.format("Login denied due to email registered user not verified, username %s, email %s", username, userData.emailAddress));
                        context.errorOccurred("Please activate your account first or go to m.mig.me/resend to resend token");
                        var54 = SSOLogin.SSOLoginResult.ERROR;
                        return var54;
                     } else {
                        MemCachedClientWrapper.delete(MemCachedKeySpaces.RateLimitKeySpace.FAILED_LOGIN_ATTEMPTS, exFLAKey);
                        MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.CAPTCHA_REQUIRED, StringUtil.normalizeUsername(username));
                        MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                        CountryData countryData = misEJB.getCountry(userData.countryID);
                        if (countryData != null && countryData.iddCode != null) {
                           CurrencyData currencyData = misEJB.getCurrency(userData.currency);
                           if (currencyData == null) {
                              log.error(String.format("Invalid currency code for user %s, currency %s", username, userData.currency));
                              throw new LoginException("Internal server error, please try again");
                           } else if (createSSOSession(sessionInfo, passwordHash, userData)) {
                              context.ssoSessionCreated(sessionInfo, userData);
                              boolean sameMobileDevice = false;
                              if (userData.mobileDevice != null && sessionInfo.mobileDevice != null) {
                                 sameMobileDevice = userData.mobileDevice.equals(DataUtils.truncateMobileDevice(sessionInfo.mobileDevice));
                              }

                              boolean sameUserAgent = false;
                              if (userData.userAgent != null && sessionInfo.userAgent != null) {
                                 sameUserAgent = userData.userAgent.equals(DataUtils.truncateUserAgent(sessionInfo.userAgent));
                              }

                              boolean minLoginTimeExceeded = false;
                              int minLoginUpdatePeriod = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.MIN_LOGIN_UPDATE_PERIOD_IN_SECONDS);
                              if (userData.lastLoginDate != null) {
                                 minLoginTimeExceeded = Calendar.getInstance().getTimeInMillis() - userData.lastLoginDate.getTime() > (long)(minLoginUpdatePeriod * 1000);
                              }

                              if (sameMobileDevice && sameUserAgent && !minLoginTimeExceeded) {
                                 log.warn("User: [" + userData.username + "] logged in multiple times within the space of [" + minLoginUpdatePeriod + "] seconds. Mobile Device: [" + sessionInfo.mobileDevice + "], UserAgent: [" + sessionInfo.userAgent + "], Remote IP: [" + sessionInfo.remoteAddress + "]");
                              } else {
                                 userEJB.loginSucceeded(userData.username, DataUtils.truncateMobileDevice(sessionInfo.mobileDevice), DataUtils.truncateUserAgent(sessionInfo.userAgent), sessionInfo.language);
                              }

                              if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.VAS_LOG_ENABLED) && sessionInfo.connection != null && !StringUtil.isBlank(sessionInfo.connection.getVasID())) {
                                 vasLog.info(sessionInfo.sessionID + "\t" + sessionInfo.connection.getVasID());
                              }

                              if (sessionInfo.view.isMigboView() || sessionInfo.view == SSOEnums.View.UNKNOWN) {
                                 long sessionStartTime = System.currentTimeMillis() / 1000L;
                                 String jsonStrPost = String.format("{'timestamp':%d,'records':{'username':'%s','clientType':%d,'clientVersion':%d}}", sessionStartTime, username, sessionInfo.getClientType(), sessionInfo.getClientVersion());
                                 logSSOClientSession(sessionInfo.sessionID, sessionInfo.view, username, sessionStartTime, jsonStrPost);
                              }

                              SSOLogin.SSOLoginResult var59 = SSOLogin.SSOLoginResult.OK;
                              return var59;
                           } else {
                              throw new LoginException("Unable to create session. Please try again later.");
                           }
                        } else {
                           log.error(String.format("Unable to determine user's country or IDD code for user %s, countryID %d", username, userData.countryID));
                           throw new LoginException("Internal server error, please try again");
                        }
                     }
                  }
               }
            }
         } catch (CreateException var39) {
            log.error("Unable to create bean", var39);
            context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var39);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (RemoteException var40) {
            log.error("Remote exception: " + var40.getClass().getName(), var40);
            context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var40);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (ObjectNotFoundException var41) {
            log.error("User object not found in Object cache: ", var41);
            context.errorOccurred((String)"Login failed - Please try again", (Exception)var41);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (ObjectExistsException var42) {
            log.error("User object already exists in object cache: ", var42);
            context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var42);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (FusionException var43) {
            log.error("FusionException : ", var43);
            context.errorOccurred((String)("Login failed - " + var43.message), (Exception)var43);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (IceLocalExceptionAndContext var44) {
            log.error("Unable to login due to Ice exception " + var44.getRootException().ice_name() + ", proxy=" + var44.getProxy() + ", " + var44.getMessage(), var44);
            context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var44);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (LocalException var45) {
            log.error("Unable to login due to Ice exception " + var45.ice_name() + ", " + var45.getMessage(), var45);
            context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var45);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (LoginException var46) {
            log.warn("Login failed for " + getUserInfoForLogging(username, sessionInfo) + ", Exception: " + var46.getMessage(), var46);
            context.errorOccurred((String)("Login failed - " + var46.getMessage()), (Exception)var46);
            return SSOLogin.SSOLoginResult.ERROR;
         } catch (Exception var47) {
            if (var47.getMessage() == null) {
               log.error("Unexpected exception without message: " + getUserInfoForLogging(username, sessionInfo) + var47.getClass().getName(), var47);
            } else {
               log.error("Login failed for " + getUserInfoForLogging(username, sessionInfo), var47);
            }

            context.errorOccurred("Login failed - Internal server error, please try again", var47);
            return SSOLogin.SSOLoginResult.ERROR;
         } finally {
            if (loginLockAcquired) {
               releaseLoginSerializationLock(username);
            }

         }
      }
   }

   private static String getFailedLoginAttemptKey(String username, String remoteAddress) {
      return username + "/" + remoteAddress;
   }

   private static String getUserInfoForLogging(String username, SSOLoginSessionInfo sessionInfo) {
      return sessionInfo == null ? String.format("user:[%s]", username) : String.format("user:[%s], sessionId [%s], useragent [%s]", username, sessionInfo.sessionID, sessionInfo.userAgent);
   }

   private static boolean authenticateUser(SSOLoginClientContext context, String username, String loginChallenge, Integer passwordHash, SSOLoginSessionInfo sessionInfo) throws LoginException, FusionException, EJBException {
      String exFLAKey = getFailedLoginAttemptKey(username, sessionInfo.remoteAddress);
      if (passwordHash == null) {
         MemCachedClientWrapper.addOrIncr(MemCachedKeySpaces.RateLimitKeySpace.FAILED_LOGIN_ATTEMPTS, exFLAKey);
         context.errorOccurred("Username and password do not match");
         return false;
      } else {
         AuthenticationServiceResponseCodeEnum responseCode = context.findAuthenticationServiceProxy(sessionInfo).authenticate(new FusionCredential(0, username, (String)null, PasswordType.FUSION.value(), passwordHash, loginChallenge), sessionInfo.remoteAddress);
         if (log.isDebugEnabled()) {
            log.debug("Response code from auth service for user [" + username + "] is " + responseCode);
         }

         if (responseCode != AuthenticationServiceResponseCodeEnum.Success) {
            if (responseCode != AuthenticationServiceResponseCodeEnum.UnknownUsername) {
               MemCachedClientWrapper.addOrIncr(MemCachedKeySpaces.RateLimitKeySpace.FAILED_LOGIN_ATTEMPTS, exFLAKey);
            } else {
               try {
                  User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                  if (userEJB.isEmailRegistrationNotVerifiedForUsername(username)) {
                     context.errorOccurred("Please activate your account first or go to m.mig.me/resend to resend token");
                     return false;
                  }
               } catch (CreateException var8) {
                  log.error("Unable to create bean", var8);
                  context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var8);
               } catch (RemoteException var9) {
                  log.error("Unable to create bean", var9);
                  context.errorOccurred((String)"Login failed - Internal server error, please try again", (Exception)var9);
               }
            }

            context.errorOccurred(FusionPktError.Code.INCORRECT_CREDENTIAL, "Username and password do not match");
            return false;
         } else if (MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.LOGIN_BAN, username) != null) {
            context.errorOccurred("Your account is suspended");
            return false;
         } else if (MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.TEMP_LOGIN_SUSPENSION, username) != null) {
            context.errorOccurred(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.TEMP_LOGIN_SUSPENSION_ERROR_MESSAGE));
            return false;
         } else {
            return true;
         }
      }
   }

   private static long to_int32(long i) {
      return i >= -2147483648L && i <= 2147483647L ? i : (i + 2147483648L) % 4294967296L - 2147483648L;
   }

   public static int calculatePasswordHash(String password, String challenge) {
      long hash = 0L;
      char[] arr$ = (challenge + password).toCharArray();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         char c = arr$[i$];
         hash = to_int32(hash * 31L) + (long)c;
      }

      return (int)hash;
   }

   private static boolean createSSOSession(SSOLoginSessionInfo sessionInfo, Integer passwordHash, UserData userData) throws LoginException {
      if (sessionInfo.sessionID == null) {
         throw new LoginException("Username and password do not match");
      } else {
         SSOLoginData loginData = new SSOLoginData();
         loginData.username = userData.username;
         loginData.userID = userData.userID;
         loginData.presence = sessionInfo.initialPresence;
         loginData.clientVersion = sessionInfo.getClientVersion();
         loginData.deviceType = Integer.valueOf(sessionInfo.getClientType());
         loginData.userAgent = sessionInfo.userAgent;
         loginData.mobileDevice = sessionInfo.mobileDevice;
         loginData.voiceCapability = ContactData.VoiceCapabilityEnum.NOT_AVAILABLE.value();
         loginData.passwordHash = passwordHash;
         loginData.sessionStartTimeInMillis = new Long(System.currentTimeMillis());
         return setLoginDataInMemcache(sessionInfo.sessionID, loginData);
      }
   }

   static {
      DEFAULT_FUSION_SESSION_ALLOWED_DEVICE_TYPES = new int[]{ClientType.AJAX1.value(), ClientType.AJAX2.value()};
      DEFAULT_SSO_SESSION_ALLOWED_VIEWS = new int[]{SSOEnums.View.MIGBO_WEB.value(), SSOEnums.View.MIGBO_WAP.value()};
   }

   public static enum SSOLoginResult {
      OK,
      ERROR,
      SESSION_ACTIVE,
      REQUIRE_CAPTCHA;
   }
}
