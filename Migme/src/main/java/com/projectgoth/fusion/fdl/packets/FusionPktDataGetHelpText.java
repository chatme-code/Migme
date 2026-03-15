package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataGetHelpText extends FusionRequest {
   public FusionPktDataGetHelpText() {
      super(PacketType.GET_HELP_TEXT);
   }

   public FusionPktDataGetHelpText(short transactionId) {
      super(PacketType.GET_HELP_TEXT, transactionId);
   }

   public FusionPktDataGetHelpText(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataGetHelpText(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return false;
   }

   public final Integer getHelpTextId() {
      return this.getIntField((short)1);
   }

   public final void setHelpTextId(int helpTextId) {
      this.setField((short)1, helpTextId);
   }
}
