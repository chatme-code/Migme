package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataLogout extends FusionRequest {
   public FusionPktDataLogout() {
      super(PacketType.LOGOUT);
   }

   public FusionPktDataLogout(short transactionId) {
      super(PacketType.LOGOUT, transactionId);
   }

   public FusionPktDataLogout(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataLogout(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }
}
