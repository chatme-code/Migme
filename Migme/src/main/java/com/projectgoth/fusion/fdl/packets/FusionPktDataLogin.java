package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.enums.ServiceType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataLogin extends FusionRequest {
   public FusionPktDataLogin() {
      super(PacketType.LOGIN);
   }

   public FusionPktDataLogin(short transactionId) {
      super(PacketType.LOGIN, transactionId);
   }

   public FusionPktDataLogin(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataLogin(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return false;
   }

   public final Short getProtocolVersion() {
      return this.getShortField((short)1);
   }

   public final void setProtocolVersion(short protocolVersion) {
      this.setField((short)1, protocolVersion);
   }

   public final ClientType getClientType() {
      return ClientType.fromValue(this.getByteField((short)2));
   }

   public final void setClientType(ClientType clientType) {
      this.setField((short)2, clientType.value());
   }

   public final Short getClientVersion() {
      return this.getShortField((short)3);
   }

   public final void setClientVersion(short clientVersion) {
      this.setField((short)3, clientVersion);
   }

   public final ServiceType getServiceType() {
      return ServiceType.fromValue(this.getByteField((short)4));
   }

   public final void setServiceType(ServiceType serviceType) {
      this.setField((short)4, serviceType.value());
   }

   public final String getUsername() {
      return this.getStringField((short)5);
   }

   public final void setUsername(String username) {
      this.setField((short)5, username);
   }

   public final String getEmailAddress() {
      return this.getStringField((short)6);
   }

   public final void setEmailAddress(String emailAddress) {
      this.setField((short)6, emailAddress);
   }

   public final String getUserAgent() {
      return this.getStringField((short)7);
   }

   public final void setUserAgent(String userAgent) {
      this.setField((short)7, userAgent);
   }

   public final String getDeviceName() {
      return this.getStringField((short)8);
   }

   public final void setDeviceName(String deviceName) {
      this.setField((short)8, deviceName);
   }

   public final PresenceType getInitialPresence() {
      return PresenceType.fromValue(this.getByteField((short)9));
   }

   public final void setInitialPresence(PresenceType initialPresence) {
      this.setField((short)9, initialPresence.value());
   }

   public final Boolean getIsVoiceCapable() {
      return this.getBooleanField((short)10);
   }

   public final void setIsVoiceCapable(boolean isVoiceCapable) {
      this.setField((short)10, isVoiceCapable);
   }

   public final Integer getFontHeight() {
      return this.getIntField((short)11);
   }

   public final void setFontHeight(int fontHeight) {
      this.setField((short)11, fontHeight);
   }

   public final Integer getScreenWidth() {
      return this.getIntField((short)12);
   }

   public final void setScreenWidth(int screenWidth) {
      this.setField((short)12, screenWidth);
   }

   public final Integer getScreenHeight() {
      return this.getIntField((short)13);
   }

   public final void setScreenHeight(int screenHeight) {
      this.setField((short)13, screenHeight);
   }

   public final Integer getWallpaperId() {
      return this.getIntField((short)14);
   }

   public final void setWallpaperId(int wallpaperId) {
      this.setField((short)14, wallpaperId);
   }

   public final String getLanguage() {
      return this.getStringField((short)15);
   }

   public final void setLanguage(String language) {
      this.setField((short)15, language);
   }

   public final Integer getThemeId() {
      return this.getIntField((short)16);
   }

   public final void setThemeId(int themeId) {
      this.setField((short)16, themeId);
   }

   public final String getCellId() {
      return this.getStringField((short)17);
   }

   public final void setCellId(String cellId) {
      this.setField((short)17, cellId);
   }

   public final String getSessionId() {
      return this.getStringField((short)18);
   }

   public final void setSessionId(String sessionId) {
      this.setField((short)18, sessionId);
   }

   public final Boolean getStreamUserEvents() {
      return this.getBooleanField((short)19);
   }

   public final void setStreamUserEvents(boolean streamUserEvents) {
      this.setField((short)19, streamUserEvents);
   }

   public final String getLocalAreaCode() {
      return this.getStringField((short)20);
   }

   public final void setLocalAreaCode(String localAreaCode) {
      this.setField((short)20, localAreaCode);
   }

   public final String getMobileNetworkCode() {
      return this.getStringField((short)21);
   }

   public final void setMobileNetworkCode(String mobileNetworkCode) {
      this.setField((short)21, mobileNetworkCode);
   }

   public final String getMobileCountryCode() {
      return this.getStringField((short)22);
   }

   public final void setMobileCountryCode(String mobileCountryCode) {
      this.setField((short)22, mobileCountryCode);
   }

   public final Integer getApplicationMenuVersion() {
      return this.getIntField((short)23);
   }

   public final void setApplicationMenuVersion(int applicationMenuVersion) {
      this.setField((short)23, applicationMenuVersion);
   }

   public final String getVasTrackingId() {
      return this.getStringField((short)24);
   }

   public final void setVasTrackingId(String vasTrackingId) {
      this.setField((short)24, vasTrackingId);
   }

   public final Integer getVirtualGiftPixelSize() {
      return this.getIntField((short)25);
   }

   public final void setVirtualGiftPixelSize(int virtualGiftPixelSize) {
      this.setField((short)25, virtualGiftPixelSize);
   }

   public final Integer getStickerPixelSize() {
      return this.getIntField((short)26);
   }

   public final void setStickerPixelSize(int stickerPixelSize) {
      this.setField((short)26, stickerPixelSize);
   }
}
