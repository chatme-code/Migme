package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.ByteArrayOutputStream;

public class FusionPktPresenceOld extends FusionPacket {
   public FusionPktPresenceOld() {
      super((short)404);
   }

   public FusionPktPresenceOld(short transactionId) {
      super((short)404, transactionId);
   }

   public FusionPktPresenceOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktPresenceOld(int contactID, int imType, int presence, ConnectionI connection) {
      super((short)404);
      this.setContactId(contactID);
      if (imType != ImType.FUSION.value() && connection.isMidletVersionAndAbove(430)) {
         byte[] imTypes = new byte[]{(byte)imType};
         byte[] imPresences = new byte[]{(byte)presence};
         this.setIMTypes(imTypes);
         this.setIMPresences(imPresences);
      } else {
         this.setField((short)(imType + 1), (byte)presence);
      }

   }

   public FusionPktPresenceOld(short transactionId, ContactData contactData, ConnectionI connection) {
      super((short)404, transactionId);
      if (contactData.id != null) {
         this.setContactId(contactData.id);
      }

      if (contactData.fusionPresence != null) {
         this.setFusionPresence(contactData.fusionPresence.value());
      }

      if (connection.isMidletVersionAndAbove(430)) {
         ByteArrayOutputStream imTypes = new ByteArrayOutputStream();
         ByteArrayOutputStream imPresences = new ByteArrayOutputStream();
         if (contactData.msnPresence != null) {
            imTypes.write(Enums.IMEnum.MSN.getImType().value());
            imPresences.write(contactData.msnPresence.value());
         }

         if (contactData.aimPresence != null) {
         }

         if (contactData.yahooPresence != null) {
            imTypes.write(Enums.IMEnum.YAHOO.getImType().value());
            imPresences.write(contactData.yahooPresence.value());
         }

         if (contactData.gtalkPresence != null) {
            imTypes.write(Enums.IMEnum.GTALK.getImType().value());
            imPresences.write(contactData.gtalkPresence.value());
         }

         if (contactData.facebookPresence != null) {
            imTypes.write(Enums.IMEnum.FACEBOOK.getImType().value());
            imPresences.write(contactData.facebookPresence.value());
         }

         this.setIMTypes(imTypes.toByteArray());
         this.setIMPresences(imPresences.toByteArray());
      } else {
         if (contactData.msnPresence != null) {
            this.setMSNPresence(contactData.msnPresence.value());
         }

         if (contactData.aimPresence != null) {
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

   }

   public Integer getContactId() {
      return this.getIntField((short)1);
   }

   public void setContactId(int ContactId) {
      this.setField((short)1, ContactId);
   }

   public Byte getFusionPresence() {
      return this.getByteField((short)2);
   }

   public void setFusionPresence(byte fusionPresence) {
      this.setField((short)2, fusionPresence);
   }

   public Byte getMSNPresence() {
      return this.getByteField((short)3);
   }

   public void setMSNPresence(byte msnPresence) {
      this.setField((short)3, msnPresence);
   }

   public Byte getAIMPresence() {
      return this.getByteField((short)4);
   }

   public void setAIMPresence(byte aimPresence) {
      this.setField((short)4, aimPresence);
   }

   public Byte getYahooPresence() {
      return this.getByteField((short)5);
   }

   public void setYahooPresence(byte yahooPresence) {
      this.setField((short)5, yahooPresence);
   }

   public Byte getICQPresence() {
      return this.getByteField((short)6);
   }

   public void setICQPresence(byte icqPresence) {
      this.setField((short)6, icqPresence);
   }

   public Byte getGTalkPresence() {
      return this.getByteField((short)7);
   }

   public void setGTalkPresence(byte gtalkPresence) {
      this.setField((short)7, gtalkPresence);
   }

   public Byte getFacebookPresence() {
      return this.getByteField((short)8);
   }

   public void setFacebookPresence(byte facebookPresence) {
      this.setField((short)8, facebookPresence);
   }

   public byte[] getIMTypes() {
      return this.getByteArrayField((short)9);
   }

   public void setIMTypes(byte[] imTypes) {
      this.setField((short)9, imTypes);
   }

   public byte[] getIMPresences() {
      return this.getByteArrayField((short)10);
   }

   public void setIMPresences(byte[] imPresences) {
      this.setField((short)10, imPresences);
   }
}
