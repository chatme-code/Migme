package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataLoginOk;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktLoginOk extends FusionPktDataLoginOk {
   public FusionPktLoginOk() {
   }

   public FusionPktLoginOk(short transactionId) {
      super(transactionId);
   }

   public FusionPktLoginOk(FusionPacket packet) {
      super(packet);
   }
}
