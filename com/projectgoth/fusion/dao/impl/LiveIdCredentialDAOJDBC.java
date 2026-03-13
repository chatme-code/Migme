package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.dao.LiveIdCredentialDAO;
import com.projectgoth.fusion.domain.LiveIdCredential;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;

public class LiveIdCredentialDAOJDBC extends MigJdbcDaoSupport implements LiveIdCredentialDAO {
   public LiveIdCredential getCredential(String username) {
      try {
         return (LiveIdCredential)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("LiveIdCredentialDAO.getCredential"), new Object[]{username}, new LiveIdCredentialDAOJDBC.LiveIdCredentialRowMapper());
      } catch (IncorrectResultSizeDataAccessException var3) {
         return null;
      }
   }

   public void persistCredential(LiveIdCredential credential) {
      this.getJdbcTemplate().update(this.getExternalizedQuery("LiveIdCredentialDAO.persistCredential"), new Object[]{credential.getUsername(), credential.getLiveId(), credential.getPassword(), new Timestamp(System.currentTimeMillis())});
   }

   private static final class LiveIdCredentialRowMapper implements RowMapper {
      private LiveIdCredentialRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         LiveIdCredential liveIdCredential = new LiveIdCredential(rs.getString("username"), rs.getString("liveid"), rs.getString("password"));
         return liveIdCredential;
      }

      // $FF: synthetic method
      LiveIdCredentialRowMapper(Object x0) {
         this();
      }
   }
}
