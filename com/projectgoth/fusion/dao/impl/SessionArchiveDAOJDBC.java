package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.dao.SessionArchiveDAO;
import com.projectgoth.fusion.restapi.data.SSOSessionMetrics;
import com.projectgoth.fusion.sessioncache.ArchiveSSOSessionsTask;
import com.projectgoth.fusion.sessioncache.SessionArchiveDetail;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

public class SessionArchiveDAOJDBC extends MigJdbcDaoSupport implements SessionArchiveDAO {
   private Logger log = Logger.getLogger(SessionArchiveDAOJDBC.class);
   private DailyVariableTableName archiveTableName;

   @Required
   public synchronized void setArchiveTableName(DailyVariableTableName archiveTableName) {
      this.archiveTableName = archiveTableName;
   }

   public String getInsertQuery() {
      StringBuffer buffer = new StringBuffer("insert into ");
      buffer.append(this.archiveTableName.getTablename());
      buffer.append(" (userid, username, countryID, startdate, enddate, authenticated, deviceType, connectionType, clientVersion, port, remotePort, remoteAddress, mobileDevice,migLevelScore, migLevel, uniqueUsersPrivateChattedWith, privateMessagesSent, groupMessagesSent, groupChatsEntered, chatroomMessagesSent, chatroomsEntered,uniqueChatroomsEntered, inviteByPhoneNumber, inviteByUsername, themeUpdated, statusMessagesSet, profileEdited, photosUploaded, language) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      if (this.log.isDebugEnabled()) {
         this.log.debug(buffer.toString());
      }

      return buffer.toString();
   }

   public void archiveSession(SessionArchiveDetail session) {
      this.getJdbcTemplate().update(this.getInsertQuery(), new Object[]{session.getUsername(), session.getSourceCountryID(), session.getStartDateTime(), session.getEndDateTime(), session.isAuthenticated(), session.getDeviceType().value(), session.getConnectionType().value(), session.getClientVersion(), session.getPort(), session.getRemotePort(), session.getRemoteAddress(), session.getMobileDevice(), session.getSessionMetrics().uniqueUsersPrivateChattedWith, session.getSessionMetrics().privateMessagesSent, session.getSessionMetrics().groupMessagesSent, session.getSessionMetrics().groupChatsEntered, session.getSessionMetrics().chatroomMessagesSent, session.getSessionMetrics().chatroomsEntered, session.getSessionMetrics().uniqueChatroomsEntered, session.getSessionMetrics().inviteByPhoneNumber, session.getSessionMetrics().inviteByUsername, session.getSessionMetrics().themeUpdated, session.getSessionMetrics().statusMessagesSet, session.getSessionMetrics().profileEdited, session.getSessionMetrics().photosUploaded, session.getLanguage()});
   }

