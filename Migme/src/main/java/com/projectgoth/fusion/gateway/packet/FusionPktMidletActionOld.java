package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktMidletActionOld extends FusionPacket {
   public FusionPktMidletActionOld() {
      super((short)920);
   }

   public FusionPktMidletActionOld(short transactionId) {
      super((short)920, transactionId);
   }

   public FusionPktMidletActionOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getAction() {
      return this.getIntField((short)1);
   }

   public void setAction(int action) {
      this.setField((short)1, action);
   }
}
