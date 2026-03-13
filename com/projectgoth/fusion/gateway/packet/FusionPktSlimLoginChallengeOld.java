package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktSlimLoginChallengeOld extends FusionPacket {
   public FusionPktSlimLoginChallengeOld() {
      super((short)212);
   }

   public FusionPktSlimLoginChallengeOld(short transactionId) {
      super((short)212, transactionId);
   }

   public FusionPktSlimLoginChallengeOld(FusionPacket packet) {
      super(packet);
   }

   public String getSessionId() {
      return this.getStringField((short)1);
   }

   public void setSessionId(String sessionId) {
      this.setField((short)1, sessionId);
   }

   public String getChallenge() {
      return this.getStringField((short)2);
   }

   public void setChallenge(String sessionId) {
      this.setField((short)2, sessionId);
   }
}
