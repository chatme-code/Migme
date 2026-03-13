package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktServerQuestionReplyOld extends FusionRequest {
   public FusionPktServerQuestionReplyOld() {
      super((short)9);
   }

   public FusionPktServerQuestionReplyOld(short transactionId) {
      super((short)9, transactionId);
   }

   public FusionPktServerQuestionReplyOld(FusionPacket packet) {
      super(packet);
   }

   public Byte getAnswer() {
      return this.getByteField((short)1);
   }

   public void setAnswer(byte answer) {
      this.setField((short)1, answer);
   }

   public Short getQuestionId() {
      return this.getShortField((short)2);
   }

   public void setQuestionId(short questionId) {
      this.setField((short)2, questionId);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktServerQuestionOld serverQuestion = connection.getServerQuestionOld();
      if (serverQuestion == null) {
         FusionPktError error = new FusionPktError(this.transactionId);
         error.setErrorDescription("You have already replied the question");
         return new FusionPacket[]{error};
      } else {
         connection.setServerQuestionOld((FusionPktServerQuestionOld)null);
         Byte answer = this.getAnswer();
         if (answer != null && answer == 1) {
            String url = serverQuestion.getURL();
            if (url != null) {
               FusionPktMidletTab midletTab = new FusionPktMidletTab();
               midletTab.setURL(url);
               midletTab.setFocus((byte)1);
               return new FusionPacket[]{new FusionPktOk(this.transactionId), midletTab};
            }
         }

         return new FusionPacket[]{new FusionPktOk(this.transactionId)};
      }
   }
}
