package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataGetChatroomCategoriesComplete extends FusionPacket {
   public FusionPktDataGetChatroomCategoriesComplete() {
      super(PacketType.GET_CHATROOM_CATEGORIES_COMPLETE);
   }

   public FusionPktDataGetChatroomCategoriesComplete(short transactionId) {
      super(PacketType.GET_CHATROOM_CATEGORIES_COMPLETE, transactionId);
   }

   public FusionPktDataGetChatroomCategoriesComplete(FusionPacket packet) {
      super(packet);
   }
}
