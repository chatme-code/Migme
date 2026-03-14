package com.projectgoth.fusion.botservice.bot.migbot.dice;

import java.security.SecureRandom;

public class DiceRoll {
   private static SecureRandom random = new SecureRandom();
   private int die1 = 0;
   private int die2 = 0;
   private boolean isWinner;

   public void roll() {
      this.die1 = random.nextInt(6) + 1;
      this.die2 = random.nextInt(6) + 1;
   }

   public void rollAndMatch(int total) {
      this.roll();
      this.isWinner = this.total() >= total;
   }

   public int getDie1() {
      return this.die1;
   }

   public int getDie2() {
      return this.die2;
   }

   public int total() {
      return this.die1 + this.die2;
   }

   public void reset() {
      this.die1 = 0;
      this.die2 = 0;
   }

   public boolean isWinner() {
      return this.isWinner;
   }

   public String toString() {
      return this.getDisplayString(this.die1) + " " + this.getDisplayString(this.die2);
   }

   private String getDisplayString(int die) {
      return "(d" + die + ")";
   }
}
