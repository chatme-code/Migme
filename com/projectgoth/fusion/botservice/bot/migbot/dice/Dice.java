package com.projectgoth.fusion.botservice.bot.migbot.dice;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.botservice.bot.Pot;
import com.projectgoth.fusion.common.ConfigUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class Dice extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Dice.class));
   public static final String EMOTICON_HOTKEY_DICE_PREFIX = "d";
   public static final String TIMER_JOIN_GAME = "timerJoinGame";
   public static final String TIMER_CHARGE_CONF = "timerChargeConfirm";
   public static final String TIMER_ROLL = "timerRoll";
   public static final String TIMER_NEW_ROUND_INTERVAL = "timerNewRound";
   public static final String TIMER_IDLE = "timerIdle";
   public static final String AMOUNT_JOIN_POT = "amountJoinPot";
   public static final long TIMER_ROLL_VALUE = 10L;
   public static final long TIMER_NEW_ROUND_VALUE = 3L;
   public static final double AMOUNT_JOIN_POT_VALUE = 5.0D;
   public static final long IDLE_TIME_VALUE = 3L;
   long timeToJoinGame = 90L;
   long timeToConfirmCharge = 20L;
   long timeToRoll = 10L;
   long timeToNewRound = 3L;
   double amountJoinPot = 5.0D;
   double winnings = 0.0D;
   public int minPlayers = 2;
   long timeAllowedToIdle = 30L;
   double amountOriginalJoinPot = 5.0D;
   public static final String COMMAND_ROLL = "!r";
   Date lastActivityTime;
   private Map<String, DiceRoll> playerDiceRolls = new HashMap();
   private Map<Integer, Set<String>> safePlayers = new HashMap();
   private BotData.BotStateEnum gameState;
   DiceRoll botDice;
   boolean hasWinner;
   int currentRoundNumber;
   int numPlayed;
   boolean isRoundStarted;
   ScheduledFuture nextRollTimerTask;

   public Dice(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.botDice = new DiceRoll();
      this.hasWinner = false;
      this.currentRoundNumber = 0;
      this.numPlayed = 0;
      this.isRoundStarted = false;
      this.nextRollTimerTask = null;
      this.loadGameConfig();
      log.info("DiceBot [" + this.instanceID + "] added to channel [" + this.channel + "]");
      this.sendChannelMessage(this.createMessage("BOT_ADDED"));
      String message = this.amountJoinPot > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
      this.sendChannelMessage(message);
      this.updateLastActivityTime();
   }

   private synchronized BotData.BotStateEnum getGameState() {
      return this.gameState;
   }

   private synchronized void setGameState(BotData.BotStateEnum gameState) {
      this.gameState = gameState;
   }

   public void stopBot() {
      if (log.isDebugEnabled()) {
         log.debug("Stopping bot instanceID[" + this.instanceID + "]");
      }

      if (this.nextRollTimerTask != null && !this.nextRollTimerTask.isDone() && !this.nextRollTimerTask.isCancelled()) {
         this.nextRollTimerTask.cancel(true);
      }

      if (this.pot != null) {
         log.debug("Expiring pot [" + this.pot.getPotID() + "] for bot instanceID[" + this.instanceID + "]");

         try {
            this.pot.cancel();
         } catch (Exception var2) {
            log.error("Error canceling pot [" + this.pot.getPotID() + "], botInstanceID[" + this.instanceID + "]");
         }
      }

      this.setGameState(BotData.BotStateEnum.NO_GAME);
      log.debug("Stopped bot instanceID[" + this.instanceID + "]");
   }

   public boolean isIdle() {
      long timeSince = (new Date()).getTime() - this.lastActivityTime.getTime();
      if (timeSince < 0L) {
         log.warn("Error calculating time since. Target date is in the future.");
         return false;
      } else {
         long minutes = timeSince / 60000L % 60L;
         if (minutes > this.timeAllowedToIdle) {
            log.warn("Bot has been idle for " + minutes + (minutes == 1L ? " minute" : " minutes") + ". Marking as idle, and resetting game, if any...");
            this.resetGame(false);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean canBeStoppedNow() {
      return (this.gameState != BotData.BotStateEnum.PLAYING || this.pot == null) && this.gameState != BotData.BotStateEnum.GAME_JOINING && this.gameState != BotData.BotStateEnum.GAME_STARTING;
   }

   private synchronized void updateLastActivityTime() {
      this.lastActivityTime = new Date();
   }

   private void loadGameConfig() {
      this.timeToJoinGame = this.getLongParameter("timerJoinGame", this.timeToJoinGame);
      this.timeToConfirmCharge = this.getLongParameter("timerChargeConfirm", this.timeToConfirmCharge);
      this.timeToRoll = this.getLongParameter("timerRoll", 10L);
      this.timeToNewRound = this.getLongParameter("timerNewRound", 3L);
      this.amountJoinPot = this.getDoubleParameter("amountJoinPot", 5.0D);
      this.amountOriginalJoinPot = this.amountJoinPot;
      this.timeAllowedToIdle = this.getLongParameter("timerIdle", 3L);
   }

   public synchronized void onMessage(String username, String messageText, long receivedTimestamp) {
      synchronized(this) {
         if (messageText.equalsIgnoreCase("!n")) {
            this.processNoMessage(username);
         } else if (messageText.toLowerCase().startsWith("!start")) {
            this.start(username, messageText);
         } else if (messageText.equalsIgnoreCase("!j")) {
            this.join(username);
         } else if (messageText.equalsIgnoreCase("!r")) {
            if (this.gameState == BotData.BotStateEnum.PLAYING && this.isRoundStarted) {
               if (!this.playerDiceRolls.containsKey(username)) {
                  this.sendMessage(this.createMessage("NOT_IN_GAME", username), username);
               } else {
                  if (log.isDebugEnabled()) {
                     log.debug("botInstanceID[" + this.instanceID + "]: " + username + " rolls");
                  }

                  this.roll(username, false);
               }
            } else {
               this.sendMessage(this.createMessage("INVALID_COMMAND", username), username);
            }
         }

      }
   }

   private void join(String username) {
      if (!this.playerDiceRolls.containsKey(username)) {
         if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
            this.addPlayer(username);
         } else if (this.gameState == BotData.BotStateEnum.PLAYING) {
            this.sendMessage(this.createMessage("JOIN_ENDED", username), username);
         } else {
            this.sendMessage(this.createMessage("INVALID_COMMAND", username), username);
         }
      } else {
         this.sendMessage(this.createMessage("ALREADY_IN_GAME", username), username);
      }

   }

   private void start(String username, String messageText) {
      if (this.getGameState() == BotData.BotStateEnum.NO_GAME) {
         if (messageText.trim().length() > "!start".length()) {
            String parameter = messageText.trim().substring("!start".length() + 1);
            if (StringUtils.hasLength(parameter) && this.checkJoinPotParameter(parameter, username)) {
               try {
                  this.startGame(username);
               } catch (Exception var6) {
                  log.error("Error starting game with custom amount. Command was : '" + messageText + "'", var6);
               }
            }
         } else {
            try {
               this.startGame(username);
            } catch (Exception var5) {
               log.error("Error starting game with default amount: ", var5);
            }
         }
      } else {
         this.sendGameCannotStartMessage(username);
      }

   }

   private void sendGameCannotStartMessage(String username) {
      String message = null;
      switch(this.gameState.value()) {
      case 1:
      case 2:
      case 5:
         message = this.createMessage("STATUS-PLAYING", username);
         break;
      case 3:
         message = this.createMessage("STATUS-JOINING", username);
         break;
      case 4:
      default:
         message = this.createMessage("STATUS-CANNOT-START", username);
      }

      this.sendMessage(message, username);
   }

   public Map<String, DiceRoll> getPlayers() {
      return this.playerDiceRolls;
   }

   public void addPlayer(String username) {
      if (this.getGameState() == BotData.BotStateEnum.GAME_STARTED || this.getGameState() == BotData.BotStateEnum.GAME_JOINING) {
         if (this.amountJoinPot > 0.0D) {
            boolean hasFunds = this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, !this.gameStarter.equals(username));
            if (!hasFunds) {
               return;
            }
         }

         synchronized(this.playerDiceRolls) {
            if (!this.playerDiceRolls.containsKey(username)) {
               this.playerDiceRolls.put(username, new DiceRoll());
            }
         }

         StringBuilder message = new StringBuilder();
         message.append(this.createMessage("ADDED_TO_GAME", username));
         if (this.amountJoinPot > 0.0D) {
            message.append(this.createMessage("CHARGES_APPLY_POT"));
         }

         log.info(username + " joined the game");
         this.sendMessage(message.toString(), username);
         if (!username.equals(this.gameStarter)) {
            this.sendChannelMessage(this.createMessage("JOIN", username));
         }
      }

   }

   public void processNoMessage(String username) {
      String message = null;
      switch(this.getGameState().value()) {
      case 1:
         if (username.equals(this.gameStarter) && this.amountJoinPot > 0.0D) {
            this.revertLimitInCache(this.gameStarter);
            this.setGameState(BotData.BotStateEnum.NO_GAME);
            this.amountJoinPot = 5.0D;
            this.gameStarter = null;
            message = this.createMessage("NOT_CHARGED", username);
         } else {
            message = this.createMessage("INVALID_COMMAND", username);
         }
         break;
      default:
         message = this.createMessage("INVALID_COMMAND", username);
      }

      this.sendMessage(message, username);
   }

   private boolean checkJoinPotParameter(String parameter, String username) {
      boolean isAmountValid = false;

      try {
         double amount = Double.parseDouble(parameter);
         if (amount >= this.amountJoinPot) {
            this.amountJoinPot = amount;
            isAmountValid = true;
         } else {
            String message = this.createMessage("INVALID_AMOUNT", username, (DiceRoll)null, parameter);
            if (log.isDebugEnabled()) {
               log.debug("Lower value specified for amountJoinPot: " + parameter);
            }

            this.sendMessage(message, username);
         }

         if (log.isDebugEnabled()) {
            log.debug("Parameter defined : amountJoinPot=" + this.amountJoinPot);
         }
      } catch (Exception var7) {
         String message = this.createMessage("INVALID_AMOUNT", username, (DiceRoll)null, parameter);
         this.sendMessage(message, username);
      }

      return isAmountValid;
   }

   public void startGame(String username) throws Exception {
      this.updateLastActivityTime();
      if (this.gameState.equals(BotData.BotStateEnum.NO_GAME)) {
         if (this.amountJoinPot > 0.0D) {
            boolean hasFunds = this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, true);
            if (hasFunds) {
               StringBuilder message = new StringBuilder(this.createMessage("CHARGE_NEW_POT", username));
               message.append(this.createMessage("CHARGE_CONF_NO_MSG", username));
               this.setGameState(BotData.BotStateEnum.GAME_STARTING);
               this.sendMessage(message.toString(), username);
               this.gameStarter = username;
               if (log.isDebugEnabled()) {
                  log.debug("DiceBot: starting timer for StartGame()");
               }

               this.executor.schedule(new Dice.StartGame(this), this.timeToConfirmCharge, TimeUnit.SECONDS);
               if (log.isDebugEnabled()) {
                  log.debug("DiceBot: started timer for StartGame()");
               }
            } else {
               this.resetGame(false);
            }
         } else {
            if (log.isDebugEnabled()) {
               log.debug("botInstanceID[" + this.getInstanceID() + "]: No charges. Game started by user[" + username + "]");
            }

            this.setGameState(BotData.BotStateEnum.GAME_STARTING);
            this.gameStarter = username;
            if (log.isDebugEnabled()) {
               log.debug("DiceBot: starting timer for StartGame()");
            }

            this.executor.execute(new Dice.StartGame(this));
            if (log.isDebugEnabled()) {
               log.debug("DiceBot: started timer for StartGame()");
            }
         }
      } else {
         this.sendGameCannotStartMessage(username);
      }

   }

   public synchronized void onUserJoinChannel(String username) {
      String message = null;
      switch(this.getGameState().value()) {
      case 1:
      case 2:
      case 5:
         message = this.createMessage("GAME_STATE_STARTED");
         break;
      case 3:
         message = this.amountJoinPot > 0.0D ? this.createMessage("GAME_JOIN_PAID") : this.createMessage("GAME_JOIN_FREE");
         break;
      case 4:
      default:
         message = this.amountJoinPot > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
      }

      this.sendMessage(message, username);
   }

   public void onUserLeaveChannel(String username) {
      synchronized(this) {
         this.removePlayerFromPot(username);
         if (this.playerDiceRolls != null) {
            this.removeSafePlayer(username);
            if (this.playerDiceRolls.containsKey(username)) {
               this.playerDiceRolls.remove(username);
               this.sendChannelMessage(this.createMessage("PLAYER_LEFT", username));
            }

            if (this.getGameState() == BotData.BotStateEnum.PLAYING && this.playerDiceRolls.size() < this.minPlayers) {
               if (this.nextRollTimerTask != null && !this.nextRollTimerTask.isDone() && !this.nextRollTimerTask.isCancelled()) {
                  this.nextRollTimerTask.cancel(true);
               }

               this.pickWinner();
            }
         }

      }
   }

   private void removePlayerFromPot(String username) {
      if (log.isDebugEnabled()) {
         log.debug("Player lost, and is not immune :" + username + ". Removing from pot.");
      }

      if (this.pot != null) {
         try {
            this.pot.removePlayer(username);
         } catch (Exception var3) {
            log.error("BotInstanceID: " + this.instanceID + "]: Error removing player " + username + "] from pot.", var3);
         }
      }

   }

   private synchronized void endGame(String winner) {
      try {
         try {
            if (this.getGameState() != BotData.BotStateEnum.PLAYING) {
               return;
            }

            if (this.nextRollTimerTask != null && !this.nextRollTimerTask.isDone() && !this.nextRollTimerTask.isCancelled()) {
               log.debug("botInstanceID[" + this.getInstanceID() + "]: Pending timer task to cancel in endGame() ");
               this.nextRollTimerTask.cancel(true);
            }

            Pot localPot = this.pot;
            if (localPot != null) {
               try {
                  this.winnings = localPot.payout(true);
                  Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
                  this.winnings = accountEJB.convertCurrency(this.winnings, "AUD", "USD");
                  log.debug("Game over. Pot [" + localPot.getPotID() + "] payout completed.");
               } catch (Exception var9) {
                  log.error("Game over. Error in pot [" + localPot.getPotID() + "] payout.", var9);
                  this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
                  return;
               }
            }

            this.sendChannelMessageAndPopUp(this.amountJoinPot > 0.0D ? this.createMessage("GAME_OVER_PAID", winner) : this.createMessage("GAME_OVER_FREE", winner));
            this.logMostWins(winner, this.winnings);
            this.incrementMostWins(Leaderboard.Type.DICE_MOST_WINS, winner);
         } catch (Exception var10) {
            log.error("botInstanceID[" + this.getInstanceID() + "]: Error getting game winner. ", var10);
         }

      } finally {
         this.resetGame(false);
         this.updateLastActivityTime();
         this.sendChannelMessage(this.amountJoinPot > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT"));
      }
   }

   void resetGame(boolean cancel) {
      if (cancel) {
         this.revertLimitInCache(this.playerDiceRolls.keySet());
      }

      this.playerDiceRolls.clear();
      this.safePlayers.clear();
      this.nextRollTimerTask = null;
      this.currentRoundNumber = 0;
      this.isRoundStarted = false;
      this.hasWinner = false;
      this.numPlayed = 0;
      this.gameStarter = null;
      this.botDice.reset();
      this.pot = null;
      this.amountJoinPot = this.amountOriginalJoinPot;
      this.setGameState(BotData.BotStateEnum.NO_GAME);
   }

   private void addSafePlayer(int roundNumber, String username) {
      Set<String> players = (Set)this.safePlayers.get(roundNumber);
      if (players == null) {
         players = new HashSet();
         this.safePlayers.put(roundNumber, players);
      }

      ((Set)players).add(username);
   }

   private void removeSafePlayer(String username) {
      Iterator i$ = this.safePlayers.keySet().iterator();

      while(i$.hasNext()) {
         Integer roundNumber = (Integer)i$.next();
         Set<String> playerList = (Set)this.safePlayers.get(roundNumber);
         playerList.remove(username);
      }

   }

   private boolean isSafePlayer(int roundNumber, String username) {
      boolean isSafe = false;
      Set<String> players = (Set)this.safePlayers.get(roundNumber);
      if (players != null) {
         isSafe = players.contains(username);
      }

      return isSafe;
   }

   private void removeSafeList(int roundNumber) {
      this.safePlayers.remove(roundNumber);
   }

   private void showSafePlayers(int roundNumber) {
      Set<String> players = (Set)this.safePlayers.get(roundNumber);
      if (players != null) {
         Iterator i$ = players.iterator();

         while(i$.hasNext()) {
            String player = (String)i$.next();
            log.debug(player + " ");
         }
      }

   }

   protected String createMessage(String messageKey) {
      return this.createMessage(messageKey, (String)null, (DiceRoll)null, (String)null);
   }

   String createMessage(String messageKey, String username) {
      return this.createMessage(messageKey, username, (DiceRoll)null, (String)null);
   }

   private String createMessage(String messageKey, String username, DiceRoll dice) {
      return this.createMessage(messageKey, username, dice, (String)null);
   }

   private String createMessage(String messageKey, String player, DiceRoll dice, String errorInput) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Looking for messageKey: " + messageKey);
         }

         String messageToSend = (String)this.messages.get(messageKey);
         if (messageToSend == null) {
            messageToSend = messageKey;
         }

         messageToSend = messageToSend.replaceAll("BOTNAME", this.botData.getDisplayName());
         messageToSend = messageToSend.replaceAll("CONF_TIMER", this.timeToConfirmCharge + "");
         messageToSend = messageToSend.replaceAll("TIMER_JOIN", "" + this.timeToJoinGame);
         messageToSend = messageToSend.replaceAll("TIMER_ROLL", "" + this.timeToRoll);
         messageToSend = messageToSend.replaceAll("TIMER_ROUND", "" + this.timeToNewRound);
         messageToSend = messageToSend.replaceAll("CMD_ROLL", "!r");
         messageToSend = messageToSend.replaceAll("CMD_NO", "!n");
         messageToSend = messageToSend.replaceAll("CMD_JOIN", "!j");
         messageToSend = messageToSend.replaceAll("CMD_START", "!start");
         messageToSend = messageToSend.replaceAll("MINPLAYERS", this.minPlayers + "");
         if (player != null) {
            messageToSend = messageToSend.replaceAll("PLAYER", player);
            messageToSend = messageToSend.replaceAll("LEADER", player);
         }

         if (dice != null) {
            messageToSend = messageToSend.replaceAll("DICE_VALUES", dice.toString());
         }

         if (this.botDice != null && this.botDice.total() > 0) {
            messageToSend = messageToSend.replaceAll("DICE_TOTAL", this.botDice.total() + "");
         }

         messageToSend = messageToSend.replaceAll("CURRENCY", "USD");
         messageToSend = messageToSend.replaceAll("AMOUNT_POT", this.amountJoinPot / 100.0D + "");
         messageToSend = messageToSend.replaceAll("CUSTOM_MIN_AMOUNT", this.amountJoinPot + 1.0D + "");
         if (this.winnings > 0.0D) {
            DecimalFormat df = new DecimalFormat("0.00");
            df.setMinimumFractionDigits(2);
            df.setMaximumFractionDigits(2);
            messageToSend = messageToSend.replaceAll("WINNINGS", df.format(this.winnings));
         }

         if (StringUtils.hasLength(errorInput)) {
            errorInput = errorInput.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
            messageToSend = messageToSend.replaceAll("ERROR_INPUT", errorInput);
         }

         if (this.currentRoundNumber != 0) {
            messageToSend = messageToSend.replaceAll("ROUND_NUMBER", this.currentRoundNumber + "");
         }

         if (log.isDebugEnabled()) {
            log.debug("Found message for key:" + messageToSend);
         }

         return messageToSend;
      } catch (NullPointerException var7) {
         log.error("Outgoing message could not be created, key = " + messageKey, var7);
         return "";
      }
   }

   private synchronized void roll(String username, boolean auto) {
      DiceRoll dice = (DiceRoll)this.playerDiceRolls.get(username);
      if (dice.total() == 0) {
         dice.rollAndMatch(this.botDice.total());
         if (dice.total() == this.botDice.total()) {
            this.sendChannelMessage(this.createMessage(auto ? "AUTO_ROLL_MATCH" : "PLAYER_ROLLS_MATCH", username, dice));
            if (!this.hasWinner) {
               this.hasWinner = true;
            }
         } else if (dice.total() > this.botDice.total()) {
            this.sendChannelMessage(this.createMessage(auto ? "AUTO_ROLL_HIGHER" : "PLAYER_ROLLS_HIGHER", username, dice));
            if (!this.hasWinner) {
               this.hasWinner = true;
            }

            if (dice.total() == 12) {
               this.addSafePlayer(this.currentRoundNumber + 1, username);
               this.sendChannelMessage(this.createMessage("IMMUNITY", username, dice));
            }
         } else if (this.isSafePlayer(this.currentRoundNumber, username)) {
            this.sendChannelMessage(this.createMessage("SAFE_BY_IMMUNITY", username, dice));
         } else {
            this.sendChannelMessage(this.createMessage(auto ? "AUTO_ROLL_OUT" : "PLAYER_ROLLS_OUT", username, dice));
         }

         if (!auto && ++this.numPlayed >= this.playerDiceRolls.size()) {
            if (log.isDebugEnabled()) {
               log.debug("Looks like everyone has rolled. Let's tally!");
            }

            if (this.nextRollTimerTask != null && !this.nextRollTimerTask.isDone() && !this.nextRollTimerTask.isCancelled()) {
               this.nextRollTimerTask.cancel(true);
            }

            this.tallyRolls();
         }
      } else if (!auto) {
         this.sendMessage(this.createMessage("ALREADY_ROLLED", username), username);
      } else {
         log.warn("Auto roll requested for player: " + username + ". But they already seem to have rolled!");
      }

   }

   private void newRound() {
      this.isRoundStarted = true;
      ++this.currentRoundNumber;
      this.hasWinner = false;
      this.resetDice();
      this.numPlayed = 0;
      this.botDice.roll();
      this.sendChannelMessage(this.createMessage("BOT_ROLLED", (String)null, this.botDice));
      this.sendChannelMessage(this.createMessage("PLAYERS_TURN"));
      this.executor.schedule(new Dice.TimedPickWinnerTask(this, this.currentRoundNumber), this.timeToRoll, TimeUnit.SECONDS);
   }

   private void resetDice() {
      this.botDice.reset();
      Iterator i$ = this.playerDiceRolls.values().iterator();

      while(i$.hasNext()) {
         DiceRoll dice = (DiceRoll)i$.next();
         dice.reset();
      }

   }

   private void tallyRolls() {
      List<String> losers = new ArrayList();
      Iterator iterator = this.playerDiceRolls.keySet().iterator();

      while(iterator.hasNext()) {
         String player = (String)iterator.next();
         DiceRoll dice = (DiceRoll)this.playerDiceRolls.get(player);
         if (dice.total() == 0) {
            this.roll(player, true);
            if (log.isDebugEnabled()) {
               log.debug("Bot rolls for :" + player + ": " + dice.toString());
            }
         }

         if (dice.isWinner() && !this.hasWinner) {
            this.hasWinner = true;
         }

         if (!dice.isWinner()) {
            if (!this.isSafePlayer(this.currentRoundNumber, player)) {
               losers.add(player);
            } else if (log.isDebugEnabled()) {
               log.debug("Skipping removal of player " + player + "because they have immunity");
            }
         }
      }

      if (this.hasWinner) {
         if (log.isDebugEnabled()) {
            log.debug("Safe players: ");
            this.showSafePlayers(this.currentRoundNumber);
            log.debug("Next round safe players: ");
            this.showSafePlayers(this.currentRoundNumber + 1);
         }

         Iterator i$ = losers.iterator();

         while(i$.hasNext()) {
            String player = (String)i$.next();
            this.playerDiceRolls.remove(player);
            this.removePlayerFromPot(player);
            this.sendMessage(this.createMessage("PLAYER_LOST", player), player);
            if (log.isDebugEnabled()) {
               log.debug("botInstanceID[" + this.getInstanceID() + "]: " + "Removed user: " + player);
            }
         }
      }

      this.removeSafeList(this.currentRoundNumber);
      if (log.isDebugEnabled()) {
         log.debug("botInstanceID[" + this.getInstanceID() + "]: " + "Players remaining: " + this.playerDiceRolls.size());
      }

      if (this.playerDiceRolls.size() > 1) {
         if (log.isDebugEnabled()) {
            log.debug("botInstanceID[" + this.getInstanceID() + "]: " + "Players size > 1");
         }

         if (!this.hasWinner) {
            this.sendChannelMessage(this.createMessage("ALL_LOST_PLAY_AGAIN"));
         }

         this.isRoundStarted = false;
         this.sendChannelMessage(this.createMessage("NEXT_ROUND"));
         this.executor.schedule(new Runnable() {
            public void run() {
               Dice.this.newRound();
            }
         }, this.timeToNewRound, TimeUnit.SECONDS);
         if (log.isDebugEnabled()) {
            log.debug(" Started timer for a new round");
         }
      } else if (this.playerDiceRolls.size() == 1) {
         if (log.isDebugEnabled()) {
            log.debug("botInstanceID[" + this.getInstanceID() + "]: " + "Players size = 1");
         }

         this.pickWinner();
      }

   }

   private void pickWinner() {
      if (log.isDebugEnabled()) {
         log.debug("botInstanceID[" + this.getInstanceID() + "]: " + "Picking winner: ");
      }

      Iterator iterator = this.playerDiceRolls.keySet().iterator();
      String winner = (String)iterator.next();
      this.endGame(winner);
   }

   public static void main(String[] args) {
      try {
         ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
         BotData botData = new BotData();
         botData.setDisplayName("Dice");
         Dice dice = new Dice(executor, (BotChannelPrx)null, botData, "EN", "koko", (BotDAO)null);
         dice.timeToRoll = 5L;
         dice.timeToNewRound = 3L;
         dice.timeToConfirmCharge = 1L;
         dice.timeToJoinGame = 3L;
         dice.amountJoinPot = 0.0D;
         dice.messages.put("BOT_ADDED", "Bot BOTNAME added to room.");
         dice.messages.put("GAME_STATE_DEFAULT_AMOUNT", "Play now: CMD_START to enter. Cost: CURRENCY AMOUNT_POT. For custom entry, CMD_START <entry_amount>");
         dice.messages.put("ADDED_TO_GAME", "PLAYER: added to game.");
         dice.messages.put("GAME_JOIN_FREE", "Dice started. CMD_JOIN to join. TIMER_JOIN seconds");
         dice.messages.put("JOIN_NO_MIN", "Joining ends. Not enough players. Need MINPLAYERS.");
         dice.messages.put("JOIN", "PLAYER joined the game.");
         dice.messages.put("GAME_STARTED_NOTE", "Game begins! Bot rolls first - match or beat total to stay IN!");
         dice.messages.put("BOT_ROLLED", "ROUND #ROUND_NUMBER: Bot rolled DICE_VALUES Your TARGET: DICE_TOTAL!");
         dice.messages.put("PLAYERS_TURN", "Players: CMD_ROLL to roll. TIMER_ROLL seconds. ");
         dice.messages.put("AUTO_ROLL_OUT", "Bot rolls - PLAYER: DICE_VALUES OUT! ");
         dice.messages.put("AUTO_ROLL_HIGHER", "Bot rolls - PLAYER: DICE_VALUES IN!");
         dice.messages.put("AUTO_ROLL_MATCH", "Bot rolls - PLAYER: DICE_VALUES IN!");
         dice.messages.put("ALL_LOST_PLAY_AGAIN", "Nobody won, so we'll try again!");
         dice.messages.put("NEXT_ROUND", "Players, next round starts in TIMER_ROUND seconds!");
         dice.messages.put("GAME_OVER_FREE", "Game over! LEADER wins!! CONGRATS!");
         dice.messages.put("IMMUNITY", "PLAYER: DICE_VALUES = immunity for the next round!");
         dice.onMessage("koko", "!start", System.currentTimeMillis());
         Thread.sleep(1000L);
         dice.onMessage("kien", "!j", System.currentTimeMillis());
         Thread.sleep(3000L);
         dice.onUserLeaveChannel("koko");
         Thread.sleep(60000L);
         executor.shutdown();
         executor.awaitTermination(60L, TimeUnit.SECONDS);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   class TimedPickWinnerTask implements Runnable {
      Dice bot;
      int roundNumber;

      TimedPickWinnerTask(Dice bot, int roundNumber) {
         this.bot = bot;
         this.roundNumber = roundNumber;
      }

      public void run() {
         synchronized(this.bot) {
            if (this.bot.getGameState() == BotData.BotStateEnum.PLAYING && this.bot.currentRoundNumber == this.roundNumber) {
               this.bot.sendChannelMessage(this.bot.createMessage("TIME_UP"));
               this.bot.tallyRolls();
            }

         }
      }
   }

   class StartPlay implements Runnable {
      Dice bot;

      StartPlay(Dice bot) {
         this.bot = bot;
      }

      public void run() {
         try {
            if (Dice.log.isDebugEnabled()) {
               Dice.log.debug("DiceBot: starting play in StartPlay()");
            }

            BotData.BotStateEnum gameState = Dice.this.getGameState();
            if (gameState == BotData.BotStateEnum.GAME_JOINING) {
               Dice.this.setGameState(BotData.BotStateEnum.GAME_JOIN_ENDED);
               if (Dice.this.getPlayers().size() < Dice.this.minPlayers) {
                  Dice.this.sendChannelMessage(Dice.this.createMessage("JOIN_NO_MIN"));
                  if (Dice.log.isDebugEnabled()) {
                     Dice.log.debug("botInstanceID[" + Dice.this.getInstanceID() + "]: Join ended. Not enough players.");
                  }

                  Dice.this.resetGame(true);
               } else {
                  Set<String> copyOfPlayers = new HashSet();
                  copyOfPlayers.addAll(Dice.this.playerDiceRolls.keySet());
                  if (gameState != BotData.BotStateEnum.NO_GAME) {
                     gameState = BotData.BotStateEnum.PLAYING;

                     try {
                        if (Dice.this.amountJoinPot > 0.0D) {
                           this.bot.pot = new Pot(this.bot);
                           Dice.log.debug("Pot id[" + Dice.this.pot.getPotID() + "] created for bot instanceID[" + Dice.this.getInstanceID() + "]");
                           Iterator i$ = copyOfPlayers.iterator();

                           while(i$.hasNext()) {
                              String player = (String)i$.next();

                              try {
                                 Dice.this.pot.enterPlayer(player, Dice.this.amountJoinPot / 100.0D, "USD");
                                 if (Dice.log.isDebugEnabled()) {
                                    Dice.log.debug("botInstanceID[" + Dice.this.getInstanceID() + "]: Entered into pot " + player + " = " + "USD" + " " + Dice.this.amountJoinPot / 100.0D);
                                 }
                              } catch (Exception var6) {
                                 Dice.this.playerDiceRolls.remove(player);
                                 Dice.log.warn("botInstanceID[" + Dice.this.getInstanceID() + "]: Error charging player[" + player + "]", var6);
                                 Dice.this.sendMessage(Dice.this.createMessage("INSUFFICIENT_FUNDS_POT", player), player);
                              }
                           }

                           if (Dice.this.playerDiceRolls.size() < Dice.this.minPlayers) {
                              if (Dice.log.isDebugEnabled()) {
                                 Dice.log.debug("botInstanceID[" + Dice.this.getInstanceID() + "]: Not enough valid players.");
                              }

                              this.cancelPot();
                              Dice.this.resetGame(true);
                              return;
                           }
                        }

                        Dice.this.incrementGamesPlayed(Leaderboard.Type.DICE_GAMES_PLAYED, Dice.this.playerDiceRolls.keySet());
                        Dice.this.logGamesPlayed(Dice.this.playerDiceRolls.size(), Dice.this.playerDiceRolls.keySet(), Dice.this.amountJoinPot);
                        Dice.this.sendChannelMessage(Dice.this.createMessage("GAME_STARTED_NOTE"));
                        Dice.this.setGameState(BotData.BotStateEnum.PLAYING);
                        Dice.this.newRound();
                     } catch (Exception var7) {
                        Dice.log.error("Error creating pot for botInstanceID[" + Dice.this.getInstanceID() + "].", var7);
                        Dice.this.setGameState(BotData.BotStateEnum.NO_GAME);
                        Dice.this.sendChannelMessage(Dice.this.createMessage("GAME_CANCELED"));
                     }
                  } else {
                     this.cancelPot();
                     Dice.this.resetGame(true);
                     if (Dice.log.isDebugEnabled()) {
                        Dice.log.debug("botInstanceID[" + Dice.this.getInstanceID() + "]: Billing error. Game canceled. No charges.");
                     }
                  }
               }
            }
         } catch (Exception var8) {
            Dice.log.error("Unexpected exception caught in startPlay.run()", var8);
            this.cancelPot();
            Dice.this.resetGame(true);
         }

      }

      private void cancelPot() {
         try {
            if (Dice.this.pot != null) {
               Dice.this.pot.cancel();
            }
         } catch (Exception var2) {
            Dice.log.error("Error canceling pot for botInstanceID[" + Dice.this.getInstanceID() + "].", var2);
         }

         Dice.this.sendChannelMessage(Dice.this.createMessage("GAME_CANCELED"));
      }
   }

   class StartGame implements Runnable {
      Dice bot;

      StartGame(Dice bot) {
         this.bot = bot;
      }

      public void run() {
         if (Dice.log.isDebugEnabled()) {
            Dice.log.debug("botInstanceID[" + Dice.this.getInstanceID() + "]: in StartGame() ");
         }

         BotData.BotStateEnum gameState = null;
         gameState = Dice.this.getGameState();
         if (gameState == BotData.BotStateEnum.GAME_STARTING) {
            Dice.this.setGameState(BotData.BotStateEnum.GAME_STARTED);
            Dice.this.addPlayer(Dice.this.gameStarter);
            if (Dice.this.timeToJoinGame > 0L) {
               Dice.this.setGameState(BotData.BotStateEnum.GAME_JOINING);
               Dice.this.sendChannelMessage(Dice.this.amountJoinPot > 0.0D ? Dice.this.createMessage("GAME_JOIN_PAID") : Dice.this.createMessage("GAME_JOIN_FREE"));
               if (Dice.log.isDebugEnabled()) {
                  Dice.log.debug("DiceBot: starting timer for StartPlay()");
               }

               Dice.this.executor.schedule(Dice.this.new StartPlay(this.bot), Dice.this.timeToJoinGame, TimeUnit.SECONDS);
               if (Dice.log.isDebugEnabled()) {
                  Dice.log.debug("botInstanceID[" + Dice.this.getInstanceID() + "]: scheduled to start play. Awaiting join.. ");
               }
            }
         }

      }
   }
}
