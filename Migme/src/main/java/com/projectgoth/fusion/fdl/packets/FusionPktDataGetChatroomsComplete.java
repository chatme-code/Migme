package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataGetChatroomsComplete extends FusionPacket {
   public FusionPktDataGetChatroomsComplete() {
      super(PacketType.GET_CHATROOMS_COMPLETE);
   }

   public FusionPktDataGetChatroomsComplete(short transactionId) {
      super(PacketType.GET_CHATROOMS_COMPLETE, transactionId);
   }

   public FusionPktDataGetChatroomsComplete(FusionPacket packet) {
      super(packet);
   }

   public final Byte getNumberOfPages() {
      return this.getByteField((short)1);
   }

   public final void setNumberOfPages(byte numberOfPages) {
      this.setField((short)1, numberOfPages);
   }

   public final String getInfoText() {
      return this.getStringField((short)2);
   }

   public final void setInfoText(String infoText) {
      this.setField((short)2, infoText);
   }
}
