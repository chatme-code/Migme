package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ChatDestinationType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataSetChatName extends FusionRequest {
   public FusionPktDataSetChatName() {
      super(PacketType.SET_CHAT_NAME);
   }

   public FusionPktDataSetChatName(short transactionId) {
      super(PacketType.SET_CHAT_NAME, transactionId);
   }

   public FusionPktDataSetChatName(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataSetChatName(FusionPacket packet) {
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

   public final ChatDestinationType getDestinationType() {
      return ChatDestinationType.fromValue(this.getByteField((short)2));
   }

   public final void setDestinationType(ChatDestinationType destinationType) {
      this.setField((short)2, destinationType.value());
   }

   public final String getChatName() {
      return this.getStringField((short)3);
   }

   public final void setChatName(String chatName) {
      this.setField((short)3, chatName);
   }
}
