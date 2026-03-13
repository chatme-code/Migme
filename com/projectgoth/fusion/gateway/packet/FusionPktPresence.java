package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataPresence;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.ArrayList;
import java.util.List;

public class FusionPktPresence extends FusionPktDataPresence {
   public FusionPktPresence() {
   }

   public FusionPktPresence(short transactionId) {
      super(transactionId);
   }

   public FusionPktPresence(FusionPacket packet) {
      super(packet);
   }

   public FusionPktPresence(int contactID, int imTypeValue, int presenceValue, ConnectionI connection) {
      this.setContactId(contactID);
      ImType imType = ImType.fromValue((byte)imTypeValue);
      PresenceType presence = PresenceType.fromValue((byte)presenceValue);
      if (imType != ImType.FUSION && connection.isMidletVersionAndAbove(430)) {
         ImType[] imTypes = new ImType[]{imType};
         PresenceType[] imPresences = new PresenceType[]{presence};
         this.setImTypeList(imTypes);
         this.setImPresenceList(imPresences);
      } else {
         switch(imType) {
         case FUSION:
            this.setPresence(presence);
            break;
         case MSN:
            this.setMsnPresence(presence);
            break;
         case AIM:
            this.setAimPresence(presence);
            break;
         case YAHOO:
            this.setYahooPresence(presence);
            break;
         case ICQ:
            this.setIcqPresence(presence);
            break;
         case GTALK:
            this.setGtalkPresence(presence);
            break;
         case FACEBOOK:
            this.setFacebookPresence(presence);
         }
      }

   }

   public FusionPktPresence(short transactionId, ContactData contactData, ConnectionI connection) {
      super(transactionId);
      if (contactData.id != null) {
         this.setContactId(contactData.id);
      }

      if (contactData.fusionPresence != null) {
         this.setPresence(contactData.fusionPresence);
      }

      if (connection.isMidletVersionAndAbove(430)) {
         int MAXIMUM_LIST_SIZE = true;
         List<ImType> imTypeList = new ArrayList(5);
         List<PresenceType> presenceList = new ArrayList(5);
         if (contactData.msnPresence != null) {
            imTypeList.add(ImType.MSN);
            presenceList.add(contactData.msnPresence);
         }

         if (contactData.aimPresence != null) {
         }

         if (contactData.yahooPresence != null) {
            imTypeList.add(ImType.YAHOO);
            presenceList.add(contactData.yahooPresence);
         }

         if (contactData.gtalkPresence != null) {
            imTypeList.add(ImType.GTALK);
            presenceList.add(contactData.gtalkPresence);
         }

         if (contactData.facebookPresence != null) {
            imTypeList.add(ImType.FACEBOOK);
            presenceList.add(contactData.facebookPresence);
         }

         this.setImTypeList((ImType[])imTypeList.toArray(new ImType[imTypeList.size()]));
         this.setImPresenceList((PresenceType[])presenceList.toArray(new PresenceType[presenceList.size()]));
      } else {
         if (contactData.msnPresence != null) {
            this.setMsnPresence(contactData.msnPresence);
         }

         if (contactData.aimPresence != null) {
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

   }
}
