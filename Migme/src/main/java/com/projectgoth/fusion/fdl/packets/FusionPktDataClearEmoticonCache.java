package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataClearEmoticonCache extends FusionPacket {
   public FusionPktDataClearEmoticonCache() {
      super(PacketType.CLEAR_EMOTICON_CACHE);
   }

   public FusionPktDataClearEmoticonCache(short transactionId) {
      super(PacketType.CLEAR_EMOTICON_CACHE, transactionId);
   }

   public FusionPktDataClearEmoticonCache(FusionPacket packet) {
      super(packet);
   }
}
