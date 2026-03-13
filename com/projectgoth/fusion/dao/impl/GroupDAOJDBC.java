package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.dao.GroupDAO;
import com.projectgoth.fusion.data.GroupData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class GroupDAOJDBC extends MigJdbcDaoSupport implements GroupDAO {
   private Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupDAOJDBC.class));

   public GroupData getGroup(int groupId) {
      GroupData group = (GroupData)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.GROUP, String.valueOf(groupId));
      if (group == null) {
         if (this.log.isDebugEnabled()) {
            this.log.debug("no group found in memcached for id [" + groupId + "]");
         }

         group = this.loadGroupFromDB(groupId);
         if (this.log.isDebugEnabled()) {
            this.log.debug("putting group found in memcached for id [" + groupId + "]");
         }

         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GROUP, String.valueOf(groupId), group);
      } else if (this.log.isDebugEnabled()) {
         this.log.debug("returning cached group for id [" + groupId + "]");
      }

      return group;
   }

   private GroupData loadGroupFromDB(int groupId) {
      GroupData group = (GroupData)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("GroupDAO.getGroup"), new Object[]{Integer.toString(groupId)}, new GroupDAOJDBC.GroupRowMapper());
      return group;
   }

   private static final class GroupRowMapper implements RowMapper {
      private GroupRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         GroupData group = new GroupData(rs);
         return group;
      }

      // $FF: synthetic method
      GroupRowMapper(Object x0) {
         this();
      }
   }
}
