package com.projectgoth.fusion.chat.external.gtalk;

import com.projectgoth.fusion.fdl.enums.PresenceType;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

public enum GTalkStatus {
   AVAILABLE("available", "Available"),
   AWAY("away", "Away"),
   CHAT("chat", "Chat"),
   DND("dnd", "Do Not Disturb"),
   XA("xa", "Extended Away"),
   OFFLINE("offline", "Offline");

   private String value;
   private String description;

   private GTalkStatus(String value, String description) {
      this.value = value;
      this.description = description;
   }

   public String getValue() {
      return this.value;
   }

   public String getDescription() {
      return this.description;
   }

   public static GTalkStatus parse(String value) {
      GTalkStatus[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         GTalkStatus status = arr$[i$];
         if (status.value.equals(value)) {
            return status;
         }
      }

      return null;
   }

   public Presence toXMPPPresence() {
      Presence p = new Presence(Type.available);
      switch(this) {
      case AVAILABLE:
         p.setMode(Mode.available);
         return p;
      case CHAT:
         p.setMode(Mode.chat);
         return p;
      case AWAY:
         p.setMode(Mode.away);
         return p;
      case XA:
         p.setMode(Mode.xa);
         return p;
      case DND:
         p.setMode(Mode.dnd);
         return p;
      case OFFLINE:
      default:
         return new Presence(Type.unavailable);
      }
   }

   public PresenceType toFusionPresence() {
      switch(this) {
      case AVAILABLE:
      case CHAT:
         return PresenceType.AVAILABLE;
      case AWAY:
      case XA:
         return PresenceType.AWAY;
      case DND:
         return PresenceType.BUSY;
      case OFFLINE:
      default:
         return PresenceType.OFFLINE;
      }
   }

   public static GTalkStatus fromFusionPresence(PresenceType fusionPresence) {
      switch(fusionPresence) {
      case AVAILABLE:
      case ROAMING:
         return AVAILABLE;
      case AWAY:
         return AWAY;
      case BUSY:
         return DND;
      case OFFLINE:
      default:
         return OFFLINE;
      }
   }

   public static GTalkStatus fromXMPPPresence(Presence presence) {
      if (presence.isAvailable()) {
         Mode mode = presence.getMode();
         if (mode == null) {
            mode = Mode.available;
         }

         switch(mode) {
         case chat:
            return CHAT;
         case away:
            return AWAY;
         case dnd:
            return DND;
         case xa:
            return XA;
         default:
            return AVAILABLE;
         }
      } else {
         return OFFLINE;
      }
   }
}
