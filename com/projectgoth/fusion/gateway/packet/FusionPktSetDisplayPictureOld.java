package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktSetDisplayPictureOld extends FusionRequest {
   public FusionPktSetDisplayPictureOld() {
      super((short)602);
   }

   public FusionPktSetDisplayPictureOld(short transactionId) {
      super((short)602, transactionId);
   }

   public FusionPktSetDisplayPictureOld(FusionPacket packet) {
      super(packet);
   }

   public String getDisplayPicture() {
      return this.getStringField((short)1);
   }

   public void setDisplayPicture(String displayPicture) {
      this.setField((short)1, displayPicture);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         userEJB.updateDisplayPicture(connection.getUsername(), this.getDisplayPicture());
         return new FusionPacket[]{new FusionPktOk(this.transactionId)};
      } catch (CreateException var4) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set display picture - Failed to create UserEJB");
         return new FusionPacket[]{pktError};
      } catch (RemoteException var5) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set display picture - " + RMIExceptionHelper.getRootMessage(var5));
         return new FusionPacket[]{pktError};
      } catch (Exception var6) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set display picture - " + var6.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
