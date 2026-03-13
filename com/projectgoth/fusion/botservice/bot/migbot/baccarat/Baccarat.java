package com.projectgoth.fusion.botservice.bot.migbot.baccarat;

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
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class Baccarat extends Bot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Baccarat.class));
   private int minPlayers = 2;
   private long waitForPlayerInterval = 30000L;
   private long placeBetInterval = 20000L;
   private long drawCardInterval = 5000L;
   private long idleInterval = 1800000L;
   private double minCostToJoinGame = 0.05D;
   private List<Card> deck;
   private Hand bankerHand = new Hand();
   private Hand playerHand = new Hand();
   private Map<String, Baccarat.Bet> playerBets = new ConcurrentHashMap();
   private int round;
   private BotData.BotStateEnum state;
   private double costToJoinGame;
   private long timeLastGameFinished;

   public Baccarat(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDAO) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDAO);
      this.state = BotData.BotStateEnum.NO_GAME;
      this.timeLastGameFinished = System.currentTimeMillis();
      this.minPlayers = this.getIntParameter("MinPlayers", this.minPlayers);
      this.waitForPlayerInterval = this.getLongParameter("WaitForPlayerInterval", this.waitForPlayerInterval);
      this.placeBetInterval = this.getLongParameter("PlaceBetInterval", this.placeBetInterval);
      this.drawCardInterval = this.getLongParameter("DrawCardInterval", this.drawCardInterval);
      this.idleInterval = this.getLongParameter("IdleInterval", this.idleInterval);
      this.minCostToJoinGame = this.getDoubleParameter("MinCostToJoinGame", this.minCostToJoinGame);
      List<String> emoticonHotKeyList = new LinkedList();
      emoticonHotKeyList.addAll(Arrays.asList(this.emoticonHotKeys));
      emoticonHotKeyList.addAll(Arrays.asList(Card.EMOTICONS));
      this.emoticonHotKeys = (String[])emoticonHotKeyList.toArray(new String[emoticonHotKeyList.size()]);
      this.sendChannelMessage("Bot Baccarat added to the room. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
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
         this.sendMessage("Play Baccarat. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Play Baccarat. Enter !j to join the game. " + this.costToJoinGame + " " + "USD", username);
         break;
      case PLAYING:
         this.sendMessage("Baccarat is on now. Get ready for next game", username);
      }

   }

   public void onUserLeaveChannel(String username) {
      synchronized(this) {
         if (this.playerBets.remove(username) != null && this.state != BotData.BotStateEnum.NO_GAME) {
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
      } else if ("!p".equals(messageText)) {
         this.placeBet(username, Baccarat.Bet.PLAYER);
      } else if ("!b".equals(messageText)) {
         this.placeBet(username, Baccarat.Bet.BANKER);
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
         this.bankerHand.clear();
         this.playerHand.clear();
         this.playerBets.clear();
         this.playerBets.put(username, Baccarat.Bet.UNKNOWN);
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
         if (this.playerBets.containsKey(username)) {
            this.sendMessage("You have already joined the game. Please wait for the game to start", username);
         } else if (this.userCanAffordToEnterPot(username, this.costToJoinGame, true) && this.playerBets.put(username, Baccarat.Bet.UNKNOWN) == null) {
            log.info(username + " joined the game");
            this.sendChannelMessage(username + " joined the game");
         }
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   private synchronized void placeBet(String username, Baccarat.Bet bet) {
      switch(this.state) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Game starting soon! Please wait", username);
         break;
      case PLAYING:
         Baccarat.Bet existingBet = (Baccarat.Bet)this.playerBets.get(username);
         if (existingBet == null) {
            this.sendMessage("A game is currently in progress. Please wait for next game", username);
         } else if (existingBet == Baccarat.Bet.UNKNOWN) {
            this.playerBets.put(username, bet);
            this.sendMessage("You have chosen " + bet, username);
         } else {
            this.sendMessage("You have already chosen " + existingBet, username);
         }
      }

   }

   private void waitForMorePlayers() {
      this.sendChannelMessage("Waiting for more players. Enter !j to join the game. " + this.costToJoinGame + " " + "USD");
      this.state = BotData.BotStateEnum.GAME_JOINING;
      this.executor.schedule(new Runnable() {
         public void run() {
            Baccarat.this.chargeAndCountPlayers();
         }
      }, this.waitForPlayerInterval, TimeUnit.MILLISECONDS);
   }

   private synchronized void chargeAndCountPlayers() {
      try {
         if (this.costToJoinGame > 0.0D) {
            if (this.playerBets.size() < this.minPlayers) {
               this.endGame(true);
               this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
               return;
            }

            this.pot = new Pot(this);
            Iterator i = this.playerBets.keySet().iterator();

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

         if (this.playerBets.size() < this.minPlayers) {
            this.endGame(true);
            this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
         } else {
            log.info("New game started in " + this.channel);
            this.waitForPlacingBets();
         }
      } catch (Exception var5) {
         log.error("Unexpected exception occured in chargeAndCountPlayers()", var5);
         this.endGame(true);
         this.sendChannelMessage("Unable to start the game. " + ExceptionHelper.getRawRootMessage(var5));
      }

   }

   private void waitForPlacingBets() {
      this.sendChannelMessage("Round " + this.round++ + ". Dealing cards in " + this.placeBetInterval / 1000L + " seconds. !p for PLAYER or !b for BANKER");
      this.state = BotData.BotStateEnum.PLAYING;
      this.executor.schedule(new Runnable() {
         public void run() {
            Baccarat.this.placeBetsForLazyPlayers();
            Baccarat.this.dealCards();
         }
      }, this.placeBetInterval, TimeUnit.MILLISECONDS);
   }

   private synchronized void placeBetsForLazyPlayers() {
      SecureRandom random = new SecureRandom();
      List<String> players = new LinkedList();
      List<String> bankers = new LinkedList();
      Iterator i$ = this.playerBets.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Baccarat.Bet> e = (Entry)i$.next();
         if (e.getValue() == Baccarat.Bet.UNKNOWN) {
            e.setValue(random.nextBoolean() ? Baccarat.Bet.PLAYER : Baccarat.Bet.BANKER);
            this.sendMessage("You did not make a choice. Bot chose " + e.getValue() + " for you", (String)e.getKey());
         }

         if (e.getValue() == Baccarat.Bet.PLAYER) {
            players.add(e.getKey());
         } else {
            bankers.add(e.getKey());
         }
      }

      this.sendChannelMessage(Baccarat.Bet.PLAYER + " [" + StringUtil.join((Collection)players, ", ") + "] " + Baccarat.Bet.BANKER + " [" + StringUtil.join((Collection)bankers, ", ") + "]");
   }

   private synchronized void dealCards() {
      if (this.deck == null || this.deck.size() < 7) {
         this.deck = Card.newShuffledDeck();
      }

      int playerCount;
      for(playerCount = 0; playerCount < 2; ++playerCount) {
         this.playerHand.add(this.deck.remove(0));
         this.bankerHand.add(this.deck.remove(0));
      }

      playerCount = this.playerHand.count();
      int bankerCount = this.bankerHand.count();
      this.sendChannelMessage("PLAYER: " + this.playerHand + ". BANKER: " + this.bankerHand);
      if (playerCount < 8 && bankerCount < 8) {
         if (playerCount <= 5) {
            this.executor.schedule(new Runnable() {
               public void run() {
                  Baccarat.this.drawThirdCardForPlayer();
               }
            }, this.drawCardInterval, TimeUnit.MILLISECONDS);
         } else if (bankerCount <= 5) {
            this.executor.schedule(new Runnable() {
               public void run() {
                  Baccarat.this.drawThirdCardForBanker();
               }
            }, this.drawCardInterval, TimeUnit.MILLISECONDS);
         } else {
            this.executor.schedule(new Runnable() {
               public void run() {
                  Baccarat.this.tallyUp();
               }
            }, this.drawCardInterval, TimeUnit.MILLISECONDS);
         }
      } else {
         this.tallyUp();
      }

   }

   private synchronized void drawThirdCardForPlayer() {
      Card playerThirdCard = (Card)this.deck.remove(0);
      this.playerHand.add(playerThirdCard);
      this.sendChannelMessage("PLAYER drew third card. " + this.playerHand);
      if (this.bankerNeedsThirdCard(playerThirdCard)) {
         this.executor.schedule(new Runnable() {
            public void run() {
               Baccarat.this.drawThirdCardForBanker();
            }
         }, this.drawCardInterval, TimeUnit.MILLISECONDS);
      } else {
         this.executor.schedule(new Runnable() {
            public void run() {
               Baccarat.this.tallyUp();
            }
         }, this.drawCardInterval, TimeUnit.MILLISECONDS);
      }

   }

   private synchronized void drawThirdCardForBanker() {
      this.bankerHand.add(this.deck.remove(0));
      this.sendChannelMessage("BANKER drew third card. " + this.bankerHand);
      this.executor.schedule(new Runnable() {
         public void run() {
            Baccarat.this.tallyUp();
         }
      }, this.drawCardInterval, TimeUnit.MILLISECONDS);
   }

   private boolean bankerNeedsThirdCard(Card playerThirdCard) {
      switch(playerThirdCard.rank()) {
      case DEUCE:
      case THREE:
         return this.bankerHand.count() <= 4;
      case FOUR:
      case FIVE:
         return this.bankerHand.count() <= 5;
      case SIX:
      case SEVEN:
         return this.bankerHand.count() <= 6;
      case EIGHT:
         return this.bankerHand.count() <= 2;
      case NINE:
      case TEN:
      case JACK:
      case QUEEN:
      case KING:
      case ACE:
         return this.bankerHand.count() <= 3;
      default:
         return false;
      }
   }

   private synchronized void tallyUp() {
      List<String> players = new LinkedList();
      List<String> bankers = new LinkedList();

      Entry e;
      for(Iterator i$ = this.playerBets.entrySet().iterator(); i$.hasNext(); e.setValue(Baccarat.Bet.UNKNOWN)) {
         e = (Entry)i$.next();
         if (e.getValue() == Baccarat.Bet.PLAYER) {
            players.add(e.getKey());
         } else {
            bankers.add(e.getKey());
         }
      }

      int playerCount = this.playerHand.count();
      int bankerCount = this.bankerHand.count();
      Object losers;
      if (playerCount > bankerCount) {
         this.sendChannelMessage("PLAYER wins! " + this.playerHand);
         losers = bankers;
      } else if (playerCount < bankerCount) {
         this.sendChannelMessage("BANKER wins! " + this.bankerHand);
         losers = players;
      } else {
         this.sendChannelMessage("TIE! BANKER and PLAYER on " + playerCount);
         losers = Collections.EMPTY_LIST;
      }

      if (((List)losers).size() != this.playerBets.size()) {
         Iterator i$ = ((List)losers).iterator();

         while(i$.hasNext()) {
            String loser = (String)i$.next();
            this.playerBets.remove(loser);
            if (this.pot != null) {
               try {
                  this.pot.removePlayer(loser);
               } catch (Exception var9) {
                  log.error("Unable to remove " + loser + " from pot " + this.pot.getPotID(), var9);
               }
            }
         }
      }

      if (this.playerBets.size() == 0) {
         this.endGame(false);
         this.sendChannelMessage("No more players left in the game. Enter !start to start a new game");
      } else if (this.playerBets.size() == 1) {
         double payout = this.endGame(false);
         if (payout < 0.0D) {
            this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
         } else {
            this.sendChannelMessageAndPopUp((String)this.playerBets.keySet().iterator().next() + " won " + (new DecimalFormat("0.00")).format(payout) + " " + "USD");
         }

         this.sendChannelMessage("Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
      } else {
         this.sendChannelMessage(this.playerBets.size() + " players left in the game [" + StringUtil.join((Collection)this.playerBets.keySet(), ", ") + "]");
         this.playerHand.clear();
         this.bankerHand.clear();
         this.waitForPlacingBets();
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
         Baccarat bot = new Baccarat(executor, (BotChannelPrx)null, (BotData)null, "EN", "koko", (BotDAO)null);
         bot.waitForPlayerInterval = 2000L;
         bot.placeBetInterval = 6000L;
         bot.minCostToJoinGame = 0.0D;
         bot.onMessage("koko", "!start", System.currentTimeMillis());
         bot.onMessage("kien", "!j", System.currentTimeMillis());
         Thread.sleep(60000L);
         executor.shutdown();
         executor.awaitTermination(60L, TimeUnit.SECONDS);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   private static enum Bet {
      UNKNOWN,
      PLAYER,
      BANKER;
   }
}
