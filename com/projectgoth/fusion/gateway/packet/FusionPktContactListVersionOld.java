package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktContactListVersionOld extends FusionPacket {
   public FusionPktContactListVersionOld() {
      super((short)420);
   }

   public FusionPktContactListVersionOld(short transactionId) {
      super((short)420, transactionId);
   }

   public FusionPktContactListVersionOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getContactListVersion() {
      return this.getIntField((short)1);
   }

   public void setContactListVersion(int contactListVersion) {
      this.setField((short)1, contactListVersion);
   }

   public Long getStatusTimeStamp() {
      return this.getLongField((short)2);
   }

   public void setStatusTimeStamp(long statusTimeStamp) {
      this.setField((short)2, statusTimeStamp);
   }
}
