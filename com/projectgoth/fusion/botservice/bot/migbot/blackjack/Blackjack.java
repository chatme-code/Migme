package com.projectgoth.fusion.botservice.bot.migbot.blackjack;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.botservice.bot.Pot;
import com.projectgoth.fusion.botservice.bot.migbot.common.Card;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ExceptionHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class Blackjack extends Bot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Blackjack.class));
   private int minPlayers = 2;
   private int maxPlayers = 6;
   private long waitForPlayerInterval = 30000L;
   private long decisionInterval = 20000L;
   private long revealDealerCardInterval = 5000L;
   private long idleInterval = 1800000L;
   private double minCostToJoinGame = 0.05D;
   private List<Card> deck;
   private Hand dealerHand = new Hand();
   private Map<String, Hand> playerHands = new ConcurrentHashMap();
   private String currentPlayer;
   private Iterator<String> playerIterator;
   private ScheduledFuture decisionTimer;
   private int round;
   private BotData.BotStateEnum state;
   private double costToJoinGame;
   private long timeLastGameFinished;

   public Blackjack(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDAO) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDAO);
      this.state = BotData.BotStateEnum.NO_GAME;
      this.timeLastGameFinished = System.currentTimeMillis();
      this.minPlayers = this.getIntParameter("MinPlayers", this.minPlayers);
      this.maxPlayers = this.getIntParameter("MaxPlayers", this.maxPlayers);
      this.waitForPlayerInterval = this.getLongParameter("WaitForPlayerInterval", this.waitForPlayerInterval);
      this.decisionInterval = this.getLongParameter("DecisionInterval", this.decisionInterval);
      this.revealDealerCardInterval = this.getLongParameter("RevealDealerCardInterval", this.revealDealerCardInterval);
      this.idleInterval = this.getLongParameter("IdleInterval", this.idleInterval);
      this.minCostToJoinGame = this.getDoubleParameter("MinCostToJoinGame", this.minCostToJoinGame);
      List<String> emoticonHotKeyList = new LinkedList();
      emoticonHotKeyList.addAll(Arrays.asList(this.emoticonHotKeys));
      emoticonHotKeyList.addAll(Arrays.asList(Card.EMOTICONS));
      this.emoticonHotKeys = (String[])emoticonHotKeyList.toArray(new String[emoticonHotKeyList.size()]);
      this.sendChannelMessage("Bot Blackjack added to the room. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
   }

   public boolean isIdle() {
      return this.state == BotData.BotStateEnum.NO_GAME && System.currentTimeMillis() - this.timeLastGameFinished > this.idleInterval;
   }

   public boolean canBeStoppedNow() {
      return (this.state != BotData.BotStateEnum.PLAYING || this.pot == null) && this.state != BotData.BotStateEnum.GAME_JOINING && this.state != BotData.BotStateEnum.GAME_STARTING;
   }

   public void stopBot() {
      synchronized(this) {
         this.endGame(true);
      }
   }

   public void onUserJoinChannel(String username) {
      switch(this.state) {
      case NO_GAME:
         this.sendMessage("Play Blackjack. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Play Blackjack. Enter !j to join the game. " + this.costToJoinGame + " " + "USD", username);
         break;
      case PLAYING:
         this.sendMessage("Blackjack is on now. Get ready for next game", username);
      }

   }

   public void onUserLeaveChannel(String username) {
      synchronized(this) {
         if (this.playerHands.remove(username) != null && this.state != BotData.BotStateEnum.NO_GAME) {
            this.sendChannelMessage(username + " left the game");
            if (this.pot != null) {
               try {
                  this.pot.removePlayer(username);
               } catch (Exception var5) {
                  log.error("Unable to remove " + username + " from pot " + this.pot.getPotID(), var5);
               }
            }
         }

      }
   }

   public void onMessage(String username, String messageText, long receivedTimestamp) {
      messageText = messageText.toLowerCase().trim();
      if (messageText.startsWith("!start")) {
         this.startNewGame(username, messageText);
      } else if ("!j".equals(messageText)) {
         this.joinGame(username);
      } else if ("!h".equals(messageText)) {
         this.playerSays(username, Blackjack.Decision.HIT);
      } else if ("!s".equals(messageText)) {
         this.playerSays(username, Blackjack.Decision.STAND);
      } else {
         this.sendMessage(messageText + " is not a valid command", username);
      }

   }

   private synchronized void startNewGame(String username, String messageText) {
      switch(this.state) {
      case NO_GAME:
         String[] params = messageText.split(" ");

         try {
            this.costToJoinGame = params.length > 1 ? Double.parseDouble(params[1]) / 100.0D : this.minCostToJoinGame;
            if (this.costToJoinGame < this.minCostToJoinGame) {
               this.sendMessage("Minimum amount to start a game is " + this.minCostToJoinGame + " " + "USD", username);
               return;
            }

            if (!this.userCanAffordToEnterPot(username, this.costToJoinGame, true)) {
               return;
            }
         } catch (NumberFormatException var5) {
            this.sendMessage(params[1] + " is not a valid amount", username);
            return;
         }

         this.round = 1;
         this.dealerHand.clear();
         this.playerHands.clear();
         this.playerHands.put(username, new Hand());
         this.currentPlayer = null;
         this.playerIterator = null;
         this.sendChannelMessage(username + " started a new game");
         this.waitForMorePlayers();
         break;
      case GAME_JOINING:
         this.sendMessage("Enter !j to join the game. " + this.costToJoinGame + " " + "USD", username);
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   private synchronized void joinGame(String username) {
      switch(this.state) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>", username);
         break;
      case GAME_JOINING:
         if (this.playerHands.containsKey(username)) {
            this.sendMessage("You have already joined the game. Please wait for the game to start", username);
         }

         if (this.playerHands.size() >= this.maxPlayers) {
            this.sendMessage("Game is currently full. Please wait for next game", username);
         } else if (this.userCanAffordToEnterPot(username, this.costToJoinGame, true) && this.playerHands.put(username, new Hand()) == null) {
            log.info(username + " joined the game");
            this.sendChannelMessage(username + " joined the game");
         }
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   private void waitForMorePlayers() {
      this.sendChannelMessage("Waiting for more players. Enter !j to join the game. " + this.costToJoinGame + " " + "USD");
      this.state = BotData.BotStateEnum.GAME_JOINING;
      this.executor.schedule(new Runnable() {
         public void run() {
            Blackjack.this.chargeAndCountPlayers();
         }
      }, this.waitForPlayerInterval, TimeUnit.MILLISECONDS);
   }

   private synchronized void chargeAndCountPlayers() {
      try {
         if (this.costToJoinGame > 0.0D) {
            if (this.playerHands.size() < this.minPlayers) {
               this.endGame(true);
               this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
               return;
            }

            this.pot = new Pot(this);
            Iterator i = this.playerHands.keySet().iterator();

            while(i.hasNext()) {
               String player = (String)i.next();

               try {
                  this.pot.enterPlayer(player, this.costToJoinGame, "USD");
               } catch (Exception var4) {
                  i.remove();
                  this.sendMessage("Unable to join you to the game " + ExceptionHelper.getRawRootMessage(var4), player);
               }
            }
         }

         if (this.playerHands.size() < this.minPlayers) {
            this.endGame(true);
            this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
         } else {
            log.info("New game started in " + this.channel);
            this.dealCards();
            this.goAroundTheTable();
         }
      } catch (Exception var5) {
         log.error("Unexpected exception occured in chargeAndCountPlayers()", var5);
         this.endGame(true);
         this.sendChannelMessage("Unable to start the game. " + ExceptionHelper.getRawRootMessage(var5));
      }

   }

   private void dealCards() {
      this.sendChannelMessage("Round " + this.round++ + ". Dealing cards!");
      this.deck = Card.newShuffledDeck();

      Iterator i$;
      for(int i = 0; i < 2; ++i) {
         i$ = this.playerHands.values().iterator();

         while(i$.hasNext()) {
            List<Card> playerHand = (List)i$.next();
            playerHand.add(this.deck.remove(0));
         }

         this.dealerHand.add(this.deck.remove(0));
      }

      StringBuilder cardsDealt = new StringBuilder();
      cardsDealt.append("DEALER ").append(((Card)this.dealerHand.get(0)).toEmoticonHotkey());
      i$ = this.playerHands.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Hand> e = (Entry)i$.next();
         cardsDealt.append(", ").append((String)e.getKey()).append(" ").append(((Hand)e.getValue()).toEmoticonHotKeys());
      }

      this.sendChannelMessage(cardsDealt.toString());
      this.state = BotData.BotStateEnum.PLAYING;
   }

   private void goAroundTheTable() {
      if (this.playerIterator == null) {
         this.playerIterator = this.playerHands.keySet().iterator();
      }

      while(this.playerIterator.hasNext()) {
         this.currentPlayer = (String)this.playerIterator.next();
         Hand playerHand = (Hand)this.playerHands.get(this.currentPlayer);
         if (playerHand != null) {
            List<Integer> possibleCounts = playerHand.count();
            int count = (Integer)possibleCounts.get(0);
            if (count != 21) {
               if (possibleCounts.size() == 1) {
                  this.sendChannelMessage(this.currentPlayer + " " + playerHand.toEmoticonHotKeys() + ", " + possibleCounts.get(0) + ", !h to hit or !s to stand");
               } else if (possibleCounts.size() > 1) {
                  this.sendChannelMessage(this.currentPlayer + " " + playerHand.toEmoticonHotKeys() + ", " + possibleCounts.get(1) + " or " + possibleCounts.get(0) + ", !h to hit or !s to stand");
               }

               this.decisionTimer = this.executor.schedule(new Runnable() {
                  public void run() {
                     Blackjack.this.decisionTimeUp(Blackjack.this.currentPlayer);
                  }
               }, this.decisionInterval, TimeUnit.MILLISECONDS);
               return;
            }

            this.sendChannelMessage(this.currentPlayer + " " + playerHand.toEmoticonHotKeys() + ", Blackjack!");
         }
      }

      if (!this.playerIterator.hasNext()) {
         this.currentPlayer = null;
         this.playerIterator = null;
         final int count = this.dealerHand.highestCount();
         this.sendChannelMessage("DEALER on " + count + " " + this.dealerHand.toEmoticonHotKeys());
         if (count < 17) {
            this.executor.schedule(new Runnable() {
               public void run() {
                  Blackjack.this.drawTo17();
               }
            }, this.revealDealerCardInterval, TimeUnit.MILLISECONDS);
         } else {
            this.executor.schedule(new Runnable() {
               public void run() {
                  Blackjack.this.tallyUp(count);
               }
            }, this.revealDealerCardInterval, TimeUnit.MILLISECONDS);
         }
      }

   }

   private synchronized void playerSays(String username, Blackjack.Decision decision) {
      switch(this.state) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Game starting soon! Please wait", username);
         break;
      case PLAYING:
         if (!username.equals(this.currentPlayer)) {
            this.sendMessage("It's not your turn", username);
            return;
         }

         if (this.decisionTimer != null) {
            this.decisionTimer.cancel(true);
         }

         Hand playerHand = (Hand)this.playerHands.get(username);
         if (playerHand == null) {
            this.goAroundTheTable();
            return;
         }

         if (decision == Blackjack.Decision.HIT) {
            this.hit(username, playerHand);
         } else if (decision == Blackjack.Decision.STAND) {
            this.stand(username, playerHand);
         } else {
            this.sendMessage("Unexpected decision " + decision, username);
         }
      }

   }

   private void hit(String username, Hand playerHand) {
      playerHand.add(this.deck.remove(0));
      List<Integer> possibleCounts = playerHand.count();
      int count = (Integer)possibleCounts.get(0);
      if (count == 21) {
         this.sendChannelMessage(username + " HIT and 21! " + playerHand.toEmoticonHotKeys());
         this.goAroundTheTable();
      } else if (count > 21) {
         this.sendChannelMessage(username + " HIT and BUST! " + playerHand.toEmoticonHotKeys() + ", " + possibleCounts.get(0));
         this.playerHands.remove(username);
         if (this.pot != null) {
            try {
               this.pot.removePlayer(username);
            } catch (Exception var6) {
               log.error("Unable to remove " + username + " from pot " + this.pot.getPotID(), var6);
            }
         }

         this.goAroundTheTable();
      } else {
         if (possibleCounts.size() == 1) {
            this.sendChannelMessage(username + " HIT " + playerHand.toEmoticonHotKeys() + ", " + possibleCounts.get(0) + ", !h to hit or !s to stand");
         } else if (possibleCounts.size() > 1) {
            this.sendChannelMessage(username + " HIT " + playerHand.toEmoticonHotKeys() + ", " + possibleCounts.get(1) + " or " + possibleCounts.get(0) + ", !h to hit or !s to stand");
         }

         this.decisionTimer = this.executor.schedule(new Runnable() {
            public void run() {
               Blackjack.this.decisionTimeUp(Blackjack.this.currentPlayer);
            }
         }, this.decisionInterval, TimeUnit.MILLISECONDS);
      }

   }

   private void stand(String username, Hand playerHand) {
      this.sendChannelMessage(username + " STAND on " + playerHand.highestCount() + " " + playerHand.toEmoticonHotKeys());
      this.goAroundTheTable();
   }

   private synchronized void decisionTimeUp(String username) {
      this.goAroundTheTable();
   }

   private synchronized void drawTo17() {
      this.dealerHand.add(this.deck.remove(0));
      List<Integer> possibleCounts = this.dealerHand.count();
      final int count = (Integer)possibleCounts.get(0);
      if (count < 17) {
         if (possibleCounts.size() == 1) {
            this.sendChannelMessage("DEALER HIT " + this.dealerHand.toEmoticonHotKeys() + ", " + possibleCounts.get(0));
         } else if (possibleCounts.size() > 1) {
            this.sendChannelMessage("DEALER HIT " + this.dealerHand.toEmoticonHotKeys() + ", " + possibleCounts.get(1) + " or " + possibleCounts.get(0));
         }

         this.decisionTimer = this.executor.schedule(new Runnable() {
            public void run() {
               Blackjack.this.drawTo17();
            }
         }, this.revealDealerCardInterval, TimeUnit.MILLISECONDS);
      } else if (count <= 21) {
         this.sendChannelMessage("DEALER HIT and STAND " + this.dealerHand.toEmoticonHotKeys() + ", " + count);
         this.executor.schedule(new Runnable() {
            public void run() {
               Blackjack.this.tallyUp(count);
            }
         }, this.revealDealerCardInterval, TimeUnit.MILLISECONDS);
      } else {
         this.sendChannelMessage("DEALER HIT and BUST " + this.dealerHand.toEmoticonHotKeys() + ", " + count);
         this.executor.schedule(new Runnable() {
            public void run() {
               Blackjack.this.tallyUp(count);
            }
         }, this.revealDealerCardInterval, TimeUnit.MILLISECONDS);
      }

   }

   private void tallyUp(int dealerCount) {
      List<String> losers = new LinkedList();

      Iterator i$;
      Entry e;
      for(i$ = this.playerHands.entrySet().iterator(); i$.hasNext(); ((Hand)e.getValue()).clear()) {
         e = (Entry)i$.next();
         int playerCount = ((Hand)e.getValue()).highestCount();
         if (dealerCount <= 21 && playerCount < dealerCount || playerCount > 21) {
            losers.add(e.getKey());
         }
      }

      this.dealerHand.clear();
      if (losers.size() != this.playerHands.size()) {
         i$ = losers.iterator();

         while(i$.hasNext()) {
            String loser = (String)i$.next();
            this.playerHands.remove(loser);
            if (this.pot != null) {
               try {
                  this.pot.removePlayer(loser);
               } catch (Exception var6) {
                  log.error("Unable to remove " + loser + " from pot " + this.pot.getPotID(), var6);
               }
            }
         }
      }

      if (this.playerHands.size() == 0) {
         this.endGame(false);
         this.sendChannelMessage("No more players left in the game. Enter !start to start a new game");
      } else if (this.playerHands.size() == 1) {
         double payout = this.endGame(false);
         if (payout < 0.0D) {
            this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
         } else {
            this.sendChannelMessageAndPopUp((String)this.playerHands.keySet().iterator().next() + " won " + (new DecimalFormat("0.00")).format(payout) + " " + "USD");
         }

         this.sendChannelMessage("Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
      } else {
         this.sendChannelMessage(this.playerHands.size() + " players left in the game [" + StringUtil.join((Collection)this.playerHands.keySet(), ", ") + "]");
         this.dealCards();
         this.goAroundTheTable();
      }

   }

   private double endGame(boolean cancelPot) {
      double payout = 0.0D;
      if (this.pot != null) {
         if (cancelPot) {
            try {
               this.pot.cancel();
            } catch (Exception var6) {
               log.error("Unable to cancel pot " + this.pot.getPotID(), var6);
            }
         } else {
            try {
               Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
               payout = this.pot.payout(true);
               payout = accountEJB.convertCurrency(payout, "AUD", "USD");
            } catch (Exception var5) {
               log.error("Unable to payout pot " + this.pot.getPotID(), var5);
               payout = -1.0D;
            }
         }
      }

      this.timeLastGameFinished = System.currentTimeMillis();
      this.state = BotData.BotStateEnum.NO_GAME;
      return payout;
   }

   public static void main(String[] args) {
      try {
         ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
         Blackjack bot = new Blackjack(executor, (BotChannelPrx)null, (BotData)null, "EN", "koko", (BotDAO)null);
         bot.waitForPlayerInterval = 2000L;
         bot.decisionInterval = 2000L;
         bot.minCostToJoinGame = 0.0D;
         bot.onMessage("koko", "!start", System.currentTimeMillis());
         bot.onMessage("bruce", "!j", System.currentTimeMillis());
         Thread.sleep(3000L);
         bot.onMessage("dave", "!h", System.currentTimeMillis());
         bot.onMessage("dave", "!s", System.currentTimeMillis());
         Thread.sleep(60000L);
         executor.shutdown();
         executor.awaitTermination(60L, TimeUnit.SECONDS);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   private static enum Decision {
      HIT,
      STAND;
   }
}
