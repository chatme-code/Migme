package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataActivateAccount extends FusionRequest {
   public FusionPktDataActivateAccount() {
      super(PacketType.ACTIVATE_ACCOUNT);
   }

   public FusionPktDataActivateAccount(short transactionId) {
      super(PacketType.ACTIVATE_ACCOUNT, transactionId);
   }

   public FusionPktDataActivateAccount(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataActivateAccount(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getVerificationCode() {
      return this.getStringField((short)1);
   }

   public final void setVerificationCode(String verificationCode) {
      this.setField((short)1, verificationCode);
   }
}
