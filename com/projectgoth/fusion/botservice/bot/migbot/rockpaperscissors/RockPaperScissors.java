package com.projectgoth.fusion.botservice.bot.migbot.rockpaperscissors;

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
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class RockPaperScissors extends Bot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(RockPaperScissors.class));
   private int minPlayers = 2;
   private int maxPlayers = 5;
   private long waitForPlayerInterval = 30000L;
   private long countDownInterval = 10000L;
   private long idleInterval = 1800000L;
   private double minCostToJoinGame = 0.05D;
   private Map<String, Hand> playerHands = new HashMap();
   private BotData.BotStateEnum state;
   private int round;
   private double costToJoinGame;
   private long timeLastGameFinished;

   public RockPaperScissors(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDAO) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDAO);
      this.state = BotData.BotStateEnum.NO_GAME;
      this.timeLastGameFinished = System.currentTimeMillis();
      this.minPlayers = this.getIntParameter("MinPlayers", this.minPlayers);
      this.maxPlayers = this.getIntParameter("MaxPlayers", this.maxPlayers);
      this.waitForPlayerInterval = this.getLongParameter("WaitForPlayerInterval", this.waitForPlayerInterval);
      this.countDownInterval = this.getLongParameter("CountDownInterval", this.countDownInterval);
      this.idleInterval = this.getLongParameter("IdleInterval", this.idleInterval);
      this.minCostToJoinGame = this.getDoubleParameter("MinCostToJoinGame", this.minCostToJoinGame);
      this.sendChannelMessage("Bot RockPaperScissors added to the room. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
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
         this.sendMessage("Play Rock, Paper, Scissors. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Play Rock, Paper, Scissors. Enter !j to join the game. " + this.costToJoinGame + " " + "USD", username);
         break;
      case PLAYING:
         this.sendMessage("Rock, Paper, Scissors is on now. Get ready for next game", username);
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
      } else if ("!r".equals(messageText)) {
         this.pickHand(username, Hand.ROCK);
      } else if ("!p".equals(messageText)) {
         this.pickHand(username, Hand.PAPER);
      } else if ("!s".equals(messageText)) {
         this.pickHand(username, Hand.SCISSORS);
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
         this.playerHands.clear();
         this.playerHands.put(username, Hand.CLOSED);
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
         if (this.playerHands.containsKey(username)) {
            this.sendMessage("You have already joined the game. Please wait for the game to start", username);
         }

         if (this.playerHands.size() >= this.maxPlayers) {
            this.sendMessage("Game is currently full. Please wait for next game", username);
         } else if (this.userCanAffordToEnterPot(username, this.costToJoinGame, true) && this.playerHands.put(username, Hand.CLOSED) == null) {
            log.info(username + " joined the game");
            this.sendChannelMessage(username + " joined the game");
         }
         break;
      case PLAYING:
         this.sendMessage("A game is currently in progress. Please wait for next game", username);
      }

   }

   private synchronized void pickHand(String username, Hand hand) {
      switch(this.state) {
      case NO_GAME:
         this.sendMessage("Enter !start to start a game", username);
         break;
      case GAME_JOINING:
         this.sendMessage("Game starting soon! Please wait", username);
         break;
      case PLAYING:
         Hand existingHand = (Hand)this.playerHands.get(username);
         if (existingHand == null) {
            this.sendMessage("A game is currently in progress. Please wait for next game", username);
         } else if (existingHand == Hand.CLOSED) {
            this.playerHands.put(username, hand);
            this.sendMessage("You have picked " + hand.getEmoticonKey(), username);
         } else {
            this.sendMessage("You have already picked " + existingHand.getEmoticonKey(), username);
         }
      }

   }

   private void waitForMorePlayers() {
      this.sendChannelMessage("Waiting for more players. Enter !j to join the game. " + this.costToJoinGame + " " + "USD");
      this.state = BotData.BotStateEnum.GAME_JOINING;
      this.executor.schedule(new Runnable() {
         public void run() {
            RockPaperScissors.this.chargeAndCountPlayers();
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
            this.countDown();
         }
      } catch (Exception var5) {
         log.error("Unexpected exception occured in chargeAndCountPlayers()", var5);
         this.endGame(true);
         this.sendChannelMessage("Unable to start the game. " + ExceptionHelper.getRawRootMessage(var5));
      }

   }

   private synchronized void countDown() {
      this.sendChannelMessage("Round " + this.round++ + ". Counting down... Enter !r " + Hand.ROCK.getEmoticonKey() + " or !p " + Hand.PAPER.getEmoticonKey() + " or !s " + Hand.SCISSORS.getEmoticonKey() + ". " + this.countDownInterval / 1000L + " seconds");
      this.state = BotData.BotStateEnum.PLAYING;
      this.executor.schedule(new Runnable() {
         public void run() {
            RockPaperScissors.this.pickWinner();
         }
      }, this.countDownInterval, TimeUnit.MILLISECONDS);
   }

   private void pickWinner() {
      List<String> rocks = new LinkedList();
      List<String> papers = new LinkedList();
      List<String> scissorss = new LinkedList();

      String player;
      Hand hand;
      for(Iterator i$ = this.playerHands.keySet().iterator(); i$.hasNext(); this.playerHands.put(player, Hand.CLOSED)) {
         player = (String)i$.next();
         hand = (Hand)this.playerHands.get(player);
         if (hand == Hand.CLOSED) {
            hand = Hand.random();
            this.sendMessage("You did not pick a hand. Bot picked " + hand.getEmoticonKey() + " for you", player);
         }

         switch(hand) {
         case ROCK:
            rocks.add(player);
            break;
         case PAPER:
            papers.add(player);
            break;
         case SCISSORS:
            scissorss.add(player);
         }
      }

      Map<Hand, List<String>> choiceMap = new HashMap();
      choiceMap.put(Hand.ROCK, rocks);
      choiceMap.put(Hand.PAPER, papers);
      choiceMap.put(Hand.SCISSORS, scissorss);
      Iterator i$ = choiceMap.keySet().iterator();

      while(i$.hasNext()) {
         hand = (Hand)i$.next();
         List<String> l = (List)choiceMap.get(hand);
         if (!l.isEmpty()) {
            String round_status = (l.size() == 1 ? (String)l.get(0) : StringUtil.join((Collection)l.subList(0, l.size() - 1), ",")) + (l.size() > 1 ? " and " + (String)l.get(l.size() - 1) : "");
            round_status = round_status + " picked " + hand.getEmoticonKey() + ".";
            this.sendChannelMessage(round_status);
         }
      }

      Object losers;
      if (rocks.size() > 0 && scissorss.size() > 0 && papers.size() == 0) {
         this.sendChannelMessage(Hand.ROCK.getEmoticonKey() + " wins");
         losers = scissorss;
      } else if (scissorss.size() > 0 && papers.size() > 0 && rocks.size() == 0) {
         this.sendChannelMessage(Hand.SCISSORS.getEmoticonKey() + " wins");
         losers = papers;
      } else if (papers.size() > 0 && rocks.size() > 0 && scissorss.size() == 0) {
         this.sendChannelMessage(Hand.PAPER.getEmoticonKey() + " wins");
         losers = rocks;
      } else {
         this.sendChannelMessage("Draw");
         losers = Collections.EMPTY_LIST;
      }

      Iterator i$ = ((List)losers).iterator();

      while(i$.hasNext()) {
         String loser = (String)i$.next();
         this.playerHands.remove(loser);
         if (this.pot != null) {
            try {
               this.pot.removePlayer(loser);
            } catch (Exception var9) {
               log.error("Unable to remove " + loser + " from pot " + this.pot.getPotID(), var9);
            }
         }
      }

      if (this.playerHands.size() == 0) {
         this.endGame(false);
         this.sendChannelMessage("No more players left in the game. Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
      } else if (this.playerHands.size() == 1) {
         double payout = this.endGame(false);
         if (payout < 0.0D) {
            this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
         } else {
            this.sendChannelMessageAndPopUp((String)this.playerHands.keySet().iterator().next() + " won " + (new DecimalFormat("0.00")).format(payout) + " " + "USD" + "!");
         }

         this.sendChannelMessage("Enter !start to start a game. " + this.minCostToJoinGame + " " + "USD" + ". For custom entry, !start <entry_amount>");
      } else {
         this.sendChannelMessage(this.playerHands.size() + " players left in the game [" + StringUtil.join((Collection)this.playerHands.keySet(), ", ") + "]");
         this.countDown();
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
}
