package com.projectgoth.fusion.dao;

import java.util.List;

public interface LeaderboardDAO {
   void generateLeaderboard(int var1);

   List<String> getLastLoggedInForRedis(int var1);

   List<String[]> getLastLoggedInUserScoreForRedis(int var1);
}
