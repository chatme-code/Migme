package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataGetChatroomsComplete;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetChatroomsComplete extends FusionPktDataGetChatroomsComplete {
   public FusionPktGetChatroomsComplete(short transactionId) {
      super(transactionId);
   }

   public FusionPktGetChatroomsComplete(FusionPacket packet) {
      super(packet);
   }
}
