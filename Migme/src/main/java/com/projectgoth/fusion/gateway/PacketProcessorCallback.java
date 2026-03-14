package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;

public interface PacketProcessorCallback {
   void onFusionRequestProcessed(FusionRequest var1, FusionPacket[] var2);

   void onFusionRequestDiscarded(FusionRequest var1);

   void onFusionRequestException(FusionRequest var1, Exception var2);
}
