package com.projectgoth.fusion.botservice.bot.migbot.cricket;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.botservice.bot.Pot;
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
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class Cricket extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Cricket.class));
   public static final String AMOUNT_JOIN_POT = "amountJoinPot";
   public static final String MIN_PLAYERS = "minPlayers";
   public static final String MAX_PLAYERS = "maxPlayers";
   public static final String MIN_RANGE = "minRange";
   public static final String MAX_RANGE = "maxRange";
   public static final String FINAL_ROUND = "finalRound";
   public static final String TIMER_JOIN_GAME = "timeToJoinGame";
   public static final String TIMER_CANCEL_GAME = "timeToCancel";
   public static final String TIMER_END_ROUND = "timeToEndRound";
   public static final String TIMER_DECISION_INTERVAL = "decisionInterval";
   double minAmountJoinPot = 5.0D;
   double amountJoinPot = 5.0D;
   long timeToJoinGame = 60000L;
   long timeToCancel = 20000L;
   long timeToEndRound = 20000L;
   long decisionInterval = 20000L;
   long waitBetweenRoundInterval = 5000L;
   public int minPlayers = 2;
   public int maxPlayers = 10;
   public int finalRound = 6;
   public int numRollsPerRound = 3;
   private int round = 0;
   private boolean waitRound = false;
   private BotData.BotStateEnum gameState;
   private long idleInterval;
   private long timeLastGameFinished;
   private String startPlayer;
   private static DecimalFormat decimalFormat = new DecimalFormat("0.00");
   private Map<String, Integer> playerScores;
   private Map<String, Integer> playerThirdUmpires;
   private Map<String, Deck> playerDecks;
   private Map<String, Deck.Card> playerDrawnCards;
   private List<String> playerOuts;
   private ScheduledFuture decisionTimer;
   private ScheduledFuture roundTimer;
   private ScheduledFuture waitingPlayersTimer;
   private ScheduledFuture startingTimer;
   private static final String COMMAND_BOWL = "!d";
   private static final String COMMAND_CANCEL = "!n";

   public Cricket(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.idleInterval = 1800000L;
      this.timeLastGameFinished = System.currentTimeMillis();
      this.startPlayer = "";
      this.loadGameConfig();
      this.playerScores = new HashMap(this.maxPlayers);
      this.playerThirdUmpires = new HashMap(this.maxPlayers);
      this.playerDecks = new HashMap(this.maxPlayers);
      this.playerDrawnCards = new HashMap(this.maxPlayers);
      this.playerOuts = new ArrayList(this.maxPlayers);
      log.info(botData.getDisplayName() + " [" + this.instanceID + "] added to channel [" + this.channel + "]");
      this.sendChannelMessage(this.createMessage("BOT_ADDED", (String)null));
      this.sendChannelMessage(this.createMessage("GAME_STATE_DEFAULT_AMOUNT", (String)null));
   }

   private void loadGameConfig() {
      this.minAmountJoinPot = this.getDoubleParameter("amountJoinPot", this.minAmountJoinPot);
      this.minPlayers = this.getIntParameter("minPlayers", this.minPlayers);
      this.maxPlayers = this.getIntParameter("maxPlayers", this.maxPlayers);
      this.finalRound = this.getIntParameter("finalRound", this.finalRound);
      this.timeToJoinGame = this.getLongParameter("timeToJoinGame", this.timeToJoinGame);
      this.timeToCancel = this.getLongParameter("timeToCancel", this.timeToCancel);
      this.timeToEndRound = this.getLongParameter("timeToEndRound", this.timeToEndRound);
      this.decisionInterval = this.getLongParameter("decisionInterval", this.decisionInterval);
   }

   private String createMessage(String messageKey, String player) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Looking for messageKey: " + messageKey);
         }

         String messageToSend = (String)this.messages.get(messageKey);
         if (messageToSend == null) {
            messageToSend = messageKey;
         }

         messageToSend = messageToSend.replaceAll("BOTNAME", this.botData.getDisplayName());
         messageToSend = messageToSend.replaceAll("TIMER_JOIN", "" + this.timeToJoinGame / 1000L);
         messageToSend = messageToSend.replaceAll("TIMER_END", "" + this.timeToEndRound / 1000L);
         messageToSend = messageToSend.replaceAll("CMD_NO", "!n");
         messageToSend = messageToSend.replaceAll("CMD_JOIN", "!j");
         messageToSend = messageToSend.replaceAll("CMD_START", "!start");
         messageToSend = messageToSend.replaceAll("MINPLAYERS", this.minPlayers + "");
         messageToSend = messageToSend.replaceAll("MAXPLAYERS", this.maxPlayers + "");
         if (player != null) {
            messageToSend = messageToSend.replaceAll("PLAYER", player);
         }

         messageToSend = messageToSend.replaceAll("CURRENCY", "USD");
         messageToSend = messageToSend.replaceAll("AMOUNT_POT", this.amountJoinPot / 100.0D + "");
         messageToSend = messageToSend.replaceAll("CUSTOM_MIN_AMOUNT", this.amountJoinPot + 1.0D + "");
         if (this.round != 0) {
            messageToSend = messageToSend.replaceAll("ROUND_NUMBER", this.round + "");
         }

         if (log.isDebugEnabled()) {
            log.debug("Found message for key-(" + messageKey + ")->" + messageToSend);
         }

         return messageToSend;
      } catch (NullPointerException var4) {
         log.error("Outgoing message could not be created, key = " + messageKey, var4);
         return "";
      }
   }

   public boolean isIdle() {
      return this.gameState == BotData.BotStateEnum.NO_GAME && System.currentTimeMillis() - this.timeLastGameFinished > this.idleInterval;
   }

   public boolean canBeStoppedNow() {
      return (this.gameState != BotData.BotStateEnum.PLAYING || this.pot == null) && this.gameState != BotData.BotStateEnum.GAME_JOINING && this.gameState != BotData.BotStateEnum.GAME_STARTING;
   }

   public void stopBot() {
      synchronized(this) {
         this.endGame(true);
         this.gameState = BotData.BotStateEnum.NO_GAME;
      }
   }

   public void onUserJoinChannel(String username) {
      switch(this.gameState) {
      case NO_GAME:
         this.sendMessage(this.createMessage("NO_GAME_STATE", username), username);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("GAME_STARTING_STATE", username), username);
         break;
      case GAME_JOINING:
         this.sendMessage(this.createMessage("GAME_JOINING_STATE", username), username);
         break;
      case PLAYING:
         this.sendMessage(this.createMessage("GAME_PLAYING_STATE", username), username);
      }

   }

   public void onUserLeaveChannel(String username) {
      synchronized(this) {
         switch(this.gameState) {
         case GAME_JOINING:
            if (this.playerScores.containsKey(username)) {
               this.playerScores.remove(username);
               this.playerDecks.remove(username);
               this.playerThirdUmpires.remove(username);
            }
         default:
         }
      }
   }

   public synchronized void onMessage(String username, String messageText, long receivedTimestamp) {
      messageText = messageText.toLowerCase().trim();
      if (!"!start".equals(messageText) && (!messageText.startsWith("!start") || messageText.split(" ").length != 2)) {
         if ("!n".equals(messageText)) {
            if (this.startPlayer.equals(username)) {
               this.cancelGame(username);
            } else {
               this.sendMessage("Only " + this.startPlayer + " can cancel the pot", username);
            }
         } else if ("!j".equals(messageText)) {
            this.joinGame(username);
         } else if ("!d".equals(messageText)) {
            this.bowl(username, false);
         } else {
            this.sendMessage(messageText + " is not a valid command.", username);
         }
      } else {
         this.startNewGame(username, messageText);
      }

   }

   public void startNewGame(final String username, String messageText) {
      switch(this.gameState) {
      case NO_GAME:
         this.amountJoinPot = this.minAmountJoinPot;
         if (messageText.length() > "!start".length()) {
            try {
               Double amount = Double.parseDouble(messageText.substring("!start".length() + 1));
               if (amount < this.minAmountJoinPot) {
                  this.sendMessage(this.createMessage("PLAYER: Invalid amount. Custom amount has to be CURRENCY 0.05 or more (e.g. !start 5) ", username), username);
                  return;
               }

               this.amountJoinPot = amount;
            } catch (NumberFormatException var4) {
               this.sendMessage(this.createMessage("PLAYER: Invalid amount. Custom amount has to be in integer (e.g. !start 5) ", username), username);
               return;
            }
         }

         if (!this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, true)) {
            return;
         }

         this.startPlayer = username;
         this.sendMessage(this.createMessage("PLAYER: added to game. Charges apply. CURRENCY AMOUNT_POT. Create/enter pot. !n to cancel. " + this.timeToCancel / 1000L + " seconds.", username), username);
         this.gameState = BotData.BotStateEnum.GAME_STARTING;
         this.startingTimer = this.executor.schedule(new Runnable() {
            public void run() {
               Cricket.this.initGame();
               Cricket.this.playerScores.put(username, 0);
               Cricket.this.playerThirdUmpires.put(username, 0);
               Deck deck = new Deck();
               deck.init();
               Cricket.this.playerDecks.put(username, deck);
               Cricket.this.sendChannelMessage(Cricket.this.createMessage("GAME_STARTED_STATE", username));
               Cricket.this.waitForMorePlayers();
            }
         }, this.timeToCancel, TimeUnit.MILLISECONDS);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("GAME_STARTING_STATE", username), username);
         break;
      case GAME_JOINING:
         this.sendMessage(this.createMessage("GAME_JOINING_STATE", username), username);
         break;
      case PLAYING:
         this.sendMessage(this.createMessage("GAME_PLAYING_STATE", username), username);
      }

   }

   private void initGame() {
      this.round = 0;
      this.pot = null;
      this.playerScores.clear();
      this.playerThirdUmpires.clear();
      this.playerDecks.clear();
      this.playerDrawnCards.clear();
   }

   private synchronized void joinGame(String username) {
      switch(this.gameState) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("GAME_STARTING_STATE", username), username);
         break;
      case GAME_JOINING:
         if (this.playerScores.containsKey(username)) {
            this.sendMessage("You have already joined the game. Please wait for the game to start", username);
         } else if (this.playerScores.size() + 1 > this.maxPlayers) {
            this.sendMessage("Too many players joined the game. Max " + this.maxPlayers + " players. Please wait for the next game.", username);
         } else if (this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, true) && this.playerScores.put(username, 0) == null) {
            log.info(username + " joined the game");
            this.playerThirdUmpires.put(username, 0);
            Deck deck = new Deck();
            deck.init();
            this.playerDecks.put(username, deck);
            this.sendChannelMessage(username + " joined the game");
            if (this.playerScores.size() == this.maxPlayers && this.waitingPlayersTimer != null) {
               this.waitingPlayersTimer.cancel(true);
               this.chargeAndCountPlayers();
            }
         }
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   private synchronized void cancelGame(String username) {
      switch(this.gameState) {
      case GAME_STARTING:
         if (this.startingTimer != null) {
            this.startingTimer.cancel(true);
         }

         List<String> player = new ArrayList();
         player.add(username);
         this.revertLimitInCache(player);
         this.gameState = BotData.BotStateEnum.NO_GAME;
         this.amountJoinPot = this.minAmountJoinPot;
         this.sendMessage(this.createMessage("PLAYER: You were not charged.", username), username);
         break;
      default:
         this.sendChannelMessage("Invalid command.");
      }

   }

   public synchronized double endGame(boolean cancelPot) {
      if (this.gameState == BotData.BotStateEnum.NO_GAME) {
         log.warn("endGame() called but game has already ended");
         return 0.0D;
      } else {
         double payout = 0.0D;
         if (cancelPot) {
            this.revertLimitInCache(this.playerScores.keySet());
         }

         if (this.decisionTimer != null) {
            this.decisionTimer.cancel(true);
         }

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
         this.gameState = BotData.BotStateEnum.NO_GAME;
         this.amountJoinPot = this.minAmountJoinPot;
         return payout;
      }
   }

   private void waitForMorePlayers() {
      this.sendChannelMessage("Waiting for more players. Enter !j to join the game");
      this.gameState = BotData.BotStateEnum.GAME_JOINING;
      this.waitingPlayersTimer = this.executor.schedule(new Runnable() {
         public void run() {
            Cricket.this.chargeAndCountPlayers();
         }
      }, this.timeToJoinGame, TimeUnit.MILLISECONDS);
   }

   private synchronized void chargeAndCountPlayers() {
      if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
         try {
            this.pot = new Pot(this);
            List<String> notAdded = new LinkedList();
            Iterator i$ = this.playerScores.keySet().iterator();

            String notAddedPlayer;
            while(i$.hasNext()) {
               notAddedPlayer = (String)i$.next();

               try {
                  this.pot.enterPlayer(notAddedPlayer, this.amountJoinPot / 100.0D, "USD");
               } catch (Exception var5) {
                  this.sendMessage("Unable to join you to the game " + ExceptionHelper.getRawRootMessage(var5), notAddedPlayer);
                  notAdded.add(notAddedPlayer);
               }
            }

            i$ = notAdded.iterator();

            while(i$.hasNext()) {
               notAddedPlayer = (String)i$.next();
               this.playerScores.remove(notAddedPlayer);
            }

            if (this.playerScores.size() < this.minPlayers) {
               this.endGame(true);
               this.sendChannelMessage("Joining ends. Not enough players. Need " + this.minPlayers + ". Enter !start to start a new game.");
            } else if (this.playerScores.size() > this.maxPlayers) {
               this.endGame(true);
               this.sendChannelMessage("Joining ends. Too many players. Max " + this.maxPlayers + ". Enter !start to start a new game.");
            } else {
               this.logGamesPlayed(this.playerScores.size(), this.playerScores.keySet(), this.amountJoinPot);
               this.incrementGamesPlayed(Leaderboard.Type.CRICKET_GAMES_PLAYED, this.playerScores.keySet());
               this.gameState = BotData.BotStateEnum.PLAYING;
               this.sendChannelMessage(this.createMessage("GAME_BEGINS", (String)null));
               this.nextRound();
            }
         } catch (Exception var6) {
            log.error("Unexpected exception occured in chargeAndCountPlayers()", var6);
            this.endGame(true);
            this.sendChannelMessage("Unable to start the game. " + ExceptionHelper.getRawRootMessage(var6));
         }

      }
   }

   private void bowl(String username, boolean botDraw) {
      switch(this.gameState) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("GAME_STARTING_STATE", username), username);
         break;
      case GAME_JOINING:
         this.sendMessage("Game haven't started. Enter !j to join the game", username);
         break;
      case PLAYING:
         if (!this.playerScores.containsKey(username)) {
            this.sendMessage("You are not in the game", username);
            return;
         }

         if (this.waitRound) {
            this.sendMessage("Round #" + this.round + " starting. Please wait.", username);
            return;
         }

         if (this.playerDrawnCards.containsKey(username)) {
            this.sendMessage("You have already drawn ur card. Your turn ends.", username);
         } else if (this.playerScores.containsKey(username) && !this.playerDrawnCards.containsKey(username)) {
            Deck deck = (Deck)this.playerDecks.get(username);
            Deck.Card card = deck.draw();
            this.playerDrawnCards.put(username, card);
            if (botDraw) {
               this.sendChannelMessage(this.createMessage("Bot draws - PLAYER: " + card.getEmoticonKey() + " " + card.getName(), username));
            } else {
               this.sendChannelMessage(this.createMessage("PLAYER: " + card.getEmoticonKey() + " " + card.getName(), username));
            }

            Integer numUmpire;
            if (card.getType() == "O") {
               numUmpire = (Integer)this.playerThirdUmpires.get(username);
               if (numUmpire <= 0) {
                  this.playerOuts.add(username);
                  this.sendChannelMessage(this.createMessage("PLAYER: OUT by " + card.getName(), username));
               } else {
                  numUmpire = numUmpire - 1;
                  this.playerThirdUmpires.put(username, numUmpire);
                  this.sendChannelMessage(this.createMessage("PLAYER: IMMUNE by " + Deck.Card.THIRD_UMPIRE.getName() + ". Current turn ends.", username));
               }
            } else if (card.getType() == "U") {
               numUmpire = (Integer)this.playerThirdUmpires.get(username);
               this.playerThirdUmpires.put(username, numUmpire + 1);
               this.sendChannelMessage(this.createMessage("PLAYER: SAFE by " + Deck.Card.THIRD_UMPIRE.getName() + "! Immune to next out.", username));
            } else {
               numUmpire = Integer.parseInt(card.getType());
               Integer totalScore = (Integer)this.playerScores.get(username);
               this.playerScores.put(username, totalScore + numUmpire);
            }

            this.playerDecks.put(username, deck);
            if (!botDraw && this.playerDrawnCards.size() == this.playerScores.size()) {
               this.sendChannelMessage("Everyone drawn.");
               if (this.roundTimer != null) {
                  this.roundTimer.cancel(true);
               }

               this.sendChannelMessage("TIME'S UP! Tallying...");
               this.roundEnded();
            }
         }
      }

   }

   private void nextRound() {
      this.playerDrawnCards.clear();
      this.playerOuts.clear();
      ++this.round;
      this.waitRound = true;
      this.sendChannelMessage("Round #" + this.round + " is starting in 5 seconds");
      this.executor.schedule(new Runnable() {
         public void run() {
            Cricket.this.waitRound = false;
            Cricket.this.startRound();
         }
      }, 5000L, TimeUnit.MILLISECONDS);
   }

   private void startRound() {
      this.sendChannelMessage(this.createMessage("ROUND_START", (String)null));
      this.roundTimer = this.executor.schedule(new Runnable() {
         public void run() {
            Cricket.this.sendChannelMessage("TIME'S UP! Tallying...");
            Cricket.this.roundEnded();
         }
      }, this.timeToEndRound, TimeUnit.MILLISECONDS);
   }

   private void roundEnded() {
      this.playerScores = this.sortByValue(this.playerScores);
      Iterator i$ = this.playerScores.keySet().iterator();

      String player;
      while(i$.hasNext()) {
         player = (String)i$.next();
         if (!this.playerDrawnCards.containsKey(player)) {
            this.bowl(player, true);
         }
      }

      this.sendChannelMessage("Round over! Results:");
      if (this.playerOuts.size() == this.playerScores.size()) {
         this.sendChannelMessage("Nobody won, so we'll try again!");
      } else {
         for(int i = 0; i < this.playerOuts.size(); ++i) {
            player = (String)this.playerOuts.get(i);

            try {
               this.pot.removePlayer(player);
            } catch (Exception var9) {
               log.error("Unexpected exception occured in removing bottom player from the pot", var9);
            }

            this.playerScores.remove(player);
            this.playerDecks.remove(player);
            this.playerThirdUmpires.remove(player);
         }
      }

      Map<String, Integer> tallyMessages = new HashMap();
      Iterator i$ = this.playerScores.keySet().iterator();

      String player;
      while(i$.hasNext()) {
         player = (String)i$.next();
         Integer totalScore = (Integer)this.playerScores.get(player);
         Deck.Card card = (Deck.Card)this.playerDrawnCards.get(player);
         String message = "";
         if (card.getType() == "U") {
            message = "Umpire";
         } else if (card.getType() != "O") {
            Integer runs = Integer.parseInt(card.getType());
            message = "+" + runs + (runs == 1 ? " Run" : " Runs");
         }

         String msg = this.createMessage("PLAYER: " + message + " (" + totalScore + ")", player);
         tallyMessages.put(msg, totalScore);
      }

      Map<String, Integer> tallyMessages = this.sortByValue(tallyMessages);
      i$ = tallyMessages.keySet().iterator();

      while(i$.hasNext()) {
         player = (String)i$.next();
         this.sendChannelMessage(player);
      }

      if (this.round < this.finalRound && this.playerScores.size() > 1) {
         this.nextRound();
      } else {
         this.playerScores = this.sortByValue(this.playerScores);
         Integer highestScore = -1;
         List<String> playerRemoved = new ArrayList();
         Iterator i$ = this.playerScores.keySet().iterator();

         String p;
         while(i$.hasNext()) {
            p = (String)i$.next();
            Integer score = (Integer)this.playerScores.get(p);
            if (score < highestScore) {
               playerRemoved.add(p);
            } else {
               highestScore = score;
            }
         }

         for(int i = 0; i < playerRemoved.size(); ++i) {
            p = (String)playerRemoved.get(i);
            this.playerScores.remove(p);
            this.playerDecks.remove(p);
            this.playerThirdUmpires.remove(p);

            try {
               this.pot.removePlayer(p);
            } catch (Exception var8) {
               log.error("Unexpected exception occured in removing bottom player from the pot", var8);
            }
         }

         if (this.playerScores.size() > 1) {
            this.sendChannelMessage("There is a tie. " + this.playerScores.size() + " left in the game [" + StringUtil.join((Collection)this.playerScores.keySet(), ", ") + "]");
            this.nextRound();
         } else if (this.playerScores.size() == 1) {
            String winner = (String)this.playerScores.keySet().iterator().next();
            this.sendChannelMessage(this.createMessage("PLAYER is the last player in.", winner));
            double payout = this.endGame(false);
            if (payout < 0.0D) {
               this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
            } else {
               this.logMostWins(winner, payout);
               this.incrementMostWins(Leaderboard.Type.CRICKET_MOST_WINS, winner);
               this.sendChannelMessageAndPopUp(this.createMessage("GAME_OVER", winner).replaceFirst("%1", decimalFormat.format(payout)));
            }

            this.sendChannelMessage("Enter !start to start a game");
            this.executor.schedule(new Runnable() {
               public void run() {
                  Cricket.this.sendChannelMessage(Cricket.this.createMessage("GAME_STATE_DEFAULT_AMOUNT", (String)null));
               }
            }, 5000L, TimeUnit.MILLISECONDS);
         } else if (this.playerScores.size() == 0) {
            this.sendChannelMessage("No more players left in the game. Enter !start to start a new game");
            this.endGame(false);
            this.executor.schedule(new Runnable() {
               public void run() {
                  Cricket.this.sendChannelMessage(Cricket.this.createMessage("GAME_STATE_DEFAULT_AMOUNT", (String)null));
               }
            }, 5000L, TimeUnit.MILLISECONDS);
         }
      }

   }

   private Map<String, Integer> sortByValue(Map<String, Integer> map) {
      List list = new LinkedList(map.entrySet());
      Collections.sort(list, new Comparator() {
         public int compare(Object o1, Object o2) {
            return ((Comparable)((Entry)((Entry)o1)).getValue()).compareTo(((Entry)((Entry)o2)).getValue());
         }
      });
      Collections.reverse(list);
      Map result = new LinkedHashMap();
      Iterator it = list.iterator();

      while(it.hasNext()) {
         Entry entry = (Entry)it.next();
         result.put(entry.getKey(), entry.getValue());
      }

      return result;
   }
}
