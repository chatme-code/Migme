package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.dao.CountryDAO;
import com.projectgoth.fusion.data.CountryData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

public class CountryDAOJDBC extends MigJdbcDaoSupport implements CountryDAO {
   public CountryData getCountryForUser(String username) {
      try {
         return (CountryData)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("CountryDAO.getCountryForUser"), new Object[]{username}, new CountryDAOJDBC.CountryDataRowMapper());
      } catch (DataAccessException var3) {
         return null;
      }
   }

   private static final class CountryDataRowMapper implements RowMapper {
      private CountryDataRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return new CountryData(rs);
      }

      // $FF: synthetic method
      CountryDataRowMapper(Object x0) {
         this();
      }
   }
}
