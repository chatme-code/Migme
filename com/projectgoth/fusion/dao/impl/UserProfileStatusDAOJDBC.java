package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.dao.UserProfileStatusDAO;
import com.projectgoth.fusion.data.UserProfileData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class UserProfileStatusDAOJDBC extends MigJdbcDaoSupport implements UserProfileStatusDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UserProfileStatusDAOJDBC.class));

   public UserProfileData.StatusEnum getUserProfileStatus(String username) {
      try {
         UserProfileData.StatusEnum status = (UserProfileData.StatusEnum)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE_STATUS, username);
         if (status != null) {
            this.log.debug("found a UserProfileStatus in memcached");
            return status;
         }

         status = this.loadUserProfileStatusFromDB(username);
         if (status != null) {
            this.log.debug("found a UserProfileStatus in the DB");
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.USER_PROFILE_STATUS, username, status);
            return status;
         }
      } catch (Exception var3) {
         this.log.error("failed to load the UserProfileStatus", var3);
      }

      return UserProfileData.StatusEnum.PUBLIC;
   }

   private UserProfileData.StatusEnum loadUserProfileStatusFromDB(String username) {
      List<UserProfileData.StatusEnum> status = this.getJdbcTemplate().query(this.getExternalizedQuery("UserProfileStatusDAO.getUserProfileStatus"), new Object[]{username}, new UserProfileStatusDAOJDBC.UserProfileStatusRowMapper());
      return !status.isEmpty() ? (UserProfileData.StatusEnum)status.get(0) : null;
   }

   public static final class UserProfileStatusRowMapper implements RowMapper {
      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return UserProfileData.StatusEnum.fromValue(rs.getInt(1));
      }
   }
}
