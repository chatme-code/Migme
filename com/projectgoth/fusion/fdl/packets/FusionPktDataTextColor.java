package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataTextColor extends FusionPacket {
   public FusionPktDataTextColor() {
      super(PacketType.TEXT_COLOR);
   }

   public FusionPktDataTextColor(short transactionId) {
      super(PacketType.TEXT_COLOR, transactionId);
   }

   public FusionPktDataTextColor(FusionPacket packet) {
      super(packet);
   }

   public final String[] getChatSenderColorList() {
      return this.getStringArrayField((short)1, ';');
   }

   public final void setChatSenderColorList(String[] chatSenderColorList) {
      this.setField((short)1, chatSenderColorList, ';');
   }

   public final String[] getChatMessageColorList() {
      return this.getStringArrayField((short)2, ';');
   }

   public final void setChatMessageColorList(String[] chatMessageColorList) {
      this.setField((short)2, chatMessageColorList, ';');
   }
}
