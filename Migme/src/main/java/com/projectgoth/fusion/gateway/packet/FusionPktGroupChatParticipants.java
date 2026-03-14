package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataGroupChatParticipants;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGroupChatParticipants extends FusionPktDataGroupChatParticipants {
   public FusionPktGroupChatParticipants() {
   }

   public FusionPktGroupChatParticipants(short transactionId) {
      super(transactionId);
   }

   public FusionPktGroupChatParticipants(FusionPacket packet) {
      super(packet);
   }
}
