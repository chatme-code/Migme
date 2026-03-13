package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UserReputationScoreAndLevelData implements Serializable {
   public Integer score;
   public Integer userID;
   public Integer level;
   public Date lastUpdated;
   private final boolean fromMaster;

   public UserReputationScoreAndLevelData(ResultSet rs, boolean fromMaster) throws SQLException {
      this.score = rs.getInt("score");
      this.userID = rs.getInt("userID");
      this.lastUpdated = rs.getTimestamp("lastUpdated");
      this.level = rs.getInt("level");
      this.fromMaster = fromMaster;
   }

   public UserReputationScoreAndLevelData(int userID, int score, int level, Date lastUpdated, boolean fromMaster) {
      this.userID = userID;
      this.score = score;
      this.level = level;
      this.lastUpdated = lastUpdated;
      this.fromMaster = fromMaster;
   }

   public boolean isFromMaster() {
      return this.fromMaster;
   }

   public String toString() {
      return "userID[" + this.userID + "] score[" + this.score + "] level[" + this.level + "] lastUpdated[" + this.lastUpdated + "] fromMaster[" + this.fromMaster + "]";
   }

   public boolean isCompatible(boolean expectFromMaster) {
      return !expectFromMaster || this.fromMaster;
   }
}
