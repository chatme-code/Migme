package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetCategorizedChatrooms extends FusionRequest {
   public FusionPktDataGetCategorizedChatrooms() {
      super(PacketType.GET_CATEGORIZED_CHATROOMS);
   }

   public FusionPktDataGetCategorizedChatrooms(short transactionId) {
      super(PacketType.GET_CATEGORIZED_CHATROOMS, transactionId);
   }

   public FusionPktDataGetCategorizedChatrooms(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetCategorizedChatrooms(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Short getChatroomCategoryId() {
      return this.getShortField((short)1);
   }

   public final void setChatroomCategoryId(short chatroomCategoryId) {
      this.setField((short)1, chatroomCategoryId);
   }

   public final Boolean getDoRefresh() {
      return this.getBooleanField((short)2);
   }

   public final void setDoRefresh(boolean doRefresh) {
      this.setField((short)2, doRefresh);
   }
}
