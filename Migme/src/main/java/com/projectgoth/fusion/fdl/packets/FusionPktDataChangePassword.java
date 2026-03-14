package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataChangePassword extends FusionRequest {
   public FusionPktDataChangePassword() {
      super(PacketType.CHANGE_PASSWORD);
   }

   public FusionPktDataChangePassword(short transactionId) {
      super(PacketType.CHANGE_PASSWORD, transactionId);
   }

   public FusionPktDataChangePassword(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataChangePassword(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getOldPassword() {
      return this.getStringField((short)1);
   }

   public final void setOldPassword(String oldPassword) {
      this.setField((short)1, oldPassword);
   }

   public final String getNewPassword() {
      return this.getStringField((short)2);
   }

   public final void setNewPassword(String newPassword) {
      this.setField((short)2, newPassword);
   }
}
