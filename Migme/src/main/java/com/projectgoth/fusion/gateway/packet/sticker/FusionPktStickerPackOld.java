package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktStickerPackOld extends FusionPacket {
   private static final short FIELD_STICKER_PACK_ID = 1;
   private static final short FIELD_STICKER_PACK_NAME = 2;
   private static final short FIELD_STICKER_HOTKEYS = 3;
   private static final short FIELD_STICKER_PACK_ICON_URL = 4;
   private static final short FIELD_STICKER_PACK_VERSION = 5;

   public FusionPktStickerPackOld() {
      super((short)941);
   }

   public FusionPktStickerPackOld(short transactionId) {
      super((short)941, transactionId);
   }

   public FusionPktStickerPackOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getStickerPackID() {
      return this.getIntField((short)1);
   }

   public void setStickerPackID(int stickerPackID) {
      this.setField((short)1, stickerPackID);
   }

   public String getStickerPackName() {
      return this.getStringField((short)2);
   }

   public void setStickerPackName(String stickerPackName) {
      this.setField((short)2, stickerPackName);
   }

   public String getStickerHotkeys() {
      return this.getStringField((short)3);
   }

   public void setStickerHotkeys(String stickerHotkeys) {
      this.setField((short)3, stickerHotkeys);
   }

   public String getStickerPackIconURL() {
      return this.getStringField((short)4);
   }

   public void setStickerPackIconURL(String stickerPackIconURL) {
      this.setField((short)4, stickerPackIconURL);
   }

   public String getStickerPackVersion() {
      return this.getStringField((short)5);
   }

   public void setStickerPackVersion(String stickerPackVersion) {
      this.setField((short)5, stickerPackVersion);
   }
}
