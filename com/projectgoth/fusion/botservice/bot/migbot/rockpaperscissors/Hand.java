package com.projectgoth.fusion.botservice.bot.migbot.rockpaperscissors;

import java.security.SecureRandom;

public enum Hand {
   CLOSED("(rps_closed)"),
   ROCK("(rps_rock)"),
   PAPER("(rps_paper)"),
   SCISSORS("(rps_scissors)");

   private static SecureRandom random = new SecureRandom();
   private String emoticonKey;

   private Hand(String emoticonKey) {
      this.emoticonKey = emoticonKey;
   }

   public String getEmoticonKey() {
      return this.emoticonKey;
   }

   public static Hand random() {
      int i = random.nextInt(3);
      if (i == 0) {
         return ROCK;
      } else {
         return i == 1 ? PAPER : SCISSORS;
      }
   }
}
