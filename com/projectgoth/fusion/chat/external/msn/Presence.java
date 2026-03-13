package com.projectgoth.fusion.chat.external.msn;

import com.projectgoth.fusion.fdl.enums.PresenceType;

public enum Presence {
   NLN("Online"),
   BSY("Busy"),
   IDL("Idle"),
   BRB("Be Right Back"),
   AWY("Away"),
   PHN("On The Phone"),
   LUN("Out To Lunch"),
   HDN("Hidden"),
   FLN("Offline");

   private String description;

   private Presence(String description) {
      this.description = description;
   }

   public String getDescription() {
      return this.description;
   }

   public PresenceType toFusionPresence() {
      switch(this) {
      case NLN:
         return PresenceType.AVAILABLE;
      case BSY:
         return PresenceType.BUSY;
      case IDL:
      case BRB:
      case AWY:
      case PHN:
      case LUN:
         return PresenceType.AWAY;
      case HDN:
      case FLN:
         return PresenceType.OFFLINE;
      default:
         return PresenceType.OFFLINE;
      }
   }

   public static Presence fromFusionPresence(PresenceType fusionPresence) {
      switch(fusionPresence) {
      case AVAILABLE:
      case ROAMING:
         return NLN;
      case AWAY:
         return AWY;
      case BUSY:
         return BSY;
      case OFFLINE:
         return HDN;
      default:
         return HDN;
      }
   }
}
