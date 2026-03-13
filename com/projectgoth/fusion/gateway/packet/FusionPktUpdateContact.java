package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PhoneNumberType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktUpdateContact extends FusionRequest {
   public FusionPktUpdateContact() {
      super((short)407);
   }

   public FusionPktUpdateContact(short transactionId) {
      super((short)407, transactionId);
   }

   public FusionPktUpdateContact(FusionPacket packet) {
      super(packet);
   }

   public Integer getContactID() {
      return this.getIntField((short)1);
   }

   public void setContactID(int ContactID) {
      this.setField((short)1, ContactID);
   }

   public Integer getGroupID() {
      return this.getIntField((short)2);
   }

   public void setGroupID(int GroupID) {
      this.setField((short)2, GroupID);
   }

   public String getDisplayName() {
      return this.getStringField((short)3);
   }

   public void setDisplayName(String displayName) {
      this.setField((short)3, displayName);
   }

   public String getFirstName() {
      return this.getStringField((short)4);
   }

   public void setFirstName(String firstName) {
      this.setField((short)4, firstName);
   }

   public String getLastName() {
      return this.getStringField((short)5);
   }

   public void setLastName(String lastName) {
      this.setField((short)5, lastName);
   }

   public String getEmailAddress() {
      return this.getStringField((short)6);
   }

   public void setEmailAddress(String emailAddress) {
      this.setField((short)6, emailAddress);
   }

   public Byte getDefaultPhone() {
      return this.getByteField((short)7);
   }

   public void setDefaultPhone(byte defaultPhone) {
      this.setField((short)7, defaultPhone);
   }

   public String getMobilePhone() {
      return this.getStringField((short)8);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)8, mobilePhone);
   }

   public String getHomePhone() {
      return this.getStringField((short)9);
   }

   public void setHomePhone(String homePhone) {
      this.setField((short)9, homePhone);
   }

   public String getOfficePhone() {
      return this.getStringField((short)10);
   }

   public void setOfficePhone(String officePhone) {
      this.setField((short)10, officePhone);
   }

   public Byte getDefaultIM() {
      return this.getByteField((short)11);
   }

   public void setDefaultIM(byte defaultIM) {
      this.setField((short)11, defaultIM);
   }

   public String getFusionUserame() {
      String s = this.getStringField((short)12);
      return s == null ? null : s.trim().toLowerCase();
   }

   public void setFusionUserame(String fusionUserame) {
      this.setField((short)12, fusionUserame);
   }

   public String getMSNUserame() {
      return this.getStringField((short)14);
   }

   public void setMSNUserame(String msnUserame) {
      this.setField((short)14, msnUserame);
   }

   public String getAIMUserame() {
      return this.getStringField((short)16);
   }

   public void setAIMUserame(String aimUserame) {
      this.setField((short)16, aimUserame);
   }

   public String getYahooUserame() {
      return this.getStringField((short)18);
   }

   public void setYahooUserame(String yahooUserame) {
      this.setField((short)18, yahooUserame);
   }

   public String getICQUserame() {
      return this.getStringField((short)20);
   }

   public void setICQUserame(String icqUserame) {
      this.setField((short)20, icqUserame);
   }

   public String getGTalkUserame() {
      return this.getStringField((short)22);
   }

   public void setGTalkUserame(String gtalkUserame) {
      this.setField((short)22, gtalkUserame);
   }

   public String getFacebookUsername() {
      return this.getStringField((short)29);
   }

   public void setFacebookUserame(String facebookUserame) {
      this.setField((short)29, facebookUserame);
   }

   public String[] getIMUsernames() {
      return this.getStringArrayField((short)30);
   }

   public void setIMUsernames(String[] imUsernames) {
      this.setField((short)30, imUsernames);
   }

   public byte[] getIMTypes() {
      return this.getByteArrayField((short)31);
   }

   public void setIMTypes(byte[] imTypes) {
      this.setField((short)31, imTypes);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      if (this.getContactID() < 0) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Current operation not supported for this contact type")).toArray();
      } else {
         try {
            ContactData contactData = new ContactData();
            contactData.id = this.getContactID();
            contactData.contactGroupId = this.getGroupID();
            contactData.username = connection.getUsername();
            contactData.displayName = this.getDisplayName();
            contactData.firstName = this.getFirstName();
            contactData.lastName = this.getLastName();
            contactData.fusionUsername = this.getFusionUserame();
            contactData.msnUsername = this.getMSNUserame();
            contactData.aimUsername = this.getAIMUserame();
            contactData.yahooUsername = this.getYahooUserame();
            contactData.gtalkUsername = this.getGTalkUserame();
            contactData.facebookUsername = this.getFacebookUsername();
            contactData.emailAddress = this.getEmailAddress();
            contactData.mobilePhone = this.getMobilePhone();
            contactData.homePhone = this.getHomePhone();
            contactData.officePhone = this.getOfficePhone();
            contactData.displayOnPhone = true;
            if (connection.isMidletVersionAndAbove(430) && this.getIMTypes() != null && this.getIMUsernames() != null && this.getIMTypes().length == this.getIMUsernames().length) {
               for(int i = 0; i < this.getIMTypes().length; ++i) {
                  switch(ImType.fromValue(this.getIMTypes()[i])) {
                  case MSN:
                     contactData.msnUsername = this.getIMUsernames()[i];
                     break;
                  case AIM:
                     contactData.aimUsername = this.getIMUsernames()[i];
                     break;
                  case YAHOO:
                     contactData.yahooUsername = this.getIMUsernames()[i];
                     break;
                  case GTALK:
                     contactData.gtalkUsername = this.getIMUsernames()[i];
                     break;
                  case FACEBOOK:
                     contactData.facebookUsername = this.getIMUsernames()[i];
                  }
               }
            }

            Byte byteVal = this.getDefaultIM();
            if (byteVal != null) {
               contactData.defaultIM = ImType.fromValue(byteVal);
            }

            byteVal = this.getDefaultPhone();
            if (byteVal != null) {
               contactData.defaultPhoneNumber = PhoneNumberType.fromValue(byteVal);
            }

            Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
            contactData = contactEJB.updateContactDetail(connection.getUserID(), contactData);
            return (new FusionPktContactOld(this.transactionId, contactData, connection)).toArray();
         } catch (CreateException var5) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
         } catch (RemoteException var6) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to update contact detail - " + RMIExceptionHelper.getRootMessage(var6))).toArray();
         }
      }
   }
}
