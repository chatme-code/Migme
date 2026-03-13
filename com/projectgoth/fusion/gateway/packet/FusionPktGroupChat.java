package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGroupChat extends FusionPacket {
   public FusionPktGroupChat() {
      super((short)750);
   }

   public FusionPktGroupChat(short transactionId) {
      super((short)750, transactionId);
   }

   public FusionPktGroupChat(FusionPacket packet) {
      super(packet);
   }

   public String getGroupChatId() {
      return this.getStringField((short)1);
   }

   public void setGroupChatId(String groupChatId) {
      this.setField((short)1, groupChatId);
   }

   public String getCreator() {
      return this.getStringField((short)2);
   }

   public void setCreator(String creator) {
      this.setField((short)2, creator);
   }

   public Byte getIMType() {
      return this.getByteField((short)3);
   }

   public void setIMType(byte imType) {
      this.setField((short)3, imType);
   }
}
