package com.projectgoth.fusion.botservice.bot.migbot.eliza;

public class KeyStack {
   final int stackSize = 20;
   Key[] keyStack = new Key[20];
   int keyTop = 0;

   public void print() {
      System.out.println("Key stack " + this.keyTop);

      for(int i = 0; i < this.keyTop; ++i) {
         this.keyStack[i].printKey(0);
      }

   }

   public int keyTop() {
      return this.keyTop;
   }

   public void reset() {
      this.keyTop = 0;
   }

   public Key key(int n) {
      return n >= 0 && n < this.keyTop ? this.keyStack[n] : null;
   }

   public void pushKey(Key key) {
      if (key == null) {
         System.out.println("push null key");
      } else {
         int i;
         for(i = this.keyTop; i > 0 && key.rank > this.keyStack[i - 1].rank; --i) {
            this.keyStack[i] = this.keyStack[i - 1];
         }

         this.keyStack[i] = key;
         ++this.keyTop;
      }
   }
}
