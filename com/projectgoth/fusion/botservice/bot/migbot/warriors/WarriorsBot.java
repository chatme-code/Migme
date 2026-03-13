package com.projectgoth.fusion.botservice.bot.migbot.warriors;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.botservice.bot.Pot;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class WarriorsBot extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(WarriorsBot.class));
   DecimalFormat df = new DecimalFormat("0.00");
   private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();
   public static final String MIN_POT_ENTRY = "minPotEntry";
   public static final String DEFAULT_POT_ENTRY = "defaultPotEntry";
   public static final String MAX_HIT_POINTS_BY_MIGLEVEL = "maxHitPointsByMigLevel";
   public static final String MAX_HIT_POINTS_BY_FRIENDS = "maxHitPointsByFriends";
   public static final String MAX_HIT_POINTS_BY_ANTE = "maxHitPointsByAnte";
   public static final String MAX_HIT_POINTS_BY_MIGLEVEL_DIFFERENCE = "maxHitPointsByMigLevelDiff";
   public static final String MAX_ANTE_FOR_HIT_POINTS = "maxAnteForHitPoints";
   public static final String MAX_HIT_POINTS_BY_MIGDNA = "maxHitPointsByMigDNA";
   public static final String MAX_HIT_POINTS_BY_FEVER = "maxHitPointsByFever";
   public static final String TIME_TO_RESPOND = "timeToRespond";
   public static final String TIME_TO_DISPLAY_HP = "timeToDisplayHP";
   public static final String TIME_TO_CANCEL = "timeToCancel";
   public static final String IDLE_INTERVAL = "idleInterval";
   public static final String WINS_BEFORE_FEVER = "winsBeforeFever";
   public static final String LOSSES_BEFORE_BERSERK = "lossesBeforeBererk";
   public static final String FEVERS_GIVEN_FOR_CONSECUTIVE_WINS = "feversGivenForConsecuiveWins";
   public static final String BERSERKS_GIVEN_FOR_CONSECUTIVE_LOSSES = "berserksGivenForConsecLosses";
   public static final String MIN_PLAYERS = "minPlayers";
   public static final String MAX_PLAYERS = "maxPlayers";
   public static final String DEFAULT_POT_START = "defaultPotStart";
   public static final String MAX_ROUND = "maxRound";
   public static final String MAX_ATTACK_PER_ROUND = "maxAttackPerRound";
   public static final String TIME_TO_DELAY_ROUND_START = "timeToDelayRoundStart";
   public static final String TIME_TO_END_ROUND = "timeToEndRound";
   public static final String TIME_TO_END_GAME_NO_PLAYER = "timeToEndGameNoPlayer";
   private double minPotEntry = 0.05D;
   private double defaultPotEntry = 0.05D;
   private double defaultPotStart = 0.05D;
   private static int maxHitPointsByMigLevel = 800;
   private static int maxHitPointsByFriends = 800;
   private static int maxHitPointsByAnte = 100;
   private static int maxHitPointsByMigLevelDiff = 500;
   private static int maxAnteForHitPoints = 100;
   private static int maxHitPointsByMigDNA = 400;
   private static int maxHitPointsByFever = 200;
   private long idleInterval = 1800000L;
   private int minPlayers = 2;
   private int maxPlayers = 5;
   private long timeToCancel = 20000L;
   private long timeToRespond = 60000L;
   private long timeToDisplayHP = 3000L;
   private int winsBeforeFever = 3;
   private int lossesBeforeBerserk = 3;
   private int feversGivenForConsecutiveWins = 3;
   private int berserksGivenForConsecLosses = 3;
   private int maxRound = 6;
   private int maxAttackPerRound = 1;
   private long timeToDelayRoundStart = 10000L;
   private long timeToEndRound = 20000L;
   private long timeToEndGameNoPlayer = 60000L;
   private BotData.BotStateEnum gameState;
   private long timeLastGameFinished;
   private long timeGameStarted;
   private long timeGameJoining;
   private static final String COMMAND_START = "!start";
   private static final String COMMAND_CANCEL = "!n";
   private static final String COMMAND_JOIN = "!j";
   private static final String COMMAND_ATTACK = "!a";
   private static final String COMMAND_HP_CHECK = "!p";
   private static final String EMOTE_WINNER_CUP = "(warriors-trophy)";
   private static final String EMOTE_LOSER = "(warriors-death)";
   private static final String EMOTE_WEAK_HIT = "(warriors-atk-1)";
   private static final String EMOTE_AVERAGE_HIT = "(warriors-atk-2)";
   private static final String EMOTE_STRONG_HIT = "(warriors-atk-3)";
   private static final String EMOTE_BERSERK_HIT = "(warriors-atk-4)";
   public static final String EMOTE_PLAYERNUM_NORMAL = "(warriors-num-n-%d)";
   public static final String EMOTE_PLAYERNUM_BERSERK = "(warriors-num-a-%d)";
   public static final String EMOTE_PLAYERNUM_FEVER = "(warriors-num-d-%d)";
   private List<Attack> normalAttacks;
   private List<Attack> berserkAttacks;
   private Player challengerPlayer;
   private Map<String, Player> playerMap;
   private Map<String, Player> deadPlayerMap;
   private int currentRound;
   private boolean isInRound;
   private Map<String, Integer> attacksInRound;
   private ScheduledFuture<?> cancellationTimer;
   private ScheduledFuture<?> challengeResponseTimer;
   private ScheduledFuture<?> hpDisplayTimer;
   private ScheduledFuture<?> roundStartDelayTimer;
   private ScheduledFuture<?> roundEndTimer;
   private ScheduledFuture<?> noPlayerTimer;

   public WarriorsBot(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.timeLastGameFinished = System.currentTimeMillis();
      this.timeGameStarted = System.currentTimeMillis();
      this.timeGameJoining = 0L;
      this.normalAttacks = new ArrayList();
      this.berserkAttacks = new ArrayList();
      this.challengerPlayer = null;
      this.playerMap = null;
      this.deadPlayerMap = null;
      this.currentRound = 0;
      this.isInRound = false;
      this.attacksInRound = new HashMap();
      this.loadGameConfig();
      log.info(botData.getDisplayName() + " [" + this.instanceID + "] added to channel [" + this.channel + "]");
      this.playerMap = Collections.synchronizedMap(new LinkedHashMap());
      this.normalAttacks.add(new Attack("Weak Hit", "Weak hit!", 12, 36, 0.1D, "(warriors-atk-1)"));
      this.normalAttacks.add(new Attack("Average Hit", "", 48, 108, 0.73D, "(warriors-atk-2)"));
      this.normalAttacks.add(new Attack("Strong Hit", "STRONG HIT!", 120, 180, 0.17D, "(warriors-atk-3)"));
      this.berserkAttacks.add(new Attack("Weak Hit", "Weak hit!", 12, 36, 0.05D, "(warriors-atk-1)"));
      this.berserkAttacks.add(new Attack("Average Hit", "", 48, 108, 0.45D, "(warriors-atk-2)"));
      this.berserkAttacks.add(new Attack("Strong Hit", "STRONG HIT!", 120, 180, 0.25D, "(warriors-atk-3)"));
      this.berserkAttacks.add(new Attack("Berserk Hit", "BERSERK HIT!", 168, 204, 0.25D, "(warriors-atk-4)"));
      this.deadPlayerMap = new HashMap();
      this.sendChannelMessage(this.createMessage("BOT_ADDED", (String)null));
      this.sendChannelMessage(this.createMessage("PLAY_NOW", (String)null));
   }

   private void loadGameConfig() {
      this.minPotEntry = this.getDoubleParameter("minPotEntry", this.minPotEntry);
      this.defaultPotEntry = this.getDoubleParameter("defaultPotEntry", this.defaultPotEntry);
      this.defaultPotStart = this.getDoubleParameter("defaultPotStart", this.defaultPotStart);
      maxHitPointsByMigLevel = this.getIntParameter("maxHitPointsByMigLevel", maxHitPointsByMigLevel);
      maxHitPointsByFriends = this.getIntParameter("maxHitPointsByFriends", maxHitPointsByFriends);
      maxHitPointsByAnte = this.getIntParameter("maxHitPointsByAnte", maxHitPointsByAnte);
      maxHitPointsByMigLevelDiff = this.getIntParameter("maxHitPointsByMigLevelDiff", maxHitPointsByMigLevelDiff);
      maxAnteForHitPoints = this.getIntParameter("maxAnteForHitPoints", maxAnteForHitPoints);
      maxHitPointsByMigDNA = this.getIntParameter("maxHitPointsByMigDNA", maxHitPointsByMigDNA);
      maxHitPointsByFever = this.getIntParameter("maxHitPointsByFever", maxHitPointsByFever);
      this.timeToCancel = this.getLongParameter("timeToCancel", this.timeToCancel);
      this.timeToRespond = this.getLongParameter("timeToRespond", this.timeToRespond);
      this.idleInterval = this.getLongParameter("idleInterval", this.idleInterval);
      this.timeToDisplayHP = this.getLongParameter("timeToDisplayHP", this.timeToDisplayHP);
      this.winsBeforeFever = this.getIntParameter("winsBeforeFever", this.winsBeforeFever);
      this.lossesBeforeBerserk = this.getIntParameter("lossesBeforeBererk", this.lossesBeforeBerserk);
      this.feversGivenForConsecutiveWins = this.getIntParameter("feversGivenForConsecuiveWins", this.feversGivenForConsecutiveWins);
      this.berserksGivenForConsecLosses = this.getIntParameter("berserksGivenForConsecLosses", this.berserksGivenForConsecLosses);
      this.minPlayers = this.getIntParameter("minPlayers", this.minPlayers);
      this.maxPlayers = this.getIntParameter("maxPlayers", this.maxPlayers);
      this.maxRound = this.getIntParameter("maxRound", this.maxRound);
      this.maxAttackPerRound = this.getIntParameter("maxAttackPerRound", this.maxAttackPerRound);
      this.timeToDelayRoundStart = this.getLongParameter("timeToDelayRoundStart", this.timeToDelayRoundStart);
      this.timeToEndRound = this.getLongParameter("timeToEndRound", this.timeToEndRound);
      this.timeToEndGameNoPlayer = this.getLongParameter("timeToEndGameNoPlayer", this.timeToEndGameNoPlayer);
   }

   private String createMessage(String messageKey, String player) {
      return this.createMessage(messageKey, player, new String[0][]);
   }

   private String createMessage(String messageKey, String player, String[][] variables) {
      try {
         String messageToSend = (String)this.messages.get(messageKey);
         if (messageToSend == null) {
            log.warn(this.getLogMessage(String.format("Unable to find message for key '%s', using the key as message", messageKey)));
            messageToSend = messageKey;
         }

         messageToSend = messageToSend.replace("BOT_NAME", this.botData.getDisplayName());
         messageToSend = messageToSend.replace("GAME", this.botData.getGame());
         messageToSend = messageToSend.replace("DEFAULT_POT_START", this.df.format(this.defaultPotStart));
         messageToSend = messageToSend.replace("DEFAULT_POT_ENTRY", this.df.format(this.defaultPotEntry));
         messageToSend = messageToSend.replace("CURRENCY", "USD");
         if (player != null) {
            messageToSend = messageToSend.replace("PLAYER", player);
         }

         messageToSend = messageToSend.replace("COMMAND_START", "!start");
         messageToSend = messageToSend.replace("COMMAND_CANCEL", "!n");
         messageToSend = messageToSend.replace("COMMAND_JOIN", "!j");
         messageToSend = messageToSend.replace("COMMAND_ATTACK", "!a");
         messageToSend = messageToSend.replace("COMMAND_HP_CHECK", "!p");
         messageToSend = messageToSend.replace("WINNER_CUP_EMOTE", "(warriors-trophy)");
         messageToSend = messageToSend.replace("LOSER_EMOTE", "(warriors-death)");
         messageToSend = messageToSend.replace("ROUND_NUM", Integer.toString(this.currentRound));
         if (variables != null) {
            String[][] arr$ = variables;
            int len$ = variables.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               String[] vars = arr$[i$];
               if (vars != null && vars.length == 2) {
                  if (messageToSend.contains(vars[0])) {
                     messageToSend = messageToSend.replace(vars[0], vars[1]);
                  }
               } else {
                  log.warn(String.format("Incorrect variable passed to createMessage: %s", StringUtil.join((Object[])vars, ",")));
               }
            }
         }

         return messageToSend;
      } catch (NullPointerException var9) {
         log.error("Outgoing message could not be created, key = " + messageKey, var9);
         return "";
      }
   }

   public boolean isIdle() {
      return this.gameState == BotData.BotStateEnum.NO_GAME && System.currentTimeMillis() - this.timeLastGameFinished > this.idleInterval;
   }

   public boolean canBeStoppedNow() {
      return this.gameState == BotData.BotStateEnum.NO_GAME;
   }

   public synchronized void stopBot() {
      this.endGame();
   }

   public void onUserJoinChannel(String username) {
      switch(this.gameState) {
      case NO_GAME:
         this.sendMessage(this.createMessage("PLAY_NOW", (String)null), username);
         break;
      case GAME_STARTING:
         this.sendMessage(this.createMessage("GAME_STARTING_USER_JOINED", (String)null), username);
         break;
      case GAME_JOINING:
      case GAME_STARTED:
      case PLAYING:
         this.userJoinChannelWhilePlaying(username);
         break;
      default:
         this.sendMessage(this.createMessage("BYSTANDER_PLEASE_WAIT", (String)null), username);
      }

   }

   private synchronized void userJoinChannelWhilePlaying(String username) {
      if (this.playerMap.containsKey(username)) {
         if (this.noPlayerTimer != null) {
            this.noPlayerTimer.cancel(true);
            this.noPlayerTimer = null;
         }

         Player p = (Player)this.playerMap.get(username);
         p.setInChannel(true);
         String message = this.createMessage("PLAYER_REJOIN", username, new String[][]{{"SEQUENCE_EMOTE", p.getPlayerEmote()}});
         this.sendChannelMessage(message);
         if (this.gameState == BotData.BotStateEnum.GAME_STARTED) {
            this.sendMessage(this.createMessage("GAME_STARTED_PLAYER_REJOIN", username), username);
         } else if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
            this.sendMessage(this.createMessage("GAME_JOINING_PLAYER_REJOIN", username), username);
         } else if (this.isInRound) {
            this.sendMessage(this.createMessage("ROUND_STARTED_PLAYER_REJOIN", username), username);
         } else {
            this.sendMessage(this.createMessage("ROUND_STARTING_PLAYER_REJOIN", username), username);
         }
      } else if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
         long timeRemaining = this.timeToRespond - (System.currentTimeMillis() - this.timeGameJoining);
         if (timeRemaining < 1000L) {
            timeRemaining = 1000L;
         }

         this.sendMessage(this.createMessage("JOIN_NOW", this.challengerPlayer.username, new String[][]{{"SEQUENCE_EMOTE", this.challengerPlayer.getPlayerEmote()}, {"TIME", Long.toString(timeRemaining / 1000L)}, {"AMOUNT", this.df.format(this.challengerPlayer.potEntry)}}), username);

         for(int i = 2; i <= this.playerMap.size(); ++i) {
            Iterator i$ = this.playerMap.values().iterator();

            while(i$.hasNext()) {
               Player p = (Player)i$.next();
               if (p.sequence == i) {
                  this.sendMessage(this.createMessage("PLAYER_JOINED", p.username, new String[][]{{"SEQUENCE_EMOTE", p.getPlayerEmote()}, {"AMOUNT", this.df.format(p.potEntry)}, {"NUM", Integer.toString(this.playerMap.size())}}), username);
                  break;
               }
            }
         }

         switch(Player.getMode(username)) {
         case FEVER:
            this.sendMessage(this.createMessage("JOIN_REMINDER_FEVER", (String)null), username);
            break;
         case BERSERK:
            this.sendMessage(this.createMessage("JOIN_REMINDER_BERSERK", (String)null), username);
         }
      } else {
         this.sendMessage(this.createMessage("BYSTANDER_PLEASE_WAIT", (String)null), username);
      }

   }

   public void onUserLeaveChannel(String username) {
      switch(this.gameState) {
      case NO_GAME:
         break;
      case GAME_STARTING:
         if (username.equals(this.challengerPlayer.username)) {
            this.endGame();
         }
         break;
      case GAME_JOINING:
      case GAME_STARTED:
      case PLAYING:
         this.userLeaveChannelWhilePlaying(username);
         break;
      default:
         log.warn(this.getLogMessage("Unknown game state while processing onUserLeaveChannel() state[" + this.gameState + "] user[" + username + "]"));
      }

   }

   private boolean isNoPlayerInChannel() {
      int players_in_channel = 0;
      Iterator i$ = this.playerMap.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, Player> entry = (Entry)i$.next();
         if (((Player)entry.getValue()).isInChannel()) {
            ++players_in_channel;
            break;
         }
      }

      return players_in_channel == 0;
   }

   private synchronized void userLeaveChannelWhilePlaying(String username) {
      if (this.playerMap.containsKey(username)) {
         Player p = (Player)this.playerMap.get(username);
         p.setInChannel(false);
         String message = this.createMessage("PLAYER_LEFT", username, new String[][]{{"SEQUENCE_EMOTE", p.getPlayerEmote()}});
         this.sendChannelMessage(message);
         if (this.isNoPlayerInChannel()) {
            this.noPlayerTimer = this.executor.schedule(new Runnable() {
               public void run() {
                  WarriorsBot.this.endGameNoPlayer();
               }
            }, this.timeToEndGameNoPlayer, TimeUnit.MILLISECONDS);
         }
      }

   }

   private synchronized void endGameNoPlayer() {
      this.noPlayerTimer = null;
      if (this.isNoPlayerInChannel()) {
         this.sendChannelMessage(this.createMessage("END_GAME_NO_PLAYER", (String)null));
         Iterator i$ = this.playerMap.values().iterator();

         while(i$.hasNext()) {
            Player p = (Player)i$.next();

            try {
               this.pot.removePlayer(p.username);
            } catch (Exception var5) {
               log.error(this.getLogMessage(String.format("Unexpected exception occured in removing player [%s] from the pot after all players left the game", p.username)), var5);
            }
         }

         try {
            this.pot.payout(true);
         } catch (Exception var4) {
            log.error(this.getLogMessage(String.format("Unable to pay out pot [%d] to clear the pot after all players left the game", this.pot.getPotID())), var4);
            this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
         }

         this.pot = null;
         this.endGame();
         this.sendChannelMessage(this.createMessage("PLAY_NOW", (String)null));
      }

   }

   public synchronized void onMessage(String username, String messageText, long receivedTimestamp) {
      try {
         messageText = messageText.toLowerCase().trim();
         if (messageText.startsWith("!start") && this.gameState == BotData.BotStateEnum.NO_GAME) {
            this.startNewGame(username, messageText);
         } else if (messageText.startsWith("!n") && this.gameState == BotData.BotStateEnum.GAME_STARTING && username.equals(this.challengerPlayer.username)) {
            this.cancelGame(username);
         } else if ((messageText.equals("!j") || messageText.startsWith("!j ")) && this.gameState == BotData.BotStateEnum.GAME_JOINING && !this.playerMap.containsKey(username)) {
            this.playerJoinsGame(username, messageText);
         } else if (messageText.startsWith("!a") && this.gameState == BotData.BotStateEnum.PLAYING && this.playerMap.containsKey(username)) {
            this.playerAttacks(username, messageText, receivedTimestamp);
         } else if (messageText.startsWith("!p") && this.gameState == BotData.BotStateEnum.PLAYING) {
            this.displayHPAllPlayers(username);
         } else if (this.gameState == BotData.BotStateEnum.GAME_STARTING && !username.equals(this.challengerPlayer.username)) {
            this.sendMessage(this.createMessage("GAME_STARTING_NON_PLAYER", (String)null), username);
         } else if (this.gameState == BotData.BotStateEnum.NO_GAME) {
            this.sendMessage(this.createMessage("PLAY_NOW", (String)null), username);
         } else if (this.hasGameStarted() && this.deadPlayerMap.containsKey(username)) {
            this.sendMessage(this.createMessage("GAME_PLAYING_DEAD_PLAYER", (String)null), username);
         } else if (this.hasGameStarted() && !this.playerMap.containsKey(username)) {
            this.sendMessage(this.createMessage("GAME_STARTED_NON_PLAYER", (String)null), username);
         } else if (this.gameState == BotData.BotStateEnum.GAME_STARTED && this.playerMap.containsKey(username)) {
            this.sendMessage(this.createMessage("GAME_STARTED_PLAYER", (String)null), username);
         } else if (this.gameState == BotData.BotStateEnum.PLAYING && this.playerMap.containsKey(username)) {
            if (this.isInRound) {
               this.sendMessage(this.createMessage("ROUND_STARTED_PLAYER", (String)null), username);
            } else {
               this.sendMessage(this.createMessage("ROUND_STARTING_PLAYER", (String)null), username);
            }
         } else if (this.gameState == BotData.BotStateEnum.GAME_JOINING && this.playerMap.containsKey(username)) {
            this.sendMessage(this.createMessage("GAME_JOINING_PLAYER", (String)null), username);
         } else if (this.gameState == BotData.BotStateEnum.GAME_JOINING && !this.playerMap.containsKey(username)) {
            this.sendMessage(this.createMessage("GAME_JOINING_NON_PLAYER", (String)null), username);
         } else {
            this.sendMessage(messageText + " is not a valid command.", username);
         }

      } catch (Exception var6) {
         this.sendMessage("Error while processing command. Please try again.", username);
         log.error("Unknown Exception: " + var6.getMessage(), var6);
      }
   }

   private synchronized boolean hasGameStarted() {
      return this.gameState == BotData.BotStateEnum.GAME_STARTED || this.gameState == BotData.BotStateEnum.PLAYING;
   }

   private void enterPlayerIntoGame(Player player) throws Exception {
      this.pot.enterPlayer(player.username, player.potEntry, "USD");
      User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      player.userData = userEJB.loadUser(player.username, true, false);
      player.migLevel = MemCacheOrEJB.getUserReputationLevel(player.username);
      this.playerMap.put(player.username, player);
      player.sequence = this.playerMap.size();

      try {
         player.enterGameUpdateMode();
      } catch (Exception var4) {
         log.error(this.getLogMessage(String.format("failed to update mode for player [%s], mode set to %s", player.username, player.mode.name())));
      }

      log.debug(this.getLogMessage(String.format("enter player into game [%s] [seq:%d] [migLevel:%d] [mode:%s]", player.username, player.sequence, player.migLevel, player.mode.name())));
   }

   private WarriorsBot.CustomAmountData getCustomAmountFromCommand(String messageText, String username, String commandName) {
      String[] tokens = messageText.split("\\s+", 2);
      String errMsg = null;
      if (tokens.length == 2 && !StringUtil.isBlank(tokens[1])) {
         try {
            double amount = Double.parseDouble(tokens[1].trim()) / 100.0D;
            if (!(amount < this.minPotEntry)) {
               return new WarriorsBot.CustomAmountData((String)null, amount);
            }

            errMsg = this.createMessage("INVALID_AMOUNT_LESS", username, new String[][]{{"AMOUNT", this.df.format(this.minPotEntry)}});
         } catch (Exception var8) {
            errMsg = this.createMessage("INVALID_AMOUNT", username, new String[][]{{"AMOUNT", tokens[1]}, {"COMMAND", commandName}});
         }

         return new WarriorsBot.CustomAmountData(errMsg, (Double)null);
      } else {
         return new WarriorsBot.CustomAmountData((String)null, (Double)null);
      }
   }

   private synchronized void startNewGame(String username, String messageText) {
      if (this.gameState != BotData.BotStateEnum.NO_GAME) {
         this.sendMessage(this.createMessage("NEW_GAME_GOING_ON", username), username);
      } else {
         this.gameState = BotData.BotStateEnum.GAME_STARTING;

         try {
            if (this.playerMap.size() != 0) {
               this.playerMap.clear();
            }

            this.deadPlayerMap.clear();
            Player player1 = new Player(username, this.normalAttacks, this.berserkAttacks);
            player1.isChallenger = true;
            this.challengerPlayer = player1;
            boolean startShortcut = false;
            if (messageText.endsWith(" !y")) {
               messageText = messageText.substring(0, messageText.length() - 3);
               startShortcut = true;
            }

            WarriorsBot.CustomAmountData cad = this.getCustomAmountFromCommand(messageText, username, "!start");
            if (cad.errMsg != null) {
               this.sendMessage(cad.errMsg, username);
               this.endGame();
            } else {
               player1.potEntry = cad.getAmount(this.defaultPotEntry);
               if (!this.userCanAffordToEnterPot(username, player1.potEntry, true)) {
                  this.endGame();
               } else {
                  try {
                     this.pot = new Pot(this);
                  } catch (Exception var7) {
                     this.sendMessage("Unable to start the game. Please try again later.", username);
                     log.error(this.getLogMessage(String.format("Unable to create pot for user [%s], amount=%f, error=%s", username, player1.potEntry, var7.getMessage())), var7);
                     this.endGame();
                     return;
                  }

                  this.enterPlayerIntoGame(player1);
                  String message;
                  if (startShortcut) {
                     message = this.createMessage("GAME_STARTING_IMMEDIATE", username, new String[][]{{"TIME", Long.toString(this.timeToCancel / 1000L)}, {"AMOUNT", this.df.format(player1.potEntry)}});
                     this.sendMessage(message, username);
                     this.startJoiningPhase();
                  } else {
                     message = this.createMessage("GAME_STARTING_STARTER", username, new String[][]{{"TIME", Long.toString(this.timeToCancel / 1000L)}, {"AMOUNT", this.df.format(player1.potEntry)}});
                     this.sendMessage(message, username);
                     this.cancellationTimer = this.executor.schedule(new Runnable() {
                        public void run() {
                           WarriorsBot.this.startJoiningPhase();
                        }
                     }, this.timeToCancel - 100L, TimeUnit.MILLISECONDS);
                  }

               }
            }
         } catch (Exception var8) {
            this.sendMessage("Unable to start the game. Please try again later.", username);
            log.error("Unable to start game for [" + username + "] :" + var8.getMessage(), var8);
            this.endGame();
         }
      }
   }

   private synchronized void cancelGame(String username) {
      if (this.gameState == BotData.BotStateEnum.GAME_STARTING) {
         if (this.cancellationTimer != null) {
            this.cancellationTimer.cancel(true);
            this.cancellationTimer = null;
         }

         try {
            if (username != null) {
               this.sendMessage(this.createMessage("CANCEL_GAME", username), username);
            }

            this.endGame();
         } catch (Exception var3) {
            log.error("Unexpected exception: " + var3.getMessage(), var3);
         }

      }
   }

   private synchronized void startJoiningPhase() {
      if (this.gameState == BotData.BotStateEnum.GAME_STARTING) {
         this.gameState = BotData.BotStateEnum.GAME_JOINING;
         this.timeGameJoining = System.currentTimeMillis();
         this.cancellationTimer = null;
         this.sendChannelMessage(this.createMessage("JOIN_NOW", this.challengerPlayer.username, new String[][]{{"SEQUENCE_EMOTE", this.challengerPlayer.getPlayerEmote()}, {"TIME", Long.toString(this.timeToRespond / 1000L)}, {"AMOUNT", this.df.format(this.challengerPlayer.potEntry)}}));
         String message2 = this.createMessage("WAITING_FOR_PLAYERS", (String)null);
         this.sendChannelMessage(message2);
         String[] allParticipants = this.getChannelProxy().getParticipants((String)null);
         String[] arr$ = allParticipants;
         int len$ = allParticipants.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String participant = arr$[i$];
            if (!this.playerMap.containsKey(participant)) {
               switch(Player.getMode(participant)) {
               case FEVER:
                  this.sendMessage(this.createMessage("JOIN_REMINDER_FEVER", (String)null), participant);
                  break;
               case BERSERK:
                  this.sendMessage(this.createMessage("JOIN_REMINDER_BERSERK", (String)null), participant);
               }
            }
         }

         this.challengeResponseTimer = this.executor.schedule(new Runnable() {
            public void run() {
               WarriorsBot.this.challengeTimesOut();
            }
         }, this.timeToRespond - (System.currentTimeMillis() - this.timeGameJoining), TimeUnit.MILLISECONDS);
      }
   }

   private synchronized void challengeTimesOut() {
      if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
         if (this.challengeResponseTimer != null) {
            this.challengeResponseTimer = null;
         }

         if (this.playerMap.size() >= this.minPlayers) {
            this.prepareForGames();
         } else {
            this.sendChannelMessage(this.createMessage("JOINING_END", (String)null, new String[][]{{"NUM", Integer.toString(this.minPlayers)}}));
            this.endGame();
         }

      }
   }

   private synchronized void prepareForGames() {
      if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
         try {
            this.timeGameStarted = System.currentTimeMillis();
            this.gameState = BotData.BotStateEnum.GAME_STARTED;
            this.sendChannelMessage(this.createMessage("PREPARING_TO_FIGHT_1", (String)null));
            this.sendChannelMessage(this.createMessage("PREPARING_TO_FIGHT_2", (String)null));
            this.sendChannelMessage(this.createMessage("PREPARING_TO_FIGHT_3", (String)null));
            this.sendChannelMessage(this.createMessage("PREPARING_TO_FIGHT_4", (String)null));
            int opponentMigLevel = this.challengerPlayer.migLevel;
            Iterator i$ = this.playerMap.values().iterator();

            while(i$.hasNext()) {
               Player player = (Player)i$.next();
               if (player.migLevel > opponentMigLevel) {
                  opponentMigLevel = player.migLevel;
               }
            }

            i$ = this.playerMap.entrySet().iterator();

            while(i$.hasNext()) {
               Entry<String, Player> entry = (Entry)i$.next();
               Player p = (Player)entry.getValue();
               p.HP = this.calculateHitPoints(p, opponentMigLevel);
               p.maxHP = p.HP;
               log.debug(this.getLogMessage(String.format("start game [%s] [seq:%d] [HP:%d] [migLevel:%d] [mode:%s]", p.username, p.sequence, p.HP, p.migLevel, p.mode.name())));
            }

            this.hpDisplayTimer = this.executor.schedule(new Runnable() {
               public void run() {
                  WarriorsBot.this.displayHPAndStartGame();
               }
            }, Math.max(0L, this.timeToDisplayHP - (System.currentTimeMillis() - this.timeGameStarted)), TimeUnit.MILLISECONDS);
            List<String> playerUsernames = new ArrayList(this.playerMap.size());
            List<Integer> playerUserids = new ArrayList(this.playerMap.size());
            Iterator i$ = this.playerMap.values().iterator();

            while(i$.hasNext()) {
               Player p = (Player)i$.next();
               playerUsernames.add(p.username);
               playerUserids.add(p.userData.userID);
            }

            this.incrementGamesPlayed(Leaderboard.Type.WARRIORS_GAMES_PLAYED, playerUsernames, playerUserids);
            this.logGamesPlayed(this.playerMap.size(), this.playerMap.keySet(), this.pot.getTotalAmountInBaseCurrency());
         } catch (Exception var6) {
            this.sendChannelMessage("Unxpected error while starting game. Please try again.");
            log.error("Unxpected exeception: " + var6.getMessage(), var6);
            this.endGame();
         }

      }
   }

   private synchronized void displayHPAllPlayers(String targetUsername) {
      int numPlayers = this.playerMap.size() + this.deadPlayerMap.size();

      for(int i = 1; i <= numPlayers; ++i) {
         Player player = null;
         boolean dead = false;
         Iterator message = this.playerMap.values().iterator();

         Player p;
         while(message.hasNext()) {
            p = (Player)message.next();
            if (p.sequence == i) {
               player = p;
               break;
            }
         }

         if (player == null) {
            dead = true;
            message = this.deadPlayerMap.values().iterator();

            while(message.hasNext()) {
               p = (Player)message.next();
               if (p.sequence == i) {
                  player = p;
                  break;
               }
            }
         }

         if (null != player) {
            message = null;
            String message;
            if (dead) {
               message = this.createMessage("PLAYER_HITPOINT_DEAD", player.username, new String[][]{{"SEQUENCE_EMOTE", player.getPlayerEmote()}, {"MODE_MSG", player.getModeDisplay()}});
            } else {
               message = this.createMessage("PLAYER_HITPOINT", player.username, new String[][]{{"SEQUENCE_EMOTE", player.getPlayerEmote()}, {"HITPOINT", Integer.toString(player.HP)}, {"MODE_MSG", player.getModeDisplay()}});
            }

            if (targetUsername == null) {
               this.sendChannelMessage(message);
            } else {
               this.sendMessage(message, targetUsername);
            }
         }
      }

   }

   private synchronized void displayHPAndStartGame() {
      this.displayHPAllPlayers((String)null);
      this.sendChannelMessage(this.createMessage("BATTLE_BEGIN", (String)null));
      this.sendChannelMessage(this.createMessage("BATTLE_BEGIN_HELP", (String)null));
      this.gameState = BotData.BotStateEnum.PLAYING;
      this.startNewRound();
   }

   private synchronized void startNewRound() {
      if (this.gameState == BotData.BotStateEnum.PLAYING) {
         ++this.currentRound;
         this.isInRound = false;
         this.sendChannelMessage(this.createMessage("ROUND_STARTING", (String)null, new String[][]{{"TIME", Long.toString(this.timeToDelayRoundStart / 1000L)}}));
         this.roundStartDelayTimer = this.executor.schedule(new Runnable() {
            public void run() {
               WarriorsBot.this.roundStarted();
            }
         }, this.timeToDelayRoundStart, TimeUnit.MILLISECONDS);
      }
   }

   private synchronized void roundStarted() {
      if (this.gameState == BotData.BotStateEnum.PLAYING) {
         this.roundStartDelayTimer = null;
         this.sendChannelMessage(this.createMessage("ROUND_STARTED", (String)null, new String[][]{{"TIME", Long.toString(this.timeToEndRound / 1000L)}}));
         this.isInRound = true;
         this.attacksInRound.clear();
         this.roundEndTimer = this.executor.schedule(new Runnable() {
            public void run() {
               WarriorsBot.this.roundEnded();
            }
         }, this.timeToEndRound, TimeUnit.MILLISECONDS);
      }
   }

   private synchronized void roundEnded() {
      if (this.gameState == BotData.BotStateEnum.PLAYING) {
         if (this.isInRound) {
            this.roundEndTimer = null;
            this.isInRound = false;
            List<String> nonAttackers = new ArrayList(this.playerMap.size());
            Iterator i$ = this.playerMap.values().iterator();

            while(i$.hasNext()) {
               Player p = (Player)i$.next();
               if (!this.attacksInRound.containsKey(p.username)) {
                  nonAttackers.add(String.format("%s %s", p.getPlayerEmote(), p.username));
               }
            }

            if (nonAttackers.size() > 0) {
               this.sendChannelMessage(this.createMessage("ROUND_IDLE", (String)null, new String[][]{{"PLAYES", StringUtil.join((Collection)nonAttackers, ", ")}}));
            }

            this.sendChannelMessage(this.createMessage("ROUND_ENDED", (String)null));
            this.displayHPAllPlayers((String)null);
            if (this.currentRound >= this.maxRound) {
               this.gameTimeOut();
            } else {
               this.startNewRound();
            }

         }
      }
   }

   private synchronized void gameTimeOut() {
      if (this.gameState == BotData.BotStateEnum.PLAYING) {
         ArrayList<Player> survivors = new ArrayList();
         Iterator i$ = this.playerMap.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, Player> entry = (Entry)i$.next();
            Player p = (Player)entry.getValue();
            if (p.HP > 0) {
               survivors.add(p);
            }
         }

         Collections.sort(survivors);
         Player winner = (Player)survivors.get(survivors.size() - 1);

         for(int i = 0; i < survivors.size() - 1; ++i) {
            this.removePlayerFromPot((Player)survivors.get(i), winner, false);
         }

         this.makeWinner(winner.username);
      }
   }

   private synchronized void playerJoinsGame(String playerName, String messageText) {
      if (this.gameState == BotData.BotStateEnum.GAME_JOINING) {
         if (this.challengerPlayer == null) {
            this.sendMessage("PLAYER: Unable to accept the challenge, please try again", playerName);
            log.error(this.getLogMessage("Unable to join challenge [" + playerName + "]"));
         } else {
            Player player2 = new Player(playerName, this.normalAttacks, this.berserkAttacks);
            WarriorsBot.CustomAmountData cad = this.getCustomAmountFromCommand(messageText, playerName, "!j");
            if (cad.errMsg != null) {
               this.sendMessage(cad.errMsg, playerName);
            } else {
               player2.potEntry = cad.getAmount(this.defaultPotEntry);
               if (this.userCanAffordToEnterPot(player2.username, player2.potEntry, true)) {
                  try {
                     this.enterPlayerIntoGame(player2);
                  } catch (Exception var6) {
                     this.sendMessage("PLAYER: Unable to accept the challenge, please try again", player2.username);
                     log.error(this.getLogMessage("Unable to accept challenge:" + var6.getMessage()), var6);
                     return;
                  }

                  this.sendChannelMessage(this.createMessage("PLAYER_JOINED", player2.username, new String[][]{{"SEQUENCE_EMOTE", player2.getPlayerEmote()}, {"AMOUNT", this.df.format(player2.potEntry)}, {"NUM", Integer.toString(this.playerMap.size())}}));
                  if (this.playerMap.size() >= this.maxPlayers) {
                     if (this.challengeResponseTimer != null) {
                        this.challengeResponseTimer.cancel(true);
                        this.challengeResponseTimer = null;
                     }

                     this.sendChannelMessage(this.createMessage("MAX_PLAYER_JOINED", (String)null, new String[][]{{"NUM", Integer.toString(this.playerMap.size())}}));
                     this.prepareForGames();
                  }

               }
            }
         }
      }
   }

   private synchronized void playerAttacks(String username, String messageText, long receivedTimestamp) {
      if (this.gameState == BotData.BotStateEnum.PLAYING) {
         if (!this.isInRound) {
            this.sendMessage(this.createMessage("ROUND_STARTING_PLAYER", (String)null), username);
         } else {
            Player attacker = (Player)this.playerMap.get(username);
            int curNumAttack = this.attacksInRound.containsKey(username) ? (Integer)this.attacksInRound.get(username) : 0;
            if (curNumAttack >= this.maxAttackPerRound) {
               this.sendMessage(this.createMessage("ROUND_ATTACK_EXCEEDED", username, new String[][]{{"MAX_ATTACK_PER_ROUND", Integer.toString(this.maxAttackPerRound)}}), username);
            } else {
               Player defender = null;
               int target = false;
               messageText = messageText.trim();
               Iterator i$;
               int target;
               if (messageText.equals("!a")) {
                  target = RANDOM_GENERATOR.nextInt(this.playerMap.size() - 1);
                  int curIndex = 0;
                  i$ = this.playerMap.values().iterator();

                  while(i$.hasNext()) {
                     Player player = (Player)i$.next();
                     if (player.sequence != attacker.sequence) {
                        if (target == curIndex) {
                           defender = player;
                           break;
                        }

                        ++curIndex;
                     }
                  }

                  log.debug("random attack target " + target + " of " + (this.playerMap.size() - 1) + " for [" + username + "]");
               } else {
                  String playerNumStr = messageText.substring("!a".length()).trim();

                  try {
                     target = Integer.parseInt(playerNumStr);
                  } catch (NumberFormatException var16) {
                     this.sendMessage("Invalid target [" + playerNumStr + "] specified for attack command.", username);
                     return;
                  }

                  if (target == attacker.sequence) {
                     this.sendMessage(this.createMessage("ATTACK_SELF", (String)null, new String[][]{{"SEQUENCE_EMOTE", attacker.getPlayerEmote()}}), username);
                     return;
                  }

                  log.debug("attack target [" + target + "] for [" + username + "]");
                  i$ = this.playerMap.entrySet().iterator();

                  while(i$.hasNext()) {
                     Entry<String, Player> entry = (Entry)i$.next();
                     Player p = (Player)entry.getValue();
                     if (p.sequence == target) {
                        defender = p;
                        break;
                     }
                  }
               }

               if (defender == null) {
                  Iterator i$ = this.deadPlayerMap.values().iterator();

                  while(i$.hasNext()) {
                     Player p = (Player)i$.next();
                     if (p.sequence == target) {
                        defender = p;
                        break;
                     }
                  }

                  if (defender == null) {
                     log.error(this.getLogMessage(String.format("Can't find defender sequence [%d] for [%s] to attack", target, username)));
                     this.sendMessage("Invalid target [" + target + "] specified for attack command.", username);
                  } else {
                     log.error(this.getLogMessage(String.format("Can't find defender sequence [%d] for [%s] to attack", target, username)));
                     this.sendMessage(this.createMessage("ATTACK_DEAD_PLAYER", defender.username, new String[][]{{"SEQUENCE_EMOTE", defender.getPlayerEmote()}}), username);
                  }

               } else {
                  log.debug("Found target [" + target + "] for [" + username + "]");
                  Attack attack = attacker.getAttack();
                  int damage = attack.getRandomizedAttackDamage();
                  defender.HP -= damage;
                  if (defender.HP < 0) {
                     defender.HP = 0;
                  }

                  String message = this.createMessage("ATTACK_MESSAGE", attacker.username, new String[][]{{"PLAYE2", defender.username}, {"ATTACK_EMOTE", attack.getAttackEmote()}, {"SEQUENCE_EMOTE", attacker.getPlayerEmote()}, {"SEQUENCE2_EMOTE", defender.getPlayerEmote()}, {"DAMAGE_POINT", Integer.toString(damage)}, {"HITPOINT", Integer.toString(defender.HP)}});
                  this.sendChannelMessage(message);
                  if (defender.HP == 0) {
                     this.removePlayerFromPot(defender, attacker, true);
                     this.incrementMostWins(Leaderboard.Type.WARRIORS_NUM_KILLS, username, attacker.userData.userID);
                     if (this.playerMap.size() == 1) {
                        this.makeWinner(attacker.username);
                        return;
                     }
                  } else {
                     String messageKey = defender.getMilestoneMessageKey();
                     if (messageKey != null) {
                        message = this.createMessage(messageKey, defender.username, new String[][]{{"SEQUENCE_EMOTE", defender.getPlayerEmote()}});
                        this.sendChannelMessage(message);
                     }
                  }

                  this.attacksInRound.put(username, curNumAttack + 1);
                  if (this.attacksInRound.size() == this.playerMap.size()) {
                     boolean allAttacked = true;
                     Iterator i$ = this.playerMap.values().iterator();

                     while(i$.hasNext()) {
                        Player p = (Player)i$.next();
                        Integer numOfAttacks = (Integer)this.attacksInRound.get(p.username);
                        if (numOfAttacks != null && numOfAttacks < this.maxAttackPerRound) {
                           allAttacked = false;
                           break;
                        }
                     }

                     if (allAttacked) {
                        if (this.roundEndTimer != null) {
                           this.roundEndTimer.cancel(true);
                           this.roundEndTimer = null;
                        }

                        this.roundEnded();
                     }
                  }

               }
            }
         }
      }
   }

   private synchronized void makeWinner(String username) {
      if (this.gameState == BotData.BotStateEnum.PLAYING) {
         Player winning_player = (Player)this.playerMap.get(username);
         double payout = 0.0D;

         try {
            double payoutInBaseCurrency = this.pot.payout(true);
            Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
            payout = accountEJB.convertCurrency(payoutInBaseCurrency, "AUD", "USD");
            this.logMostWins(username, payoutInBaseCurrency);
            this.incrementMostWins(Leaderboard.Type.WARRIORS_MOST_WINS, username, winning_player.userData.userID);
         } catch (Exception var10) {
            log.error(this.getLogMessage(String.format("Unable to pay out pot [%d] amount %s %f to [%s]", this.pot.getPotID(), "USD", payout, username)), var10);
         }

         if (payout < 0.0D) {
            this.sendChannelMessageAndPopUp(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.PAYOUT_FAILURE_MESSAGE));
         } else {
            try {
               long count = winning_player.endGameUpdateModeStat(true);
               if (count >= 3L) {
                  String messageKey = this.getMessageKeyForModeEndGame(winning_player, count, true);
                  if (messageKey != null) {
                     this.sendMessage(this.createMessage(messageKey, winning_player.username), winning_player.username);
                  }
               }
            } catch (Exception var9) {
               log.error(this.getLogMessage(String.format("failed to update mode stat after game ended for winner [%s]", username)));
            }

            this.sendChannelMessage(this.createMessage("CHAMPION_MESSAGE", username, new String[][]{{"SEQUENCE_EMOTE", winning_player.getPlayerEmote()}}));
            this.sendChannelMessageAndPopUp(this.createMessage("WINNER_MESSAGE", username).replace("AMOUNT", this.df.format(payout)));
            this.sendChannelMessage(this.createMessage("PLAY_NOW", (String)null));
         }

         this.pot = null;
         this.endGame();
      }
   }

   private synchronized void endGame() {
      if (this.gameState != BotData.BotStateEnum.NO_GAME) {
         log.info(this.getLogMessage("Ending game, cur game state: " + this.gameState.name()));
         if (this.pot != null) {
            try {
               this.pot.cancel();
            } catch (Exception var2) {
               log.error(this.getLogMessage("Unable to endGame() with pot ID " + this.pot.getPotID() + ": " + var2.getMessage()), var2);
            }

            this.pot = null;
         }

         if (this.cancellationTimer != null) {
            this.cancellationTimer.cancel(true);
            this.cancellationTimer = null;
         }

         if (this.challengeResponseTimer != null) {
            this.challengeResponseTimer.cancel(true);
            this.challengeResponseTimer = null;
         }

         if (this.hpDisplayTimer != null) {
            this.hpDisplayTimer.cancel(true);
            this.hpDisplayTimer = null;
         }

         if (this.roundStartDelayTimer != null) {
            this.roundStartDelayTimer.cancel(true);
            this.roundStartDelayTimer = null;
         }

         if (this.roundEndTimer != null) {
            this.roundEndTimer.cancel(true);
            this.roundEndTimer = null;
         }

         if (this.noPlayerTimer != null) {
            this.noPlayerTimer.cancel(true);
            this.noPlayerTimer = null;
         }

         this.playerMap.clear();
         this.deadPlayerMap.clear();
         this.currentRound = 0;
         this.challengerPlayer = null;
         this.timeLastGameFinished = System.currentTimeMillis();
         this.gameState = BotData.BotStateEnum.NO_GAME;
      }
   }

   private String getMessageKeyForModeEndGame(Player player, long count, boolean won) {
      if (count >= 3L) {
         if (count == 3L) {
            return won ? "END_GAME_FEVER" : "END_GAME_BERSERK";
         } else {
            String modeStr = "";
            if (player.isFever()) {
               modeStr = "FEVER";
            } else {
               if (!player.isBerserk()) {
                  return null;
               }

               modeStr = "BERSERK";
            }

            return String.format("END_GAME_%s_%d_MORE", modeStr, 6L - count);
         }
      } else {
         return null;
      }
   }

   private void removePlayerFromPot(Player loser, Player attacker, boolean broadcaseLostMessage) {
      if (log.isDebugEnabled()) {
         log.debug("Player lost: " + loser.username + ". Removing from pot.");
      }

      if (this.pot != null) {
         try {
            this.pot.removePlayer(loser.username);

            try {
               long count = loser.endGameUpdateModeStat(false);
               if (count >= 3L) {
                  String messageKey = this.getMessageKeyForModeEndGame(loser, count, false);
                  if (messageKey != null) {
                     this.sendMessage(this.createMessage(messageKey, loser.username), loser.username);
                  }
               }
            } catch (Exception var7) {
               log.error(this.getLogMessage(String.format("failed to update mode stat for loser [%s]", loser.username)), var7);
            }

            this.playerMap.remove(loser.username);
            this.deadPlayerMap.put(loser.username, loser);
            if (broadcaseLostMessage) {
               String message = this.createMessage("LOST_MESSAGE", attacker.username, new String[][]{{"PLAYE2", loser.username}, {"SEQUENCE_EMOTE", attacker.getPlayerEmote()}, {"SEQUENCE2_EMOTE", loser.getPlayerEmote()}, {"NUM", Integer.toString(this.playerMap.size())}});
               this.sendChannelMessage(message);
            }
         } catch (Exception var8) {
            log.error(this.getLogMessage(String.format("Unexpected exception occured in removing bottom player [%s] from the pot", loser.username)), var8);
         }
      }

   }

   private int calculateHitPoints(Player player, int opponentMigLevel) {
      String username = player.username;
      int migLevel = player.migLevel;
      int numFriends = player.userData.broadcastList.size();
      int migLevelDiff = Math.max(0, opponentMigLevel - player.migLevel);
      double ante = player.potEntry;
      boolean isFever = player.isFever();
      double migDNA = (double)username.length() / (double)SystemProperty.getInt((String)"MaxUsernameLength", 128);
      int maxMigLevel = SystemProperty.getInt((String)"MaxMigLevel", 100);
      int minHp = SystemProperty.getInt((String)"MinHp", 400);
      log.debug("username [" + username + "] migLevel [" + migLevel + "] numFriends [" + numFriends + "] migLevelDifference [" + migLevelDiff + "] ante[" + ante + "] migDNA[" + migDNA + "]");
      int hitPoints = (int)(Math.ceil(1.0D * (double)migLevel / (double)maxMigLevel * (double)maxHitPointsByMigLevel + (double)minHp + 1.0D * (double)numFriends / (double)SystemProperty.getInt((String)"MaxFusionContacts", 2000) * (double)maxHitPointsByFriends) + Math.pow((double)migLevelDiff, 2.0D) / Math.pow((double)maxMigLevel, 2.0D) * (double)maxHitPointsByMigLevelDiff + 100.0D * ante / (double)maxAnteForHitPoints * (double)maxHitPointsByAnte + migDNA * (double)maxHitPointsByMigDNA + (double)(isFever ? maxHitPointsByFever : 0));
      return hitPoints;
   }

   private class CustomAmountData {
      String errMsg = null;
      Double amount;

      CustomAmountData(String errMsg, Double amount) {
         this.errMsg = errMsg;
         this.amount = amount;
      }

      double getAmount(double defaultAmount) {
         return this.amount == null ? defaultAmount : this.amount;
      }
   }
}
