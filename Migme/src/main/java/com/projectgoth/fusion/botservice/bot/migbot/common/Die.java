package com.projectgoth.fusion.botservice.bot.migbot.common;

import java.security.SecureRandom;

public class Die {
   private int value = 1;
   private static SecureRandom random = new SecureRandom();

   public int getValue() {
      return this.value;
   }

   public String toEmoticonHotkey() {
      return "(d" + Integer.toString(this.value) + ")";
   }

   public boolean equals(Object obj) {
      if (obj != null && obj instanceof Die) {
         return this.value == ((Die)obj).getValue();
      } else {
         return false;
      }
   }

   public String toString() {
      return Integer.toString(this.value);
   }

   public int roll() {
      this.value = random.nextInt(6);
      return this.value;
   }

   public String rollAndGetEmoticonHotkey() {
      this.roll();
      return this.toEmoticonHotkey();
   }
}
