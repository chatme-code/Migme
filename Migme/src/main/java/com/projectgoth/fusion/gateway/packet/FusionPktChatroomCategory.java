package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ChatroomCategoryData;
import com.projectgoth.fusion.fdl.packets.FusionPktDataChatroomCategory;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatroomCategory extends FusionPktDataChatroomCategory {
   public FusionPktChatroomCategory(short transactionId) {
      super(transactionId);
   }

   public FusionPktChatroomCategory(FusionPacket packet) {
      super(packet);
   }

   public FusionPktChatroomCategory(short transactionId, ChatroomCategoryData chatroomCategory) {
      super(transactionId);
      this.setCategoryId((short)chatroomCategory.id.byteValue());
      this.setCategoryName(chatroomCategory.name);
      this.setRefreshMethod(chatroomCategory.refreshMethod);
      this.setIsCollapsed(chatroomCategory.initiallyCollapsed);
      this.setItemsCanBeDeleted(chatroomCategory.itemsCanBeDeleted);
   }
}
