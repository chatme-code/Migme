package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetStickerPack extends FusionRequest {
   public FusionPktDataGetStickerPack() {
      super(PacketType.GET_STICKER_PACK);
   }

   public FusionPktDataGetStickerPack(short transactionId) {
      super(PacketType.GET_STICKER_PACK, transactionId);
   }

   public FusionPktDataGetStickerPack(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetStickerPack(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String[] getStickerPackIdList() {
      return this.getStringArrayField((short)1, ' ');
   }

   public final void setStickerPackIdList(String[] stickerPackIdList) {
      this.setField((short)1, stickerPackIdList, ' ');
   }
}
