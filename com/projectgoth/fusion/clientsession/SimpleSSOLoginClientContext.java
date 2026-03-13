package com.projectgoth.fusion.clientsession;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.common.CaptchaService;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.FusionException;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class SimpleSSOLoginClientContext implements SSOLoginClientContext {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SimpleSSOLoginClientContext.class));
   private String username;
   private String loginChallenge;
   private int passwordHash;
   private String errorMessage;
   private Exception errorCause;
   private String sessionID;
   private String encryptedSessionID;
   private Captcha captcha;
   private static CaptchaService captchaService;

   public SimpleSSOLoginClientContext(String username, String loginChallenge, int passwordHash) {
      this.username = username;
      this.loginChallenge = loginChallenge;
      this.passwordHash = passwordHash;
   }

   public AuthenticationServicePrx findAuthenticationServiceProxy(SSOLoginSessionInfo sessionInfo) {
      return EJBIcePrxFinder.getAuthenticationServiceProxy();
   }

   public void captchaRequired(SSOLoginSessionInfo sessionInfo) {
      this.sessionID = sessionInfo.sessionID;
      SSOEncryptedSessionIDInfo eidInfo = new SSOEncryptedSessionIDInfo(this.sessionID, 0, this.username, sessionInfo.remoteAddress, sessionInfo.userAgent, 0L);
      this.encryptedSessionID = SSOLogin.generateEncryptedSessionID(eidInfo);
      this.captcha = nextCaptcha(this.sessionID, new SSOCaptchaState((String)null, this.username, this.loginChallenge, this.passwordHash));
      log.info(String.format("Setting captcha (id=%s, %s) for session id %s", this.captcha.getId(), this.captcha.getAnswer(), sessionInfo.sessionID));
   }

   public void ssoSessionCreated(SSOLoginSessionInfo sessionInfo, UserData userData) throws RemoteException, CreateException, FusionException, LoginException {
      log.info(String.format("Setting session id %s for user %s", sessionInfo.sessionID, userData.username));
      this.sessionID = sessionInfo.sessionID;
      SSOEncryptedSessionIDInfo eidInfo = new SSOEncryptedSessionIDInfo(this.sessionID, userData.userID, this.username, sessionInfo.remoteAddress, sessionInfo.userAgent);
      this.encryptedSessionID = SSOLogin.generateEncryptedSessionID(eidInfo);
   }

   public void errorOccurred(FusionPktError.Code errorCode, String errorMessage) {
      this.errorOccurred((String)errorMessage, (Exception)null);
   }

   public void errorOccurred(String errorMessage) {
      this.errorOccurred((String)errorMessage, (Exception)null);
   }

   public void errorOccurred(String errorMessage, Exception errorCause) {
      this.errorMessage = errorMessage;
      this.errorCause = errorCause;
   }

   public String getErrorMessage() {
      return this.errorMessage;
   }

   public String getSessionID() {
      return this.sessionID;
   }

   public String getEncryptedSessionID() {
      return this.encryptedSessionID;
   }

   public String getCaptchaImageAsString() {
      return getCaptchaImageAsString(this.captcha);
   }

   private static String getCaptchaImageAsString(Captcha captcha) {
      try {
         return captcha.getBase64EncodedString("png");
      } catch (IOException var2) {
         log.error(String.format("Unable to get captcha image due to IOException"), var2);
         return null;
      }
   }

   public static SSOCaptchaState validateCaptchaResponse(String sessionID, String response) {
      String captchaStateString = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.SSO_LOGIN_CAPTCHA_MAPPING, sessionID);
      if (captchaStateString == null) {
         return null;
      } else {
         SSOCaptchaState captchaState = SSOCaptchaState.fromString(captchaStateString);
         if (captchaState == null) {
            log.error(String.format("Incorrect sso captcha state '%s'", captchaStateString));
            return null;
         } else {
            boolean validated = captchaService.validateResponse(captchaState.captchaId, response);
            if (validated) {
               MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.SSO_LOGIN_CAPTCHA_MAPPING, sessionID);
            } else {
               captchaState.captchaId = null;
            }

            return captchaState;
         }
      }
   }

   private static Captcha nextCaptcha(String sessionID, SSOCaptchaState previousState) {
      Captcha captcha = captchaService.nextCaptcha(4);
      previousState.captchaId = captcha.getId();
      MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.SSO_LOGIN_CAPTCHA_MAPPING, sessionID, previousState.toString());
      return captcha;
   }

   public static String getNextCaptchaImageAsString(String sessionID, SSOCaptchaState previousState) {
      Captcha captcha = nextCaptcha(sessionID, previousState);
      log.info(String.format("Setting new captcha (id=%s, %s) for session id %s", captcha.getId(), captcha.getAnswer(), sessionID));
      return getCaptchaImageAsString(captcha);
   }

   static {
      MemCachedClient captchaMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.captcha);
      if (captchaMemcache == null) {
         log.warn("Failed to initialize memcached for CAPTCHA service. Local cache will be used instead");
      }

      captchaService = new CaptchaService(captchaMemcache, true);
   }
}
