package com.projectgoth.fusion.botservice.bot.migbot.russianroulette;

import com.projectgoth.fusion.data.BotData;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TestRussianRoulette {
   public static ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);
   private BotData.BotStateEnum gameState;
   private List<String> players;
   private List<String> playersRemaining;
   private ScheduledFuture nextSpinTimerTask;
   private String gameStarter;
   private String currentPlayer;

   public TestRussianRoulette() {
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.players = new ArrayList();
      this.playersRemaining = new ArrayList();
   }

   public synchronized BotData.BotStateEnum getGameState() {
      return this.gameState;
   }

   public synchronized void setGameState(BotData.BotStateEnum gameState) {
      this.gameState = gameState;
   }

   private void resetGame() {
      this.nextSpinTimerTask = null;
      this.currentPlayer = null;
      this.gameStarter = null;
      this.players.clear();
      this.playersRemaining.clear();
      this.setGameState(BotData.BotStateEnum.NO_GAME);
   }

   public synchronized void onMessage(String username, String messageText) {
      if (messageText.equalsIgnoreCase("!no")) {
         this.processNoMessage(username);
      } else if (messageText.toLowerCase().startsWith("!start")) {
         try {
            this.startGame(username);
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      } else if (messageText.equalsIgnoreCase("!j")) {
         if (!this.players.contains(username)) {
            if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
               this.addPlayer(username);
            } else {
               System.out.println("Cannot join at this time. Game is in " + this.gameState + " state");
            }
         } else {
            System.out.println(username + " already in the game");
         }
      } else if (messageText.equalsIgnoreCase("!s") && this.gameState == BotData.BotStateEnum.PLAYING) {
         if (!username.equals(this.currentPlayer)) {
            System.out.println("Not your turn to spin " + username);
         } else {
            System.out.println(username + " spins");
            if (this.nextSpinTimerTask != null && !this.nextSpinTimerTask.isCancelled()) {
               this.nextSpinTimerTask.cancel(false);
            }

            this.spin(username);
         }
      }

   }

   public synchronized void onUserLeaveChannel(String username) {
      synchronized(this.playersRemaining) {
         if (this.playersRemaining.contains(username)) {
            this.playersRemaining.remove(username);
         }
      }

      if (this.players != null) {
         synchronized(this.players) {
            if (this.players.contains(username)) {
               this.players.remove(username);
               System.out.println(username + " [LEFT] the game channel\n");
            }
         }

         if (this.getGameState() == BotData.BotStateEnum.PLAYING) {
            if (this.players.size() >= 2 && username.equals(this.currentPlayer)) {
               if (this.nextSpinTimerTask != null && !this.nextSpinTimerTask.isDone() && !this.nextSpinTimerTask.isCancelled()) {
                  this.nextSpinTimerTask.cancel(true);
               }

               this.nextPlayer();
            } else if (this.players.size() < 2) {
               this.endGame();
            }
         }
      }

   }

   public void processNoMessage(String username) {
      switch(this.getGameState().value()) {
      case 1:
         if (username.equals(this.gameStarter)) {
            this.setGameState(BotData.BotStateEnum.NO_GAME);
            this.gameStarter = null;
         }
      default:
      }
   }

   public void addPlayer(String username) {
      if (this.getGameState() == BotData.BotStateEnum.GAME_STARTED || this.getGameState() == BotData.BotStateEnum.GAME_JOINING) {
         synchronized(this.players) {
            if (!this.players.contains(username)) {
               this.players.add(username);
            }
         }

         System.out.println(username + " added to the game");
      }

   }

   public void startGame(String username) throws Exception {
      if (this.gameState.equals(BotData.BotStateEnum.NO_GAME)) {
         this.setGameState(BotData.BotStateEnum.GAME_STARTING);
         this.gameStarter = username;
         System.out.println("RussianRouletteBot: starting timer for StartGame()");
         executor.schedule(new TestRussianRoulette.StartGame(), 0L, TimeUnit.SECONDS);
         System.out.println("RussianRouletteBot: started timer for StartGame()");
      } else {
         System.out.println("Cannot start game. Game is in " + this.gameState + " state");
      }

   }

   public void spin(String player) {
      if (this.players.size() > 1) {
         Random ChamberValue = new Random(System.currentTimeMillis());
         int intChamber = ChamberValue.nextInt(6);
         if (intChamber == 5) {
            System.out.println(player + " got a [BANG]\n");
            synchronized(this.players) {
               this.players.remove(player);
            }
         } else {
            System.out.println(player + " got a [CLICK]\n");
         }

         if (this.players.size() > 1) {
            this.nextPlayer();
         } else if (this.players.size() == 1) {
            this.endGame();
         }
      }

   }

   private void nextPlayer() {
      if (this.players.size() > 1) {
         if (this.playersRemaining.isEmpty()) {
            this.playersRemaining.addAll(this.players);
         }

         this.currentPlayer = (String)this.playersRemaining.remove(0);
         if (this.currentPlayer == null) {
            System.err.println("OH DAMNNNN!!!");
            System.exit(-1);
         }

         this.nextSpinTimerTask = executor.schedule(new TestRussianRoulette.TimedAutoSpinTask(this, this.currentPlayer), 100L, TimeUnit.MILLISECONDS);
         System.out.println("Next spin scheduled for " + this.currentPlayer);
      }

   }

   public void endGame() {
      try {
         if (this.getGameState() == BotData.BotStateEnum.PLAYING) {
            if (this.nextSpinTimerTask != null && !this.nextSpinTimerTask.isDone() && !this.nextSpinTimerTask.isCancelled()) {
               System.out.println("Pending timer task to cancel in endGame()");
               this.nextSpinTimerTask.cancel(true);
            }

            if (!this.players.isEmpty()) {
               String winner = (String)this.players.get(0);
               System.out.println("Winner is " + winner);
            } else {
               System.out.println("endGame(): There are no players remaining. players is null or empty.");
            }

            return;
         }
      } catch (Exception var6) {
         var6.printStackTrace();
         return;
      } finally {
         this.resetGame();
      }

   }

   public static void main(String[] args) {
      try {
         SecureRandom random = new SecureRandom();
         System.out.println("Hello");
         TestRussianRoulette t = new TestRussianRoulette();
         t.onMessage("koko", "!start");
         Thread.sleep(1000L);
         int numPlayers = 100;

         int i;
         for(i = 0; i < numPlayers; ++i) {
            t.onMessage("dave" + i, "!j");
         }

         for(i = 0; i < 10000; ++i) {
            t.onMessage(t.currentPlayer, "!s");
            if (random.nextDouble() > 0.5D) {
               t.onUserLeaveChannel("dave" + random.nextInt(numPlayers));
            }

            Thread.sleep(100L);
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   private class StartPlay implements Runnable {
      private StartPlay() {
      }

      public void run() {
         System.out.println("RussianRouletteBot: starting play in StartPlay()");
         BotData.BotStateEnum gameState = TestRussianRoulette.this.getGameState();
         if (gameState == BotData.BotStateEnum.GAME_JOINING) {
            TestRussianRoulette.this.setGameState(BotData.BotStateEnum.GAME_JOIN_ENDED);
            if (TestRussianRoulette.this.players.size() < 2) {
               System.out.println("Join ended. Not enough players.");
               TestRussianRoulette.this.setGameState(BotData.BotStateEnum.NO_GAME);
            } else if (gameState != BotData.BotStateEnum.NO_GAME) {
               gameState = BotData.BotStateEnum.PLAYING;

               try {
                  System.out.println("Starting game!");
                  TestRussianRoulette.this.setGameState(BotData.BotStateEnum.PLAYING);
                  TestRussianRoulette.this.nextPlayer();
               } catch (Exception var3) {
                  var3.printStackTrace();
                  TestRussianRoulette.this.setGameState(BotData.BotStateEnum.NO_GAME);
               }
            } else {
               TestRussianRoulette.this.resetGame();
               System.out.println("Billing error. Game canceled. No charges.");
            }
         }

      }

      // $FF: synthetic method
      StartPlay(Object x1) {
         this();
      }
   }

   private class StartGame implements Runnable {
      private StartGame() {
      }

      public void run() {
         System.out.println("In StartGame() ");
         BotData.BotStateEnum gameState = null;
         gameState = TestRussianRoulette.this.getGameState();
         if (gameState == BotData.BotStateEnum.GAME_STARTING) {
            TestRussianRoulette.this.setGameState(BotData.BotStateEnum.GAME_STARTED);
            TestRussianRoulette.this.addPlayer(TestRussianRoulette.this.gameStarter);
            TestRussianRoulette.this.setGameState(BotData.BotStateEnum.GAME_JOINING);
            TestRussianRoulette.executor.schedule(TestRussianRoulette.this.new StartPlay(), 2L, TimeUnit.SECONDS);
            System.out.println("Scheduled to start play. Awaiting join.. ");
         }

      }

      // $FF: synthetic method
      StartGame(Object x1) {
         this();
      }
   }

   private class TimedAutoSpinTask implements Runnable {
      private TestRussianRoulette bot;
      private String player;

      public TimedAutoSpinTask(TestRussianRoulette TestRussianRoulette, String player) {
         this.player = player;
      }

      public void run() {
         synchronized(this.bot) {
            if (TestRussianRoulette.this.getGameState() == BotData.BotStateEnum.PLAYING && this.player.equals(TestRussianRoulette.this.currentPlayer)) {
               System.out.println("Auto spin for " + this.player);
               TestRussianRoulette.this.spin(TestRussianRoulette.this.currentPlayer);
            }

         }
      }
   }
}
