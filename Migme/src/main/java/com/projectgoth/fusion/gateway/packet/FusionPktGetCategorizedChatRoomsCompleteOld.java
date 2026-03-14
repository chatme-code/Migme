package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetCategorizedChatRoomsCompleteOld extends FusionPacket {
   public FusionPktGetCategorizedChatRoomsCompleteOld() {
      super((short)717);
   }

   public FusionPktGetCategorizedChatRoomsCompleteOld(short transactionId) {
      super((short)717, transactionId);
   }

   public FusionPktGetCategorizedChatRoomsCompleteOld(FusionPacket packet) {
      super(packet);
   }

   public String getCategoryFooter() {
      return this.getStringField((short)1);
   }

   public void setCategoryFooter(String categoryFooter) {
      this.setField((short)1, categoryFooter);
   }
}
