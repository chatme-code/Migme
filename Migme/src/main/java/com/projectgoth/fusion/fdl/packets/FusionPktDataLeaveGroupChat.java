package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataLeaveGroupChat extends FusionRequest {
   public FusionPktDataLeaveGroupChat() {
      super(PacketType.LEAVE_GROUP_CHAT);
   }

   public FusionPktDataLeaveGroupChat(short transactionId) {
      super(PacketType.LEAVE_GROUP_CHAT, transactionId);
   }

   public FusionPktDataLeaveGroupChat(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataLeaveGroupChat(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getGroupChatId() {
      return this.getStringField((short)1);
   }

   public final void setGroupChatId(String groupChatId) {
      this.setField((short)1, groupChatId);
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)2));
   }

   public final void setImType(ImType imType) {
      this.setField((short)2, imType.value());
   }
}
