package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ChatroomCategoryRefreshType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataChatroomCategory extends FusionPacket {
   public FusionPktDataChatroomCategory() {
      super(PacketType.CHATROOM_CATEGORY);
   }

   public FusionPktDataChatroomCategory(short transactionId) {
      super(PacketType.CHATROOM_CATEGORY, transactionId);
   }

   public FusionPktDataChatroomCategory(FusionPacket packet) {
      super(packet);
   }

   public final Short getCategoryId() {
      return this.getShortField((short)1);
   }

   public final void setCategoryId(short categoryId) {
      this.setField((short)1, categoryId);
   }

   public final String getCategoryName() {
      return this.getStringField((short)2);
   }

   public final void setCategoryName(String categoryName) {
      this.setField((short)2, categoryName);
   }

   public final ChatroomCategoryRefreshType getRefreshMethod() {
      return ChatroomCategoryRefreshType.fromValue(this.getByteField((short)3));
   }

   public final void setRefreshMethod(ChatroomCategoryRefreshType refreshMethod) {
      this.setField((short)3, refreshMethod.value());
   }

   public final Boolean getIsCollapsed() {
      return this.getBooleanField((short)4);
   }

   public final void setIsCollapsed(boolean isCollapsed) {
      this.setField((short)4, isCollapsed);
   }

   public final Boolean getItemsCanBeDeleted() {
      return this.getBooleanField((short)5);
   }

   public final void setItemsCanBeDeleted(boolean itemsCanBeDeleted) {
      this.setField((short)5, itemsCanBeDeleted);
   }
}
