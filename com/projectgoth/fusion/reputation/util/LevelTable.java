package com.projectgoth.fusion.reputation.util;

import com.projectgoth.fusion.data.ReputationLevelData;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class LevelTable {
   public static SortedMap<Integer, Integer> readLevelTable(Connection connection) throws SQLException {
      SortedMap<Integer, Integer> map = new TreeMap(Collections.reverseOrder());
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select score,level from ReputationScoreToLevel order by score desc");

      while(rs.next()) {
         map.put(rs.getInt(1), rs.getInt(2));
      }

      return map;
   }

   public static SortedMap<Integer, ReputationLevelData> readLevelDataTable(Connection connection) throws SQLException {
      SortedMap<Integer, ReputationLevelData> map = new TreeMap(Collections.reverseOrder());
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select * from ReputationScoreToLevel order by score desc");

      while(rs.next()) {
         ReputationLevelData levelData = new ReputationLevelData(rs);
         map.put(levelData.level, levelData);
      }

      return map;
   }

   public static int getLevelForScore(int score, SortedMap<Integer, Integer> levelTable) {
      Iterator i$ = levelTable.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return 1;
         }

         key = (Integer)i$.next();
      } while(score < key);

      return (Integer)levelTable.get(key);
   }

   public static ReputationLevelData getLevelDataForScore(int score, SortedMap<Integer, ReputationLevelData> levelDataTable) {
      Iterator i$ = levelDataTable.values().iterator();

      ReputationLevelData value;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         value = (ReputationLevelData)i$.next();
      } while(score < value.score);

      return value;
   }

   public static int getScoreForLevel(int level, SortedMap<Integer, Integer> levelTable) {
      Iterator i$ = levelTable.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return 0;
         }

         key = (Integer)i$.next();
      } while(level < (Integer)levelTable.get(key));

      return key;
   }

   public static void main(String[] args) {
   }
}
