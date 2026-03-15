package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetGift extends FusionRequest {
   public FusionPktDataGetGift() {
      super(PacketType.GET_GIFT);
   }

   public FusionPktDataGetGift(short transactionId) {
      super(PacketType.GET_GIFT, transactionId);
   }

   public FusionPktDataGetGift(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetGift(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Integer getLimit() {
      return this.getIntField((short)1);
   }

   public final void setLimit(int limit) {
      this.setField((short)1, limit);
   }
}
