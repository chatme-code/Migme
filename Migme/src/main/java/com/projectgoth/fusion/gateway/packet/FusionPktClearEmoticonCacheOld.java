package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktClearEmoticonCacheOld extends FusionPacket {
   public FusionPktClearEmoticonCacheOld() {
      super((short)915);
   }

   public FusionPktClearEmoticonCacheOld(short transactionId) {
      super((short)915, transactionId);
   }

   public FusionPktClearEmoticonCacheOld(FusionPacket packet) {
      super(packet);
   }
}
