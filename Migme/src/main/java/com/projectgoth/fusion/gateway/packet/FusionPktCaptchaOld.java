package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;

public class FusionPktCaptchaOld extends FusionPacket {
   public FusionPktCaptchaOld() {
      super((short)16);
   }

   public FusionPktCaptchaOld(short transactionId) {
      super((short)16, transactionId);
   }

   public FusionPktCaptchaOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktCaptchaOld(short transactionId, String displayText, Captcha captcha, ConnectionI connetcion) throws IOException {
      super((short)16, transactionId);
      this.setDisplayText(displayText);
      if (!connetcion.isBrowserDevice() && !connetcion.isUnknownDevice()) {
         this.setCaptchaImage(captcha.getImageByteArray("png"));
      } else {
         this.setCaptchaImage(captcha.getBase64EncodedString("png"));
      }

   }

   public String getDisplayText() {
      return this.getStringField((short)1);
   }

   public void setDisplayText(String displayText) {
      this.setField((short)1, displayText);
   }

   public byte[] getCaptchaImage() {
      return this.getByteArrayField((short)2);
   }

   public String getCaptchaImageAsString() {
      return this.getStringField((short)2);
   }

   public void setCaptchaImage(String captchaImage) {
      this.setField((short)2, captchaImage);
   }

   public void setCaptchaImage(byte[] captchaImage) {
      this.setField((short)2, captchaImage);
   }

   public static String getImageFormat() {
      return "png";
   }
}
