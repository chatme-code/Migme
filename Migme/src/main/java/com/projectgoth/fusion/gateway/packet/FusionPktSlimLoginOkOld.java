package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktSlimLoginOkOld extends FusionPacket {
   public FusionPktSlimLoginOkOld() {
      super((short)210);
   }

   public FusionPktSlimLoginOkOld(short transactionId) {
      super((short)210, transactionId);
   }

   public FusionPktSlimLoginOkOld(FusionPacket packet) {
      super(packet);
   }

   public String getSessionId() {
      return this.getStringField((short)1);
   }

   public void setSessionId(String sessionId) {
      this.setField((short)1, sessionId);
   }
}
