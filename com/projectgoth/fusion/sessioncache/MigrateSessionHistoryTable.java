package com.projectgoth.fusion.sessioncache;

import com.mchange.v2.c3p0.PooledDataSource;
import com.projectgoth.fusion.common.ConfigUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;

public class MigrateSessionHistoryTable {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Main.class));
   private static final int BATCH_SIZE = 100;
   private static final int CHUNK_SIZE = 2000;
   private static ApplicationContext context;

   public static void main(String[] args) {
      PropertyConfigurator.configureAndWatch("log4j.properties");
      context = SessionCacheApplicationContext.getContext();
      PooledDataSource masterDataSource = (PooledDataSource)context.getBean("dataSource");
      Connection masterConnection = null;
      long rowCount = 0L;

      try {
         masterConnection = masterDataSource.getConnection();
         Statement statement = masterConnection.createStatement();
         ResultSet resultSet = statement.executeQuery("select count(id) from sessionhistory");
         if (resultSet.next()) {
            rowCount = resultSet.getLong(1);
         }

         log.info(rowCount + " sessions in the original sessionhistory table");
      } catch (SQLException var35) {
         log.error(var35);
      } finally {
         if (masterConnection != null) {
            try {
               masterConnection.close();
               masterConnection = null;
            } catch (SQLException var33) {
               log.error(var33);
            }
         }

      }

      long fromID = 0L;
      long toID = 0L;

      try {
         masterConnection = masterDataSource.getConnection();
         PreparedStatement statement = masterConnection.prepareStatement("select * from sessionhistory where id >= ? and id < ?");

         for(int i = 0; i < 20; ++i) {
            toID += 100L;
            statement.setLong(1, fromID);
            statement.setLong(2, toID);

            for(ResultSet resultSet = statement.executeQuery(); resultSet.next(); rowCount = resultSet.getLong(1)) {
            }

            fromID += 100L;
         }
      } catch (SQLException var37) {
         log.error(var37);
      } finally {
         if (masterConnection != null) {
            try {
               masterConnection.close();
            } catch (SQLException var34) {
               log.error(var34);
            }
         }

      }

   }
}
