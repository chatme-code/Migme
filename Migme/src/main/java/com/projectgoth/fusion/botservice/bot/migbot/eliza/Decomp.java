package com.projectgoth.fusion.botservice.bot.migbot.eliza;

public class Decomp {
   String pattern;
   boolean mem;
   ReasembList reasemb;
   int currReasmb;

   Decomp(String pattern, boolean mem, ReasembList reasemb) {
      this.pattern = pattern;
      this.mem = mem;
      this.reasemb = reasemb;
      this.currReasmb = 100;
   }

   public void print(int indent) {
      String m = this.mem ? "true" : "false";

      for(int i = 0; i < indent; ++i) {
         System.out.print(" ");
      }

      System.out.println("decomp: " + this.pattern + " " + m);
      this.reasemb.print(indent + 2);
   }

   public String pattern() {
      return this.pattern;
   }

   public boolean mem() {
      return this.mem;
   }

   public String nextRule() {
      if (this.reasemb.size() == 0) {
         System.out.println("No reassembly rule.");
         return null;
      } else {
         return (String)this.reasemb.elementAt(this.currReasmb);
      }
   }

   public void stepRule() {
      int size = this.reasemb.size();
      if (this.mem) {
         this.currReasmb = (int)(Math.random() * (double)size);
      }

      ++this.currReasmb;
      if (this.currReasmb >= size) {
         this.currReasmb = 0;
      }

   }
}
