package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataSlimLoginChallenge extends FusionPacket {
   public FusionPktDataSlimLoginChallenge() {
      super(PacketType.SLIM_LOGIN_CHALLENGE);
   }

   public FusionPktDataSlimLoginChallenge(short transactionId) {
      super(PacketType.SLIM_LOGIN_CHALLENGE, transactionId);
   }

   public FusionPktDataSlimLoginChallenge(FusionPacket packet) {
      super(packet);
   }

   public final String getSessionId() {
      return this.getStringField((short)1);
   }

   public final void setSessionId(String sessionId) {
      this.setField((short)1, sessionId);
   }

   public final String getChallenge() {
      return this.getStringField((short)2);
   }

   public final void setChallenge(String challenge) {
      this.setField((short)2, challenge);
   }
}
