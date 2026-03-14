package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.exceptions.FusionRequestException;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;

public class FusionPktCaptchaResponseOld extends FusionRequest {
   public FusionPktCaptchaResponseOld() {
      super((short)17);
   }

   public FusionPktCaptchaResponseOld(short transactionId) {
      super((short)17, transactionId);
   }

   public FusionPktCaptchaResponseOld(FusionPacket packet) {
      super(packet);
   }

   public String getCaptchaResponse() {
      return this.getStringField((short)1);
   }

   public void setCaptchaResponse(String captchaResponse) {
      this.setField((short)1, captchaResponse);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         FusionRequest request = connection.validateCaptchaResponse(this.getCaptchaResponse());
         if (request != null) {
            request.setSkipCaptchaCheck(true);
            return request.process(connection);
         } else {
            Captcha captcha = connection.getDeviceType() != null && connection.isMidletVersionBelow(430) ? connection.updateCaptcha() : connection.updateCaptcha(4);
            return (new FusionPktCaptchaOld(this.transactionId, "The letters you entered were incorrect. Please try again.", captcha, connection)).toArray();
         }
      } catch (IOException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to create Captcha")).toArray();
      } catch (FusionRequestException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to create Captcha - " + var5.getMessage())).toArray();
      }
   }
}
