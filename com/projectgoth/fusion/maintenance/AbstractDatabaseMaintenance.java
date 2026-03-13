package com.projectgoth.fusion.maintenance;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public abstract class AbstractDatabaseMaintenance {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AbstractDatabaseMaintenance.class));
   protected Properties databaseProperties;
   protected DataSource misDataSource;
   protected DataSource masterDataSource;
   protected DataSource slaveDataSource;

   protected void loadProperties() throws IOException {
      this.databaseProperties = new Properties();
      this.databaseProperties.load(new FileInputStream(ConfigUtils.getConfigDirectory() + "database.properties"));
   }

   protected void configureDataSourceDefaults(ComboPooledDataSource dataSource) {
      try {
         dataSource.setDriverClass(this.databaseProperties.getProperty("database.driver"));
         dataSource.setMinPoolSize(1);
         dataSource.setAcquireIncrement(1);
         dataSource.setAcquireRetryAttempts(2);
         dataSource.setMaxPoolSize(1);
      } catch (PropertyVetoException var3) {
         log.fatal("failed to setup driver class", var3);
         System.exit(1);
      }

   }

   protected void configureDataSources() {
      this.misDataSource = this.configureMisDataSource();
      this.configureDataSourceDefaults((ComboPooledDataSource)this.misDataSource);
      this.masterDataSource = this.configureMasterDataSource();
      this.configureDataSourceDefaults((ComboPooledDataSource)this.masterDataSource);
      this.slaveDataSource = this.configureSlaveDataSource();
      this.configureDataSourceDefaults((ComboPooledDataSource)this.slaveDataSource);
   }

   protected DataSource configureMisDataSource() {
      ComboPooledDataSource datasource = new ComboPooledDataSource();

      try {
         log.info("mis jdbc url: " + this.databaseProperties.getProperty("mis.database.jdbcUrl"));
         datasource.setJdbcUrl(this.databaseProperties.getProperty("mis.database.jdbcUrl"));
         datasource.setUser(this.databaseProperties.getProperty("mis.database.username"));
         datasource.setPassword(this.databaseProperties.getProperty("mis.database.password"));
         return datasource;
      } catch (Exception var3) {
         log.fatal("failed to setup mis datasource", var3);
         System.exit(1);
         return null;
      }
   }

   protected DataSource configureMasterDataSource() {
      ComboPooledDataSource datasource = new ComboPooledDataSource();

      try {
         log.info("master jdbc url: " + this.databaseProperties.getProperty("database.jdbcUrl"));
         datasource.setJdbcUrl(this.databaseProperties.getProperty("database.jdbcUrl"));
         datasource.setUser(this.databaseProperties.getProperty("database.username"));
         datasource.setPassword(this.databaseProperties.getProperty("database.password"));
         return datasource;
      } catch (Exception var3) {
         log.fatal("failed to setup master datasource", var3);
         System.exit(1);
         return null;
      }
   }

   protected DataSource configureSlaveDataSource() {
      ComboPooledDataSource datasource = new ComboPooledDataSource();

      try {
         log.info("Slave jdbc url: " + this.databaseProperties.getProperty("slaveVIP.database.jdbcUrl"));
         datasource.setJdbcUrl(this.databaseProperties.getProperty("slaveVIP.database.jdbcUrl"));
         datasource.setUser(this.databaseProperties.getProperty("slaveVIP.database.username"));
         datasource.setPassword(this.databaseProperties.getProperty("slaveVIP.database.password"));
         return datasource;
      } catch (Exception var3) {
         log.fatal("failed to setup slave datasource", var3);
         System.exit(1);
         return null;
      }
   }

   protected Set<String> loadStringListForUser(String username, String field, String tableName, Connection connection) throws SQLException {
      Set<String> set = new HashSet();
      PreparedStatement preparedStatement = connection.prepareStatement("select " + field + " from " + tableName + " where username = ?");
      preparedStatement.setString(1, username);
      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
         do {
            set.add(resultSet.getString(field));
         } while(resultSet.next());
      }

      resultSet.close();
      preparedStatement.close();
      return set;
   }
}
