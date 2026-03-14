package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.fdl.packets.FusionPktDataStickerPack;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktStickerPack extends FusionPktDataStickerPack {
   public FusionPktStickerPack(short transactionId) {
      super(transactionId);
   }

   public FusionPktStickerPack(FusionPacket packet) {
      super(packet);
   }
}
