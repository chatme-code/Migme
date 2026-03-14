package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChatRoomParticipantsOld extends FusionPacket {
   public FusionPktChatRoomParticipantsOld() {
      super((short)708);
   }

   public FusionPktChatRoomParticipantsOld(short transactionId) {
      super((short)708, transactionId);
   }

   public FusionPktChatRoomParticipantsOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   public String getParticipants() {
      return this.getStringField((short)2);
   }

   public void setParticipants(String participants) {
      this.setField((short)2, participants);
   }

   public String getAdministrators() {
      return this.getStringField((short)3);
   }

   public void setAdministrators(String administrators) {
      this.setField((short)3, administrators);
   }

   public String getMutedParticipants() {
      return this.getStringField((short)4);
   }

   public void setMutedParticipants(String mutedParticipants) {
      this.setField((short)4, mutedParticipants);
   }
}
