package com.projectgoth.fusion.botservice.bot.migbot.lowcard;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.botservice.bot.Pot;
import com.projectgoth.fusion.botservice.bot.migbot.common.Card;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class LowCard extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LowCard.class));
   public static final String TIMER_JOIN_GAME = "timerJoinGame";
   public static final String TIMER_CHARGE_CONF = "timerChargeConfirm";
   public static final String TIMER_DRAW = "timerDraw";
   public static final String TIMER_NEW_ROUND_INTERVAL = "timerNewRound";
   public static final String TIMER_IDLE = "timerIdle";
   public static final String AMOUNT_JOIN_POT = "amountJoinPot";
   public static final long TIMER_DRAW_VALUE = 10L;
   public static final long TIMER_NEW_ROUND_VALUE = 3L;
   public static final double AMOUNT_JOIN_POT_VALUE = 5.0D;
   public static final long IDLE_TIME_VALUE = 3L;
   long timeToJoinGame = 90L;
   long timeToConfirmCharge = 20L;
   long timeToDraw = 10L;
   long timeToNewRound = 10L;
   double amountJoinPot = 5.0D;
   double winnings = 0.0D;
   public int minPlayers = 2;
   long timeAllowedToIdle = 30L;
   double amountOriginalJoinPot = 5.0D;
   public static final String COMMAND_DRAW = "!d";
   Date lastActivityTime;
   private Map<String, LowCard.Hand> playerHands = new HashMap();
   private Map<String, LowCard.Hand> tiebreakerHands = new HashMap();
   private BotData.BotStateEnum gameState;
   private boolean isRoundStarted;
   private boolean isTiebreaker;
   LowCard.Deck deck;
   LowCard.Hand lowestHandAlreadyLeft;
   int currentRoundNumber;
   ScheduledFuture nextDrawTimerTask;

   public LowCard(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.isRoundStarted = false;
      this.isTiebreaker = false;
      this.deck = new LowCard.Deck();
      this.lowestHandAlreadyLeft = null;
      this.currentRoundNumber = 0;
      this.nextDrawTimerTask = null;
      this.loadGameConfig();
      this.loadCardEmoticons();
      log.info("LowCardBot [" + this.instanceID + "] added to channel [" + this.channel + "]");
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

   private void loadCardEmoticons() {
      String[] cardEmoticons = new String[Card.EMOTICONS.length + (this.emoticonHotKeys != null ? this.emoticonHotKeys.length : 0)];

      int i;
      for(i = 0; i < Card.EMOTICONS.length; ++i) {
         cardEmoticons[i] = Card.EMOTICONS[i];
      }

      if (this.emoticonHotKeys != null) {
         i = 0;

         for(int j = Card.EMOTICONS.length; i < this.emoticonHotKeys.length; ++j) {
            cardEmoticons[j] = this.emoticonHotKeys[i];
            ++i;
         }
      }

      this.emoticonHotKeys = cardEmoticons;
   }

   public void stopBot() {
      if (log.isDebugEnabled()) {
         log.debug("Stopping bot instanceID[" + this.instanceID + "]");
      }

      if (this.nextDrawTimerTask != null && !this.nextDrawTimerTask.isDone() && !this.nextDrawTimerTask.isCancelled()) {
         this.nextDrawTimerTask.cancel(true);
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
      this.timeToDraw = this.getLongParameter("timerDraw", 10L);
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
         } else if (messageText.toLowerCase().startsWith("!d")) {
            if (this.gameState == BotData.BotStateEnum.PLAYING && this.isRoundStarted) {
               if (!this.playerHands.containsKey(username)) {
                  this.sendMessage(this.createMessage("NOT_IN_GAME", username), username);
               } else {
                  if (this.isTiebreaker && !this.tiebreakerHands.containsKey(username)) {
                     this.sendMessage(this.createMessage("ONLY_TIED_PLAYERS", username), username);
                     return;
                  }

                  this.draw(username, "", false);
               }
            } else {
               this.sendMessage(this.createMessage("INVALID_COMMAND", username), username);
            }
         }

      }
   }

   private void join(String username) {
      if (!this.playerHands.containsKey(username)) {
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

   public Map<String, LowCard.Hand> getPlayers() {
      return this.playerHands;
   }

   public void addPlayer(String username) {
      if (this.getGameState() == BotData.BotStateEnum.GAME_STARTED || this.getGameState() == BotData.BotStateEnum.GAME_JOINING) {
         if (this.amountJoinPot > 0.0D) {
            boolean hasFunds = this.userCanAffordToEnterPot(username, this.amountJoinPot / 100.0D, !this.gameStarter.equals(username));
            if (!hasFunds) {
               return;
            }
         }

         synchronized(this.playerHands) {
            if (!this.playerHands.containsKey(username)) {
               this.playerHands.put(username, new LowCard.Hand(username));
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
            String message = this.createMessage("INVALID_AMOUNT", username, (Card)null, parameter);
            if (log.isDebugEnabled()) {
               log.debug("Lower value specified for amountJoinPot: " + parameter);
            }

            this.sendMessage(message, username);
         }

         if (log.isDebugEnabled()) {
            log.debug("Parameter defined : amountJoinPot=" + this.amountJoinPot);
         }
      } catch (Exception var7) {
         String message = this.createMessage("INVALID_AMOUNT", username, (Card)null, parameter);
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
                  log.debug("LowCardBot: starting timer for StartGame()");
               }

               this.executor.schedule(new LowCard.StartGame(this), this.timeToConfirmCharge, TimeUnit.SECONDS);
               if (log.isDebugEnabled()) {
                  log.debug("LowCardBot: started timer for StartGame()");
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
               log.debug("LowCardBot: starting timer for StartGame()");
            }

            this.executor.execute(new LowCard.StartGame(this));
            if (log.isDebugEnabled()) {
               log.debug("LowCardBot: started timer for StartGame()");
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
         if (this.playerHands != null) {
            Map<String, LowCard.Hand> currentHands = this.isTiebreaker ? this.tiebreakerHands : this.playerHands;
            LowCard.Hand hand = (LowCard.Hand)currentHands.get(username);
            if (hand != null && hand.getCard() != null && (this.lowestHandAlreadyLeft == null || hand.compareTo(this.lowestHandAlreadyLeft) < 0)) {
               this.lowestHandAlreadyLeft = hand;
            }

            if (this.playerHands.containsKey(username)) {
               this.playerHands.remove(username);
               this.sendChannelMessage(this.createMessage("PLAYER_LEFT", username));
            }

            if (this.tiebreakerHands.containsKey(username)) {
               this.tiebreakerHands.remove(username);
            }

            if (this.getGameState() == BotData.BotStateEnum.PLAYING && this.playerHands.size() < this.minPlayers) {
               if (this.nextDrawTimerTask != null && !this.nextDrawTimerTask.isDone() && !this.nextDrawTimerTask.isCancelled()) {
                  log.debug("botInstanceID[" + this.getInstanceID() + "]: Pending timer task to cancel in endGame() ");
                  this.nextDrawTimerTask.cancel(true);
               }

               this.pickWinner();
            }
         }

      }
   }

   private void removePlayerFromPot(String username) {
      if (log.isDebugEnabled()) {
         log.debug("Player lost: " + username + ". Removing from pot.");
      }

      if (this.pot != null) {
         try {
            this.pot.removePlayer(username);
         } catch (Exception var3) {
            log.error("BotInstanceID: " + this.instanceID + "]: Error removing player " + username + "] from pot.", var3);
         }
      }

   }

   private void endGame(String winner) {
      try {
         if (this.getGameState() != BotData.BotStateEnum.PLAYING) {
            return;
         }

         if (this.nextDrawTimerTask != null && !this.nextDrawTimerTask.isDone() && !this.nextDrawTimerTask.isCancelled()) {
            log.debug("botInstanceID[" + this.getInstanceID() + "]: Pending timer task to cancel in endGame() ");
            this.nextDrawTimerTask.cancel(true);
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

         if (winner != null) {
            this.sendChannelMessageAndPopUp(this.amountJoinPot > 0.0D ? this.createMessage("GAME_OVER_PAID", winner) : this.createMessage("GAME_OVER_FREE", winner));
            this.logMostWins(winner, this.winnings);
            this.incrementMostWins(Leaderboard.Type.LOW_CARD_MOST_WINS, winner);
         }
      } catch (Exception var10) {
         log.error("botInstanceID[" + this.getInstanceID() + "]: Error getting game winner. ", var10);
      } finally {
         this.resetGame(false);
         this.updateLastActivityTime();
         this.sendChannelMessage(this.amountJoinPot > 0.0D ? this.createMessage("GAME_STATE_DEFAULT_AMOUNT") : this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT"));
      }

   }

   void resetGame(boolean cancel) {
      if (cancel) {
         this.revertLimitInCache(this.playerHands.keySet());
      }

      this.playerHands.clear();
      this.tiebreakerHands.clear();
      this.isTiebreaker = false;
      this.lowestHandAlreadyLeft = null;
      this.nextDrawTimerTask = null;
      this.currentRoundNumber = 0;
      this.isRoundStarted = false;
      this.gameStarter = null;
      this.pot = null;
      this.amountJoinPot = this.amountOriginalJoinPot;
      this.setGameState(BotData.BotStateEnum.NO_GAME);
   }

   protected String createMessage(String messageKey) {
      return this.createMessage(messageKey, (String)null, (Card)null, (String)null);
   }

   String createMessage(String messageKey, String username) {
      return this.createMessage(messageKey, username, (Card)null, (String)null);
   }

   private String createMessage(String messageKey, String username, Card card) {
      return this.createMessage(messageKey, username, card, (String)null);
   }

   private String createMessage(String messageKey, String player, Card card, String errorInput) {
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
         messageToSend = messageToSend.replaceAll("TIMER_DRAW", "" + this.timeToDraw);
         messageToSend = messageToSend.replaceAll("TIMER_ROUND", "" + this.timeToNewRound);
         messageToSend = messageToSend.replaceAll("CMD_DRAW", "!d");
         messageToSend = messageToSend.replaceAll("CMD_NO", "!n");
         messageToSend = messageToSend.replaceAll("CMD_JOIN", "!j");
         messageToSend = messageToSend.replaceAll("CMD_START", "!start");
         messageToSend = messageToSend.replaceAll("MINPLAYERS", this.minPlayers + "");
         if (player != null) {
            messageToSend = messageToSend.replaceAll("PLAYER", player);
            messageToSend = messageToSend.replaceAll("LEADER", player);
         }

         if (card != null) {
            messageToSend = messageToSend.replaceAll("CARD", card.toEmoticonHotkey());
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

   private String stringifyPlayerList(Map<String, LowCard.Hand> players) {
      StringBuilder playerList = new StringBuilder();
      Iterator iterator = players.keySet().iterator();

      while(iterator.hasNext()) {
         playerList.append(iterator.next()).append(", ");
      }

      String playerListString = playerList.toString();
      return playerListString.endsWith(", ") ? playerListString.substring(0, playerListString.length() - 2) : playerListString;
   }

   private synchronized void draw(String username, String cardToDraw, boolean auto) {
      Map<String, LowCard.Hand> currentRoundHands = this.isTiebreaker ? this.tiebreakerHands : this.playerHands;
      LowCard.Hand hand = (LowCard.Hand)currentRoundHands.get(username);
      if (hand != null) {
         if (hand.card == null) {
            Card card = this.deck.dealCard(cardToDraw);
            hand.setCard(card);
            this.sendChannelMessage(this.createMessage(auto ? "AUTO_DRAW" : "PLAYER_DRAWS", username, card));
            if (!auto) {
               Iterator i$ = currentRoundHands.values().iterator();

               while(i$.hasNext()) {
                  LowCard.Hand currentHand = (LowCard.Hand)i$.next();
                  if (currentHand.getCard() == null) {
                     return;
                  }
               }

               if (log.isDebugEnabled()) {
                  log.debug("Looks like everyone has drawn. Let's tally!");
               }

               if (this.nextDrawTimerTask != null && !this.nextDrawTimerTask.isDone() && !this.nextDrawTimerTask.isCancelled()) {
                  this.nextDrawTimerTask.cancel(true);
               }

               this.tallyDraws();
            }
         } else if (!auto) {
            this.sendMessage(this.createMessage("ALREADY_DRAWN", username), username);
         } else {
            log.warn("Auto draw requested for player: " + username + ". But they already seem to have drawn a card!");
         }
      }

   }

   private synchronized void newRound() {
      if (this.playerHands.size() > 1) {
         this.isRoundStarted = true;
         log.debug("Time is " + new Date());
         ++this.currentRoundNumber;
         this.deck = new LowCard.Deck();
         this.resetHands(this.tiebreakerHands);
         this.resetHands(this.playerHands);
         this.lowestHandAlreadyLeft = null;
         this.sendChannelMessage(this.createMessage("PLAYERS_TURN"));
         this.executor.schedule(new LowCard.TimedPickWinnerTask(this, this.currentRoundNumber), this.timeToDraw, TimeUnit.SECONDS);
      }

   }

   private void tallyDraws() {
      Map<String, LowCard.Hand> currentHands = this.isTiebreaker ? this.tiebreakerHands : this.playerHands;
      List<LowCard.Hand> lowestHands = new ArrayList();
      Iterator i$;
      LowCard.Hand hand;
      if (currentHands.size() > 1) {
         i$ = currentHands.values().iterator();

         label72:
         while(true) {
            do {
               if (!i$.hasNext()) {
                  break label72;
               }

               hand = (LowCard.Hand)i$.next();
               if (hand.getCard() == null) {
                  this.draw(hand.getPlayer(), "", true);
               }
            } while(this.lowestHandAlreadyLeft != null && hand.compareTo(this.lowestHandAlreadyLeft) >= 0);

            if (lowestHands.size() == 0) {
               lowestHands.add(hand);
            } else if (hand.compareTo(lowestHands.get(0)) < 0) {
               lowestHands.clear();
               lowestHands.add(hand);
            } else if (hand.compareTo(lowestHands.get(0)) == 0) {
               lowestHands.add(hand);
            }
         }
      }

      if (lowestHands.size() == 0) {
         this.tiebreakerHands.clear();
      } else if (lowestHands.size() == 1) {
         LowCard.Hand lowestHand = (LowCard.Hand)lowestHands.get(0);
         this.playerHands.remove(lowestHand.player);
         this.removePlayerFromPot(lowestHand.player);
         this.sendChannelMessage(this.createMessage(this.isTiebreaker ? "PLAYER_TIEBREAK_LOWCARD" : "PLAYER_LOWCARD", lowestHand.player, lowestHand.card));
         this.tiebreakerHands.clear();
      } else {
         this.tiebreakerHands.clear();
         i$ = lowestHands.iterator();

         while(i$.hasNext()) {
            hand = (LowCard.Hand)i$.next();
            this.tiebreakerHands.put(hand.player, hand);
         }
      }

      if (this.playerHands.size() < this.minPlayers) {
         this.pickWinner();
      } else {
         this.isRoundStarted = false;
         this.isTiebreaker = this.tiebreakerHands.size() > 0;
         if (this.isTiebreaker) {
            this.sendChannelMessage(this.createMessage("TIED_PLAYERS_LEFT") + "(" + this.tiebreakerHands.size() + "): " + this.stringifyPlayerList(this.tiebreakerHands));
            this.sendChannelMessage(this.createMessage("TIEBREAKER_ROUND"));
         } else {
            this.sendChannelMessage(this.createMessage("ALL_PLAYERS_LEFT") + "(" + this.playerHands.size() + "): " + this.stringifyPlayerList(this.playerHands));
            this.sendChannelMessage(this.createMessage("NEXT_ROUND"));
         }

         this.nextDrawTimerTask = this.executor.schedule(new LowCard.TimedNewRoundTask(this, this.currentRoundNumber), this.timeToNewRound, TimeUnit.SECONDS);
      }

   }

   private void resetHands(Map<String, LowCard.Hand> currentRoundHands) {
      Iterator iterator = currentRoundHands.keySet().iterator();

      while(iterator.hasNext()) {
         String player = (String)iterator.next();
         LowCard.Hand hand = (LowCard.Hand)currentRoundHands.get(player);
         hand.setCard((Card)null);
      }

   }

   private void pickWinner() {
      if (log.isDebugEnabled()) {
         log.debug("botInstanceID[" + this.getInstanceID() + "]: " + "Picking winner: ");
      }

      Iterator iterator = this.playerHands.keySet().iterator();
      String winner = null;
      if (iterator.hasNext()) {
         winner = (String)iterator.next();
      }

      this.endGame(winner);
   }

   class Deck {
      private List<Card> deck = Card.newShuffledDeck();

      public Deck() {
      }

      public Card dealCard() {
         return this.dealCard("");
      }

      public Card dealCard(String cardToDeal) {
         if (this.deck.isEmpty()) {
            this.deck = Card.newShuffledDeck();
            LowCard.log.warn("Should not be happening! Deck ran out in the middle of a round. Resetting deck...");
         }

         cardToDeal = cardToDeal.trim();
         if (cardToDeal.length() == 2) {
            Card.Rank rank = Card.Rank.fromChar(cardToDeal.charAt(0));
            Card.Suit suit = Card.Suit.fromChar(cardToDeal.toUpperCase().charAt(1));
            if (rank != null && suit != null) {
               Iterator i = this.deck.iterator();

               while(i.hasNext()) {
                  Card card = (Card)i.next();
                  if (card.rank() == rank && card.suit() == suit) {
                     i.remove();
                     return card;
                  }
               }
            }
         }

         return (Card)this.deck.remove(0);
      }
   }

   class Hand implements Comparable {
      private String player;
      private Card card;

      public Hand(String player) {
         this.player = player;
      }

      public String getPlayer() {
         return this.player;
      }

      public Card getCard() {
         return this.card;
      }

      public void setCard(Card card) {
         this.card = card;
      }

      public int compareTo(Object obj) {
         LowCard.Hand compareHand = (LowCard.Hand)obj;
         return this.getCard().compareTo(compareHand.getCard());
      }

      public boolean equals(Object obj) {
         if (obj != null && obj instanceof LowCard.Hand) {
            LowCard.Hand compareHand = (LowCard.Hand)obj;
            return this.player.equals(compareHand.player) && this.card.equals(compareHand.card);
         } else {
            return false;
         }
      }
   }

   class TimedPickWinnerTask implements Runnable {
      LowCard bot;
      int roundNumber;

      TimedPickWinnerTask(LowCard bot, int roundNumber) {
         this.bot = bot;
         this.roundNumber = roundNumber;
      }

      public void run() {
         synchronized(this.bot) {
            if (this.bot.getGameState() == BotData.BotStateEnum.PLAYING && this.bot.currentRoundNumber == this.roundNumber) {
               this.bot.sendChannelMessage(this.bot.createMessage("TIME_UP"));
               this.bot.tallyDraws();
            }

         }
      }
   }

   class TimedNewRoundTask implements Runnable {
      LowCard bot;
      int roundNumber;

      TimedNewRoundTask(LowCard bot, int roundNumber) {
         this.bot = bot;
         this.roundNumber = roundNumber;
      }

      public void run() {
         synchronized(this.bot) {
            if (LowCard.log.isDebugEnabled()) {
               LowCard.log.debug("TimedNewRoundTask: currentRoundNumber = " + this.bot.currentRoundNumber + ", task roundNumber = " + this.roundNumber);
            }

            if (this.bot.getGameState() == BotData.BotStateEnum.PLAYING && this.bot.currentRoundNumber == this.roundNumber) {
               LowCard.this.newRound();
            }

         }
      }
   }

   class StartPlay implements Runnable {
      LowCard bot;

      StartPlay(LowCard bot) {
         this.bot = bot;
      }

      public void run() {
         synchronized(this.bot) {
            try {
               if (LowCard.log.isDebugEnabled()) {
                  LowCard.log.debug("LowCardBot: starting play in StartPlay()");
               }

               BotData.BotStateEnum gameState = LowCard.this.getGameState();
               if (gameState == BotData.BotStateEnum.GAME_JOINING) {
                  LowCard.this.setGameState(BotData.BotStateEnum.GAME_JOIN_ENDED);
                  if (LowCard.this.getPlayers().size() < LowCard.this.minPlayers) {
                     LowCard.this.sendChannelMessage(LowCard.this.createMessage("JOIN_NO_MIN"));
                     if (LowCard.log.isDebugEnabled()) {
                        LowCard.log.debug("botInstanceID[" + LowCard.this.getInstanceID() + "]: Join ended. Not enough players.");
                     }

                     LowCard.this.resetGame(true);
                  } else {
                     Set<String> copyOfPlayers = new HashSet();
                     copyOfPlayers.addAll(LowCard.this.playerHands.keySet());
                     if (gameState != BotData.BotStateEnum.NO_GAME) {
                        gameState = BotData.BotStateEnum.PLAYING;

                        try {
                           if (LowCard.this.amountJoinPot > 0.0D) {
                              this.bot.pot = new Pot(this.bot);
                              LowCard.log.debug("Pot id[" + LowCard.this.pot.getPotID() + "] created for bot instanceID[" + LowCard.this.getInstanceID() + "]");
                              Iterator i$ = copyOfPlayers.iterator();

                              while(i$.hasNext()) {
                                 String player = (String)i$.next();

                                 try {
                                    LowCard.this.pot.enterPlayer(player, LowCard.this.amountJoinPot / 100.0D, "USD");
                                    if (LowCard.log.isDebugEnabled()) {
                                       LowCard.log.debug("botInstanceID[" + LowCard.this.getInstanceID() + "]: Entered into pot " + player + " = " + "USD" + " " + LowCard.this.amountJoinPot / 100.0D);
                                    }
                                 } catch (Exception var8) {
                                    LowCard.this.playerHands.remove(player);
                                    LowCard.log.warn("botInstanceID[" + LowCard.this.getInstanceID() + "]: Error charging player[" + player + "]", var8);
                                    LowCard.this.sendMessage(LowCard.this.createMessage("INSUFFICIENT_FUNDS_POT", player), player);
                                 }
                              }

                              if (LowCard.this.playerHands.size() < LowCard.this.minPlayers) {
                                 if (LowCard.log.isDebugEnabled()) {
                                    LowCard.log.debug("botInstanceID[" + LowCard.this.getInstanceID() + "]: Not enough valid players.");
                                 }

                                 this.cancelPot();
                                 LowCard.this.resetGame(true);
                                 return;
                              }
                           }

                           LowCard.this.incrementGamesPlayed(Leaderboard.Type.LOW_CARD_GAMES_PLAYED, LowCard.this.playerHands.keySet());
                           LowCard.this.logGamesPlayed(LowCard.this.playerHands.size(), LowCard.this.playerHands.keySet(), LowCard.this.amountJoinPot);
                           LowCard.this.sendChannelMessage(LowCard.this.createMessage("GAME_STARTED_NOTE"));
                           LowCard.this.setGameState(BotData.BotStateEnum.PLAYING);
                           LowCard.this.newRound();
                        } catch (Exception var9) {
                           LowCard.log.error("Error creating pot for botInstanceID[" + LowCard.this.getInstanceID() + "].", var9);
                           LowCard.this.setGameState(BotData.BotStateEnum.NO_GAME);
                           LowCard.this.sendChannelMessage(LowCard.this.createMessage("GAME_CANCELED"));
                        }

                     } else {
                        this.cancelPot();
                        LowCard.this.resetGame(true);
                        if (LowCard.log.isDebugEnabled()) {
                           LowCard.log.debug("botInstanceID[" + LowCard.this.getInstanceID() + "]: Billing error. Game canceled. No charges.");
                        }

                     }
                  }
               }
            } catch (Exception var10) {
               LowCard.log.error("Unexpected exception caught in startPlay.run()", var10);
               this.cancelPot();
               LowCard.this.resetGame(true);
            }
         }
      }

      private void cancelPot() {
         try {
            if (LowCard.this.pot != null) {
               LowCard.this.pot.cancel();
            }
         } catch (Exception var2) {
            LowCard.log.error("Error canceling pot for botInstanceID[" + LowCard.this.getInstanceID() + "].", var2);
         }

         LowCard.this.sendChannelMessage(LowCard.this.createMessage("GAME_CANCELED"));
      }
   }

   class StartGame implements Runnable {
      LowCard bot;

      StartGame(LowCard bot) {
         this.bot = bot;
      }

      public void run() {
         synchronized(this.bot) {
            if (LowCard.log.isDebugEnabled()) {
               LowCard.log.debug("botInstanceID[" + LowCard.this.getInstanceID() + "]: in StartGame() ");
            }

            BotData.BotStateEnum gameState = null;
            gameState = LowCard.this.getGameState();
            if (gameState == BotData.BotStateEnum.GAME_STARTING) {
               LowCard.this.setGameState(BotData.BotStateEnum.GAME_STARTED);
               LowCard.this.addPlayer(LowCard.this.gameStarter);
               if (LowCard.this.timeToJoinGame > 0L) {
                  LowCard.this.setGameState(BotData.BotStateEnum.GAME_JOINING);
                  LowCard.this.sendChannelMessage(LowCard.this.amountJoinPot > 0.0D ? LowCard.this.createMessage("GAME_JOIN_PAID") : LowCard.this.createMessage("GAME_JOIN_FREE"));
                  if (LowCard.log.isDebugEnabled()) {
                     LowCard.log.debug("LowCardBot: starting timer for StartPlay()");
                  }

                  LowCard.this.executor.schedule(LowCard.this.new StartPlay(this.bot), LowCard.this.timeToJoinGame, TimeUnit.SECONDS);
                  if (LowCard.log.isDebugEnabled()) {
                     LowCard.log.debug("botInstanceID[" + LowCard.this.getInstanceID() + "]: scheduled to start play. Awaiting join.. ");
                  }
               }
            }

         }
      }
   }
}
