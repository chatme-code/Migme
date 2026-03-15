package com.projectgoth.fusion.botservice.bot.migbot.eliza;

import java.util.Vector;

public class ReasembList extends Vector {
   public void add(String reasmb) {
      this.addElement(reasmb);
   }

   public void print(int indent) {
      for(int i = 0; i < this.size(); ++i) {
         for(int j = 0; j < indent; ++j) {
            System.out.print(" ");
         }

         String s = (String)this.elementAt(i);
         System.out.println("reasemb: " + s);
      }

   }
}
