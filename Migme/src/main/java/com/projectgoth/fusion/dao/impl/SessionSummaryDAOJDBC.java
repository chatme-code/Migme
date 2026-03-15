package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.dao.SessionSummaryDAO;
import com.projectgoth.fusion.domain.CountryLogins;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

public class SessionSummaryDAOJDBC extends MigJdbcDaoSupport implements SessionSummaryDAO {
   public void updateTotalSession(Date date, int country, int totalAuthenticated, int totalNonAuthenticated) {
      if (totalAuthenticated > 0 || totalNonAuthenticated > 0) {
         this.getJdbcTemplate().update(this.getExternalizedQuery("SessionSummaryDAO.updateTotalSession"), new Object[]{totalAuthenticated, totalNonAuthenticated, new java.sql.Date(date.getTime()), country});
      }
   }

   public void updateUniqueTotals(final Date periodDate, int period, boolean authenticated, final List<CountryLogins> countryLogins) {
      String queryKey = "SessionSummaryDAO.updateUniqueTotals";
      queryKey = queryKey + (authenticated ? "Auth" : "NonAuth");
      queryKey = queryKey + period;
      this.getJdbcTemplate().batchUpdate(this.getExternalizedQuery(queryKey), new BatchPreparedStatementSetter() {
         public int getBatchSize() {
            return countryLogins.size();
         }

         public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, ((CountryLogins)countryLogins.get(i)).getLogins());
            ps.setInt(2, ((CountryLogins)countryLogins.get(i)).getCountry());
            ps.setDate(3, new java.sql.Date(periodDate.getTime()));
         }
      });
   }

   public void createDailyRows(Date date) {
      this.getJdbcTemplate().update(this.getExternalizedQuery("SessionSummaryDAO.createDailyRows"), new Object[]{date});
   }

   public boolean rowsExist(Date date) {
      return this.getJdbcTemplate().queryForInt(this.getExternalizedQuery("SessionSummaryDAO.rowsExist"), new Object[]{new java.sql.Date(date.getTime())}) > 0;
   }

   public boolean yesterdaysTotalsExists(Date yesterday) {
      return this.getJdbcTemplate().queryForInt(this.getExternalizedQuery("SessionSummaryDAO.yesterdaysTotalsExists"), new Object[]{new java.sql.Date(yesterday.getTime())}) > 0;
   }
}
