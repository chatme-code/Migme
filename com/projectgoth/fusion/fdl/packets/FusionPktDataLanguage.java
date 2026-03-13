package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataLanguage extends FusionRequest {
   public FusionPktDataLanguage() {
      super(PacketType.LANGUAGE);
   }

   public FusionPktDataLanguage(short transactionId) {
      super(PacketType.LANGUAGE, transactionId);
   }

   public FusionPktDataLanguage(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataLanguage(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getLanguage() {
      return this.getStringField((short)1);
   }

   public final void setLanguage(String language) {
      this.setField((short)1, language);
   }
}
