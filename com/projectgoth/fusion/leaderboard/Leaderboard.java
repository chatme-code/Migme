package com.projectgoth.fusion.leaderboard;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Redis;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.PipelineBlock;

public class Leaderboard {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Leaderboard.class));

   public static void insert(Leaderboard.Type leaderboardType, Leaderboard.Period leaderboardPeriod, int userID, String username, double amount) throws Exception {
      String redisKey = leaderboardType.append(leaderboardPeriod.toString());
      String setMember = Integer.toString(userID) + ':' + username;
      Jedis masterInst = Redis.getLeaderboardsMasterInstance();
      masterInst.zadd(redisKey, amount, setMember);
      Redis.disconnect(masterInst, log);
   }

   public static void increment(Leaderboard.Type leaderboardType, Leaderboard.Period leaderboardPeriod, int userID, String username, double amount) throws Exception {
      String redisKey = leaderboardType.append(leaderboardPeriod.toString());
      String setMember = Integer.toString(userID) + ':' + username;
      Jedis masterInst = Redis.getLeaderboardsMasterInstance();
      masterInst.zincrby(redisKey, amount, setMember);
      Redis.disconnect(masterInst, log);
   }

   public static void setLastLoggedIn() throws Exception {
   }

   public static void freeLastLoggedIn() throws Exception {
      String lastLoggedInKey = "LastLoggedIn";
      Jedis masterInst = Redis.getLeaderboardsMasterInstance();
      masterInst.del(lastLoggedInKey);
      Redis.disconnect(masterInst, log);
   }

   public static void allTimeCleanUp(Leaderboard.Type leaderboardType) throws Exception {
   }

   public static void reset(Leaderboard.Type leaderboardType, Leaderboard.Period leaderboardPeriod, Leaderboard.Period leaderboardPreviousPeriod) throws Exception {
      String redisKey = leaderboardType.append(leaderboardPeriod.toString());
      String previousRedisKey = leaderboardType.append(leaderboardPreviousPeriod.toString());

      try {
         Jedis masterInst = Redis.getLeaderboardsMasterInstance();
         masterInst.rename(redisKey, previousRedisKey);
         Redis.disconnect(masterInst, log);
      } catch (Exception var6) {
         log.info("[ERROR] Unable to rename " + previousRedisKey + " from " + redisKey);
      }

   }

   public static void recordGamesMetric(Leaderboard.Type leaderboardType, List<String> usernames) throws Exception {
      User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      List<Integer> userids = new ArrayList(usernames.size());

      for(int i = 0; i < usernames.size(); ++i) {
         String username = (String)usernames.get(i);
         int userid = userBean.getUserID(username, (Connection)null);
         userids.add(userid);
      }

      recordGamesMetric(leaderboardType, usernames, userids);
   }

   public static void recordGamesMetric(Leaderboard.Type leaderboardType, List<String> usernames, List<Integer> userids) throws Exception {
      if (usernames.size() != userids.size()) {
         log.warn(String.format("Incorrect parameters to recordGamesMetric for key [%s], %d usernames but %d userids. Recalculate userids from UserBean.", leaderboardType.value, usernames.size(), userids.size()));
         recordGamesMetric(leaderboardType, usernames);
      } else {
         final String redisKeyDaily = leaderboardType.append(Leaderboard.Period.DAILY.toString());
         final String redisKeyWeekly = leaderboardType.append(Leaderboard.Period.WEEKLY.toString());
         Jedis masterInst = null;
         final List<String> userKeys = new ArrayList(usernames.size());

         for(int i = 0; i < usernames.size(); ++i) {
            String username = (String)usernames.get(i);
            int userid = (Integer)userids.get(i);
            userKeys.add(username + ":" + userid);
         }

         try {
            masterInst = Redis.getLeaderboardsMasterInstance();
            masterInst.pipelined(new PipelineBlock() {
               public void execute() {
                  for(int j = 0; j < userKeys.size(); ++j) {
                     String userKey = (String)userKeys.get(j);
                     this.zincrby(redisKeyDaily, 1.0D, userKey);
                     this.zincrby(redisKeyWeekly, 1.0D, userKey);
                  }

               }
            });
         } catch (Exception var13) {
            log.info("[ERROR] Unable to recordGamesMetric for key [" + leaderboardType.value + "]");
         } finally {
            Redis.disconnect(masterInst, log);
         }

      }
   }

   public static void recordGamesMetric(Leaderboard.Type leaderboardType, String username, int userId, int amount) {
      String redisKeyDaily = leaderboardType.append(Leaderboard.Period.DAILY.toString());
      String redisKeyWeekly = leaderboardType.append(Leaderboard.Period.WEEKLY.toString());
      String userKey = username + ":" + userId;
      Jedis masterInstance = null;

      try {
         masterInstance = Redis.getLeaderboardsMasterInstance();
         masterInstance.zincrby(redisKeyDaily, (double)amount, userKey);
         masterInstance.zincrby(redisKeyWeekly, (double)amount, userKey);
      } catch (Exception var12) {
         log.error("Error getting master instance of redis to update leaderboard");
      } finally {
         Redis.disconnect(masterInstance, log);
      }

   }

   public static List<Integer> getGamesMetric(Leaderboard.Type leaderboardType, Leaderboard.Period period, List<String> usernames) throws Exception {
      User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
      List<Integer> userids = new ArrayList(usernames.size());

      for(int i = 0; i < usernames.size(); ++i) {
         String username = (String)usernames.get(i);
         int userid = userBean.getUserID(username, (Connection)null);
         userids.add(userid);
      }

      return getGamesMetric(leaderboardType, period, usernames, userids);
   }

   public static List<Integer> getGamesMetric(Leaderboard.Type leaderboardType, Leaderboard.Period period, List<String> usernames, List<Integer> userids) throws Exception {
      if (usernames.size() != userids.size()) {
         log.warn(String.format("Incorrect parameters to getGamesMetric for key [%s] period [%s], %d usernames but %d userids. Recalculate userids from UserBean.", leaderboardType.value, period.value, usernames.size(), userids.size()));
         return getGamesMetric(leaderboardType, period, usernames);
      } else {
         final String redisKey = leaderboardType.append(period.toString());
         Jedis masterInst = null;
         final List<String> userKeys = new ArrayList(usernames.size());

         String username;
         for(int i = 0; i < usernames.size(); ++i) {
            username = (String)usernames.get(i);
            int userid = (Integer)userids.get(i);
            userKeys.add(username + ":" + userid);
         }

         try {
            masterInst = Redis.getLeaderboardsMasterInstance();
            List<Object> results = masterInst.pipelined(new PipelineBlock() {
               public void execute() {
                  for(int j = 0; j < userKeys.size(); ++j) {
                     String userKey = (String)userKeys.get(j);
                     this.zscore(redisKey, userKey);
                  }

               }
            });
            List<Integer> intResults = new ArrayList(results.size());
            Iterator i$ = results.iterator();

            while(i$.hasNext()) {
               Object o = i$.next();
               intResults.add(((Double)o).intValue());
            }

            ArrayList var19 = intResults;
            return var19;
         } catch (Exception var14) {
            log.info("[ERROR] Unable to getGamesMetric for key [" + redisKey + "] " + var14.getMessage());
            username = null;
         } finally {
            Redis.disconnect(masterInst, log);
         }

         return username;
      }
   }

   public static void updateMigLevel() throws Exception {
   }

   public static void sendVirtualGift(String senderUsername, String receiverUsername) throws Exception {
      if (SystemProperty.getBool("EnableLeaderboards", false)) {
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         int senderUserID = userBean.getUserID(senderUsername, (Connection)null);
         int receiverUserID = userBean.getUserID(receiverUsername, (Connection)null);
         final String senderUserKey = senderUsername + ":" + senderUserID;
         final String receiverUserKey = receiverUsername + ":" + receiverUserID;
         Leaderboard.Type giftSentLBType = Leaderboard.Type.GIFT_SENT;
         Leaderboard.Type giftReceivedLBType = Leaderboard.Type.GIFT_RECEIVED;
         final String giftSentDailyRedisKey = giftSentLBType.append(Leaderboard.Period.DAILY.toString());
         final String giftSentWeeklyRedisKey = giftSentLBType.append(Leaderboard.Period.WEEKLY.toString());
         final String giftReceivedDailyRedisKey = giftReceivedLBType.append(Leaderboard.Period.DAILY.toString());
         final String giftReceivedWeeklyRedisKey = giftReceivedLBType.append(Leaderboard.Period.WEEKLY.toString());
         Jedis masterInst = null;

         try {
            masterInst = Redis.getLeaderboardsMasterInstance();
            masterInst.pipelined(new PipelineBlock() {
               public void execute() {
                  this.zincrby(giftSentDailyRedisKey, 1.0D, senderUserKey);
                  this.zincrby(giftSentWeeklyRedisKey, 1.0D, senderUserKey);
                  this.zincrby(giftReceivedDailyRedisKey, 1.0D, receiverUserKey);
                  this.zincrby(giftReceivedWeeklyRedisKey, 1.0D, receiverUserKey);
               }
            });
         } catch (Exception var18) {
            log.error("Pipeline execution error during updating of GiftSent/GiftReceived Daily/Weekly Leaderboards: " + var18.getMessage());
         } finally {
            Redis.disconnect(masterInst, log);
         }

      }
   }

   public static void updateReferrerLeaderboards(String referrerUsername, int referrerUserID) throws Exception {
      if (SystemProperty.getBool("EnableLeaderboards", false)) {
         final String referrerUserKey = referrerUsername + ":" + referrerUserID;
         Leaderboard.Type referrerLBType = Leaderboard.Type.REFERRER;
         final String referrerDailyRedisKey = referrerLBType.append(Leaderboard.Period.DAILY.toString());
         final String referrerWeeklyRedisKey = referrerLBType.append(Leaderboard.Period.WEEKLY.toString());
         Jedis masterInst = null;

         try {
            masterInst = Redis.getLeaderboardsMasterInstance();
            masterInst.pipelined(new PipelineBlock() {
               public void execute() {
                  this.zincrby(referrerDailyRedisKey, 1.0D, referrerUserKey);
                  this.zincrby(referrerWeeklyRedisKey, 1.0D, referrerUserKey);
               }
            });
         } catch (Exception var11) {
            log.error("Pipeline execution error during updating of Referrer Daily/Weekly Leaderboards: " + var11.getMessage());
         } finally {
            Redis.disconnect(masterInst, log);
         }

      }
   }

   public static enum Period {
      DAILY("Daily"),
      WEEKLY("Weekly"),
      ALL_TIME("AllTime"),
      PREVIOUS_DAILY("PreviousDaily"),
      PREVIOUS_WEEKLY("PreviousWeekly");

      private String value;

      private Period(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }
   }

   public static enum Type {
      LOW_CARD_GAMES_PLAYED("LB:GamesPlayed:LowCard:"),
      DICE_GAMES_PLAYED("LB:GamesPlayed:Dice:"),
      DANGER_GAMES_PLAYED("LB:GamesPlayed:Danger:"),
      CRICKET_GAMES_PLAYED("LB:GamesPlayed:Cricket:"),
      FOOTBALL_GAMES_PLAYED("LB:GamesPlayed:Football:"),
      GUESS_GAMES_PLAYED("LB:GamesPlayed:Guess:"),
      WARRIORS_GAMES_PLAYED("LB:GamesPlayed:Warriors:"),
      LOW_CARD_MOST_WINS("LB:MostWins:LowCard:"),
      DICE_MOST_WINS("LB:MostWins:Dice:"),
      DANGER_MOST_WINS("LB:MostWins:Danger:"),
      CRICKET_MOST_WINS("LB:MostWins:Cricket:"),
      FOOTBALL_MOST_WINS("LB:MostWins:Football:"),
      GUESS_MOST_WINS("LB:MostWins:Guess:"),
      WARRIORS_MOST_WINS("LB:MostWins:Warriors:"),
      WARRIORS_NUM_KILLS("LB:NumKills:Warriors:"),
      USER_LIKES("LB:UserLikes:"),
      MIG_LEVEL("LB:MigLevel:"),
      REFERRER("LB:Referrer:"),
      GIFT_SENT("LB:GiftSent:"),
      GIFT_RECEIVED("LB:GiftReceived:"),
      AVATAR_VOTES("LB:AvatarVotes:"),
      PAINT_WARS_PAINT_POINTS("LB:PaintPoints:");

      public static final Leaderboard.Type[] TOTAL_PLAYED_LEADERBOARDS = new Leaderboard.Type[]{LOW_CARD_GAMES_PLAYED, DICE_GAMES_PLAYED, DANGER_GAMES_PLAYED, CRICKET_GAMES_PLAYED, FOOTBALL_GAMES_PLAYED, GUESS_GAMES_PLAYED, WARRIORS_GAMES_PLAYED};
      private String value;

      private Type(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }

      public String append(String s) {
         return this.value + s;
      }
   }
}
