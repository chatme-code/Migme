package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktApplicationMenuComplete extends FusionPacket {
   public FusionPktApplicationMenuComplete() {
      super((short)934);
   }

   public FusionPktApplicationMenuComplete(short transactionId) {
      super((short)934, transactionId);
   }

   public int getMenuVersionId() {
      return this.getIntField((short)1);
   }

   public void setMenuVersionId(int menuVersionId) {
      this.setField((short)1, menuVersionId);
   }
}
