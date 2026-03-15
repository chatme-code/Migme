package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktOpenURLResponse extends FusionPacket {
   public FusionPktOpenURLResponse() {
      super((short)923);
   }

   public FusionPktOpenURLResponse(short transactionId) {
      super((short)923, transactionId);
   }

   public FusionPktOpenURLResponse(FusionPacket packet) {
      super(packet);
   }

   public FusionPktOpenURLResponse(short transactionId, int responseCode) {
      super((short)923, transactionId);
      this.setResponseCode(responseCode);
   }

   public FusionPktOpenURLResponse(short transactionId, int responseCode, byte[] content) {
      super((short)923, transactionId);
      this.setResponseCode(responseCode);
      this.setContent(content);
   }

   public Integer getResponseCode() {
      return this.getIntField((short)1);
   }

   public void setResponseCode(int responseCode) {
      this.setField((short)1, responseCode);
   }

   public byte[] getContent() {
      return this.getByteArrayField((short)2);
   }

   public void setContent(byte[] content) {
      this.setField((short)2, content);
   }

   public Long getTimeToLive() {
      return this.getLongField((short)3);
   }

   public void setTimeToLive(long timeToLive) {
      this.setField((short)3, timeToLive);
   }

   public String[] getCookies() {
      return this.getStringArrayField((short)11);
   }

   public void setCookies(String[] cookies) {
      this.setField((short)11, cookies);
   }
}
