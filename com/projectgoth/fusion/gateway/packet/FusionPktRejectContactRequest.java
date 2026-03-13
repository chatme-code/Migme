package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktRejectContactRequest extends FusionRequest {
   public FusionPktRejectContactRequest() {
      super((short)414);
   }

   public FusionPktRejectContactRequest(short transactionId) {
      super((short)414, transactionId);
   }

   public FusionPktRejectContactRequest(FusionPacket packet) {
      super(packet);
   }

   public String getContactUserName() {
      return this.getStringField((short)1);
   }

   public void setContactUserName(String contactUserName) {
      this.setField((short)1, contactUserName);
   }

   public Byte getBlcokUser() {
      return this.getByteField((short)2);
   }

   public void setBlockUser(byte blockUser) {
      this.setField((short)2, blockUser);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String contactUsername = this.getContactUserName();
         if (contactUsername == null) {
            FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Contact username not set");
            return new FusionPacket[]{pktError};
         } else {
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            contactEJB.rejectContactRequest(connection.getUserID(), connection.getUsername(), contactUsername);
            Byte blockUser = this.getBlcokUser();
            if (blockUser != null && blockUser == 1) {
               contactEJB.blockContact(connection.getUserID(), connection.getUsername(), contactUsername);
            }

            return (new FusionPktOk(this.transactionId)).toArray();
         }
      } catch (CreateException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var6) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to reject user - " + RMIExceptionHelper.getRootMessage(var6))).toArray();
      }
   }
}
