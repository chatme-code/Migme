package com.projectgoth.fusion.chatnewsfeed.cricket.data;

import java.util.List;
import org.apache.axis.utils.StringUtils;

public class MatchScorecard {
   Match match;
   String score;
   int lastReadNode;
   int currentNode;
   String lastCommentary;
   String battingTeam;
   String bowlingTeam;
   String innings;
   boolean isInningsChange;
   String over;
   String ball;
   boolean changeChatRoomDescription = false;
   boolean isLiveStatusUpdated;
   String ballDetail;
   boolean isEndOfOver;
   String FIScoreString;
   String SIScoreString;
   String TIScoreString;
   String FOIScoreString;
   String currentInningsSummary;
   int total;
   int wickets;
   int allottedOvers;
   float completedOvers;
   int runsInOver;
   int targetScore;
   int runsNeeded;
   float runRate;
   float requiredRunRate;
   List<String> BatsmenStats;
   List<String> BowlerStats;

   public MatchScorecard(Match match) {
      try {
         this.match = (Match)match.clone();
      } catch (CloneNotSupportedException var3) {
         var3.printStackTrace();
      }

   }

   public String getScore() {
      return this.score == null ? "0/0" : this.score;
   }

   public void setScore(String score) {
      this.score = score;
   }

   public Match getMatch() {
      return this.match;
   }

   public void setMatch(Match match) {
      this.match = match;
   }

   public int getCurrentNode() {
      return this.currentNode;
   }

   public void setCurrentNode(int currentNode) {
      this.currentNode = currentNode;
   }

   public int getLastReadNode() {
      return this.lastReadNode;
   }

   public void setLastReadNode(int lastReadNode) {
      this.lastReadNode = lastReadNode;
   }

   public String getLastCommentary() {
      return this.lastCommentary;
   }

   public void setLastCommentary(String lastCommentary) {
      this.lastCommentary = lastCommentary;
   }

   public String getBattingTeam() {
      return this.battingTeam;
   }

   public void setBattingTeam(String battingTeam) {
      this.battingTeam = battingTeam;
   }

   public String getBowlingTeam() {
      return this.bowlingTeam;
   }

   public void setBowlingTeam(String bowlingTeam) {
      this.bowlingTeam = bowlingTeam;
   }

   public String getInnings() {
      return this.innings;
   }

   public void setInnings(String innings) {
      this.innings = innings;
   }

   public boolean isInningsChange() {
      return this.isInningsChange;
   }

   public void setInningsChange(boolean isInningsChange) {
      this.isInningsChange = isInningsChange;
   }

   public boolean isChangeChatRoomDescription() {
      return this.changeChatRoomDescription;
   }

   public void setChangeChatRoomDescription(boolean changeChatRoomDescription) {
      this.changeChatRoomDescription = changeChatRoomDescription;
   }

   public boolean isLiveStatusUpdated() {
      return this.isLiveStatusUpdated;
   }

   public void setLiveStatusUpdated(boolean isLiveStatusUpdated) {
      this.isLiveStatusUpdated = isLiveStatusUpdated;
   }

   public String getOver() {
      return this.over;
   }

   public void setOver(String over) {
      this.over = over;
   }

   public String getBall() {
      return this.ball;
   }

   public void setBall(String ball) {
      this.ball = ball;
   }

   public String getBallDetail() {
      return this.ballDetail;
   }

   public void setBallDetail(String ballDetail) {
      this.ballDetail = ballDetail;
   }

   public boolean isEndOfOver() {
      return this.isEndOfOver;
   }

   public void setEndOfOver(boolean isEndOfOver) {
      this.isEndOfOver = isEndOfOver;
   }

   public String getFIScoreString() {
      return this.FIScoreString;
   }

   public void setFIScoreString(String scoreString) {
      this.FIScoreString = scoreString;
   }

   public String getSIScoreString() {
      return this.SIScoreString;
   }

   public void setSIScoreString(String scoreString) {
      this.SIScoreString = scoreString;
   }

   public String getTIScoreString() {
      return this.TIScoreString;
   }

   public void setTIScoreString(String scoreString) {
      this.TIScoreString = scoreString;
   }

   public String getFOIScoreString() {
      return this.FOIScoreString;
   }

   public void setFOIScoreString(String scoreString) {
      this.FOIScoreString = scoreString;
   }

   public String getCurrentInningsSummary() {
      return this.currentInningsSummary;
   }

   public void setCurrentInningsSummary(String inningsSummary) {
      this.currentInningsSummary = inningsSummary;
   }

   public int getTotal() {
      return this.total;
   }

   public void setTotal(int total) {
      this.total = total;
   }

   public int getWickets() {
      return this.wickets;
   }

   public void setWickets(int wickets) {
      this.wickets = wickets;
   }

   public int getAllottedOvers() {
      return this.allottedOvers;
   }

   public void setAllottedOvers(int allottedOvers) {
      this.allottedOvers = allottedOvers;
   }

   public float getCompletedOvers() {
      return this.completedOvers;
   }

   public void setCompletedOvers(float completedOvers) {
      this.completedOvers = completedOvers;
   }

   public int getRunsInOver() {
      return this.runsInOver;
   }

   public void setRunsInOver(int runsInOver) {
      this.runsInOver = runsInOver;
   }

   public int getTargetScore() {
      return this.targetScore;
   }

   public void setTargetScore(int targetScore) {
      this.targetScore = targetScore;
   }

   public int getRunsNeeded() {
      return this.runsNeeded;
   }

   public void setRunsNeeded(int runsNeeded) {
      this.runsNeeded = runsNeeded;
   }

   public float getRunRate() {
      return this.runRate;
   }

   public void setRunRate(float runRate) {
      this.runRate = runRate;
   }

   public float getRequiredRunRate() {
      return this.requiredRunRate;
   }

   public void setRequiredRunRate(float requiredRunRate) {
      this.requiredRunRate = requiredRunRate;
   }

   public List<String> getBatsmenStats() {
      return this.BatsmenStats;
   }

   public void setBatsmenStats(List<String> batsmenStats) {
      this.BatsmenStats = batsmenStats;
   }

   public List<String> getBowlerStats() {
      return this.BowlerStats;
   }

   public void setBowlerStats(List<String> bowlerStats) {
      this.BowlerStats = bowlerStats;
   }

   public String toString() {
      StringBuilder string = new StringBuilder(super.toString());
      string.append(this.match.toString());
      string.append(", lastReadNode [").append(this.lastReadNode).append("]");
      string.append(", commentary [").append(this.lastCommentary).append("]");
      string.append(", innings [").append(this.innings).append("]");
      string.append(", batting [").append(this.battingTeam).append("]");
      string.append(", bowling [").append(this.bowlingTeam).append("]");
      return string.toString();
   }

   public boolean isSixer() {
      return !StringUtils.isEmpty(this.ballDetail.trim()) && this.ballDetail.equalsIgnoreCase("Sixer");
   }

   public boolean isFour() {
      return !StringUtils.isEmpty(this.ballDetail.trim()) && this.ballDetail.equalsIgnoreCase("Boundary");
   }

   public boolean isOut() {
      return !StringUtils.isEmpty(this.ballDetail.trim()) && (this.ballDetail.startsWith("c ") || this.ballDetail.startsWith("b ") || this.ballDetail.startsWith("run out "));
   }
}
