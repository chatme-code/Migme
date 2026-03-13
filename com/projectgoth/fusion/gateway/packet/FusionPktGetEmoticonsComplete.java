package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetEmoticonsComplete extends FusionPacket {
   public FusionPktGetEmoticonsComplete() {
      super((short)928);
   }

   public FusionPktGetEmoticonsComplete(short transactionId) {
      super((short)928, transactionId);
   }

   public FusionPktGetEmoticonsComplete(FusionPacket packet) {
      super(packet);
   }
}
