package com.projectgoth.fusion.gateway.packet.chatsync;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktEndMessages extends FusionPacket {
   public FusionPktEndMessages(String chatID, String firstMessageGUID, String finalMessageGUID, int messagesSent, Short fusionPktTxnId) {
      super((short)562);
      this.setChatIdentifier(chatID);
      if (firstMessageGUID != null) {
         this.setFirstMessageGUID(firstMessageGUID);
      }

      if (finalMessageGUID != null) {
         this.setFinalMessageGUID(finalMessageGUID);
      }

      this.setMessagesSent(messagesSent);
      if (fusionPktTxnId != null) {
         this.setTransactionId(fusionPktTxnId);
      }

   }

   public FusionPktEndMessages(short transactionId) {
      super((short)562, transactionId);
   }

   public FusionPktEndMessages(FusionPacket packet) {
      super(packet);
   }

   public String getChatIdentifier() {
      return this.getStringField((short)1);
   }

   public void setChatIdentifier(String id) {
      this.setField((short)1, id);
   }

   public String getFirstMessageGUID() {
      return this.getStringField((short)2);
   }

   public void setFirstMessageGUID(String guid) {
      this.setField((short)2, guid);
   }

   public String getFinalMessageGUID() {
      return this.getStringField((short)3);
   }

   public void setFinalMessageGUID(String guid) {
      this.setField((short)3, guid);
   }

   public int getMessagesSent() {
      return this.getIntField((short)4);
   }

   public void setMessagesSent(int count) {
      this.setField((short)4, count);
   }
}
