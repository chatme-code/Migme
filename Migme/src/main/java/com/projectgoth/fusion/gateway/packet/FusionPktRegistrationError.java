package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.common.CaptchaService;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;

public class FusionPktRegistrationError extends FusionPacket {
   public FusionPktRegistrationError() {
      super((short)104);
   }

   public FusionPktRegistrationError(short transactionId) {
      super((short)104, transactionId);
   }

   public FusionPktRegistrationError(FusionPacket packet) {
      super(packet);
   }

   public FusionPktRegistrationError(short transactionId, String error) {
      super((short)104, transactionId);
      this.setErrorDescription(error);
   }

   public FusionPktRegistrationError(short transactionId, String error, CaptchaService captchaService) throws IOException {
      super((short)104, transactionId);
      boolean randomizeCaptchaWordLength = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.RANDOMIZE_MIDLET_REGISTRATION_CAPTCHA_ENABLED);
      Captcha captcha;
      if (randomizeCaptchaWordLength) {
         int min = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MIN_WORD_LENGTH_FOR_RANDOMIZED_MIDLET_REGISTRATION_CAPTCHA);
         int max = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MAX_WORD_LENGTH_FOR_RANDOMIZED_MIDLET_REGISTRATION_CAPTCHA);
         int captchaWordLength = min + (int)(Math.random() * (double)(max - min + 1));
         captcha = captchaService.nextCaptcha(captchaWordLength);
      } else {
         captcha = captchaService.nextCaptcha();
      }

      this.setErrorDescription(error);
      this.setCaptchaId(captcha.getId());
      this.setCaptchaImage(captcha.getImageByteArray("png"));
   }

   public String getErrorDescription() {
      return this.getField((short)1).getStringVal();
   }

   public void setErrorDescription(String errorDescription) {
      this.setField((short)1, errorDescription);
   }

   public String getCaptchaId() {
      return this.getStringField((short)2);
   }

   public void setCaptchaId(String captchaId) {
      this.setField((short)2, captchaId);
   }

   public byte[] getCaptchaImage() {
      return this.getByteArrayField((short)3);
   }

   public void setCaptchaImage(byte[] captchaImage) {
      this.setField((short)3, captchaImage);
   }
}
