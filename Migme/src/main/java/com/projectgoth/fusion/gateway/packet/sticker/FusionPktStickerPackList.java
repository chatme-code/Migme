package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.fdl.packets.FusionPktDataStickerPackList;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktStickerPackList extends FusionPktDataStickerPackList {
   public FusionPktStickerPackList(short transactionId) {
      super(transactionId);
   }

   public FusionPktStickerPackList(FusionPacket packet) {
      super(packet);
   }
}
