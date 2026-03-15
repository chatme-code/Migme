package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataSlimLoginChallenge;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktSlimLoginChallenge extends FusionPktDataSlimLoginChallenge {
   public FusionPktSlimLoginChallenge(short transactionId) {
      super(transactionId);
   }

   public FusionPktSlimLoginChallenge(FusionPacket packet) {
      super(packet);
   }
}
