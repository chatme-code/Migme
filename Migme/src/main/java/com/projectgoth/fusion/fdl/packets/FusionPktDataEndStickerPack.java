package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataEndStickerPack extends FusionPacket {
   public FusionPktDataEndStickerPack() {
      super(PacketType.END_STICKER_PACK);
   }

   public FusionPktDataEndStickerPack(short transactionId) {
      super(PacketType.END_STICKER_PACK, transactionId);
   }

   public FusionPktDataEndStickerPack(FusionPacket packet) {
      super(packet);
   }
}
