package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataLoginResponse extends FusionRequest {
   public FusionPktDataLoginResponse() {
      super(PacketType.LOGIN_RESPONSE);
   }

   public FusionPktDataLoginResponse(short transactionId) {
      super(PacketType.LOGIN_RESPONSE, transactionId);
   }

   public FusionPktDataLoginResponse(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataLoginResponse(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Integer getPasswordHash() {
      return this.getIntField((short)1);
   }

   public final void setPasswordHash(int passwordHash) {
      this.setField((short)1, passwordHash);
   }
}
