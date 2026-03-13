package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktGetHelpTextOld extends FusionRequest {
   public FusionPktGetHelpTextOld() {
      super((short)7);
   }

   public FusionPktGetHelpTextOld(short transactionId) {
      super((short)7, transactionId);
   }

   public FusionPktGetHelpTextOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getId() {
      return this.getIntField((short)1);
   }

   public void setId(int id) {
      this.setField((short)1, id);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         String text = misEJB.getHelpText(this.getId());
         if (text == null) {
            throw new Exception("Invalid help ID " + this.getId());
         } else {
            FusionPktHelpTextOld pkt = new FusionPktHelpTextOld(this.transactionId);
            pkt.setText(text);
            return new FusionPacket[]{pkt};
         }
      } catch (CreateException var5) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get help text - " + var5.getMessage());
         return new FusionPacket[]{pktError};
      } catch (RemoteException var6) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get help text - " + RMIExceptionHelper.getRootMessage(var6));
         return new FusionPacket[]{pktError};
      } catch (Exception var7) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get help text - " + var7.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
