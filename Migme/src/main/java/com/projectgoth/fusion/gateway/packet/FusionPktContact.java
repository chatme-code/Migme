package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataContact;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.ArrayList;
import java.util.List;

public class FusionPktContact extends FusionPktDataContact {
   public FusionPktContact(short transactionId) {
      super(transactionId);
   }

   public FusionPktContact(FusionPacket packet) {
      super(packet);
   }

   public FusionPktContact(ContactData contactData, ConnectionI connection) {
      this((short)0, contactData, connection);
   }

   public FusionPktContact(short transactionId, ContactData contactData, ConnectionI connection) {
      super(transactionId);
      if (contactData.id != null) {
         this.setContactId(contactData.id);
      }

      if (contactData.contactGroupId != null) {
         this.setContactGroupId(contactData.contactGroupId);
      }

      if (contactData.displayName != null) {
         this.setDisplayName(contactData.displayName);
      }

      if (contactData.firstName != null) {
         this.setFirstName(contactData.firstName);
      }

      if (contactData.lastName != null) {
         this.setLastName(contactData.lastName);
      }

      if (contactData.emailAddress != null) {
         this.setEmailAddress(contactData.emailAddress);
      }

      if (contactData.defaultPhoneNumber != null) {
         this.setDefaultPhone(contactData.defaultPhoneNumber);
      }

      if (contactData.mobilePhone != null) {
         this.setMobilePhoneNumber(contactData.mobilePhone);
      }

      if (contactData.homePhone != null) {
         this.setHomePhoneNumber(contactData.homePhone);
      }

      if (contactData.officePhone != null) {
         this.setOfficePhoneNumber(contactData.officePhone);
      }

      if (contactData.defaultIM != null) {
         this.setDefaultIm(contactData.defaultIM);
      }

      if (contactData.fusionUsername != null) {
         this.setFusionUsername(contactData.fusionUsername);
      }

      if (contactData.fusionPresence != null) {
         this.setFusionPresence(contactData.fusionPresence);
      }

      if (connection.isMidletVersionAndAbove(430)) {
         int MAXIMUM_LIST_SIZE = true;
         List<ImType> imTypeList = new ArrayList(5);
         List<PresenceType> presenceList = new ArrayList(5);
         List<String> usernameList = new ArrayList(5);
         if (contactData.msnUsername != null) {
            imTypeList.add(ImType.MSN);
            presenceList.add(contactData.msnPresence == null ? PresenceType.OFFLINE : contactData.msnPresence);
            usernameList.add(contactData.msnUsername);
         }

         if (contactData.yahooUsername != null) {
            imTypeList.add(ImType.YAHOO);
            presenceList.add(contactData.yahooPresence == null ? PresenceType.OFFLINE : contactData.yahooPresence);
            usernameList.add(contactData.yahooUsername);
         }

         if (contactData.gtalkUsername != null) {
            imTypeList.add(ImType.GTALK);
            presenceList.add(contactData.gtalkPresence == null ? PresenceType.OFFLINE : contactData.gtalkPresence);
            usernameList.add(contactData.gtalkUsername);
         }

         if (contactData.facebookUsername != null) {
            imTypeList.add(ImType.FACEBOOK);
            presenceList.add(contactData.facebookPresence == null ? PresenceType.OFFLINE : contactData.facebookPresence);
            usernameList.add(contactData.facebookUsername);
         }

         this.setImTypeList((ImType[])imTypeList.toArray(new ImType[imTypeList.size()]));
         this.setImPresenceList((PresenceType[])presenceList.toArray(new PresenceType[presenceList.size()]));
         this.setImUsernameList((String[])usernameList.toArray(new String[usernameList.size()]));
      } else {
         if (contactData.msnUsername != null) {
            this.setMsnUsername(contactData.msnUsername);
         }

         if (contactData.yahooUsername != null) {
            this.setYahooUsername(contactData.yahooUsername);
         }

         if (contactData.gtalkUsername != null) {
            this.setGtalkUsername(contactData.gtalkUsername);
         }

         if (contactData.facebookUsername != null) {
            this.setFacebookUsername(contactData.facebookUsername);
         }

         if (contactData.msnPresence != null) {
            this.setMsnPresence(contactData.msnPresence);
         }

         if (contactData.yahooPresence != null) {
            this.setYahooPresence(contactData.yahooPresence);
         }

         if (contactData.gtalkPresence != null) {
            this.setGtalkPresence(contactData.gtalkPresence);
         }

         if (contactData.facebookPresence != null) {
            this.setFacebookPresence(contactData.facebookPresence);
         }
      }

      if (contactData.statusMessage != null) {
         this.setStatusMessage(contactData.statusMessage);
      }

      if (contactData.displayPicture != null) {
         this.setDisplayPictureGuid(contactData.displayPicture);
      }

      if (!connection.isMidletVersionAndAbove(440) && !connection.isAjax() && contactData.contactGroupId != null) {
         this.setContactGroupId(contactData.contactGroupId < 0 ? -1 : contactData.contactGroupId);
      }

   }
}
