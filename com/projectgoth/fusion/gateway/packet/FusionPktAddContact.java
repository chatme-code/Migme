package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PhoneNumberType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.springframework.util.StringUtils;

public class FusionPktAddContact extends FusionRequest {
   public FusionPktAddContact() {
      super((short)405);
   }

   public FusionPktAddContact(short transactionId) {
      super((short)405, transactionId);
   }

   public FusionPktAddContact(FusionPacket packet) {
      super(packet);
   }

   public Integer getGroupID() {
      return this.getIntField((short)2);
   }

   public void setGroupId(int GroupID) {
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

   public String getFusionUsername() {
      String s = this.getStringField((short)12);
      return s == null ? null : s.trim().toLowerCase();
   }

   public void setFusionUsername(String fusionUsername) {
      this.setField((short)12, fusionUsername);
   }

   public String getMSNUsername() {
      return this.getStringField((short)14);
   }

   public void setMSNUsername(String msnUsername) {
      this.setField((short)14, msnUsername);
   }

   public String getAIMUsername() {
      return this.getStringField((short)16);
   }

   public void setAIMUsername(String aimUsername) {
      this.setField((short)16, aimUsername);
   }

   public String getYahooUsername() {
      return this.getStringField((short)18);
   }

   public void setYahooUsername(String yahooUsername) {
      this.setField((short)18, yahooUsername);
   }

   public String getICQUsername() {
      return this.getStringField((short)20);
   }

   public void setICQUsername(String icqUsername) {
      this.setField((short)20, icqUsername);
   }

   public String getGTalkUsername() {
      return this.getStringField((short)22);
   }

   public void setGTalkUsername(String gtalkUsername) {
      this.setField((short)22, gtalkUsername);
   }

   public Byte getShareMobilePhone() {
      return this.getByteField((short)24);
   }

   public void setShareMobilePhone(byte shareMobilePhone) {
      this.setField((short)24, shareMobilePhone);
   }

   public String getFacebookUsername() {
      return this.getStringField((short)29);
   }

   public void setFacebookUsername(String facebookUsername) {
      this.setField((short)29, facebookUsername);
   }

   public String getIMUsername() {
      return this.getStringField((short)30);
   }

   public void setIMUsername(String imUsername) {
      this.setField((short)30, imUsername);
   }

   public Byte getIMType() {
      return this.getByteField((short)31);
   }

   public void setIMType(byte imType) {
      this.setField((short)31, imType);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ContactData contactData = new ContactData();
         contactData.username = connection.getUsername();
         contactData.displayName = this.getDisplayName();
         contactData.firstName = this.getFirstName();
         contactData.lastName = this.getLastName();
         contactData.fusionUsername = this.getFusionUsername();
         contactData.msnUsername = this.getMSNUsername();
         contactData.aimUsername = this.getAIMUsername();
         contactData.yahooUsername = this.getYahooUsername();
         contactData.gtalkUsername = this.getGTalkUsername();
         contactData.facebookUsername = this.getFacebookUsername();
         contactData.emailAddress = this.getEmailAddress();
         contactData.mobilePhone = this.getMobilePhone();
         contactData.homePhone = this.getHomePhone();
         contactData.officePhone = this.getOfficePhone();
         contactData.contactGroupId = this.getGroupID();
         if (connection.isMidletVersionAndAbove(430) && this.getIMType() != null && this.getIMUsername() != null) {
            switch(ImType.fromValue(this.getIMType())) {
            case MSN:
               contactData.msnUsername = this.getIMUsername();
               break;
            case AIM:
               contactData.aimUsername = this.getIMUsername();
               break;
            case YAHOO:
               contactData.yahooUsername = this.getIMUsername();
               break;
            case GTALK:
               contactData.gtalkUsername = this.getIMUsername();
               break;
            case FACEBOOK:
               contactData.facebookUsername = this.getIMUsername();
            }
         }

         contactData.displayOnPhone = true;
         Byte byteVal = this.getDefaultIM();
         if (byteVal != null) {
            contactData.defaultIM = ImType.fromValue(byteVal);
         }

         byteVal = this.getDefaultPhone();
         if (byteVal != null) {
            contactData.defaultPhoneNumber = PhoneNumberType.fromValue(byteVal);
         }

         byteVal = this.getShareMobilePhone();
         if (byteVal != null) {
            contactData.shareMobilePhone = byteVal.intValue() == 1;
         }

         UserPrx userPrx = connection.getUserPrx();
         if (contactData.msnUsername != null) {
            userPrx.otherIMAddContact(ImType.MSN.value(), contactData.msnUsername);
            return (new FusionPktOk(this.transactionId)).toArray();
         } else if (contactData.yahooUsername != null) {
            userPrx.otherIMAddContact(ImType.YAHOO.value(), contactData.yahooUsername);
            return (new FusionPktOk(this.transactionId)).toArray();
         } else if (contactData.aimUsername != null) {
            userPrx.otherIMAddContact(ImType.AIM.value(), contactData.aimUsername);
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         } else if (contactData.gtalkUsername != null) {
            userPrx.otherIMAddContact(ImType.GTALK.value(), contactData.gtalkUsername);
            return (new FusionPktOk(this.transactionId)).toArray();
         } else if (contactData.facebookUsername != null) {
            userPrx.otherIMAddContact(ImType.FACEBOOK.value(), contactData.facebookUsername);
            return (new FusionPktOk(this.transactionId)).toArray();
         } else {
            Contact contactEJB;
            if (!StringUtils.hasLength(contactData.fusionUsername) && StringUtils.hasLength(contactData.mobilePhone)) {
               contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               contactData = contactEJB.addPhoneContact(connection.getUserID(), contactData);
               return (new FusionPktContactOld(this.transactionId, contactData, connection)).toArray();
            } else {
               contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.FOLLOW_ON_ADD_CONTACT_ENABLED)) {
                  boolean followOnMiniblog = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.MIGBO_INTEGRATION_ENABLED);
                  contactData = contactEJB.addFusionUserAsContact(connection.getUserID(), contactData, followOnMiniblog);
               } else {
                  contactData = contactEJB.addPendingFusionContact(connection.getUserID(), contactData);
               }

               connection.getSessionPrx().friendInvitedByUsername();
               return (new FusionPktContactOld(this.transactionId, contactData, connection)).toArray();
            }
         }
      } catch (CreateException var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create ContactEJB")).toArray();
      } catch (RemoteException var8) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Add contact failed - " + RMIExceptionHelper.getRootMessage(var8))).toArray();
      } catch (FusionEJBException var9) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Add contact failed - " + var9.getMessage())).toArray();
      } catch (FusionException var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Add contact failed - " + var10.message)).toArray();
      } catch (LocalException var11) {
         return (new FusionPktInternalServerError(this.transactionId, var11, "Add contact failed")).toArray();
      } catch (Exception var12) {
         return (new FusionPktInternalServerError(this.transactionId, var12, "Add contact failed")).toArray();
      }
   }
}
