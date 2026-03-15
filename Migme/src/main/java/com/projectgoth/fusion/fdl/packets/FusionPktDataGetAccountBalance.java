package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetAccountBalance extends FusionRequest {
   public FusionPktDataGetAccountBalance() {
      super(PacketType.GET_ACCOUNT_BALANCE);
   }

   public FusionPktDataGetAccountBalance(short transactionId) {
      super(PacketType.GET_ACCOUNT_BALANCE, transactionId);
   }

   public FusionPktDataGetAccountBalance(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetAccountBalance(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }
}
