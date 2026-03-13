package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLoginData;
import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedDistributedLock;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataSlimLogin;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;

public class FusionPktSlimLogin extends FusionPktDataSlimLogin {
   private static final String INCORRECT_CREDENTIAL = "Username and password do not match";
   private static final long MILLIS_IN_DAY = 86400000L;
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktSlimLogin.class));
   private static final long pow2_31 = 2147483648L;
   private static final long pow2_32 = 4294967296L;

   public FusionPktSlimLogin() {
   }

   public FusionPktSlimLogin(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktSlimLogin(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      if (!SSOLogin.isSSOLoginEnabled()) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login is disabled.")).toArray();
      } else {
         try {
            if (connection.getSessionID() != null && SSOLogin.sessionIsActive(connection.getSessionID())) {
               throw new Exception("You currently have an active session.");
            }
         } catch (Exception var29) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - " + var29.getMessage())).toArray();
         }

         FusionPktError pktError = null;
         UserData userData = null;
         FusionPktLogin loginPacket = null;
         boolean loginLockAcquired = false;
         String loginLockId = "";

         label260: {
            FusionPacket[] var16;
            try {
               String username = this.getUsername();
               if (username == null || username.length() == 0) {
                  throw new Exception("Username and password do not match");
               }

               FusionPktLogin login = this.createDummyLoginPacket();
               connection = connection.onLogin(login);
               Integer passwordHash = this.calculatePasswordHash(this.getPassword(), connection.getLoginChallenge());
               this.setPasswordHash(passwordHash);
               String exFLAKey = username + "/" + connection.getRemoteAddress();
               if (!this.skipCaptchaCheck()) {
                  long failedLoginAttempts = MemCachedClientWrapper.getCounter(MemCachedKeySpaces.RateLimitKeySpace.FAILED_LOGIN_ATTEMPTS, exFLAKey);
                  if (failedLoginAttempts > (long)connection.getGateway().getMaxFailedLoginAttempts()) {
                     ArrayList<FusionPacket> responsePackets = new ArrayList();
                     FusionPktLoginChallenge loginChallenge = new FusionPktLoginChallenge(this.transactionId);
                     loginChallenge.setSessionId(connection.getSessionID());
                     responsePackets.add(loginChallenge);
                     Captcha captcha = connection.createCaptcha(this, 4);
                     FusionPktCaptcha captchaPkt = new FusionPktCaptcha(this.transactionId, "Please enter the letters as shown in the image below.", captcha, connection);
                     responsePackets.add(captchaPkt);
                     FusionPacket[] var17 = (FusionPacket[])responsePackets.toArray(new FusionPacket[responsePackets.size()]);
                     return var17;
                  }
               }

               loginLockId = MemCachedKeyUtils.getFullKeyFromStrings("LS", this.getUsername());
               loginLockAcquired = MemCachedDistributedLock.getDistributedLock(loginLockId, 0L);
               if (!loginLockAcquired) {
                  log.warn(String.format("Disallowed login from user '%s' due to concurrent login sessions.", this.getUsername()));
                  throw new Exception("Unable to login now. Please try again later.");
               }

               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               userData = userEJB.loadUser(username, false, false);
               if (userData == null) {
                  throw new Exception("Username and password do not match");
               }

               if (userData.status != UserData.StatusEnum.ACTIVE) {
                  throw new Exception("Account is not active");
               }

               if (!userEJB.userAttemptedVerification(username, SystemProperty.getInt((String)"AllowedVerificationWindowInHours", 96)) && !userData.mobileVerified && SystemProperty.getBool("UnauthUserLoginBlockEnabled", false)) {
                  Date today = new Date();
                  Date dateRegistered = userData.dateRegistered;
                  long dateDiffInMsec = today.getTime() - dateRegistered.getTime();
                  if (dateDiffInMsec / 86400000L > (long)SystemProperty.getInt((String)"UnauthUserAllowLoginInDays", 90)) {
                     String message = SystemProperty.get("UnauthUserLoginBlockMessage", "Authenticate yr account at http://m.mig.me/auth by stating yr USER ID, MOBILE NO & COUNTRY");
                     throw new Exception(message);
                  }
               }

               MemCachedClientWrapper.delete(MemCachedKeySpaces.RateLimitKeySpace.FAILED_LOGIN_ATTEMPTS, exFLAKey);
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               CountryData countryData = misEJB.getCountry(userData.countryID);
               if (countryData == null || countryData.iddCode == null) {
                  throw new Exception("Unable to determine user's country and IDD code");
               }

               CurrencyData currencyData = misEJB.getCurrency(userData.currency);
               if (currencyData == null) {
                  throw new Exception("Invalid currency code " + userData.currency);
               }

               if (!this.createSession(connection, userData)) {
                  throw new Exception("Unable to create session. Please try again later.");
               }

               FusionPktSlimLoginOk loginOk = new FusionPktSlimLoginOk(this.transactionId);
               loginOk.setSessionId(connection.getSessionID());
               userEJB.loginSucceeded(userData.username, DataUtils.truncateMobileDevice(this.getDeviceName()), DataUtils.truncateUserAgent(this.getUserAgent()), userData.language);
               var16 = loginOk.toArray();
            } catch (FusionException var26) {
               pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - " + var26.message);
               break label260;
            } catch (Exception var27) {
               if (var27.getMessage() == null) {
                  log.error("Unexpected exception without message: " + this.getUserInfoForLogging((FusionPktLogin)loginPacket) + var27.getClass().getName(), var27);
                  pktError = new FusionPktInternalServerError(this.transactionId, var27, "Login failed");
               } else {
                  log.error("Login failed for " + this.getUserInfoForLogging((FusionPktLogin)loginPacket), var27);
                  pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - " + var27.getMessage());
               }
               break label260;
            } finally {
               if (loginLockAcquired) {
                  MemCachedDistributedLock.releaseDistributedLock(loginLockId);
               }

            }

            return var16;
         }

         try {
            connection.removeConnectionFromIceAdaptor();
         } catch (Exception var25) {
         }

         return ((FusionPktError)pktError).toArray();
      }
   }

   private String getUserInfoForLogging(FusionPktLogin loginPacket) {
      return loginPacket == null ? "" : "user:[" + loginPacket.getUsername() + "], sessionId [" + loginPacket.getSessionId() + "], useragent [" + loginPacket.getUserAgent() + "] ";
   }

   private boolean createSession(ConnectionI connection, UserData userData) throws Exception {
      String sessionID = connection.getSessionID();
      if (sessionID == null) {
         throw new Exception("Username and password do not match");
      } else {
         Integer presence = 1;
         if (this.getInitialPresence() != null) {
            presence = Integer.valueOf(this.getInitialPresence().value());
         }

         SSOLoginData loginData = new SSOLoginData();
         loginData.username = userData.username;
         loginData.userID = userData.userID;
         loginData.presence = presence;
         loginData.clientVersion = this.getClientVersion();
         loginData.userAgent = this.getUserAgent();
         loginData.mobileDevice = this.getDeviceName();
         loginData.voiceCapability = ContactData.VoiceCapabilityEnum.NOT_AVAILABLE.value();
         loginData.passwordHash = this.calculatePasswordHash(this.getPassword(), connection.getLoginChallenge());
         loginData.sessionStartTimeInMillis = new Long(System.currentTimeMillis());
         return SSOLogin.setLoginDataInMemcache(sessionID, loginData);
      }
   }

   private FusionPktLogin createDummyLoginPacket() {
      FusionPktLogin login = new FusionPktLogin();
      login.setProtocolVersion(this.getProtocolVersion());
      login.setClientVersion(this.getClientVersion());
      login.setServiceType(this.getServiceType());
      login.setUsername(this.getUsername());
      login.setUserAgent(this.getUserAgent());
      login.setDeviceName(this.getDeviceName());
      login.setInitialPresence(this.getInitialPresence());
      login.setStreamUserEvents(false);
      return login;
   }

   private long to_int32(long i) {
      return i >= -2147483648L && i <= 2147483647L ? i : (i + 2147483648L) % 4294967296L - 2147483648L;
   }

   private int calculatePasswordHash(String password, String challenge) {
      long hash = 0L;
      char[] arr$ = (challenge + password).toCharArray();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         char c = arr$[i$];
         hash = this.to_int32(hash * 31L) + (long)c;
      }

      return (int)hash;
   }
}
