package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.fdl.packets.FusionPktDataServerQuestion;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktServerQuestion extends FusionPktDataServerQuestion {
   public FusionPktServerQuestion() {
   }

   public FusionPktServerQuestion(short transactionId) {
      super(transactionId);
   }

   public FusionPktServerQuestion(FusionPacket packet) {
      super(packet);
   }

   public FusionPktServerQuestion(AlertMessageData alertMessageData, short transactionID) {
      super(transactionID);
      this.setQuestionType(FusionPktDataServerQuestion.QuestionType.YES_NO);
      this.setQuestion(alertMessageData.content);
      this.setUrl(alertMessageData.url);
   }

   public FusionPktServerQuestion(AlertMessageData alertMessageData) {
      this(alertMessageData, (short)0);
   }
}
