package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataCreateChatroom extends FusionRequest {
   public FusionPktDataCreateChatroom() {
      super(PacketType.CREATE_CHATROOM);
   }

   public FusionPktDataCreateChatroom(short transactionId) {
      super(PacketType.CREATE_CHATROOM, transactionId);
   }

   public FusionPktDataCreateChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataCreateChatroom(FusionPacket packet) {
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

   public final String getDescription() {
      return this.getStringField((short)2);
   }

   public final void setDescription(String description) {
      this.setField((short)2, description);
   }

   public final String getKeywords() {
      return this.getStringField((short)3);
   }

   public final void setKeywords(String keywords) {
      this.setField((short)3, keywords);
   }

   public final String getLanguage() {
      return this.getStringField((short)4);
   }

   public final void setLanguage(String language) {
      this.setField((short)4, language);
   }

   public final Boolean getAllowKicking() {
      return this.getBooleanField((short)5);
   }

   public final void setAllowKicking(boolean allowKicking) {
      this.setField((short)5, allowKicking);
   }
}
