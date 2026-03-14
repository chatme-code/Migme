package com.projectgoth.fusion.botservice.bot.migbot.warriors;

class Attack implements Comparable<Attack> {
   String name;
   int min;
   int max;
   double probability;
   String description;
   String attackEmote;

   Attack(String name, String description, int min, int max, double probability, String attackEmote) {
      this.description = description;
      this.name = name;
      this.min = min;
      this.max = max;
      this.probability = probability;
      this.attackEmote = attackEmote;
   }

   public int getRandomizedAttackDamage() {
      return (int)((double)this.min + Math.random() * (double)(this.max - this.min));
   }

   public int compareTo(Attack att) {
      return (int)(100.0D * (this.probability - att.probability));
   }

   public String getAttackEmote() {
      return this.attackEmote;
   }
}
