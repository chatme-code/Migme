package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.fdl.packets.FusionPktDataCaptcha;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;

public class FusionPktCaptcha extends FusionPktDataCaptcha {
   public FusionPktCaptcha(FusionPacket packet) {
      super(packet);
   }

   public FusionPktCaptcha(short transactionId, String displayText, Captcha captcha, ConnectionI connetcion) throws IOException {
      super(transactionId);
      this.setDisplayText(displayText);
      this.setCaptchaImage(captcha.getImageByteArray("png"));
   }
}
