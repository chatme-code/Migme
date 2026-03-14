package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.HashMap;

public class FusionPktDataServerQuestion extends FusionPacket {
   public FusionPktDataServerQuestion() {
      super(PacketType.SERVER_QUESTION);
   }

   public FusionPktDataServerQuestion(short transactionId) {
      super(PacketType.SERVER_QUESTION, transactionId);
   }

   public FusionPktDataServerQuestion(FusionPacket packet) {
      super(packet);
   }

   public final FusionPktDataServerQuestion.QuestionType getQuestionType() {
      return FusionPktDataServerQuestion.QuestionType.fromValue(this.getByteField((short)1));
   }

   public final void setQuestionType(FusionPktDataServerQuestion.QuestionType questionType) {
      this.setField((short)1, questionType.value());
   }

   public final String getQuestion() {
      return this.getStringField((short)2);
   }

   public final void setQuestion(String question) {
      this.setField((short)2, question);
   }

   public final String getUrl() {
      return this.getStringField((short)3);
   }

   public final void setUrl(String url) {
      this.setField((short)3, url);
   }

   public final String getTitle() {
      return this.getStringField((short)4);
   }

   public final void setTitle(String title) {
      this.setField((short)4, title);
   }

   public final Short getTimeout() {
      return this.getShortField((short)5);
   }

   public final void setTimeout(short timeout) {
      this.setField((short)5, timeout);
   }

   public static enum QuestionType {
      YES_NO((byte)1),
      OK_CANCEL((byte)2);

      private byte value;
      private static final HashMap<Byte, FusionPktDataServerQuestion.QuestionType> LOOKUP = new HashMap();

      private QuestionType(byte value) {
         this.value = value;
      }

      public byte value() {
         return this.value;
      }

      public static FusionPktDataServerQuestion.QuestionType fromValue(int value) {
         return (FusionPktDataServerQuestion.QuestionType)LOOKUP.get((byte)value);
      }

      public static FusionPktDataServerQuestion.QuestionType fromValue(Byte value) {
         return (FusionPktDataServerQuestion.QuestionType)LOOKUP.get(value);
      }

      static {
         FusionPktDataServerQuestion.QuestionType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionPktDataServerQuestion.QuestionType questionType = arr$[i$];
            LOOKUP.put(questionType.value, questionType);
         }

      }
   }
}
