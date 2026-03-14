package com.projectgoth.fusion.botservice.bot.migbot.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Card implements Comparable {
   public static final String[] EMOTICONS = new String[]{"(2C)", "(3C)", "(4C)", "(5C)", "(6C)", "(7C)", "(8C)", "(9C)", "(TC)", "(JC)", "(QC)", "(KC)", "(AC)", "(2D)", "(3D)", "(4D)", "(5D)", "(6D)", "(7D)", "(8D)", "(9D)", "(TD)", "(JD)", "(QD)", "(KD)", "(AD)", "(2H)", "(3H)", "(4H)", "(5H)", "(6H)", "(7H)", "(8H)", "(9H)", "(TH)", "(JH)", "(QH)", "(KH)", "(AH)", "(2S)", "(3S)", "(4S)", "(5S)", "(6S)", "(7S)", "(8S)", "(9S)", "(TS)", "(JS)", "(QS)", "(KS)", "(AS)"};
   private final Card.Rank rank;
   private final Card.Suit suit;
   private static final List<Card> newDeck = new ArrayList();

   private Card(Card.Rank rank, Card.Suit suit) {
      this.rank = rank;
      this.suit = suit;
   }

   public Card.Rank rank() {
      return this.rank;
   }

   public Card.Suit suit() {
      return this.suit;
   }

   public String toString() {
      return Character.toString(this.rank.toChar()) + this.suit.toChar();
   }

   public static List<Card> newShuffledDeck() {
      List<Card> deck = new ArrayList(newDeck);
      Collections.shuffle(deck);
      return deck;
   }

   public String toEmoticonHotkey() {
      return "(" + this.toString() + ")";
   }

   public int compareTo(Object arg0) {
      Card compareCard = (Card)arg0;
      if (compareCard.rank().getRankOrder() < this.rank.getRankOrder()) {
         return 1;
      } else {
         return compareCard.rank().getRankOrder() > this.rank.getRankOrder() ? -1 : 0;
      }
   }

   public boolean equals(Object obj) {
      if (obj != null && obj instanceof Card) {
         return this.rank.getRankOrder() == ((Card)obj).rank().getRankOrder();
      } else {
         return false;
      }
   }

   public static void main(String[] args) {
      System.out.println("Testing Card class");
      System.out.println("Creating a shuffled deck...");
      List<Card> deck = newShuffledDeck();
      System.out.println("Contents of the shuffled deck:");
      Iterator i$ = deck.iterator();

      Card card;
      while(i$.hasNext()) {
         card = (Card)i$.next();
         System.out.print(card.toEmoticonHotkey() + ", ");
      }

      System.out.println("\n\nFirst card is higher than the second card? ");
      System.out.println(((Card)deck.get(0)).compareTo(deck.get(1)) > 0);
      System.out.println("\nNow sorting the deck in ascending order...");
      Collections.sort(deck);
      System.out.println("Contents of the sorted deck:");
      i$ = deck.iterator();

      while(i$.hasNext()) {
         card = (Card)i$.next();
         System.out.print(card.toEmoticonHotkey() + ", ");
      }

      System.out.println("\nDone");
   }

   static {
      Card.Suit[] arr$ = Card.Suit.values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Card.Suit suit = arr$[i$];
         Card.Rank[] arr$ = Card.Rank.values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Card.Rank rank = arr$[i$];
            newDeck.add(new Card(rank, suit));
         }
      }

   }

   public static enum Suit {
      CLUBS('C'),
      DIAMONDS('D'),
      HEARTS('H'),
      SPADES('S');

      private final char c;

      private Suit(char c) {
         this.c = c;
      }

      public char toChar() {
         return this.c;
      }

      public static Card.Suit fromChar(char c) {
         Card.Suit[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Card.Suit suit = arr$[i$];
            if (suit.c == c) {
               return suit;
            }
         }

         return null;
      }
   }

   public static enum Rank {
      DEUCE(1, '2'),
      THREE(2, '3'),
      FOUR(3, '4'),
      FIVE(4, '5'),
      SIX(5, '6'),
      SEVEN(6, '7'),
      EIGHT(7, '8'),
      NINE(8, '9'),
      TEN(9, 'T'),
      JACK(10, 'J'),
      QUEEN(11, 'Q'),
      KING(12, 'K'),
      ACE(13, 'A');

      private final int rankOrder;
      private final char rankChar;

      private Rank(int rankOrder, char rankChar) {
         this.rankOrder = rankOrder;
         this.rankChar = rankChar;
      }

      public int getRankOrder() {
         return this.rankOrder;
      }

      public char toChar() {
         return this.rankChar;
      }

      public static Card.Rank fromChar(char c) {
         Card.Rank[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Card.Rank rank = arr$[i$];
            if (rank.rankChar == c) {
               return rank;
            }
         }

         return null;
      }
   }
}
