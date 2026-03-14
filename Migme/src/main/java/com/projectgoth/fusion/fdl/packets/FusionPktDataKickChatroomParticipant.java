package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataKickChatroomParticipant extends FusionRequest {
   public FusionPktDataKickChatroomParticipant() {
      super(PacketType.KICK_CHATROOM_PARTICIPANT);
   }

   public FusionPktDataKickChatroomParticipant(short transactionId) {
      super(PacketType.KICK_CHATROOM_PARTICIPANT, transactionId);
   }

   public FusionPktDataKickChatroomParticipant(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataKickChatroomParticipant(FusionPacket packet) {
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

   public final String getUsername() {
      return this.getStringField((short)2);
   }

   public final void setUsername(String username) {
      this.setField((short)2, username);
   }
}
