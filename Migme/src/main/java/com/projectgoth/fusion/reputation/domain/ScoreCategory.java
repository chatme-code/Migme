package com.projectgoth.fusion.reputation.domain;

public enum ScoreCategory {
   TIME_IN_PRODUCT(0),
   CREDITS_SPENT(1),
   HUMAN_LIKELY_BEHAVIOUR(2),
   BASIC_ACTIVITY(3);

   private final int value;

   private ScoreCategory(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }

   public static ScoreCategory fromValue(int value) {
      ScoreCategory[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         ScoreCategory e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return null;
   }
}
