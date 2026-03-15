package com.projectgoth.fusion.gateway.packet.chatsync;

import com.projectgoth.fusion.fdl.packets.FusionPktDataChatListVersion;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatListVersion extends FusionPktDataChatListVersion {
   public FusionPktChatListVersion(short transactionId) {
      super(transactionId);
   }

   public FusionPktChatListVersion(FusionPacket packet) {
      super(packet);
   }
}
