package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktMoveContactOld extends FusionRequest {
   public FusionPktMoveContactOld() {
      super((short)411);
   }

   public FusionPktMoveContactOld(short transactionId) {
      super((short)411, transactionId);
   }

   public FusionPktMoveContactOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getContactID() {
      return this.getIntField((short)1);
   }

   public void setContactID(int contactID) {
      this.setField((short)1, contactID);
   }

   public Integer getGroupID() {
      return this.getIntField((short)2);
   }

   public void setGroupID(int groupID) {
      this.setField((short)2, groupID);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         Integer contactID = this.getContactID();
         if (contactID == null) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Contact ID not set")).toArray();
         } else if (contactID < 0) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Current operation not supported for this contact type")).toArray();
         } else {
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            contactEJB.moveContactToGroup(connection.getUserID(), connection.getUsername(), contactID, this.getGroupID());
            return (new FusionPktOk(this.transactionId)).toArray();
         }
      } catch (CreateException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to move contact - " + RMIExceptionHelper.getRootMessage(var5))).toArray();
      }
   }
}
