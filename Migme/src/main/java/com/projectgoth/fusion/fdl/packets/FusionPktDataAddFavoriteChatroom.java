package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataAddFavoriteChatroom extends FusionRequest {
   public FusionPktDataAddFavoriteChatroom() {
      super(PacketType.ADD_FAVORITE_CHATROOM);
   }

   public FusionPktDataAddFavoriteChatroom(short transactionId) {
      super(PacketType.ADD_FAVORITE_CHATROOM, transactionId);
   }

   public FusionPktDataAddFavoriteChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataAddFavoriteChatroom(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getChatroomName() {
      return this.getStringField((short)1);
   }

   public final void setChatroomName(String chatroomName) {
      this.setField((short)1, chatroomName);
   }
}
