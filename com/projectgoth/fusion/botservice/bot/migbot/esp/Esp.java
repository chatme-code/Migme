package com.projectgoth.fusion.botservice.bot.migbot.esp;

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
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

public class Esp extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Esp.class));
   public static final String MIN_AMOUNT_JOIN_POT = "minAmountJoinPot";
   public static final String MIN_PLAYERS = "minPlayers";
   public static final String MAX_PLAYERS = "maxPlayers";
   public static final String MIN_RANGE = "minRange";
   public static final String MAX_RANGE = "maxRange";
   public static final String FINAL_ROUND = "finalRound";
   public static final String TIMER_JOIN_GAME = "timeToJoinGame";
   public static final String TIMER_BETWEEN_ROUNDS = "timeBetweenRounds";
   public static final String TIMER_GUESSES = "timeToGuess";
   public static final long IDLE_TIME_VALUE = 5L;
   double minAmountJoinPot = 5.0D;
   long timeToJoinGame = 60000L;
   long timeBetweenRounds = 10000L;
   long timeToGuess = 20000L;
   long timeToCancel = 20000L;
   public int minPlayers = 2;
   public int maxPlayers = 5;
   public int minRange = 1;
   public int maxRange = 11;
   public int finalRound = 5;
   Date lastActivityTime;
   private double amountJoinPot = 5.0D;
   private BotData.BotStateEnum gameState;
   private Map<String, Integer> playerGuesses;
   private Map<String, Integer> playerScores;
   private int round;
   private long idleInterval;
   private long timeLastGameFinished;
   private String startPlayer;
   private boolean waitPeriod;
   private static final String COMMAND_CANCEL = "!n";
   private ScheduledFuture startingTimer;

   public Esp(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.playerGuesses = new HashMap();
      this.playerScores = new HashMap();
      this.round = 0;
      this.idleInterval = 1800000L;
      this.timeLastGameFinished = System.currentTimeMillis();
      this.startPlayer = "";
      this.waitPeriod = false;
      this.loadGameConfig();
      log.info("GuessBot [" + this.instanceID + "] added to channel [" + this.channel + "]");
      this.sendChannelMessage(this.createMessage("BOT_ADDED", (String)null));
      this.sendChannelMessage(this.createMessage("GAME_STATE_DEFAULT_AMOUNT", (String)null));
   }

   private void loadGameConfig() {
      this.minAmountJoinPot = this.getDoubleParameter("minAmountJoinPot", this.minAmountJoinPot);
      this.minPlayers = this.getIntParameter("minPlayers", this.minPlayers);
      this.maxPlayers = this.getIntParameter("maxPlayers", this.maxPlayers);
      this.minRange = this.getIntParameter("minRange", this.minRange);
      this.maxRange = this.getIntParameter("maxRange", this.maxRange);
      this.finalRound = this.getIntParameter("finalRound", this.finalRound);
      this.timeToJoinGame = this.getLongParameter("timeToJoinGame", this.timeToJoinGame);
      this.timeBetweenRounds = this.getLongParameter("timeBetweenRounds", this.timeBetweenRounds);
      this.timeToGuess = this.getLongParameter("timeToGuess", this.timeToGuess);
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
         messageToSend = messageToSend.replaceAll("TIMER_JOIN", "" + this.timeToJoinGame);
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
            log.debug("Found message for key:" + messageToSend);
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
         this.sendMessage("Play Guess. Enter !start to start a game.", username);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("PLAYER: Guess Game is starting soon.", username), username);
         break;
      case GAME_JOINING:
         this.sendMessage("Play Guess. Enter !j to join the game.", username);
         break;
      case PLAYING:
         this.sendMessage("Guess is on going now. Get ready for the next game.", username);
      }

   }

   public void onUserLeaveChannel(String username) {
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
         } else if (messageText.startsWith("!")) {
            String input = messageText.substring("!".length());
            if (!this.playerGuesses.containsKey(username)) {
               this.sendMessage(this.createMessage("PLAYER: You're not in the game.", username), username);
               return;
            }

            try {
               Integer guess = Integer.parseInt(input);
               if (this.waitPeriod) {
                  this.sendMessage("Please wait till the round begins.", username);
               } else if (guess >= this.minRange && guess <= this.maxRange) {
                  this.guessNumber(username, guess);
               } else {
                  this.sendMessage(this.createMessage("PLAYER: Guess a number from " + this.minRange + "-" + this.maxRange, username), username);
               }
            } catch (NumberFormatException var7) {
               this.sendMessage(this.createMessage("PLAYER: You can only guess numbers.", username), username);
            }
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
               double amount = Double.parseDouble(messageText.substring("!start".length() + 1));
               if (amount < this.minAmountJoinPot) {
                  this.sendMessage(this.createMessage("PLAYER: Invalid amount. Custom amount has to be CURRENCY " + this.amountJoinPot / 100.0D + " or more (e.g. !start 5) ", username), username);
                  return;
               }

               this.amountJoinPot = amount;
            } catch (NumberFormatException var5) {
               this.sendMessage(this.createMessage("PLAYER: Invalid amount. Custom amount has to be in integer (e.g. !start 5) ", username), username);
               return;
            }
         }

         if (!this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, true)) {
            return;
         }

         this.startPlayer = username;
         this.sendMessage(this.createMessage("PLAYER: added to game. Charges apply. CURRENCY " + this.amountJoinPot / 100.0D + ". Create/enter pot. !n to cancel. " + this.timeToCancel / 1000L + " seconds.", username), username);
         this.gameState = BotData.BotStateEnum.GAME_STARTING;
         this.startingTimer = this.executor.schedule(new Runnable() {
            public void run() {
               Esp.this.initGame();
               Esp.this.playerGuesses.put(username, -1);
               Esp.this.playerScores.put(username, 0);
               Esp.this.waitForMorePlayers();
               Esp.this.sendMessage(Esp.this.createMessage("PLAYER: added to game. Charges apply. CURRENCY " + Esp.this.amountJoinPot / 100.0D + ".", username), username);
               Esp.this.sendChannelMessage(Esp.this.createMessage("Guess game started. !j to join. Cost CURRENCY " + Esp.this.amountJoinPot / 100.0D + ". 60 seconds.", username));
            }
         }, this.timeToCancel, TimeUnit.MILLISECONDS);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("PLAYER: Guess Game is starting soon.", username), username);
         break;
      case GAME_JOINING:
         this.sendMessage("Game has already started. Enter !j to join the game.", username);
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game.", username);
      }

   }

   private void initGame() {
      this.round = 0;
      this.playerGuesses.clear();
      this.playerScores.clear();
      this.pot = null;
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

   private synchronized void joinGame(String username) {
      switch(this.gameState) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("PLAYER: Guess Game is starting soon.", username), username);
         break;
      case GAME_JOINING:
         if (this.playerGuesses.containsKey(username)) {
            this.sendMessage("You have already joined the game. Please wait for the game to start", username);
         } else if (this.playerGuesses.size() + 1 > this.maxPlayers) {
            this.sendMessage("Too many players joined the game. Max " + this.maxPlayers + " players. Please wait for the next game.", username);
         } else if (this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, true) && this.playerGuesses.put(username, -1) == null) {
            log.info(username + " joined the game");
            this.playerScores.put(username, 0);
            this.sendMessage(this.createMessage("PLAYER: added to game. Charges apply. CURRENCY " + this.amountJoinPot / 100.0D + ".", username), username);
            this.sendChannelMessage(username + " joined the game");
         }
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   public synchronized double endGame(boolean cancelPot) {
      if (this.gameState == BotData.BotStateEnum.NO_GAME) {
         log.warn("endGame() called but game has already ended");
         return 0.0D;
      } else {
         double payout = 0.0D;
         if (cancelPot) {
            this.revertLimitInCache(this.playerGuesses.keySet());
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
      this.executor.schedule(new Runnable() {
         public void run() {
            Esp.this.chargeAndCountPlayers();
         }
      }, this.timeToJoinGame, TimeUnit.MILLISECONDS);
   }

   private synchronized void chargeAndCountPlayers() {
      try {
         this.pot = new Pot(this);
         List<String> notAdded = new LinkedList();
         Iterator i$ = this.playerGuesses.keySet().iterator();

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
            this.playerGuesses.remove(notAddedPlayer);
            this.playerScores.remove(notAddedPlayer);
         }

         this.sendChannelMessage("Number of players: " + this.playerGuesses.size());
         if (this.playerGuesses.size() < this.minPlayers) {
            this.endGame(true);
            this.sendChannelMessage("Joining ends. Not enough players. Need " + this.minPlayers + ". Enter !start to start a new game.");
         } else if (this.playerGuesses.size() > this.maxPlayers) {
            this.endGame(true);
            this.sendChannelMessage("Joining ends. Too many players. Max " + this.maxPlayers + ". Enter !start to start a new game.");
         } else {
            this.logGamesPlayed(this.playerGuesses.size(), this.playerGuesses.keySet(), this.amountJoinPot);
            this.incrementGamesPlayed(Leaderboard.Type.GUESS_GAMES_PLAYED, this.playerScores.keySet());
            this.sendChannelMessage("Game begins - Guess the secret number!");
            this.waitForNextRound();
         }
      } catch (Exception var6) {
         log.error("Unexpected exception occured in chargeAndCountPlayers()", var6);
         this.endGame(true);
         this.sendChannelMessage("Unable to start the game. " + ExceptionHelper.getRawRootMessage(var6));
      }

   }

   private synchronized void waitForNextRound() {
      ++this.round;
      this.waitPeriod = true;
      this.sendChannelMessage("Round #" + this.round + " starting in " + this.timeBetweenRounds / 1000L + " seconds.");
      this.executor.schedule(new Runnable() {
         public void run() {
            Esp.this.waitForGuesses();
         }
      }, this.timeBetweenRounds, TimeUnit.MILLISECONDS);
   }

   private synchronized void waitForGuesses() {
      Iterator i$ = this.playerGuesses.keySet().iterator();

      while(i$.hasNext()) {
         String player = (String)i$.next();
         this.playerGuesses.put(player, -1);
      }

      this.sendChannelMessage("Round #" + this.round + ". Reveal number in " + this.timeToGuess / 1000L + " seconds. !<number> to make your guess.");
      this.gameState = BotData.BotStateEnum.PLAYING;
      this.waitPeriod = false;
      this.executor.schedule(new Runnable() {
         public void run() {
            Esp.this.revealNumber();
         }
      }, this.timeToGuess, TimeUnit.MILLISECONDS);
   }

   private void guessNumber(String username, Integer guess) {
      switch(this.gameState) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game.", username);
      case GAME_STARTING:
      default:
         break;
      case GAME_JOINING:
         this.sendMessage("Please wait till the game starts.", username);
         break;
      case PLAYING:
         Integer number = (Integer)this.playerGuesses.get(username);
         if (number == null) {
            this.sendMessage(this.createMessage("PLAYER: You're not in the game. Please wait for next game.", username), username);
         } else if (number == -1) {
            this.playerGuesses.put(username, guess);
            this.sendChannelMessage(this.createMessage("PLAYER: Guessed " + guess + ".", username));
         } else {
            this.sendMessage(this.createMessage("PLAYER: You already guessed.", username), username);
         }
      }

   }

   private void revealNumber() {
      SecureRandom random = new SecureRandom();
      Integer highestScore;
      if (this.round > this.finalRound) {
         Iterator i$ = this.playerGuesses.keySet().iterator();

         while(i$.hasNext()) {
            String player = (String)i$.next();
            highestScore = (Integer)this.playerGuesses.get(player);
            if (highestScore == -1) {
               highestScore = random.nextInt(this.maxRange - this.minRange + 1) + this.minRange;
               this.playerGuesses.put(player, highestScore);
               this.sendMessage(this.createMessage("Bot guess: " + highestScore, player), player);
            }
         }
      }

      Integer magicNumber = random.nextInt(this.maxRange - this.minRange) + this.minRange;
      this.sendChannelMessage("TIME'S UP! The magic number was " + magicNumber + "!");
      this.sendChannelMessage("Results for Round #" + this.round + ":");
      Map<String, Integer> tallyMessages = new HashMap();

      String player;
      Integer score;
      String msg;
      Iterator i$;
      for(i$ = this.playerGuesses.keySet().iterator(); i$.hasNext(); tallyMessages.put(msg, score)) {
         player = (String)i$.next();
         Integer guess = (Integer)this.playerGuesses.get(player);
         Integer score = 0;
         if (guess == -1) {
            score = 0;
         } else if (guess == magicNumber) {
            score = 2;
         } else {
            score = 1;
         }

         score = (Integer)this.playerScores.get(player) + score;
         this.playerScores.put(player, score);
         msg = "";
         switch(score) {
         case 0:
            msg = this.createMessage("PLAYER: No guess +" + score + " (" + score + ")", player);
            break;
         case 1:
            msg = this.createMessage("PLAYER: Incorrect +" + score + " (" + score + ")", player);
            break;
         case 2:
            msg = this.createMessage("PLAYER: Correct! +" + score + " (" + score + ")", player);
         }
      }

      Map<String, Integer> tallyMessages = this.sortByValue(tallyMessages);
      i$ = tallyMessages.keySet().iterator();

      while(i$.hasNext()) {
         player = (String)i$.next();
         this.sendChannelMessage(player);
      }

      if (this.round < this.finalRound) {
         this.waitForNextRound();
      } else if (this.round >= this.finalRound) {
         this.playerScores = this.sortByValue(this.playerScores);
         highestScore = -1;
         List<String> playerRemoved = new ArrayList();
         Iterator i$ = this.playerScores.keySet().iterator();

         String p;
         while(i$.hasNext()) {
            p = (String)i$.next();
            score = (Integer)this.playerScores.get(p);
            if (score < highestScore) {
               playerRemoved.add(p);
            } else {
               highestScore = score;
            }
         }

         for(int i = 0; i < playerRemoved.size(); ++i) {
            p = (String)playerRemoved.get(i);

            try {
               this.pot.removePlayer(p);
            } catch (Exception var10) {
               log.error("Unexpected exception occured in removing bottom player from the pot", var10);
            }

            this.playerGuesses.remove(p);
            this.playerScores.remove(p);
         }

         if (this.playerGuesses.size() > 1) {
            this.sendChannelMessage("There is a tie. " + this.playerGuesses.size() + " left in the game [" + StringUtil.join((Collection)this.playerGuesses.keySet(), ", ") + "]");
            this.waitForNextRound();
         } else if (this.playerGuesses.size() == 1) {
            double payout = this.endGame(false);
            if (payout < 0.0D) {
               this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
            } else {
               String winner = (String)this.playerScores.keySet().iterator().next();
               this.sendChannelMessageAndPopUp(this.createMessage("Guess Game over! PLAYER WINS CURRENCY " + (new DecimalFormat("0.00")).format(payout) + " CONGRATS!", winner));
               this.logMostWins(winner, payout);
               this.incrementMostWins(Leaderboard.Type.GUESS_MOST_WINS, winner);
            }

            this.executor.schedule(new Runnable() {
               public void run() {
                  Esp.this.sendChannelMessage(Esp.this.createMessage("GAME_STATE_DEFAULT_AMOUNT", (String)null));
               }
            }, 3000L, TimeUnit.MILLISECONDS);
         } else if (this.playerGuesses.size() == 0) {
            this.endGame(false);
            this.sendChannelMessage("No more players left in the game.");
            this.executor.schedule(new Runnable() {
               public void run() {
                  Esp.this.sendChannelMessage(Esp.this.createMessage("GAME_STATE_DEFAULT_AMOUNT", (String)null));
               }
            }, 3000L, TimeUnit.MILLISECONDS);
         }
      }

   }

   private Map sortByValue(Map map) {
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
