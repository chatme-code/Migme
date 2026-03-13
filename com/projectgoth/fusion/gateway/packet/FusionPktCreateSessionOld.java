package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.clientsession.LoginException;
import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLoginData;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktCreateSessionOld extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktCreateSessionOld.class));
   private static final String LOGIN_DISABLED = "You cannot login at this time, please try again";
   private static final Short DEFAULT_PROTOCOL_VERSION = new Short((short)1);
   private static final byte DEFAULT_STREAM_USER_EVENT = 1;

   public FusionPktCreateSessionOld() {
      super((short)211, (short)0);
   }

   public FusionPktCreateSessionOld(short transactionId) {
      super((short)211, transactionId);
   }

   public FusionPktCreateSessionOld(FusionPacket packet) {
      super(packet);
   }

   public Short getProtocolVersion() {
      return this.getShortField((short)1);
   }

   public void setProtocolVersion(short protocolVersion) {
      this.setField((short)1, protocolVersion);
   }

   public Byte getClientType() {
      return this.getByteField((short)2);
   }

   public void setClientType(byte clientType) {
      this.setField((short)2, clientType);
   }

   public String getSessionID() {
      return this.getStringField((short)3);
   }

   public void setSessionID(String sid) {
      this.setField((short)3, sid);
   }

   public Byte getStreamUserEvents() {
      return this.getByteField((short)4);
   }

   public void setStreamUserEvents(byte streamUserEvents) {
      this.setField((short)4, streamUserEvents);
   }

   public Short getClientVersion() {
      return this.getShortField((short)5);
   }

   public void setClientVersion(short clientVersion) {
      this.setField((short)5, clientVersion);
   }

   public String getUserAgent() {
      return this.getStringField((short)6);
   }

   public void setUserAgent(String userAgent) {
      this.setField((short)6, userAgent);
   }

   public String getMobileDevice() {
      return this.getStringField((short)7);
   }

   public void setMobileDevice(String mobileDevice) {
      this.setField((short)7, mobileDevice);
   }

   public Byte getInitialPresence() {
      return this.getByteField((short)8);
   }

   public void setInitialPresence(byte initialPresence) {
      this.setField((short)8, initialPresence);
   }

   /** @deprecated */
   @Deprecated
   public Byte getVoiceCapability() {
      return (byte)ContactData.VoiceCapabilityEnum.NOT_AVAILABLE.value();
   }

   /** @deprecated */
   @Deprecated
   public void setVoiceCapability(byte voiceCapability) {
   }

   public Integer getFontHeight() {
      return this.getIntField((short)10);
   }

   public void setFontHeight(int fontHeight) {
      this.setField((short)10, fontHeight);
   }

   public Integer getScreenWidth() {
      return this.getIntField((short)11);
   }

   public void setScreenWidth(int screenWidth) {
      this.setField((short)11, screenWidth);
   }

   public Integer getScreenHeight() {
      return this.getIntField((short)12);
   }

   public void setScreenHeight(int screenHeight) {
      this.setField((short)12, screenHeight);
   }

   public Integer getWallpaperId() {
      return this.getIntField((short)13);
   }

   public void setWallpaperId(int wallpaperId) {
      this.setField((short)13, wallpaperId);
   }

   public Integer getThemeId() {
      return this.getIntField((short)14);
   }

   public void setThemeID(int themeId) {
      this.setField((short)14, themeId);
   }

   public String getLanguage() {
      return this.getStringField((short)15);
   }

   public void setLanguage(String language) {
      this.setField((short)15, language);
   }

   public Integer getApplicationMenuVersion() {
      return this.getIntField((short)16);
   }

   public void setApplicationMenuVersion(int appMenuVersionId) {
      this.setField((short)16, appMenuVersionId);
   }

   public String getVASTrackingId() {
      return this.getStringField((short)17);
   }

   public void setVASTrackingId(String vasTrackingId) {
      this.setField((short)17, vasTrackingId);
   }

   public Integer getVGSize() {
      return this.getIntField((short)18);
   }

   public void setVGSize(int vgSize) {
      this.setField((short)18, vgSize);
   }

   public Integer getStickerSize() {
      return this.getIntField((short)19);
   }

   public void setStickerSize(int vgSize) {
      this.setField((short)19, vgSize);
   }

   public boolean sessionRequired() {
      return false;
   }

   private FusionPktLoginOld createDummyLoginPacket(SSOLoginData loginData) throws FusionException {
      FusionPktLoginOld login = new FusionPktLoginOld();
      Short protocolVersion = this.getProtocolVersion();
      if (protocolVersion == null) {
         protocolVersion = DEFAULT_PROTOCOL_VERSION;
      }

      login.setProtocolVersion(protocolVersion);
      Short clientVersion = this.getClientVersion();
      if (clientVersion == null) {
         clientVersion = loginData.clientVersion;
      }

      if (clientVersion != null) {
         login.setClientVersion(clientVersion);
      }

      Byte deviceType = this.getClientType();
      if (deviceType == null && loginData.deviceType != null) {
         deviceType = loginData.deviceType.byteValue();
      }

      if (deviceType == null) {
         throw new FusionException("Client/Device Type not supplied");
      } else {
         login.setClientType(deviceType);
         login.setUsername(loginData.username);
         String userAgent = this.getUserAgent();
         if (StringUtil.isBlank(userAgent)) {
            userAgent = loginData.userAgent;
         }

         login.setUserAgent(userAgent);
         String mobileDevice = this.getMobileDevice();
         if (StringUtil.isBlank(mobileDevice)) {
            mobileDevice = loginData.mobileDevice;
         }

         login.setMobileDevice(mobileDevice);
         Byte initialPresenceValue = this.getInitialPresence();
         if (initialPresenceValue == null && loginData.presence != null) {
            initialPresenceValue = loginData.presence.byteValue();
         }

         PresenceType presence = null;
         if (initialPresenceValue != null) {
            presence = PresenceType.fromValue(initialPresenceValue);
         }

         if (presence != null) {
            login.setInitialPresence(presence.value());
         }

         Byte streamUserEvents = this.getStreamUserEvents();
         if (streamUserEvents == null) {
            streamUserEvents = 1;
         }

         login.setStreamUserEvents(streamUserEvents);
         Integer screenHeight = this.getScreenHeight();
         if (screenHeight != null) {
            login.setScreenHeight(screenHeight);
         }

         Integer screenWidth = this.getScreenWidth();
         if (screenWidth != null) {
            login.setScreenWidth(screenWidth);
         }

         Integer fontHeight = this.getFontHeight();
         if (fontHeight != null) {
            login.setFontHeight(fontHeight);
         }

         Integer wallpaperId = this.getWallpaperId();
         if (wallpaperId != null) {
            login.setWallpaperId(wallpaperId);
         }

         String language = this.getLanguage();
         if (!StringUtil.isBlank(language)) {
            login.setLanguage(language);
         }

         Integer applicationMenuVersion = this.getApplicationMenuVersion();
         if (applicationMenuVersion != null) {
            login.setApplicationMenuVersion(applicationMenuVersion);
         }

         String vasTrackingId = this.getVASTrackingId();
         if (!StringUtil.isBlank(vasTrackingId)) {
            login.setVASTrackingId(vasTrackingId);
         }

         Integer vgSize = this.getVGSize();
         if (vgSize != null) {
            login.setVGSize(vgSize);
         }

         Integer stickerSize = this.getStickerSize();
         if (stickerSize != null) {
            login.setStickerSize(stickerSize);
         }

         return login;
      }
   }

   private static SSOLoginData refreshSSOLoginData(String sid, FusionPktLoginOld dummyLoginPkt, String username, Integer userID, Integer passwordHash, Long sessionStartTimeInMillies) {
      SSOLoginData userData = new SSOLoginData();
      userData.username = username;
      userData.userID = userID;
      userData.presence = dummyLoginPkt.getInitialPresence() != null ? dummyLoginPkt.getInitialPresence().intValue() : null;
      userData.clientVersion = dummyLoginPkt.getClientVersion();
      userData.deviceType = dummyLoginPkt.getClientType() != null ? dummyLoginPkt.getClientType().intValue() : null;
      userData.userAgent = dummyLoginPkt.getUserAgent();
      userData.mobileDevice = dummyLoginPkt.getMobileDevice();
      userData.voiceCapability = ContactData.VoiceCapabilityEnum.NOT_AVAILABLE.value();
      userData.passwordHash = passwordHash;
      if (sessionStartTimeInMillies == null) {
         sessionStartTimeInMillies = System.currentTimeMillis();
      }

      userData.sessionStartTimeInMillis = sessionStartTimeInMillies;
      SSOLogin.setLoginDataInMemcache(sid, userData);
      return userData;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      if (!SSOLogin.isSSOLoginEnabled()) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login is disabled.")).toArray();
      } else if (null == this.getClientType()) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unknown device.")).toArray();
      } else {
         Short protoVersion = this.getProtocolVersion();
         if (protoVersion == null) {
            protoVersion = DEFAULT_PROTOCOL_VERSION;
         }

         if (!connection.getGateway().verifyProtocolVersion(protoVersion.intValue())) {
            return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNSUPPORTED_PROTOCOL, "Protocol version " + protoVersion + " is no longer supported.")};
         } else {
            List<FusionPacket> packetsToReturn = new ArrayList();
            FusionPktError pktError = null;
            String eid = this.getSessionID();
            String sid = null;
            if (SSOLogin.isEncryptedSessionID(eid)) {
               sid = SSOLogin.getSessionIDFromEncryptedSessionID(eid);
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.LOG_EID_DECRYPTION_FAILED_REMOTE_ADDRESS_ENABLED) && sid == null) {
                  log.error("Decryption failure of encrypted session id " + eid + " was from remote address=" + connection.getRemoteAddress());
               }
            } else {
               sid = eid;
            }

            if (sid == null) {
               return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "No login session is found.")).toArray();
            } else {
               boolean succeeded = false;

               try {
                  log.info("Creating ice session for [SID:" + sid + "] " + "[IP:" + connection.getRemoteAddress() + "] " + "[MD:" + this.getMobileDevice() + "] [CV:" + this.getClientVersion() + "] " + "cxn args: [SID:" + connection.getSessionID() + "] [DT:" + connection.getDeviceType() + " ] " + "[CV:" + connection.getClientVersion() + "] [MD:" + connection.getMobileDevice() + "]");
                  SSOLoginData loginData = SSOLogin.getLoginDataFromMemcache(sid);
                  if (loginData != null) {
                     label231: {
                        ClientType device = ClientType.fromValue(this.getClientType());
                        if (null != device && SSOLogin.isFusionSessionAllowedForDeviceType(device.value())) {
                           loginData.deviceType = Integer.valueOf(device.value());
                           if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CONCURRENT_SESSIONS_BREACHED_LOCKDOWN_ENABLED) && null != MemCachedClientWrapper.get(MemCachedKeySpaces.RateLimitKeySpace.MAX_CONCURRENT_SESSIONS_BREACHED, loginData.username)) {
                              pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "You cannot login at this time, please try again");
                              return new FusionPacket[]{pktError};
                           }

                           FusionPktLoginOld loginPacket = this.createDummyLoginPacket(loginData);
                           boolean loginLockAcquired = false;

                           try {
                              loginLockAcquired = SSOLogin.getLoginSerializationLock(loginData.username, 0L);
                              if (!loginLockAcquired) {
                                 log.warn(String.format("Disallowed create-session from user '%s' due to concurrent login/create-session sessions.", loginData.username));
                                 throw new LoginException("Unable to login now. Please try again later.");
                              }

                              loginData = refreshSSOLoginData(sid, loginPacket, loginData.username, loginData.userID, loginData.passwordHash, loginData.sessionStartTimeInMillis);
                              SSOLogin.setLoginDataInMemcache(sid, loginData);
                              User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                              UserData userData = userEJB.loadUser(loginData.username, false, false);
                              MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                              CurrencyData currencyData = misEJB.getCurrency(userData.currency);
                              if (currencyData == null) {
                                 throw new Exception("Invalid currency code " + userData.currency);
                              }

                              CountryData countryData = misEJB.getCountry(userData.countryID);
                              if (countryData != null && countryData.iddCode != null) {
                                 if (!connection.isOnLoginCalled()) {
                                    loginPacket.setSessionId(sid);
                                    connection = connection.onLogin(loginPacket);
                                 } else {
                                    log.info(String.format("re-connection of a logged-in session %s, will skip onLogin", sid));
                                 }

                                 if (connection.getSessionPrx() == null) {
                                    connection.setDeviceType(device);
                                    FusionPktLoginResponseOld.createSessionObject(connection, userData, loginPacket);
                                 } else {
                                    log.info(String.format("re-connection of a logged-in session %s, will skip creating session", sid));
                                 }

                                 ClientType deviceType = connection.getDeviceType();
                                 if (deviceType != null && SystemProperty.isValueInArray(deviceType.name(), SystemPropertyEntities.SSO.DEVICES_REQUIRING_FULL_LOGIN_RESPONSE_ON_CREATE_SESSION)) {
                                    FusionPktLoginResponseOld.populatePacketsOnLogin(packetsToReturn, connection, misEJB, this.transactionId, userData, countryData, currencyData, loginPacket, loginData.presence);
                                 } else {
                                    FusionPktLoginResponseOld.populatePacketsOnSlimLogin(packetsToReturn, connection, misEJB, this.transactionId, userData, countryData, loginPacket, loginData.presence);
                                 }

                                 succeeded = true;
                                 log.info(String.format("created session for [%s]", sid));
                                 break label231;
                              }

                              throw new Exception("Unable to determine user's country and IDD code");
                           } finally {
                              if (loginLockAcquired) {
                                 SSOLogin.releaseLoginSerializationLock(loginData.username);
                              }

                           }
                        }

                        throw new Exception("Unable to create session for this device.");
                     }
                  } else {
                     log.warn("Attempt to create session without valid sso session for [" + sid + "]");
                     pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "No active session detected.");
                     packetsToReturn.add(pktError);
                  }
               } catch (CreateException var26) {
                  log.error("Login failed for [" + sid + "]" + var26.getMessage(), var26);
                  pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
                  packetsToReturn.add(pktError);
               } catch (RemoteException var27) {
                  log.error("Login failed for [" + sid + "]" + var27.getMessage(), var27);
                  pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
                  packetsToReturn.add(pktError);
               } catch (LoginException var28) {
                  log.error("Login failed for [" + sid + "]" + var28.getMessage(), var28);
                  pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - " + var28.getMessage());
                  packetsToReturn.add(pktError);
               } catch (Exception var29) {
                  log.error("Login failed for [" + sid + "]" + var29.getMessage(), var29);
                  pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Login failed - Internal server error, please try again");
                  packetsToReturn.add(pktError);
               }

               if (!succeeded) {
                  log.warn("Removing connection ice adaptor due to create session failure for [" + sid + "]");
                  connection.removeConnectionFromIceAdaptor();
               }

               return (FusionPacket[])packetsToReturn.toArray(new FusionPacket[packetsToReturn.size()]);
            }
         }
      }
   }
}
