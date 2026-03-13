package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetStickerPackList extends FusionRequest {
   public FusionPktDataGetStickerPackList() {
      super(PacketType.GET_STICKER_PACK_LIST);
   }

   public FusionPktDataGetStickerPackList(short transactionId) {
      super(PacketType.GET_STICKER_PACK_LIST, transactionId);
   }

   public FusionPktDataGetStickerPackList(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetStickerPackList(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }
}
