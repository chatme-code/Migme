package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataLeaveChatroom extends FusionRequest {
   public FusionPktDataLeaveChatroom() {
      super(PacketType.LEAVE_CHATROOM);
   }

   public FusionPktDataLeaveChatroom(short transactionId) {
      super(PacketType.LEAVE_CHATROOM, transactionId);
   }

   public FusionPktDataLeaveChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataLeaveChatroom(FusionPacket packet) {
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
