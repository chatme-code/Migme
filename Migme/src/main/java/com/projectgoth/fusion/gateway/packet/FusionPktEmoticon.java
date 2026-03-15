package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktEmoticon extends FusionPacket {
   private static final short FIELD_PACK_ID = 6;
   private static final short FIELD_URL = 7;
   private static final short FIELD_FULL_URL_DATA = 8;

   public FusionPktEmoticon() {
      super((short)914);
   }

   public FusionPktEmoticon(short transactionId) {
      super((short)914, transactionId);
   }

   public FusionPktEmoticon(FusionPacket packet) {
      super(packet);
   }

   public String getHotKey() {
      return this.getStringField((short)1);
   }

   public void setHotKey(String hotKey) {
      this.setField((short)1, hotKey);
   }

   public String getAlternateHotKeys() {
      return this.getStringField((short)2);
   }

   public void setAlternateHotKeys(String alternateKeys) {
      this.setField((short)2, alternateKeys);
   }

   public String getAlias() {
      return this.getStringField((short)3);
   }

   public void setAlias(String alias) {
      this.setField((short)3, alias);
   }

   public byte[] getContent() {
      return this.getByteArrayField((short)4);
   }

   public void setContent(byte[] content) {
      this.setField((short)4, content);
   }

   public Byte getContentType() {
      return this.getByteField((short)5);
   }

   public void setContentType(byte contentType) {
      this.setField((short)5, contentType);
   }

   public Integer getPackID() {
      return this.getIntField((short)6);
   }

   public void setPackID(int contentType) {
      this.setField((short)6, contentType);
   }

   public String getURL() {
      return this.getStringField((short)7);
   }

   public void setURL(String url) {
      this.setField((short)7, url);
   }

   public String getFullURLData() {
      return this.getStringField((short)8);
   }

   public void setFullURLData(String fullURLData) {
      this.setField((short)8, fullURLData);
   }
}
