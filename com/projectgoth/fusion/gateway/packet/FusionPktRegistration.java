package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Captcha;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.WebCommon;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktRegistration extends FusionRequest {
   public FusionPktRegistration() {
      super((short)101);
   }

   public FusionPktRegistration(short transactionId) {
      super((short)101, transactionId);
   }

   public FusionPktRegistration(FusionPacket packet) {
      super(packet);
   }

   public Integer getScreenWidth() {
      return this.getIntField((short)1);
   }

   public void setScreenWidth(int screenWidth) {
      this.setField((short)1, screenWidth);
   }

   public Integer getScreenHeight() {
      return this.getIntField((short)2);
   }

   public void setScreenHeight(int screenHeight) {
      this.setField((short)2, screenHeight);
   }

   public Byte getClientType() {
      return this.getByteField((short)3);
   }

   public void setClientType(byte clientType) {
      this.setField((short)3, clientType);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         FusionPktRegistrationChallenge challengePkt = new FusionPktRegistrationChallenge(this.transactionId);
         MIS misBean = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         CountryData countryData = misBean.getCountryFromIPNumber(WebCommon.toIPNumber(connection.getRemoteAddress()));
         if (countryData != null) {
            challengePkt.setIDDCode(countryData.iddCode.toString());
         }

         boolean randomizeCaptchaWordLength = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.RANDOMIZE_MIDLET_REGISTRATION_CAPTCHA_ENABLED);
         Captcha captcha;
         if (randomizeCaptchaWordLength) {
            int min = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MIN_WORD_LENGTH_FOR_RANDOMIZED_MIDLET_REGISTRATION_CAPTCHA);
            int max = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MAX_WORD_LENGTH_FOR_RANDOMIZED_MIDLET_REGISTRATION_CAPTCHA);
            int captchaWordLength = min + (int)(Math.random() * (double)(max - min + 1));
            captcha = connection.getGatewayContext().getCaptchaService().nextCaptcha(captchaWordLength);
         } else {
            captcha = connection.getGatewayContext().getCaptchaService().nextCaptcha();
         }

         challengePkt.setCaptchaId(captcha.getId());
         Byte clientType = this.getClientType();
         ClientType device = ClientType.fromValue(clientType == null ? 0 : clientType);
         if (device != null && ConnectionI.checkBrowserDevice(device)) {
            challengePkt.setCaptchaImage(captcha.getBase64EncodedString("png"));
         } else {
            challengePkt.setCaptchaImage(captcha.getImageByteArray("png"));
         }

         return new FusionPacket[]{challengePkt};
      } catch (CreateException var10) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "New registration failed - Failed to create MIS EJB");
         return new FusionPacket[]{pktError};
      } catch (RemoteException var11) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "New registration failed - " + var11.getClass().getName() + ":" + RMIExceptionHelper.getRootMessage(var11));
         return new FusionPacket[]{pktError};
      } catch (Exception var12) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "New registration failed - " + var12.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
