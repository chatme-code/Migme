package com.projectgoth.fusion.botservice.bot.migbot.eliza;

public class PrePost {
   String src;
   String dest;

   PrePost(String src, String dest) {
      this.src = src;
      this.dest = dest;
   }

   public void print(int indent) {
      for(int i = 0; i < indent; ++i) {
         System.out.print(" ");
      }

      System.out.println("pre-post: " + this.src + "  " + this.dest);
   }

   public String src() {
      return this.src;
   }

   public String dest() {
      return this.dest;
   }
}
