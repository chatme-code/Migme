package com.projectgoth.fusion.botservice.bot.migbot.football;

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

public class Football extends Bot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Football.class));
   private int minPlayers = 2;
   private int maxPlayers = 8;
   private long waitForPlayerInterval = 30000L;
   private long countDownInterval = 10000L;
   private long nextRoundInterval = 5000L;
   private long idleInterval = 1800000L;
   private double minCostToJoinGame = 0.05D;
   private Map<String, Direction> playerKicks = new HashMap();
   private BotData.BotStateEnum state;
   private int round;
   private double costToJoinGame;
   private long timeLastGameFinished;

   public Football(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDAO) {
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
      this.sendChannelMessage("Bot Football added to the room. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
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
         this.sendMessage("Play Football. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Play Football. Enter !j to join the game. " + this.costToJoinGame + " " + "USD", username);
         break;
      case PLAYING:
         this.sendMessage("Football is on now. Get ready for next game", username);
      }

   }

   public void onUserLeaveChannel(String username) {
      synchronized(this) {
         if (this.playerKicks.remove(username) != null && this.state != BotData.BotStateEnum.NO_GAME) {
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
      } else if ("!r".equals(messageText)) {
         this.kickTheBall(username, Direction.RIGHT);
      } else if ("!c".equals(messageText)) {
         this.kickTheBall(username, Direction.CENTRE);
      } else if ("!l".equals(messageText)) {
         this.kickTheBall(username, Direction.LEFT);
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
         this.playerKicks.clear();
         this.playerKicks.put(username, Direction.UNKNOWN);
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
         if (this.playerKicks.containsKey(username)) {
            this.sendMessage("You have already joined the game. Please wait for the game to start", username);
         }

         if (this.playerKicks.size() >= this.maxPlayers) {
            this.sendMessage("Game is currently full. Please wait for next game", username);
         } else if (this.userCanAffordToEnterPot(username, this.costToJoinGame, true) && this.playerKicks.put(username, Direction.UNKNOWN) == null) {
            log.info(username + " joined the game");
            this.sendChannelMessage(username + " joined the game");
         }
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   private synchronized void kickTheBall(String username, Direction direction) {
      switch(this.state) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Game starting soon! Please wait", username);
         break;
      case PLAYING:
         Direction existingDirection = (Direction)this.playerKicks.get(username);
         if (existingDirection == null) {
            this.sendMessage("A game is currently in progress. Please wait for next game", username);
         } else if (existingDirection == Direction.UNKNOWN) {
            this.playerKicks.put(username, direction);
            this.sendMessage("You have kicked the ball to " + direction, username);
         } else {
            this.sendMessage("You have already kicked the ball to " + existingDirection, username);
         }
      }

   }

   private void waitForMorePlayers() {
      this.sendChannelMessage("Waiting for more players. Enter !j to join the game. " + this.costToJoinGame + " " + "USD");
      this.state = BotData.BotStateEnum.GAME_JOINING;
      this.executor.schedule(new Runnable() {
         public void run() {
            Football.this.chargeAndCountPlayers();
         }
      }, this.waitForPlayerInterval, TimeUnit.MILLISECONDS);
   }

   private synchronized void chargeAndCountPlayers() {
      try {
         if (this.costToJoinGame > 0.0D) {
            if (this.playerKicks.size() < this.minPlayers) {
               this.endGame(true);
               this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
               return;
            }

            this.pot = new Pot(this);
            Iterator i = this.playerKicks.keySet().iterator();

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

         if (this.playerKicks.size() < this.minPlayers) {
            this.endGame(true);
            this.sendChannelMessage("Not enough players joined the game. Enter !start to start a new game");
         } else {
            this.logGamesPlayed(this.playerKicks.size(), this.playerKicks.keySet(), this.costToJoinGame);
            this.countDown();
            this.incrementGamesPlayed(Leaderboard.Type.FOOTBALL_GAMES_PLAYED, this.playerKicks.keySet());
         }
      } catch (Exception var5) {
         log.error("Unexpected exception occured in chargeAndCountPlayers()", var5);
         this.endGame(true);
         this.sendChannelMessage("Unable to start the game. " + ExceptionHelper.getRawRootMessage(var5));
      }

   }

   private void countDown() {
      this.sendChannelMessage("Round " + this.round++ + ". Counting down... Enter !l or !c or !r. " + this.countDownInterval / 1000L + " seconds");
      this.state = BotData.BotStateEnum.PLAYING;
      this.executor.schedule(new Runnable() {
         public void run() {
            Football.this.pickWinner();
         }
      }, this.countDownInterval, TimeUnit.MILLISECONDS);
   }

   private synchronized void pickWinner() {
      List<String> left = new LinkedList();
      List<String> centre = new LinkedList();
      List<String> right = new LinkedList();

      String player;
      for(Iterator i$ = this.playerKicks.keySet().iterator(); i$.hasNext(); this.playerKicks.put(player, Direction.UNKNOWN)) {
         player = (String)i$.next();
         Direction direction = (Direction)this.playerKicks.get(player);
         if (direction == Direction.UNKNOWN) {
            direction = Direction.random();
            this.sendMessage("You did not kick the ball. Bot kicked to " + direction + " for you", player);
         }

         if (direction == Direction.LEFT) {
            left.add(player);
         } else if (direction == Direction.CENTRE) {
            centre.add(player);
         } else {
            right.add(player);
         }
      }

      this.sendChannelMessage(Direction.LEFT + " [" + StringUtil.join((Collection)left, ", ") + "] " + Direction.CENTRE + " [" + StringUtil.join((Collection)centre, ", ") + "] " + Direction.RIGHT + " [" + StringUtil.join((Collection)right, ", ") + "]");
      Direction botDirection = Direction.random();
      this.sendChannelMessage("Bot defended " + botDirection);
      List<String> losers = new LinkedList();
      if (botDirection == Direction.LEFT) {
         losers.addAll(left);
      } else if (botDirection == Direction.CENTRE) {
         losers.addAll(centre);
      } else {
         losers.addAll(right);
      }

      if (losers.size() != this.playerKicks.size()) {
         Iterator i$ = losers.iterator();

         while(i$.hasNext()) {
            String loser = (String)i$.next();
            this.playerKicks.remove(loser);
            if (this.pot != null) {
               try {
                  this.pot.removePlayer(loser);
               } catch (Exception var9) {
                  log.error("Unable to remove " + loser + " from pot " + this.pot.getPotID(), var9);
               }
            }
         }
      }

      if (this.playerKicks.size() == 0) {
         this.endGame(false);
         this.sendChannelMessage("No more players left in the game. Enter !start to start a new game");
      } else if (this.playerKicks.size() == 1) {
         double payout = this.endGame(false);
         if (payout < 0.0D) {
            this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
         } else {
            String winner = (String)this.playerKicks.keySet().iterator().next();
            this.sendChannelMessageAndPopUp(winner + " won " + (new DecimalFormat("0.00")).format(payout) + " " + "USD");
            this.logMostWins(winner, payout);
            this.incrementMostWins(Leaderboard.Type.FOOTBALL_MOST_WINS, winner);
         }

         this.sendChannelMessage("Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
      } else {
         this.sendChannelMessage(this.playerKicks.size() + " players left in the game [" + StringUtil.join((Collection)this.playerKicks.keySet(), ", ") + "]. Next round starts in " + this.nextRoundInterval / 1000L + " seconds");
         this.executor.schedule(new Runnable() {
            public void run() {
               Football.this.countDown();
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
            this.revertLimitInCache(this.playerKicks.keySet());
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
         Football bot = new Football(executor, (BotChannelPrx)null, (BotData)null, "EN", "koko", (BotDAO)null);
         bot.waitForPlayerInterval = 2000L;
         bot.countDownInterval = 2000L;
         bot.minCostToJoinGame = 0.0D;
         bot.onMessage("koko", "!start", System.currentTimeMillis());
         bot.onMessage("kien", "!j", System.currentTimeMillis());
         bot.onMessage("dave", "!j", System.currentTimeMillis());
         bot.onMessage("phong", "!j", System.currentTimeMillis());
         bot.onMessage("lakshmi", "!j", System.currentTimeMillis());
         Thread.sleep(60000L);
         executor.shutdown();
         executor.awaitTermination(60L, TimeUnit.SECONDS);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }
}
