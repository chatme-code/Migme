package com.projectgoth.fusion.gateway.packet;

import Ice.TimeoutException;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.authentication.DecayingFailedAuthsByIPScore;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.clientsession.LoginException;
import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLoginClientContext;
import com.projectgoth.fusion.clientsession.SSOLoginSessionInfo;
import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.ApplicationMenuOptionData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupInvitationData;
import com.projectgoth.fusion.data.MenuData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.exception.IceLocalExceptionAndContext;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.Gateway;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.Voice;
import com.projectgoth.fusion.interfaces.VoiceHome;
import com.projectgoth.fusion.messageswitchboard.MessageSwitchboardDispatcher;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectCachePrx;
import com.projectgoth.fusion.slice.ObjectExistsException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.userevent.domain.ShortTextStatusUserEvent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktLoginResponseOld extends FusionRequest implements SSOLoginClientContext {
   private static final String INCORRECT_CREDENTIAL = "Username and password do not match";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLoginResponseOld.class));
   private FusionPktError pktError = null;
   private List<FusionPacket> packetsToReturn = new ArrayList();

   public FusionPktLoginResponseOld() {
      super((short)202);
   }

   public FusionPktLoginResponseOld(short transactionId) {
      super((short)202, transactionId);
   }

   public FusionPktLoginResponseOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktLoginResponseOld(short type, short transactionId) {
      super(type, transactionId);
   }

   public Integer getPasswordHash() {
      return this.getIntField((short)1);
   }

   public void setPasswordHash(int passwordHash) {
      this.setField((short)1, passwordHash);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      if (connection.getSessionPrx() != null) {
         this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Session already exists");
      } else {
         FusionPktLoginOld loginPacket = connection.getLoginPacketOld();
         if (loginPacket == null) {
            this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
         } else {
            String username = loginPacket.getUsername();
            ClientType clientTypeEnum = ClientType.fromValue(loginPacket.getClientType());
            SSOEnums.View view = SSOEnums.View.fromFusionDeviceEnum(clientTypeEnum);
            SSOLoginSessionInfo sessionInfo = new SSOLoginSessionInfo(view, loginPacket.getClientType(), loginPacket.getClientVersion(), connection.getSessionID(), Integer.valueOf(loginPacket.getInitialPresenceEnum().value()), connection.getRemoteIPAddress(), loginPacket.getMobileDevice(), loginPacket.getUserAgent(), loginPacket.getLanguage(), connection);
            SSOLogin.SSOLoginResult loginResult = SSOLogin.doLogin(username, connection.getLoginChallenge(), this.getPasswordHash(), sessionInfo, this.skipCaptchaCheck(), this, false);
            if (loginResult == SSOLogin.SSOLoginResult.OK) {
               if (this.pktError == null) {
                  return (FusionPacket[])this.packetsToReturn.toArray(new FusionPacket[this.packetsToReturn.size()]);
               }
            } else if (loginResult == SSOLogin.SSOLoginResult.REQUIRE_CAPTCHA) {
               if (this.pktError == null) {
                  return (FusionPacket[])this.packetsToReturn.toArray(new FusionPacket[this.packetsToReturn.size()]);
               }
            } else if (loginResult == SSOLogin.SSOLoginResult.ERROR) {
               if (this.pktError == null) {
                  log.error(String.format("SSO Login failed, but no pktError packet was set, for %s", this.getUserInfoForLogging(loginPacket)));
                  this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
               }

               this.pktError = this.applyResponseDelayIfFailedAuthsExceedThreshold(connection, this.pktError);
            } else if (loginResult == SSOLogin.SSOLoginResult.SESSION_ACTIVE) {
               this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Session already exists");
            } else {
               log.error(String.format("SSO Login failed - unknown result %s, for %s", loginResult.name(), this.getUserInfoForLogging(loginPacket)));
               this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
            }
         }
      }

      connection.removeConnectionFromIceAdaptor();
      return this.pktError.toArray();
   }

   private FusionPktError applyResponseDelayIfFailedAuthsExceedThreshold(ConnectionI connection, FusionPktError pktError) {
      try {
         String ip = connection.getRemoteAddress();
         DecayingFailedAuthsByIPScore score = DecayingFailedAuthsByIPScore.load(ip);
         int threshold = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FAILED_AUTHS_PER_IP_THRESHOLD);
         if (score.getScore() > (double)threshold) {
            String warnStub = "[IP:" + ip + "] [USER:" + connection.getUsername() + "] " + "Failed auths score=" + score.getScore() + " exceed ";
            if (score.getScore() < (double)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.SSO.FAILED_AUTHS_PER_IP_PACKET_DROP_THRESHOLD)) {
               log.warn(warnStub + "lower threshold, delaying returning LOGIN_RESPONSE result to user");
               this.setResponseDelay(score.getLoginResponseResultDelayMillis(threshold));
               return pktError;
            }

            log.warn(warnStub + "upper threshold, returning no LOGIN_RESPONSE result to user");
            return null;
         }
      } catch (Exception var7) {
         log.error("Exception applying failed auth login response delay: e=" + var7, var7);
      }

      return pktError;
   }

   public void ssoSessionCreated(SSOLoginSessionInfo sessionInfo, UserData userData) throws RemoteException, CreateException, FusionException, LoginException {
      if (sessionInfo.connection == null) {
         log.error(String.format("sessionInfo.connection is null, for login of user %s", userData.username));
         throw new LoginException("Login failed - Internal server error, please try again");
      } else {
         ConnectionI connection = sessionInfo.connection;
         FusionPktLoginOld loginPacket = connection.getLoginPacketOld();
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         CountryData countryData = misEJB.getCountry(userData.countryID);
         if (countryData != null && countryData.iddCode != null) {
            CurrencyData currencyData = misEJB.getCurrency(userData.currency);
            if (currencyData == null) {
               throw new LoginException("Invalid currency code " + userData.currency);
            } else {
               createSessionObject(connection, userData, loginPacket);
               this.packetsToReturn = new ArrayList();
               boolean isReconnecting = loginPacket.getSessionId() != null;
               if (isReconnecting) {
                  populatePacketsOnSlimLogin(this.packetsToReturn, connection, misEJB, this.transactionId, userData, countryData, loginPacket, (Integer)null);
                  populateFusionPktMailInfo(this.packetsToReturn, connection, userData, countryData);
               } else {
                  populatePacketsOnLogin(this.packetsToReturn, connection, misEJB, this.transactionId, userData, countryData, currencyData, loginPacket, (Integer)null);
               }

            }
         } else {
            throw new LoginException("Unable to determine user's country and IDD code");
         }
      }
   }

   private String getUserInfoForLogging(FusionPktLoginOld loginPacket) {
      return loginPacket == null ? "" : "user:[" + loginPacket.getUsername() + "], sessionId [" + loginPacket.getSessionId() + "], useragent [" + loginPacket.getUserAgent() + "] ";
   }

   public static SessionPrx createSessionObject(ConnectionI connection, UserData userData, FusionPktLoginOld loginPacket) throws LoginException, ObjectExistsException, ObjectNotFoundException, FusionException {
      String sessionID = connection.getSessionID();
      if (sessionID == null) {
         throw new LoginException("Username and password do not match");
      } else {
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx == null) {
            throw new LoginException("Unable to locate registry");
         } else {
            UserPrx userPrx = null;

            try {
               userPrx = registryPrx.findUserObject(userData.username);
            } catch (ObjectNotFoundException var8) {
               userPrx = null;
            }

            if (userPrx == null) {
               ObjectCachePrx objectCachePrx = registryPrx.getLowestLoadedObjectCache();
               userPrx = objectCachePrx.createUserObject(userData.username);
            }

            connection.onCreatingSession();

            try {
               SessionPrx sessionPrx = userPrx.createSession(sessionID, loginPacket.getInitialPresenceEnum().value(), ClientType.fromValue(loginPacket.getClientType()).value(), connection.getServerType() == Gateway.ServerType.TCP ? Enums.ConnectionEnum.TCP.value() : Enums.ConnectionEnum.HTTP.value(), ImType.FUSION.value(), connection.getPort(), connection.getRemotePort(), connection.getRemoteAddress(), DataUtils.truncateMobileDevice(loginPacket.getMobileDevice(), true, String.format("creating new user session for user '%s'", userData.username)), DataUtils.truncateUserAgent(loginPacket.getUserAgent(), true, String.format("creating new user session for user '%s'", userData.username)), loginPacket.getClientVersion(), connection.getLanguage(), connection.getConnectionPrx());
               connection.onSessionCreatedOld(userPrx, sessionPrx, userData);
               return sessionPrx;
            } catch (TimeoutException var7) {
               throw new IceLocalExceptionAndContext(var7, userPrx);
            }
         }
      }
   }

   private static SessionPrx createSessionObject_beforeSE441(ConnectionI connection, UserData userData, FusionPktLoginOld loginPacket) throws LoginException, ObjectExistsException, ObjectNotFoundException, FusionException {
      String sessionID = connection.getSessionID();
      if (sessionID == null) {
         throw new LoginException("Username and password do not match");
      } else {
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx == null) {
            throw new LoginException("Unable to locate registry");
         } else {
            UserPrx userPrx = null;

            try {
               userPrx = registryPrx.findUserObject(userData.username);
            } catch (ObjectNotFoundException var7) {
               userPrx = null;
            }

            if (userPrx == null) {
               ObjectCachePrx objectCachePrx = registryPrx.getLowestLoadedObjectCache();
               userPrx = objectCachePrx.createUserObject(userData.username);
            }

            connection.onCreatingSession();
            SessionPrx sessionPrx = userPrx.createSession(sessionID, loginPacket.getInitialPresenceEnum().value(), ClientType.fromValue(loginPacket.getClientType()).value(), connection.getServerType() == Gateway.ServerType.TCP ? Enums.ConnectionEnum.TCP.value() : Enums.ConnectionEnum.HTTP.value(), ImType.FUSION.value(), connection.getPort(), connection.getRemotePort(), connection.getRemoteAddress(), DataUtils.truncateMobileDevice(loginPacket.getMobileDevice(), true, String.format("creating new user session for user '%s'", userData.username)), DataUtils.truncateUserAgent(loginPacket.getUserAgent(), true, String.format("creating new user session for user '%s'", userData.username)), loginPacket.getClientVersion(), connection.getLanguage(), connection.getConnectionPrx());
            connection.onSessionCreatedOld(userPrx, sessionPrx, userData);
            return sessionPrx;
         }
      }
   }

   private static void streamInitialUserEvents(ConnectionI connection, UserData userData) {
      try {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED) && connection.findEventSystem() != null) {
            connection.findEventSystem().streamEventsToLoggingInUser(userData.username, connection.getConnectionPrx());
         } else {
            ShortTextStatusUserEvent event = new ShortTextStatusUserEvent();
            event.setGeneratingUsername("migme");
            event.setStatus(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_WELCOME_MESSAGE));
            event.setTimestamp(System.currentTimeMillis());
            UserEventIce eventIce = event.toIceEvent();
            connection.putEvent(eventIce);
         }
      } catch (Exception var4) {
         log.warn("Failed to ask EventSystem to start streaming events for user [" + userData.username + "]", var4);
      }

   }

   private static int getUnreadEmailCount(ConnectionI connection, CountryData countryData, UserData userData) {
      try {
         if (countryData.allowEmail != null && countryData.allowEmail && userData.emailActivated != null && userData.emailActivated) {
            UserPrx userPrx = connection.getUserPrx();
            if (userPrx == null) {
               log.error("UserPrx is null");
               return 0;
            } else {
               int unreadEmailCount = userPrx.getUnreadEmailCount();
               if (unreadEmailCount < 0) {
                  connection.findEmailAlert().requestUnreadEmailCount(userData.username, userData.password, connection.getUserPrx());
               }

               return unreadEmailCount;
            }
         } else {
            return 0;
         }
      } catch (Exception var5) {
         log.warn("Failed to get unread email count for user [" + userData.username + "]", var5);
         return 0;
      }
   }

   private static FusionPktLoginOkOld constructSlimLoginOk(short transactionId, ConnectionI connection, UserData userData, FusionPktLoginOld loginPacket) throws RemoteException, CreateException, FusionException {
      FusionPktLoginOkOld loginOk = new FusionPktLoginOkOld(transactionId);
      UserPrx userPrx = connection.getUserPrx();
      if (userPrx == null) {
         throw new FusionException("You are no longer logged in");
      } else {
         int[] connectedIMs = userPrx.getConnectedOtherIMs();
         if (connectedIMs == null) {
            connectedIMs = new int[0];
         } else {
            Arrays.sort(connectedIMs);
         }

         byte[] availablePasswordTypes = connection.findAuthenticationService().availableCredentialTypes(userData.userID);
         loginOk.setMSNDetail((byte)(Arrays.binarySearch(availablePasswordTypes, PasswordType.MSN_IM.value()) < 0 ? 0 : (Arrays.binarySearch(connectedIMs, ImType.MSN.value()) < 0 ? 1 : 2)));
         loginOk.setYahooDetail((byte)(Arrays.binarySearch(availablePasswordTypes, PasswordType.YAHOO_IM.value()) < 0 ? 0 : (Arrays.binarySearch(connectedIMs, ImType.YAHOO.value()) < 0 ? 1 : 2)));
         loginOk.setAIMDetail((byte)(Arrays.binarySearch(availablePasswordTypes, PasswordType.AIM_IM.value()) < 0 ? 0 : (Arrays.binarySearch(connectedIMs, ImType.AIM.value()) < 0 ? 1 : 2)));
         loginOk.setGTalkDetail((byte)(Arrays.binarySearch(availablePasswordTypes, PasswordType.GTALK_IM.value()) < 0 ? 0 : (Arrays.binarySearch(connectedIMs, ImType.GTALK.value()) < 0 ? 1 : 2)));
         loginOk.setFacebookDetail((byte)(Arrays.binarySearch(availablePasswordTypes, PasswordType.FACEBOOK_IM.value()) < 0 ? 0 : (Arrays.binarySearch(connectedIMs, ImType.FACEBOOK.value()) < 0 ? 1 : 2)));
         loginOk.setContactListVersion(userPrx.getContactListVersion());
         if (loginPacket != null && SSOLogin.isSSOLoginEnabledClient(loginPacket.getClientType(), loginPacket.getClientVersion())) {
            loginOk.setUserID(userData.userID);
         }

         loginOk.setUsername(userData.username);
         byte flag = MessageSwitchboardDispatcher.getInstance().isFeatureEnabled() ? 1 : 0;
         loginOk.setSupportsChatSync((byte)flag);
         byte stickersFlag = ContentUtils.isStickersEnabled() && ContentUtils.userCanSendStickers(userData.userID) ? 1 : 0;
         loginOk.setSupportsStickers((byte)stickersFlag);
         loginOk.setServerTime(System.currentTimeMillis());
         return loginOk;
      }
   }

   public static FusionPktLoginOkOld constructLoginOk(short transactionId, ConnectionI connection, UserData userData, CountryData countryData, CurrencyData currencyData, FusionPktLoginOld loginPacket) throws RemoteException, CreateException, FusionException {
      FusionPktLoginOkOld loginOk = constructSlimLoginOk(transactionId, connection, userData, loginPacket);
      if (userData.mobilePhone != null) {
         loginOk.setMobilePhone(userData.mobilePhone);
      }

      loginOk.setMobileVerified((byte)(AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.RETURN_VERIFIED_UPON_LOGIN, userData) ? 1 : 0));
      loginOk.setUserType((byte)userData.type.value());
      User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      boolean groupInvitationAlertPushed = false;

      String pageletURL;
      try {
         GroupInvitationData groupInvitation = (GroupInvitationData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.GROUP_INVITATION, userData.username);
         if (groupInvitation != null && groupInvitation.groupID != null && connection.getDeviceType() == ClientType.MIDP2) {
            if (groupInvitation.inviter == null) {
               pageletURL = URLUtil.replaceViewTypeToken(SystemProperty.get("GroupJoinURL", ""), connection.getDeviceType());
               if (!StringUtil.isBlank(pageletURL)) {
                  pageletURL = pageletURL + groupInvitation.groupID;
                  loginOk.setAlertContentType(AlertContentType.URL.value());
                  loginOk.setAlert(pageletURL);
                  groupInvitationAlertPushed = true;
               }
            } else {
               pageletURL = SystemProperty.get("GroupInvitationAlert", "");
               if (pageletURL.length() > 0) {
                  GroupData groupData = userEJB.getGroup(groupInvitation.groupID);
                  pageletURL = pageletURL.replaceAll("%inviter%", groupInvitation.inviter).replaceAll("%groupname%", groupData.name);
                  loginOk.setAlertContentType(AlertContentType.TEXT.value());
                  loginOk.setAlert(pageletURL);
                  groupInvitationAlertPushed = true;
               }
            }

            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.GROUP_INVITATION, userData.username);
         }
      } catch (Exception var14) {
         log.warn("Exception occurred when handling a group invitation for " + userData.username, var14);
      }

      if (!groupInvitationAlertPushed) {
         AlertMessageData alertMessage;
         if (connection.getDeviceType() == ClientType.MIDP1) {
            alertMessage = userEJB.getLatestAlertMessage(connection.getClientVersion(), AlertMessageData.TypeEnum.LOGIN, userData.countryID, userData.lastLoginDate, AlertContentType.TEXT, ClientType.MIDP2.value());
         } else {
            alertMessage = userEJB.getLatestAlertMessage(connection.getClientVersion(), AlertMessageData.TypeEnum.LOGIN, userData.countryID, userData.lastLoginDate, (AlertContentType)null, connection.getDeviceType().value());
         }

         if (alertMessage != null) {
            if (alertMessage.contentType == AlertContentType.URL_WITH_CONFIRMATION) {
               if (alertMessage.url != null) {
                  connection.setServerQuestionOld(new FusionPktServerQuestionOld(alertMessage));
               }
            } else {
               loginOk.setAlertContentType(alertMessage.contentType.value());
               loginOk.setAlert(alertMessage.content);
            }
         }
      }

      loginOk.setCurrency(userData.currency);
      if (!CurrencyData.baseCurrency.equalsIgnoreCase(userData.currency)) {
         loginOk.setExchangeRate(currencyData.formatExchangeRate());
      }

      String imageServerURL = SystemProperty.get("ImageServerURL", "");
      if (imageServerURL.length() > 0) {
         loginOk.setImageServerURL(imageServerURL);
      }

      pageletURL = SystemProperty.get("PageletURL", "");
      if (pageletURL.length() > 0) {
         loginOk.setPageletURL(pageletURL);
      }

      Voice voiceEJB = (Voice)EJBHomeCache.getObject("ejb/Voice", VoiceHome.class);
      if (voiceEJB.getDIDNumber(userData.countryID) == null) {
         loginOk.setLocalDIDSupport((byte)0);
      } else {
         loginOk.setLocalDIDSupport((byte)1);
      }

      if (connection.isMidletVersionAndAbove(405)) {
         boolean anonymousCalling = SystemProperty.getBool("AnonymousCalling", false);
         if (anonymousCalling) {
            loginOk.setAnonymousCalling((byte)1);
         }
      }

      if (countryData.allowEmail != null && countryData.allowEmail) {
         String mailURL = SystemProperty.get("MailURL", "");
         if (mailURL.length() > 0) {
            loginOk.setMailURL(mailURL);
            loginOk.setMailCount(0);
         }
      }

      loginOk.setUserEventsToKeep(connection.getGateway().getNumberOfEventsToKeep());
      loginOk.setEmoticonHeight(connection.getOptimalEmoticonHeight());
      loginOk.setVirtualGiftSize(connection.getOptimalVirtualGiftSize());
      if (connection.isMidletVersionAndAbove(420) || connection.isBrowserDevice()) {
         ReputationLevelData levelData = MemCacheOrEJB.getUserReputationLevelData(userData, userEJB);
         loginOk.setReputationLevel((short)levelData.level);
         if (connection.isBrowserDevice()) {
            loginOk.setReputationImagePath(levelData.image);
         } else {
            String reputationImagePath;
            if (connection.isMobileClientV2()) {
               reputationImagePath = SystemProperty.get("ReputationImagePath", "");
               loginOk.setReputationImagePath(reputationImagePath + levelData.image + "-42x42.png");
            } else {
               reputationImagePath = SystemProperty.get("ReputationImagePath", "");
               loginOk.setReputationImagePath(reputationImagePath + levelData.image + "-12x12.png");
            }
         }
      }

      if (SystemProperty.getBool("TCPTunnelling", false)) {
         loginOk.setTCPTunnelling((byte)1);
      }

      return loginOk;
   }

   public void captchaRequired(SSOLoginSessionInfo sessionInfo) {
      if (sessionInfo.connection == null) {
         this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
      } else if (!sessionInfo.connection.isMidletVersionAndAbove(430) && !sessionInfo.connection.isBrowserDevice()) {
         this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "You have too many failed login attempts. Please login using latest version of migme, Web, or WAP");
      } else {
         Captcha captcha = sessionInfo.connection.createCaptcha(this, 4);

         try {
            this.packetsToReturn.add(new FusionPktCaptchaOld(this.transactionId, "Please enter the letters as shown in the image below.", captcha, sessionInfo.connection));
         } catch (IOException var4) {
            log.error(String.format("Failed to create captcha packet for " + this.getUserInfoForLogging(sessionInfo.connection.getLoginPacketOld())));
            this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
         }
      }

   }

   public void errorOccurred(String errorMessage) {
      this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, errorMessage);
   }

   public void errorOccurred(FusionPktError.Code errorCode, String errorMessage) {
      this.pktError = new FusionPktError(this.transactionId, errorCode, errorMessage);
   }

   public void errorOccurred(String errorMessage, Exception errorCause) {
      if (errorCause != null) {
         this.pktError = new FusionPktInternalServerError(this.transactionId, errorCause, errorMessage);
      } else {
         this.pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, errorMessage);
      }

   }

   public AuthenticationServicePrx findAuthenticationServiceProxy(SSOLoginSessionInfo sessionInfo) {
      return sessionInfo.connection.findAuthenticationService();
   }

   public static void populateFusionPktMailInfo(Collection<FusionPacket> packetsToReturn, ConnectionI connection, UserData userData, CountryData countryData) {
      int unreadEmailCount = getUnreadEmailCount(connection, countryData, userData);
      if (unreadEmailCount > 0) {
         packetsToReturn.add(new FusionPktMailInfo(unreadEmailCount));
      }

   }

   public static void populatePacketsOnSlimLogin(Collection<FusionPacket> packetsToReturn, ConnectionI connection, MIS misEJB, short loginPktTransactionId, UserData userData, CountryData countryData, FusionPktLoginOld loginPacket, Integer fusionPresence) throws RemoteException, FusionException, CreateException {
      FusionPktLoginOkOld loginOk = constructSlimLoginOk(loginPktTransactionId, connection, userData, loginPacket);
      if (fusionPresence != null) {
         loginOk.setFusionPresence(fusionPresence);
      }

      packetsToReturn.add(loginOk);
   }

   public static void populatePacketsOnLogin(Collection<FusionPacket> packetsToReturn, ConnectionI connection, MIS misEJB, short loginPktTransactionId, UserData userData, CountryData countryData, CurrencyData currencyData, FusionPktLoginOld loginPacket, Integer fusionPresence) throws RemoteException, FusionException, CreateException {
      if (!(Boolean)SystemPropertyEntities.Temp.Cache.se456Packet208ChangesEnabled.getValue()) {
         populatePacketsOnLogin_beforeSE456(packetsToReturn, connection, misEJB, loginPktTransactionId, userData, countryData, currencyData, loginPacket, fusionPresence);
      } else {
         FusionPktLoginOkOld loginOk = constructLoginOk(loginPktTransactionId, connection, userData, countryData, currencyData, loginPacket);
         if (fusionPresence != null) {
            loginOk.setFusionPresence(fusionPresence);
         }

         packetsToReturn.add(loginOk);
         FusionPktEmoticonHotKeysOld emoticonHotKeys = new FusionPktEmoticonHotKeysOld(loginPktTransactionId, connection.getUserPrx());
         packetsToReturn.add(emoticonHotKeys);
         if (connection.isMidletVersionAndAbove(300)) {
            List<MenuData> menus = misEJB.getMenus(userData.countryID, connection.getClientVersion(), connection.getDeviceType().value());
            if (menus != null) {
               Iterator i$ = menus.iterator();

               while(i$.hasNext()) {
                  MenuData menuData = (MenuData)i$.next();
                  packetsToReturn.add(new FusionPktDynamicMenu(loginPktTransactionId, menuData));
               }
            }
         }

         if (connection.isMobileClientV2AndNewVersion()) {
            int appMenuVersion = misEJB.getApplicationMenuVersion(loginPacket.getClientType(), loginPacket.getClientVersion(), loginPacket.getVASTrackingId());
            Integer loginPacketAppMenuVer = loginPacket.getApplicationMenuVersion();
            if (loginPacketAppMenuVer != null && appMenuVersion > loginPacketAppMenuVer) {
               List<ApplicationMenuOptionData> menuOptions = misEJB.getApplicationMenuOptions(appMenuVersion);
               if (menuOptions != null) {
                  Iterator i$ = menuOptions.iterator();

                  while(i$.hasNext()) {
                     ApplicationMenuOptionData menu = (ApplicationMenuOptionData)i$.next();
                     packetsToReturn.add(new FusionPktApplicationMenu(menu));
                  }

                  FusionPktApplicationMenuComplete menuCompletePacket = new FusionPktApplicationMenuComplete();
                  menuCompletePacket.setMenuVersionId(appMenuVersion);
                  packetsToReturn.add(menuCompletePacket);
               }
            }
         }

         Short migLevel = loginOk.getReputationLevel() == null ? 0 : loginOk.getReputationLevel();
         boolean isNewUser = migLevel <= SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MigLevel.NEW_USER_MAX);
         if (connection.isMidletVersionAndAbove(400) || connection.isAjax()) {
            packetsToReturn.add(new FusionPktAvatarOld(loginPktTransactionId, userData));
            packetsToReturn.add(new FusionPktAccountBalanceOld(loginPktTransactionId, userData, currencyData));
            if (!connection.isMidletVersionAndAbove(430) && !connection.isAjax()) {
               if (connection.isMidletVersionAndAbove(420)) {
                  populateImPacketsForMidlet420(packetsToReturn, connection, loginPktTransactionId, loginOk);
               }
            } else {
               populateImPackets(packetsToReturn, connection, loginPktTransactionId, loginOk);
            }

            if (connection.isMidletVersionAndAbove(420) && isNewUser) {
               connection.addMidletTabAfterLogin(FusionPktMidletTab.newUserRegistrationWizardTab(loginPktTransactionId, loginOk.getMobileVerified() == 1, connection.getDeviceType()));
            }

            if (connection.isMidletVersionBelow(450)) {
               if (connection.getMidletTabsAfterLogin().size() == 0) {
                  connection.addMidletTabAfterLogin(FusionPktMidletTab.newMigWorldTab(loginPktTransactionId, connection.getRemoteAddress(), connection.getDeviceType()));
               }

               connection.addMidletTabAfterLogin(FusionPktMidletTab.newHomeTab(loginPktTransactionId));
            } else if (!isNewUser) {
               connection.addMidletTabAfterLogin(FusionPktMidletTab.newMigWorldTab(loginPktTransactionId, connection.getRemoteAddress(), connection.getDeviceType()));
            }

            if (!connection.isAjax() && userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
               connection.addMidletTabAfterLogin(FusionPktMidletTab.newMerchantHomePage(loginPktTransactionId, connection.getDeviceType()));
            }

            if (!connection.isAjax() && userData.type != UserData.TypeEnum.MIG33_TOP_MERCHANT && !userData.chatRoomAdmin) {
               connection.addMidletTabAfterLogin(FusionPktMidletTab.newMigGamesPage(loginPktTransactionId, connection.getDeviceType()));
            }

            if (misEJB.isVASBuild(connection.getUserAgent())) {
               connection.addMidletTabAfterLogin(FusionPktMidletTab.newVASPortalTab(loginPktTransactionId, connection.getDeviceType()));
            }
         }

         Byte streamUserEvents = loginPacket.getStreamUserEvents();
         if (streamUserEvents == null && connection.isMidletVersionAndAbove(400) || streamUserEvents != null && streamUserEvents == 1) {
            streamInitialUserEvents(connection, userData);
         }

         populateFusionPktMailInfo(packetsToReturn, connection, userData, countryData);
      }
   }

   private static void populateImPackets(Collection<FusionPacket> packetsToReturn, ConnectionI connection, short loginPktTransactionId, FusionPktLoginOkOld loginOk) {
      byte imDetail = 0;
      Enums.IMEnum[] arr$ = Enums.IMEnum.values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Enums.IMEnum otherIM = arr$[i$];
         if (otherIM != Enums.IMEnum.AIM && otherIM != Enums.IMEnum.FUSION) {
            switch(otherIM) {
            case MSN:
               imDetail = loginOk.getMSNDetail();
            case AIM:
            default:
               break;
            case YAHOO:
               imDetail = loginOk.getYahooDetail();
               break;
            case GTALK:
               imDetail = loginOk.getGTalkDetail();
               break;
            case FACEBOOK:
               imDetail = loginOk.getFacebookDetail();
            }

            FusionPktIMAvailableOld imAvailablePkt = new FusionPktIMAvailableOld(loginPktTransactionId, otherIM, imDetail);
            packetsToReturn.add(imAvailablePkt);
         }
      }

   }

   private static void populateImPacketsForMidlet420(Collection<FusionPacket> packetsToReturn, ConnectionI connection, short loginPktTransactionId, FusionPktLoginOkOld loginOk) {
      if (loginOk.getMSNDetail() != null && loginOk.getMSNDetail() == 2) {
         packetsToReturn.add(new FusionPktIMSessionStatusOld(loginPktTransactionId, ImType.MSN, (byte)1, (String)null));
      }

      if (loginOk.getYahooDetail() != null && loginOk.getYahooDetail() == 2) {
         packetsToReturn.add(new FusionPktIMSessionStatusOld(loginPktTransactionId, ImType.YAHOO, (byte)1, (String)null));
      }

   }

   private static void populatePacketsOnLogin_beforeSE456(Collection<FusionPacket> packetsToReturn, ConnectionI connection, MIS misEJB, short loginPktTransactionId, UserData userData, CountryData countryData, CurrencyData currencyData, FusionPktLoginOld loginPacket, Integer fusionPresence) throws RemoteException, FusionException, CreateException {
      FusionPktLoginOkOld loginOk = constructLoginOk(loginPktTransactionId, connection, userData, countryData, currencyData, loginPacket);
      if (fusionPresence != null) {
         loginOk.setFusionPresence(fusionPresence);
      }

      packetsToReturn.add(loginOk);
      FusionPktEmoticonHotKeysOld emoticonHotKeys = new FusionPktEmoticonHotKeysOld(loginPktTransactionId, connection.getUserPrx());
      packetsToReturn.add(emoticonHotKeys);
      if (connection.isMidletVersionAndAbove(300)) {
         List<MenuData> menus = misEJB.getMenus(userData.countryID, connection.getClientVersion(), connection.getDeviceType().value());
         if (menus != null) {
            Iterator i$ = menus.iterator();

            while(i$.hasNext()) {
               MenuData menuData = (MenuData)i$.next();
               packetsToReturn.add(new FusionPktDynamicMenu(loginPktTransactionId, menuData));
            }
         }
      }

      if (connection.isMobileClientV2AndNewVersion()) {
         int appMenuVersion = misEJB.getApplicationMenuVersion(loginPacket.getClientType(), loginPacket.getClientVersion(), loginPacket.getVASTrackingId());
         Integer loginPacketAppMenuVer = loginPacket.getApplicationMenuVersion();
         if (loginPacketAppMenuVer != null && appMenuVersion > loginPacketAppMenuVer) {
            List<ApplicationMenuOptionData> menuOptions = misEJB.getApplicationMenuOptions(appMenuVersion);
            if (menuOptions != null) {
               Iterator i$ = menuOptions.iterator();

               while(i$.hasNext()) {
                  ApplicationMenuOptionData menu = (ApplicationMenuOptionData)i$.next();
                  packetsToReturn.add(new FusionPktApplicationMenu(menu));
               }

               FusionPktApplicationMenuComplete menuCompletePacket = new FusionPktApplicationMenuComplete();
               menuCompletePacket.setMenuVersionId(appMenuVersion);
               packetsToReturn.add(menuCompletePacket);
            }
         }
      }

      Short migLevel = loginOk.getReputationLevel() == null ? 0 : loginOk.getReputationLevel();
      boolean isNewUser = migLevel <= SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MigLevel.NEW_USER_MAX);
      if (connection.isMidletVersionAndAbove(400) || connection.isAjax()) {
         packetsToReturn.add(new FusionPktAvatarOld(loginPktTransactionId, userData));
         packetsToReturn.add(new FusionPktAccountBalanceOld(loginPktTransactionId, userData, currencyData));
         if (connection.isMidletVersionAndAbove(420)) {
            if (connection.isMidletVersionAndAbove(430)) {
               byte imDetail = 0;
               Enums.IMEnum[] arr$ = Enums.IMEnum.values();
               int len$ = arr$.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  Enums.IMEnum otherIM = arr$[i$];
                  if (otherIM != Enums.IMEnum.AIM && otherIM != Enums.IMEnum.FUSION) {
                     switch(otherIM) {
                     case MSN:
                        imDetail = loginOk.getMSNDetail();
                     case AIM:
                     default:
                        break;
                     case YAHOO:
                        imDetail = loginOk.getYahooDetail();
                        break;
                     case GTALK:
                        imDetail = loginOk.getGTalkDetail();
                        break;
                     case FACEBOOK:
                        imDetail = loginOk.getFacebookDetail();
                     }

                     FusionPktIMAvailableOld imAvailablePkt = new FusionPktIMAvailableOld(loginPktTransactionId, otherIM, imDetail);
                     packetsToReturn.add(imAvailablePkt);
                  }
               }
            } else {
               if (loginOk.getMSNDetail() != null && loginOk.getMSNDetail() == 2) {
                  packetsToReturn.add(new FusionPktIMSessionStatusOld(loginPktTransactionId, ImType.MSN, (byte)1, (String)null));
               }

               if (loginOk.getYahooDetail() != null && loginOk.getYahooDetail() == 2) {
                  packetsToReturn.add(new FusionPktIMSessionStatusOld(loginPktTransactionId, ImType.YAHOO, (byte)1, (String)null));
               }
            }

            if (isNewUser) {
               connection.addMidletTabAfterLogin(FusionPktMidletTab.newUserRegistrationWizardTab(loginPktTransactionId, loginOk.getMobileVerified() == 1, connection.getDeviceType()));
            }
         }

         if (connection.isMidletVersionBelow(450)) {
            if (connection.getMidletTabsAfterLogin().size() == 0) {
               connection.addMidletTabAfterLogin(FusionPktMidletTab.newMigWorldTab(loginPktTransactionId, connection.getRemoteAddress(), connection.getDeviceType()));
            }

            connection.addMidletTabAfterLogin(FusionPktMidletTab.newHomeTab(loginPktTransactionId));
         } else if (!isNewUser) {
            connection.addMidletTabAfterLogin(FusionPktMidletTab.newMigWorldTab(loginPktTransactionId, connection.getRemoteAddress(), connection.getDeviceType()));
         }

         if (!connection.isAjax() && userData.type == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
            connection.addMidletTabAfterLogin(FusionPktMidletTab.newMerchantHomePage(loginPktTransactionId, connection.getDeviceType()));
         }

         if (!connection.isAjax() && userData.type != UserData.TypeEnum.MIG33_TOP_MERCHANT && !userData.chatRoomAdmin) {
            connection.addMidletTabAfterLogin(FusionPktMidletTab.newMigGamesPage(loginPktTransactionId, connection.getDeviceType()));
         }

         if (misEJB.isVASBuild(connection.getUserAgent())) {
            connection.addMidletTabAfterLogin(FusionPktMidletTab.newVASPortalTab(loginPktTransactionId, connection.getDeviceType()));
         }
      }

      Byte streamUserEvents = loginPacket.getStreamUserEvents();
      if (streamUserEvents == null && connection.isMidletVersionAndAbove(400) || streamUserEvents != null && streamUserEvents == 1) {
         streamInitialUserEvents(connection, userData);
      }

      populateFusionPktMailInfo(packetsToReturn, connection, userData, countryData);
   }

   public Gateway.ThreadPoolName getThreadPool() {
      return this.getResponseDelay() > 0L ? Gateway.ThreadPoolName.DELAYED_LOGIN_RESPONSE_RESULT : super.getThreadPool();
   }
}
