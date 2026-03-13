package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataSetStatusMessage extends FusionRequest {
   public FusionPktDataSetStatusMessage() {
      super(PacketType.SET_STATUS_MESSAGE);
   }

   public FusionPktDataSetStatusMessage(short transactionId) {
      super(PacketType.SET_STATUS_MESSAGE, transactionId);
   }

   public FusionPktDataSetStatusMessage(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataSetStatusMessage(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getStatusMessage() {
      return this.getStringField((short)1);
   }

   public final void setStatusMessage(String statusMessage) {
      this.setField((short)1, statusMessage);
   }
}
