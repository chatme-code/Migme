package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.SessionPrx;

public class FusionPktSetPresenceOld extends FusionRequest {
   public FusionPktSetPresenceOld() {
      super((short)600);
   }

   public FusionPktSetPresenceOld(short transactionId) {
      super((short)600, transactionId);
   }

   public FusionPktSetPresenceOld(FusionPacket packet) {
      super(packet);
   }

   public Byte getNewPresence() {
      return this.getByteField((short)1);
   }

   public void setNewPresence(byte newPresence) {
      this.setField((short)1, newPresence);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         SessionPrx sessionPrx = connection.getSessionPrx();
         if (sessionPrx == null) {
            pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "User not logged in");
            return new FusionPacket[]{pktError};
         } else {
            sessionPrx.setPresence(this.getNewPresence());
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
