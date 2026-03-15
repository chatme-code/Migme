package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataContactListVersion;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktContactListVersion extends FusionPktDataContactListVersion {
   public FusionPktContactListVersion() {
   }

   public FusionPktContactListVersion(short transactionId) {
      super(transactionId);
   }

   public FusionPktContactListVersion(FusionPacket packet) {
      super(packet);
   }
}
