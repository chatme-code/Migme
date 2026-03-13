package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetServerQuestion extends FusionRequest {
   public FusionPktDataGetServerQuestion() {
      super(PacketType.GET_SERVER_QUESTION);
   }

   public FusionPktDataGetServerQuestion(short transactionId) {
      super(PacketType.GET_SERVER_QUESTION, transactionId);
   }

   public FusionPktDataGetServerQuestion(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetServerQuestion(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }
}
