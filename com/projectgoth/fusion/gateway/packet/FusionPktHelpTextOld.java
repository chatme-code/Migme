package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktHelpTextOld extends FusionPacket {
   public FusionPktHelpTextOld() {
      super((short)6);
   }

   public FusionPktHelpTextOld(short transactionId) {
      super((short)6, transactionId);
   }

   public FusionPktHelpTextOld(FusionPacket packet) {
      super(packet);
   }

   public String getText() {
      return this.getStringField((short)1);
   }

   public void setText(String text) {
      this.setField((short)1, text);
   }
}
