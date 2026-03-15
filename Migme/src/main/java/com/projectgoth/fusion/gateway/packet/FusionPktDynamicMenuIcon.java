package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDynamicMenuIcon extends FusionPacket {
   public FusionPktDynamicMenuIcon() {
      super((short)930);
   }

   public FusionPktDynamicMenuIcon(short transactionId) {
      super((short)930, transactionId);
   }

   public FusionPktDynamicMenuIcon(FusionPacket packet) {
      super(packet);
   }

   public int getIconId() {
      return this.getIntField((short)1);
   }

   public void setIconId(int iconId) {
      this.setField((short)1, iconId);
   }

   public byte[] getIcon() {
      return this.getByteArrayField((short)2);
   }

   public void setIcon(byte[] icon) {
      this.setField((short)2, icon);
   }
}
