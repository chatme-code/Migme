package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataRemoveFavoriteChatroom extends FusionRequest {
   public FusionPktDataRemoveFavoriteChatroom() {
      super(PacketType.REMOVE_FAVORITE_CHATROOM);
   }

   public FusionPktDataRemoveFavoriteChatroom(short transactionId) {
      super(PacketType.REMOVE_FAVORITE_CHATROOM, transactionId);
   }

   public FusionPktDataRemoveFavoriteChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataRemoveFavoriteChatroom(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Short getCategoryId() {
      return this.getShortField((short)1);
   }

   public final void setCategoryId(short categoryId) {
      this.setField((short)1, categoryId);
   }

   public final String getChatroomName() {
      return this.getStringField((short)2);
   }

   public final void setChatroomName(String chatroomName) {
      this.setField((short)2, chatroomName);
   }
}
