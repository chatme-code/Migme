package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.clientsession.SSOCaptchaState;
import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLoginData;
import com.projectgoth.fusion.clientsession.SSOLoginSessionInfo;
import com.projectgoth.fusion.clientsession.SSOLogoutSessionInfo;
import com.projectgoth.fusion.clientsession.SimpleSSOLoginClientContext;
import com.projectgoth.fusion.clientsession.SimpleSSOLogoutClientContext;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.restapi.data.AuthenticationGetChallengeResponseData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.DisconnectUserSessionData;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.InternalLoginData;
import com.projectgoth.fusion.restapi.data.SSOCheckResponseData;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.data.SSOLoginResponseData;
import com.projectgoth.fusion.slice.AuthenticationServiceCredentialResponse;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.FusionCredential;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

@Provider
@Path("/sso")
public class SSOResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SSOResource.class));

   @GET
   @Path("/logout/{sid}")
   @Produces({"application/json"})
   public DataHolder<String> logout(@PathParam("sid") String sid) throws FusionRestException {
      log.info(String.format("SSO: logout request sid='%s'", sid));
      if (StringUtil.isBlank(sid)) {
         throw new FusionRestException(FusionRestException.RestException.MISSING_SESSION_ID);
      } else {
         if (SSOLogin.isEncryptedSessionID(sid)) {
            sid = SSOLogin.getSessionIDFromEncryptedSessionID(sid);
         }

         SSOLoginData userData = SSOLogin.getLoginDataFromMemcache(sid);
         if (userData == null) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "No session detected.");
         } else {
            SSOLogoutSessionInfo sessionInfo = new SSOLogoutSessionInfo(sid, (ConnectionI)null);
            SimpleSSOLogoutClientContext context = new SimpleSSOLogoutClientContext();
            SSOLogin.doLogout(sessionInfo, context);
            return new DataHolder("ok");
         }
      }
   }

   @GET
   @Path("/logout")
   @Produces({"application/json"})
   public DataHolder<String> logoutWithEID(@QueryParam("eid") String eid) throws FusionRestException {
      log.info(String.format("SSO: logout request eid='%s'", eid));
      return this.logout(eid);
   }

   @POST
   @Path("/login")
   @Produces({"application/json"})
   public DataHolder<SSOLoginResponseData> doSlimLogin(@DefaultValue("") @FormParam("eid") String eid, @DefaultValue("") @FormParam("sid") String sidForBackward, @DefaultValue("") @FormParam("username") String username, @DefaultValue("") @FormParam("password") String password, @DefaultValue("") @FormParam("captcha") String captcha, @DefaultValue("1") @FormParam("presence") byte presence, @DefaultValue("") @FormParam("useragent") String userAgent, @DefaultValue("") @FormParam("view") String viewStr, @DefaultValue("") @FormParam("clientVersion") String clientVersionStr, @DefaultValue("") @FormParam("ip") String remoteIp, @DefaultValue("") @FormParam("passwordType") String passwordTypeStr) throws FusionRestException {
      if (!StringUtil.isBlank(captcha) || !StringUtil.isBlank(username) && !StringUtil.isBlank(password)) {
         int clientVersion = StringUtil.toIntOrDefault(clientVersionStr, 0);
         log.info(String.format("SSO: Login request for ['%s'] with presence ['%s'] and view ['%s']", username, presence, viewStr));
         SSOEnums.View view = SSOEnums.View.fromString(viewStr.toUpperCase());
         if (StringUtil.isBlank(viewStr)) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Please provide a view.");
         } else if (!SSOLogin.isSSOLoginEnabledView(view, clientVersion)) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unauthorized access.");
         } else {
            byte passwordType = StringUtil.toByteOrDefault(passwordTypeStr, PasswordType.FUSION.value());
            if (passwordType != PasswordType.FUSION.value()) {
               if (passwordType != PasswordType.FACEBOOK_IM.value()) {
                  throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid password type");
               }

               Credential fusionCredential = this.getFusionCredentialsFromFacebookCredentials(username, passwordTypeStr);
               username = fusionCredential.username;
               password = fusionCredential.password;
            }

            String sid = null;
            if (!StringUtil.isBlank(eid)) {
               sid = SSOLogin.getSessionIDFromEncryptedSessionID(eid);
            }

            boolean skipCaptchaCheck = false;
            String loginChallenge = null;
            int passwordHash = -1;
            if (!StringUtil.isBlank(captcha)) {
               if (StringUtil.isBlank(sid)) {
                  throw new FusionRestException(FusionRestException.RestException.ERROR, "Session Id is missing");
               }

               SSOCaptchaState captchaState = SimpleSSOLoginClientContext.validateCaptchaResponse(sid, captcha);
               if (captchaState == null) {
                  throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid captcha");
               }

               if (captchaState.captchaId == null) {
                  SSOLoginResponseData data = new SSOLoginResponseData();
                  data.sessionId = eid;
                  data.captcha = SimpleSSOLoginClientContext.getNextCaptchaImageAsString(sid, captchaState);
                  data.message = "The letters you entered were incorrect. Please try again.";
                  return new DataHolder(data);
               }

               skipCaptchaCheck = true;
               log.debug(String.format("captcha is validated, reusing old username '%s' challenge '%s' pwdhash %d", captchaState.username, captchaState.loginChallenge, captchaState.passwordHash));
               username = captchaState.username;
               loginChallenge = captchaState.loginChallenge;
               passwordHash = captchaState.passwordHash;
            }

            ClientType clientTypeEnum = view.toFusionDeviceEnum();
            SSOLoginSessionInfo sessionInfo = new SSOLoginSessionInfo(view, clientTypeEnum == null ? null : clientTypeEnum.value(), (short)clientVersion, sid, Integer.valueOf(presence), remoteIp, "1", userAgent, (String)null, (ConnectionI)null);
            if (loginChallenge == null) {
               loginChallenge = ConnectionI.newChallengeString(username);
               passwordHash = SSOLogin.calculatePasswordHash(password.toLowerCase(), loginChallenge);
            }

            SimpleSSOLoginClientContext context = new SimpleSSOLoginClientContext(username, loginChallenge, passwordHash);
            SSOLoginResponseData loginResult = this.doLogin(username, loginChallenge, passwordHash, sessionInfo, skipCaptchaCheck, context, false);
            if ((Boolean)SystemPropertyEntities.Temp.Cache.se501ClearMemcachedOnSsoDisconnectWapEnabled.getValue()) {
               this.addSessionToRecentWebOnlySidsKey(username, SSOLogin.getSessionIDFromEncryptedSessionID(loginResult.sessionId));
            }

            return new DataHolder(loginResult);
         }
      } else {
         throw new FusionRestException(FusionRestException.RestException.USER_CREDENTIALS_REQUIRED);
      }
   }

   private void addSessionToRecentWebOnlySidsKey(String username, String sid) throws FusionRestException {
      Jedis masterInstance = null;

      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int userId = userEJB.getUserID(username, (Connection)null);
         long nowInSeconds = System.currentTimeMillis() / 1000L;
         String key = Redis.getRecentWebOnlySidsKey(userId);
         long elementCountForKey = 0L;
         Jedis slaveInstanceForUserID = null;

         try {
            slaveInstanceForUserID = Redis.getSlaveInstanceForUserID(userId);
            if (slaveInstanceForUserID != null) {
               elementCountForKey = slaveInstanceForUserID.zcount(key, 0.0D, (double)nowInSeconds);
            }
         } finally {
            if (slaveInstanceForUserID != null) {
               Redis.disconnect(slaveInstanceForUserID, log);
            }

         }

         masterInstance = Redis.getMasterInstanceForUserID(userId);
         long maxSessionIdCountForUser = (Long)SystemPropertyEntities.SSO.Cache.MAX_SESSIONID_COUNT_FOR_USER.getValue();
         if (elementCountForKey > maxSessionIdCountForUser) {
            masterInstance.zremrangeByScore(key, 0.0D, (double)(elementCountForKey - (maxSessionIdCountForUser + 1L)));
         }

         Pipeline pipelined = masterInstance.pipelined();
         if (log.isDebugEnabled()) {
            log.debug(String.format("adding sid to reids with key[%s] member[%s]", key, sid));
         }

         pipelined.zadd(key, (double)nowInSeconds, sid);
         pipelined.expire(key, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.SESSION_KEY_CONTAINER_EXPIRY_TIME_SECS));
         pipelined.sync();
      } catch (Exception var24) {
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, var24.getMessage());
      } finally {
         Redis.disconnect(masterInstance, log);
      }

   }

   private Credential getFusionCredentialsFromFacebookCredentials(String username, String password) throws FusionRestException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.FacebookConnect.ENABLED)) {
         AuthenticationServicePrx asp = EJBIcePrxFinder.getAuthenticationServiceProxy();
         if (asp == null) {
            log.error("Unable to find authentication service proxy for FB Connect SSO login");
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } else {
            AuthenticationServiceCredentialResponse resp = asp.getLatestCredentialByUsernameAndPasswordType(username, PasswordType.FACEBOOK_IM.value());
            if (resp.code != AuthenticationServiceResponseCodeEnum.Success) {
               if (resp.code == AuthenticationServiceResponseCodeEnum.UnknownCredential) {
                  throw new FusionRestException(FusionRestException.RestException.USER_CREDENTIALS_REQUIRED, "Facebook account is not currently linked to any migme account");
               } else {
                  log.error("Unable to check if credentials exist for FB Connect");
                  throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
               }
            } else {
               if (!password.equals(resp.userCredential.password)) {
                  log.info("User [" + resp.userCredential.userID + "] access tokens are different. Stored Access Token [" + resp.userCredential.password + "] != Input Access Token [" + password + "]");
                  resp.userCredential.password = password;

                  try {
                     AuthenticationServiceResponseCodeEnum updateResp = asp.updateCredential(resp.userCredential);
                     if (updateResp != AuthenticationServiceResponseCodeEnum.Success) {
                        log.warn("Unable to update facebook credentials for user ID [" + resp.userCredential.userID + "]");
                     }
                  } catch (FusionException var7) {
                     log.warn("Unable to update facebook credentials for user ID [" + resp.userCredential.userID + "]: " + var7);
                  }
               }

               try {
                  AuthenticationServiceCredentialResponse fusionCredentialResp = asp.getCredential(resp.userCredential.userID, PasswordType.FUSION.value());
                  if (fusionCredentialResp.code != AuthenticationServiceResponseCodeEnum.Success) {
                     log.error("Unable to find user credentials for user ID [" + resp.userCredential.userID + "]");
                     throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                  } else {
                     return fusionCredentialResp.userCredential;
                  }
               } catch (FusionException var6) {
                  log.error("Unable to find user credentials for user ID [" + resp.userCredential.userID + "]: " + var6);
                  throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
               }
            }
         }
      } else {
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      }
   }

   @POST
   @Path("/check/{view}/{sid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<SSOCheckResponseData> doCheckSessionWithMetrics(@PathParam("sid") String sid, @PathParam("view") String viewStr, @DefaultValue("0") @QueryParam("usertype") String requireUserTypeStr, String jsonStrPost) throws FusionRestException {
      log.debug(String.format("SSO DEBUG: sid[%s] view[%s] jsonBody[%s]", sid, viewStr, jsonStrPost));
      if (StringUtil.isBlank(viewStr)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "The parameter 'view' is missing.");
      } else {
         SSOEnums.View view = SSOEnums.View.fromString(viewStr.toUpperCase());
         if (StringUtil.isBlank(jsonStrPost)) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Missing post body");
         } else if (StringUtil.isBlank(sid)) {
            throw new FusionRestException(FusionRestException.RestException.MISSING_SESSION_ID);
         } else {
            if (SSOLogin.isEncryptedSessionID(sid)) {
               sid = SSOLogin.getSessionIDFromEncryptedSessionID(sid);
               if (sid == null) {
                  throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid session id");
               }
            }

            SSOCheckResponseData response = this.checkSession(sid);
            boolean requireUserType = StringUtil.toBooleanOrDefault(requireUserTypeStr, false);
            if (requireUserType && response.type == null) {
               try {
                  UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  UserData userData = userEJB.loadUserFromID(response.userid);
                  if (userData.type != null) {
                     response.type = userData.type.value();
                  }
               } catch (CreateException var11) {
                  log.error(String.format("unable to create userbean ejb object to get user type for sso check - %s , user id %d, returning null", sid, response.userid), var11);
               } catch (EJBException var12) {
                  log.error(String.format("unable to get user type for sso check - %s , user id %d, returning null", sid, response.userid), var12);
               }
            }

            try {
               SSOLogin.logSSOClientSession(sid, view, response.username, response.sessionStartTimeInSeconds, jsonStrPost);
            } catch (Exception var10) {
               log.error("Session Archival Error: " + var10.getMessage());
            }

            return new DataHolder(response);
         }
      }
   }

   @POST
   @Path("/check/{view}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<SSOCheckResponseData> doCheckSessionWithMetricsWithEID(@QueryParam("eid") String eid, @PathParam("view") String viewStr, @DefaultValue("0") @QueryParam("usertype") String requireUserTypeStr, String jsonStrPost) throws FusionRestException {
      return this.doCheckSessionWithMetrics(eid, viewStr, requireUserTypeStr, jsonStrPost);
   }

   @GET
   @Path("/check/{view}/{sid}")
   @Produces({"application/json"})
   public DataHolder<SSOCheckResponseData> doCheckSessionBackward(@PathParam("sid") String sid, @PathParam("view") String viewStr, @DefaultValue("0") @QueryParam("usertype") String requireUserTypeStr) throws FusionRestException {
      return this.doCheckSessionWithMetrics(sid, viewStr, requireUserTypeStr, String.format("{'timestamp':%d}", System.currentTimeMillis() / 1000L));
   }

   @GET
   @Path("/check/{view}")
   @Produces({"application/json"})
   public DataHolder<SSOCheckResponseData> doCheckSession(@QueryParam("eid") String eid, @PathParam("view") String viewStr, @DefaultValue("0") @QueryParam("usertype") String requireUserTypeStr) throws FusionRestException {
      return this.doCheckSessionWithMetrics(eid, viewStr, requireUserTypeStr, String.format("{'timestamp':%d}", System.currentTimeMillis() / 1000L));
   }

   @POST
   @Path("/login/full/{sid}")
   @Produces({"application/json"})
   public DataHolder<String> doFusionLogin(@PathParam("sid") String sid, @DefaultValue("0") @FormParam("devicetype") int deviceType) throws FusionRestException {
      log.info(String.format("SSO: Login request for ['%s'] with presence ['%s']", sid, deviceType));
      throw new FusionRestException(FusionRestException.RestException.ERROR, "full login from fusion-rest is no longer supported");
   }

   private SSOCheckResponseData checkSession(String sid) throws FusionRestException {
      SSOLoginData userData = SSOLogin.getLoginDataFromMemcache(sid);
      SSOCheckResponseData respData = null;
      if (userData != null) {
         try {
            SSOLogin.setLoginDataInMemcache(sid, userData);
            respData = userData.toSSOCheckResponseData();
         } catch (Exception var9) {
            log.error("Unable to extend session for [" + userData.username + "] [" + userData + "] " + var9.getMessage());
         }
      } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.CREATE_SSO_SESSION_FROM_FUSION)) {
         try {
            ConnectionPrx connectionPrx = EJBIcePrxFinder.findConnectionPrx(sid);
            if (connectionPrx != null) {
               UserPrx userPrx = connectionPrx.getUserObject();
               if (userPrx != null) {
                  log.info(String.format("Re-creating sso session from fusion session sid '%s'", sid));

                  try {
                     userData = new SSOLoginData();
                     UserDataIce userDataIce = userPrx.getUserData();
                     userData.username = userDataIce.username;
                     userData.userID = userDataIce.userID;
                     userData.presence = userPrx.getOverallFusionPresence((String)null);
                     userData.clientVersion = connectionPrx.getClientVersion();
                     userData.deviceType = connectionPrx.getDeviceTypeAsInt();
                     userData.userAgent = connectionPrx.getUserAgent();
                     userData.mobileDevice = connectionPrx.getMobileDevice();
                     userData.voiceCapability = ContactData.VoiceCapabilityEnum.NOT_AVAILABLE.value();
                     userData.passwordHash = null;
                     userData.sessionStartTimeInMillis = new Long(System.currentTimeMillis());
                     SSOLogin.setLoginDataInMemcache(sid, userData);
                     respData = userData.toSSOCheckResponseData();
                     respData.type = userDataIce.type;
                  } catch (Exception var7) {
                     log.error(String.format("Exception occurred while reconstructing sso session from fusion session sid '%s'", sid), var7);
                     userData = null;
                  }
               }
            }
         } catch (EJBException var8) {
         }
      }

      if (userData == null) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid session id '%s'", sid));
      } else {
         return respData;
      }
   }

   private String getFailedLoginAttemptKey(String username, String remoteAddress) {
      return username + "/" + remoteAddress;
   }

   @GET
   @Path("/authentication/{username}/getchallenge")
   @Produces({"application/json"})
   public DataHolder<AuthenticationGetChallengeResponseData> getLoginChallenge(@PathParam("username") String username, @QueryParam("ip") String remoteIp) throws FusionRestException {
      if (StringUtil.isBlank(username)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid username '%s'", username));
      } else if (StringUtil.isBlank(remoteIp)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("IP address is not specified"));
      } else {
         String exFLAKey = this.getFailedLoginAttemptKey(username, remoteIp);
         long failedLoginAttempts = MemCachedClientWrapper.getCounter(MemCachedKeySpaces.RateLimitKeySpace.FAILED_AUTHENTICATION_ATTEMPTS, exFLAKey);
         if (failedLoginAttempts >= (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.MAX_FAILED_AUTHENTICATION_ATTEMPTS)) {
            throw new FusionRestException(FusionRestException.RestException.MAX_FAILED_AUTHENTICATION_REACHED);
         } else {
            AuthenticationGetChallengeResponseData respData = new AuthenticationGetChallengeResponseData();
            respData.username = username;
            respData.challenge = ConnectionI.newChallengeString(username);
            return new DataHolder(respData);
         }
      }
   }

   @GET
   @Path("/authentication/{username}/authenticate")
   @Produces({"application/json"})
   public DataHolder<String> authenticate(@PathParam("username") String username, @QueryParam("challenge") String challenge, @QueryParam("passwordHash") String passwordHashStr, @QueryParam("ip") String remoteIp) throws FusionRestException {
      if (StringUtil.isBlank(username)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid username '%s'", username));
      } else if (StringUtil.isBlank(challenge)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid challenge '%s'", challenge));
      } else {
         int passwordHash = StringUtil.toIntOrDefault(passwordHashStr, Integer.MAX_VALUE);
         if (passwordHash == Integer.MAX_VALUE) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid password hash '%s'", passwordHashStr));
         } else if (StringUtil.isBlank(remoteIp)) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("IP address is not specified"));
         } else {
            String exFLAKey = this.getFailedLoginAttemptKey(username, remoteIp);
            long failedLoginAttempts = MemCachedClientWrapper.getCounter(MemCachedKeySpaces.RateLimitKeySpace.FAILED_AUTHENTICATION_ATTEMPTS, exFLAKey);
            if (failedLoginAttempts >= (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.MAX_FAILED_AUTHENTICATION_ATTEMPTS)) {
               throw new FusionRestException(FusionRestException.RestException.MAX_FAILED_AUTHENTICATION_REACHED);
            } else {
               AuthenticationServicePrx authSvcPrx = EJBIcePrxFinder.getAuthenticationServiceProxy();
               if (authSvcPrx == null) {
                  log.error(String.format("Unable to find authentication service proxy to do authentication for user %s, challenge %s, password hash %s, ip %s", username, challenge, passwordHash, remoteIp));
                  throw new FusionRestException(FusionRestException.RestException.ERROR);
               } else {
                  try {
                     AuthenticationServiceResponseCodeEnum responseCode = authSvcPrx.authenticate(new FusionCredential(0, username, (String)null, PasswordType.FUSION.value(), passwordHash, challenge), remoteIp);
                     if (log.isDebugEnabled()) {
                        log.debug("Response code from auth service for user [" + username + "] is " + responseCode);
                     }

                     if (responseCode != AuthenticationServiceResponseCodeEnum.Success) {
                        if (responseCode != AuthenticationServiceResponseCodeEnum.UnknownUsername) {
                           MemCachedClientWrapper.addOrIncr(MemCachedKeySpaces.RateLimitKeySpace.FAILED_AUTHENTICATION_ATTEMPTS, exFLAKey, (long)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.AUTHENTICATION_FAILED_ATTEMPT_EXIPRY_IN_SECONDS) * 1000L);
                        }

                        throw new FusionRestException(FusionRestException.RestException.INCORRECT_AUTHENTICATION_DETAIL);
                     } else {
                        MemCachedClientWrapper.delete(MemCachedKeySpaces.RateLimitKeySpace.FAILED_AUTHENTICATION_ATTEMPTS, exFLAKey);
                        return new DataHolder("ok");
                     }
                  } catch (FusionException var11) {
                     log.error(String.format("Error in authenticating user %s, challenge %s, password hash %s, ip %s - %s", username, challenge, passwordHash, remoteIp, var11.getMessage()));
                     throw new FusionRestException(FusionRestException.RestException.ERROR);
                  }
               }
            }
         }
      }
   }

   @POST
   @Path("/login/{username}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<String> internalLogin(@PathParam("username") String usernameStr, DataHolder<InternalLoginData> internalLoginData) throws FusionRestException {
      String sessionID = null;
      InternalLoginData loginData = (InternalLoginData)internalLoginData.data;
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.AUTO_LOGIN_ENABLED)) {
         throw new FusionRestException(FusionRestException.RestException.DISABLED_FEATURE);
      } else {
         String sessID;
         if (loginData.reuseExistingSession()) {
            if (StringUtil.isBlank(loginData.sid)) {
               throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Please provide the session id.");
            }

            try {
               sessID = SSOLogin.getSessionIDFromEncryptedSessionID(loginData.sid);
               if (!SSOLogin.sessionIsActive(sessID)) {
                  throw new FusionRestException(FusionRestException.RestException.UNABLE_TO_CREATE_SESSION);
               }
            } catch (Exception var11) {
               throw new FusionRestException(FusionRestException.RestException.UNABLE_TO_CREATE_SESSION);
            }

            sessionID = loginData.sid;
         } else {
            sessID = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.AUTO_LOGIN_RATE_LIMIT);

            try {
               log.info("Auto login request from [" + usernameStr + "]");
               MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.AUTO_LOGIN, usernameStr, sessID);
            } catch (MemCachedRateLimiter.LimitExceeded var9) {
               log.error("Rate limit breached for auto login [" + usernameStr + "] with rate limit [" + sessID + "]");
               throw new FusionRestException(FusionRestException.RestException.AUTO_LOGIN_RATE_LIMIT);
            } catch (MemCachedRateLimiter.FormatError var10) {
               log.error("Unable to automatically login user [" + usernameStr + "] due to invalid rate limit format", var10);
               throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Unable to process your request at the moment, please try again later.");
            }

            SSOLoginSessionInfo sessionInfo = new SSOLoginSessionInfo(SSOEnums.View.fromString(loginData.view), loginData.deviceType, loginData.clientVersion, (String)null, loginData.presence, loginData.remoteIpAddress, "1", loginData.userAgent, (String)null, (ConnectionI)null);
            SimpleSSOLoginClientContext loginContext = new SimpleSSOLoginClientContext(usernameStr, (String)null, 0);
            SSOLoginResponseData loginResult = this.doLogin(usernameStr, (String)null, (Integer)null, sessionInfo, true, loginContext, true);
            if (!StringUtil.isBlank(loginResult.captcha)) {
               throw new FusionRestException(FusionRestException.RestException.UNABLE_TO_CREATE_SESSION);
            }

            sessionID = loginResult.sessionId;
         }

         return new DataHolder(sessionID);
      }
   }

   private SSOLoginResponseData doLogin(String username, String loginChallenge, Integer passwordHash, SSOLoginSessionInfo sessionInfo, boolean skipCaptchaCheck, SimpleSSOLoginClientContext context, boolean skipAuthenticationCheck) throws FusionRestException {
      SSOLoginResponseData data = new SSOLoginResponseData();
      SSOLogin.SSOLoginResult loginResult = SSOLogin.doLogin(username, loginChallenge, passwordHash, sessionInfo, skipCaptchaCheck, context, skipAuthenticationCheck);
      String errorMessage = context.getErrorMessage();
      if (errorMessage != null) {
         log.error(String.format("Failed to login for user '%s'", username));
         if (errorMessage.equals("Please activate your account first or go to m.mig.me/resend to resend token")) {
            throw new FusionRestException(FusionRestException.RestException.EMAIL_REGISTERED_USER_NOT_VERIFIED);
         } else {
            throw new FusionRestException(FusionRestException.RestException.ERROR, errorMessage);
         }
      } else if (loginResult == SSOLogin.SSOLoginResult.ERROR) {
         log.error(String.format("SSO Login failed, but no error message was set, for %s", username));
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Login failed - Internal server error, please try again");
      } else {
         if (loginResult == SSOLogin.SSOLoginResult.OK) {
            data.sessionId = context.getEncryptedSessionID();
            log.debug(String.format("login successful for user %s with eid=%s", username, data.sessionId));
         } else {
            if (loginResult != SSOLogin.SSOLoginResult.REQUIRE_CAPTCHA) {
               if (loginResult == SSOLogin.SSOLoginResult.SESSION_ACTIVE) {
                  throw new FusionRestException(FusionRestException.RestException.SESSION_ALREADY_EXISTS);
               }

               log.error(String.format("SSO Login failed - unknown result %s, for %s", loginResult.name(), username));
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Login failed - Internal server error, please try again");
            }

            data.sessionId = context.getEncryptedSessionID();
            data.captcha = context.getCaptchaImageAsString();
            data.message = "Please enter the letters as shown in the image below.";
         }

         return data;
      }
   }

   @POST
   @Path("/disconnect")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public DataHolder<Boolean> disconnect(DataHolder<DisconnectUserSessionData> disconnectUserSessionPostData) throws FusionRestException {
      DisconnectUserSessionData disconnectUserSessionData = (DisconnectUserSessionData)disconnectUserSessionPostData.data;
      if (StringUtil.isBlank(disconnectUserSessionData.username)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Please provide the username.");
      } else if (StringUtil.isBlank(disconnectUserSessionData.reason)) {
         throw new FusionRestException(FusionRestException.RestException.MANDATORY_FIELD_MISSING, "Please provide the reason why the user sessions are to be disconnected.");
      } else {
         try {
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userEJB.loadUser(disconnectUserSessionData.username, false, false);
            if (userData == null) {
               throw new FusionRestException(FusionRestException.RestException.UNKNOWN_USER_NAME);
            }

            UserPrx userProxy = EJBIcePrxFinder.findUserPrx(disconnectUserSessionData.username);
            if (userProxy != null) {
               userProxy.disconnect(disconnectUserSessionData.reason);
            }

            if ((Boolean)SystemPropertyEntities.Temp.Cache.se501ClearMemcachedOnSsoDisconnectWapEnabled.getValue()) {
               this.removeWebOnlySessions(userData);
            }
         } catch (CreateException var6) {
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, String.format("Unable to disconnect user session for %s", disconnectUserSessionData.username));
         }

         return new DataHolder(true);
      }
   }

   private void removeWebOnlySessions(UserData userData) throws FusionRestException {
      try {
         int userId = (int)userData.getUserId();
         Jedis masterInstance = null;

         try {
            String key = Redis.getRecentWebOnlySidsKey(userId);
            long nowInSeconds = System.currentTimeMillis() / 1000L;
            Jedis slaveInstance = null;

            try {
               slaveInstance = Redis.getSlaveInstanceForUserID(userId);
               if (slaveInstance != null) {
                  Set<String> sessionIds = slaveInstance.zrange(key, 0L, nowInSeconds);
                  Iterator i$ = sessionIds.iterator();

                  while(i$.hasNext()) {
                     String sid = (String)i$.next();
                     SSOLogin.removeDataFromMemcache(sid);
                  }
               }
            } finally {
               if (slaveInstance != null) {
                  Redis.disconnect(slaveInstance, log);
               }

            }

            masterInstance = Redis.getMasterInstanceForUserID(userId);
            masterInstance.del(key);
         } finally {
            Redis.disconnect(masterInstance, log);
         }

      } catch (Exception var23) {
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, String.format("%s, Unable to data from memcached, for session id %s", var23.getMessage(), ""));
      }
   }
}
