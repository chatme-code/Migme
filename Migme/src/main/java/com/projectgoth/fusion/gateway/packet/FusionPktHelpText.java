package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataHelpText;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktHelpText extends FusionPktDataHelpText {
   public FusionPktHelpText(short transactionId) {
      super(transactionId);
   }

   public FusionPktHelpText(FusionPacket packet) {
      super(packet);
   }
}
