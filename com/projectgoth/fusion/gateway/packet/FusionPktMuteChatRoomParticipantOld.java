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

public class FusionPktMuteChatRoomParticipantOld extends FusionRequest {
   public FusionPktMuteChatRoomParticipantOld() {
      super((short)709);
   }

   public FusionPktMuteChatRoomParticipantOld(short transactionId) {
      super((short)709, transactionId);
   }

   public FusionPktMuteChatRoomParticipantOld(FusionPacket packet) {
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
      try {
         String chatRoomName = this.getChatRoomName();
         if (chatRoomName == null) {
            throw new Exception("You must specify a chat room name");
         } else {
            String userToMute = this.getUsername();
            if (userToMute == null) {
               throw new Exception("You must specify a user to mute");
            } else {
               Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               contactEJB.blockContact(connection.getUserID(), connection.getUsername(), userToMute);
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               FusionPktOk pkt = new FusionPktOk(this.transactionId);
               pkt.setServerResponse(misEJB.getInfoText(38).replaceAll("%u", userToMute));
               return pkt.toArray();
            }
         }
      } catch (CreateException var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to mute user - Failed to create ContactEJB")).toArray();
      } catch (RemoteException var8) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to mute user - " + RMIExceptionHelper.getRootMessage(var8))).toArray();
      } catch (Exception var9) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to mute user - " + var9.getMessage())).toArray();
      }
   }
}
