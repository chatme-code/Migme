package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktStatusMessageOld extends FusionPacket {
   public FusionPktStatusMessageOld() {
      super((short)421);
   }

   public FusionPktStatusMessageOld(short transactionId) {
      super((short)421, transactionId);
   }

   public FusionPktStatusMessageOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getContactID() {
      return this.getIntField((short)1);
   }

   public void setContactID(int ContactID) {
      this.setField((short)1, ContactID);
   }

   public String getStatusMessage() {
      return this.getStringField((short)2);
   }

   public void setStatusMessage(String statusMessage) {
      this.setField((short)2, statusMessage);
   }

   public Long getStatusTimeStamp() {
      return this.getLongField((short)3);
   }

   public void setStatusTimeStamp(long statusTimeStamp) {
      this.setField((short)3, statusTimeStamp);
   }
}
