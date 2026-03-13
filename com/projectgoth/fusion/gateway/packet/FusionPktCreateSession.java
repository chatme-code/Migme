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
import com.projectgoth.fusion.fdl.packets.FusionPktDataCreateSession;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktCreateSession extends FusionPktDataCreateSession {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktCreateSession.class));
   private static final String LOGIN_DISABLED = "You cannot login at this time, please try again";
   private static final Short DEFAULT_PROTOCOL_VERSION = new Short((short)1);
   private static final boolean DEFAULT_STREAM_USER_EVENT = true;

   public FusionPktCreateSession() {
   }

   public FusionPktCreateSession(short transactionId) {
      super(transactionId);
   }

   public FusionPktCreateSession(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktCreateSession(FusionPacket packet) {
      super(packet);
   }

   private FusionPktLogin createDummyLoginPacket(SSOLoginData loginData) throws FusionException {
      FusionPktLogin login = new FusionPktLogin();
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

      ClientType clientType = this.getClientType();
      if (clientType == null && loginData.deviceType != null) {
         clientType = ClientType.fromValue(loginData.deviceType.byteValue());
      }

      if (clientType == null) {
         throw new FusionException("Client/Device Type not supplied");
      } else {
         login.setClientType(clientType);
         login.setUsername(loginData.username);
         String userAgent = this.getUserAgent();
         if (StringUtil.isBlank(userAgent)) {
            userAgent = loginData.userAgent;
         }

         login.setUserAgent(userAgent);
         String mobileDevice = this.getDeviceName();
         if (StringUtil.isBlank(mobileDevice)) {
            mobileDevice = loginData.mobileDevice;
         }

         login.setDeviceName(mobileDevice);
         PresenceType initialPresenceValue = this.getInitialPresence();
         if (initialPresenceValue == null && loginData.presence != null) {
            initialPresenceValue = PresenceType.fromValue(loginData.presence.byteValue());
         }

         if (initialPresenceValue != null) {
            login.setInitialPresence(initialPresenceValue);
         }

         Boolean streamUserEvents = this.getStreamUserEvents();
         if (streamUserEvents == null) {
            streamUserEvents = true;
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

         String vasTrackingId = this.getVasTrackingId();
         if (!StringUtil.isBlank(vasTrackingId)) {
            login.setVasTrackingId(vasTrackingId);
         }

         Integer vgSize = this.getVirtualGiftPixelSize();
         if (vgSize != null) {
            login.setVirtualGiftPixelSize(vgSize);
         }

         Integer stickerSize = this.getStickerPixelSize();
         if (stickerSize != null) {
            login.setStickerPixelSize(stickerSize);
         }

         return login;
      }
   }

   private static SSOLoginData refreshSSOLoginData(String sid, FusionPktLogin dummyLoginPkt, String username, Integer userID, Integer passwordHash, Long sessionStartTimeInMillies) {
      SSOLoginData userData = new SSOLoginData();
      userData.username = username;
      userData.userID = userID;
      userData.presence = dummyLoginPkt.getInitialPresence() != null ? Integer.valueOf(dummyLoginPkt.getInitialPresence().value()) : null;
      userData.clientVersion = dummyLoginPkt.getClientVersion();
      userData.deviceType = dummyLoginPkt.getClientType() != null ? Integer.valueOf(dummyLoginPkt.getClientType().value()) : null;
      userData.userAgent = dummyLoginPkt.getUserAgent();
      userData.mobileDevice = dummyLoginPkt.getDeviceName();
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
            String eid = this.getSessionId();
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
                  log.info("Creating ice session for [SID:" + sid + "] " + "[IP:" + connection.getRemoteAddress() + "] " + "[MD:" + this.getDeviceName() + "] [CV:" + this.getClientVersion() + "] " + "cxn args: [SID:" + connection.getSessionID() + "] [DT:" + connection.getDeviceType() + " ] " + "[CV:" + connection.getClientVersion() + "] [MD:" + connection.getMobileDevice() + "]");
                  SSOLoginData loginData = SSOLogin.getLoginDataFromMemcache(sid);
                  if (loginData != null) {
                     label231: {
                        ClientType device = this.getClientType();
                        if (null != device && SSOLogin.isFusionSessionAllowedForDeviceType(device.value())) {
                           loginData.deviceType = Integer.valueOf(device.value());
                           if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CONCURRENT_SESSIONS_BREACHED_LOCKDOWN_ENABLED) && null != MemCachedClientWrapper.get(MemCachedKeySpaces.RateLimitKeySpace.MAX_CONCURRENT_SESSIONS_BREACHED, loginData.username)) {
                              pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "You cannot login at this time, please try again");
                              return new FusionPacket[]{pktError};
                           }

                           FusionPktLogin loginPacket = this.createDummyLoginPacket(loginData);
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
                                    FusionPktLoginResponse.createSessionObject(connection, userData, loginPacket);
                                 } else {
                                    log.info(String.format("re-connection of a logged-in session %s, will skip creating session", sid));
                                 }

                                 ClientType deviceType = connection.getDeviceType();
                                 if (deviceType != null && SystemProperty.isValueInArray(deviceType.name(), SystemPropertyEntities.SSO.DEVICES_REQUIRING_FULL_LOGIN_RESPONSE_ON_CREATE_SESSION)) {
                                    FusionPktLoginResponse.populatePacketsOnLogin(packetsToReturn, connection, misEJB, this.transactionId, userData, countryData, currencyData, loginPacket, loginData.presence);
                                 } else {
                                    FusionPktLoginResponse.populatePacketsOnSlimLogin(packetsToReturn, connection, misEJB, this.transactionId, userData, countryData, loginPacket, loginData.presence);
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
