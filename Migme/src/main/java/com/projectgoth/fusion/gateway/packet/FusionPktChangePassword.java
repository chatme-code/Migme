package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.fdl.packets.FusionPktDataChangePassword;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktChangePassword extends FusionPktDataChangePassword {
   public FusionPktChangePassword(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktChangePassword(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      String forgotPasswordNoSupportMsg = SystemProperty.get("ForgotPasswordNoSupportMsg", "Please go to http://m.mig.me/forgot-password to reset your password.");
      return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, forgotPasswordNoSupportMsg)};
   }
}
