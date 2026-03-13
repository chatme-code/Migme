package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataSetDisplayPicture;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktSetDisplayPicture extends FusionPktDataSetDisplayPicture {
   public FusionPktSetDisplayPicture(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktSetDisplayPicture(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         userEJB.updateDisplayPicture(connection.getUsername(), this.getDisplayPictureGuid());
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
