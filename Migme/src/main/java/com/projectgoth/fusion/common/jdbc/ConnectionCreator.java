package com.projectgoth.fusion.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public interface ConnectionCreator {
   Connection create() throws SQLException;

   public static class FromDataSource implements ConnectionCreator {
      private final DataSource ds;

      public FromDataSource(DataSource ds) {
         this.ds = ds;
      }

      public Connection create() throws SQLException {
         return this.ds.getConnection();
      }
   }
}
