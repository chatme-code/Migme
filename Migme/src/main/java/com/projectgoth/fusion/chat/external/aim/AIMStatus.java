package com.projectgoth.fusion.chat.external.aim;

import com.projectgoth.fusion.fdl.enums.PresenceType;

public enum AIMStatus {
   AVAILABLE(0, "Available"),
   AWAY(1, "Away"),
   INVISIBLE(256, "Invisible"),
   OFFLINE(-1, "Offline");

   private int value;
   private String description;

   private AIMStatus(int value, String description) {
      this.value = value;
      this.description = description;
   }

   public int getValue() {
      return this.value;
   }

   public String getDescription() {
      return this.description;
   }

   public static AIMStatus valueOf(int value) {
      AIMStatus[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         AIMStatus status = arr$[i$];
         if (status.value == value) {
            return status;
         }
      }

      return null;
   }

   public PresenceType toFusionPresence() {
      switch(this) {
      case AVAILABLE:
         return PresenceType.AVAILABLE;
      case AWAY:
         return PresenceType.AWAY;
      case INVISIBLE:
      case OFFLINE:
      default:
         return PresenceType.OFFLINE;
      }
   }

   public static AIMStatus fromFusionPresence(PresenceType fusionPresence) {
      switch(fusionPresence) {
      case AVAILABLE:
      case ROAMING:
         return AVAILABLE;
      case AWAY:
      case BUSY:
         return AWAY;
      case OFFLINE:
      default:
         return OFFLINE;
      }
   }
}
