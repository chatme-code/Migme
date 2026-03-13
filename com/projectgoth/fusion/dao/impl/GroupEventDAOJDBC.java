package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.dao.GroupEventDAO;
import com.projectgoth.fusion.data.GroupEventData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

public class GroupEventDAOJDBC extends MigJdbcDaoSupport implements GroupEventDAO {
   public GroupEventData getGroupEvent(int groupId) {
      try {
         return (GroupEventData)this.getJdbcTemplate().queryForObject(this.getExternalizedQuery("GroupEventDAO.getGroupEvent"), new Object[]{groupId}, new GroupEventDAOJDBC.GroupEventRowMapper());
      } catch (IncorrectResultSizeDataAccessException var3) {
         return null;
      }
   }

   public int persistGroupEvent(final GroupEventData groupEvent) {
      KeyHolder keyHolder = new GeneratedKeyHolder();
      this.getJdbcTemplate().update(new PreparedStatementCreator() {
         public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(GroupEventDAOJDBC.this.getExternalizedQuery("GroupEventDAO.persistGroupEvent"));
            ps.setInt(1, groupEvent.groupID);
            ps.setString(2, groupEvent.description);
            ps.setTimestamp(3, new Timestamp(groupEvent.startTime.getTime()));
            ps.setObject(4, groupEvent.durationMinutes == 0 ? null : groupEvent.durationMinutes);
            ps.setObject(5, StringUtils.hasLength(groupEvent.chatRoomName) ? groupEvent.chatRoomName : null);
            ps.setObject(6, groupEvent.chatRoomCategoryID == 0 ? null : groupEvent.chatRoomCategoryID);
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.setInt(8, groupEvent.status.value());
            return ps;
         }
      }, keyHolder);
      return keyHolder.getKey().intValue();
   }

   public int updateGroupEvent(final GroupEventData groupEvent) {
      return this.getJdbcTemplate().update(new PreparedStatementCreator() {
         public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(GroupEventDAOJDBC.this.getExternalizedQuery("GroupEventDAO.updateGroupEvent"));
            ps.setString(1, groupEvent.description);
            ps.setTimestamp(2, new Timestamp(groupEvent.startTime.getTime()));
            ps.setObject(3, groupEvent.durationMinutes == 0 ? null : groupEvent.durationMinutes);
            ps.setObject(4, StringUtils.hasLength(groupEvent.chatRoomName) ? groupEvent.chatRoomName : null);
            ps.setObject(5, groupEvent.chatRoomCategoryID == 0 ? null : groupEvent.chatRoomCategoryID);
            ps.setInt(6, groupEvent.status.value());
            ps.setInt(7, groupEvent.id);
            return ps;
         }
      });
   }

   public int updateGroupEventStatus(final int groupEventID, final int status) {
      return this.getJdbcTemplate().update(new PreparedStatementCreator() {
         public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            PreparedStatement ps = connection.prepareStatement(GroupEventDAOJDBC.this.getExternalizedQuery("GroupEventDAO.updateGroupEventStatus"));
            ps.setInt(1, status);
            ps.setInt(2, groupEventID);
            return ps;
         }
      });
   }

   private static final class GroupEventRowMapper implements RowMapper {
      private GroupEventRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         GroupEventData groupEventData = GroupEventData.fromResultSet(rs);
         return groupEventData;
      }

      // $FF: synthetic method
      GroupEventRowMapper(Object x0) {
         this();
      }
   }
}
