package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktDataCaptcha extends FusionPacket {
   public FusionPktDataCaptcha() {
      super(PacketType.CAPTCHA);
   }

   public FusionPktDataCaptcha(short transactionId) {
      super(PacketType.CAPTCHA, transactionId);
   }

   public FusionPktDataCaptcha(FusionPacket packet) {
      super(packet);
   }

   public final String getDisplayText() {
      return this.getStringField((short)1);
   }

   public final void setDisplayText(String displayText) {
      this.setField((short)1, displayText);
   }

   public final byte[] getCaptchaImage() {
      return this.getByteArrayField((short)2);
   }

   public final void setCaptchaImage(byte[] captchaImage) {
      this.setField((short)2, captchaImage);
   }
}
