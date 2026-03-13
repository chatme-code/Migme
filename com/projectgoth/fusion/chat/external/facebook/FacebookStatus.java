package com.projectgoth.fusion.chat.external.facebook;

import com.projectgoth.fusion.fdl.enums.PresenceType;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

public enum FacebookStatus {
   OFFLINE("Offline"),
   AVAILABLE("Available"),
   AWAY("Away");

   private String description;

   private FacebookStatus(String description) {
      this.description = description;
   }

   public String getDescription() {
      return this.description;
   }

   public PresenceType toFusionPresence() {
      switch(this) {
      case AVAILABLE:
         return PresenceType.AVAILABLE;
      case AWAY:
         return PresenceType.AWAY;
      case OFFLINE:
      default:
         return PresenceType.OFFLINE;
      }
   }

   public Presence toXMPPPresence() {
      Presence p;
      switch(this) {
      case AVAILABLE:
         p = new Presence(Type.available);
         p.setMode(Mode.available);
         return p;
      case AWAY:
         p = new Presence(Type.available);
         p.setMode(Mode.away);
         return p;
      case OFFLINE:
      default:
         return new Presence(Type.unavailable);
      }
   }

   public static FacebookStatus fromFusionPresence(PresenceType fusionPresence) {
      switch(fusionPresence) {
      case AVAILABLE:
      case ROAMING:
      case BUSY:
         return AVAILABLE;
      case AWAY:
         return AWAY;
      case OFFLINE:
      default:
         return OFFLINE;
      }
   }

   public static FacebookStatus fromXMPPPresence(Presence presence) {
      if (presence.isAvailable()) {
         return AVAILABLE;
      } else {
         return presence.isAway() ? AWAY : OFFLINE;
      }
   }
}
