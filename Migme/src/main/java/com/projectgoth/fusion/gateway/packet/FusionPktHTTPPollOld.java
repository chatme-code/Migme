package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktHTTPPollOld extends FusionRequest {
   public FusionPktHTTPPollOld() {
      super((short)13);
   }

   public FusionPktHTTPPollOld(short transactionId) {
      super((short)13, transactionId);
   }

   public FusionPktHTTPPollOld(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return new FusionPacket[0];
   }
}
