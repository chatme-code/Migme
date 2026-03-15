package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataGetChatroomCategoriesComplete;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetChatroomCategoriesComplete extends FusionPktDataGetChatroomCategoriesComplete {
   public FusionPktGetChatroomCategoriesComplete(short transactionId) {
      super(transactionId);
   }

   public FusionPktGetChatroomCategoriesComplete(FusionPacket packet) {
      super(packet);
   }
}
