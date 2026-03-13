package com.projectgoth.fusion.botservice.bot.migbot.blackjack;

import com.projectgoth.fusion.botservice.bot.migbot.common.Card;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Hand extends ArrayList<Card> {
   public List<Integer> count() {
      int minSum = 0;
      boolean hasAce = false;
      Iterator i$ = this.iterator();

      while(i$.hasNext()) {
         Card card = (Card)i$.next();
         switch(card.rank()) {
         case ACE:
            ++minSum;
            hasAce = true;
            break;
         case DEUCE:
            minSum += 2;
            break;
         case THREE:
            minSum += 3;
            break;
         case FOUR:
            minSum += 4;
            break;
         case FIVE:
            minSum += 5;
            break;
         case SIX:
            minSum += 6;
            break;
         case SEVEN:
            minSum += 7;
            break;
         case EIGHT:
            minSum += 8;
            break;
         case NINE:
            minSum += 9;
            break;
         case TEN:
         case JACK:
         case QUEEN:
         case KING:
            minSum += 10;
         }
      }

      List<Integer> possibleCounts = new ArrayList();
      possibleCounts.add(minSum);
      if (hasAce && minSum + 10 <= 21) {
         possibleCounts.add(0, minSum + 10);
      }

      return possibleCounts;
   }

   public int highestCount() {
      List<Integer> possibleCounts = this.count();
      return (Integer)possibleCounts.get(0);
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
}
