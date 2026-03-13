package com.projectgoth.fusion.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserReputationScoreData {
   public final int userid;
   public final int score;
   public final Timestamp lastUpdated;

   public UserReputationScoreData(int userid, int score, Timestamp lastUpdated) {
      this.userid = userid;
      this.score = score;
      this.lastUpdated = lastUpdated;
   }

   public static UserReputationScoreData fromResultSet(ResultSet rs) throws SQLException {
      return new UserReputationScoreData(rs.getInt("userid"), rs.getInt("score"), rs.getTimestamp("lastUpdated"));
   }

   public String toString() {
      return "UserReputationScoreData{userid=[" + this.userid + "]" + ", score=[" + this.score + "]" + ", lastUpdated=[" + this.lastUpdated + "]" + '}';
   }
}
