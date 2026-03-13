package com.projectgoth.fusion.reputation.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.reputation.UpdateScoreTable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.util.StopWatch;

public class MoveButterflyUsersAccordingToRegistrationDate {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UpdateScoreTable.class));

   public static DataSource getMasterDataSource() throws Exception {
      Properties databaseProperties = new Properties();
      databaseProperties.load(new FileInputStream(System.getProperty("config.dir") + "database.properties"));
      ComboPooledDataSource datasource = new ComboPooledDataSource();
      log.info("rep jdbc url: " + databaseProperties.getProperty("database.jdbcUrl"));
      datasource.setJdbcUrl(databaseProperties.getProperty("database.jdbcUrl"));
      datasource.setUser(databaseProperties.getProperty("database.username"));
      datasource.setPassword(databaseProperties.getProperty("database.password"));
      datasource.setDriverClass(databaseProperties.getProperty("database.driver"));
      datasource.setMinPoolSize(1);
      datasource.setAcquireIncrement(1);
      datasource.setAcquireRetryAttempts(2);
      datasource.setMaxPoolSize(1);
      return datasource;
   }

   public static SortedMap<Integer, Integer> readLevelTable(Connection connection) throws SQLException {
      SortedMap<Integer, Integer> map = new TreeMap(Collections.reverseOrder());
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("select score,level from ReputationScoreToLevel order by score desc");

      while(rs.next()) {
         map.put(rs.getInt(1), rs.getInt(2));
      }

      return map;
   }

   public static int getUserLevel(int score, SortedMap<Integer, Integer> levelTable) {
      Iterator i$ = levelTable.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return 1;
         }

         key = (Integer)i$.next();
      } while(score < key);

      return (Integer)levelTable.get(key);
   }

   public static int getScoreForLevel(int level, SortedMap<Integer, Integer> levelTable) {
      Iterator i$ = levelTable.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return 0;
         }

         key = (Integer)i$.next();
      } while(level < (Integer)levelTable.get(key));

      return key;
   }

   public static int getScoreForDateRegistered(long dateRegistered, SortedMap<Long, Integer> brackets) {
      Iterator i$ = brackets.keySet().iterator();

      Long key;
      do {
         if (!i$.hasNext()) {
            return (Integer)brackets.get(brackets.lastKey());
         }

         key = (Long)i$.next();
      } while(dateRegistered > key);

      return (Integer)brackets.get(key);
   }

   public static void main(String[] args) throws Exception {
      if (args.length < 1) {
         System.err.println("Usage PopulateInitialSubLevels <filename>");
         System.exit(1);
      }

      DOMConfigurator.configureAndWatch("log4j.xml");
      String filename = args[0];
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      BufferedReader reader = new BufferedReader(new FileReader(directoryHolder.getDataDirectory() + filename));
      DataSource masterDataSource = getMasterDataSource();
      Connection masterConnection = null;
      StopWatch sw = new StopWatch();
      int numberOfUsers = 0;

      String line;
      for(line = null; reader.readLine() != null; ++numberOfUsers) {
      }

      reader.close();
      reader = new BufferedReader(new FileReader(directoryHolder.getDataDirectory() + filename));

      try {
         String username = null;
         masterConnection = masterDataSource.getConnection();
         SortedMap<Integer, Integer> levelTable = readLevelTable(masterConnection);
         Iterator i$ = levelTable.keySet().iterator();

         while(i$.hasNext()) {
            Integer key = (Integer)i$.next();
            log.info(key + " -> " + levelTable.get(key));
         }

         log.info(getScoreForLevel(6, levelTable));
         Calendar now = Calendar.getInstance();
         now.add(2, -10);
         SortedMap<Long, Integer> brackets = new TreeMap();
         brackets.put(now.getTimeInMillis() / 1000L, getScoreForLevel(9, levelTable));
         now.add(2, 1);
         brackets.put(now.getTimeInMillis() / 1000L, getScoreForLevel(8, levelTable));
         now.add(2, 2);
         brackets.put(now.getTimeInMillis() / 1000L, getScoreForLevel(7, levelTable));
         now.add(2, 2);
         brackets.put(now.getTimeInMillis() / 1000L, getScoreForLevel(6, levelTable));
         log.info("brackets");
         Iterator i$ = brackets.keySet().iterator();

         while(i$.hasNext()) {
            Long key = (Long)i$.next();
            log.info(new Date(key * 1000L) + " -> " + brackets.get(key));
         }

         log.info("partition");
         sw.start("starting partition");
         int lineNumber = 0;
         int updated = 0;
         byte inserted = 0;

         while((line = reader.readLine()) != null) {
            ++lineNumber;
            username = CSVUtils.getColumnFromLine(line, 0, '\t');
            long dateRegistered = Long.parseLong(CSVUtils.getColumnFromLine(line, 1, '\t'));
            System.out.println(username + " " + DateTimeUtils.getTimeSince(new Date(dateRegistered * 1000L)) + " -> " + getScoreForDateRegistered(dateRegistered, brackets));
         }

         sw.stop();
         log.info("finished with populating the birds, updated [" + updated + "] inserted [" + inserted + "]");
      } finally {
         if (masterConnection != null) {
            masterConnection.close();
         }

      }

   }
}
