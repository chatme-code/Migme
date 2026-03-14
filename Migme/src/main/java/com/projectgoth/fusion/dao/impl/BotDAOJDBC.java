package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.dao.BotDAO;
import com.projectgoth.fusion.data.BotData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public class BotDAOJDBC extends MigJdbcDaoSupport implements BotDAO {
   public List<BotData> getBots() {
      try {
         return this.getJdbcTemplate().query(this.getExternalizedQuery("BotDAO.getBots"), new Object[0], new BotDAOJDBC.BotDataRowMapper());
      } catch (DataAccessException var2) {
         return null;
      }
   }

   public Map<String, String> getBotConfig(long botID) {
      final Map<String, String> config = new HashMap();
      this.getJdbcTemplate().query(this.getExternalizedQuery("BotDAO.getBotConfig"), new Object[]{botID}, new RowCallbackHandler() {
         public void processRow(ResultSet rs) throws SQLException {
            config.put(rs.getString("PropertyName"), rs.getString("PropertyValue"));
         }
      });
      return config;
   }

   public Map<String, String> getBotCommands(long botID, String languageCode) {
      final Map<String, String> commands = new HashMap();
      this.getJdbcTemplate().query(this.getExternalizedQuery("BotDAO.getBotCommandsForLanguage"), new Object[]{botID, languageCode}, new RowCallbackHandler() {
         public void processRow(ResultSet rs) throws SQLException {
            commands.put(rs.getString("CommandKey"), rs.getString("CommandValue"));
         }
      });
      return commands;
   }

   public Map<String, String> getBotMessages(long botID, String languageCode) {
      final Map<String, String> messages = new HashMap();
      this.getJdbcTemplate().query(this.getExternalizedQuery("BotDAO.getBotMessagesForLanguage"), new Object[]{botID, languageCode}, new RowCallbackHandler() {
         public void processRow(ResultSet rs) throws SQLException {
            messages.put(rs.getString("MessageKey"), rs.getString("MessageValue"));
         }
      });
      return messages;
   }

   private static final class BotDataRowMapper implements RowMapper {
      private BotDataRowMapper() {
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return new BotData(rs);
      }

      // $FF: synthetic method
      BotDataRowMapper(Object x0) {
         this();
      }
   }
}
