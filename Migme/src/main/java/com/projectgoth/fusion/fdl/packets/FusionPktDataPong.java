package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataPong extends FusionPacket {
   public FusionPktDataPong() {
      super(PacketType.PONG);
   }

   public FusionPktDataPong(short transactionId) {
      super(PacketType.PONG, transactionId);
   }

   public FusionPktDataPong(FusionPacket packet) {
      super(packet);
   }
}
