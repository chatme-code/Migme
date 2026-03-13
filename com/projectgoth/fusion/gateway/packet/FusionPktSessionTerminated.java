package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktSessionTerminated extends FusionPacket {
   public FusionPktSessionTerminated() {
      super((short)301);
   }

   public FusionPktSessionTerminated(short transactionId) {
      super((short)301, transactionId);
   }

   public FusionPktSessionTerminated(short transactionId, String reason) {
      this(transactionId);
      this.setReason(reason);
   }

   public FusionPktSessionTerminated(FusionPacket packet) {
      super(packet);
   }

   public String getReason() {
      return this.getStringField((short)1);
   }

   public void setReason(String reason) {
      this.setField((short)1, reason);
   }
}
