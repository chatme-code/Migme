package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ChatroomCategoryData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatRoomCategoryOld extends FusionPacket {
   public FusionPktChatRoomCategoryOld() {
      super((short)714);
   }

   public FusionPktChatRoomCategoryOld(short transactionId) {
      super((short)714, transactionId);
   }

   public FusionPktChatRoomCategoryOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktChatRoomCategoryOld(short transactionId, ChatroomCategoryData chatroomCategory) {
      super((short)714, transactionId);
      this.setCategory((short)chatroomCategory.id.byteValue());
      this.setHeader(chatroomCategory.name);
      this.setRefreshMethod(chatroomCategory.refreshMethod.value());
      this.setCollapse(chatroomCategory.intiallyCollapsedByteValue());
      this.setItemsCanBeDeleted(chatroomCategory.itemsCanBeDeletedByteValue());
   }

   private void setCategory(short category) {
      this.setField((short)1, category);
   }

   private void setHeader(String header) {
      this.setField((short)2, header);
   }

   private void setRefreshMethod(byte refreshMethod) {
      this.setField((short)3, refreshMethod);
   }

   private void setCollapse(byte collapse) {
      this.setField((short)4, collapse);
   }

   private void setItemsCanBeDeleted(byte itemsCanBeDeleted) {
      this.setField((short)5, itemsCanBeDeleted);
   }
}
