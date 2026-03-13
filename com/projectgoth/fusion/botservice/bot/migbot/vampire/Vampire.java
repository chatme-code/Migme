package com.projectgoth.fusion.botservice.bot.migbot.vampire;

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

public class Vampire extends Bot {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Vampire.class));
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
   Vector vampires;
   Vector vampireVictim;
   final int JOINTIME;
   final int MINPLAYERS;
   final int MAXPLAYERS;
   final int TWOVAMPIRES;
   long dayTime;
   long nightTime;
   long voteTime;
   int slayer;
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
   boolean[] vampire;
   boolean[] dead;
   boolean[] voted;
   String role;
   String oneVampire;
   String manyVampires;
   String winnerString;

   public Vampire(ScheduledExecutorService executor, BotChannelPrx channelProxy, BotData botData, String languageCode, String botStarter, BotDAO botDao) {
      super(executor, channelProxy, botData, languageCode, botStarter, botDao);
      this.gameState = BotData.BotStateEnum.NO_GAME;
      this.timeToJoinGame = 90L;
      this.minPlayers = 5;
      this.maxPlayers = 12;
      this.timeAllowedToIdle = 30L;
      this.JOINTIME = 90;
      this.MINPLAYERS = 5;
      this.MAXPLAYERS = 12;
      this.TWOVAMPIRES = 8;
      this.dayTime = 90L;
      this.nightTime = 60L;
      this.voteTime = 30L;
      this.toSee = -1;
      this.playing = false;
      this.day = false;
      this.gameStart = false;
      this.tieGame = true;
      this.loadGameConfig();
      log.info("VampireBot [" + this.instanceID + "] added to channel [" + this.channel + "]");
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
      this.oneVampire = (String)this.messages.get("1-VAMPIRE");
      this.manyVampires = (String)this.messages.get("MANY-VAMPIRES");
      this.timeAllowedToIdle = this.getLongParameter("timerIdle", 5L);
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
            messageToSend = messageToSend.replaceAll("ISAVAMPIRE?", this.role);
            messageToSend = messageToSend.replaceAll("ROLE", this.role);
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
                     boolean isVampire = false;
                     i = -1;

                     int otherVampire;
                     for(otherVampire = 0; otherVampire < this.vampires.size(); ++otherVampire) {
                        if (this.vampires.get(otherVampire).equals(username)) {
                           isVampire = true;
                           i = otherVampire;
                           break;
                        }
                     }

                     if (isVampire) {
                        otherVampire = -1;
                        if (this.vampires.size() > 1) {
                           otherVampire = i == 0 ? 1 : 0;
                        }

                        try {
                           String victim = messageText.substring(messageText.indexOf(" ") + 1, messageText.length());
                           victim = victim.trim();
                           if (this.players.contains(victim)) {
                              if (otherVampire != -1 && victim.equals(this.vampires.get(otherVampire))) {
                                 if (log.isDebugEnabled()) {
                                    log.debug("Invalid: " + username + " tried to kill the other vampire " + victim);
                                 }

                                 this.sendMessage(this.createMessage("CANT_KILL_OTHER_VAMPIRE", username, victim), username);
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
                                       if (this.vampireVictim.add(victim)) {
                                          if (this.vampires.size() == 1) {
                                             this.sendMessage(this.createMessage("VAMPIRE-CHOICE", username, victim), username);
                                          } else {
                                             this.sendMessage(this.createMessage("VAMPIRES-CHOICE", username, victim), username);

                                             for(i = 0; i < this.vampires.size(); ++i) {
                                                if (!((String)this.vampires.get(i)).equals(username)) {
                                                   this.sendMessage(this.createMessage("VAMPIRES-CHOICE-OTHER" + (i + 1), username, victim), (String)this.vampires.get(i));
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
                        this.sendMessage(this.createMessage("NOT-VAMPIRE", username), username);
                     }
                  } else {
                     this.sendMessage(this.createMessage("NOT_PLAYING", username), username);
                  }
               }

               if (messageText.toLowerCase().startsWith("!s ")) {
                  try {
                     if (!this.players.contains(username)) {
                        this.sendMessage(this.createMessage("INVALID_CHOICE", username), username);
                     } else if (this.players.get(this.slayer) != null && ((String)this.players.get(this.slayer)).equals(username)) {
                        if (this.dead != null && this.dead.length > 0 && !this.dead[this.slayer]) {
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
                           this.sendMessage(this.createMessage("SLAYER-DEAD", username), username);
                        }
                     } else {
                        this.sendMessage(this.createMessage("NOT-SLAYER", username), username);
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
                     if (this.vampire[i]) {
                        if (this.vampires.size() == 1) {
                           this.sendMessage(this.createMessage("V-ROLE", username), username);
                        } else {
                           for(i = 0; i < this.vampires.size(); ++i) {
                              if (!username.equals(this.vampires.get(i))) {
                                 this.sendMessage(this.createMessage("VS-ROLE", username, (String)this.vampires.get(i)), username);
                              }
                           }
                        }
                     } else if (i == this.slayer) {
                        this.sendMessage(this.createMessage("S-ROLE", username), username);
                     } else {
                        this.sendMessage(this.createMessage("E-ROLE", username), username);
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

   public List<String> getPlayers() {
      return this.players;
   }

   public void addPlayer(String username) {
      if (this.getGameState() == BotData.BotStateEnum.GAME_STARTED || this.getGameState() == BotData.BotStateEnum.GAME_JOINING) {
         synchronized(this.players) {
            if (!this.players.contains(username)) {
               this.players.add(username);
            }
         }

         StringBuilder message = new StringBuilder();
         message.append(this.createMessage("ADDED_TO_GAME", username));
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
            log.debug("VampireBot: executing StartGame()");
         }

         this.executor.execute(new Vampire.StartGame(this));
         if (log.isDebugEnabled()) {
            log.debug("VampireBot: executed for StartGame()");
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
         message = this.createMessage("STATUS-JOINING", username);
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
                     if (this.vampire[i]) {
                        for(j = 0; j < this.vampires.size(); ++j) {
                           if (((String)this.vampires.get(j)).equals(username)) {
                              this.vampires.remove(j);
                           }
                        }

                        this.sendChannelMessage(this.createMessage("FLEE-VAMPIRE", username));
                     } else {
                        this.sendChannelMessage(this.createMessage("FLEE-EXPLORER", username));
                     }

                     this.dead[i] = true;
                     if (this.vampireVictim != null) {
                        for(j = 0; j < this.vampireVictim.size(); ++j) {
                           if (username.equals(this.vampireVictim.get(j))) {
                              this.vampireVictim.set(j, (Object)null);
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

         if (guilty == this.slayer) {
            this.sendChannelMessage(this.createMessage("SLAYER-LYNCH", guiltyStr));
            this.role = (String)this.messages.get("ROLE-SLAYER");
         } else if (!this.vampire[guilty]) {
            this.sendChannelMessage(this.createMessage("EXPLORER-LYNCH", guiltyStr));
            this.role = (String)this.messages.get("ROLE-EXPLORER");
         } else {
            if (this.vampires.size() != 1) {
               for(int i = 0; i < this.vampires.size(); ++i) {
                  if (guiltyStr.equals((String)this.vampires.get(i))) {
                     this.vampires.remove(i);
                  }
               }
            }

            this.sendChannelMessage(this.createMessage("VAMPIRE-LYNCH", guiltyStr));
            this.role = (String)this.messages.get("ROLE-VAMPIRE");
         }

         this.sendChannelMessage(this.createMessage("IS-LYNCHED", guiltyStr));
         if (guilty != this.slayer && guilty > -1 && !this.vampire[guilty]) {
            this.sendMessage(this.createMessage("DYING-BREATH", guiltyStr), guiltyStr);
         }
      } else {
         this.sendChannelMessage(this.createMessage("NO-VOTES"));
      }

   }

   protected void vampireKill() {
      String victim = "";
      if (this.vampireVictim.isEmpty()) {
         this.sendChannelMessage(this.createMessage("NO-KILL"));
      } else {
         int i;
         if (this.vampireVictim.size() == 1) {
            victim = (String)this.vampireVictim.get(0);
         } else if (this.vampireVictim.get(0).equals(this.vampireVictim.get(1))) {
            victim = (String)this.vampireVictim.get(0);
         } else {
            i = (int)(Math.random() * (double)this.vampireVictim.size());
            victim = (String)this.vampireVictim.get(i);
         }

         for(i = 0; i < this.players.size(); ++i) {
            if (this.players.get(i) != null && ((String)this.players.get(i)).equalsIgnoreCase(victim)) {
               if (this.players.get(i) != null) {
                  String deadName = (String)this.players.get(i);
                  this.dead[i] = true;
                  if (i == this.slayer) {
                     this.sendChannelMessage(this.createMessage("SLAYER-KILL", deadName));
                     this.role = (String)this.messages.get("ROLE-SLAYER");
                  } else {
                     this.sendChannelMessage(this.createMessage("EXPLORER-KILL", deadName));
                     this.role = (String)this.messages.get("ROLE-EXPLORER");
                  }

                  this.sendChannelMessage(this.createMessage("IS-KILLED", deadName));
               } else {
                  for(int j = 0; j < this.vampires.size(); ++j) {
                     this.sendMessage(this.createMessage("VAMPIRE_SELECTION_LEFT", (String)this.vampires.get(j)), (String)this.vampires.get(j));
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
         int randVampire = (int)(Math.random() * (double)this.players.size());
         this.vampires.add(this.players.get(randVampire));
         this.vampire[randVampire] = true;
         if (this.players.size() < 8) {
            this.sendMessage(this.createMessage("VAMPIRE-ROLE", (String)this.players.get(randVampire)), (String)this.players.get(randVampire));
         } else {
            boolean isVampire = true;

            while(isVampire) {
               randVampire = (int)(Math.random() * (double)this.players.size());
               if (!this.vampire[randVampire]) {
                  isVampire = false;
               }
            }

            this.vampires.add(this.players.get(randVampire));
            this.vampire[randVampire] = true;

            for(int i = 0; i < this.vampires.size(); ++i) {
               this.sendMessage(this.createMessage("VAMPIRES-ROLE", (String)this.vampires.get(i), (String)((String)(i == 0 ? this.vampires.get(1) : this.vampires.get(0)))), (String)this.vampires.get(i));
            }

            this.sendChannelMessage(this.createMessage("TWOVAMPIRES"));
         }

         boolean isVampire = true;

         while(isVampire) {
            this.slayer = (int)(Math.random() * (double)this.players.size());
            if (!this.vampire[this.slayer]) {
               isVampire = false;
            }
         }

         this.sendMessage(this.createMessage("SLAYER-ROLE", (String)this.players.get(this.slayer)), (String)this.players.get(this.slayer));

         for(int i = 0; i < this.players.size(); ++i) {
            try {
               if (i % 2 == 0) {
                  Thread.sleep(300L);
               }
            } catch (Exception var4) {
               var4.printStackTrace();
            }

            if (!this.vampire[i] && i != this.slayer) {
               this.sendMessage(this.createMessage("EXPLORER-ROLE", (String)this.players.get(i)), (String)this.players.get(i));
            }
         }

      }
   }

   protected boolean checkWin() {
      int humanCount = 0;
      int vampireCount = 0;

      int i;
      for(i = 0; i < this.players.size(); ++i) {
         if (!this.vampire[i] && !this.dead[i] && this.players.get(i) != null) {
            ++humanCount;
         } else if (this.vampire[i] && !this.dead[i]) {
            ++vampireCount;
         }
      }

      if (vampireCount == 0) {
         this.playing = false;
         this.sendChannelMessage(this.createMessage("EXPLORERS-WIN"));
         this.sendChannelMessage(this.createMessage("CONGR-VILL"));
         this.day = false;

         for(i = 0; i < this.players.size(); ++i) {
            this.dead[i] = false;
         }

         this.winnerString = "Explorers each win";
         this.endGame();
         return true;
      } else if (vampireCount != humanCount) {
         return false;
      } else {
         this.playing = false;
         int i;
         String theVampires;
         if (this.players.size() < 8) {
            theVampires = "";

            for(i = 0; i < this.players.size(); ++i) {
               if (this.vampire[i]) {
                  theVampires = (String)this.players.get(i);
               }
            }

            this.sendChannelMessage(this.createMessage("VAMPIRE-WIN", theVampires));
            this.sendChannelMessage(this.createMessage("CONGR-VAMPIRE", theVampires));
            this.winnerString = "Vampire wins";
         } else {
            theVampires = (String)this.messages.get("VAMPIRES-WERE");

            for(i = 0; i < this.vampires.size(); ++i) {
               if (this.vampires.get(i) != null) {
                  theVampires = theVampires + (String)this.vampires.get(i) + " ";
               }
            }

            this.sendChannelMessage(this.createMessage("VAMPIRES-WIN"));
            this.sendChannelMessage(this.createMessage("CONGR-VAMPIRES"));
            this.sendChannelMessage(theVampires);
         }

         for(i = 0; i < this.players.size(); ++i) {
            this.dead[i] = false;
         }

         this.day = false;
         this.winnerString = vampireCount > 1 ? "Vampires each win" : "Vampire wins";
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
               log.debug("VampireBot: starting timer for WereTask(): voting");
            }

            this.executor.schedule(new Vampire.VampireTask(this), this.voteTime, TimeUnit.SECONDS);
            if (log.isDebugEnabled()) {
               log.debug("VampireBot: started timer for WereTask(): voting");
            }
         } else if (this.day) {
            if (this.toSee != -1) {
               if (!this.dead[this.slayer] && !this.dead[this.toSee]) {
                  if (this.vampire[this.toSee]) {
                     this.role = (String)this.messages.get("ROLE-VAMPIRE");
                  } else {
                     this.role = (String)this.messages.get("ROLE-EXPLORER");
                  }

                  this.sendMessage(this.createMessage("SLAYER-SEE", (String)this.players.get(this.slayer), (String)this.players.get(this.toSee)), (String)this.players.get(this.slayer));
               } else if (this.dead[this.slayer]) {
                  this.sendMessage(this.createMessage("SLAYER-SEE-KILLED", (String)this.players.get(this.slayer), (String)this.players.get(this.toSee)), (String)this.players.get(this.slayer));
               } else {
                  this.sendMessage(this.createMessage("SLAYER-SEE-TARGET-KILLED", (String)this.players.get(this.slayer), (String)this.players.get(this.toSee)), (String)this.players.get(this.slayer));
               }
            }

            this.sendChannelMessage(this.createMessage("DAYTIME", (String)null, (String)null, (int)this.dayTime, (String)null));
            if (log.isDebugEnabled()) {
               log.debug("VampireBot: starting timer for WereTask(): daytime");
            }

            this.executor.schedule(new Vampire.VampireTask(this), this.dayTime, TimeUnit.SECONDS);
            if (log.isDebugEnabled()) {
               log.debug("VampireBot: started timer for WereTask(): daytime");
            }
         } else if (!this.day) {
            if (this.firstNight) {
               this.firstNight = false;
               this.sendChannelMessage(this.createMessage("FIRSTNIGHT"));
            } else {
               this.sendChannelMessage(this.createMessage("NIGHTTIME"));
            }

            if (this.vampires.size() == 1) {
               this.sendChannelMessage(this.createMessage("VAMPIRE-INSTRUCTIONS", (String)null, (String)null, (int)this.nightTime, (String)null));
            } else {
               this.sendChannelMessage(this.createMessage("VAMPIRES-INSTRUCTIONS", (String)null, (String)null, (int)this.nightTime, (String)null));
            }

            if (!this.dead[this.slayer]) {
               this.sendChannelMessage(this.createMessage("SLAYER-INSTRUCTIONS", (String)null, (String)null, (int)this.nightTime, (String)null));
            }

            if (log.isDebugEnabled()) {
               log.debug("VampireBot: starting timer for WereTask(): nighttime");
            }

            this.executor.schedule(new Vampire.VampireTask(this), this.nightTime, TimeUnit.SECONDS);
            if (log.isDebugEnabled()) {
               log.debug("VampireBot: started timer for WereTask(): nighttime");
            }
         }
      }

   }

   private class VampireTask implements Runnable {
      private Vampire bot;

      VampireTask(Vampire bot) {
         this.bot = bot;
      }

      public void run() {
         synchronized(this.bot) {
            if (Vampire.this.getGameState() == BotData.BotStateEnum.PLAYING) {
               if (Vampire.this.day) {
                  Vampire.this.day = !Vampire.this.day;
                  Vampire.this.timeToVote = true;
                  Vampire.this.playGame();
               } else if (Vampire.this.timeToVote) {
                  Vampire.this.timeToVote = false;
                  Vampire.this.tallyVotes();
                  Vampire.this.votes = new Vector(1, 1);

                  int i;
                  for(i = 0; i < Vampire.this.voted.length; ++i) {
                     Vampire.this.voted[i] = false;
                  }

                  for(i = 0; i < Vampire.this.wasVoted.length; ++i) {
                     Vampire.this.wasVoted[i] = 0;
                  }

                  Vampire.this.toSee = -1;
                  Vampire.this.checkWin();
                  Vampire.this.playGame();
               } else if (!Vampire.this.day) {
                  Vampire.this.vampireKill();
                  Vampire.this.vampireVictim = new Vector(1, 1);
                  Vampire.this.day = !Vampire.this.day;
                  Vampire.this.checkWin();
                  Vampire.this.playGame();
               }
            }

         }
      }
   }

   class StartPlay implements Runnable {
      Vampire bot;

      StartPlay(Vampire bot) {
         this.bot = bot;
      }

      public void run() {
         try {
            synchronized(this.bot) {
               BotData.BotStateEnum gameState = this.bot.getGameState();
               if (gameState == BotData.BotStateEnum.GAME_JOINING) {
                  this.bot.setGameState(BotData.BotStateEnum.GAME_JOIN_ENDED);
                  if (this.bot.getPlayers().size() < this.bot.minPlayers) {
                     this.bot.sendChannelMessage(Vampire.this.createMessage("JOIN_NO_MIN"));
                     if (Vampire.log.isDebugEnabled()) {
                        Vampire.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: Join ended. Not enough players.");
                     }

                     this.bot.setGameState(BotData.BotStateEnum.NO_GAME);
                  } else {
                     Set<String> copyOfPlayers = new HashSet();
                     copyOfPlayers.addAll(this.bot.players);
                     if (gameState != BotData.BotStateEnum.NO_GAME) {
                        gameState = BotData.BotStateEnum.PLAYING;

                        try {
                           Vampire.log.info("New game started in " + Vampire.this.channel);
                           Vampire.this.setGameState(BotData.BotStateEnum.PLAYING);
                           if (Vampire.this.gameStart) {
                              this.initializePlay();
                              Vampire.this.sendChannelMessage(Vampire.this.createMessage("JOIN_ENDED"));
                              Vampire.this.setRoles();
                              if (Vampire.this.players.size() >= 5) {
                                 Vampire.this.day = true;
                              }

                              Vampire.this.playGame();
                           }
                        } catch (Exception var6) {
                           Vampire.log.error("Error creating pot for botInstanceID[" + Vampire.this.getInstanceID() + "].", var6);
                           Vampire.this.setGameState(BotData.BotStateEnum.NO_GAME);
                           Vampire.this.sendChannelMessage(Vampire.this.createMessage("GAME_CANCELED"));
                        }
                     } else {
                        if (Vampire.log.isDebugEnabled()) {
                           Vampire.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: Billing error. Game canceled. No charges.");
                        }

                        Vampire.this.resetGame();
                     }
                  }
               }
            }
         } catch (Exception var8) {
            Vampire.log.error("Unexpected exception caught in StartPlay.run()", var8);
            Vampire.this.resetGame();
         }

      }

      private void initializePlay() {
         Vampire.this.gameStart = false;
         Vampire.this.vampireVictim = new Vector(1, 1);
         Vampire.this.votes = new Vector(1, 1);
         Vampire.this.voted = new boolean[Vampire.this.players.size()];
         Vampire.this.vampire = new boolean[Vampire.this.players.size()];
         Vampire.this.dead = new boolean[Vampire.this.players.size()];
         Vampire.this.notVoted = new int[Vampire.this.players.size()];
         Vampire.this.wasVoted = new int[Vampire.this.players.size()];

         for(int i = 0; i < Vampire.this.players.size(); ++i) {
            Vampire.this.voted[i] = false;
            Vampire.this.vampire[i] = false;
            Vampire.this.dead[i] = false;
            Vampire.this.notVoted[i] = 0;
            Vampire.this.wasVoted[i] = 0;
         }

      }
   }

   class StartGame implements Runnable {
      Vampire bot;

      StartGame(Vampire bot) {
         this.bot = bot;
      }

      public void run() {
         synchronized(this.bot) {
            if (Vampire.log.isDebugEnabled()) {
               Vampire.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: in StartGame() ");
            }

            BotData.BotStateEnum gameState = null;
            gameState = this.bot.getGameState();
            if (gameState == BotData.BotStateEnum.GAME_STARTING) {
               this.bot.setGameState(BotData.BotStateEnum.GAME_STARTED);
               Vampire.this.players = new ArrayList(5);
               this.bot.addPlayer(this.bot.gameStarter);
               Vampire.this.priority = new Vector(1, 1);
               Vampire.this.vampires = new Vector(1, 1);
               Vampire.this.playing = true;
               Vampire.this.day = false;
               Vampire.this.timeToVote = false;
               Vampire.this.gameStart = true;
               Vampire.this.firstDay = true;
               Vampire.this.firstNight = true;
               Vampire.this.toSee = -1;
               String messageKey = "STARTGAME_FREE";
               Vampire.this.sendChannelMessage(Vampire.this.createMessage(messageKey, this.bot.gameStarter, (String)null, (int)Vampire.this.timeToJoinGame, (String)null));
               if (Vampire.this.players.contains(Vampire.this.gameStarter)) {
                  Vampire.this.sendChannelMessage(Vampire.this.createMessage("JOIN", Vampire.this.gameStarter));
               }

               if (this.bot.timeToJoinGame > 0L) {
                  this.bot.setGameState(BotData.BotStateEnum.GAME_JOINING);
                  if (Vampire.log.isDebugEnabled()) {
                     Vampire.log.debug("VampireBot: starting timer for StartPlay()");
                  }

                  Vampire.this.executor.schedule(Vampire.this.new StartPlay(this.bot), this.bot.timeToJoinGame, TimeUnit.SECONDS);
                  if (Vampire.log.isDebugEnabled()) {
                     Vampire.log.debug("botInstanceID[" + this.bot.getInstanceID() + "]: scheduled to start play. Awaiting join.. ");
                  }
               }
            }

         }
      }
   }
}
