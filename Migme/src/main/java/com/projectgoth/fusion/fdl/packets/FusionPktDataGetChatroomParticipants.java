package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetChatroomParticipants extends FusionRequest {
   public FusionPktDataGetChatroomParticipants() {
      super(PacketType.GET_CHATROOM_PARTICIPANTS);
   }

   public FusionPktDataGetChatroomParticipants(short transactionId) {
      super(PacketType.GET_CHATROOM_PARTICIPANTS, transactionId);
   }

   public FusionPktDataGetChatroomParticipants(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetChatroomParticipants(FusionPacket packet) {
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
