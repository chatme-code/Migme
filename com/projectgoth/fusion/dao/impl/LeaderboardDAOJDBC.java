package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.LeaderboardDAO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class LeaderboardDAOJDBC extends MigJdbcDaoSupport implements LeaderboardDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LeaderboardDAOJDBC.class));
   private int lastLoginDayLength = 14;

   public List<String> getLastLoggedInForRedis(int days) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("LeaderboardDAO.getLastLoggedInUsersForRedis"), new Object[]{days}, new LeaderboardDAOJDBC.UserLoggedInRowMapper());
   }

   public List<String[]> getLastLoggedInUserScoreForRedis(int days) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("LeaderboardDAO.getLastLoggedInUserScoreForRedis"), new Object[]{days}, new LeaderboardDAOJDBC.UserLoggedInScoreRowMapper());
   }

   public void generateLeaderboard(int leaderboardId) {
      DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
      Date date = new Date();
      String directory = "/var/tmp";
      switch(leaderboardId) {
      case 1:
         this.log.info("Generating leaderboard for ProfileLikesAllTime");
         this.generateLeaderboardProfileLikesAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for ProfileLikesAllTime");
         break;
      case 2:
         this.log.info("Generating leaderboard for MigLevelAllTime");
         this.generateLeaderboardMigLevelAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for MigLevelAllTime");
         break;
      case 3:
         this.log.info("Generating leaderboard for ReferrerAllTime");
         this.generateLeaderboardReferrerAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for ReferrerAllTime");
         break;
      case 4:
         this.log.info("Generating leaderboard for ReferrerWeekly");
         this.generateLeaderboardReferrerWeekly(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for ReferrerWeekly");
         break;
      case 5:
         this.log.info("Generating leaderboard for GiftReceiverAllTime");
         this.generateLeaderboardGiftReceiverAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for GiftReceiverAllTime");
         break;
      case 6:
         this.log.info("Generating leaderboard for GiftReceiverWeekly");
         this.generateLeaderboardGiftReceiverWeekly(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for GiftReceiverWeekly");
         break;
      case 7:
         this.log.info("Generating leaderboard for GiftSenderAllTime");
         this.generateLeaderboardGiftSenderAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for GiftSenderAllTime");
         break;
      case 8:
         this.log.info("Generating leaderboard for GiftSenderWeekly");
         this.generateLeaderboardGiftSenderWeekly(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for GiftSenderWeekly");
         break;
      case 9:
         this.log.info("Generating leaderboard for GroupLikesAllTime");
         this.generateLeaderboardGroupLikesAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for GroupLikesAllTime");
         break;
      case 10:
         this.log.info("Generating leaderboard for GroupPhotosAllTime");
         this.generateLeaderboardGroupPhotosAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for GroupPhotosAllTime");
         break;
      case 11:
         this.log.info("Generating leaderboard for GroupTopicsAllTime");
         this.generateLeaderboardGroupTopicsAllTime(directory, date, dateFormat);
         this.log.info("[DONE] Generating leaderboard for GroupTopicsAllTime");
      }

   }

   private void generateLeaderboardProfileLikesAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/profilelikesalltime.txt";
      this.getJdbcTemplate().execute("SELECT uls.userid as `UserID`, uls.numlikes as `Value`, NULL as `Rank` FROM userlikesummary uls, `user` u, `userid` uid WHERE u.username = uid.username AND uid.id = uls.userid AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "ORDER BY uls.numlikes DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardprofilelikesalltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardprofilelikesalltime");
   }

   private void generateLeaderboardMigLevelAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/miglevelalltime.txt";
      this.getJdbcTemplate().execute("SELECT s.userid as `UserID`, s.score as `Value`, NULL as `Rank` FROM score s, `user` u, `userid` uid WHERE u.username = uid.username AND uid.id = s.userid AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "ORDER BY s.score DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardmiglevelalltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardmiglevelalltime");
   }

   private void generateLeaderboardReferrerAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/referreralltime.txt";
      this.getJdbcTemplate().execute("SELECT uid.id as `UserID`, COUNT(*) as `Value`, NULL as `Rank` FROM `userreferral` ur, `user` u, `userid` uid WHERE u.username = uid.username AND uid.username = ur.username AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "AND ur.paid > 0 " + "GROUP BY ur.username " + "ORDER BY `Value` DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardreferreralltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardreferreralltime");
   }

   private void generateLeaderboardReferrerWeekly(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/referrerweekly.txt";
      this.getJdbcTemplate().execute("SELECT uid.id as `UserID`, COUNT(*) as `Value`, NULL as `Rank` FROM `userreferral` ur, `user` u, `userid` uid, activation a WHERE u.username = uid.username AND uid.username = ur.username AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "AND ur.paid > 0 " + "AND a.username = uid.username AND WEEK(a.DateCreated) = WEEK(NOW()) " + "GROUP BY ur.username " + "ORDER BY `Value` DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardreferrerweekly");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardreferrerweekly");
   }

   private void generateLeaderboardGiftReceiverAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/giftreceiveralltime.txt";
      this.getJdbcTemplate().execute("SELECT uid.id as `UserID`, COUNT(*) as `Value`, NULL as `Rank` FROM `virtualgiftreceived` vgr, `user` u, `userid` uid WHERE u.username = uid.username AND uid.username = vgr.username AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "GROUP BY vgr.username " + "ORDER BY `Value` DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardgiftreceiveralltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardgiftreceiveralltime");
   }

   private void generateLeaderboardGiftReceiverWeekly(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/giftreceiverweekly.txt";
      this.getJdbcTemplate().execute("SELECT uid.id as `UserID`, COUNT(*) as `Value`, NULL as `Rank` FROM `virtualgiftreceived` vgr, `user` u, `userid` uid WHERE u.username = uid.username AND uid.username = vgr.username AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "AND WEEK(vgr.DateCreated) = WEEK(NOW()) " + "GROUP BY vgr.username " + "ORDER BY `Value` DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardgiftreceiverweekly");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardgiftreceiverweekly");
   }

   private void generateLeaderboardGiftSenderAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/giftsenderalltime.txt";
      this.getJdbcTemplate().execute("SELECT uid.id as `UserID`, COUNT(*) as `Value`, NULL as `Rank` FROM `virtualgiftreceived` vgr, `user` u, `userid` uid WHERE u.username = uid.username AND uid.username = vgr.sender AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "GROUP BY vgr.sender " + "ORDER BY `Value` DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardgiftsenderalltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardgiftsenderalltime");
   }

   private void generateLeaderboardGiftSenderWeekly(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/giftsenderweekly.txt";
      this.getJdbcTemplate().execute("SELECT uid.id as `UserID`, COUNT(*) as `Value`, NULL as `Rank` FROM `virtualgiftreceived` vgr, `user` u, `userid` uid WHERE u.username = uid.username AND uid.username = vgr.sender AND DATEDIFF(NOW(), u.LastLoginDate) < " + this.lastLoginDayLength + " " + "AND WEEK(vgr.DateCreated) = WEEK(NOW()) " + "GROUP BY vgr.sender " + "ORDER BY `Value` DESC " + "INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardgiftsenderweekly");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardgiftsenderweekly");
   }

   private void generateLeaderboardGroupLikesAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/grouplikesalltime.txt";
      this.getJdbcTemplate().execute("SELECT g.id as `Id`, gl.numlikes as `Value`, NULL as `Rank` FROM `groups` g, `grouplikesummary` gl WHERE g.id = gl.groupid ORDER BY `Value` DESC INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardgrouplikesalltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardgrouplikesalltime");
   }

   private void generateLeaderboardGroupPhotosAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/groupphotosalltime.txt";
      this.getJdbcTemplate().execute("SELECT g.id as `Id`, g.numphotos as `Value`, NULL as `Rank` FROM `groups` g ORDER BY `Value` DESC INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardgroupphotosalltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardgroupphotosalltime");
   }

   private void generateLeaderboardGroupTopicsAllTime(String directory, Date date, DateFormat dateFormat) {
      String filename = directory + "/grouptopicsalltime.txt";
      this.getJdbcTemplate().execute("SELECT g.id as `Id`, g.numforumposts as `Value`, NULL as `Rank` FROM `groups` g ORDER BY `Value` DESC INTO OUTFILE '" + filename + "'");
      this.getJdbcTemplate().execute("TRUNCATE leaderboardgrouptopicsalltime");
      this.getJdbcTemplate().execute("LOAD DATA INFILE '" + filename + "' INTO TABLE leaderboardgrouptopicsalltime");
   }

   private static final class UserLoggedInScoreRowMapper implements RowMapper {
      private UserLoggedInScoreRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         String user = rs.getString("userKey");
         String score = rs.getString("score");
         String[] result = new String[]{user, score};
         return result;
      }

      // $FF: synthetic method
      UserLoggedInScoreRowMapper(Object x0) {
         this();
      }
   }

   private static final class UserLoggedInRowMapper implements RowMapper {
      private UserLoggedInRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         String user = rs.getString("userKey");
         return user;
      }

      // $FF: synthetic method
      UserLoggedInRowMapper(Object x0) {
         this();
      }
   }
}
