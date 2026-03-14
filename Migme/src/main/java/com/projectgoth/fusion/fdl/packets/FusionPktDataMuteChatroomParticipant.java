package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataMuteChatroomParticipant extends FusionRequest {
   public FusionPktDataMuteChatroomParticipant() {
      super(PacketType.MUTE_CHATROOM_PARTICIPANT);
   }

   public FusionPktDataMuteChatroomParticipant(short transactionId) {
      super(PacketType.MUTE_CHATROOM_PARTICIPANT, transactionId);
   }

   public FusionPktDataMuteChatroomParticipant(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataMuteChatroomParticipant(FusionPacket packet) {
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
