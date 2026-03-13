package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataMuteChatroomParticipant;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktMuteChatroomParticipant extends FusionPktDataMuteChatroomParticipant {
   public FusionPktMuteChatroomParticipant(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktMuteChatroomParticipant(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomNameForRateLimit() {
      return this.getChatroomName();
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatroomName();
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
