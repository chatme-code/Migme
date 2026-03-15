package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktPingOld extends FusionRequest {
   public FusionPktPingOld() {
      super((short)2);
   }

   public FusionPktPingOld(short transactionId) {
      super((short)2, transactionId);
   }

   public FusionPktPingOld(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return new FusionPacket[]{new FusionPktPingReplyOld(this.transactionId)};
   }
}
