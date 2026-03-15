package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataPong;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktPong extends FusionPktDataPong {
   public FusionPktPong(short transactionId) {
      super(transactionId);
   }

   public FusionPktPong(FusionPacket packet) {
      super(packet);
   }
}
