package com.projectgoth.fusion.botservice.bot.migbot.baccarat;

import com.projectgoth.fusion.botservice.bot.migbot.common.Card;
import java.util.ArrayList;
import java.util.Iterator;

public class Hand extends ArrayList<Card> {
   public int count() {
      int sum = 0;
      Iterator i$ = this.iterator();

      while(i$.hasNext()) {
         Card card = (Card)i$.next();
         switch(card.rank()) {
         case ACE:
            ++sum;
            break;
         case DEUCE:
            sum += 2;
            break;
         case THREE:
            sum += 3;
            break;
         case FOUR:
            sum += 4;
            break;
         case FIVE:
            sum += 5;
            break;
         case SIX:
            sum += 6;
            break;
         case SEVEN:
            sum += 7;
            break;
         case EIGHT:
            sum += 8;
            break;
         case NINE:
            sum += 9;
         case TEN:
         case JACK:
         case QUEEN:
         case KING:
         }
      }

      return sum % 10;
   }

   public String toEmoticonHotKeys() {
      StringBuilder builder = new StringBuilder();
      Iterator i$ = this.iterator();

      while(i$.hasNext()) {
         Card card = (Card)i$.next();
         builder.append(card.toEmoticonHotkey());
      }

      return builder.toString();
   }

   public String toString() {
      StringBuilder builder = new StringBuilder(this.toEmoticonHotKeys());
      builder.append(" ");
      int count = this.count();
      if (count >= 8 && this.size() == 2) {
         builder.append("Natural ");
      }

      return builder.append(count).toString();
   }
}
