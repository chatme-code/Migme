package com.projectgoth.fusion.botservice.bot.migbot.cricket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

public class Deck {
   private LinkedList<Deck.Card> deck = new LinkedList();
   private HashMap<Deck.Card, Integer> combination = new HashMap(12);

   public Deck() {
      this.combination.put(Deck.Card.ONE, 45);
      this.combination.put(Deck.Card.TWO, 39);
      this.combination.put(Deck.Card.THREE, 3);
      this.combination.put(Deck.Card.FOUR, 21);
      this.combination.put(Deck.Card.SIX, 12);
      this.combination.put(Deck.Card.BOWLED, 4);
      this.combination.put(Deck.Card.STUMPED, 2);
      this.combination.put(Deck.Card.CATCH, 6);
      this.combination.put(Deck.Card.HIT_WICKET, 1);
      this.combination.put(Deck.Card.LBW, 3);
      this.combination.put(Deck.Card.RUN_OUT, 2);
      this.combination.put(Deck.Card.THIRD_UMPIRE, 3);
   }

   public void loadConfig() {
   }

   public void init() {
      Iterator iterator = this.combination.entrySet().iterator();

      while(iterator.hasNext()) {
         Entry<Deck.Card, Integer> e = (Entry)iterator.next();
         Deck.Card card = (Deck.Card)e.getKey();
         int qty = (Integer)e.getValue();

         for(int i = 0; i < qty; ++i) {
            this.deck.add(card);
         }
      }

      this.shuffle();
   }

   public void shuffle() {
      Collections.shuffle(this.deck);
   }

   public Deck.Card draw() {
      return this.deck.size() <= 0 ? null : (Deck.Card)this.deck.poll();
   }

   public static enum Card {
      ONE("1", "One", "(cr1)"),
      TWO("2", "Two", "(cr2)"),
      THREE("3", "Three", "(cr3)"),
      FOUR("4", "Four", "(cr4)"),
      SIX("6", "Six", "(cr6)"),
      BOWLED("O", "Bowled", "(crBowled)"),
      STUMPED("O", "Stumped", "(crStumped)"),
      CATCH("O", "Catch", "(crCatch)"),
      HIT_WICKET("O", "Hit Wicket", "(crHitWicket)"),
      LBW("O", "Leg Before Wicket", "(crLBW)"),
      RUN_OUT("O", "Run Out", "(crRunOut)"),
      THIRD_UMPIRE("U", "Third Umpire", "(crThirdUmpire)");

      private String type;
      private String name;
      private String emoticonKey;

      private Card(String type, String name, String emoticonKey) {
         this.type = type;
         this.name = name;
         this.emoticonKey = emoticonKey;
      }

      public String getType() {
         return this.type;
      }

      public String getName() {
         return this.name;
      }

      public String getEmoticonKey() {
         return this.emoticonKey;
      }
   }
}
