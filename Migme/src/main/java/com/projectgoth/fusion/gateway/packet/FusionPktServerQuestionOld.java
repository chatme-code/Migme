package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktServerQuestionOld extends FusionPacket {
   public FusionPktServerQuestionOld() {
      super((short)8);
   }

   public FusionPktServerQuestionOld(short transactionId) {
      super((short)8, transactionId);
   }

   public FusionPktServerQuestionOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktServerQuestionOld(AlertMessageData alertMessageData, short transactionID) {
      super((short)8, transactionID);
      this.setQuestionType((byte)1);
      this.setQuestion(alertMessageData.content);
      this.setURL(alertMessageData.url);
   }

   public FusionPktServerQuestionOld(AlertMessageData alertMessageData) {
      this(alertMessageData, (short)0);
   }

   public Byte getQuestionType() {
      return this.getByteField((short)1);
   }

   public void setQuestionType(byte questionType) {
      this.setField((short)1, questionType);
   }

   public String getQuestion() {
      return this.getStringField((short)2);
   }

   public void setQuestion(String question) {
      this.setField((short)2, question);
   }

   public String getURL() {
      return this.getStringField((short)3);
   }

   public void setURL(String url) {
      this.setField((short)3, url);
   }

   public String getTitle() {
      return this.getStringField((short)4);
   }

   public void setTitle(String title) {
      this.setField((short)4, title);
   }

   public Short getTimeout() {
      return this.getShortField((short)5);
   }

   public void setTimeout(short timeout) {
      this.setField((short)5, timeout);
   }
}
