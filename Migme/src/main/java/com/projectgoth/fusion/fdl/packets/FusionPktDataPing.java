package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataPing extends FusionRequest {
   public FusionPktDataPing() {
      super(PacketType.PING);
   }

   public FusionPktDataPing(short transactionId) {
      super(PacketType.PING, transactionId);
   }

   public FusionPktDataPing(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataPing(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return false;
   }
}
