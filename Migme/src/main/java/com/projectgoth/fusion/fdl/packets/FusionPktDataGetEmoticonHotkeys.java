package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetEmoticonHotkeys extends FusionRequest {
   public FusionPktDataGetEmoticonHotkeys() {
      super(PacketType.GET_EMOTICON_HOTKEYS);
   }

   public FusionPktDataGetEmoticonHotkeys(short transactionId) {
      super(PacketType.GET_EMOTICON_HOTKEYS, transactionId);
   }

   public FusionPktDataGetEmoticonHotkeys(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetEmoticonHotkeys(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getSessionId() {
      return this.getStringField((short)1);
   }

   public final void setSessionId(String sessionId) {
      this.setField((short)1, sessionId);
   }
}
