package com.projectgoth.fusion.botservice.bot.migbot.warriors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

class Player implements Comparable<Player> {
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Player.class));
   public static final int MAX_CONSECUTIVE_GAME_FOR_MODE_CHANGE = 3;
   public static final int MAX_CONSECUTIVE_GAME_IN_MODE_CHANGE = 3;
   boolean isChallenger = false;
   boolean hasAcceptedChallenge = false;
   Player.ModeEnum mode;
   String username;
   UserData userData;
   int migLevel;
   int HP;
   int maxHP;
   double potEntry;
   int gamesPlayed = -1;
   Player.MilestoneEnum lastMilestone;
   int sequence;
   boolean isInChannel;
   private List<Attack> normalAttacks;
   private List<Attack> berserkAttacks;

   Player(String username, List<Attack> normalAttacks, List<Attack> berserkAttacks) {
      this.mode = Player.ModeEnum.NORMAL;
      this.username = username;
      this.normalAttacks = normalAttacks;
      this.berserkAttacks = berserkAttacks;
      this.lastMilestone = Player.MilestoneEnum.NONE;
      this.userData = null;
      this.migLevel = 0;
      this.HP = 0;
      this.maxHP = 0;
      this.potEntry = 0.0D;
      this.sequence = 0;
      this.isInChannel = true;
   }

   public synchronized void setInChannel(boolean flag) {
      this.isInChannel = flag;
   }

   public synchronized boolean isInChannel() {
      return this.isInChannel;
   }

   public boolean isFever() {
      return this.mode == Player.ModeEnum.FEVER;
   }

   public boolean isBerserk() {
      return this.mode == Player.ModeEnum.BERSERK;
   }

   public void changeMode(Player.ModeEnum newMode) {
      this.mode = newMode;
   }

   public String getPlayerEmote() {
      switch(this.mode) {
      case NORMAL:
         return String.format("(warriors-num-n-%d)", this.sequence);
      case FEVER:
         return String.format("(warriors-num-d-%d)", this.sequence);
      case BERSERK:
         return String.format("(warriors-num-a-%d)", this.sequence);
      default:
         return " ";
      }
   }

   public Attack getAttack() {
      List attackList;
      if (this.isBerserk()) {
         attackList = this.berserkAttacks;
      } else {
         attackList = this.normalAttacks;
      }

      double random = Math.random();
      double probability_sum = 0.0D;
      Iterator i$ = attackList.iterator();

      Attack att;
      do {
         if (!i$.hasNext()) {
            return (Attack)attackList.get(0);
         }

         att = (Attack)i$.next();
         probability_sum += att.probability;
      } while(!(random <= probability_sum));

      return att;
   }

   public int compareTo(Player player2) {
      if (this.HP == player2.HP) {
         if (this.userData.broadcastList.size() != player2.userData.broadcastList.size()) {
            return this.userData.broadcastList.size() - player2.userData.broadcastList.size();
         } else {
            if (this.gamesPlayed == -1 || player2.gamesPlayed == -1) {
               List<String> usernames = new ArrayList(2);
               List<Integer> userids = new ArrayList(2);
               if (this.gamesPlayed == -1) {
                  usernames.add(this.username);
                  userids.add(this.userData.userID);
               }

               if (player2.gamesPlayed == -1) {
                  usernames.add(player2.username);
                  userids.add(player2.userData.userID);
               }

               try {
                  List<Integer> gamesPlayedStats = Leaderboard.getGamesMetric(Leaderboard.Type.WARRIORS_GAMES_PLAYED, Leaderboard.Period.WEEKLY, usernames, userids);
                  int index = 0;
                  if (this.gamesPlayed == -1) {
                     this.gamesPlayed = (Integer)gamesPlayedStats.get(index);
                     ++index;
                  }

                  if (player2.gamesPlayed == -1) {
                     player2.gamesPlayed = (Integer)gamesPlayedStats.get(index);
                  }
               } catch (Exception var6) {
                  log.warn(String.format("Failed to get weekly games played for Warriors to compare players [%s] [%s]", this.username, player2.username));
                  return Math.random() > 0.5D ? -1 : 1;
               }
            }

            if (this.gamesPlayed == player2.gamesPlayed) {
               return Math.random() > 0.5D ? -1 : 1;
            } else {
               return this.gamesPlayed - player2.gamesPlayed;
            }
         }
      } else {
         return this.HP - player2.HP;
      }
   }

   public String getMilestoneMessageKey() {
      String messageKey = null;
      if (this.HP < 10) {
         if (this.lastMilestone != Player.MilestoneEnum.D10) {
            messageKey = "MILESTONE_D10";
            this.lastMilestone = Player.MilestoneEnum.D10;
         }
      } else if ((double)this.HP <= 0.25D * (double)this.maxHP) {
         if (this.lastMilestone != Player.MilestoneEnum.P25) {
            messageKey = "MILESTONE_P25";
            this.lastMilestone = Player.MilestoneEnum.P25;
         }
      } else if ((double)this.HP <= 0.5D * (double)this.maxHP) {
         if (this.lastMilestone != Player.MilestoneEnum.P50) {
            messageKey = "MILESTONE_P50";
            this.lastMilestone = Player.MilestoneEnum.P50;
         }
      } else if ((double)this.HP <= 0.75D * (double)this.maxHP && this.lastMilestone != Player.MilestoneEnum.P75) {
         messageKey = "MILESTONE_P75";
         this.lastMilestone = Player.MilestoneEnum.P75;
      }

      return messageKey;
   }

   public static boolean isUserInMode(String username, Player.ModeEnum modeToCheck) {
      MemCachedKeySpaces.CommonKeySpace keySpace = modeToCheck == Player.ModeEnum.FEVER ? MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_FEVER : MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_BERSERK;
      long count = MemCachedClientWrapper.getCounter(keySpace, username);
      return count >= 3L && count < 6L;
   }

   public static Player.ModeEnum getMode(String username) {
      if (isUserInMode(username, Player.ModeEnum.BERSERK)) {
         if (log.isDebugEnabled()) {
            log.debug(String.format("username '%s' in mode '%s'", username, "BERSERK"));
         }

         return Player.ModeEnum.BERSERK;
      } else if (isUserInMode(username, Player.ModeEnum.FEVER)) {
         if (log.isDebugEnabled()) {
            log.debug(String.format("username '%s' in mode '%s'", username, "FEVER"));
         }

         return Player.ModeEnum.FEVER;
      } else {
         if (log.isDebugEnabled()) {
            log.debug(String.format("username '%s' in mode '%s'", username, "NORMAL"));
         }

         return Player.ModeEnum.NORMAL;
      }
   }

   public String getModeDisplay() {
      switch(this.mode) {
      case NORMAL:
      default:
         return "";
      case FEVER:
         return "(Fever)";
      case BERSERK:
         return "(Berserk)";
      }
   }

   private Player.ModeEnum checkAndUpdateMode(Player.ModeEnum modeToCheck) {
      MemCachedKeySpaces.CommonKeySpace keySpace = modeToCheck == Player.ModeEnum.FEVER ? MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_FEVER : MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_BERSERK;
      long count = MemCachedClientWrapper.getCounter(keySpace, this.username);
      if (count >= 3L) {
         if (count == 3L) {
            MemCachedClientWrapper.set(keySpace, this.username, 4L);
         } else {
            if (count >= 6L) {
               MemCachedClientWrapper.delete(keySpace, this.username);
               return Player.ModeEnum.NORMAL;
            }

            MemCachedClientWrapper.incr(keySpace, this.username);
         }

         return modeToCheck;
      } else {
         return count > 0L ? Player.ModeEnum.NORMAL : null;
      }
   }

   public void enterGameUpdateMode() {
      Player.ModeEnum m = this.checkAndUpdateMode(Player.ModeEnum.BERSERK);
      if (m == null) {
         m = this.checkAndUpdateMode(Player.ModeEnum.FEVER);
      }

      if (m == null) {
         m = Player.ModeEnum.NORMAL;
      }

      this.changeMode(m);
      if (log.isDebugEnabled()) {
         log.debug(String.format("enter game mode stat '%s' FEVER %d, BERSERK %d", this.username, MemCachedClientWrapper.getCounter(MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_FEVER, this.username), MemCachedClientWrapper.getCounter(MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_BERSERK, this.username)));
      }

   }

   public long endGameUpdateModeStat(boolean winner) {
      MemCachedKeySpaces.CommonKeySpace keySpace = winner ? MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_FEVER : MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_BERSERK;
      long count = MemCachedClientWrapper.getCounter(keySpace, this.username);
      if (this.mode == Player.ModeEnum.NORMAL) {
         if (count > 0L) {
            count = MemCachedClientWrapper.incr(keySpace, this.username);
         } else {
            MemCachedClientWrapper.set(keySpace, this.username, 1L);
            MemCachedKeySpaces.CommonKeySpace otherKeySpace = !winner ? MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_FEVER : MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_BERSERK;
            MemCachedClientWrapper.delete(otherKeySpace, this.username);
            count = 1L;
         }
      }

      if (log.isDebugEnabled()) {
         log.debug(String.format("end game mode stat '%s' FEVER %d, BERSERK %d", this.username, MemCachedClientWrapper.getCounter(MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_FEVER, this.username), MemCachedClientWrapper.getCounter(MemCachedKeySpaces.CommonKeySpace.BOT_WARRIORS_BERSERK, this.username)));
      }

      return count;
   }

   static enum MilestoneEnum {
      NONE,
      D10,
      P25,
      P50,
      P75;
   }

   static enum ModeEnum {
      NORMAL,
      FEVER,
      BERSERK;
   }
}
