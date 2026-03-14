package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataChatroomParticipants extends FusionPacket {
   public FusionPktDataChatroomParticipants() {
      super(PacketType.CHATROOM_PARTICIPANTS);
   }

   public FusionPktDataChatroomParticipants(short transactionId) {
      super(PacketType.CHATROOM_PARTICIPANTS, transactionId);
   }

   public FusionPktDataChatroomParticipants(FusionPacket packet) {
      super(packet);
   }

   public final String getChatroomName() {
      return this.getStringField((short)1);
   }

   public final void setChatroomName(String chatroomName) {
      this.setField((short)1, chatroomName);
   }

   public final String[] getParticipantList() {
      return this.getStringArrayWithTrailingSeparatorField((short)2, ';');
   }

   public final void setParticipantList(String[] participantList) {
      this.setFieldWithTrailingSeparator((short)2, participantList, ';');
   }

   public final String[] getAdministratorList() {
      return this.getStringArrayWithTrailingSeparatorField((short)3, ';');
   }

   public final void setAdministratorList(String[] administratorList) {
      this.setFieldWithTrailingSeparator((short)3, administratorList, ';');
   }

   public final String[] getMutedList() {
      return this.getStringArrayWithTrailingSeparatorField((short)4, ';');
   }

   public final void setMutedList(String[] mutedList) {
      this.setFieldWithTrailingSeparator((short)4, mutedList, ';');
   }
}
