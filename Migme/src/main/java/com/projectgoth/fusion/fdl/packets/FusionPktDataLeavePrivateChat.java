package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataLeavePrivateChat extends FusionRequest {
   public FusionPktDataLeavePrivateChat() {
      super(PacketType.LEAVE_PRIVATE_CHAT);
   }

   public FusionPktDataLeavePrivateChat(short transactionId) {
      super(PacketType.LEAVE_PRIVATE_CHAT, transactionId);
   }

   public FusionPktDataLeavePrivateChat(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataLeavePrivateChat(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getChatId() {
      return this.getStringField((short)1);
   }

   public final void setChatId(String chatId) {
      this.setField((short)1, chatId);
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)2));
   }

   public final void setImType(ImType imType) {
      this.setField((short)2, imType.value());
   }
}
