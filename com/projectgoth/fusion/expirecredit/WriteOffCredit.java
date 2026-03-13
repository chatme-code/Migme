package com.projectgoth.fusion.expirecredit;

import Ice.Application;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntryData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class WriteOffCredit extends Application {
   private static final String APP_NAME = "WriteOffCredit";
   static String dbMasterHost;
   static String dbMasterUsername;
   static String dbMasterPassword;
   static String dbSlaveHost;
   static String dbSlaveUsername;
   static String dbSlavePassword;
   static volatile int numProcessed = 0;
   static volatile int numExpired = 0;
   static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   static DecimalFormat df2 = new DecimalFormat("0.00");
   static DecimalFormat df6 = new DecimalFormat("0.000000");
   static boolean simulationMode = false;
   static volatile double totalCreditExpired = 0.0D;
   static volatile double totalFundedCreditExpired = 0.0D;
   static char startChar1 = '!';
   static char startChar2 = '!';
   static String usernamesToExclude;
   static RequestCounter requestCounter = new RequestCounter();
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(WriteOffCredit.class));
   static int startOfPeak = 14;
   static int endOfPeak = 18;
   static int numThreads = 2;
   static ExecutorService executorService;
   static BufferedWriter writer = null;
   static String dateParam = "2010-07-01";

   public static void main(String[] args) {
      log.info("WriteOffCredit starting");

      for(int i = 0; i < args.length; ++i) {
         if (args[i].equalsIgnoreCase("-s")) {
            simulationMode = true;
         } else if (args[i].equalsIgnoreCase("-mh")) {
            dbMasterHost = args[i + 1];
         } else if (args[i].equalsIgnoreCase("-mu")) {
            dbMasterUsername = args[i + 1];
         } else if (args[i].equalsIgnoreCase("-mp")) {
            dbMasterPassword = args[i + 1];
         } else if (args[i].equalsIgnoreCase("-sh")) {
            dbSlaveHost = args[i + 1];
         } else if (args[i].equalsIgnoreCase("-su")) {
            dbSlaveUsername = args[i + 1];
         } else if (args[i].equalsIgnoreCase("-sp")) {
            dbSlavePassword = args[i + 1];
         } else if (args[i].equalsIgnoreCase("-r")) {
            startChar1 = args[i + 1].toUpperCase().charAt(0);
            startChar2 = args[i + 1].toUpperCase().charAt(1);
            if (startChar1 < '!' || startChar2 < '!' || startChar1 > '~' || startChar2 > '~') {
               log.error("Invalid starting prefix");
               return;
            }
         } else if (args[i].equalsIgnoreCase("-peakstart")) {
            startOfPeak = Integer.parseInt(args[i + 1]);
         } else if (args[i].equalsIgnoreCase("-peakend")) {
            endOfPeak = Integer.parseInt(args[i + 1]);
         } else if (args[i].equalsIgnoreCase("-numthreads")) {
            numThreads = Integer.parseInt(args[i + 1]);
         } else if (args[i].equalsIgnoreCase("-date")) {
            dateParam = args[i + 1];
         }
      }

      if (dbMasterHost != null && dbMasterUsername != null && dbMasterPassword != null && dbSlaveHost != null && dbSlaveUsername != null && dbSlavePassword != null) {
         WriteOffCredit app = new WriteOffCredit();
         int status = app.main("WriteOffCredit", args);
         log.info("Terminating");
         System.exit(status);
      } else {
         log.error("Usage: WriteOffCredit [-s] [-r starting_prefix] [-peakstart hour] [-peakend hour] [-numthreads n] -mh master_db_host -mu master_db_username -mp master_db_password -sh slave_db_host -su slave_db_username -sp slave_db_password");
         log.error("   -s : Run in simulation mode");
         log.error("   -r : Start writing-off credit from users with username starting with starting_prefix");
         log.error(" -peakstart : Starting time of peak (when database queries will be slowed down). Specify the hour. e.g., for 14:00 specify 14. Default: 14");
         log.error(" -peakend : Ending time of peak (when database queries will no longer be slowed down). Default: 18 (for 18:00)");
         log.error(" -numthreads : Number of threads we want writing-off users' credit concurrently. Default: 2");
         log.error("e.g. WriteOffCredit -s -r BE -mh 10.3.1.111 -mu fusion -mp fusionpass -sh 10.3.1.19 -su readonly -sp slavepass");
      }
   }

   public int run(String[] arg0) {
      usernamesToExclude = communicator().getProperties().getProperty("UsernamesToExclude");
      if (StringUtil.isBlank(usernamesToExclude)) {
         usernamesToExclude = "''";
      }

      if (simulationMode) {
         log.info("** Running in simulation mode **");
      }

      String prefix = this.readUsernamePrefixFromFile();
      if (prefix != null) {
         startChar1 = prefix.toUpperCase().charAt(0);
         startChar2 = prefix.toUpperCase().charAt(1);
         if (startChar1 < '!' || startChar2 < '!' || startChar1 > '~' || startChar2 > '~') {
            log.error("Invalid starting prefix: " + prefix);
            return 1;
         }
      }

      writer = openNewLogFile();
      if (writer == null) {
         return 1;
      } else {
         log.info("Will connect to master DB on host " + dbMasterHost + " as " + dbMasterUsername);

         Connection conn;
         try {
            conn = getMasterConnection();
            conn.close();
         } catch (Exception var19) {
            log.fatal("Unable to connect to to master DB", var19);
            return 1;
         }

         log.info("Will connect to slave DB on host " + dbSlaveHost + " as " + dbSlaveUsername);

         try {
            conn = getSlaveConnection();
            conn.close();
         } catch (Exception var18) {
            log.fatal("Unable to connect to to slave DB", var18);
            return 1;
         }

         MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.ACCOUNT_BALANCE, "test");
         executorService = (ThreadPoolExecutor)Executors.newFixedThreadPool(numThreads);

         try {
            for(char c1 = startChar1; c1 <= '~'; ++c1) {
               if (c1 != '\\' && (c1 <= '`' || c1 >= '{')) {
                  for(char c2 = c1 == startChar1 ? startChar2 : 33; c2 <= '~'; ++c2) {
                     if (c2 != '\\' && (c2 <= '`' || c2 >= '{')) {
                        if (c1 != '%' && c1 != '_') {
                           prefix = Character.toString(c1);
                        } else {
                           prefix = "\\" + Character.toString(c1);
                        }

                        if (c2 != '%' && c2 != '_') {
                           prefix = prefix + Character.toString(c2);
                        } else {
                           prefix = prefix + "\\" + Character.toString(c2);
                        }

                        executorService.execute(new WriteOffCredit.ProcessUsersWithPrefix(prefix));
                     }
                  }
               }
            }

            executorService.shutdown();

            try {
               executorService.awaitTermination(2592000L, TimeUnit.SECONDS);
            } catch (InterruptedException var17) {
               var17.printStackTrace();
            }

            this.deletePrefixFile();
            log.info("Credit write-off complete");
            log.info("" + numProcessed + " users processed");
            log.info("Total credit written off: " + df2.format(totalCreditExpired));
            log.info("Total funded credit written off: " + df2.format(totalFundedCreditExpired));
         } catch (Exception var20) {
            log.error("An exception occurred: " + var20.toString());
            var20.printStackTrace();
         } finally {
            try {
               writer.close();
            } catch (IOException var16) {
               log.error("Unable to close output file: " + var16.getMessage());
            }

         }

         return 0;
      }
   }

   private static BufferedWriter openNewLogFile() {
      try {
         SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
         String logFileName = "WriteOffCredit" + (simulationMode ? "_SIMULATION_" : "") + dateFormatter.format(new Date()) + ".csv";
         File logFile = new File(logFileName);
         logFile.createNewFile();
         log.info("Creating output file " + logFileName);
         return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));
      } catch (Exception var3) {
         log.error("Unable to create output file. Exception:");
         var3.printStackTrace();
         return null;
      }
   }

   private synchronized void writeLineToOutputFile(String line) {
      try {
         writer.write(line);
         writer.newLine();
         writer.flush();
      } catch (IOException var3) {
         log.error("Unable to write to log file", var3);
      }

   }

   private String readUsernamePrefixFromFile() {
      try {
         File f = new File("prefix");
         if (f.exists()) {
            FileReader fr = new FileReader(f);
            BufferedReader in = new BufferedReader(fr);
            String prefix = in.readLine();
            log.info("Prefix file found. Starting from username prefix: " + prefix);
            return prefix;
         } else {
            return null;
         }
      } catch (Exception var5) {
         log.warn("Unable to read 'prefix' file: " + var5.getMessage());
         return null;
      }
   }

   private void writeUsernamePrefixToFile(String prefix) {
      try {
         FileWriter fileWriter = new FileWriter("prefix", false);
         BufferedWriter writer = new BufferedWriter(fileWriter);
         writer.write(prefix);
         writer.flush();
         writer.close();
      } catch (Exception var4) {
         log.warn("Unable to write prefix " + prefix + " to 'prefix' file: " + var4.getMessage());
      }

   }

   private void deletePrefixFile() {
      File f = new File("prefix");
      if (f.exists()) {
         f.delete();
      }

   }

   private static Connection getMasterConnection() throws Exception {
      return DriverManager.getConnection("jdbc:mysql://" + dbMasterHost + "/fusion", dbMasterUsername, dbMasterPassword);
   }

   private static Connection getSlaveConnection() throws Exception {
      return DriverManager.getConnection("jdbc:mysql://" + dbSlaveHost + "/fusion", dbSlaveUsername, dbSlavePassword);
   }

   private class ProcessUsersWithPrefix implements Runnable {
      private String prefix;

      public ProcessUsersWithPrefix(String prefix) {
         this.prefix = prefix;
      }

      public void run() {
         try {
            int numCandidates = false;
            WriteOffCredit.log.info("Processing users with username starting with '" + this.prefix + "' (" + WriteOffCredit.numProcessed + " users processed, " + WriteOffCredit.numExpired + " written off so far)");
            WriteOffCredit.this.writeUsernamePrefixToFile(this.prefix);
            Connection connSlave = WriteOffCredit.getSlaveConnection();
            PreparedStatement psCandidateUsers;
            if ((Boolean)SystemPropertyEntities.Temp.Cache.ER74_ENABLED.getValue()) {
               psCandidateUsers = connSlave.prepareStatement("SELECT Username FROM user WHERE Username LIKE ? AND Balance > 0 AND Status = 0 AND LastLoginDate < ? AND user.Username NOT IN (?)");
               psCandidateUsers.setString(1, this.prefix + '%');
               psCandidateUsers.setDate(2, java.sql.Date.valueOf(WriteOffCredit.dateParam));
               psCandidateUsers.setString(3, WriteOffCredit.usernamesToExclude);
            } else {
               psCandidateUsers = connSlave.prepareStatement("SELECT Username FROM user WHERE Username LIKE ? AND Balance > 0 AND Status = 0 AND LastLoginDate < '2010-07-01' AND user.Username NOT IN (?)");
               psCandidateUsers.setString(1, this.prefix + '%');
               psCandidateUsers.setString(2, WriteOffCredit.usernamesToExclude);
            }

            ResultSet rsCandidateUsers = psCandidateUsers.executeQuery();
            Vector usernames = new Vector();

            while(rsCandidateUsers.next()) {
               usernames.add(rsCandidateUsers.getString(1));
            }

            rsCandidateUsers.close();
            psCandidateUsers.close();
            connSlave.close();
            int numCandidatesx = usernames.size();
            if (numCandidatesx == 0) {
               WriteOffCredit.log.info("No candidates found for prefix: " + this.prefix);
               return;
            }

            WriteOffCredit.log.info("Processing " + numCandidatesx + " candidates for prefix: " + this.prefix);
            int numExpiredSoFar = WriteOffCredit.numExpired;
            Iterator i$ = usernames.iterator();

            while(i$.hasNext()) {
               String username = (String)i$.next();
               this.processUser(username);
               WriteOffCredit.requestCounter.add();
            }

            String msg = "Prefix \"" + this.prefix + "\" done. Processed " + WriteOffCredit.df2.format((double)WriteOffCredit.requestCounter.getRequestsPerSecond()) + "/s (" + (WriteOffCredit.numExpired - numExpiredSoFar) + " written off). ";
            msg = msg + "Written off so far: $" + WriteOffCredit.df2.format(WriteOffCredit.totalCreditExpired) + " ($" + WriteOffCredit.df2.format(WriteOffCredit.totalFundedCreditExpired) + " funded)";
            WriteOffCredit.log.info(msg);
         } catch (Exception var9) {
            WriteOffCredit.log.error("An exception occurred processing prefix \"" + this.prefix + "\": " + var9.getMessage(), var9);
         }

      }

      private void processUser(String username) throws Exception {
         try {
            ++WriteOffCredit.numProcessed;
            String escapedUsername = username.replaceAll("'", "\\\\'");
            String sql = "SELECT balance.*, maxid.* FROM (SELECT Balance, FundedBalance, user.Currency UserCurrency, Exchangerate, user.Type FROM user, currency WHERE Username = '" + escapedUsername + "' AND user.Currency = currency.Code" + ") balance, " + "(" + "SELECT MAX(ID) MaxAccountEntryID " + "FROM accountentry " + "WHERE Username='" + escapedUsername + "'" + ") maxid";
            Connection connSlave = WriteOffCredit.getSlaveConnection();
            ResultSet rsSlaveData = connSlave.createStatement().executeQuery(sql);
            if (!rsSlaveData.next()) {
               return;
            }

            double balance = rsSlaveData.getDouble("Balance");
            if (!(balance <= 0.0D)) {
               double fundedBalance = rsSlaveData.getDouble("FundedBalance");
               String userCurrency = rsSlaveData.getString("UserCurrency");
               double exchangeRate = rsSlaveData.getDouble("ExchangeRate");
               long maxSlaveAccountEntryID = rsSlaveData.getLong("MaxAccountEntryID");
               rsSlaveData.close();
               connSlave.close();
               double expiration = balance;
               double fundedExpiration = fundedBalance;
               Connection connMaster = null;

               try {
                  try {
                     if (!WriteOffCredit.simulationMode) {
                        connMaster = WriteOffCredit.getMasterConnection();
                        connMaster.setAutoCommit(false);
                        ResultSet rsMaxMasterAccountEntryID = connMaster.createStatement().executeQuery("SELECT MAX(ID) FROM accountentry WHERE username='" + escapedUsername + "' FOR UPDATE");
                        if (!rsMaxMasterAccountEntryID.next()) {
                           throw new Exception("Unable to check max accountentry ID on master: " + username);
                        }

                        if (rsMaxMasterAccountEntryID.getLong(1) != maxSlaveAccountEntryID) {
                           WriteOffCredit.log.warn("User did a transaction during write-off: " + username);
                           return;
                        }

                        PreparedStatement psInsertAccountEntry = connMaster.prepareStatement("INSERT INTO AccountEntry (Username, DateCreated, Type, Reference, Description, Amount, FundedAmount, Tax, Currency, ExchangeRate) VALUES (?,now(),?,?,'Credit Adjustment',?,?,0,?,?)");
                        psInsertAccountEntry.setString(1, username);
                        psInsertAccountEntry.setInt(2, AccountEntryData.TypeEnum.CREDIT_WRITE_OFF.value());
                        psInsertAccountEntry.setString(3, username);
                        psInsertAccountEntry.setDouble(4, -expiration);
                        psInsertAccountEntry.setDouble(5, -fundedExpiration);
                        psInsertAccountEntry.setString(6, userCurrency);
                        psInsertAccountEntry.setDouble(7, exchangeRate);
                        if (psInsertAccountEntry.executeUpdate() != 1) {
                           throw new Exception("ERROR: Unable to insert a write-off account entry. Username: " + username);
                        }

                        psInsertAccountEntry.close();
                        PreparedStatement psUpdateBalance = connMaster.prepareStatement("UPDATE User SET Balance = ?, FundedBalance = ? WHERE Username = ?");
                        psUpdateBalance.setDouble(1, balance - expiration);
                        psUpdateBalance.setDouble(2, fundedBalance - fundedExpiration);
                        psUpdateBalance.setString(3, username);
                        if (psUpdateBalance.executeUpdate() != 1) {
                           throw new Exception("ERROR: Unable to update a user's balance. Username: " + username);
                        }

                        psUpdateBalance.close();
                        MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.ACCOUNT_BALANCE, username);
                        connMaster.commit();
                        connMaster.close();
                        connMaster = null;
                     }

                     StringBuilder sb = new StringBuilder();
                     sb.append("\"");
                     sb.append(username.replaceAll("\"", "\"\""));
                     sb.append("\",\"");
                     sb.append(WriteOffCredit.dateFormatter.format(new Date()));
                     sb.append("\",");
                     sb.append(WriteOffCredit.df6.format(balance));
                     sb.append(',');
                     sb.append(WriteOffCredit.df6.format(fundedBalance));
                     sb.append(',');
                     sb.append(WriteOffCredit.df6.format(exchangeRate));
                     sb.append(",\"");
                     sb.append(userCurrency);
                     sb.append("\",");
                     sb.append(WriteOffCredit.df6.format(balance / exchangeRate));
                     sb.append(',');
                     sb.append(WriteOffCredit.df6.format(fundedBalance / exchangeRate));
                     WriteOffCredit.this.writeLineToOutputFile(sb.toString());
                     ++WriteOffCredit.numExpired;
                     WriteOffCredit.totalCreditExpired += expiration / exchangeRate;
                     WriteOffCredit.totalFundedCreditExpired += fundedExpiration / exchangeRate;
                  } catch (Exception var35) {
                     if (connMaster != null) {
                        connMaster.rollback();
                     }

                     WriteOffCredit.log.error("Exception processing the user " + username + ": " + var35.getMessage());
                  }

                  return;
               } finally {
                  if (connMaster != null) {
                     connMaster.close();
                  }

               }
            }
         } finally {
            Calendar var27 = Calendar.getInstance();
            int currentHour = var27.get(11);
            if (currentHour >= WriteOffCredit.startOfPeak && currentHour < WriteOffCredit.endOfPeak) {
               Thread.sleep(1000L);
            }

         }

      }
   }
}
