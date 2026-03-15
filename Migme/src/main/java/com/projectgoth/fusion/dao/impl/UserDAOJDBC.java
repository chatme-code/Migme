package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.dao.UserDAO;
import com.projectgoth.fusion.domain.CountryLogins;
import com.projectgoth.fusion.userevent.system.domain.AllowListEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

public class UserDAOJDBC extends MigJdbcDaoSupport implements UserDAO {
   public List<CountryLogins> findRecentLoginsGroupedByCountry(Date since, Date until, int period) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("UserDAO.findRecentLoginsGroupedByCountry"), new Object[]{since, period, until}, new UserDAOJDBC.CountryLoginsRowMapper());
   }

   public List<AllowListEntry> getAllowListForUser(String username) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("UserDAO.getAllowListForUser"), new Object[]{username}, new UserDAOJDBC.AllowListEntryRowMapper());
   }

   public String getMobileNumberForUser(String username) {
      return (String)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("UserDAO.getMobileNumberForUser"), new Object[]{username}, String.class);
   }

   public boolean isVerfiedEmailAddress(String recipient) {
      int result = this.getJdbcTemplate().queryForInt(this.getExternalizedQuery("UserDAO.isVerfiedEmailAddress"), new Object[]{recipient});
      return result != 0;
   }

   public boolean isBounceEmailAddress(String recipient) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.UserNotificationServiceSettings.CHECK_EMAIL_BOUNCEDB_ENABLED)) {
         return false;
      } else {
         int result = this.getJdbcTemplate().queryForInt(this.getExternalizedQuery("UserDAO.isBounceEmailAddress"), new Object[]{recipient});
         return result != 0;
      }
   }

   private static final class AllowListEntryRowMapper implements RowMapper {
      private AllowListEntryRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         AllowListEntry countryLogins = new AllowListEntry(rs.getString("AllowUsername"), rs.getBoolean("PendingApproval"));
         return countryLogins;
      }

      // $FF: synthetic method
      AllowListEntryRowMapper(Object x0) {
         this();
      }
   }

   private static final class CountryLoginsRowMapper implements RowMapper {
      private CountryLoginsRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         CountryLogins countryLogins = new CountryLogins(rs.getInt("country"), rs.getInt("sessions"), rs.getBoolean("mobileverified"));
         return countryLogins;
      }

      // $FF: synthetic method
      CountryLoginsRowMapper(Object x0) {
         this();
      }
   }
}
