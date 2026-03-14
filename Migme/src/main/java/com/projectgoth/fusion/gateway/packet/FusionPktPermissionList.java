package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktPermissionList extends FusionPacket {
   public FusionPktPermissionList() {
      super((short)416);
   }

   public FusionPktPermissionList(short transactionId) {
      super((short)416, transactionId);
   }

   public FusionPktPermissionList(FusionPacket packet) {
      super(packet);
   }

   public String getAllowList() {
      return this.getStringField((short)1);
   }

   public void setAllowList(String allowList) {
      this.setField((short)1, allowList);
   }

   public String getBlockList() {
      return this.getStringField((short)2);
   }

   public void setBlockList(String blockList) {
      this.setField((short)2, blockList);
   }
}
