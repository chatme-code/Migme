package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataChatroomParticipants;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatroomParticipants extends FusionPktDataChatroomParticipants {
   public FusionPktChatroomParticipants() {
   }

   public FusionPktChatroomParticipants(short transactionId) {
      super(transactionId);
   }

   public FusionPktChatroomParticipants(FusionPacket packet) {
      super(packet);
   }
}
