package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataMoveContact;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktMoveContact extends FusionPktDataMoveContact {
   public FusionPktMoveContact(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktMoveContact(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         Integer contactID = this.getContactId();
         if (contactID == null) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Contact ID not set")).toArray();
         } else if (contactID < 0) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Current operation not supported for this contact type")).toArray();
         } else {
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            contactEJB.moveContactToGroup(connection.getUserID(), connection.getUsername(), contactID, this.getGroupId());
            return (new FusionPktOk(this.transactionId)).toArray();
         }
      } catch (CreateException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to move contact - " + RMIExceptionHelper.getRootMessage(var5))).toArray();
      }
   }
}
