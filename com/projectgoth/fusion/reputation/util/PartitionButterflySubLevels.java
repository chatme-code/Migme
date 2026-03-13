package com.projectgoth.fusion.reputation.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.reputation.UpdateScoreTable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.util.StopWatch;

public class PartitionButterflySubLevels {
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

   public static int getPartitionForLine(int lineNumber, SortedMap<Integer, Integer> brackets) {
      Iterator i$ = brackets.keySet().iterator();

      Integer key;
      do {
         if (!i$.hasNext()) {
            return (Integer)brackets.get(brackets.firstKey());
         }

         key = (Integer)i$.next();
      } while(lineNumber > key);

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
      BufferedWriter writer = new BufferedWriter(new FileWriter(directoryHolder.getDataDirectory() + "partitioned." + filename));
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
         SortedMap<Integer, Integer> brackets = new TreeMap();
         brackets.put((int)(0.4D * (double)numberOfUsers), getScoreForLevel(6, levelTable));
         brackets.put((int)(0.65D * (double)numberOfUsers), getScoreForLevel(7, levelTable));
         brackets.put((int)(0.8500000000000001D * (double)numberOfUsers), getScoreForLevel(8, levelTable));
         brackets.put((int)(0.9500000000000001D * (double)numberOfUsers), getScoreForLevel(9, levelTable));
         brackets.put((int)(1.0D * (double)numberOfUsers), getScoreForLevel(10, levelTable));
         log.info("brackets");
         Iterator i$ = brackets.keySet().iterator();

         while(i$.hasNext()) {
            Integer key = (Integer)i$.next();
            log.info(key + " -> " + brackets.get(key));
         }

         log.info("partition");
         sw.start("starting partition");
         int lineNumber = 0;

         while((line = reader.readLine()) != null) {
            username = CSVUtils.getColumnFromLine(line, 0, ',');
            ++lineNumber;
            writer.write(username);
            writer.write(",");
            writer.write(Integer.toString(getPartitionForLine(lineNumber, brackets)));
            writer.write(",");
            writer.newLine();
         }

         sw.stop();
         log.info("finished with score dump...");
      } finally {
         if (masterConnection != null) {
            masterConnection.close();
         }

         writer.close();
      }

   }
}
