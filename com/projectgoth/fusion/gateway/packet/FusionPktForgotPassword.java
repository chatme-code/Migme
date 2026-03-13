package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktForgotPassword extends FusionRequest {
   public FusionPktForgotPassword() {
      super((short)904);
   }

   public FusionPktForgotPassword(short transactionId) {
      super((short)904, transactionId);
   }

   public FusionPktForgotPassword(FusionPacket packet) {
      super(packet);
   }

   public String getUsername() {
      return this.getStringField((short)1);
   }

   public void setUsername(String username) {
      this.setField((short)1, username);
   }

   public String getMobilePhone() {
      return this.getStringField((short)2);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)2, mobilePhone);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      String forgotPasswordNoSupportMsg = SystemProperty.get("ForgotPasswordNoSupportMsg", "Please go to http://m.mig.me/forgot-password to reset your password.");
      return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, forgotPasswordNoSupportMsg)};
   }
}
