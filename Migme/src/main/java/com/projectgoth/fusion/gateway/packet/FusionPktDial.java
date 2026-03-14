package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDial extends FusionPacket {
   public FusionPktDial() {
      super((short)804);
   }

   public FusionPktDial(short transactionId) {
      super((short)804, transactionId);
   }

   public FusionPktDial(FusionPacket packet) {
      super(packet);
   }

   public String getPhoneNumber() {
      return this.getStringField((short)1);
   }

   public void setPhoneNumber(String phoneNumber) {
      this.setField((short)1, phoneNumber);
   }

   public String getConfirmationMessage() {
      return this.getStringField((short)2);
   }

   public void setConfirmationMessage(String confirmationMessage) {
      this.setField((short)2, confirmationMessage);
   }
}
