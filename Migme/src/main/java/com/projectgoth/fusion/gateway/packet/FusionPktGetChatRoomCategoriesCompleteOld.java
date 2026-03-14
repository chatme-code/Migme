package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetChatRoomCategoriesCompleteOld extends FusionPacket {
   public FusionPktGetChatRoomCategoriesCompleteOld() {
      super((short)715);
   }

   public FusionPktGetChatRoomCategoriesCompleteOld(short transactionId) {
      super((short)715, transactionId);
   }

   public FusionPktGetChatRoomCategoriesCompleteOld(FusionPacket packet) {
      super(packet);
   }
}
