package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataGroupChatParticipants extends FusionPacket {
   public FusionPktDataGroupChatParticipants() {
      super(PacketType.GROUP_CHAT_PARTICIPANTS);
   }

   public FusionPktDataGroupChatParticipants(short transactionId) {
      super(PacketType.GROUP_CHAT_PARTICIPANTS, transactionId);
   }

   public FusionPktDataGroupChatParticipants(FusionPacket packet) {
      super(packet);
   }

   public final String getGroupChatId() {
      return this.getStringField((short)1);
   }

   public final void setGroupChatId(String groupChatId) {
      this.setField((short)1, groupChatId);
   }

   public final String[] getParticipantList() {
      return this.getStringArrayWithTrailingSeparatorField((short)2, ';');
   }

   public final void setParticipantList(String[] participantList) {
      this.setFieldWithTrailingSeparator((short)2, participantList, ';');
   }

   public final String[] getMutedList() {
      return this.getStringArrayWithTrailingSeparatorField((short)4, ';');
   }

   public final void setMutedList(String[] mutedList) {
      this.setFieldWithTrailingSeparator((short)4, mutedList, ';');
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)5));
   }

   public final void setImType(ImType imType) {
      this.setField((short)5, imType.value());
   }
}
