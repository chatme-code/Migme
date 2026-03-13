package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataChatroomUserStatus;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatroomUserStatus extends FusionPktDataChatroomUserStatus {
   public FusionPktChatroomUserStatus() {
   }

   public FusionPktChatroomUserStatus(FusionPacket packet) {
      super(packet);
   }
}
