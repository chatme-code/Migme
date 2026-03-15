package com.projectgoth.fusion.botservice.bot.migbot.eliza;

public class Mem {
   final int memMax = 20;
   String[] memory = new String[20];
   int memTop = 0;

   public void save(String str) {
      if (this.memTop < 20) {
         this.memory[this.memTop++] = str;
      }

   }

   public String get() {
      if (this.memTop == 0) {
         return null;
      } else {
         String m = this.memory[0];

         for(int i = 0; i < this.memTop - 1; ++i) {
            this.memory[i] = this.memory[i + 1];
         }

         --this.memTop;
         return m;
      }
   }
}
