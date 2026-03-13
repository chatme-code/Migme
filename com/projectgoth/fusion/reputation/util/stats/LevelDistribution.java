package com.projectgoth.fusion.reputation.util.stats;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.reputation.ReputationServiceI;
import com.projectgoth.fusion.reputation.UpdateScoreTable;
import com.projectgoth.fusion.reputation.util.LevelTable;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class LevelDistribution {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(UpdateScoreTable.class));
   private DataSource olapDataSource;
   private SortedMap<Integer, Integer> levelTable;

   public LevelDistribution(DataSource olapDataSource, SortedMap<Integer, Integer> levelTable) {
      this.olapDataSource = olapDataSource;
      this.levelTable = levelTable;
   }

   public SortedMap<Integer, Integer> getDistribution() throws SQLException {
      Connection olapConnection = null;

      try {
         olapConnection = this.olapDataSource.getConnection();
         PreparedStatement selectScoreLevelStatement = olapConnection.prepareStatement("select count(*) from score where score >= ? and score < ?");
         SortedMap<Integer, Integer> distribution = new TreeMap();
         Iterator i$ = this.levelTable.values().iterator();

         Integer level;
         while(i$.hasNext()) {
            level = (Integer)i$.next();
            int bottomBracket = LevelTable.getScoreForLevel(level, this.levelTable);
            int topBracket = LevelTable.getScoreForLevel(level + 1, this.levelTable);
            if (topBracket != bottomBracket) {
               selectScoreLevelStatement.setInt(1, bottomBracket);
               selectScoreLevelStatement.setInt(2, topBracket);
               ResultSet rs = selectScoreLevelStatement.executeQuery();
               if (rs.next()) {
                  distribution.put(level, rs.getInt(1));
               }
            }
         }

         log.info("level distribution: ");
         i$ = distribution.keySet().iterator();

         while(i$.hasNext()) {
            level = (Integer)i$.next();
            log.info(level + "," + distribution.get(level));
         }

         TreeMap var13 = distribution;
         return var13;
      } finally {
         if (olapConnection != null) {
            olapConnection.close();
         }

      }
   }

   public static void main(String[] args) throws Exception {
      String outFilename;
      if (args.length < 1) {
         outFilename = "score.distribution." + ReputationServiceI.FILE_DATE_FORMAT.format(new Date()) + ".csv";
         System.err.println("Usage ScoreDistribution <filename>, using default filename [" + outFilename + "]");
      } else {
         outFilename = args[0];
      }

      DOMConfigurator.configureAndWatch("log4j.xml");
   }

   public static DataSource getOLAPDataSource() throws Exception {
      Properties databaseProperties = new Properties();
      databaseProperties.load(new FileInputStream(System.getProperty("config.dir") + "database.properties"));
      ComboPooledDataSource datasource = new ComboPooledDataSource();
      log.info("rep jdbc url: jdbc:mysql://olap01:3306/fusion");
      datasource.setJdbcUrl("jdbc:mysql://olap01:3306/fusion");
      datasource.setUser(databaseProperties.getProperty("database.username"));
      datasource.setPassword(databaseProperties.getProperty("database.password"));
      datasource.setDriverClass(databaseProperties.getProperty("database.driver"));
      datasource.setMinPoolSize(1);
      datasource.setAcquireIncrement(1);
      datasource.setAcquireRetryAttempts(2);
      datasource.setMaxPoolSize(1);
      return datasource;
   }
}
