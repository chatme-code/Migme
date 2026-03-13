package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataCreateGroupChat extends FusionRequest {
   public FusionPktDataCreateGroupChat() {
      super(PacketType.CREATE_GROUP_CHAT);
   }

   public FusionPktDataCreateGroupChat(short transactionId) {
      super(PacketType.CREATE_GROUP_CHAT, transactionId);
   }

   public FusionPktDataCreateGroupChat(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataCreateGroupChat(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getUsername() {
      return this.getStringField((short)1);
   }

   public final void setUsername(String username) {
      this.setField((short)1, username);
   }

   public final String getInvitedUsername() {
      return this.getStringField((short)2);
   }

   public final void setInvitedUsername(String invitedUsername) {
      this.setField((short)2, invitedUsername);
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)3));
   }

   public final void setImType(ImType imType) {
      this.setField((short)3, imType.value());
   }

   public final String[] getInvitedUsernameList() {
      return this.getStringArrayField((short)4);
   }

   public final void setInvitedUsernameList(String[] invitedUsernameList) {
      this.setField((short)4, invitedUsernameList);
   }
}
