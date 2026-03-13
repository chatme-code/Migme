package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.fdl.packets.FusionPktDataEndStickerPack;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktEndStickerPack extends FusionPktDataEndStickerPack {
   public FusionPktEndStickerPack(short transactionId) {
      super(transactionId);
   }

   public FusionPktEndStickerPack(FusionPacket packet) {
      super(packet);
   }
}
