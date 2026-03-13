package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktRegistrationChallenge extends FusionPacket {
   public FusionPktRegistrationChallenge() {
      super((short)102);
   }

   public FusionPktRegistrationChallenge(short transactionId) {
      super((short)102, transactionId);
   }

   public FusionPktRegistrationChallenge(FusionPacket packet) {
      super(packet);
   }

   public String getIDDCode() {
      return this.getField((short)1).getStringVal();
   }

   public void setIDDCode(String iddCode) {
      this.setField((short)1, iddCode);
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

   public void setCaptchaImage(String captchaImage) {
      this.setField((short)3, captchaImage);
   }
}
