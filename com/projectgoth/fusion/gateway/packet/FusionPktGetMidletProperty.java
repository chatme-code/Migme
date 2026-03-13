package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataGetMidletProperty;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetMidletProperty extends FusionPktDataGetMidletProperty {
   public FusionPktGetMidletProperty() {
   }

   public FusionPktGetMidletProperty(short transactionId) {
      super(transactionId);
   }

   public FusionPktGetMidletProperty(FusionPacket packet) {
      super(packet);
   }
}
