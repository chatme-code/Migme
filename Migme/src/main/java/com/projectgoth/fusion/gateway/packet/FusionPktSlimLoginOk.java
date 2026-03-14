package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataSlimLoginOk;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktSlimLoginOk extends FusionPktDataSlimLoginOk {
   public FusionPktSlimLoginOk(short transactionId) {
      super(transactionId);
   }

   public FusionPktSlimLoginOk(FusionPacket packet) {
      super(packet);
   }
}
