package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataGetContactsComplete;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetContactsComplete extends FusionPktDataGetContactsComplete {
   public FusionPktGetContactsComplete(short transactionId) {
      super(transactionId);
   }

   public FusionPktGetContactsComplete(FusionPacket packet) {
      super(packet);
   }
}
