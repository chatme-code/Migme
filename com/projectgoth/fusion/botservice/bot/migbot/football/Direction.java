package com.projectgoth.fusion.botservice.bot.migbot.football;

import java.security.SecureRandom;

public enum Direction {
   UNKNOWN,
   LEFT,
   CENTRE,
   RIGHT;

   private static SecureRandom random = new SecureRandom();

   public static Direction random() {
      int i = random.nextInt(3);
      if (i == 0) {
         return LEFT;
      } else {
         return i == 1 ? CENTRE : RIGHT;
      }
   }
}
