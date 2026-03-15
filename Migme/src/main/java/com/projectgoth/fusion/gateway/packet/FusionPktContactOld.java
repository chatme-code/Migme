package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FusionPktContactOld extends FusionPacket {
   public FusionPktContactOld() {
      super((short)402);
   }

   public FusionPktContactOld(short transactionId) {
      super((short)402, transactionId);
   }

   public FusionPktContactOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktContactOld(short transactionId, ContactData contactData, ConnectionI connection) {
      super((short)402, transactionId);
      if (contactData.id != null) {
         this.setContactId(contactData.id);
      }

      if (contactData.contactGroupId != null) {
         this.setGroupId(contactData.contactGroupId);
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
         this.setDefaultPhone(contactData.defaultPhoneNumber.value());
      }

      if (contactData.mobilePhone != null) {
         this.setMobilePhone(contactData.mobilePhone);
      }

      if (contactData.homePhone != null) {
         this.setHomePhone(contactData.homePhone);
      }

      if (contactData.officePhone != null) {
         this.setOfficePhone(contactData.officePhone);
      }

      if (contactData.defaultIM != null) {
         this.setDefaultIM(contactData.defaultIM.value());
      }

      if (contactData.fusionUsername != null) {
         this.setFusionUsername(contactData.fusionUsername);
      }

      if (contactData.fusionPresence != null) {
         this.setFusionPresence(contactData.fusionPresence.value());
      }

      if (connection.isMidletVersionAndAbove(430)) {
         ByteArrayOutputStream imTypes = new ByteArrayOutputStream();
         ByteArrayOutputStream imPresences = new ByteArrayOutputStream();
         ArrayList<String> imUsernames = new ArrayList();
         if (contactData.msnUsername != null) {
            imTypes.write(Enums.IMEnum.MSN.getImType().value());
            imPresences.write(contactData.msnPresence == null ? PresenceType.OFFLINE.value() : contactData.msnPresence.value());
            imUsernames.add(contactData.msnUsername);
         }

         if (contactData.yahooUsername != null) {
            imTypes.write(Enums.IMEnum.YAHOO.getImType().value());
            imPresences.write(contactData.yahooPresence == null ? PresenceType.OFFLINE.value() : contactData.yahooPresence.value());
            imUsernames.add(contactData.yahooUsername);
         }

         if (contactData.gtalkUsername != null) {
            imTypes.write(Enums.IMEnum.GTALK.getImType().value());
            imPresences.write(contactData.gtalkPresence == null ? PresenceType.OFFLINE.value() : contactData.gtalkPresence.value());
            imUsernames.add(contactData.gtalkUsername);
         }

         if (contactData.facebookUsername != null) {
            imTypes.write(Enums.IMEnum.FACEBOOK.getImType().value());
            imPresences.write(contactData.facebookPresence == null ? PresenceType.OFFLINE.value() : contactData.facebookPresence.value());
            imUsernames.add(contactData.facebookUsername);
         }

         if (imTypes.size() > 0) {
            this.setIMTypes(imTypes.toByteArray());
         }

         if (imPresences.size() > 0) {
            this.setIMPresences(imPresences.toByteArray());
         }

         if (imUsernames.size() > 0) {
            this.setIMUsernames((String[])imUsernames.toArray(new String[imUsernames.size()]));
         }
      } else {
         if (contactData.msnUsername != null) {
            this.setMSNUsername(contactData.msnUsername);
         }

         if (contactData.yahooUsername != null) {
            this.setYahooUsername(contactData.yahooUsername);
         }

         if (contactData.gtalkUsername != null) {
            this.setGTalkUsername(contactData.gtalkUsername);
         }

         if (contactData.facebookUsername != null) {
            this.setFacebookUsername(contactData.facebookUsername);
         }

         if (contactData.msnPresence != null) {
            this.setMSNPresence(contactData.msnPresence.value());
         }

         if (contactData.yahooPresence != null) {
            this.setYahooPresence(contactData.yahooPresence.value());
         }

         if (contactData.gtalkPresence != null) {
            this.setGTalkPresence(contactData.gtalkPresence.value());
         }

         if (contactData.facebookPresence != null) {
            this.setFacebookPresence(contactData.facebookPresence.value());
         }
      }

      if (contactData.statusMessage != null) {
         this.setStatusMessage(contactData.statusMessage);
      }

      if (contactData.displayPicture != null) {
         this.setDisplayPicture(contactData.displayPicture);
      }

      if (!connection.isMidletVersionAndAbove(440) && !connection.isAjax() && contactData.contactGroupId != null) {
         this.setGroupId(contactData.contactGroupId < 0 ? -1 : contactData.contactGroupId);
      }

   }

   public Integer getContactId() {
      return this.getIntField((short)1);
   }

   public void setContactId(int contactId) {
      this.setField((short)1, contactId);
   }

   public Integer getGroupId() {
      return this.getIntField((short)2);
   }

   public void setGroupId(int groupId) {
      this.setField((short)2, groupId);
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
      return this.getStringField((short)12);
   }

   public void setFusionUsername(String fusionUsername) {
      this.setField((short)12, fusionUsername);
   }

   public Byte getFusionPresence() {
      return this.getByteField((short)13);
   }

   public void setFusionPresence(byte fusionPresence) {
      this.setField((short)13, fusionPresence);
   }

   public String getMSNUsername() {
      return this.getStringField((short)14);
   }

   public void setMSNUsername(String msnUsername) {
      this.setField((short)14, msnUsername);
   }

   public Byte getMSNPresence() {
      return this.getByteField((short)15);
   }

   public void setMSNPresence(byte msnPresence) {
      this.setField((short)15, msnPresence);
   }

   public String getAIMUsername() {
      return this.getStringField((short)16);
   }

   public void setAIMUsername(String aimUsername) {
      this.setField((short)16, aimUsername);
   }

   public Byte getAIMPresence() {
      return this.getByteField((short)17);
   }

   public void setAIMPresence(byte aimPresence) {
      this.setField((short)17, aimPresence);
   }

   public String getYahooUsername() {
      return this.getStringField((short)18);
   }

   public void setYahooUsername(String yahooUsername) {
      this.setField((short)18, yahooUsername);
   }

   public Byte getYahooPresence() {
      return this.getByteField((short)19);
   }

   public void setYahooPresence(byte yahooPresence) {
      this.setField((short)19, yahooPresence);
   }

   public String getICQUsername() {
      return this.getStringField((short)20);
   }

   public void setICQUsername(String icqUsername) {
      this.setField((short)20, icqUsername);
   }

   public Byte getICQPresence() {
      return this.getByteField((short)21);
   }

   public void setICQPresence(byte icqPresence) {
      this.setField((short)21, icqPresence);
   }

   public String getGTalkUsername() {
      return this.getStringField((short)22);
   }

   public void setGTalkUsername(String gtalkUsername) {
      this.setField((short)22, gtalkUsername);
   }

   public Byte getGTalkPresence() {
      return this.getByteField((short)23);
   }

   public void setGTalkPresence(byte gtalkPresence) {
      this.setField((short)23, gtalkPresence);
   }

   public Byte getShareMobilePhone() {
      return this.getByteField((short)24);
   }

   public void setShareMobilePhone(byte shareMobilePhone) {
      this.setField((short)24, shareMobilePhone);
   }

   /** @deprecated */
   @Deprecated
   public Byte getVoiceCapability() {
      return (byte)ContactData.VoiceCapabilityEnum.NOT_AVAILABLE.value();
   }

   /** @deprecated */
   @Deprecated
   public void setVoiceCapability(byte voiceCapability) {
   }

   public String getStatusMessage() {
      return this.getStringField((short)27);
   }

   public void setStatusMessage(String statusMessage) {
      this.setField((short)27, statusMessage);
   }

   public String getDisplayPicture() {
      return this.getStringField((short)28);
   }

   public void setDisplayPicture(String displayPicture) {
      this.setField((short)28, displayPicture);
   }

   public String getFacebookUsername() {
      return this.getStringField((short)29);
   }

   public void setFacebookUsername(String facebookUsername) {
      this.setField((short)29, facebookUsername);
   }

   public Byte getFacebookPresence() {
      return this.getByteField((short)30);
   }

   public void setFacebookPresence(byte facebookPresence) {
      this.setField((short)30, facebookPresence);
   }

   public byte[] getIMTypes() {
      return this.getByteArrayField((short)31);
   }

   public void setIMTypes(byte[] imTypes) {
      this.setField((short)31, imTypes);
   }

   public byte[] getIMPresences() {
      return this.getByteArrayField((short)32);
   }

   public void setIMPresences(byte[] imPresences) {
      this.setField((short)32, imPresences);
   }

   public String[] getIMUsernames() {
      return this.getStringArrayField((short)33);
   }

   public void setIMUsernames(String[] imUsernames) {
      this.setField((short)33, imUsernames);
   }
}
