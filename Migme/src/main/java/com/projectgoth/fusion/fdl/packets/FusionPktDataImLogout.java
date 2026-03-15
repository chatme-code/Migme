package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataImLogout extends FusionRequest {
   public FusionPktDataImLogout() {
      super(PacketType.IM_LOGOUT);
   }

   public FusionPktDataImLogout(short transactionId) {
      super(PacketType.IM_LOGOUT, transactionId);
   }

   public FusionPktDataImLogout(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataImLogout(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)1));
   }

   public final void setImType(ImType imType) {
      this.setField((short)1, imType.value());
   }
}
