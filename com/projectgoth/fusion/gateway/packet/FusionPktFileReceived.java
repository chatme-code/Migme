package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktFileReceived extends FusionPacket {
   public FusionPktFileReceived() {
      super((short)502);
   }

   public FusionPktFileReceived(short transactionId) {
      super((short)502, transactionId);
   }

   public FusionPktFileReceived(FusionPacket packet) {
      super(packet);
   }

   public Byte getSourceType() {
      return this.getByteField((short)1);
   }

   public void setSourceType(byte sourceType) {
      this.setField((short)1, sourceType);
   }

   public String getSource() {
      String s = this.getStringField((short)2);
      return s == null ? s : s.trim().toLowerCase();
   }

   public void setSource(String source) {
      this.setField((short)2, source);
   }

   public Integer getContactId() {
      return this.getIntField((short)3);
   }

   public void setContactId(int contactId) {
      this.setField((short)3, contactId);
   }

   public String getInfoMessage() {
      return this.getStringField((short)4);
   }

   public void setInfoMessage(String infoMessage) {
      this.setField((short)4, infoMessage);
   }

   public String getURL() {
      return this.getStringField((short)5);
   }

   public void setURL(String url) {
      this.setField((short)5, url);
   }
}
