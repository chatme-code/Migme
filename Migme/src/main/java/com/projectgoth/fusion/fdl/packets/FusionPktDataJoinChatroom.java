package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataJoinChatroom extends FusionRequest {
   public FusionPktDataJoinChatroom() {
      super(PacketType.JOIN_CHATROOM);
   }

   public FusionPktDataJoinChatroom(short transactionId) {
      super(PacketType.JOIN_CHATROOM, transactionId);
   }

   public FusionPktDataJoinChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataJoinChatroom(FusionPacket packet) {
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
