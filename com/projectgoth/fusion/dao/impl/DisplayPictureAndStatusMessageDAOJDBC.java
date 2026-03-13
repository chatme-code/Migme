package com.projectgoth.fusion.dao.impl;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.dao.DisplayPictureAndStatusMessageDAO;
import com.projectgoth.fusion.data.DisplayPictureAndStatusMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class DisplayPictureAndStatusMessageDAOJDBC extends MigJdbcDaoSupport implements DisplayPictureAndStatusMessageDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DisplayPictureAndStatusMessageDAOJDBC.class));
   private static MemCachedClient displayPictureAndStatusMessageMemcache;

   public DisplayPictureAndStatusMessage getDisplayPictureAndStatusMessage(String username) {
      try {
         DisplayPictureAndStatusMessage displayPictureAndStatusMessage = DisplayPictureAndStatusMessage.getDisplayPictureAndStatusMessage(displayPictureAndStatusMessageMemcache, username);
         if (displayPictureAndStatusMessage != null) {
            this.log.debug("found a displayPictureAndStatusMessage in memcached");
            return displayPictureAndStatusMessage;
         }

         displayPictureAndStatusMessage = this.loadDisplayPictureAndStatusMessageFromDB(username);
         if (displayPictureAndStatusMessage != null) {
            this.log.debug("found a displayPictureAndStatusMessage in the DB");
            DisplayPictureAndStatusMessage.setDisplayPictureAndStatusMessage(displayPictureAndStatusMessageMemcache, username, displayPictureAndStatusMessage);
            return displayPictureAndStatusMessage;
         }
      } catch (Exception var3) {
         this.log.error("failed to load the DisplayPictureAndStatusMessage", var3);
      }

      return null;
   }

   public String getGroupDisplayPicture(int groupId) {
      Object result = this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("DisplayPictureAndStatusMessageDAO.getGroupDisplayPicture"), new Object[]{groupId}, String.class);
      return result != null ? (String)result : null;
   }

   private DisplayPictureAndStatusMessage loadDisplayPictureAndStatusMessageFromDB(String username) {
      List<DisplayPictureAndStatusMessage> displayPictureAndStatusMessage = this.getJdbcTemplate().query(this.getExternalizedQuery("DisplayPictureAndStatusMessageDAO.getDisplayPictureAndStatusMessage"), new Object[]{username}, new DisplayPictureAndStatusMessageDAOJDBC.DisplayPictureAndStatusMessageRowMapper());
      return !displayPictureAndStatusMessage.isEmpty() ? (DisplayPictureAndStatusMessage)displayPictureAndStatusMessage.get(0) : null;
   }

   static {
      displayPictureAndStatusMessageMemcache = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.userDisplayPictureAndStatus);
   }

   private static final class DisplayPictureAndStatusMessageRowMapper implements RowMapper {
      private DisplayPictureAndStatusMessageRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         DisplayPictureAndStatusMessage displayPictureAndStatusMessage = new DisplayPictureAndStatusMessage();
         displayPictureAndStatusMessage.setDisplayPicture(rs.getString("displayPicture"));
         displayPictureAndStatusMessage.setStatusMessage(rs.getString("statusMessage"));

         try {
            displayPictureAndStatusMessage.setStatusTimestamp(rs.getTimestamp("statusTimeStamp"));
         } catch (Exception var5) {
            displayPictureAndStatusMessage.setStatusTimestamp(new Date(0L));
         }

         return displayPictureAndStatusMessage;
      }

      // $FF: synthetic method
      DisplayPictureAndStatusMessageRowMapper(Object x0) {
         this();
      }
   }
}
