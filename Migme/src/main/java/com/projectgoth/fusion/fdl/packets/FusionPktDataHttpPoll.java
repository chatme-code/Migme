package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataHttpPoll extends FusionRequest {
   public FusionPktDataHttpPoll() {
      super(PacketType.HTTP_POLL);
   }

   public FusionPktDataHttpPoll(short transactionId) {
      super(PacketType.HTTP_POLL, transactionId);
   }

   public FusionPktDataHttpPoll(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataHttpPoll(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }
}
