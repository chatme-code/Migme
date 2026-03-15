package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataGetCategorizedChatroomsComplete;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetCategorizedChatroomsComplete extends FusionPktDataGetCategorizedChatroomsComplete {
   public FusionPktGetCategorizedChatroomsComplete(short transactionId) {
      super(transactionId);
   }

   public FusionPktGetCategorizedChatroomsComplete(FusionPacket packet) {
      super(packet);
   }
}
