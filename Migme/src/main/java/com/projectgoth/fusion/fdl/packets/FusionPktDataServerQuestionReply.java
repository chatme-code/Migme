package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataServerQuestionReply extends FusionRequest {
   public FusionPktDataServerQuestionReply() {
      super(PacketType.SERVER_QUESTION_REPLY);
   }

   public FusionPktDataServerQuestionReply(short transactionId) {
      super(PacketType.SERVER_QUESTION_REPLY, transactionId);
   }

   public FusionPktDataServerQuestionReply(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataServerQuestionReply(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Boolean getAnswer() {
      return this.getBooleanField((short)1);
   }

   public final void setAnswer(boolean answer) {
      this.setField((short)1, answer);
   }

   public final Short getQuestionId() {
      return this.getShortField((short)2);
   }

   public final void setQuestionId(short questionId) {
      this.setField((short)2, questionId);
   }
}
