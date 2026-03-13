package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.CaptchaService;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktRegistrationResponse extends FusionRequest {
   public FusionPktRegistrationResponse() {
      super((short)103);
   }

   public FusionPktRegistrationResponse(short transactionId) {
      super((short)103, transactionId);
   }

   public FusionPktRegistrationResponse(FusionPacket packet) {
      super(packet);
   }

   public String getUsername() {
      String s = this.getStringField((short)1);
      return s == null ? null : s.trim().toLowerCase();
   }

   public void setUsername(String username) {
      this.setField((short)1, username);
   }

   public String getPassword() {
      return this.getStringField((short)2);
   }

   public void setPassword(String password) {
      this.setField((short)2, password);
   }

   public String getMobilePhone() {
      return this.getStringField((short)3);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)3, mobilePhone);
   }

   public String getCaptchaID() {
      return this.getStringField((short)4);
   }

   public void setCaptchaID(String captchaID) {
      this.setField((short)4, captchaID);
   }

   public String getCaptchaResponse() {
      return this.getStringField((short)5);
   }

   public void setCaptchaResponse(String captchaResponse) {
      this.setField((short)5, captchaResponse);
   }

   public Integer getGroupID() {
      return this.getIntField((short)6);
   }

   public void setGroupID(int groupID) {
      this.setField((short)6, groupID);
   }

   public String getDateOfBirth() {
      return this.getStringField((short)8);
   }

   public void setDateOfBirth(String dateOfBirth) {
      this.setField((short)8, dateOfBirth);
   }

   public String getUserAgent() {
      return this.getStringField((short)9);
   }

   public void setUserAgent(String userAgent) {
      this.setField((short)9, userAgent);
   }

   public String getMobileDevice() {
      return this.getStringField((short)10);
   }

   public void setMobileDevice(String mobileDevice) {
      this.setField((short)10, mobileDevice);
   }

   public String getIMEI() {
      return this.getStringField((short)11);
   }

   public void setIMEI(String imei) {
      this.setField((short)11, imei);
   }

   public boolean sessionRequired() {
      return false;
   }

   public String getPacketUnsupportedMessage() {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Registration.MOBILE_REGISTRATION_DISABLED_MESSAGE);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         CaptchaService captchaService = connection.getGatewayContext().getCaptchaService();
         synchronized(connection) {
            if (!captchaService.validateResponse(this.getCaptchaID(), this.getCaptchaResponse())) {
               return (new FusionPktRegistrationError(this.transactionId, "Please enter the letters shown in the image", captchaService)).toArray();
            }
         }

         FusionPktNewRegistration newRegistrationPkt = new FusionPktNewRegistration(this.transactionId);
         newRegistrationPkt.setServiceType((byte)1);
         newRegistrationPkt.setUsername(this.getUsername());
         newRegistrationPkt.setPassword(this.getPassword());
         newRegistrationPkt.setMobilePhone(this.getMobilePhone());
         Integer groupID = this.getGroupID();
         if (groupID != null) {
            newRegistrationPkt.setGroupID(groupID);
         }

         String dob = this.getDateOfBirth();
         if (dob != null) {
            newRegistrationPkt.setDateOfBirth(dob);
         }

         String mobileDevice = this.getMobileDevice();
         if (mobileDevice != null) {
            mobileDevice = DataUtils.truncateMobileDevice(mobileDevice, true, String.format("new user registration '%s'", this.getUsername()));
            newRegistrationPkt.setMobileDevice(mobileDevice);
         }

         String userAgent = this.getUserAgent();
         if (userAgent != null) {
            userAgent = DataUtils.truncateUserAgent(userAgent, true, String.format("new user registration '%s'", this.getUsername()));
            newRegistrationPkt.setUserAgent(userAgent);
         }

         String imei = this.getIMEI();
         if (imei != null) {
            newRegistrationPkt.setIMEI(imei);
         }

         FusionPacket[] returnPkts = newRegistrationPkt.processRequest(connection, true);

         for(int i = 0; i < returnPkts.length; ++i) {
            FusionPacket pkt = returnPkts[i];
            if (pkt.getType() == 0 && pkt.getTransactionId() == this.transactionId) {
               returnPkts[i] = new FusionPktRegistrationError(this.transactionId, pkt.getStringField((short)2), captchaService);
            }
         }

         return returnPkts;
      } catch (Exception var13) {
         return (new FusionPktRegistrationError(this.transactionId, "migme registration is temporarily unavailable. Please go to wap.mig.me to register")).toArray();
      }
   }
}
