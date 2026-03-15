package com.projectgoth.fusion.botservice.bot.migbot.eliza;

import java.util.Vector;

public class KeyList extends Vector {
   public void add(String key, int rank, DecompList decomp) {
      this.addElement(new Key(key, rank, decomp));
   }

   public void print(int indent) {
      for(int i = 0; i < this.size(); ++i) {
         Key k = (Key)this.elementAt(i);
         k.print(indent);
      }

   }

   Key getKey(String s) {
      for(int i = 0; i < this.size(); ++i) {
         Key key = (Key)this.elementAt(i);
         if (s.equals(key.key())) {
            return key;
         }
      }

      return null;
   }

   public void buildKeyStack(KeyStack stack, String s) {
      stack.reset();
      s = EString.trim(s);

      Key k;
      for(String[] lines = new String[2]; EString.match(s, "* *", lines); s = lines[1]) {
         k = this.getKey(lines[0]);
         if (k != null) {
            stack.pushKey(k);
         }
      }

      k = this.getKey(s);
      if (k != null) {
         stack.pushKey(k);
      }

   }
}
