package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.dao.FashionShowDAO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

public class FashionShowDAOJDBC extends MigJdbcDaoSupport implements FashionShowDAO {
   private Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(FashionShowDAOJDBC.class));

   public List<String> getAvatarCandidatesForRedis(int days, int level, int avatarItemCount) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("FashionShowDAO.getAvatarCandidatesForRedis"), new Object[]{days, level, avatarItemCount}, new FashionShowDAOJDBC.AvatarCandidatesRowMapper());
   }

   private static final class AvatarCandidatesRowMapper implements RowMapper {
      private AvatarCandidatesRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         String user = rs.getString("uname") + ":" + rs.getString("uid");
         return user;
      }

      // $FF: synthetic method
      AvatarCandidatesRowMapper(Object x0) {
         this();
      }
   }
}
