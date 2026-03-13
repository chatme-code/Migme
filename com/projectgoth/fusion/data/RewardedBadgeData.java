package com.projectgoth.fusion.data;

import java.io.Serializable;

public class RewardedBadgeData implements Serializable {
   public static final int INVALID_BADGE_ID = -1;
   private final int id;

   public RewardedBadgeData(int badgeId) {
      this.id = badgeId;
   }

   public int getId() {
      return this.id;
   }

   public String toString() {
      return "badge#" + this.id;
   }
}
