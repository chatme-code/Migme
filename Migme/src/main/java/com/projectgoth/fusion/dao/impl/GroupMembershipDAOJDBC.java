package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.dao.GroupMembershipDAO;
import com.projectgoth.fusion.domain.UsernameAndMobileNumber;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;

public class GroupMembershipDAOJDBC extends MigJdbcDaoSupport implements GroupMembershipDAO {
   public List<String> getGroupMemberUsernamesForEmailNotification(int groupId) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("GroupMembershipDAO.getGroupMemberUsernamesForEmailNotification"), new Object[]{groupId}, new MigJdbcDaoSupport.StringRowMapper("username"));
   }

   public List<String> getGroupMemberUsernamesForEventNotification(int groupId) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("GroupMembershipDAO.getGroupMemberUsernamesForEventNotification"), new Object[]{groupId}, new MigJdbcDaoSupport.StringRowMapper("username"));
   }

   public List<UsernameAndMobileNumber> getGroupMemberUsernamesAndMobileNumbersForSMSNotification(int groupId) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("GroupMembershipDAO.getGroupMemberUsernamesAndMobileNumbersForSMSNotification"), new Object[]{groupId}, new GroupMembershipDAOJDBC.UsernameAndMobileNumberRowMapper());
   }

   public List<UsernameAndMobileNumber> getGroupMemberUsernamesAndMobileNumbersForGroupEventSMSNotification(int groupId) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("GroupMembershipDAO.getGroupMemberUsernamesAndMobileNumbersForGroupEventSMSNotification"), new Object[]{groupId}, new GroupMembershipDAOJDBC.UsernameAndMobileNumberRowMapper());
   }

   public List<String> getGroupMemberUsernamesForGroupEventNotification(int groupId) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("GroupMembershipDAO.getGroupMemberUsernamesForGroupEventNotification"), new Object[]{groupId}, new MigJdbcDaoSupport.StringRowMapper("username"));
   }

   public List<String> getGroupMemberUsernamesForNewGroupUserPostNotificationViaEmail(int userPostId) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("GroupMembershipDAO.getGroupMemberUsernamesForNewGroupUserPostNotificationViaEmail"), new Object[]{userPostId}, new MigJdbcDaoSupport.StringRowMapper("username"));
   }

   public List<String> getGroupMemberUsernamesForNewGroupUserPostNotificationViaEventSystem(int userPostId) {
      return this.getJdbcTemplate().query(this.getExternalizedQuery("GroupMembershipDAO.getGroupMemberUsernamesForNewGroupUserPostNotificationViaEventSystem"), new Object[]{userPostId}, new MigJdbcDaoSupport.StringRowMapper("username"));
   }

   private static final class UsernameAndMobileNumberRowMapper implements RowMapper {
      private UsernameAndMobileNumberRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         UsernameAndMobileNumber uam = new UsernameAndMobileNumber(rs.getString("username"), rs.getString("mobilephone"));
         return uam;
      }

      // $FF: synthetic method
      UsernameAndMobileNumberRowMapper(Object x0) {
         this();
      }
   }
}
