package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataMidletAction;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktMidletAction extends FusionPktDataMidletAction {
   public FusionPktMidletAction() {
   }

   public FusionPktMidletAction(short transactionId) {
      super(transactionId);
   }

   public FusionPktMidletAction(FusionPacket packet) {
      super(packet);
   }
}
