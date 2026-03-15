package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataLoginChallenge extends FusionPacket {
   public FusionPktDataLoginChallenge() {
      super(PacketType.LOGIN_CHALLENGE);
   }

   public FusionPktDataLoginChallenge(short transactionId) {
      super(PacketType.LOGIN_CHALLENGE, transactionId);
   }

   public FusionPktDataLoginChallenge(FusionPacket packet) {
      super(packet);
   }

   public final String getChallenge() {
      return this.getStringField((short)1);
   }

   public final void setChallenge(String challenge) {
      this.setField((short)1, challenge);
   }

   public final String getSessionId() {
      return this.getStringField((short)2);
   }

   public final void setSessionId(String sessionId) {
      this.setField((short)2, sessionId);
   }
}
