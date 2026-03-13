package com.projectgoth.fusion.maintenance;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

public class PopulateUserID extends AbstractDatabaseMaintenance {
   protected static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PopulateUserID.class));

   public PopulateUserID() throws IOException {
      this.loadProperties();
      this.configureDataSources();
   }

   public void go(int batchCount, int iterationDelaySeconds) throws SQLException, InterruptedException {
      long totalCount = 0L;
      int rowCount = false;
      int reportCount = batchCount < 10000 ? 10000 : batchCount;
      Connection masterConnection = this.masterDataSource.getConnection();
      System.out.println("sleeping for " + iterationDelaySeconds + " between iterations");
      long start = System.currentTimeMillis();

      int rowCount;
      try {
         do {
            Statement statement = masterConnection.createStatement();
            rowCount = statement.executeUpdate("INSERT INTO userid (username) SELECT username FROM user WHERE NOT EXISTS (SELECT Id from UserID where UserID.username = user.username) LIMIT " + batchCount);
            statement.close();
            long end = System.currentTimeMillis();
            totalCount += (long)rowCount;
            if (totalCount % (long)reportCount == 0L) {
               System.out.println(totalCount + " done, last " + reportCount + " in " + (double)(end - start) / 1000.0D + " seconds (including sleep for 2+ iterations)");
               start = System.currentTimeMillis();
            }

            Thread.sleep((long)(iterationDelaySeconds * 1000));
         } while(rowCount > 0);
      } finally {
         if (masterConnection != null) {
            masterConnection.close();
         }

      }

   }

   public static void main(String[] args) throws IOException {
      try {
         int batchCount = 1000;
         int iterationDelaySeconds = 125;
         if (args.length >= 1) {
            batchCount = Integer.parseInt(args[0]);
            iterationDelaySeconds = Integer.parseInt(args[1]);
         }

         PopulateUserID pop = new PopulateUserID();
         pop.go(batchCount, iterationDelaySeconds);
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

   }
}
