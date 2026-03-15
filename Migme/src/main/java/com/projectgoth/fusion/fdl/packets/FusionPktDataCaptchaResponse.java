package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class FusionPktDataCaptchaResponse extends FusionRequest {
   public FusionPktDataCaptchaResponse() {
      super(PacketType.CAPTCHA_RESPONSE);
   }

   public FusionPktDataCaptchaResponse(short transactionId) {
      super(PacketType.CAPTCHA_RESPONSE, transactionId);
   }

   public FusionPktDataCaptchaResponse(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataCaptchaResponse(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final String getCaptchaResponse() {
      return this.getStringField((short)1);
   }

   public final void setCaptchaResponse(String captchaResponse) {
      this.setField((short)1, captchaResponse);
   }
}
