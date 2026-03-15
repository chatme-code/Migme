package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktTextColourOld extends FusionPacket {
   public FusionPktTextColourOld() {
      super((short)924);
   }

   public FusionPktTextColourOld(short transactionId) {
      super((short)924, transactionId);
   }

   public FusionPktTextColourOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatSenderColours() {
      return this.getStringField((short)1);
   }

   public void setChatSenderColours(String chatSenderColours) {
      this.setField((short)1, chatSenderColours);
   }

   public String getChatMessageColours() {
      return this.getStringField((short)2);
   }

   public void setChatMessageColours(String chatMessageColours) {
      this.setField((short)2, chatMessageColours);
   }
}
