package com.projectgoth.fusion.reputation;

import com.danga.MemCached.MemCachedClient;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.reputation.cache.ReputationLastRan;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class GatherRawData {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GatherRawData.class));
   public static MemCachedClient memCached;
   private Properties databaseProperties;
   private DataSource repDataSource;
   private DataSource repODSDataSource;
   private String outfileDirectory;
   private Date endOfPeriodObserving;

   public GatherRawData(DataSource repDataSource, DataSource repODSDataSource, Date endOfPeriodObserving) throws IOException {
      this.repDataSource = repDataSource;
      this.repODSDataSource = repODSDataSource;
      this.endOfPeriodObserving = endOfPeriodObserving;
      this.outfileDirectory = "/reputation/dump/";
   }

   public String gather(String runDateString) throws Exception {
      log.info("gathering data for period ending " + this.endOfPeriodObserving + " with time marker " + runDateString);

      try {
         long start = System.currentTimeMillis();
         log.info("dumping " + ReputationLastRan.getSessionArchiveTableName(memCached) + " since " + ReputationLastRan.getSessionArchiveLastId(memCached));
         this.dumpSessionArchiveData(this.outfileDirectory, "sessionarchive." + runDateString + ".csv", ReputationLastRan.getSessionArchiveLastId(memCached));
         log.info("dumping accountentry for date " + runDateString + " since " + ReputationLastRan.getAccountEntryLastId(memCached));
         this.dumpAccountEntryData(this.outfileDirectory, "accountentry." + runDateString + ".csv", ReputationLastRan.getAccountEntryLastId(memCached));
         log.info("dumping virtualgiftreceived for date " + runDateString + " since " + ReputationLastRan.getVirtualGiftLastId(memCached));
         this.dumpVirtualGiftReceivedData(this.outfileDirectory, "virtualgiftreceived." + runDateString + ".csv", ReputationLastRan.getVirtualGiftLastId(memCached));
         log.info("dumping phonecall for date " + runDateString + " since " + ReputationLastRan.getPhoneCallLastId(memCached));
         this.dumpPhoneCallData(this.outfileDirectory, "phonecall." + runDateString + ".csv", ReputationLastRan.getPhoneCallLastId(memCached));
         log.info("data gathering completed in " + (System.currentTimeMillis() - start) / 1000L + " seconds");
         return runDateString;
      } catch (Exception var4) {
         throw var4;
      }
   }

   protected void dumpAccountEntryData(String directory, String filename, int sinceId) throws SQLException {
      Connection connection = null;

      try {
         connection = this.repDataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("select id, username, datecreated, type, reference, replace(description, '\r\n', ''), amount, fundedamount, tax, costofgoodssold, costoftrial, currency, exchangerate from accountentry where id > ? and datecreated < ? into outfile '" + directory + filename + "' FIELDS TERMINATED BY '|' LINES TERMINATED BY '\n'");
         statement.setInt(1, sinceId);
         statement.setDate(2, new java.sql.Date(this.endOfPeriodObserving.getTime()));
         statement.execute();
      } finally {
         if (connection != null) {
            connection.close();
         }

      }

   }

   protected void dumpVirtualGiftReceivedData(String directory, String filename, int sinceId) throws SQLException {
      Connection connection = null;

      try {
         connection = this.repDataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("select id,username,unix_timestamp(datecreated),purchaselocation,virtualgiftid,sender,private,removed from virtualgiftreceived where id > ? and datecreated < ? into outfile '" + directory + filename + "' FIELDS TERMINATED BY '|' LINES TERMINATED BY '\n'");
         statement.setInt(1, sinceId);
         statement.setDate(2, new java.sql.Date(this.endOfPeriodObserving.getTime()));
         statement.execute();
      } finally {
         if (connection != null) {
            connection.close();
         }

      }

   }

   protected void dumpPhoneCallData(String directory, String filename, int sinceId) throws SQLException {
      Connection connection = null;

      try {
         connection = this.repDataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("select id,username,unix_timestamp(datecreated),destinationduration from phonecall where status = 2 and id > ? and datecreated < ? into outfile '" + directory + filename + "' FIELDS TERMINATED BY '|' LINES TERMINATED BY '\n'");
         statement.setInt(1, sinceId);
         statement.setDate(2, new java.sql.Date(this.endOfPeriodObserving.getTime()));
         statement.execute();
      } finally {
         if (connection != null) {
            connection.close();
         }

      }

   }

   protected void dumpSessionArchiveData(String directory, String filename, int sinceId) throws SQLException {
      Connection connection = null;

      try {
         connection = this.repODSDataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("select id,username,countryid,unix_timestamp(startdate),unix_timestamp(enddate),authenticated,devicetype,connectiontype,port,remoteport,remoteaddress,REPLACE(mobiledevice, '\n', ' ') mobiledevice,clientversion,uniqueUsersPrivateChattedWith,privatemessagessent,groupChatsEntered,groupmessagessent,chatroomMessagesSent,chatroomsEntered,uniqueChatroomsEntered,inviteByPhoneNumber,inviteByUsername,themeUpdated,statusMessagesSet,profileEdited,photosUploaded from " + ReputationLastRan.getSessionArchiveTableName(memCached) + " where id > ? and startDate < ? " + " into outfile '" + directory + filename + "' FIELDS TERMINATED BY '|' LINES TERMINATED BY '\n'");
         statement.setInt(1, sinceId);
         statement.setDate(2, new java.sql.Date(this.endOfPeriodObserving.getTime()));
         statement.execute();
      } finally {
         if (connection != null) {
            connection.close();
         }

      }

   }

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
      }

   }

   protected DataSource configureRepDataSource() {
      ComboPooledDataSource datasource = new ComboPooledDataSource();

      try {
         log.info("rep jdbc url: " + this.databaseProperties.getProperty("database.jdbcUrl"));
         datasource.setJdbcUrl(this.databaseProperties.getProperty("database.jdbcUrl"));
         datasource.setUser(this.databaseProperties.getProperty("database.username"));
         datasource.setPassword(this.databaseProperties.getProperty("database.password"));
         return datasource;
      } catch (Exception var3) {
         log.fatal("failed to setup rep datasource", var3);
         System.exit(1);
         return null;
      }
   }

   protected void configureDataSources() {
      this.repDataSource = this.configureRepDataSource();
      this.configureDataSourceDefaults((ComboPooledDataSource)this.repDataSource);
   }

   static {
      memCached = MemCachedUtils.getMemCachedClient(MemCachedUtils.Instance.broadcastList);
   }
}
