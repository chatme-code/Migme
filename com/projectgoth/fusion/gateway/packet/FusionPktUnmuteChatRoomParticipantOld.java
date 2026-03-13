package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktUnmuteChatRoomParticipantOld extends FusionRequest {
   public FusionPktUnmuteChatRoomParticipantOld() {
      super((short)710);
   }

   public FusionPktUnmuteChatRoomParticipantOld(short transactionId) {
      super((short)710, transactionId);
   }

   public FusionPktUnmuteChatRoomParticipantOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   public String getUsername() {
      return this.getStringField((short)2);
   }

   public void setUsername(String username) {
      this.setField((short)2, username);
   }

   public boolean sessionRequired() {
      return true;
   }

   public String getChatRoomNameForRateLimit() {
      return this.getChatRoomName();
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         String chatRoomName = this.getChatRoomName();
         if (chatRoomName == null) {
            throw new Exception("You must specify a chat room name");
         } else {
            String userToUnmute = this.getUsername();
            if (userToUnmute == null) {
               throw new Exception("You must specify a user to unmute");
            } else {
               Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               contactEJB.unblockContact(connection.getUsername(), userToUnmute, false);
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               FusionPktOk pkt = new FusionPktOk(this.transactionId);
               pkt.setServerResponse(misEJB.getInfoText(39).replaceAll("%u", userToUnmute));
               return new FusionPacket[]{pkt};
            }
         }
      } catch (CreateException var7) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to unmute user - Failed to create ContactEJB");
         return new FusionPacket[]{pktError};
      } catch (RemoteException var8) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to unmute user - " + RMIExceptionHelper.getRootMessage(var8));
         return new FusionPacket[]{pktError};
      } catch (Exception var9) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to unmute user - " + var9.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
