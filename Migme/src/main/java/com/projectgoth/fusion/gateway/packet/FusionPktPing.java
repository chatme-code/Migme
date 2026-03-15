package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataPing;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktPing extends FusionPktDataPing {
   public FusionPktPing() {
   }

   public FusionPktPing(short transactionId) {
      super(transactionId);
   }

   public FusionPktPing(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktPing(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return new FusionPacket[]{new FusionPktPong(this.transactionId)};
   }
}
