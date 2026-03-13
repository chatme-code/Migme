package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.enums.ServiceType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataSlimLogin extends FusionRequest {
   public FusionPktDataSlimLogin() {
      super(PacketType.SLIM_LOGIN);
   }

   public FusionPktDataSlimLogin(short transactionId) {
      super(PacketType.SLIM_LOGIN, transactionId);
   }

   public FusionPktDataSlimLogin(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataSlimLogin(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return false;
   }

   public final Integer getPasswordHash() {
      return this.getIntField((short)1);
   }

   public final void setPasswordHash(int passwordHash) {
      this.setField((short)1, passwordHash);
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

   public final String getPassword() {
      return this.getStringField((short)6);
   }

   public final void setPassword(String password) {
      this.setField((short)6, password);
   }

   public final PresenceType getInitialPresence() {
      return PresenceType.fromValue(this.getByteField((short)7));
   }

   public final void setInitialPresence(PresenceType initialPresence) {
      this.setField((short)7, initialPresence.value());
   }

   public final String getUserAgent() {
      return this.getStringField((short)8);
   }

   public final void setUserAgent(String userAgent) {
      this.setField((short)8, userAgent);
   }

   public final String getDeviceName() {
      return this.getStringField((short)9);
   }

   public final void setDeviceName(String deviceName) {
      this.setField((short)9, deviceName);
   }

   public final Short getProtocolVersion() {
      return this.getShortField((short)10);
   }

   public final void setProtocolVersion(short protocolVersion) {
      this.setField((short)10, protocolVersion);
   }
}
