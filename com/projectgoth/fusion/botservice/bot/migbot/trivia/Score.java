package com.projectgoth.fusion.botservice.bot.migbot.trivia;

public class Score implements Comparable<Score> {
   String player;
   int points;

   public Score(String player, int points) {
      this.player = player;
      this.points = points;
   }

   public String getPlayer() {
      return this.player;
   }

   public int getScore() {
      return this.points;
   }

   public void incrementScore(int points) {
      this.points += points;
   }

   public boolean equals(Object scoreObj) {
      if (scoreObj != null && scoreObj instanceof Score) {
         Score score = (Score)scoreObj;
         return this.player.equals(score.player) && this.points == score.points;
      } else {
         return false;
      }
   }

   public int compareTo(Score o) {
      if (this.points > o.points) {
         return -1;
      } else {
         return this.points > o.points ? 0 : 1;
      }
   }
}
