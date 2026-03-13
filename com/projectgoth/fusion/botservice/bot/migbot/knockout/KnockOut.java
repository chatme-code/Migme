package com.projectgoth.fusion.botservice.bot.migbot.knockout;

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
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class KnockOut extends Bot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(KnockOut.class));
   private int minPlayers = 2;
   private int maxPlayers = 20;
   private long waitForPlayerInterval = 30000L;
   private long countDownInterval = 10000L;
   private long nextRoundInterval = 5000L;
   private long idleInterval = 1800000L;
   private double minCostToJoinGame = 0.05D;
   private Map<String, KnockOut.Action> playerActions = new HashMap();
   private BotData.BotStateEnum state;
   private int round;
   private double costToJoinGame;
   private long timeLastGameFinished;

   public KnockOut(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDAO) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDAO);
      this.state = BotData.BotStateEnum.NO_GAME;
      this.timeLastGameFinished = System.currentTimeMillis();
      this.minPlayers = this.getIntParameter("MinPlayers", this.minPlayers);
      this.maxPlayers = this.getIntParameter("MaxPlayers", this.maxPlayers);
      this.waitForPlayerInterval = this.getLongParameter("WaitForPlayerInterval", this.waitForPlayerInterval);
      this.countDownInterval = this.getLongParameter("CountDownInterval", this.countDownInterval);
      this.nextRoundInterval = this.getLongParameter("NextRoundInterval", this.nextRoundInterval);
      this.idleInterval = this.getLongParameter("IdleInterval", this.idleInterval);
      this.minCostToJoinGame = this.getDoubleParameter("MinCostToJoinGame", this.minCostToJoinGame);
      this.sendChannelMessage("Bot KnockOut added to the room. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
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
         this.sendMessage("Play KnockOut. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Play KnockOut. Enter !j to join the game. " + this.costToJoinGame + " " + "USD", username);
         break;
      case PLAYING:
         this.sendMessage("KnockOut is on now. Get ready for next game", username);
      }

   }

   public void onUserLeaveChannel(String username) {
      synchronized(this) {
         if (this.playerActions.remove(username) != null && this.state != BotData.BotStateEnum.NO_GAME) {
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
      } else if ("!a".equals(messageText)) {
         this.pickAction(username, KnockOut.Action.ATTACK);
      } else if ("!d".equals(messageText)) {
         this.pickAction(username, KnockOut.Action.DEFEND);
      } else {
         this.sendMessage(messageText + " is not a valid command", username);
      }

   }

   private synchronized void startNewGame(String username, String messageText) {
      switch(this.state) {
      case NO_GAME:
         String[] params = messageText.split(" ");

         try {
            this.costToJoinGame = params.length > 1 ? (double)Integer.parseInt(params[1]) / 100.0D : this.minCostToJoinGame;
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
         this.playerActions.clear();
         this.playerActions.put(username, KnockOut.Action.UNKNOWN);
         this.pot = null;
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
         if (this.playerActions.containsKey(username)) {
            this.sendMessage("You have already joined the game. Please wait for the game to start", username);
         }

         if (this.playerActions.size() >= this.maxPlayers) {
            this.sendMessage("Game is currently full. Please wait for next game", username);
         } else if (this.userCanAffordToEnterPot(username, this.costToJoinGame, true) && this.playerActions.put(username, KnockOut.Action.UNKNOWN) == null) {
            log.info(username + " joined the game");
            this.sendChannelMessage(username + " joined the game");
         }
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   private synchronized void pickAction(String username, KnockOut.Action action) {
      switch(this.state) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Game starting soon! Please wait", username);
         break;
      case PLAYING:
         KnockOut.Action existingAction = (KnockOut.Action)this.playerActions.get(username);
         if (existingAction == null) {
            this.sendMessage("A game is currently in progress. Please wait for next game", username);
         } else if (existingAction == KnockOut.Action.UNKNOWN) {
            this.playerActions.put(username, action);
            this.sendMessage("You have choose to " + action, username);
         } else {
            this.sendMessage("You have already choose to " + existingAction, username);
         }
      }

   }

   private void waitForMorePlayers() {
      this.sendChannelMessage("Waiting for more players. Enter !j to join the game. " + this.costToJoinGame + " " + "USD");
      this.state = BotData.BotStateEnum.GAME_JOINING;
      this.executor.schedule(new Runnable() {
         public void run() {
            KnockOut.this.chargeAndCountPlayers();
         }
      }, this.waitForPlayerInterval, TimeUnit.MILLISECONDS);
   }

   private synchronized void chargeAndCountPlayers() {
      try {
         if (this.costToJoinGame > 0.0D) {
            if (this.playerActions.size() < this.minPlayers) {
               this.endGame(true);
               this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
               return;
            }

            this.pot = new Pot(this);
            Iterator i = this.playerActions.keySet().iterator();

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

         if (this.playerActions.size() < this.minPlayers) {
            this.endGame(true);
            this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
         } else {
            log.info("New game started in " + this.channel);
            this.countDown();
         }
      } catch (Exception var5) {
         log.error("Unexpected exception occured in chargeAndCountPlayers()", var5);
         this.endGame(true);
         this.sendChannelMessage("Unable to start the game. " + ExceptionHelper.getRawRootMessage(var5));
      }

   }

   private void countDown() {
      this.sendChannelMessage("Round " + this.round++ + ". Counting down... Enter !a or !d. " + this.countDownInterval / 1000L + " seconds");
      this.state = BotData.BotStateEnum.PLAYING;
      this.executor.schedule(new Runnable() {
         public void run() {
            KnockOut.this.pickWinner();
         }
      }, this.countDownInterval, TimeUnit.MILLISECONDS);
   }

   private synchronized void pickWinner() {
      SecureRandom random = new SecureRandom();
      List<String> attackers = new LinkedList();
      List<String> defenders = new LinkedList();

      String player;
      for(Iterator i$ = this.playerActions.keySet().iterator(); i$.hasNext(); this.playerActions.put(player, KnockOut.Action.UNKNOWN)) {
         player = (String)i$.next();
         KnockOut.Action action = (KnockOut.Action)this.playerActions.get(player);
         if (action == KnockOut.Action.UNKNOWN) {
            action = random.nextBoolean() ? KnockOut.Action.ATTACK : KnockOut.Action.DEFEND;
            this.sendMessage("You did not pick an action. Bot choose " + action + " for you", player);
         }

         if (action == KnockOut.Action.ATTACK) {
            attackers.add(player);
         } else {
            defenders.add(player);
         }
      }

      this.sendChannelMessage(KnockOut.Action.ATTACK + " [" + StringUtil.join((Collection)attackers, ", ") + "] " + KnockOut.Action.DEFEND + " [" + StringUtil.join((Collection)defenders, ", ") + "]");
      LinkedList losers;
      if (random.nextBoolean()) {
         this.sendChannelMessage("Bot is ATTACKING and killed all attackers");
         losers = attackers;
      } else {
         this.sendChannelMessage("Bot is DEFENDING and attackers get the glory");
         losers = defenders;
      }

      if (losers.size() != this.playerActions.size()) {
         Iterator i$ = losers.iterator();

         while(i$.hasNext()) {
            String loser = (String)i$.next();
            this.playerActions.remove(loser);
            if (this.pot != null) {
               try {
                  this.pot.removePlayer(loser);
               } catch (Exception var8) {
                  log.error("Unable to remove " + loser + " from pot " + this.pot.getPotID(), var8);
               }
            }
         }
      }

      if (this.playerActions.size() == 0) {
         this.endGame(false);
         this.sendChannelMessage("No more players left in the game. Enter !start to start a new game");
      } else if (this.playerActions.size() == 1) {
         double payout = this.endGame(false);
         if (payout < 0.0D) {
            this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
         } else {
            this.sendChannelMessageAndPopUp((String)this.playerActions.keySet().iterator().next() + " won " + (new DecimalFormat("0.00")).format(payout) + " " + "USD");
         }

         this.sendChannelMessage("Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
      } else {
         this.sendChannelMessage(this.playerActions.size() + " players left in the game [" + StringUtil.join((Collection)this.playerActions.keySet(), ", ") + "]. Next round starts in " + this.nextRoundInterval / 1000L + " seconds");
         this.executor.schedule(new Runnable() {
            public void run() {
               KnockOut.this.countDown();
            }
         }, this.nextRoundInterval, TimeUnit.MILLISECONDS);
      }

   }

   private synchronized double endGame(boolean cancelPot) {
      if (this.state == BotData.BotStateEnum.NO_GAME) {
         log.warn("endGame() called but game has already ended");
         return 0.0D;
      } else {
         double payout = 0.0D;
         if (cancelPot) {
            this.revertLimitInCache(this.playerActions.keySet());
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
         this.state = BotData.BotStateEnum.NO_GAME;
         return payout;
      }
   }

   public static void main(String[] args) {
      try {
         ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
         KnockOut bot = new KnockOut(executor, (BotChannelPrx)null, (BotData)null, "EN", "koko", (BotDAO)null);
         bot.waitForPlayerInterval = 2000L;
         bot.countDownInterval = 2000L;
         Thread.sleep(60000L);
         executor.shutdown();
         executor.awaitTermination(60L, TimeUnit.SECONDS);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }

   private static enum Action {
      UNKNOWN,
      ATTACK,
      DEFEND;
   }
}
