package com.projectgoth.fusion.dao;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;

public interface ReputationDAO {
   SortedMap<Integer, Integer> readLevelTable();

   int getUserScore(String var1);

   Map<Integer, Integer> getUserScores(int[] var1);

   int updateDailyScoreDistribution(Date var1, SortedMap<Integer, Integer> var2);

   int updateDailyLevelDistribution(Date var1, SortedMap<Integer, Integer> var2);
}
