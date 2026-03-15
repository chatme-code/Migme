package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataGetCategorizedChatroomsComplete extends FusionPacket {
   public FusionPktDataGetCategorizedChatroomsComplete() {
      super(PacketType.GET_CATEGORIZED_CHATROOMS_COMPLETE);
   }

   public FusionPktDataGetCategorizedChatroomsComplete(short transactionId) {
      super(PacketType.GET_CATEGORIZED_CHATROOMS_COMPLETE, transactionId);
   }

   public FusionPktDataGetCategorizedChatroomsComplete(FusionPacket packet) {
      super(packet);
   }

   public final String getCategoryFooterText() {
      return this.getStringField((short)1);
   }

   public final void setCategoryFooterText(String categoryFooterText) {
      this.setField((short)1, categoryFooterText);
   }
}
