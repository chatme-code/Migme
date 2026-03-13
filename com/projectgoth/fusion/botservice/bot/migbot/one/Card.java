package com.projectgoth.fusion.botservice.bot.migbot.one;

import java.util.HashMap;
import java.util.Map;

public class Card implements Comparable {
   public static Map<Integer, String> colorEmoticonMappings = new HashMap();
   private int colour = -1;
   private int value = -1;

   public Card(int colour, int value) {
      this.colour = colour;
      this.value = value;
   }

   public int getColour() {
      return this.colour;
   }

   public int getValue() {
      return this.value;
   }

   public String toString() {
      StringBuffer cardStr = new StringBuffer();
      switch(this.value) {
      case 10:
         cardStr.append("w");
         break;
      case 20:
         cardStr.append("d2");
         break;
      case 21:
         cardStr.append("r");
         break;
      case 22:
         cardStr.append("s");
         break;
      case 23:
         cardStr.append("wd4");
         break;
      case 99:
         cardStr.append("*");
         break;
      default:
         cardStr.append(this.value);
      }

      switch(this.colour) {
      case 1:
      case 2:
      case 3:
      case 4:
         cardStr.append((String)colorEmoticonMappings.get(this.colour));
         break;
      case 5:
      case 6:
      case 7:
      case 8:
      case 9:
      case 10:
      default:
         cardStr.append("");
      }

      return cardStr.toString();
   }

   public boolean equals(Object cardObj) {
      if (cardObj != null && cardObj instanceof Card) {
         Card card = (Card)cardObj;
         return this.value == card.value && this.colour == card.colour;
      } else {
         return false;
      }
   }

   public int compareTo(Object cardObj) {
      Card card = (Card)cardObj;
      if (this.value < card.getValue()) {
         return -1;
      } else {
         return this.value <= card.getValue() ? 0 : 1;
      }
   }

   static {
      colorEmoticonMappings.put(1, "(uno_blue)");
      colorEmoticonMappings.put(2, "(uno_green)");
      colorEmoticonMappings.put(3, "(uno_red)");
      colorEmoticonMappings.put(4, "(uno_yellow)");
   }
}
