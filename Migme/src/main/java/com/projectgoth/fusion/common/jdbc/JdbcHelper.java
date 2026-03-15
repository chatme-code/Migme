package com.projectgoth.fusion.common.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcHelper {
   public static void execQuery(Connection conn, String sql, JdbcHelper.ParameterSetup setup, JdbcHelper.ResultSetHandler rsHandler) throws Exception {
      PreparedStatement ps = conn.prepareStatement(sql);

      try {
         setup.setParamValues(ps);
         ResultSet rs = ps.executeQuery();

         try {
            rsHandler.handle(rs);
         } finally {
            rs.close();
         }
      } finally {
         ps.close();
      }

   }

   public static void execQuery(Connection conn, String sql, JdbcHelper.QueryHandler queryHandler) throws Exception {
      execQuery(conn, sql, queryHandler, queryHandler);
   }

   public static void execQuery(ConnectionCreator connCreator, String sql, JdbcHelper.QueryHandler queryHandler) throws Exception {
      Connection conn = connCreator.create();

      try {
         execQuery(conn, sql, queryHandler, queryHandler);
      } finally {
         conn.close();
      }

   }

   public abstract static class QueryHandlerAdapter implements JdbcHelper.QueryHandler {
      public void setParamValues(PreparedStatement preparedStatement) throws Exception {
      }
   }

   public interface QueryHandler extends JdbcHelper.ParameterSetup, JdbcHelper.ResultSetHandler {
   }

   public interface ParameterSetup {
      void setParamValues(PreparedStatement var1) throws Exception;
   }

   public interface ResultSetHandler {
      void handle(ResultSet var1) throws Exception;
   }
}
