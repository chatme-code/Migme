package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataHttpPoll;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktHttpPoll extends FusionPktDataHttpPoll {
   public FusionPktHttpPoll() {
   }

   public FusionPktHttpPoll(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktHttpPoll(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return new FusionPacket[0];
   }
}
