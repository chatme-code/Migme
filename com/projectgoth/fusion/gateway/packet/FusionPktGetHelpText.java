package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetHelpText;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktGetHelpText extends FusionPktDataGetHelpText {
   public FusionPktGetHelpText(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetHelpText(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
         String text = misEJB.getHelpText(this.getHelpTextId());
         if (text == null) {
            throw new Exception("Invalid help ID " + this.getHelpTextId());
         } else {
            FusionPktHelpText pkt = new FusionPktHelpText(this.transactionId);
            pkt.setHelpText(text);
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
