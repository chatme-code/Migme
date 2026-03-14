package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktGetEmoticonHotkeysOld extends FusionRequest {
   public FusionPktGetEmoticonHotkeysOld() {
      super((short)937);
   }

   public FusionPktGetEmoticonHotkeysOld(short transactionId) {
      super((short)937, transactionId);
   }

   public FusionPktGetEmoticonHotkeysOld(FusionPacket packet) {
      super(packet);
   }

   public String getSessionId() {
      return this.getStringField((short)1);
   }

   public void setSessionId(String sessionId) {
      this.setField((short)1, sessionId);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktEmoticonHotKeysOld emoticonHotKeys = new FusionPktEmoticonHotKeysOld(this.getTransactionId(), connection.getUserPrx());
      return emoticonHotKeys.toArray();
   }
}
