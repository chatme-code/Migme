package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataDisplayPicture;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDisplayPicture extends FusionPktDataDisplayPicture {
   public FusionPktDisplayPicture() {
   }

   public FusionPktDisplayPicture(short transactionId) {
      super(transactionId);
   }

   public FusionPktDisplayPicture(FusionPacket packet) {
      super(packet);
   }
}
