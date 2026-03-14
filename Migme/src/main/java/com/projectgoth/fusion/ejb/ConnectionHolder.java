package com.projectgoth.fusion.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionHolder {
   private DataSource dataSource;
   private Connection conn;
   private boolean created;
   private boolean closed;

   public ConnectionHolder(DataSource dataSource, Connection conn) {
      this.dataSource = dataSource;
      this.conn = conn;
   }

   public void close() throws SQLException {
      try {
         if (this.created && this.conn != null) {
            this.conn.close();
         }
      } finally {
         this.conn = null;
         this.closed = true;
      }

   }

   public Connection getConnection() throws SQLException {
      if (this.closed) {
         throw new IllegalStateException();
      } else {
         if (this.conn == null) {
            this.conn = this.dataSource.getConnection();
            this.created = true;
         }

         return this.conn;
      }
   }
}
