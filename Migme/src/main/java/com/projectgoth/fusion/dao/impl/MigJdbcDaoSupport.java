package com.projectgoth.fusion.dao.impl;

import com.projectgoth.fusion.common.ExternalizedQueries;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.PlatformTransactionManager;

public class MigJdbcDaoSupport extends JdbcDaoSupport {
   private ExternalizedQueries queries;
   private JdbcTemplate masterTemplate;
   private PlatformTransactionManager masterTransactionManager;

   public final void setMasterDataSource(DataSource dataSource) {
      if (this.masterTemplate == null || dataSource != this.masterTemplate.getDataSource()) {
         this.masterTemplate = this.createJdbcTemplate(dataSource);
      }

   }

   public JdbcTemplate getMasterTemplate() {
      return this.masterTemplate;
   }

   public PlatformTransactionManager getMasterTransactionManager() {
      return this.masterTransactionManager;
   }

   public void setMasterTransactionManager(PlatformTransactionManager transactionManager) {
      this.masterTransactionManager = transactionManager;
   }

   @Required
   public void setQueries(ExternalizedQueries queries) {
      this.queries = queries;
   }

   public String getExternalizedQuery(String name) {
      return this.queries.getQuery(name);
   }

   protected String preparedStatementParameters(int count) {
      StringBuffer buffer = new StringBuffer("(");

      for(int i = 0; i < count; ++i) {
         if (i < count - 1) {
            buffer.append("?,");
         } else {
            buffer.append("?");
         }
      }

      buffer.append(")");
      return buffer.toString();
   }

   protected static final class IntegerRowMapper implements RowMapper {
      private int column;

      public IntegerRowMapper(int column) {
         this.column = column;
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return rs.getInt(this.column);
      }
   }

   protected static final class BooleanRowMapper implements RowMapper {
      private int column;

      public BooleanRowMapper(int column) {
         this.column = column;
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return rs.getBoolean(this.column);
      }
   }

   protected static final class ByteRowMapper implements RowMapper {
      private int column;

      public ByteRowMapper(int column) {
         this.column = column;
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return rs.getByte(this.column);
      }
   }

   protected static final class StringRowMapper implements RowMapper {
      private String match;

      public StringRowMapper(String match) {
         this.match = match;
      }

      public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
         return rs.getString(this.match);
      }
   }
}