   public void bulkArchiveSession(final List<SessionArchiveDetail> sessions) {
      this.getJdbcTemplate().batchUpdate(this.getInsertQuery(), new BatchPreparedStatementSetter() {
         public int getBatchSize() {
            return sessions.size();
         }

         public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setInt(1, ((SessionArchiveDetail)sessions.get(i)).getUserID());
            ps.setString(2, ((SessionArchiveDetail)sessions.get(i)).getUsername());
            ps.setInt(3, ((SessionArchiveDetail)sessions.get(i)).getSourceCountryID());
            ps.setTimestamp(4, new Timestamp(((SessionArchiveDetail)sessions.get(i)).getStartDateTime().getTime()));
            ps.setTimestamp(5, new Timestamp(((SessionArchiveDetail)sessions.get(i)).getEndDateTime().getTime()));
            ps.setBoolean(6, ((SessionArchiveDetail)sessions.get(i)).isAuthenticated());
            if (((SessionArchiveDetail)sessions.get(i)).getDeviceType() == null) {
               ps.setNull(7, 4);
            } else {
               ps.setInt(7, ((SessionArchiveDetail)sessions.get(i)).getDeviceType().value());
            }

            ps.setInt(8, ((SessionArchiveDetail)sessions.get(i)).getConnectionType().value());
            ps.setShort(9, ((SessionArchiveDetail)sessions.get(i)).getClientVersion());
            ps.setInt(10, ((SessionArchiveDetail)sessions.get(i)).getPort());
            ps.setInt(11, ((SessionArchiveDetail)sessions.get(i)).getRemotePort());
            ps.setString(12, ((SessionArchiveDetail)sessions.get(i)).getRemoteAddress());
            ps.setString(13, ((SessionArchiveDetail)sessions.get(i)).getMobileDevice());
            ps.setInt(14, ((SessionArchiveDetail)sessions.get(i)).getMigLevelScore());
            ps.setInt(15, ((SessionArchiveDetail)sessions.get(i)).getMigLevel());
            ps.setShort(16, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().uniqueUsersPrivateChattedWith);
            ps.setShort(17, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().privateMessagesSent);
            ps.setShort(18, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().groupMessagesSent);
            ps.setShort(19, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().groupChatsEntered);
            ps.setShort(20, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().chatroomMessagesSent);
            ps.setShort(21, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().chatroomsEntered);
            ps.setShort(22, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().uniqueChatroomsEntered);
            ps.setShort(23, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().inviteByPhoneNumber);
            ps.setShort(24, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().inviteByUsername);
            ps.setShort(25, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().themeUpdated);
            ps.setShort(26, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().statusMessagesSet);
            ps.setShort(27, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().profileEdited);
            ps.setShort(28, ((SessionArchiveDetail)sessions.get(i)).getSessionMetrics().photosUploaded);
            ps.setString(29, ((SessionArchiveDetail)sessions.get(i)).getLanguage());
         }
      });
   }

   public void bulkArchiveSSOSessionMetrics(final List<SSOSessionMetrics> sessionMetrics) {
      String tableName = ArchiveSSOSessionsTask.getDailyTableName();
      StringBuffer insertQuery = (new StringBuffer()).append("INSERT INTO ").append(tableName).append(" (sessionid, view, startdate, enddate, jsondata) ").append(" VALUES ").append(" (?, ?, ?, ?, ?)");
      this.getJdbcTemplate().batchUpdate(insertQuery.toString(), new BatchPreparedStatementSetter() {
         public int getBatchSize() {
            return sessionMetrics.size();
         }

         public void setValues(PreparedStatement ps, int i) throws SQLException {
            ps.setString(1, ((SSOSessionMetrics)sessionMetrics.get(i)).getSessionID());
            ps.setString(2, ((SSOSessionMetrics)sessionMetrics.get(i)).getView().toString());
            ps.setTimestamp(3, new Timestamp(((SSOSessionMetrics)sessionMetrics.get(i)).getSessionStartTime() * 1000L));
            ps.setTimestamp(4, new Timestamp(((SSOSessionMetrics)sessionMetrics.get(i)).getTimestamp() * 1000L));
            ps.setString(5, ((SSOSessionMetrics)sessionMetrics.get(i)).toJSONString());
         }
      });
   }

   public void createSSOSessionMetricsTableIfNotExists() {
      String tableName = ArchiveSSOSessionsTask.getDailyTableName();
      StringBuffer createTableQuery = (new StringBuffer()).append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(id int(11) AUTO_INCREMENT NOT NULL, ").append("view varchar(30) NOT NULL, ").append("sessionid varchar(255) NOT NULL, ").append("startdate datetime NOT NULL, ").append("enddate datetime NOT NULL, ").append("jsondata TEXT, ").append("PRIMARY KEY(id) ) ").append("ENGINE=MyISAM DEFAULT CHARSET=utf8");
      this.getJdbcTemplate().execute(createTableQuery.toString());
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER68_ENABLED)) {
         tableName = ArchiveSSOSessionsTask.getNextDayDailyTableName();
         createTableQuery = (new StringBuffer()).append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(id int(11) AUTO_INCREMENT NOT NULL, ").append("view varchar(30) NOT NULL, ").append("sessionid varchar(255) NOT NULL, ").append("startdate datetime NOT NULL, ").append("enddate datetime NOT NULL, ").append("jsondata TEXT, ").append("PRIMARY KEY(id) ) ").append("ENGINE=MyISAM DEFAULT CHARSET=utf8");
         this.getJdbcTemplate().execute(createTableQuery.toString());
      }

   }
}
