package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.dao.ReputationDAO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public class ReputationDAOJDBC extends MigJdbcDaoSupport implements ReputationDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ReputationDAOJDBC.class));

   public SortedMap<Integer, Integer> readLevelTable() {
      SortedMap<Integer, Integer> map = new TreeMap(Collections.reverseOrder());
      List<List<Integer>> levels = this.getMasterTemplate().query(this.getExternalizedQuery("ReputationDAO.readLevelTable"), new ReputationDAOJDBC.ReputationLevelRowMapper());
      Iterator i$ = levels.iterator();

      while(i$.hasNext()) {
         List<Integer> level = (List)i$.next();
         map.put(level.get(0), level.get(1));
      }

      return map;
   }

   public int getUserScore(String username) {
      try {
         return this.getJdbcTemplate().queryForInt(this.getExternalizedQuery("ReputationDAO.getUserScore"), new Object[]{username});
      } catch (EmptyResultDataAccessException var3) {
         return 0;
      }
   }

   public Map<Integer, Integer> getUserScores(int[] userIDs) {
      Object map = new HashMap();

      try {
         if (this.log.isDebugEnabled()) {
            this.log.debug("getting scores for userids [" + StringUtil.asStringWithoutQuotes(userIDs) + "]");
         }

         map = (Map)this.getJdbcTemplate().query(this.getExternalizedQuery("ReputationDAO.getUserScores") + "(" + StringUtil.asStringWithoutQuotes(userIDs) + ")", new ResultSetExtractor() {
            public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
               HashMap map = new HashMap();

               while(rs.next()) {
                  map.put(rs.getInt(1), rs.getInt(2));
               }

               return map;
            }
         });
      } catch (EmptyResultDataAccessException var7) {
         this.log.error("failed to get user scores", var7);
      }

      int[] arr$ = userIDs;
      int len$ = userIDs.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         int userID = arr$[i$];
         if (!((Map)map).containsKey(userID)) {
            ((Map)map).put(userID, 0);
         }
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("we received " + userIDs.length + " userIDs and sending back " + ((Map)map).size() + " entries");
      }

      return (Map)map;
   }

   public int updateDailyScoreDistribution(Date runDate, SortedMap<Integer, Integer> scoreDistribution) {
      Object[] scoreValues = scoreDistribution.values().toArray();
      Object[] values = new Object[scoreValues.length + 1];
      values[0] = runDate;
      System.arraycopy(scoreValues, 0, values, 1, scoreValues.length);
      if (this.log.isDebugEnabled()) {
         Object[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Object object = arr$[i$];
            this.log.debug(object);
         }
      }

      return this.getMasterTemplate().update(this.getExternalizedQuery("ReputationDAO.updateDailyScoreDistribution"), values);
   }

   public int updateDailyLevelDistribution(Date runDate, SortedMap<Integer, Integer> levelDistribution) {
      Object[] levelValues = levelDistribution.values().toArray();
      Object[] values = new Object[levelValues.length + 1];
      values[0] = runDate;
      System.arraycopy(levelValues, 0, values, 1, levelValues.length);
      StringBuffer buffer = new StringBuffer("(runDate,");

      for(int i = 0; i < levelValues.length; ++i) {
         if (i < levelValues.length - 1) {
            buffer.append("l").append(i + 1).append(",");
         } else {
            buffer.append("l").append(i + 1);
         }
      }

      buffer.append(")");
      String sql = this.getExternalizedQuery("ReputationDAO.updateDailyLevelDistribution") + buffer.toString() + " values " + this.preparedStatementParameters(values.length);
      if (this.log.isDebugEnabled()) {
         this.log.debug(values.length + " values");
         Object[] arr$ = values;
         int len$ = values.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Object object = arr$[i$];
            this.log.debug(object);
         }

         this.log.debug("sql = " + sql);
      }

      return this.getMasterTemplate().update(sql, values);
   }

   private static final class ReputationLevelRowMapper implements RowMapper {
      private ReputationLevelRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         List<Integer> results = new ArrayList();
         results.add(rs.getInt(1));
         results.add(rs.getInt(2));
         return results;
      }

      // $FF: synthetic method
      ReputationLevelRowMapper(Object x0) {
         this();
      }
   }
}
