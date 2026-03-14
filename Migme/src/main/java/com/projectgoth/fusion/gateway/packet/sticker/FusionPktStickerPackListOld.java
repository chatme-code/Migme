package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktStickerPackListOld extends FusionPacket {
   private static final short FIELD_STICKER_PACK_IDS = 1;
   private static final short FIELD_STICKER_PACK_VERSIONS = 2;

   public FusionPktStickerPackListOld() {
      super((short)939);
   }

   public FusionPktStickerPackListOld(short transactionId) {
      super((short)939, transactionId);
   }

   public FusionPktStickerPackListOld(FusionPacket packet) {
      super(packet);
   }

   public String getStickerPackIDs() {
      return this.getStringField((short)1);
   }

   public void setStickerPackIDs(String stickerPackIDs) {
      this.setField((short)1, stickerPackIDs);
   }

   public String getStickerPackVersions() {
      return this.getStringField((short)2);
   }

   public void setStickerPackVersions(String stickerPackVersions) {
      this.setField((short)2, stickerPackVersions);
   }
}
