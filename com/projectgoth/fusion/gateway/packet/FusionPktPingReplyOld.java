package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktPingReplyOld extends FusionPacket {
   public FusionPktPingReplyOld() {
      super((short)3);
   }

   public FusionPktPingReplyOld(short transactionId) {
      super((short)3, transactionId);
   }

   public FusionPktPingReplyOld(FusionPacket packet) {
      super(packet);
   }
}
