package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDisplayPictureOld extends FusionPacket {
   public FusionPktDisplayPictureOld() {
      super((short)423);
   }

   public FusionPktDisplayPictureOld(short transactionId) {
      super((short)423, transactionId);
   }

   public FusionPktDisplayPictureOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getContactID() {
      return this.getIntField((short)1);
   }

   public void setContactID(int contactID) {
      this.setField((short)1, contactID);
   }

   public String getDisplayPicture() {
      return this.getStringField((short)2);
   }

   public void setDisplayPicture(String displayPicture) {
      this.setField((short)2, displayPicture);
   }

   public Long getStatusTimeStamp() {
      return this.getLongField((short)3);
   }

   public void setStatusTimeStamp(long statusTimeStamp) {
      this.setField((short)3, statusTimeStamp);
   }
}
