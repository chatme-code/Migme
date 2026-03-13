package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktMailInfo extends FusionPacket {
   public FusionPktMailInfo() {
      super((short)503);
   }

   public FusionPktMailInfo(short transactionId) {
      super((short)503, transactionId);
   }

   public FusionPktMailInfo(FusionPacket packet) {
      super(packet);
   }

   public FusionPktMailInfo(int mailCount) {
      super((short)503);
      this.setMailCount(mailCount);
   }

   public Integer getMailCount() {
      return this.getIntField((short)1);
   }

   public void setMailCount(int mailCount) {
      this.setField((short)1, mailCount);
   }
}
