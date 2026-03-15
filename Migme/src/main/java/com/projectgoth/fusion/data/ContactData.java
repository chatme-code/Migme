package com.projectgoth.fusion.data;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PhoneNumberType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.slice.ContactDataIce;
import com.projectgoth.fusion.slice.PresenceAndCapabilityIce;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ContactData implements Serializable {
   private static final long serialVersionUID = -4889719923367976759L;
   public Integer id;
   public String username;
   public String displayName;
   public String firstName;
   public String lastName;
   public String fusionUsername;
   public String msnUsername;
   public String aimUsername;
   public String yahooUsername;
   public String facebookUsername;
   public String gtalkUsername;
   public String emailAddress;
   public String mobilePhone;
   public String homePhone;
   public String officePhone;
   public ImType defaultIM;
   public PhoneNumberType defaultPhoneNumber;
   public Integer contactGroupId;
   public Boolean shareMobilePhone;
   public Boolean displayOnPhone;
   public ContactData.StatusEnum status;
   public String displayPicture;
   public String statusMessage;
   public Date statusTimeStamp;
   public PresenceType fusionPresence;
   public PresenceType msnPresence;
   public PresenceType aimPresence;
   public PresenceType yahooPresence;
   public PresenceType facebookPresence;
   public PresenceType gtalkPresence;
   public final ContactData.VoiceCapabilityEnum voiceCapability;

   public boolean isMSNOnly() {
      return this.msnUsername != null && this.fusionUsername == null && this.emailAddress == null && this.mobilePhone == null && this.homePhone == null && this.officePhone == null && this.yahooUsername == null && this.aimUsername == null && this.gtalkUsername == null && this.facebookUsername == null;
   }

   public boolean isYahooOnly() {
      return this.yahooUsername != null && this.fusionUsername == null && this.emailAddress == null && this.mobilePhone == null && this.homePhone == null && this.officePhone == null && this.msnUsername == null && this.aimUsername == null && this.gtalkUsername == null && this.facebookUsername == null;
   }

   public boolean isAIMOnly() {
      return this.aimUsername != null && this.fusionUsername == null && this.emailAddress == null && this.mobilePhone == null && this.homePhone == null && this.officePhone == null && this.yahooUsername == null && this.msnUsername == null && this.gtalkUsername == null && this.facebookUsername == null;
   }

   public boolean isGTalkOnly() {
      return this.gtalkUsername != null && this.fusionUsername == null && this.emailAddress == null && this.mobilePhone == null && this.homePhone == null && this.officePhone == null && this.yahooUsername == null && this.msnUsername == null && this.aimUsername == null && this.facebookUsername == null;
   }

   public boolean isFacebookOnly() {
      return this.facebookUsername != null && this.fusionUsername == null && this.emailAddress == null && this.mobilePhone == null && this.homePhone == null && this.officePhone == null && this.yahooUsername == null && this.msnUsername == null && this.aimUsername == null && this.gtalkUsername == null;
   }

   public boolean isOtherIMOnly() {
      return this.fusionUsername == null && this.emailAddress == null && this.mobilePhone == null && this.homePhone == null && this.officePhone == null && this.yahooUsername != null ^ this.msnUsername != null ^ this.aimUsername != null ^ this.facebookUsername != null ^ this.gtalkUsername != null;
   }

   public boolean isOnline() {
      return this.fusionPresence != null && this.fusionPresence != PresenceType.OFFLINE || this.msnPresence != null && this.msnPresence != PresenceType.OFFLINE || this.yahooPresence != null && this.yahooPresence != PresenceType.OFFLINE || this.aimPresence != null && this.aimPresence != PresenceType.OFFLINE || this.gtalkPresence != null && this.gtalkPresence != PresenceType.OFFLINE || this.facebookPresence != null && this.facebookPresence != PresenceType.OFFLINE;
   }

   public ContactData() {
      this.voiceCapability = ContactData.VoiceCapabilityEnum.NOT_AVAILABLE;
   }

   public ContactData(ResultSet rs) throws SQLException {
      this.voiceCapability = ContactData.VoiceCapabilityEnum.NOT_AVAILABLE;
      this.id = (Integer)rs.getObject("contact.id");
      this.username = rs.getString("contact.username");
      this.displayName = rs.getString("contact.displayName");
      this.firstName = rs.getString("contact.firstName");
      this.lastName = rs.getString("contact.lastName");
      this.fusionUsername = rs.getString("contact.fusionUsername");
      this.msnUsername = rs.getString("contact.msnUsername");
      this.aimUsername = rs.getString("contact.aimUsername");
      this.yahooUsername = rs.getString("contact.yahooUsername");
      this.facebookUsername = rs.getString("contact.icqUsername");
      this.gtalkUsername = rs.getString("contact.jabberUsername");
      this.emailAddress = rs.getString("contact.emailAddress");
      this.mobilePhone = rs.getString("contact.mobilePhone");
      this.homePhone = rs.getString("contact.homePhone");
      this.officePhone = rs.getString("contact.officePhone");
      this.contactGroupId = (Integer)rs.getObject("contact.contactGroupId");
      Integer intval = (Integer)rs.getObject("contact.defaultIM");
      if (intval != null) {
         this.defaultIM = ImType.fromValue(intval);
      }

      intval = (Integer)rs.getObject("contact.defaultPhoneNumber");
      if (intval != null) {
         this.defaultPhoneNumber = PhoneNumberType.fromValue(intval);
      }

      intval = (Integer)rs.getObject("contact.displayOnPhone");
      if (intval != null) {
         this.displayOnPhone = intval != 0;
      }

      intval = (Integer)rs.getObject("contact.status");
      if (intval != null) {
         this.status = ContactData.StatusEnum.fromValue(intval);
      }

   }

   public ContactData(ContactDataIce contactIce) {
      this.voiceCapability = ContactData.VoiceCapabilityEnum.NOT_AVAILABLE;
      this.id = contactIce.id == Integer.MIN_VALUE ? null : contactIce.id;
      this.username = contactIce.username.equals("\u0000") ? null : contactIce.username;
      this.displayName = contactIce.displayName.equals("\u0000") ? null : contactIce.displayName;
      this.firstName = contactIce.firstName.equals("\u0000") ? null : contactIce.firstName;
      this.lastName = contactIce.lastName.equals("\u0000") ? null : contactIce.lastName;
      this.fusionUsername = contactIce.fusionUsername.equals("\u0000") ? null : contactIce.fusionUsername;
      this.msnUsername = contactIce.msnUsername.equals("\u0000") ? null : contactIce.msnUsername;
      this.aimUsername = contactIce.aimUsername.equals("\u0000") ? null : contactIce.aimUsername;
      this.yahooUsername = contactIce.yahooUsername.equals("\u0000") ? null : contactIce.yahooUsername;
      this.facebookUsername = contactIce.facebookUsername.equals("\u0000") ? null : contactIce.facebookUsername;
      this.gtalkUsername = contactIce.gtalkUsername.equals("\u0000") ? null : contactIce.gtalkUsername;
      this.emailAddress = contactIce.emailAddress.equals("\u0000") ? null : contactIce.emailAddress;
      this.mobilePhone = contactIce.mobilePhone.equals("\u0000") ? null : contactIce.mobilePhone;
      this.homePhone = contactIce.homePhone.equals("\u0000") ? null : contactIce.homePhone;
      this.officePhone = contactIce.officePhone.equals("\u0000") ? null : contactIce.officePhone;
      this.defaultIM = contactIce.defaultIM == Integer.MIN_VALUE ? null : ImType.fromValue(contactIce.defaultIM);
      this.defaultPhoneNumber = contactIce.defaultPhoneNumber == Integer.MIN_VALUE ? null : PhoneNumberType.fromValue(contactIce.defaultPhoneNumber);
      this.contactGroupId = contactIce.contactGroupId == Integer.MIN_VALUE ? null : contactIce.contactGroupId;
      this.shareMobilePhone = contactIce.shareMobilePhone == Integer.MIN_VALUE ? null : contactIce.shareMobilePhone == 1;
      this.displayOnPhone = contactIce.displayOnPhone == Integer.MIN_VALUE ? null : contactIce.displayOnPhone == 1;
      this.status = contactIce.status == Integer.MIN_VALUE ? null : ContactData.StatusEnum.fromValue(contactIce.status);
      this.fusionPresence = contactIce.fusionPresence == Integer.MIN_VALUE ? null : PresenceType.fromValue(contactIce.fusionPresence);
      this.msnPresence = contactIce.msnPresence == Integer.MIN_VALUE ? null : PresenceType.fromValue(contactIce.msnPresence);
      this.aimPresence = contactIce.aimPresence == Integer.MIN_VALUE ? null : PresenceType.fromValue(contactIce.aimPresence);
      this.yahooPresence = contactIce.yahooPresence == Integer.MIN_VALUE ? null : PresenceType.fromValue(contactIce.yahooPresence);
      this.facebookPresence = contactIce.facebookPresence == Integer.MIN_VALUE ? null : PresenceType.fromValue(contactIce.facebookPresence);
      this.gtalkPresence = contactIce.gtalkPresence == Integer.MIN_VALUE ? null : PresenceType.fromValue(contactIce.gtalkPresence);
      this.displayPicture = contactIce.displayPicture.equals("\u0000") ? null : contactIce.displayPicture;
      this.statusMessage = contactIce.statusMessage.equals("\u0000") ? null : contactIce.statusMessage;
      this.statusTimeStamp = contactIce.statusTimeStamp == Long.MIN_VALUE ? null : new Date(contactIce.statusTimeStamp);
   }

   public ContactDataIce toIceObject() {
      ContactDataIce contactIce = new ContactDataIce();
      contactIce.id = this.id == null ? Integer.MIN_VALUE : this.id;
      contactIce.username = this.username == null ? "\u0000" : this.username;
      contactIce.displayName = this.displayName == null ? "\u0000" : this.displayName;
      contactIce.firstName = this.firstName == null ? "\u0000" : this.firstName;
      contactIce.lastName = this.lastName == null ? "\u0000" : this.lastName;
      contactIce.fusionUsername = this.fusionUsername == null ? "\u0000" : this.fusionUsername;
      contactIce.msnUsername = this.msnUsername == null ? "\u0000" : this.msnUsername;
      contactIce.aimUsername = this.aimUsername == null ? "\u0000" : this.aimUsername;
      contactIce.yahooUsername = this.yahooUsername == null ? "\u0000" : this.yahooUsername;
      contactIce.facebookUsername = this.facebookUsername == null ? "\u0000" : this.facebookUsername;
      contactIce.gtalkUsername = this.gtalkUsername == null ? "\u0000" : this.gtalkUsername;
      contactIce.emailAddress = this.emailAddress == null ? "\u0000" : this.emailAddress;
      contactIce.mobilePhone = this.mobilePhone == null ? "\u0000" : this.mobilePhone;
      contactIce.homePhone = this.homePhone == null ? "\u0000" : this.homePhone;
      contactIce.officePhone = this.officePhone == null ? "\u0000" : this.officePhone;
      contactIce.defaultIM = this.defaultIM == null ? Integer.MIN_VALUE : this.defaultIM.value();
      contactIce.defaultPhoneNumber = this.defaultPhoneNumber == null ? Integer.MIN_VALUE : this.defaultPhoneNumber.value();
      contactIce.contactGroupId = this.contactGroupId == null ? Integer.MIN_VALUE : this.contactGroupId;
      contactIce.shareMobilePhone = this.shareMobilePhone == null ? Integer.MIN_VALUE : (this.shareMobilePhone ? 1 : 0);
      contactIce.displayOnPhone = this.displayOnPhone == null ? Integer.MIN_VALUE : (this.displayOnPhone ? 1 : 0);
      contactIce.status = this.status == null ? Integer.MIN_VALUE : this.status.value();
      contactIce.fusionPresence = this.fusionPresence == null ? Integer.MIN_VALUE : this.fusionPresence.value();
      contactIce.msnPresence = this.msnPresence == null ? Integer.MIN_VALUE : this.msnPresence.value();
      contactIce.aimPresence = this.aimPresence == null ? Integer.MIN_VALUE : this.aimPresence.value();
      contactIce.yahooPresence = this.yahooPresence == null ? Integer.MIN_VALUE : this.yahooPresence.value();
      contactIce.facebookPresence = this.facebookPresence == null ? Integer.MIN_VALUE : this.facebookPresence.value();
      contactIce.gtalkPresence = this.gtalkPresence == null ? Integer.MIN_VALUE : this.gtalkPresence.value();
      contactIce.displayPicture = this.displayPicture == null ? "\u0000" : this.displayPicture;
      contactIce.statusMessage = this.statusMessage == null ? "\u0000" : this.statusMessage;
      contactIce.statusTimeStamp = this.statusTimeStamp == null ? Long.MIN_VALUE : this.statusTimeStamp.getTime();
      return contactIce;
   }

   public void copyPresenceAndCapability(ContactDataIce contact) {
      this.fusionPresence = PresenceType.fromValue(contact.fusionPresence);
      this.aimPresence = PresenceType.fromValue(contact.aimPresence);
      this.gtalkPresence = PresenceType.fromValue(contact.gtalkPresence);
      this.facebookPresence = PresenceType.fromValue(contact.facebookPresence);
      this.msnPresence = PresenceType.fromValue(contact.msnPresence);
      this.yahooPresence = PresenceType.fromValue(contact.yahooPresence);
   }

   public void assignPresence(PresenceAndCapabilityIce presence) {
      if (presence != null) {
         this.fusionPresence = PresenceType.fromValue(presence.fusionPresence);
         this.aimPresence = PresenceType.fromValue(presence.aimPresence);
         this.gtalkPresence = PresenceType.fromValue(presence.gtalkPresence);
         this.facebookPresence = PresenceType.fromValue(presence.facebookPresence);
         this.msnPresence = PresenceType.fromValue(presence.msnPresence);
         this.yahooPresence = PresenceType.fromValue(presence.yahooPresence);
      }
   }

   public PresenceAndCapabilityIce getPresenceAndCapabilty() {
      PresenceAndCapabilityIce presence = new PresenceAndCapabilityIce();
      presence.aimPresence = this.aimPresence.value();
      presence.fusionPresence = this.fusionPresence.value();
      presence.gtalkPresence = this.gtalkPresence.value();
      presence.facebookPresence = this.facebookPresence.value();
      presence.msnPresence = this.msnPresence.value();
      presence.yahooPresence = this.yahooPresence.value();
      return presence;
   }

   public static void initializePresenceToOffline(PresenceAndCapabilityIce presence) {
      presence.fusionPresence = PresenceType.OFFLINE.value();
      presence.msnPresence = PresenceType.OFFLINE.value();
      presence.aimPresence = PresenceType.OFFLINE.value();
      presence.yahooPresence = PresenceType.OFFLINE.value();
      presence.facebookPresence = PresenceType.OFFLINE.value();
      presence.gtalkPresence = PresenceType.OFFLINE.value();
   }

   public static enum VoiceCapabilityEnum {
      NOT_AVAILABLE(0),
      AVAILABLE(1);

      private int value;

      private VoiceCapabilityEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ContactData.VoiceCapabilityEnum fromValue(int value) {
         ContactData.VoiceCapabilityEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ContactData.VoiceCapabilityEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ContactData.StatusEnum fromValue(int value) {
         ContactData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ContactData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
