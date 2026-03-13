package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGroupChatParticipantsOld extends FusionPacket {
   public FusionPktGroupChatParticipantsOld() {
      super((short)755);
   }

   public FusionPktGroupChatParticipantsOld(short transactionId) {
      super((short)755, transactionId);
   }

   public FusionPktGroupChatParticipantsOld(FusionPacket packet) {
      super(packet);
   }

   public String getGroupChatId() {
      return this.getStringField((short)1);
   }

   public void setGroupChatId(String groupChatId) {
      this.setField((short)1, groupChatId);
   }

   public String getParticipants() {
      return this.getStringField((short)2);
   }

   public void setParticipants(String participants) {
      this.setField((short)2, participants);
   }

   public String getMutedParticipants() {
      return this.getStringField((short)4);
   }

   public void setMutedParticipants(String mutedParticipants) {
      this.setField((short)4, mutedParticipants);
   }

   public Byte getIMType() {
      return this.getByteField((short)5);
   }

   public void setIMType(byte imType) {
      this.setField((short)5, imType);
   }
}
