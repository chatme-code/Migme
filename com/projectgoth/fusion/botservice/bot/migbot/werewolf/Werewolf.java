package com.projectgoth.fusion.botservice.bot.migbot.werewolf;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.slice.BotChannelPrx;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class Werewolf extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Werewolf.class));
   List<String> players;
   public static final String TIMER_JOIN_GAME = "timerJoinGame";
   public static final String MAX_PLAYERS = "maxPlayers";
   public static final String TIMER_DAYTIME = "dayTime";
   public static final String TIMER_NIGHTTIME = "nightTime";
   public static final String TIMER_VOTE = "voteTime";
   public static final String IS_TIE_GAME_ON = "tieGame";
   public static final String TIMER_IDLE = "timerIdle";
   public static final long TIMER_DAYTIME_VALUE = 45L;
   public static final long TIMER_NIGHTTIME_VALUE = 45L;
   public static final long TIMER_VOTE_VALUE = 60L;
   public static final long IDLE_TIME_VALUE = 5L;
   public static final String COMMAND_VOTE = "!v";
   public static final String COMMAND_KILL = "!k";
   public static final String COMMAND_SEE = "!s";
   public static final String COMMAND_ALIVE = "!a";
   public static final String COMMAND_ROLE = "!r";
   Date lastActivityTime;
   private BotData.BotStateEnum gameState;
   long timeToJoinGame;
   public int minPlayers;
   public int maxPlayers;
   long timeAllowedToIdle;
   Vector priority;
   Vector votes;
   Vector wolves;
   Vector wolfVictim;
   final int JOINTIME;
   final int MINPLAYERS;
   final int MAXPLAYERS;
   final int TWOWOLVES;
   long dayTime;
   long nightTime;
   long voteTime;
   int seer;
   int toSee;
   int[] notVoted;
   int[] wasVoted;
   boolean playing;
   boolean day;
   boolean gameStart;
   boolean firstDay;
   boolean firstNight;
   boolean tieGame;
   boolean timeToVote;
   boolean[] wolf;
   boolean[] dead;
   boolean[] voted;
   String role;
   String oneWolf;
   String manyWolves;
   String winnerString;

   public Werewolf(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.timeToJoinGame = 90L;
      this.minPlayers = 5;
      this.maxPlayers = 12;
      this.timeAllowedToIdle = 30L;
      this.JOINTIME = 90;
      this.MINPLAYERS = 5;
      this.MAXPLAYERS = 12;
      this.TWOWOLVES = 8;
      this.dayTime = 90L;
      this.nightTime = 60L;
      this.voteTime = 30L;
      this.toSee = -1;
      this.playing = false;
      this.day = false;
      this.gameStart = false;
      this.tieGame = true;
      this.loadGameConfig();
      log.info("WerewolfBot [" + this.instanceID + "] added to channel [" + this.channel + "]");
      this.sendChannelMessage(this.createMessage("BOT_ADDED"));
      String message = this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
      this.sendChannelMessage(message);
      this.players = new ArrayList();
      this.priority = new Vector(1, 1);
      this.dead = new boolean[this.players.size()];
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
            log.debug("Bot has been idle for " + minutes + (minutes == 1L ? " minute" : " minutes") + ". Canceling pot and resetting game, if any");
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

   private void loadGameConfig() {
      this.timeToJoinGame = this.getLongParameter("timerJoinGame", this.timeToJoinGame);
      this.dayTime = this.getLongParameter("dayTime", 45L);
      this.nightTime = this.getLongParameter("nightTime", 45L);
      this.voteTime = this.getLongParameter("voteTime", 60L);
      this.tieGame = this.getBooleanParameter("tieGame", false);
      this.maxPlayers = this.getIntParameter("maxPlayers", 12);
      this.oneWolf = (String)this.messages.get("1-WOLF");
      this.manyWolves = (String)this.messages.get("MANY-WOLVES");
      this.timeAllowedToIdle = this.getLongParameter("timerIdle", 5L);
   }

   public List<String> getPlayers() {
      return this.players;
   }

   protected String createMessage(String message) {
      return this.createMessage(message, (String)null, (String)null, -1, (String)null);
   }

   private String createMessage(String message, String username) {
      return this.createMessage(message, username, (String)null, -1, (String)null);
   }

   private String createMessage(String message, String username, String username2) {
      return this.createMessage(message, username, username2, -1, (String)null);
   }

   private String createMessage(String messageKey, String player, String player2, int time, String errorInput) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Looking for messageKey: " + messageKey);
         }

         String messageToSend = (String)this.messages.get(messageKey);
         messageToSend = messageToSend.replaceAll("BOTNAME", this.botData.getDisplayName());
         messageToSend = messageToSend.replaceAll("CMD_NO", "!n");
         messageToSend = messageToSend.replaceAll("CMD_JOIN", "!j");
         messageToSend = messageToSend.replaceAll("MINPLAYERS", this.minPlayers + "");
         if (player != null) {
            messageToSend = messageToSend.replaceAll("PLAYER", player);
         }

         if (StringUtils.hasLength(this.winnerString)) {
            messageToSend = messageToSend.replaceAll("WINNER_STRING", this.winnerString);
            String winningsText = (String)this.messages.get("WINNINGS_ZERO");
            messageToSend = messageToSend.replaceAll("WINNINGS_TEXT", winningsText);
         }

         messageToSend = messageToSend.replaceAll("CURRENCY", "USD");
         messageToSend = messageToSend.replaceAll("AMOUNT_POT", "0");
         messageToSend = messageToSend.replaceAll("DENOM", "c");
         if (player2 != null) {
            messageToSend = messageToSend.replaceAll("PLAYR2", player2);
         }

         messageToSend = messageToSend.replaceAll("TIME", "" + time);
         if (this.role != null) {
            messageToSend = messageToSend.replaceAll("ISAWOLF?", this.role);
            messageToSend = messageToSend.replaceAll("ROLE", this.role);
         }

         if (this.wolves != null && !this.wolves.isEmpty()) {
            messageToSend = messageToSend.replaceAll("WEREWOLF", this.wolves.size() == 1 ? this.oneWolf : this.manyWolves);
         }

         if (StringUtils.hasLength(errorInput)) {
            messageToSend = messageToSend.replaceAll("ERROR_INPUT", errorInput);
         }

         return messageToSend;
      } catch (NullPointerException var8) {
         log.error("Outgoing message could not be created, key = " + messageKey, var8);
         return "";
      }
   }

   public synchronized void onMessage(String username, String messageText, long receivedTimestamp) {
      if (messageText.equalsIgnoreCase("!n")) {
         this.processNoMessage(username);
      } else if (messageText.startsWith("!start")) {
         if (this.getGameState() == BotData.BotStateEnum.NO_GAME) {
            try {
               this.startGame(username);
            } catch (Exception var11) {
               log.error("Error starting game with default amount: ", var11);
            }
         } else {
            this.sendGameCannotStartMessage(username);
         }
      }

      if (this.playing) {
         if (messageText.toLowerCase().equalsIgnoreCase("!j")) {
            if (this.gameStart) {
               if (!this.isNameAdded(username)) {
                  if (this.players.size() < 12) {
                     this.addPlayer(username);
                     if (!this.players.contains(username)) {
                        this.sendMessage(this.createMessage("COULD-NOT-ADD", username), username);
                     }
                  } else {
                     this.sendMessage(this.createMessage("MAX-REACHED", username), username);
                  }
               } else {
                  this.sendMessage(this.createMessage("ALREADY_IN_GAME", username), username);
               }
            } else {
               this.sendMessage(this.createMessage("GAME-PLAYING", username), username);
            }
         } else if (this.players.contains(username)) {
            int i;
            String see;
            int i;
            if (this.timeToVote) {
               if (messageText.toLowerCase().startsWith("!v ")) {
                  i = this.players.lastIndexOf(username);
                  if (i != -1 && this.dead != null && this.dead.length > 0 && this.dead[i]) {
                     this.sendMessage(this.createMessage("DEAD_CANT_VOTE", username), username);
                     return;
                  }

                  if (this.hasVoted(username)) {
                     this.sendMessage(this.createMessage("ALREADY_VOTED", username), username);
                  } else {
                     try {
                        String choice = messageText.substring(messageText.indexOf(" ") + 1, messageText.length());
                        choice = choice.trim();
                        if (this.players.contains(choice) && !choice.equalsIgnoreCase(username)) {
                           Vote vote = new Vote(username, choice);
                           int i = 0;

                           label250:
                           while(true) {
                              if (i >= this.players.size()) {
                                 i = 0;

                                 while(true) {
                                    if (i >= this.players.size()) {
                                       break label250;
                                    }

                                    if (username.equals((String)this.players.get(i))) {
                                       if (!this.dead[i]) {
                                          this.voted[i] = true;
                                          this.notVoted[i] = 0;
                                          this.sendChannelMessage(this.createMessage("HAS-VOTED", username, choice));
                                       } else {
                                          this.sendMessage(this.createMessage("DEAD_CANT_VOTE", username), username);
                                       }
                                    }

                                    ++i;
                                 }
                              }

                              if (this.players.get(i) != null) {
                                 if (((String)this.players.get(i)).equalsIgnoreCase(choice) && !this.dead[i]) {
                                    while(!this.votes.add(vote)) {
                                    }
                                 } else if (((String)this.players.get(i)).equalsIgnoreCase(choice) && this.dead[i]) {
                                    this.sendMessage(this.createMessage("ALREADY_DEAD", username), username);
                                    return;
                                 }
                              }

                              ++i;
                           }
                        } else {
                           this.sendMessage(this.createMessage("INVALID_CHOICE", username), username);
                        }
                     } catch (Exception var12) {
                        this.sendMessage(this.createMessage("INVALID_VOTE_COMMAND", username), username);
                        var12.printStackTrace();
                     }
                  }
               } else if (messageText.toLowerCase().startsWith("!k ") || messageText.toLowerCase().startsWith("!s ")) {
                  this.sendMessage(this.createMessage("VOTING_TIME_ONLY", username), username);
               }
            } else if (!this.day) {
               if (messageText.toLowerCase().startsWith("!k ")) {
                  if (this.players.contains(username)) {
                     boolean isWolf = false;
                     i = -1;

                     int otherWolf;
                     for(otherWolf = 0; otherWolf < this.wolves.size(); ++otherWolf) {
                        if (this.wolves.get(otherWolf).equals(username)) {
                           isWolf = true;
                           i = otherWolf;
                           break;
                        }
                     }

                     if (isWolf) {
                        otherWolf = -1;
                        if (this.wolves.size() > 1) {
                           otherWolf = i == 0 ? 1 : 0;
                        }

                        try {
                           String victim = messageText.substring(messageText.indexOf(" ") + 1, messageText.length());
                           victim = victim.trim();
                           if (this.players.contains(victim)) {
                              if (otherWolf != -1 && victim.equals(this.wolves.get(otherWolf))) {
                                 if (log.isDebugEnabled()) {
                                    log.debug("Invalid: " + username + " tried to kill the other wolf " + victim);
                                 }

                                 this.sendMessage(this.createMessage("CANT_KILL_OTHER_WOLF", username, victim), username);
                                 return;
                              }

                              boolean isDead = false;

                              int i;
                              for(i = 0; i < this.players.size(); ++i) {
                                 if (this.players.get(i) != null && ((String)this.players.get(i)).equalsIgnoreCase(victim) && this.dead[i]) {
                                    isDead = true;
                                 }
                              }

                              if (!isDead) {
                                 if (!victim.equalsIgnoreCase(username)) {
                                    while(true) {
                                       if (this.wolfVictim.add(victim)) {
                                          if (this.wolves.size() == 1) {
                                             this.sendMessage(this.createMessage("WOLF-CHOICE", username, victim), username);
                                          } else {
                                             this.sendMessage(this.createMessage("WOLVES-CHOICE", username, victim), username);

                                             for(i = 0; i < this.wolves.size(); ++i) {
                                                if (!((String)this.wolves.get(i)).equals(username)) {
                                                   this.sendMessage(this.createMessage("WOLVES-CHOICE-OTHER" + (i + 1), username, victim), (String)this.wolves.get(i));
                                                }
                                             }
                                          }
                                          break;
                                       }
                                    }
                                 } else {
                                    this.sendMessage(this.createMessage("CANT_EAT_SELF", username), username);
                                 }
                              } else {
                                 this.sendMessage(this.createMessage("ALREADY_DEAD", username), username);
                              }
                           } else {
                              this.sendMessage(this.createMessage("VALID_PLAYER", username), username);
                           }
                        } catch (Exception var14) {
                           var14.printStackTrace();
                           this.sendMessage(this.createMessage("VALID_PLAYER", username), username);
                        }
                     } else {
                        this.sendMessage(this.createMessage("NOT-WOLF", username), username);
                     }
                  } else {
                     this.sendMessage(this.createMessage("NOT_PLAYING", username), username);
                  }
               }

               if (messageText.toLowerCase().startsWith("!s ")) {
                  try {
                     if (!this.players.contains(username)) {
                        this.sendMessage(this.createMessage("INVALID_CHOICE", username), username);
                     } else if (this.players.get(this.seer) != null && ((String)this.players.get(this.seer)).equals(username)) {
                        if (this.dead != null && this.dead.length > 0 && !this.dead[this.seer]) {
                           see = messageText.substring(messageText.indexOf(" ") + 1, messageText.length());
                           see = see.trim();
                           if (this.players.contains(see)) {
                              if (username.equals(see)) {
                                 this.sendMessage(this.createMessage("ALREADY_KNOW_HUMAN", username), username);
                              } else {
                                 for(i = 0; i < this.players.size(); ++i) {
                                    if (this.players.get(i) != null && ((String)this.players.get(i)).equalsIgnoreCase(see)) {
                                       this.toSee = i;
                                    }
                                 }

                                 this.sendMessage(this.createMessage("WILL-SEE", username, (String)this.players.get(this.toSee)), username);
                              }
                           }
                        } else {
                           this.sendMessage(this.createMessage("SEER-DEAD", username), username);
                        }
                     } else {
                        this.sendMessage(this.createMessage("NOT-SEER", username), username);
                     }
                  } catch (Exception var13) {
                     this.sendMessage(this.createMessage("VALID_PLAYER", username), username);
                     var13.printStackTrace();
                  }
               } else if (messageText.toLowerCase().startsWith("!v ")) {
                  this.sendMessage(this.createMessage("NIGHT_TIME_ONLY", username), username);
               }
            }

            if (messageText.toLowerCase().equalsIgnoreCase("!a")) {
               see = this.getPlayersAlive();
               this.sendMessage(this.createMessage("PLAYERS_ALIVE", see), username);
            }

            if (messageText.toLowerCase().equalsIgnoreCase("!r") && !this.gameStart) {
               for(i = 0; i < this.players.size(); ++i) {
                  if (username.equals((String)this.players.get(i))) {
                     if (this.wolf[i]) {
                        if (this.wolves.size() == 1) {
                           this.sendMessage(this.createMessage("W-ROLE", username), username);
                        } else {
                           for(i = 0; i < this.wolves.size(); ++i) {
                              if (!username.equals(this.wolves.get(i))) {
                                 this.sendMessage(this.createMessage("WS-ROLE", username, (String)this.wolves.get(i)), username);
                              }
                           }
                        }
                     } else if (i == this.seer) {
                        this.sendMessage(this.createMessage("S-ROLE", username), username);
                     } else {
                        this.sendMessage(this.createMessage("V-ROLE", username), username);
                     }
                  }
               }
            }
         }
      }

   }

   private String getPlayersAlive() {
      String names = "";

      for(int i = 0; i < this.players.size(); ++i) {
         if (this.dead != null && this.dead.length > 0 && !this.dead[i] && this.players.get(i) != null) {
            names = names + (String)this.players.get(i) + " ";
         }
      }

      return names;
   }

   private boolean isNameAdded(String name) {
      for(int i = 0; i < this.players.size(); ++i) {
         if (name.equals((String)this.players.get(i))) {
            return true;
         }
      }

      return false;
   }

   public void addPlayer(String username) {
      if (this.getGameState() == BotData.BotStateEnum.GAME_STARTED || this.getGameState() == BotData.BotStateEnum.GAME_JOINING) {
         synchronized(this.players) {
            if (!this.players.contains(username)) {
               this.players.add(username);
            }
         }

         StringBuilder message = new StringBuilder();
         message.append(this.createMessage("ADDED", username));
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
         message = this.createMessage("INVALID_COMMAND", username);
         break;
      default:
         message = this.createMessage("INVALID_COMMAND", username);
      }

      this.sendMessage(message, username);
   }

   public void startGame(String username) throws Exception {
      this.updateLastActivityTime();
      if (this.gameState.equals(BotData.BotStateEnum.NO_GAME)) {
         if (log.isDebugEnabled()) {
            log.debug("botInstanceID[" + this.getInstanceID() + "]: No charges. Game started by user[" + username + "]");
         }

         this.setGameState(BotData.BotStateEnum.GAME_STARTING);
         this.gameStarter = username;
         if (log.isDebugEnabled()) {
            log.debug("WerewolfBot: executing StartGame()");
         }

         this.executor.execute(new Werewolf.StartGame(this));
         if (log.isDebugEnabled()) {
            log.debug("WerewolfBot: executed for StartGame()");
         }
      } else {
         this.sendGameCannotStartMessage(username);
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

   public synchronized void onUserJoinChannel(String username) {
      String message = null;
      switch(this.getGameState().value()) {
      case 2:
         message = this.createMessage("GAME-STARTED");
         break;
      case 3:
         message = this.createMessage("GAME-STARTED");
         break;
      case 4:
      default:
         message = this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT");
         break;
      case 5:
         message = this.createMessage("STATUS-PLAYING");
      }

      this.sendMessage(message, username);
   }

   public synchronized void onUserLeaveChannel(String username) {
      if (this.playing) {
         int i;
         if (!this.gameStart) {
            for(i = 0; i < this.players.size(); ++i) {
               if (username.equals((String)this.players.get(i))) {
                  this.players.set(i, (Object)null);
                  if (this.dead != null && this.dead.length > 0 && !this.dead[i]) {
                     int j;
                     if (this.wolf[i]) {
                        for(j = 0; j < this.wolves.size(); ++j) {
                           if (((String)this.wolves.get(j)).equals(username)) {
                              this.wolves.remove(j);
                           }
                        }

                        this.sendChannelMessage(this.createMessage("FLEE-WOLF", username));
                     } else {
                        this.sendChannelMessage(this.createMessage("FLEE-VILLAGER", username));
                     }

                     this.dead[i] = true;
                     if (this.wolfVictim != null) {
                        for(j = 0; j < this.wolfVictim.size(); ++j) {
                           if (username.equals(this.wolfVictim.get(j))) {
                              this.wolfVictim.set(j, (Object)null);
                           }
                        }
                     }

                     this.checkWin();
                  }
               }
            }

            if (this.timeToVote) {
               for(i = 0; i < this.votes.size(); ++i) {
                  if (((Vote)this.votes.get(i)).getVote().equalsIgnoreCase(username)) {
                     this.votes.remove(i);
                  }
               }
            }
         } else if (this.gameStart) {
            for(i = 0; i < this.players.size(); ++i) {
               if (username.equals(this.players.get(i))) {
                  this.players.remove(i);
                  this.sendChannelMessage(this.createMessage("FLEE", username));
                  break;
               }
            }
         }
      }

   }

   public void endGame() {
      if (this.getGameState() == BotData.BotStateEnum.PLAYING) {
         this.sendChannelMessageAndPopUp(this.createMessage("GAME_OVER_FREE"));
         this.resetGame();
         this.updateLastActivityTime();
         this.sendChannelMessage(this.createMessage("GAME_STATE_DEFAULT_NO_AMOUNT"));
      }
   }

   private synchronized void updateLastActivityTime() {
      this.lastActivityTime = new Date();
   }

   void resetGame() {
      this.gameStarter = null;
      this.players.clear();
      this.priority.clear();
      this.setGameState(BotData.BotStateEnum.NO_GAME);
   }

   protected boolean hasVoted(String name) {
      for(int i = 0; i < this.players.size(); ++i) {
         if (name.equals((String)this.players.get(i))) {
            if (this.voted[i]) {
               return true;
            }

            return false;
         }
      }

      return false;
   }

   protected void tallyVotes() {
      this.sendChannelMessage(this.createMessage("TALLY"));

      int i;
      for(i = 0; i < this.players.size(); ++i) {
         if (this.players.get(i) != null) {
            int var10002;
            if (!this.voted[i]) {
               if (!this.dead[i]) {
                  var10002 = this.notVoted[i]++;
               }
            } else {
               Vote thisVote = null;

               int j;
               for(j = 0; j < this.votes.size(); ++j) {
                  if (((String)this.players.get(i)).equals(((Vote)this.votes.get(j)).getName())) {
                     thisVote = (Vote)this.votes.get(j);
                     break;
                  }
               }

               for(j = 0; j < this.players.size(); ++j) {
                  if (this.players.get(j) != null && thisVote.getVote() != null && thisVote.getVote().equalsIgnoreCase((String)this.players.get(j))) {
                     var10002 = this.wasVoted[j]++;
                  }
               }
            }
         }
      }

      i = 0;
      int guilty = true;
      Vector majIndexes = new Vector(1, 1);

      int rand;
      for(rand = 0; rand < this.wasVoted.length; ++rand) {
         if (this.wasVoted[rand] > i) {
            i = this.wasVoted[rand];
         }
      }

      for(rand = 0; rand < this.wasVoted.length; ++rand) {
         if (this.wasVoted[rand] == i) {
            majIndexes.add(new Integer(rand));
         }
      }

      int guilty;
      if (majIndexes.size() == 1) {
         guilty = Integer.parseInt(((Integer)majIndexes.get(0)).toString());
      } else if (this.tieGame && majIndexes != null && majIndexes.size() != 0) {
         rand = (int)(Math.random() * (double)majIndexes.size());
         if (this.wasVoted[(Integer)majIndexes.get(rand)] == 0) {
            guilty = -1;
         } else {
            guilty = (Integer)majIndexes.get(rand);
            this.sendChannelMessage(this.createMessage("TIE"));
         }
      } else {
         guilty = -10;
      }

      if (guilty == -10) {
         this.sendChannelMessage(this.createMessage("NO-LYNCH"));
      } else if (guilty != -1) {
         String guiltyStr = (String)this.players.get(guilty);
         this.dead[guilty] = true;
         if (guiltyStr == null) {
            this.sendChannelMessage(this.createMessage("LYNCH-LEFT"));
            return;
         }

         if (guilty == this.seer) {
            this.sendChannelMessage(this.createMessage("SEER-LYNCH", guiltyStr));
            this.role = (String)this.messages.get("ROLE-SEER");
         } else if (!this.wolf[guilty]) {
            this.sendChannelMessage(this.createMessage("VILLAGER-LYNCH", guiltyStr));
            this.role = (String)this.messages.get("ROLE-VILLAGER");
         } else {
            if (this.wolves.size() != 1) {
               for(int i = 0; i < this.wolves.size(); ++i) {
                  if (guiltyStr.equals((String)this.wolves.get(i))) {
                     this.wolves.remove(i);
                  }
               }
            }

            this.sendChannelMessage(this.createMessage("WOLF-LYNCH", guiltyStr));
            this.role = (String)this.messages.get("ROLE-WOLF");
         }

         this.sendChannelMessage(this.createMessage("IS-LYNCHED", guiltyStr));
         if (guilty != this.seer && guilty > -1 && !this.wolf[guilty]) {
            this.sendMessage(this.createMessage("DYING-BREATH", guiltyStr), guiltyStr);
         }
      } else {
         this.sendChannelMessage(this.createMessage("NO-VOTES"));
      }

   }

   protected void wolfKill() {
      String victim = "";
      if (this.wolfVictim.isEmpty()) {
         this.sendChannelMessage(this.createMessage("NO-KILL"));
      } else {
         int i;
         if (this.wolfVictim.size() == 1) {
            victim = (String)this.wolfVictim.get(0);
         } else if (this.wolfVictim.get(0).equals(this.wolfVictim.get(1))) {
            victim = (String)this.wolfVictim.get(0);
         } else {
            i = (int)(Math.random() * (double)this.wolfVictim.size());
            victim = (String)this.wolfVictim.get(i);
         }

         for(i = 0; i < this.players.size(); ++i) {
            if (this.players.get(i) != null && ((String)this.players.get(i)).equalsIgnoreCase(victim)) {
               if (this.players.get(i) != null) {
                  String deadName = (String)this.players.get(i);
                  this.dead[i] = true;
                  if (i == this.seer) {
                     this.sendChannelMessage(this.createMessage("SEER-KILL", deadName));
                     this.role = (String)this.messages.get("ROLE-SEER");
                  } else {
                     this.sendChannelMessage(this.createMessage("VILLAGER-KILL", deadName));
                     this.role = (String)this.messages.get("ROLE-VILLAGER");
                  }

                  this.sendChannelMessage(this.createMessage("IS-KILLED", deadName));
               } else {
                  for(int j = 0; j < this.wolves.size(); ++j) {
                     this.sendMessage(this.createMessage("WOLF_SELECTION_LEFT", (String)this.wolves.get(j)), (String)this.wolves.get(j));
                  }
               }
            }
         }

      }
   }

   protected void setRoles() {
      if (this.players.size() < 5) {
         this.sendChannelMessage(this.createMessage("NOT-ENOUGH"));
         this.playing = false;
      } else {
         int randWolf = (int)(Math.random() * (double)this.players.size());
         this.wolves.add(this.players.get(randWolf));
         this.wolf[randWolf] = true;
         if (this.players.size() < 8) {
            this.sendMessage(this.createMessage("WOLF-ROLE", (String)this.players.get(randWolf)), (String)this.players.get(randWolf));
         } else {
            boolean isWolf = true;

            while(isWolf) {
               randWolf = (int)(Math.random() * (double)this.players.size());
               if (!this.wolf[randWolf]) {
                  isWolf = false;
               }
            }

            this.wolves.add(this.players.get(randWolf));
            this.wolf[randWolf] = true;

            for(int i = 0; i < this.wolves.size(); ++i) {
               this.sendMessage(this.createMessage("WOLVES-ROLE", (String)this.wolves.get(i), (String)((String)(i == 0 ? this.wolves.get(1) : this.wolves.get(0)))), (String)this.wolves.get(i));
            }

            this.sendChannelMessage(this.createMessage("TWOWOLVES"));
         }

         boolean isWolf = true;

         while(isWolf) {
            this.seer = (int)(Math.random() * (double)this.players.size());
            if (!this.wolf[this.seer]) {
               isWolf = false;
            }
         }

         this.sendMessage(this.createMessage("SEER-ROLE", (String)this.players.get(this.seer)), (String)this.players.get(this.seer));

         for(int i = 0; i < this.players.size(); ++i) {
            try {
               if (i % 2 == 0) {
                  Thread.sleep(300L);
               }
            } catch (Exception var4) {
               var4.printStackTrace();
            }

            if (!this.wolf[i] && i != this.seer) {
               this.sendMessage(this.createMessage("VILLAGER-ROLE", (String)this.players.get(i)), (String)this.players.get(i));
            }
         }

      }
   }

   protected boolean checkWin() {
      int humanCount = 0;
      int wolfCount = 0;

      int i;
      for(i = 0; i < this.players.size(); ++i) {
         if (!this.wolf[i] && !this.dead[i] && this.players.get(i) != null) {
            ++humanCount;
         } else if (this.wolf[i] && !this.dead[i]) {
            ++wolfCount;
         }
      }

      if (wolfCount == 0) {
         this.playing = false;
         this.sendChannelMessage(this.createMessage("VILLAGERS-WIN"));
         this.sendChannelMessage(this.createMessage("CONGR-VILL"));
         this.day = false;

         for(i = 0; i < this.players.size(); ++i) {
            this.dead[i] = false;
         }

         this.winnerString = "Villagers each win";
         this.endGame();
         return true;
      } else if (wolfCount != humanCount) {
         return false;
      } else {
         this.playing = false;
         int i;
         String theWolves;
         if (this.players.size() < 8) {
            theWolves = "";

            for(i = 0; i < this.players.size(); ++i) {
               if (this.wolf[i]) {
                  theWolves = (String)this.players.get(i);
               }
            }

            this.sendChannelMessage(this.createMessage("WOLF-WIN", theWolves));
            this.sendChannelMessage(this.createMessage("CONGR-WOLF", theWolves));
            this.winnerString = "Wolf wins";
         } else {
            theWolves = (String)this.messages.get("WOLVES-WERE");

            for(i = 0; i < this.wolves.size(); ++i) {
               if (this.wolves.get(i) != null) {
                  theWolves = theWolves + (String)this.wolves.get(i) + " ";
               }
            }

            this.sendChannelMessage(this.createMessage("WOLVES-WIN"));
            this.sendChannelMessage(this.createMessage("CONGR-WOLVES"));
            this.sendChannelMessage(theWolves);
         }

         for(i = 0; i < this.players.size(); ++i) {
            this.dead[i] = false;
         }

         this.day = false;
         this.winnerString = wolfCount > 1 ? "Wolves each win" : "Wolf wins";
         this.endGame();
         return true;
      }
   }

   protected void playGame() {
      if (this.playing) {
         if (this.timeToVote) {
            for(int i = 0; i < this.players.size(); ++i) {
               if (!this.dead[i] && this.notVoted[i] == 2) {
                  this.dead[i] = true;
                  this.sendChannelMessage(this.createMessage("NOT-VOTED", (String)this.players.get(i)));
                  this.sendMessage(this.createMessage("NOT-VOTED-NOTICE", (String)this.players.get(i)), (String)this.players.get(i));
               }
            }

            if (this.checkWin()) {
               return;
            }

            this.sendChannelMessage(this.createMessage("VOTETIME", (String)null, (String)null, (int)this.voteTime, (String)null));
            if (log.isDebugEnabled()) {
               log.debug("WerewolfBot: starting timer for WereTask(): voting");
            }

            this.executor.schedule(new Werewolf.WereTask(this), this.voteTime, TimeUnit.SECONDS);
            if (log.isDebugEnabled()) {
               log.debug("WerewolfBot: started timer for WereTask(): voting");
            }
         } else if (this.day) {
            if (this.toSee != -1) {
               if (!this.dead[this.seer] && !this.dead[this.toSee]) {
                  if (this.wolf[this.toSee]) {
                     this.role = (String)this.messages.get("ROLE-WOLF");
                  } else {
                     this.role = (String)this.messages.get("ROLE-VILLAGER");
                  }

                  this.sendMessage(this.createMessage("SEER-SEE", (String)this.players.get(this.seer), (String)this.players.get(this.toSee)), (String)this.players.get(this.seer));
               } else if (this.dead[this.seer]) {
                  this.sendMessage(this.createMessage("SEER-SEE-KILLED", (String)this.players.get(this.seer), (String)this.players.get(this.toSee)), (String)this.players.get(this.seer));
               } else {
                  this.sendMessage(this.createMessage("SEER-SEE-TARGET-KILLED", (String)this.players.get(this.seer), (String)this.players.get(this.toSee)), (String)this.players.get(this.seer));
               }
            }

            this.sendChannelMessage(this.createMessage("DAYTIME", (String)null, (String)null, (int)this.dayTime, (String)null));
            if (log.isDebugEnabled()) {
               log.debug("WerewolfBot: starting timer for WereTask(): daytime");
            }

            this.executor.schedule(new Werewolf.WereTask(this), this.dayTime, TimeUnit.SECONDS);
            if (log.isDebugEnabled()) {
               log.debug("WerewolfBot: started timer for WereTask(): daytime");
            }
         } else if (!this.day) {
            if (this.firstNight) {
               this.firstNight = false;
               this.sendChannelMessage(this.createMessage("FIRSTNIGHT"));
            } else {
               this.sendChannelMessage(this.createMessage("NIGHTTIME"));
            }

            if (this.wolves.size() == 1) {
               this.sendChannelMessage(this.createMessage("WOLF-INSTRUCTIONS", (String)null, (String)null, (int)this.nightTime, (String)null));
            } else {
               this.sendChannelMessage(this.createMessage("WOLVES-INSTRUCTIONS", (String)null, (String)null, (int)this.nightTime, (String)null));
            }

            if (!this.dead[this.seer]) {
               this.sendChannelMessage(this.createMessage("SEER-INSTRUCTIONS", (String)null, (String)null, (int)this.nightTime, (String)null));
            }

            if (log.isDebugEnabled()) {
               log.debug("WerewolfBot: starting timer for WereTask(): nighttime");
            }

            this.executor.schedule(new Werewolf.WereTask(this), this.nightTime, TimeUnit.SECONDS);
            if (log.isDebugEnabled()) {
               log.debug("WerewolfBot: started timer for WereTask(): nighttime");
            }
         }
      }

   }

   private class WereTask implements Runnable {
      private Werewolf bot;

      WereTask(Werewolf bot) {
         this.bot = bot;
      }

      public void run() {
         synchronized(this.bot) {
            if (Werewolf.this.getGameState() == BotData.BotStateEnum.PLAYING) {
               if (Werewolf.this.day) {
                  Werewolf.this.day = !Werewolf.this.day;
                  Werewolf.this.timeToVote = true;
                  Werewolf.this.playGame();
               } else if (Werewolf.this.timeToVote) {
                  Werewolf.this.timeToVote = false;
                  Werewolf.this.tallyVotes();
                  Werewolf.this.votes = new Vector(1, 1);

                  int i;
                  for(i = 0; i < Werewolf.this.voted.length; ++i) {
                     Werewolf.this.voted[i] = false;
                  }

                  for(i = 0; i < Werewolf.this.wasVoted.length; ++i) {
                     Werewolf.this.wasVoted[i] = 0;
                  }

                  Werewolf.this.toSee = -1;
                  Werewolf.this.checkWin();
                  Werewolf.this.playGame();
               } else if (!Werewolf.this.day) {
                  Werewolf.this.wolfKill();
                  Werewolf.this.wolfVictim = new Vector(1, 1);
                  Werewolf.this.day = !Werewolf.this.day;
                  Werewolf.this.checkWin();
                  Werewolf.this.playGame();
               }
            }

         }
      }
   }

   class StartPlay implements Runnable {
      Werewolf bot;

      StartPlay(Werewolf bot) {
         this.bot = bot;
      }

      public void run() {
         try {
            synchronized(this.bot) {
               BotData.BotStateEnum gameState = this.bot.getGameState();
               if (gameState == BotData.BotStateEnum.GAME_JOINING) {
                  this.bot.setGameState(BotData.BotStateEnum.GAME_JOIN_ENDED);
                  if (this.bot.getPlayers().size() < this.bot.minPlayers) {
                     this.bot.sendChannelMessage(Werewolf.this.createMessage("JOIN_NO_MIN"));
                     if (Werewolf.log.isDebugEnabled()) {
                        Werewolf.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: Join ended. Not enough players.");
                     }

                     this.bot.setGameState(BotData.BotStateEnum.NO_GAME);
                  } else {
                     Set<String> copyOfPlayers = new HashSet();
                     copyOfPlayers.addAll(this.bot.players);
                     if (gameState != BotData.BotStateEnum.NO_GAME) {
                        gameState = BotData.BotStateEnum.PLAYING;

                        try {
                           Werewolf.log.info("New game started in " + Werewolf.this.channel);
                           Werewolf.this.setGameState(BotData.BotStateEnum.PLAYING);
                           if (Werewolf.this.gameStart) {
                              this.initializePlay();
                              Werewolf.this.sendChannelMessage(Werewolf.this.createMessage("JOIN_ENDED"));
                              Werewolf.this.setRoles();
                              if (Werewolf.this.players.size() >= 5) {
                                 Werewolf.this.day = true;
                              }

                              Werewolf.this.playGame();
                           }
                        } catch (Exception var6) {
                           Werewolf.log.error("Error creating pot for botInstanceID[" + Werewolf.this.getInstanceID() + "].", var6);
                           Werewolf.this.setGameState(BotData.BotStateEnum.NO_GAME);
                           Werewolf.this.sendChannelMessage(Werewolf.this.createMessage("GAME_CANCELED"));
                        }
                     } else {
                        if (Werewolf.log.isDebugEnabled()) {
                           Werewolf.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: Billing error. Game canceled. No charges.");
                        }

                        Werewolf.this.resetGame();
                     }
                  }
               }
            }
         } catch (Exception var8) {
            Werewolf.log.error("Unexpected exception caught in StartPlay.run()", var8);
            Werewolf.this.resetGame();
         }

      }

      private void initializePlay() {
         Werewolf.this.gameStart = false;
         Werewolf.this.wolfVictim = new Vector(1, 1);
         Werewolf.this.votes = new Vector(1, 1);
         Werewolf.this.voted = new boolean[Werewolf.this.players.size()];
         Werewolf.this.wolf = new boolean[Werewolf.this.players.size()];
         Werewolf.this.dead = new boolean[Werewolf.this.players.size()];
         Werewolf.this.notVoted = new int[Werewolf.this.players.size()];
         Werewolf.this.wasVoted = new int[Werewolf.this.players.size()];

         for(int i = 0; i < Werewolf.this.players.size(); ++i) {
            Werewolf.this.voted[i] = false;
            Werewolf.this.wolf[i] = false;
            Werewolf.this.dead[i] = false;
            Werewolf.this.notVoted[i] = 0;
            Werewolf.this.wasVoted[i] = 0;
         }

      }
   }

   class StartGame implements Runnable {
      Werewolf bot;

      StartGame(Werewolf bot) {
         this.bot = bot;
      }

      public void run() {
         synchronized(this.bot) {
            if (Werewolf.log.isDebugEnabled()) {
               Werewolf.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: in StartGame() ");
            }

            BotData.BotStateEnum gameState = null;
            gameState = this.bot.getGameState();
            if (gameState == BotData.BotStateEnum.GAME_STARTING) {
               this.bot.setGameState(BotData.BotStateEnum.GAME_STARTED);
               Werewolf.this.players = new ArrayList(5);
               this.bot.addPlayer(this.bot.gameStarter);
               Werewolf.this.priority = new Vector(1, 1);
               Werewolf.this.wolves = new Vector(1, 1);
               Werewolf.this.playing = true;
               Werewolf.this.day = false;
               Werewolf.this.timeToVote = false;
               Werewolf.this.gameStart = true;
               Werewolf.this.firstDay = true;
               Werewolf.this.firstNight = true;
               Werewolf.this.toSee = -1;
               String messageKey = "STARTGAME_FREE";
               Werewolf.this.sendChannelMessage(Werewolf.this.createMessage(messageKey, this.bot.gameStarter, (String)null, (int)Werewolf.this.timeToJoinGame, (String)null));
               if (Werewolf.this.players.contains(Werewolf.this.gameStarter)) {
                  Werewolf.this.sendChannelMessage(Werewolf.this.createMessage("JOIN", Werewolf.this.gameStarter));
               }

               if (this.bot.timeToJoinGame > 0L) {
                  this.bot.setGameState(BotData.BotStateEnum.GAME_JOINING);
                  if (Werewolf.log.isDebugEnabled()) {
                     Werewolf.log.debug("WerewolfBot: starting timer for StartPlay()");
                  }

                  Werewolf.this.executor.schedule(Werewolf.this.new StartPlay(this.bot), this.bot.timeToJoinGame, TimeUnit.SECONDS);
                  if (Werewolf.log.isDebugEnabled()) {
                     Werewolf.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: scheduled to start play. Awaiting join.. ");
                  }
               }
            }

         }
      }
   }
}
