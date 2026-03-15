package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.fdl.packets.FusionPktDataSetPresence;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.SessionPrx;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktSetPresence extends FusionPktDataSetPresence {
   public FusionPktSetPresence(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktSetPresence(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         SessionPrx sessionPrx = connection.getSessionPrx();
         if (sessionPrx == null) {
            pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "User not logged in");
            return new FusionPacket[]{pktError};
         } else {
            sessionPrx.setPresence(this.getPresence().value());
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         }
      } catch (LocalException var4) {
         return (new FusionPktInternalServerError(this.transactionId, var4, "Failed to set presence")).toArray();
      } catch (Exception var5) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to set presence - " + var5.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
