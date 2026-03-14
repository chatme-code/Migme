package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ChatParticipantType;
import com.projectgoth.fusion.fdl.enums.ChatUserStatusType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataChatroomUserStatus extends FusionPacket {
   public FusionPktDataChatroomUserStatus() {
      super(PacketType.CHATROOM_USER_STATUS);
   }

   public FusionPktDataChatroomUserStatus(short transactionId) {
      super(PacketType.CHATROOM_USER_STATUS, transactionId);
   }

   public FusionPktDataChatroomUserStatus(FusionPacket packet) {
      super(packet);
   }

   public final ChatUserStatusType getUserStatus() {
      return ChatUserStatusType.fromValue(this.getByteField((short)1));
   }

   public final void setUserStatus(ChatUserStatusType userStatus) {
      this.setField((short)1, userStatus.value());
   }

   public final String getChatroomName() {
      return this.getStringField((short)2);
   }

   public final void setChatroomName(String chatroomName) {
      this.setField((short)2, chatroomName);
   }

   public final String getUsername() {
      return this.getStringField((short)3);
   }

   public final void setUsername(String username) {
      this.setField((short)3, username);
   }

   public final ChatParticipantType getParticipantType() {
      return ChatParticipantType.fromValue(this.getByteField((short)4));
   }

   public final void setParticipantType(ChatParticipantType participantType) {
      this.setField((short)4, participantType.value());
   }
}
