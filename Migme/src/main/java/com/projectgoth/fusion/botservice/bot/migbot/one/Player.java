package com.projectgoth.fusion.botservice.bot.migbot.one;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Player {
   private String name = "";
   private List<Card> cards = new ArrayList();
   boolean calledUno = false;

   public Player(String name) {
      this.name = name;
   }

   public void addCard(Card card) {
      if (null != card) {
         this.cards.add(card);
      }
   }

   public List getCards() {
      return this.cards;
   }

   public void setCalledUno(boolean called) {
      this.calledUno = called;
   }

   public boolean hasCalledUno() {
      return this.calledUno;
   }

   public String toString() {
      StringBuffer playersCards = new StringBuffer(this.name + ": (" + this.cards.size() + " cards) ");
      Iterator i$ = this.cards.iterator();

      while(i$.hasNext()) {
         Card card = (Card)i$.next();
         playersCards.append(" ");
         playersCards.append(card.toString());
      }

      return playersCards.toString();
   }

   public List getHand() {
      List<Card> blue = new ArrayList();
      List<Card> green = new ArrayList();
      List<Card> red = new ArrayList();
      List<Card> yellow = new ArrayList();
      List<Card> wild = new ArrayList();
      Iterator i$ = this.cards.iterator();

      while(i$.hasNext()) {
         Card card = (Card)i$.next();
         switch(card.getColour()) {
         case 1:
            blue.add(card);
            break;
         case 2:
            green.add(card);
            break;
         case 3:
            red.add(card);
            break;
         case 4:
            yellow.add(card);
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         default:
            break;
         case 10:
            wild.add(card);
         }
      }

      Collections.sort(blue);
      Collections.sort(green);
      Collections.sort(red);
      Collections.sort(yellow);
      Collections.sort(wild);
      blue.addAll(green);
      blue.addAll(red);
      blue.addAll(yellow);
      blue.addAll(wild);
      return blue;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String newName) {
      this.name = newName;
   }

   public Card getCard(int cardValue, int cardColour) {
      Iterator i$ = this.cards.iterator();

      Card card;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         card = (Card)i$.next();
      } while((card.getColour() != cardColour || card.getValue() != cardValue) && (card.getValue() != 10 || cardValue != 10) && (card.getValue() != 23 || cardValue != 23));

      return card;
   }

   public boolean hasCardWithValue(int cardValue) {
      Iterator i$ = this.cards.iterator();

      Card card;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         card = (Card)i$.next();
      } while(card.getValue() != cardValue && card.getValue() != 10 && card.getValue() != 23);

      return true;
   }

   public boolean hasCardWithColour(int cardColour) {
      Iterator i$ = this.cards.iterator();

      Card card;
      do {
         if (!i$.hasNext()) {
            return false;
         }

         card = (Card)i$.next();
      } while(card.getColour() != cardColour);

      return true;
   }

   public int getPoints() {
      int val = true;
      int scr = 0;
      Iterator i$ = this.cards.iterator();

      while(true) {
         while(true) {
            while(i$.hasNext()) {
               Card card = (Card)i$.next();
               int val = card.getValue();
               if (val != 21 && val != 22 && val != 20) {
                  if (val != 10 && val != 23) {
                     scr += val;
                  } else {
                     scr += 50;
                  }
               } else {
                  scr += 20;
               }
            }

            return scr;
         }
      }
   }

   public void removeCard(Card card) {
      if (this.cards.contains(card)) {
         this.cards.remove(card);
      }

   }

   public boolean isLastCard() {
      return this.cards.size() == 1;
   }

   public boolean hasUno() {
      return this.cards.size() == 1;
   }

   public boolean hasWon() {
      return this.cards.size() == 0;
   }

   public int cardCount() {
      return this.cards.size();
   }
}
