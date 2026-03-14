package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetChatRoomsCompleteOld extends FusionPacket {
   public FusionPktGetChatRoomsCompleteOld() {
      super((short)702);
   }

   public FusionPktGetChatRoomsCompleteOld(short transactionId) {
      super((short)702, transactionId);
   }

   public FusionPktGetChatRoomsCompleteOld(FusionPacket packet) {
      super(packet);
   }

   public Byte getPages() {
      return this.getByteField((short)1);
   }

   public void setPages(byte pages) {
      this.setField((short)1, pages);
   }

   public String getInfoText() {
      return this.getStringField((short)2);
   }

   public void setInfoText(String infoText) {
      this.setField((short)2, infoText);
   }
}
