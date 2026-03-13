package com.projectgoth.fusion.maintenance;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import com.projectgoth.fusion.common.ConfigUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class PopulateUserIDFromFile extends AbstractDatabaseMaintenance {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PopulateUserIDFromFile.class));

   public PopulateUserIDFromFile() throws IOException {
      this.loadProperties();
      this.configureDataSources();
   }

   public void go(String filename, int batchCount, int iterationDelayMilliSeconds) throws SQLException, InterruptedException, IOException {
      long totalCount = 0L;
      int readCount = false;
      int reportCount = batchCount < 10000 ? 10000 : batchCount;
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line = null;
      List<String> usernames = new ArrayList(batchCount + 1);
      StringBuilder sql = new StringBuilder();
      Connection masterConnection = this.masterDataSource.getConnection();
      System.out.println("sleeping for " + iterationDelayMilliSeconds + " between iterations");
      long start = System.currentTimeMillis();

      try {
         do {
            int readCount = 0;
            usernames.clear();
            sql.append("INSERT INTO userid (username) VALUES ");

            while(readCount++ < batchCount && reader != null && (line = reader.readLine()) != null) {
               usernames.add(line.replace("'", "''"));
            }

            if (usernames.size() > 0) {
               Iterator i$ = usernames.iterator();

               while(i$.hasNext()) {
                  String username = (String)i$.next();
                  sql.append("('").append(username).append("'),");
               }

               sql.delete(sql.length() - 1, sql.length());

               try {
                  Statement statement = masterConnection.createStatement();
                  statement.executeUpdate(sql.toString());
                  statement.close();
                  long end = System.currentTimeMillis();
                  totalCount += (long)batchCount;
                  if (totalCount % (long)reportCount == 0L) {
                     System.out.println(totalCount + " done, last " + reportCount + " in " + (double)(end - start) / 1000.0D + " seconds (including sleep for 2+ iterations)");
                     start = System.currentTimeMillis();
                  }
               } catch (MySQLSyntaxErrorException var24) {
                  System.out.println("skipping users, error with " + sql);
               }
            }

            sql.delete(0, sql.length());
            Thread.sleep((long)iterationDelayMilliSeconds);
         } while(line != null);
      } finally {
         if (masterConnection != null) {
            masterConnection.close();
         }

      }

   }

   public static void main(String[] args) throws IOException {
      try {
         int batchCount = true;
         int iterationDelaySeconds = true;
         if (args.length < 3) {
            System.err.println("Usage PUID <filename> <batchcount> <iteration delay in ms>");
            System.exit(1);
         }

         int batchCount = Integer.parseInt(args[1]);
         int iterationDelaySeconds = Integer.parseInt(args[2]);
         PopulateUserIDFromFile pop = new PopulateUserIDFromFile();
         pop.go(args[0], batchCount, iterationDelaySeconds);
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

   }
}
