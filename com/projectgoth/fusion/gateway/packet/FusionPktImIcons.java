package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataImIcons;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktImIcons extends FusionPktDataImIcons {
   public FusionPktImIcons(short transactionId) {
      super(transactionId);
   }

   public FusionPktImIcons(FusionPacket packet) {
      super(packet);
   }
}
