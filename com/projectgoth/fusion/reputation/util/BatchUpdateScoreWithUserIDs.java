package com.projectgoth.fusion.reputation.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.reputation.UpdateScoreTable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

public class BatchUpdateScoreWithUserIDs {
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

   public static void main(String[] args) throws Exception {
      if (args.length < 1) {
         System.err.println("Usage BatchUpdateScore <filename> <level>");
         System.exit(1);
      }

      DOMConfigurator.configureAndWatch("log4j.xml");
      String filename = args[0];
      int levelToAssign = Integer.parseInt(args[1]);
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      BufferedReader reader = new BufferedReader(new FileReader(directoryHolder.getDataDirectory() + filename));
      DataSource masterDataSource = getMasterDataSource();
      Connection masterConnection = null;
      StopWatch sw = new StopWatch();
      String line = null;
      log.info("partition");
      sw.start("starting partition");
      int lineNumber = 0;
      int updated = 0;
      int inserted = 0;

      try {
         masterConnection = masterDataSource.getConnection();
         PreparedStatement updateScoreStatement = masterConnection.prepareStatement("update score set score = ? where userid = ?");
         PreparedStatement insertScoreStatement = masterConnection.prepareStatement("insert into score (userid, score) values (?,?)");
         SortedMap<Integer, Integer> levelTable = readLevelTable(masterConnection);
         Iterator i$ = levelTable.keySet().iterator();

         label94:
         while(true) {
            if (!i$.hasNext()) {
               int score = getScoreForLevel(levelToAssign, levelTable) - 510;
               System.out.println("setting score to " + score);

               while(true) {
                  if ((line = reader.readLine()) == null) {
                     break label94;
                  }

                  ++lineNumber;
                  int userid = Integer.parseInt(CSVUtils.getColumnFromLine(line, 0, ','));
                  if (lineNumber % 5000 == 0) {
                     System.out.println(lineNumber);
                  }

                  updateScoreStatement.setInt(1, score);
                  updateScoreStatement.setInt(2, userid);
                  int rows = updateScoreStatement.executeUpdate();
                  if (rows == 0) {
                     insertScoreStatement.setInt(1, userid);
                     insertScoreStatement.setInt(2, score);
                     insertScoreStatement.executeUpdate();
                     ++inserted;
                  } else {
                     ++updated;
                  }
               }
            }

            Integer key = (Integer)i$.next();
            log.info(key + " -> " + levelTable.get(key));
         }
      } finally {
         if (masterConnection != null) {
            masterConnection.close();
         }

      }

      sw.stop();
      log.info("finished with batch, updated [" + updated + "] inserted [" + inserted + "]");
   }
}
