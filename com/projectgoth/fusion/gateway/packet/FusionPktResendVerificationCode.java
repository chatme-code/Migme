package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktResendVerificationCode extends FusionRequest {
   public FusionPktResendVerificationCode() {
      super((short)205);
   }

   public FusionPktResendVerificationCode(short transactionId) {
      super((short)205, transactionId);
   }

   public FusionPktResendVerificationCode(FusionPacket packet) {
      super(packet);
   }

   public String getMobilePhone() {
      return this.getStringField((short)1);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)1, mobilePhone);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         userEJB.resendVerificationCode(connection.getUsername(), this.getMobilePhone(), new AccountEntrySourceData(connection));
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (CreateException var3) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create UserEJB")).toArray();
      } catch (RemoteException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to resend activation code - " + RMIExceptionHelper.getRootMessage(var4))).toArray();
      }
   }
}
