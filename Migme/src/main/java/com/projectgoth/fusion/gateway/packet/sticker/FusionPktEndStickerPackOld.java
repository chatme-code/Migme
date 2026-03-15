package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktEndStickerPackOld extends FusionPacket {
   public FusionPktEndStickerPackOld() {
      super((short)942);
   }

   public FusionPktEndStickerPackOld(short transactionId) {
      super((short)942, transactionId);
   }

   public FusionPktEndStickerPackOld(FusionPacket packet) {
      super(packet);
   }
}
