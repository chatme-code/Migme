package com.projectgoth.fusion.botservice.bot.migbot.russianroulette;

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
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class RussianRoulette extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RussianRoulette.class));
   public static final String TIMER_JOIN_GAME = "timerJoinGame";
   public static final String TIMER_CHARGE_CONF = "timerChargeConfirm";
   public static final String TIMER_SPIN = "timerSpin";
   public static final String TIMER_IDLE = "timerIdle";
   public static final String AMOUNT_JOIN_POT = "amountJoinPot";
   public static final long TIMER_SPIN_VALUE = 10L;
   public static final double AMOUNT_JOIN_POT_VALUE = 5.0D;
   public static final long IDLE_TIME_VALUE = 3L;
   long timeToJoinGame = 90L;
   long timeToConfirmCharge = 20L;
   long timeToSpin = 10L;
   double amountJoinPot = 5.0D;
   double winnings = 0.0D;
   public int minPlayers = 2;
   long timeAllowedToIdle = 30L;
   double amountOriginalJoinPot = 5.0D;
   public static final String COMMAND_SPIN = "!s";
   Date lastActivityTime;
   private BotData.BotStateEnum gameState;
   int currentRoundNumber;
   String currentPlayer;
   public List<String> players;
   List<String> playersRemaining;
   ScheduledFuture nextSpinTimerTask;

   public RussianRoulette(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.currentRoundNumber = 0;
      this.nextSpinTimerTask = null;
      this.loadGameConfig();
      log.info("RussianRouletteBot [" + this.instanceID + "] added to channel [" + this.channel + "]");
      this.sendChannelMessage(this.createMessage("BOT_ADDED"));
      String message = this.amountJoinPot > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
      this.sendChannelMessage(message);
      this.players = new ArrayList();
      this.playersRemaining = new ArrayList();
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

      if (this.nextSpinTimerTask != null && !this.nextSpinTimerTask.isDone() && !this.nextSpinTimerTask.isCancelled()) {
         this.nextSpinTimerTask.cancel(true);
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
            this.resetGame();
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
      this.timeToSpin = this.getLongParameter("timerSpin", 10L);
      this.amountJoinPot = this.getDoubleParameter("amountJoinPot", 5.0D);
      this.amountOriginalJoinPot = this.amountJoinPot;
      this.timeAllowedToIdle = this.getLongParameter("timerIdle", 3L);
   }

   public void onMessage(String username, String messageText, long receivedTimestamp) {
      synchronized(this) {
         if (messageText.equalsIgnoreCase("!n")) {
            this.processNoMessage(username);
         } else if (messageText.toLowerCase().startsWith("!start")) {
            if (this.getGameState() == BotData.BotStateEnum.NO_GAME) {
               if (messageText.trim().length() > "!start".length()) {
                  String parameter = messageText.trim().substring("!start".length() + 1);
                  if (StringUtils.hasLength(parameter) && this.checkJoinPotParameter(parameter, username)) {
                     try {
                        this.startGame(username);
                     } catch (Exception var10) {
                        log.error("Error starting game with custom amount. Command was : '" + messageText + "'", var10);
                     }
                  }
               } else {
                  try {
                     this.startGame(username);
                  } catch (Exception var9) {
                     log.error("Error starting game with default amount: ", var9);
                  }
               }
            } else {
               this.sendGameCannotStartMessage(username);
            }
         } else if (messageText.equalsIgnoreCase("!j")) {
            if (!this.players.contains(username)) {
               if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
                  this.addPlayer(username);
               } else {
                  this.sendMessage(this.createMessage("JOIN_ENDED", username), username);
               }
            } else {
               this.sendMessage(this.createMessage("ALREADY_IN_GAME", username), username);
            }
         } else if (messageText.equalsIgnoreCase("!s") && this.gameState == BotData.BotStateEnum.PLAYING) {
            if (!username.equals(this.currentPlayer)) {
               this.sendMessage(this.createMessage("NOT_YOUR_TURN", username), username);
            } else {
               this.sendChannelMessage(this.createMessage("PLAYER_SPINS", username));
               if (log.isDebugEnabled()) {
                  log.debug("botInstanceID[" + this.instanceID + "]: " + username + " spins");
               }

               if (this.nextSpinTimerTask != null && !this.nextSpinTimerTask.isCancelled()) {
                  this.nextSpinTimerTask.cancel(false);
                  if (log.isDebugEnabled()) {
                     log.debug("User " + username + " spins for themselves. Auto-spin canceled? " + this.nextSpinTimerTask.isCancelled() + ". Or was it done? " + this.nextSpinTimerTask.isDone());
                  }
               }

               this.spin(username);
            }
         }

      }
   }

   private void sendGameCannotStartMessage(String username) {
      String message = null;
      switch(this.gameState.value()) {
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

   public List<String> getPlayers() {
      return this.players;
   }

   public void addPlayer(String username) {
      if (this.getGameState() == BotData.BotStateEnum.GAME_STARTED || this.getGameState() == BotData.BotStateEnum.GAME_JOINING) {
         if (this.amountJoinPot > 0.0D) {
            boolean hasFunds = this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, !this.gameStarter.equals(username));
            if (!hasFunds) {
               return;
            }
         }

         synchronized(this.players) {
            if (!this.players.contains(username)) {
               this.players.add(username);
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

   void spin(String player) {
      if (this.players.size() > 1) {
         Random ChamberValue = new Random(System.currentTimeMillis());
         int intChamber = ChamberValue.nextInt(6);
         if (intChamber == 5) {
            if (log.isDebugEnabled()) {
               log.debug("botInstanceID[" + this.instanceID + "]: " + player + " got a BANG");
            }

            this.sendChannelMessage(this.createMessage("BANG", player));
            synchronized(this.players) {
               this.players.remove(player);
            }

            try {
               this.pot.removePlayer(player);
            } catch (Exception var6) {
               log.error("botInstanceID[" + this.instanceID + "]: Problem removing player[" + player + "] from pot stake. ");
            }
         } else {
            if (log.isDebugEnabled()) {
               log.debug("botInstanceID[" + this.instanceID + "]: " + player + " got a CLICK");
            }

            this.sendChannelMessage(this.createMessage("CLICK", player));
         }

         if (this.players.size() > 1) {
            this.nextPlayer();
         } else if (this.players.size() == 1) {
            this.endGame();
         }
      }

   }

   void nextPlayer() {
      if (this.players.size() > 1) {
         if (this.playersRemaining.isEmpty()) {
            this.playersRemaining.addAll(this.players);
            if (++this.currentRoundNumber > 1) {
               this.sendChannelMessage(this.createMessage("NEXT_ROUND"));
            }

            this.sendChannelMessage(this.createMessage("SPIN_ORDER"));
         }

         this.currentPlayer = (String)this.playersRemaining.remove(0);
         if (this.currentPlayer == null) {
            log.warn("Unable to assign current player. playersRemaining.remove(0) return null");
            this.resetGame();
            return;
         }

         if (log.isDebugEnabled()) {
            log.debug("botInstanceID[" + this.instanceID + "]: Next spin by " + this.currentPlayer);
         }

         this.sendChannelMessage(this.createMessage("PLAYER_TURN_TO_SPIN", this.currentPlayer));
         this.nextSpinTimerTask = this.executor.schedule(new RussianRoulette.TimedAutoSpinTask(this, this.currentPlayer), this.timeToSpin, TimeUnit.SECONDS);
      }

   }

   public void processNoMessage(String username) {
      String message = null;
      switch(this.getGameState().value()) {
      case 1:
         if (username.equals(this.gameStarter) && this.amountJoinPot > 0.0D) {
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
            String message = this.createMessage("INVALID_AMOUNT", username, parameter);
            if (log.isDebugEnabled()) {
               log.debug("Lower value specified for amountJoinPot: " + parameter);
            }

            this.sendMessage(message, username);
         }

         if (log.isDebugEnabled()) {
            log.debug("Parameter defined : amountJoinPot=" + this.amountJoinPot);
         }
      } catch (Exception var7) {
         String message = this.createMessage("INVALID_AMOUNT", username, parameter);
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
                  log.debug("RussianRouletteBot: starting timer for StartGame()");
               }

               this.executor.schedule(new RussianRoulette.StartGame(this), this.timeToConfirmCharge, TimeUnit.SECONDS);
               if (log.isDebugEnabled()) {
                  log.debug("RussianRouletteBot: started timer for StartGame()");
               }
            } else {
               this.resetGame();
            }
         } else {
            if (log.isDebugEnabled()) {
               log.debug("botInstanceID[" + this.getInstanceID() + "]: No charges. Game started by user[" + username + "]");
            }

            this.setGameState(BotData.BotStateEnum.GAME_STARTING);
            this.gameStarter = username;
            if (log.isDebugEnabled()) {
               log.debug("RussianRouletteBot: starting timer for StartGame()");
            }

            this.executor.execute(new RussianRoulette.StartGame(this));
            if (log.isDebugEnabled()) {
               log.debug("RussianRouletteBot: started timer for StartGame()");
            }
         }
      } else {
         this.sendGameCannotStartMessage(username);
      }

   }

   public void onUserJoinChannel(String username) {
      String message = null;
      switch(this.getGameState().value()) {
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
         if (this.pot != null) {
            try {
               this.pot.removePlayer(username);
            } catch (Exception var7) {
               log.error("BotInstanceID: " + this.instanceID + "]: Error making player " + username + "] ineligible for pot.", var7);
            }
         }

         synchronized(this.playersRemaining) {
            if (this.playersRemaining.contains(username)) {
               this.playersRemaining.remove(username);
            }
         }

         if (this.players != null) {
            synchronized(this.players) {
               if (this.players.contains(username)) {
                  this.players.remove(username);
                  this.sendChannelMessage(this.createMessage("PLAYER_LEFT", username));
               }
            }

            if (this.getGameState() == BotData.BotStateEnum.PLAYING) {
               if (this.players.size() >= this.minPlayers && username.equals(this.currentPlayer)) {
                  if (this.nextSpinTimerTask != null && !this.nextSpinTimerTask.isDone() && !this.nextSpinTimerTask.isCancelled()) {
                     this.nextSpinTimerTask.cancel(true);
                  }

                  this.nextPlayer();
               } else if (this.players.size() < this.minPlayers) {
                  this.endGame();
               }
            }
         }

      }
   }

   public void endGame() {
      try {
         if (this.getGameState() != BotData.BotStateEnum.PLAYING) {
            return;
         }

         if (this.nextSpinTimerTask != null && !this.nextSpinTimerTask.isDone() && !this.nextSpinTimerTask.isCancelled()) {
            log.debug("botInstanceID[" + this.getInstanceID() + "]: Pending timer task to cancel in endGame() ");
            this.nextSpinTimerTask.cancel(true);
         }

         if (!this.players.isEmpty()) {
            String winner = (String)this.players.get(0);
            Pot localPot = this.pot;
            if (localPot != null) {
               try {
                  this.winnings = localPot.payout(true);
                  Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
                  this.winnings = accountEJB.convertCurrency(this.winnings, "AUD", "USD");
                  log.debug("Game over. Pot [" + localPot.getPotID() + "] payout completed.");
               } catch (Exception var9) {
                  log.error("Game over. Error in pot [" + localPot.getPotID() + "] payout.", var9);
               }
            }

            if (this.winnings < 0.0D) {
               this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
            } else {
               this.sendChannelMessageAndPopUp(this.amountJoinPot > 0.0D ? this.createMessage("GAME_OVER_PAID", winner) : this.createMessage("GAME_OVER_FREE", winner));
            }
         } else {
            log.debug("botInstanceID[" + this.getInstanceID() + "]: endGame(): There are no players remaining. players empty.");
         }
      } catch (Exception var10) {
         log.error("botInstanceID[" + this.getInstanceID() + "]: Error getting game winner. ", var10);
      } finally {
         this.resetGame();
         this.updateLastActivityTime();
         this.sendChannelMessage(this.amountJoinPot > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT"));
      }

   }

   void resetGame() {
      this.nextSpinTimerTask = null;
      this.currentRoundNumber = 0;
      this.currentPlayer = null;
      this.gameStarter = null;
      this.players.clear();
      this.playersRemaining.clear();
      this.pot = null;
      this.amountJoinPot = this.amountOriginalJoinPot;
      this.setGameState(BotData.BotStateEnum.NO_GAME);
   }

   String createMessage(String messageKey, String username) {
      return this.createMessage(messageKey, username, (String)null);
   }

   protected String createMessage(String messageKey) {
      return this.createMessage(messageKey, (String)null, (String)null);
   }

   private String createMessage(String messageKey, String player, String errorInput) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Looking for messageKey: " + messageKey);
         }

         String messageToSend = (String)this.messages.get(messageKey);
         messageToSend = messageToSend.replaceAll("BOTNAME", this.botData.getDisplayName());
         messageToSend = messageToSend.replaceAll("CONF_TIMER", this.timeToConfirmCharge + "");
         messageToSend = messageToSend.replaceAll("TIMER_JOIN", "" + this.timeToJoinGame);
         messageToSend = messageToSend.replaceAll("TIMER_SPIN", "" + this.timeToSpin);
         messageToSend = messageToSend.replaceAll("SPIN_COMMAND", "!s");
         messageToSend = messageToSend.replaceAll("CMD_NO", "!n");
         messageToSend = messageToSend.replaceAll("CMD_JOIN", "!j");
         messageToSend = messageToSend.replaceAll("CMD_START", "!start");
         messageToSend = messageToSend.replaceAll("MINPLAYERS", this.minPlayers + "");
         if (player != null) {
            messageToSend = messageToSend.replaceAll("PLAYER", player);
            messageToSend = messageToSend.replaceAll("LEADER", player);
         }

         messageToSend = messageToSend.replaceAll("CURRENCY", "USD");
         messageToSend = messageToSend.replaceAll("AMOUNT_POT", this.amountJoinPot / 100.0D + "");
         messageToSend = messageToSend.replaceAll("CUSTOM_MIN_AMOUNT", this.amountJoinPot + 1.0D + "");
         if (this.winnings > 0.0D) {
            DecimalFormat df = new DecimalFormat("0.00");
            messageToSend = messageToSend.replaceAll("WINNINGS", df.format(this.winnings));
         }

         if (StringUtils.hasLength(errorInput)) {
            errorInput = errorInput.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\\\$");
            messageToSend = messageToSend.replaceAll("ERROR_INPUT", errorInput);
         }

         if (this.currentRoundNumber != 0) {
            messageToSend = messageToSend.replaceAll("ROUND_NUMBER", this.currentRoundNumber + "");
         }

         if (this.playersRemaining != null && this.playersRemaining.size() > 1) {
            String playerListString = this.stringifyPlayerList();
            messageToSend = messageToSend.replaceAll("PLAYERS", playerListString);
         }

         if (log.isDebugEnabled()) {
            log.debug("Found message for key:" + messageToSend);
         }

         return messageToSend;
      } catch (NullPointerException var6) {
         log.error("Outgoing message could not be created, key = " + messageKey, var6);
         return "";
      }
   }

   private String stringifyPlayerList() {
      StringBuilder playerList = new StringBuilder();

      for(int i = 0; i < this.playersRemaining.size(); ++i) {
         playerList.append((String)this.playersRemaining.get(i)).append(", ");
      }

      String playerListString = playerList.toString();
      return playerListString.endsWith(", ") ? playerListString.substring(0, playerListString.length() - 2) : playerListString;
   }

   class TimedAutoSpinTask implements Runnable {
      RussianRoulette bot;
      String player;

      TimedAutoSpinTask(RussianRoulette bot, String player) {
         this.bot = bot;
         this.player = player;
      }

      public void run() {
         synchronized(this.bot) {
            if (this.bot.getGameState() == BotData.BotStateEnum.PLAYING && this.player.equals(RussianRoulette.this.currentPlayer)) {
               this.bot.sendChannelMessage(this.bot.createMessage("TIME_UP_AUTO_SPIN", RussianRoulette.this.currentPlayer));
               this.bot.spin(RussianRoulette.this.currentPlayer);
            }

         }
      }
   }

   class StartPlay implements Runnable {
      RussianRoulette bot;

      StartPlay(RussianRoulette bot) {
         this.bot = bot;
      }

      public void run() {
         try {
            if (RussianRoulette.log.isDebugEnabled()) {
               RussianRoulette.log.debug("RussianRouletteBot: starting play in StartPlay()");
            }

            BotData.BotStateEnum gameState = RussianRoulette.this.getGameState();
            if (gameState == BotData.BotStateEnum.GAME_JOINING) {
               RussianRoulette.this.setGameState(BotData.BotStateEnum.GAME_JOIN_ENDED);
               if (RussianRoulette.this.getPlayers().size() < RussianRoulette.this.minPlayers) {
                  RussianRoulette.this.sendChannelMessage(RussianRoulette.this.createMessage("JOIN_NO_MIN"));
                  if (RussianRoulette.log.isDebugEnabled()) {
                     RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: Join ended. Not enough players.");
                  }

                  RussianRoulette.this.setGameState(BotData.BotStateEnum.NO_GAME);
               } else {
                  boolean hasFunds = false;
                  if (RussianRoulette.this.amountJoinPot > 0.0D) {
                     hasFunds = RussianRoulette.this.userCanAffordToEnterPot(RussianRoulette.this.gameStarter, RussianRoulette.this.amountJoinPot / 100.0D, false);
                     if (!hasFunds) {
                        RussianRoulette.this.setGameState(BotData.BotStateEnum.NO_GAME);
                        if (RussianRoulette.log.isDebugEnabled()) {
                           RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: " + RussianRoulette.this.gameStarter + " has insufficient funds.");
                        }

                        return;
                     }
                  }

                  Set<String> copyOfPlayers = new HashSet();
                  copyOfPlayers.addAll(RussianRoulette.this.players);
                  Iterator i$;
                  String player;
                  if (RussianRoulette.this.amountJoinPot > 0.0D) {
                     i$ = copyOfPlayers.iterator();

                     label119: {
                        do {
                           do {
                              do {
                                 if (!i$.hasNext()) {
                                    break label119;
                                 }

                                 player = (String)i$.next();
                              } while(player.equals(RussianRoulette.this.gameStarter));

                              hasFunds = RussianRoulette.this.userCanAffordToEnterPot(player, RussianRoulette.this.amountJoinPot / 100.0D, false);
                           } while(hasFunds);

                           RussianRoulette.this.players.remove(player);
                           if (RussianRoulette.log.isDebugEnabled()) {
                              RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: " + player + " has insufficient funds.");
                           }
                        } while(RussianRoulette.this.players.size() >= RussianRoulette.this.minPlayers && RussianRoulette.this.players.size() != 0);

                        RussianRoulette.this.setGameState(BotData.BotStateEnum.NO_GAME);
                        if (RussianRoulette.log.isDebugEnabled()) {
                           RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: Not enough valid players.");
                        }
                     }

                     if (gameState == BotData.BotStateEnum.NO_GAME) {
                        RussianRoulette.log.error("Game canceled: instanceID[" + RussianRoulette.this.getInstanceID() + "]");
                        RussianRoulette.this.sendChannelMessage(RussianRoulette.this.createMessage("GAME_CANCELED"));
                        return;
                     }
                  }

                  if (gameState != BotData.BotStateEnum.NO_GAME) {
                     gameState = BotData.BotStateEnum.PLAYING;

                     try {
                        if (RussianRoulette.this.amountJoinPot > 0.0D) {
                           this.bot.pot = new Pot(this.bot);
                           RussianRoulette.log.debug("Pot id[" + RussianRoulette.this.pot.getPotID() + "] created for bot instanceID[" + RussianRoulette.this.getInstanceID() + "]");
                           i$ = copyOfPlayers.iterator();

                           while(true) {
                              if (!i$.hasNext()) {
                                 if (RussianRoulette.this.players.size() < RussianRoulette.this.minPlayers || RussianRoulette.this.players.size() == 0) {
                                    if (RussianRoulette.log.isDebugEnabled()) {
                                       RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: Not enough valid players.");
                                    }

                                    this.cancelPot();
                                    RussianRoulette.this.resetGame();
                                    return;
                                 }
                                 break;
                              }

                              player = (String)i$.next();

                              try {
                                 RussianRoulette.this.pot.enterPlayer(player, RussianRoulette.this.amountJoinPot / 100.0D, "USD");
                                 if (RussianRoulette.log.isDebugEnabled()) {
                                    RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: Entered into pot " + player + " = " + "USD" + " " + RussianRoulette.this.amountJoinPot / 100.0D);
                                 }
                              } catch (Exception var7) {
                                 RussianRoulette.this.players.remove(player);
                                 RussianRoulette.log.error("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: Error charging player[" + player + "]", var7);
                                 RussianRoulette.this.sendMessage(RussianRoulette.this.createMessage("INSUFFICIENT_FUNDS_POT", player), player);
                              }
                           }
                        }

                        RussianRoulette.log.info("New game started in " + RussianRoulette.this.channel);
                        RussianRoulette.this.sendChannelMessage(RussianRoulette.this.createMessage("GAME_STARTED_NOTE"));
                        RussianRoulette.this.setGameState(BotData.BotStateEnum.PLAYING);
                        Collections.shuffle(RussianRoulette.this.players);
                        RussianRoulette.this.nextPlayer();
                     } catch (Exception var8) {
                        RussianRoulette.log.error("Error creating pot for botInstanceID[" + RussianRoulette.this.getInstanceID() + "].", var8);
                        RussianRoulette.this.setGameState(BotData.BotStateEnum.NO_GAME);
                        RussianRoulette.this.sendChannelMessage(RussianRoulette.this.createMessage("GAME_CANCELED"));
                     }
                  } else {
                     this.cancelPot();
                     RussianRoulette.this.resetGame();
                     if (RussianRoulette.log.isDebugEnabled()) {
                        RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: Billing error. Game canceled. No charges.");
                     }
                  }
               }
            }
         } catch (Exception var9) {
            RussianRoulette.log.error("Unexpected exception caught in startPlay.run()", var9);
            this.cancelPot();
            RussianRoulette.this.resetGame();
         }

      }

      private void cancelPot() {
         try {
            if (RussianRoulette.this.pot != null) {
               RussianRoulette.this.pot.cancel();
            }
         } catch (Exception var2) {
            RussianRoulette.log.error("Error canceling pot for botInstanceID[" + RussianRoulette.this.getInstanceID() + "].", var2);
         }

         RussianRoulette.this.sendChannelMessage(RussianRoulette.this.createMessage("GAME_CANCELED"));
      }
   }

   class StartGame implements Runnable {
      RussianRoulette bot;

      StartGame(RussianRoulette bot) {
         this.bot = bot;
      }

      public void run() {
         if (RussianRoulette.log.isDebugEnabled()) {
            RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: in StartGame() ");
         }

         BotData.BotStateEnum gameState = null;
         gameState = RussianRoulette.this.getGameState();
         if (gameState == BotData.BotStateEnum.GAME_STARTING) {
            RussianRoulette.this.setGameState(BotData.BotStateEnum.GAME_STARTED);
            RussianRoulette.this.addPlayer(RussianRoulette.this.gameStarter);
            if (RussianRoulette.this.timeToJoinGame > 0L) {
               RussianRoulette.this.setGameState(BotData.BotStateEnum.GAME_JOINING);
               RussianRoulette.this.sendChannelMessage(RussianRoulette.this.amountJoinPot > 0.0D ? RussianRoulette.this.createMessage("GAME_JOIN_PAID") : RussianRoulette.this.createMessage("GAME_JOIN_FREE"));
               if (RussianRoulette.log.isDebugEnabled()) {
                  RussianRoulette.log.debug("RussianRouletteBot: starting timer for StartPlay()");
               }

               RussianRoulette.this.executor.schedule(RussianRoulette.this.new StartPlay(this.bot), RussianRoulette.this.timeToJoinGame, TimeUnit.SECONDS);
               if (RussianRoulette.log.isDebugEnabled()) {
                  RussianRoulette.log.debug("botInstanceID[" + RussianRoulette.this.getInstanceID() + "]: scheduled to start play. Awaiting join.. ");
               }
            }
         }

      }
   }
}
