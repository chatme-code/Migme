package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktWAPPush extends FusionRequest {
   public FusionPktWAPPush() {
      super((short)501);
   }

   public FusionPktWAPPush(short transactionId) {
      super((short)501, transactionId);
   }

   public FusionPktWAPPush(FusionPacket packet) {
      super(packet);
   }

   public String getUsername() {
      String s = this.getStringField((short)1);
      return s == null ? null : s.trim().toLowerCase();
   }

   public void setUsername(String username) {
      this.setField((short)1, username);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Please visit http://m.mig.me");
      return new FusionPacket[]{pktError};
   }
}
