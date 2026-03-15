package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataSetPresence extends FusionRequest {
   public FusionPktDataSetPresence() {
      super(PacketType.SET_PRESENCE);
   }

   public FusionPktDataSetPresence(short transactionId) {
      super(PacketType.SET_PRESENCE, transactionId);
   }

   public FusionPktDataSetPresence(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataSetPresence(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final PresenceType getPresence() {
      return PresenceType.fromValue(this.getByteField((short)1));
   }

   public final void setPresence(PresenceType presence) {
      this.setField((short)1, presence.value());
   }
}
