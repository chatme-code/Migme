package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataCreateSession extends FusionRequest {
   public FusionPktDataCreateSession() {
      super(PacketType.CREATE_SESSION);
   }

   public FusionPktDataCreateSession(short transactionId) {
      super(PacketType.CREATE_SESSION, transactionId);
   }

   public FusionPktDataCreateSession(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataCreateSession(FusionPacket packet) {
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

   public final String getSessionId() {
      return this.getStringField((short)3);
   }

   public final void setSessionId(String sessionId) {
      this.setField((short)3, sessionId);
   }

   public final Boolean getStreamUserEvents() {
      return this.getBooleanField((short)4);
   }

   public final void setStreamUserEvents(boolean streamUserEvents) {
      this.setField((short)4, streamUserEvents);
   }

   public final Short getClientVersion() {
      return this.getShortField((short)5);
   }

   public final void setClientVersion(short clientVersion) {
      this.setField((short)5, clientVersion);
   }

   public final String getUserAgent() {
      return this.getStringField((short)6);
   }

   public final void setUserAgent(String userAgent) {
      this.setField((short)6, userAgent);
   }

   public final String getDeviceName() {
      return this.getStringField((short)7);
   }

   public final void setDeviceName(String deviceName) {
      this.setField((short)7, deviceName);
   }

   public final PresenceType getInitialPresence() {
      return PresenceType.fromValue(this.getByteField((short)8));
   }

   public final void setInitialPresence(PresenceType initialPresence) {
      this.setField((short)8, initialPresence.value());
   }

   public final Boolean getIsVoiceCapable() {
      return this.getBooleanField((short)9);
   }

   public final void setIsVoiceCapable(boolean isVoiceCapable) {
      this.setField((short)9, isVoiceCapable);
   }

   public final Integer getFontHeight() {
      return this.getIntField((short)10);
   }

   public final void setFontHeight(int fontHeight) {
      this.setField((short)10, fontHeight);
   }

   public final Integer getScreenWidth() {
      return this.getIntField((short)11);
   }

   public final void setScreenWidth(int screenWidth) {
      this.setField((short)11, screenWidth);
   }

   public final Integer getScreenHeight() {
      return this.getIntField((short)12);
   }

   public final void setScreenHeight(int screenHeight) {
      this.setField((short)12, screenHeight);
   }

   public final Integer getWallpaperId() {
      return this.getIntField((short)13);
   }

   public final void setWallpaperId(int wallpaperId) {
      this.setField((short)13, wallpaperId);
   }

   public final Integer getThemeId() {
      return this.getIntField((short)14);
   }

   public final void setThemeId(int themeId) {
      this.setField((short)14, themeId);
   }

   public final String getLanguage() {
      return this.getStringField((short)15);
   }

   public final void setLanguage(String language) {
      this.setField((short)15, language);
   }

   public final Integer getApplicationMenuVersion() {
      return this.getIntField((short)16);
   }

   public final void setApplicationMenuVersion(int applicationMenuVersion) {
      this.setField((short)16, applicationMenuVersion);
   }

   public final String getVasTrackingId() {
      return this.getStringField((short)17);
   }

   public final void setVasTrackingId(String vasTrackingId) {
      this.setField((short)17, vasTrackingId);
   }

   public final Integer getVirtualGiftPixelSize() {
      return this.getIntField((short)18);
   }

   public final void setVirtualGiftPixelSize(int virtualGiftPixelSize) {
      this.setField((short)18, virtualGiftPixelSize);
   }

   public final Integer getStickerPixelSize() {
      return this.getIntField((short)19);
   }

   public final void setStickerPixelSize(int stickerPixelSize) {
      this.setField((short)19, stickerPixelSize);
   }
}
