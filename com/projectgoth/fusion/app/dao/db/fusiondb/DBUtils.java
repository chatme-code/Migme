package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.app.dao.config.FusionBooleanConfigurationValue;
import com.projectgoth.fusion.app.dao.config.FusionConfigEnum;
import com.projectgoth.fusion.app.dao.config.FusionIntConfigurationValue;
import com.projectgoth.fusion.app.dao.config.FusionStringConfigurationValue;
import com.projectgoth.fusion.common.ConfigUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

public class DBUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DBUtils.class));

   public static Connection getFusionReadConnection() throws SQLException {
      return DBUtils.DataSourceEnum.FUSION_READ.dataSource.getConnection();
   }

   public static Connection getOlapReadConnection() throws SQLException {
      return DBUtils.DataSourceEnum.OLAP_READ.dataSource.getConnection();
   }

   public static Connection getFusionWriteConnection() throws SQLException {
      return DBUtils.DataSourceEnum.FUSION_WRITE.dataSource.getConnection();
   }

   public static void closeResource(ResultSet rs, Statement ps, Connection conn, Logger log) {
      try {
         if (rs != null) {
            rs.close();
         }
      } catch (SQLException var7) {
         rs = null;
         log.warn("Failed to close ResultSet", var7);
      }

      try {
         if (ps != null) {
            ps.close();
         }
      } catch (SQLException var6) {
         ps = null;
         log.warn("Failed to close (Prepared)Statement", var6);
      }

      try {
         if (conn != null) {
            conn.close();
         }
      } catch (SQLException var5) {
         conn = null;
         log.warn("Failed to close Connection", var5);
      }

   }

   private static void registerShutdownHook() {
      Runtime.getRuntime().addShutdownHook(new Thread() {
         public void run() {
            DBUtils.log.info("shutdown c3p0 connection pool");
            DBUtils.DataSourceEnum[] arr$ = DBUtils.DataSourceEnum.values();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               DBUtils.DataSourceEnum datasource = arr$[i$];

               try {
                  datasource.dataSource.close();
                  datasource.dataSource = null;
               } catch (Throwable var6) {
                  DBUtils.log.error(String.format("Failed to shutdown %s in shutdown hook, ignoring", datasource), var6);
               }
            }

         }
      });
   }

   static {
      registerShutdownHook();
   }

   public static enum DataSourceEnum {
      OLAP_READ(FusionConfigEnum.OLAP_DB_READ),
      FUSION_READ(FusionConfigEnum.FUSION_DB_READ),
      FUSION_WRITE(FusionConfigEnum.FUSION_DB_WRITE);

      private String driverClass;
      private String user;
      private String password;
      private String jdbcUrl;
      private int initialPoolSize;
      private int minPoolSize;
      private int maxPoolSize;
      private int maxIdleTime;
      private int idleTestPeriod;
      private int timeout;
      private boolean validate;
      private String preferredTestQuery;
      private int maxConnectionAge;
      private boolean testConnectionOnCheckin;
      private boolean testConnectionOnCheckout;
      private int acquireRetryDelay;
      private int acquireRetryAttempts;
      private int acquireIncrement;
      private boolean breakAfterAcquireFailure;
      private ComboPooledDataSource dataSource;

      private DataSourceEnum(FusionConfigEnum configEnum) {
         this.driverClass = (new FusionStringConfigurationValue(configEnum, "c3p0.driverClass", "com.mysql.jdbc.Driver")).get();
         this.user = (new FusionStringConfigurationValue(configEnum, "c3p0.user", "fusion")).get();
         this.password = (new FusionStringConfigurationValue(configEnum, "c3p0.password", "abalone5KG")).get();
         this.jdbcUrl = (new FusionStringConfigurationValue(configEnum, "c3p0.jdbcUrl", "jdbc:mysql://localhost:3306/fusion")).get();
         this.initialPoolSize = (new FusionIntConfigurationValue(configEnum, "c3p0.initialPoolSize", 3)).get();
         this.minPoolSize = (new FusionIntConfigurationValue(configEnum, "c3p0.minPoolSize", 3)).get();
         this.maxPoolSize = (new FusionIntConfigurationValue(configEnum, "c3p0.maxPoolSize", 10)).get();
         this.maxIdleTime = (new FusionIntConfigurationValue(configEnum, "c3p0.maxIdleTime", 20)).get();
         this.idleTestPeriod = (new FusionIntConfigurationValue(configEnum, "c3p0.idleTestPeriod", 3000)).get();
         this.preferredTestQuery = (new FusionStringConfigurationValue(configEnum, "c3p0.preferredTestQuery", "SELECT 1")).get();
         this.maxConnectionAge = (new FusionIntConfigurationValue(configEnum, "c3p0.maxConnectionAge", 3600)).get();
         this.testConnectionOnCheckin = (new FusionBooleanConfigurationValue(configEnum, "c3p0.testConnectionOnCheckin", true)).get();
         this.testConnectionOnCheckout = (new FusionBooleanConfigurationValue(configEnum, "c3p0.testConnectionOnCheckout", false)).get();
         this.acquireRetryDelay = (new FusionIntConfigurationValue(configEnum, "c3p0.acquireRetryDelay", 1000)).get();
         this.acquireRetryAttempts = (new FusionIntConfigurationValue(configEnum, "c3p0.acquireRetryAttempts", 30)).get();
         this.acquireIncrement = (new FusionIntConfigurationValue(configEnum, "c3p0.acquireIncrement", 1)).get();
         this.breakAfterAcquireFailure = (new FusionBooleanConfigurationValue(configEnum, "c3p0.breakAfterAcquireFailure", false)).get();
         this.timeout = (new FusionIntConfigurationValue(configEnum, "c3p0.timeout", 3500)).get();
         this.validate = (new FusionBooleanConfigurationValue(configEnum, "c3p0.validate", true)).get();
         if (DBUtils.log.isDebugEnabled()) {
            DBUtils.log.debug("Config:  " + configEnum.getIdentifier());
            DBUtils.log.debug(" URL:     " + this.jdbcUrl);
            DBUtils.log.debug(" Driver:  " + this.driverClass);
            DBUtils.log.debug(" User:    " + this.user);
            DBUtils.log.debug(" Password:\t" + this.password);
            DBUtils.log.debug(" InitialPoolSize\t" + this.initialPoolSize);
            DBUtils.log.debug(" MinPoolSize\t" + this.minPoolSize);
            DBUtils.log.debug(" MaxPoolSize\t" + this.maxPoolSize);
            DBUtils.log.debug(" MaxIdleTime: " + this.maxIdleTime);
            DBUtils.log.debug(" Timeout: " + this.timeout);
            DBUtils.log.debug(" Validate: " + this.validate);
            DBUtils.log.debug(" IdleTestPeriod: " + this.idleTestPeriod);
            DBUtils.log.debug(" PreferredTestQuery: " + this.preferredTestQuery);
            DBUtils.log.debug(" MaxConnectionAge: " + this.maxConnectionAge);
            DBUtils.log.debug(" testConnectionOnCheckin: " + this.testConnectionOnCheckin);
            DBUtils.log.debug(" testConnectionOnCheckout: " + this.testConnectionOnCheckout);
            DBUtils.log.debug(" acquireRetryDelay: " + this.acquireRetryDelay);
            DBUtils.log.debug(" acquireRetryAttempts: " + this.acquireRetryAttempts);
            DBUtils.log.debug(" acquireIncrement: " + this.acquireIncrement);
            DBUtils.log.debug(" breakAfterAcquireFailure: " + this.breakAfterAcquireFailure);
         }

         this.dataSource = new ComboPooledDataSource();

         try {
            this.dataSource.setDriverClass(this.driverClass);
            this.dataSource.setUser(this.user);
            this.dataSource.setPassword(this.password);
            this.dataSource.setJdbcUrl(this.jdbcUrl);
            this.dataSource.setInitialPoolSize(this.initialPoolSize);
            this.dataSource.setMinPoolSize(this.minPoolSize);
            this.dataSource.setMaxPoolSize(this.maxPoolSize);
            this.dataSource.setMaxIdleTime(this.maxIdleTime);
            this.dataSource.setIdleConnectionTestPeriod(this.idleTestPeriod);
            this.dataSource.setPreferredTestQuery(this.preferredTestQuery);
            this.dataSource.setMaxConnectionAge(this.maxConnectionAge);
            this.dataSource.setTestConnectionOnCheckin(this.testConnectionOnCheckin);
            this.dataSource.setTestConnectionOnCheckout(this.testConnectionOnCheckout);
            this.dataSource.setAcquireRetryDelay(this.acquireRetryDelay);
            this.dataSource.setAcquireRetryAttempts(this.acquireRetryAttempts);
            this.dataSource.setAcquireIncrement(this.acquireIncrement);
         } catch (Exception var5) {
            DBUtils.log.error("Failed to Create datasource from properties file [" + configEnum.getIdentifier() + "]. Eerror:" + var5, var5);
         }

      }
   }
}
