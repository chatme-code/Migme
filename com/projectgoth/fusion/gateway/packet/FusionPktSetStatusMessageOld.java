package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktSetStatusMessageOld extends FusionRequest {
   public FusionPktSetStatusMessageOld() {
      super((short)601);
   }

   public FusionPktSetStatusMessageOld(short transactionId) {
      super((short)601, transactionId);
   }

   public FusionPktSetStatusMessageOld(FusionPacket packet) {
      super(packet);
   }

   public String getStatusMessage() {
      return this.getStringField((short)1);
   }

   public void setStatusMessage(String statusMessage) {
      this.setField((short)1, statusMessage);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), FloodControl.Action.SET_STATUS);
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         userEJB.updateStatusMessage(connection.getUserID(), connection.getUsername(), this.getStatusMessage(), connection.getDeviceType(), (SSOEnums.View)null);
         connection.getSessionPrx().statusMessageSet();
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (CreateException var3) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set status message - Failed to create UserEJB")).toArray();
      } catch (RemoteException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set status message - " + RMIExceptionHelper.getRootMessage(var4))).toArray();
      } catch (Exception var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set status message - " + var5.getMessage())).toArray();
      }
   }
}
