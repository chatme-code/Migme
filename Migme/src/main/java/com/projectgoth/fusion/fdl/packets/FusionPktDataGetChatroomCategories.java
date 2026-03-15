package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetChatroomCategories extends FusionRequest {
   public FusionPktDataGetChatroomCategories() {
      super(PacketType.GET_CHATROOM_CATEGORIES);
   }

   public FusionPktDataGetChatroomCategories(short transactionId) {
      super(PacketType.GET_CHATROOM_CATEGORIES, transactionId);
   }

   public FusionPktDataGetChatroomCategories(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetChatroomCategories(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }
}
