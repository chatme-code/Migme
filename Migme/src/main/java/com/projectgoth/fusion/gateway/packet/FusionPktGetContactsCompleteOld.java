package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetContactsCompleteOld extends FusionPacket {
   public FusionPktGetContactsCompleteOld() {
      super((short)403);
   }

   public FusionPktGetContactsCompleteOld(short transactionId) {
      super((short)403, transactionId);
   }

   public FusionPktGetContactsCompleteOld(FusionPacket packet) {
      super(packet);
   }
}
