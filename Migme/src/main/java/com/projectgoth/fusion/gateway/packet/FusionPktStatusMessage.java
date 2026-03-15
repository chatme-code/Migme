package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataStatusMessage;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktStatusMessage extends FusionPktDataStatusMessage {
   public FusionPktStatusMessage() {
   }

   public FusionPktStatusMessage(short transactionId) {
      super(transactionId);
   }

   public FusionPktStatusMessage(FusionPacket packet) {
      super(packet);
   }
}
