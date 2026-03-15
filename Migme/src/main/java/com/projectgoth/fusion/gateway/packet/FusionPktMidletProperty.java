package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataMidletProperty;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktMidletProperty extends FusionPktDataMidletProperty {
   public FusionPktMidletProperty(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktMidletProperty(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return (new FusionPktOk(this.transactionId)).toArray();
   }
}
