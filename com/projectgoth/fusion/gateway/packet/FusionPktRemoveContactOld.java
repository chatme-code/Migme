package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktRemoveContactOld extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktRemoveContactOld.class));

   public FusionPktRemoveContactOld() {
      super((short)406);
   }

   public FusionPktRemoveContactOld(short transactionId) {
      super((short)406, transactionId);
   }

   public FusionPktRemoveContactOld(FusionPacket packet) {
      super(packet);
   }

   public Integer getContactID() {
      return this.getIntField((short)1);
   }

   public void setContactID(int contactID) {
      this.setField((short)1, contactID);
   }

   public boolean sessionRequired() {
      return true;
   }

   private void removeIMEndPoint(ConnectionI connection, int contactId) throws FusionException {
      try {
         connection.getUserPrx().otherIMRemoveContact(contactId);
      } catch (FusionException var4) {
         log.debug("Failed to remove contact[" + contactId + "] user from " + connection.getUsername() + "'s contact list - " + var4.message);
         throw var4;
      } catch (LocalException var5) {
         log.debug("Failed to remove contact[" + contactId + "] user from " + connection.getUsername() + "'s contact list - " + var5.getLocalizedMessage());
      } catch (Exception var6) {
         log.debug("Failed to remove contact[" + contactId + "] user from " + connection.getUsername() + "'s contact list - " + var6.getLocalizedMessage());
      }

   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         Integer contactID = this.getContactID();
         if (contactID == null) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Contact ID not set")).toArray();
         } else {
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.UNFOLLOW_ON_REMOVE_CONTACT_ENABLED)) {
               boolean unfollowContactOnMiniblog = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.MIGBO_INTEGRATION_ENABLED);
               contactEJB.removeFusionUserFromContact(connection.getUserID(), connection.getUsername(), contactID, unfollowContactOnMiniblog);
            } else {
               contactEJB.removeContact(connection.getUserID(), connection.getUsername(), contactID);
            }

            this.removeIMEndPoint(connection, contactID);
            return (new FusionPktOk(this.transactionId)).toArray();
         }
      } catch (CreateException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var6) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to remove contact - " + RMIExceptionHelper.getRootMessage(var6))).toArray();
      } catch (FusionEJBException var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to remove contact - " + var7.getMessage())).toArray();
      } catch (FusionException var8) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var8.message)).toArray();
      } catch (Exception var9) {
         return (new FusionPktInternalServerError(this.transactionId, var9, "Failed to remove contact")).toArray();
      }
   }
}
