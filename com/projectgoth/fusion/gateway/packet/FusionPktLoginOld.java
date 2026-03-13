package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import org.apache.log4j.Logger;

public class FusionPktLoginOld extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLoginOld.class));
   private static final String LOGIN_DISABLED = "You cannot login at this time, please try again";

   public FusionPktLoginOld() {
      super((short)200);
   }

   public FusionPktLoginOld(short transactionId) {
      super((short)200, transactionId);
   }

   public FusionPktLoginOld(FusionPacket packet) {
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

   public Short getClientVersion() {
      return this.getShortField((short)3);
   }

   public void setClientVersion(short clientVersion) {
      this.setField((short)3, clientVersion);
   }

   public Byte getServiceType() {
      return this.getByteField((short)4);
   }

   public void setServiceType(byte serviceType) {
      this.setField((short)4, serviceType);
   }

   public String getUsername() {
      String s = this.getStringField((short)5);
      return s == null ? null : s.trim().toLowerCase();
   }

   public void setUsername(String username) {
      this.setField((short)5, username);
   }

   public String getEmailAddress() {
      return this.getStringField((short)6);
   }

   public void setEmailAddress(String emailAddress) {
      this.setField((short)6, emailAddress);
   }

   public String getUserAgent() {
      return this.getStringField((short)7);
   }

   public void setUserAgent(String userAgent) {
      this.setField((short)7, userAgent);
   }

   public String getMobileDevice() {
      return this.getStringField((short)8);
   }

   public void setMobileDevice(String mobileDevice) {
      this.setField((short)8, mobileDevice);
   }

   public Byte getInitialPresence() {
      return this.getByteField((short)9);
   }

   public void setInitialPresence(byte initialPresence) {
      this.setField((short)9, initialPresence);
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
      return this.getIntField((short)11);
   }

   public void setFontHeight(int fontHeight) {
      this.setField((short)11, fontHeight);
   }

   public Integer getScreenWidth() {
      return this.getIntField((short)12);
   }

   public void setScreenWidth(int screenWidth) {
      this.setField((short)12, screenWidth);
   }

   public Integer getScreenHeight() {
      return this.getIntField((short)13);
   }

   public void setScreenHeight(int screenHeight) {
      this.setField((short)13, screenHeight);
   }

   public Integer getWallpaperId() {
      return this.getIntField((short)14);
   }

   public void setWallpaperId(int wallpaperId) {
      this.setField((short)14, wallpaperId);
   }

   public String getLanguage() {
      return this.getStringField((short)15);
   }

   public void setLanguage(String language) {
      this.setField((short)15, language);
   }

   public Integer getThemeId() {
      return this.getIntField((short)16);
   }

   public void setThemeID(int themeId) {
      this.setField((short)16, themeId);
   }

   public String getCellId() {
      return this.getStringField((short)17);
   }

   public void setCellId(String cellId) {
      this.setField((short)17, cellId);
   }

   public String getSessionId() {
      return this.getStringField((short)18);
   }

   public void setSessionId(String sessionId) {
      this.setField((short)18, sessionId);
   }

   public Byte getStreamUserEvents() {
      return this.getByteField((short)19);
   }

   public void setStreamUserEvents(byte streamUserEvents) {
      this.setField((short)19, streamUserEvents);
   }

   public String getLocalAreaCode() {
      return this.getStringField((short)20);
   }

   public void setLocalAreaCode(String localAreaCode) {
      this.setField((short)20, localAreaCode);
   }

   public String getMobileNetworkCode() {
      return this.getStringField((short)21);
   }

   public void setMobileNetworkCode(String mobileNetworkCode) {
      this.setField((short)21, mobileNetworkCode);
   }

   public String getMobileCountryCode() {
      return this.getStringField((short)22);
   }

   public void setMobileCountryCode(String mobileCountryCode) {
      this.setField((short)22, mobileCountryCode);
   }

   public Integer getApplicationMenuVersion() {
      return this.getIntField((short)23);
   }

   public void setApplicationMenuVersion(int appMenuVersionId) {
      this.setField((short)23, appMenuVersionId);
   }

   public String getVASTrackingId() {
      return this.getStringField((short)24);
   }

   public void setVASTrackingId(String vasTrackingId) {
      this.setField((short)24, vasTrackingId);
   }

   public PresenceType getInitialPresenceEnum() {
      PresenceType presenceEnum = PresenceType.AVAILABLE;
      Byte presence = this.getInitialPresence();
      if (presence != null) {
         presenceEnum = PresenceType.fromValue(presence.intValue());
         if (presenceEnum == null) {
            presenceEnum = PresenceType.AVAILABLE;
         }
      }

      return presenceEnum;
   }

   public Integer getVGSize() {
      return this.getIntField((short)25);
   }

   public void setVGSize(int vgSize) {
      this.setField((short)25, vgSize);
   }

   public Integer getStickerSize() {
      return this.getIntField((short)26);
   }

   public void setStickerSize(int stickerSize) {
      this.setField((short)26, stickerSize);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError errPkt;
      try {
         String offlineMessage = connection.getGateway().getOfflineMessage();
         if (offlineMessage != null && offlineMessage.length() > 0) {
            throw new Exception(offlineMessage);
         } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MAX_CONCURRENT_SESSIONS_BREACHED_LOCKDOWN_ENABLED) && null != MemCachedClientWrapper.get(MemCachedKeySpaces.RateLimitKeySpace.MAX_CONCURRENT_SESSIONS_BREACHED, this.getUsername())) {
            errPkt = new FusionPktError();
            errPkt.setErrorDescription("You cannot login at this time, please try again");
            return new FusionPacket[]{errPkt};
         } else if (connection.getSessionPrx() != null) {
            throw new Exception("Session already exists");
         } else {
            Byte clientType = this.getClientType();
            if (clientType == null) {
               throw new Exception("Unspecified client type");
            } else {
               Short clientVersion = this.getClientVersion();
               if (clientVersion == null) {
                  throw new Exception("Unspecified client version");
               } else {
                  ClientType device = ClientType.fromValue(clientType);
                  if (device == null) {
                     throw new Exception("Unsupported client type " + clientType);
                  } else if ((device == ClientType.MIDP1 || device == ClientType.MIDP2) && !connection.getGateway().verifyMidletVersion(clientVersion)) {
                     return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.INVALID_VERSION, 15)};
                  } else {
                     Short protocol = this.getProtocolVersion();
                     if (protocol != null && connection.getGateway().verifyProtocolVersion(protocol.intValue())) {
                        connection = connection.onLogin(this);
                        FusionPktLoginChallengeOld loginChallenge = new FusionPktLoginChallengeOld(this.transactionId);
                        loginChallenge.setChallenge(connection.getLoginChallenge());
                        String sessionIDToReturn = null;
                        if (SSOLogin.isEncryptedSessionIDSupported(clientType, clientVersion)) {
                           sessionIDToReturn = connection.getEncryptedSessionID();
                        } else {
                           sessionIDToReturn = connection.getSessionID();
                        }

                        loginChallenge.setSessionId(sessionIDToReturn);
                        return new FusionPacket[]{loginChallenge};
                     } else {
                        return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNSUPPORTED_PROTOCOL, "Protocol version " + protocol + " is no longer supported.")};
                     }
                  }
               }
            }
         }
      } catch (Exception var9) {
         connection.onSessionTerminated();
         errPkt = new FusionPktError(this.transactionId);
         errPkt.setErrorDescription("Unable to login at this time. Please try again later");
         log.error("Login error for [" + this.getUsername() + "] [" + this.getUserAgent() + "] :" + var9.getMessage(), var9);
         return new FusionPacket[]{errPkt};
      }
   }
}
