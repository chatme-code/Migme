package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataHelpText extends FusionPacket {
   public FusionPktDataHelpText() {
      super(PacketType.HELP_TEXT);
   }

   public FusionPktDataHelpText(short transactionId) {
      super(PacketType.HELP_TEXT, transactionId);
   }

   public FusionPktDataHelpText(FusionPacket packet) {
      super(packet);
   }

   public final String getHelpText() {
      return this.getStringField((short)1);
   }

   public final void setHelpText(String helpText) {
      this.setField((short)1, helpText);
   }
}
