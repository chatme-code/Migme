package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataSlimLoginOk extends FusionPacket {
   public FusionPktDataSlimLoginOk() {
      super(PacketType.SLIM_LOGIN_OK);
   }

   public FusionPktDataSlimLoginOk(short transactionId) {
      super(PacketType.SLIM_LOGIN_OK, transactionId);
   }

   public FusionPktDataSlimLoginOk(FusionPacket packet) {
      super(packet);
   }

   public final String getSessionId() {
      return this.getStringField((short)1);
   }

   public final void setSessionId(String sessionId) {
      this.setField((short)1, sessionId);
   }
}
