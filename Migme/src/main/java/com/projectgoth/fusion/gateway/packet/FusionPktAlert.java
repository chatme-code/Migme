package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataAlert;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktAlert extends FusionPktDataAlert {
   public FusionPktAlert() {
   }

   public FusionPktAlert(short transactionId) {
      super(transactionId);
   }

   public FusionPktAlert(FusionPacket packet) {
      super(packet);
   }
}
