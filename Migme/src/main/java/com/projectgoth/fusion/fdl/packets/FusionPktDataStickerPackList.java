package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataStickerPackList extends FusionPacket {
   public FusionPktDataStickerPackList() {
      super(PacketType.STICKER_PACK_LIST);
   }

   public FusionPktDataStickerPackList(short transactionId) {
      super(PacketType.STICKER_PACK_LIST, transactionId);
   }

   public FusionPktDataStickerPackList(FusionPacket packet) {
      super(packet);
   }

   public final String[] getStickerPackIdList() {
      return this.getStringArrayField((short)1, ' ');
   }

   public final void setStickerPackIdList(String[] stickerPackIdList) {
      this.setField((short)1, stickerPackIdList, ' ');
   }

   public final String[] getVersionList() {
      return this.getStringArrayField((short)2, ' ');
   }

   public final void setVersionList(String[] versionList) {
      this.setField((short)2, versionList, ' ');
   }
}
