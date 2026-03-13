package com.projectgoth.fusion.reputation.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.reputation.UpdateScoreTable;
import com.projectgoth.fusion.reputation.file.SortBigFile;
import com.projectgoth.fusion.reputation.file.comparator.SingleIndexIntegerFileEntryComparator;
import com.projectgoth.fusion.reputation.file.comparator.SingleIndexIntegerStringListComparator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.util.StopWatch;

public class CreateScoreFileForUsers {
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

   public static void main(String[] args) throws Exception {
      if (args.length < 1) {
         System.err.println("Usage InitialLevel <filename>");
         System.exit(1);
      }

      DOMConfigurator.configureAndWatch("log4j.xml");
      String filename = args[0];
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      BufferedReader reader = new BufferedReader(new FileReader(directoryHolder.getDataDirectory() + filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(directoryHolder.getDataDirectory() + "scored." + filename));
      DataSource masterDataSource = getMasterDataSource();
      Connection masterConnection = null;
      StopWatch sw = new StopWatch();

      try {
         String line = null;
         String username = null;
         masterConnection = masterDataSource.getConnection();
         PreparedStatement getOldScoreStatement = masterConnection.prepareStatement("select uid.id, score from score_orig s right outer join userid uid on s.userid = uid.id where username in (?,?,?,?,?,?,?,?,?,?)");
         log.info("starting score dump...");
         sw.start("score dump");
         ArrayList names = new ArrayList();

         label127:
         while(true) {
            do {
               if ((line = reader.readLine()) == null) {
                  getOldScoreStatement = masterConnection.prepareStatement("select uid.id, score from score_orig s right outer join userid uid on s.userid = uid.id where username = ?");
                  Iterator i$ = names.iterator();

                  while(i$.hasNext()) {
                     String name = (String)i$.next();
                     getOldScoreStatement.setString(1, name);
                     ResultSet rs = getOldScoreStatement.executeQuery();
                     if (rs.next()) {
                        writer.write(Integer.toString(rs.getInt(1)));
                        writer.write(",");
                        writer.write(Integer.toString(rs.getInt(2)));
                        writer.newLine();
                     }
                  }

                  sw.stop();
                  log.info("finished with score dump...");
                  break label127;
               }

               username = CSVUtils.getColumnFromLine(line, 0, '\t');
               names.add(username);
            } while(names.size() < 10);

            int index = 1;
            Iterator i$ = names.iterator();

            while(i$.hasNext()) {
               String name = (String)i$.next();
               getOldScoreStatement.setString(index++, name);
            }

            ResultSet rs = getOldScoreStatement.executeQuery();
            boolean var20 = false;

            while(rs.next()) {
               writer.write(Integer.toString(rs.getInt(1)));
               writer.write(",");
               writer.write(Integer.toString(rs.getInt(2)));
               writer.newLine();
            }

            rs.close();
            names.clear();
         }
      } finally {
         if (masterConnection != null) {
            masterConnection.close();
         }

      }

      writer.flush();
      writer.close();
      sw.start("sorting score dump");
      SortBigFile sorter = new SortBigFile(directoryHolder);
      sorter.go(new FileLocation(directoryHolder.getDataDirectory(), "scored." + filename), new FileLocation(directoryHolder.getDataDirectory(), "scored." + filename), new SingleIndexIntegerFileEntryComparator(1), new SingleIndexIntegerStringListComparator(1), 2, ',');
      sw.stop();
      log.info(sw.prettyPrint());
   }
}
