package com.projectgoth.fusion.maintenance;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.AuthenticationServicePrxHelper;
import com.projectgoth.fusion.slice.FusionException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.apache.log4j.xml.DOMConfigurator;

public class MigrateCredentials extends AbstractDatabaseMaintenance {
   private static Communicator iceCommunicator = Util.initialize(new String[0]);
   private String hostname;
   private AuthenticationServicePrx authenticationServiceProxy;

   public MigrateCredentials(String hostname) throws IOException {
      this.loadProperties();
      this.configureDataSources();
      this.hostname = hostname;
   }

   public synchronized AuthenticationServicePrx findAuthenticationServiceProxy() {
      try {
         if (this.authenticationServiceProxy == null) {
            ObjectPrx basePrx = iceCommunicator.stringToProxy("AuthenticationService:tcp -h " + this.hostname + " -p 23500");
            if (basePrx == null) {
               throw new Exception("communicator().stringToProxy() returned null");
            }

            this.authenticationServiceProxy = AuthenticationServicePrxHelper.checkedCast(basePrx);
            if (this.authenticationServiceProxy == null) {
               throw new Exception("AuthenticationServicePrxHelper.checkedCast() returned null");
            }
         }
      } catch (Exception var2) {
         log.warn("failed to locate authentication service at endpoint(s) " + this.hostname + "]", var2);
         this.authenticationServiceProxy = null;
      }

      return this.authenticationServiceProxy;
   }

   public void process(int delay) throws SQLException, InterruptedException {
      Connection connection = null;

      try {
         System.out.println(new Date() + " getting connection...");
         connection = this.slaveDataSource.getConnection();
         int resultsProcessed = false;
         int lastProcessedUserId = 0;

         int resultsProcessed;
         do {
            System.out.println(new Date() + " executing query...");
            PreparedStatement ps = connection.prepareStatement("select uid.id,uid.username from userid uid left outer join credential c on uid.id = c.userid and c.passwordtype = 1 where uid.id > ? and c.userid is null limit 1000");
            ps.setInt(1, lastProcessedUserId);
            ResultSet rs = ps.executeQuery();
            resultsProcessed = 0;
            System.out.println(new Date() + " iterating through results...");

            while(rs.next()) {
               int userID = rs.getInt(1);
               String username = rs.getString(2);
               System.out.println("user id [" + userID + "] and username [" + username + "]");

               try {
                  this.findAuthenticationServiceProxy().migrateUserCredentials(userID);
                  ++resultsProcessed;
                  lastProcessedUserId = userID;
                  Thread.sleep((long)delay);
               } catch (FusionException var14) {
                  log.error("failed to migrate user [" + userID + "]", var14);
               }
            }
         } while(resultsProcessed > 0);
      } finally {
         if (connection != null) {
            connection.close();
         }

      }

   }

   public static void main(String[] args) {
      System.setProperty("log.filename", "migratecredentials");
      DOMConfigurator.configureAndWatch("log4j.xml");

      try {
         MigrateCredentials mg = new MigrateCredentials(args[0]);
         mg.process(Integer.parseInt(args[1]));
      } catch (Throwable var6) {
         log.error("failed to migrate credentials", var6);
      } finally {
         System.exit(0);
      }

   }
}
