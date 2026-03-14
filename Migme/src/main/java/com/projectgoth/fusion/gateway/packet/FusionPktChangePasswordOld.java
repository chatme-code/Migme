package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;

public class FusionPktChangePasswordOld extends FusionRequest {
   public FusionPktChangePasswordOld() {
      super((short)908);
   }

   public FusionPktChangePasswordOld(short transactionId) {
      super((short)908, transactionId);
   }

   public FusionPktChangePasswordOld(FusionPacket packet) {
      super(packet);
   }

   public String getOldPassword() {
      return this.getStringField((short)1);
   }

   public void setOldPassword(String oldPassword) {
      this.setField((short)1, oldPassword);
   }

   public String getNewPassword() {
      return this.getStringField((short)2);
   }

   public void setNewPassword(String newPassword) {
      this.setField((short)2, newPassword);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      String forgotPasswordNoSupportMsg = SystemProperty.get("ForgotPasswordNoSupportMsg", "Please go to http://m.mig.me/forgot-password to reset your password.");
      return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, forgotPasswordNoSupportMsg)};
   }
}
