package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataStickerPack extends FusionPacket {
   public FusionPktDataStickerPack() {
      super(PacketType.STICKER_PACK);
   }

   public FusionPktDataStickerPack(short transactionId) {
      super(PacketType.STICKER_PACK, transactionId);
   }

   public FusionPktDataStickerPack(FusionPacket packet) {
      super(packet);
   }

   public final Integer getStickerPackId() {
      return this.getIntField((short)1);
   }

   public final void setStickerPackId(int stickerPackId) {
      this.setField((short)1, stickerPackId);
   }

   public final String getName() {
      return this.getStringField((short)2);
   }

   public final void setName(String name) {
      this.setField((short)2, name);
   }

   public final String[] getHotkeyList() {
      return this.getStringArrayField((short)3, ' ');
   }

   public final void setHotkeyList(String[] hotkeyList) {
      this.setField((short)3, hotkeyList, ' ');
   }

   public final String getIconUrl() {
      return this.getStringField((short)4);
   }

   public final void setIconUrl(String iconUrl) {
      this.setField((short)4, iconUrl);
   }

   public final String getVersion() {
      return this.getStringField((short)5);
   }

   public final void setVersion(String version) {
      this.setField((short)5, version);
   }
}
