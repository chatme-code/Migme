package com.projectgoth.fusion.botservice.bot.migbot.eliza;

public class Key {
   String key;
   int rank;
   DecompList decomp;

   Key(String key, int rank, DecompList decomp) {
      this.key = key;
      this.rank = rank;
      this.decomp = decomp;
   }

   Key() {
      this.key = null;
      this.rank = 0;
      this.decomp = null;
   }

   public void copy(Key k) {
      this.key = k.key();
      this.rank = k.rank();
      this.decomp = k.decomp();
   }

   public void print(int indent) {
      for(int i = 0; i < indent; ++i) {
         System.out.print(" ");
      }

      System.out.println("key: " + this.key + " " + this.rank);
      this.decomp.print(indent + 2);
   }

   public void printKey(int indent) {
      for(int i = 0; i < indent; ++i) {
         System.out.print(" ");
      }

      System.out.println("key: " + this.key + " " + this.rank);
   }

   public String key() {
      return this.key;
   }

   public int rank() {
      return this.rank;
   }

   public DecompList decomp() {
      return this.decomp;
   }
}
