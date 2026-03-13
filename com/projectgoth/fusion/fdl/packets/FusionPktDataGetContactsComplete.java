package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataGetContactsComplete extends FusionPacket {
   public FusionPktDataGetContactsComplete() {
      super(PacketType.GET_CONTACTS_COMPLETE);
   }

   public FusionPktDataGetContactsComplete(short transactionId) {
      super(PacketType.GET_CONTACTS_COMPLETE, transactionId);
   }

   public FusionPktDataGetContactsComplete(FusionPacket packet) {
      super(packet);
   }
}
