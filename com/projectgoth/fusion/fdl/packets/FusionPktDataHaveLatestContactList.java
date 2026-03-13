package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataHaveLatestContactList extends FusionRequest {
   public FusionPktDataHaveLatestContactList() {
      super(PacketType.HAVE_LATEST_CONTACT_LIST);
   }

   public FusionPktDataHaveLatestContactList(short transactionId) {
      super(PacketType.HAVE_LATEST_CONTACT_LIST, transactionId);
   }

   public FusionPktDataHaveLatestContactList(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataHaveLatestContactList(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final Long getTimestamp() {
      return this.getLongField((short)1);
   }

   public final void setTimestamp(long timestamp) {
      this.setField((short)1, timestamp);
   }
}
