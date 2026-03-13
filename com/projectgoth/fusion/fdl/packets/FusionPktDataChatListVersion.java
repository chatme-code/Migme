package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataChatListVersion extends FusionPacket {
   public FusionPktDataChatListVersion() {
      super(PacketType.CHAT_LIST_VERSION);
   }

   public FusionPktDataChatListVersion(short transactionId) {
      super(PacketType.CHAT_LIST_VERSION, transactionId);
   }

   public FusionPktDataChatListVersion(FusionPacket packet) {
      super(packet);
   }

   public final Integer getVersion() {
      return this.getIntField((short)1);
   }

   public final void setVersion(int version) {
      this.setField((short)1, version);
   }

   public final Long getTimestamp() {
      return this.getLongField((short)2);
   }

   public final void setTimestamp(long timestamp) {
      this.setField((short)2, timestamp);
   }
}
