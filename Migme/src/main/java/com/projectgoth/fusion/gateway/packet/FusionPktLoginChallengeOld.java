package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktLoginChallengeOld extends FusionPacket {
   public FusionPktLoginChallengeOld() {
      super((short)201);
   }

   public FusionPktLoginChallengeOld(short transactionId) {
      super((short)201, transactionId);
   }

   public FusionPktLoginChallengeOld(FusionPacket packet) {
      super(packet);
   }

   public String getChallenge() {
      return this.getStringField((short)1);
   }

   public void setChallenge(String challenge) {
      this.setField((short)1, challenge);
   }

   public String getSessionId() {
      return this.getStringField((short)2);
   }

   public void setSessionId(String sessionId) {
      this.setField((short)2, sessionId);
   }
}
