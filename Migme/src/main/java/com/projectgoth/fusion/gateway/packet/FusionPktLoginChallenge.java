package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataLoginChallenge;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktLoginChallenge extends FusionPktDataLoginChallenge {
   public FusionPktLoginChallenge(short transactionId) {
      super(transactionId);
   }

   public FusionPktLoginChallenge(FusionPacket packet) {
      super(packet);
   }
}
