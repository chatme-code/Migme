package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.dao.GroupAnnouncementDAO;
import com.projectgoth.fusion.data.GroupAnnouncementData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class GroupAnnouncementDAOJDBC extends MigJdbcDaoSupport implements GroupAnnouncementDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupAnnouncementDAOJDBC.class));

   public GroupAnnouncementData getGroupAnnouncement(int groupAnnouncementId) {
      GroupAnnouncementData groupAnnouncement = (GroupAnnouncementData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.GROUP_ANNOUNCEMENT, String.valueOf(groupAnnouncementId));
      if (groupAnnouncement == null) {
         if (this.log.isDebugEnabled()) {
            this.log.debug("no group announcement found in memcached for id [" + groupAnnouncementId + "]");
         }

         groupAnnouncement = this.loadGroupAnnouncementFromDB(groupAnnouncementId);
         if (this.log.isDebugEnabled()) {
            this.log.debug("putting group announcement found in memcached for id [" + groupAnnouncementId + "]");
         }

         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GROUP_ANNOUNCEMENT, String.valueOf(groupAnnouncementId), groupAnnouncement);
      } else if (this.log.isDebugEnabled()) {
         this.log.debug("returning cached group announcement for id [" + groupAnnouncementId + "]");
      }

      return groupAnnouncement;
   }

   private GroupAnnouncementData loadGroupAnnouncementFromDB(int groupAnnouncementId) {
      GroupAnnouncementData groupAnnouncement = (GroupAnnouncementData)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("GroupAnnouncementDAO.getGroupAnnouncement"), new Object[]{Integer.toString(groupAnnouncementId)}, new GroupAnnouncementDAOJDBC.GroupAnnouncementRowMapper());
      return groupAnnouncement;
   }

   private static final class GroupAnnouncementRowMapper implements RowMapper {
      private GroupAnnouncementRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         GroupAnnouncementData group = new GroupAnnouncementData(rs);
         return group;
      }

      // $FF: synthetic method
      GroupAnnouncementRowMapper(Object x0) {
         this();
      }
   }
}
