package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktAlertOld extends FusionPacket {
   public FusionPktAlertOld() {
      super((short)5);
   }

   public FusionPktAlertOld(short transactionId) {
      super((short)5, transactionId);
   }

   public FusionPktAlertOld(FusionPacket packet) {
      super(packet);
   }

   public Short getAlertType() {
      return this.getShortField((short)1);
   }

   public void setAlertType(short alertType) {
      this.setField((short)1, alertType);
   }

   public String getContent() {
      return this.getStringField((short)2);
   }

   public void setContent(String content) {
      this.setField((short)2, content);
   }

   public Byte getContentType() {
      return this.getByteField((short)3);
   }

   public void setContentType(byte contentType) {
      this.setField((short)3, contentType);
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
