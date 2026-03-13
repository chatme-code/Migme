package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktAcceptContactRequestOld extends FusionRequest {
   public FusionPktAcceptContactRequestOld() {
      super((short)413);
   }

   public FusionPktAcceptContactRequestOld(short transactionId) {
      super((short)413, transactionId);
   }

   public FusionPktAcceptContactRequestOld(FusionPacket packet) {
      super(packet);
   }

   public String getContactUserName() {
      return this.getStringField((short)1);
   }

   public void setContactUserName(String contactUserName) {
      this.setField((short)1, contactUserName);
   }

   public Byte getAddContact() {
      return this.getByteField((short)2);
   }

   public void setAddContact(byte addContact) {
      this.setField((short)2, addContact);
   }

   public Integer getGroupID() {
      return this.getIntField((short)3);
   }

   public void setGroupID(int GroupID) {
      this.setField((short)3, GroupID);
   }

   public Byte getShareMobilePhone() {
      return this.getByteField((short)4);
   }

   public void setShareMobilePhone(byte shareMobilePhone) {
      this.setField((short)4, shareMobilePhone);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String contactUsername = this.getContactUserName();
         if (contactUsername == null) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Contact username not set")).toArray();
         } else {
            Byte shareMobilePhone = this.getShareMobilePhone();
            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            ContactData contactData = new ContactData();
            contactData.username = connection.getUsername();
            contactData.fusionUsername = contactUsername;
            contactData.contactGroupId = this.getGroupID();
            contactData.displayOnPhone = true;
            if (shareMobilePhone != null) {
               contactData.shareMobilePhone = shareMobilePhone.intValue() == 1;
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.FOLLOW_ON_ADD_CONTACT_ENABLED)) {
               boolean followOnMiniblog = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.MIGBO_INTEGRATION_ENABLED);
               contactData = contactEJB.addFusionUserAsContact(connection.getUserID(), contactData, followOnMiniblog);
            } else {
               contactData = contactEJB.acceptContactRequest(connection.getUserID(), contactData, false);
            }

            return (new FusionPktContactOld(this.transactionId, contactData, connection)).toArray();
         }
      } catch (CreateException var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var8) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to accept user - " + RMIExceptionHelper.getRootMessage(var8))).toArray();
      } catch (FusionEJBException var9) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to accept user - " + var9.getMessage())).toArray();
      }
   }
}
