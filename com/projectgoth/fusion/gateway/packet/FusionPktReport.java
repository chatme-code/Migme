package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataReport;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktReport extends FusionPktDataReport {
   public FusionPktReport(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktReport(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return new FusionPacket[]{new FusionPktOk(this.transactionId)};
   }
}
