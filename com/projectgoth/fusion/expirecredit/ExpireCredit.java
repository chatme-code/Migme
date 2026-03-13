package com.projectgoth.fusion.expirecredit;

import Ice.Application;
import Ice.Properties;
import com.projectgoth.fusion.app.dao.db.fusiondb.DBUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.RequestCounter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.UserData;
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

public class ExpireCredit extends Application {
   private static final String APP_NAME = "Expire Credit";
   private static final String CONFIG_FILE = "ExpireCredit.cfg";
   public static Properties properties = null;
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
   static char startChar1 = 'A';
   static char startChar2 = '-';
   static String usernamesToExclude;
   static RequestCounter requestCounter = new RequestCounter();
   static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ExpireCredit.class));
   static int startOfPeak = 14;
   static int endOfPeak = 18;
   static int numThreads = 4;
   static ExecutorService executorService;
   static BufferedWriter writer = null;
   static double minExpiryAmount = 0.01D;
   static boolean inactive = false;

   public static void main(String[] args) {
      log.info("Expire Credit starting");

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
         } else if (args[i].equalsIgnoreCase("-minexpiryamount")) {
            minExpiryAmount = Double.parseDouble(args[i + 1]);
         } else if (args[i].equalsIgnoreCase("-inactive")) {
            inactive = true;
         }
      }

      if (dbMasterHost != null && dbMasterUsername != null && dbMasterPassword != null && dbSlaveHost != null && dbSlaveUsername != null && dbSlavePassword != null) {
         ExpireCredit app = new ExpireCredit();
         int status = app.main("Expire Credit", args, "ExpireCredit.cfg");
         log.info("Terminating");
         System.exit(status);
      } else {
         log.error("Usage: ExpireCredit [-s] [-r starting_prefix] [-peakstart hour] [-peakend hour] [-numthreads n] [-inactive] -mh master_db_host -mu master_db_username -mp master_db_password -sh slave_db_host -su slave_db_username -sp slave_db_password");
         log.error("   -s : Run in simulation mode");
         log.error("   -r : Start expiring credit from users with username starting with starting_prefix");
         log.error(" -peakstart : Starting time of peak (when database queries will be slowed down). Specify the hour. e.g., for 14:00 specify 14. Default: 14");
         log.error(" -peakend : Ending time of peak (when database queries will no longer be slowed down). Default: 18 (for 18:00)");
         log.error(" -numthreads : Number of threads we want expiring users' credit concurrently. Default: 2");
         log.error(" -minexpiryamount : If amount to be expired for a user is less than this, the expiry would be skipped for the user. Default: 0.01");
         log.error("e.g. ExpireCredit -s -r BE -mh 10.3.1.111 -mu fusion -mp fusionpass -sh 10.3.1.19 -su readonly -sp slavepass");
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

      log.info("Minimum amount to expire: " + minExpiryAmount);
      writer = openNewLogFile();
      if (writer == null) {
         return 1;
      } else {
         String prefix = this.readUsernamePrefixFromFile();
         if (prefix != null) {
            startChar1 = prefix.toUpperCase().charAt(0);
            startChar2 = prefix.toUpperCase().charAt(1);
            if (startChar1 < '!' || startChar2 < '!' || startChar1 > '~' || startChar2 > '~') {
               log.error("Invalid starting prefix: " + prefix);
               return 1;
            }
         }

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
         log.info("Running with " + numThreads + " threads");

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

                        executorService.execute(new ExpireCredit.ProcessUsersWithPrefix(prefix));
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
            log.info("Credit expiration complete");
            log.info("" + numProcessed + " users processed");
            log.info("Total credit expired: " + df2.format(totalCreditExpired));
            log.info("Total funded credit expired: " + df2.format(totalFundedCreditExpired));
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
         String logFileName = "ExpireCredit" + (simulationMode ? "_SIMULATION_" : "") + dateFormatter.format(new Date()) + ".csv";
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
      return (Boolean)SystemPropertyEntities.Temp.Cache.SE433_ENABLED.getValue() ? DBUtils.getFusionWriteConnection() : DriverManager.getConnection("jdbc:mysql://" + dbMasterHost + "/fusion", dbMasterUsername, dbMasterPassword);
   }

   private static Connection getSlaveConnection() throws Exception {
      return (Boolean)SystemPropertyEntities.Temp.Cache.SE433_ENABLED.getValue() ? DBUtils.getOlapReadConnection() : DriverManager.getConnection("jdbc:mysql://" + dbSlaveHost + "/fusion", dbSlaveUsername, dbSlavePassword);
   }

   private class ProcessUsersWithPrefix implements Runnable {
      private String prefix;

      public ProcessUsersWithPrefix(String prefix) {
         this.prefix = prefix;
      }

      public void run() {
         try {
            int numCandidates = false;
            ExpireCredit.log.info("Processing users with username starting with '" + this.prefix + "' (" + ExpireCredit.numProcessed + " users processed, " + ExpireCredit.numExpired + " expired so far)");
            ExpireCredit.this.writeUsernamePrefixToFile(this.prefix);
            Connection connSlave = ExpireCredit.getSlaveConnection();
            PreparedStatement psCandidateUsers;
            if (ExpireCredit.inactive) {
               psCandidateUsers = connSlave.prepareStatement("SELECT Username FROM user WHERE Username LIKE ? AND Balance > 0 AND DateRegistered < DATE_SUB(now(), INTERVAL 90 DAY) AND user.Username NOT IN (?)");
            } else {
               psCandidateUsers = connSlave.prepareStatement("SELECT Username FROM user WHERE Username LIKE ? AND Balance > 0 AND Status = 1 AND DateRegistered < DATE_SUB(now(), INTERVAL 90 DAY) AND user.Username NOT IN (?)");
            }

            psCandidateUsers.setString(1, this.prefix + '%');
            psCandidateUsers.setString(2, ExpireCredit.usernamesToExclude);
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
               ExpireCredit.log.info("No candidates found for prefix: " + this.prefix);
               return;
            }

            ExpireCredit.log.info("Processing " + numCandidatesx + " candidates for prefix: " + this.prefix);
            int numExpiredSoFar = ExpireCredit.numExpired;
            Iterator i$ = usernames.iterator();

            while(i$.hasNext()) {
               String username = (String)i$.next();
               this.processUser(username);
               ExpireCredit.requestCounter.add();
            }

            String msg = "Prefix \"" + this.prefix + "\" done. Processed " + ExpireCredit.df2.format((double)ExpireCredit.requestCounter.getRequestsPerSecond()) + "/s (" + (ExpireCredit.numExpired - numExpiredSoFar) + " expired). ";
            msg = msg + "Expired so far: $" + ExpireCredit.df2.format(ExpireCredit.totalCreditExpired) + " ($" + ExpireCredit.df2.format(ExpireCredit.totalFundedCreditExpired) + " funded)";
            ExpireCredit.log.info(msg);
         } catch (Exception var9) {
            ExpireCredit.log.error("An exception occurred processing prefix \"" + this.prefix + "\": " + var9.getMessage(), var9);
         }

      }

      private void processUser(String username) throws Exception {
         try {
            ++ExpireCredit.numProcessed;
            String sql = "SELECT balance.*, maxid.*, newcredit.* FROM (SELECT Balance, FundedBalance, user.Currency UserCurrency, Exchangerate, user.Type FROM user, currency WHERE Username = '" + username + "' AND user.Currency = currency.Code" + ") balance, " + "(" + "SELECT MAX(ID) MaxAccountEntryID, 1 as dummy " + "FROM accountentry " + "WHERE Username='" + username + "'" + ") maxid LEFT OUTER JOIN " + "(" + "SELECT Currency AccountEntryCurrency, SUM(Amount) NewCredit, 1 as dummy " + "FROM accountentry " + "WHERE Username = '" + username + "' AND accountentry.DateCreated >= DATE_SUB(now(), INTERVAL 90 DAY) " + "AND accountentry.Amount > 0 AND accountentry.Type <> 19 " + "GROUP BY Currency" + ") newcredit ON maxid.dummy=newcredit.dummy";
            Connection connSlave = ExpireCredit.getSlaveConnection();
            ResultSet rsSlaveData = connSlave.createStatement().executeQuery(sql);
            if (!rsSlaveData.next()) {
               return;
            }

            double balance = rsSlaveData.getDouble("Balance");
            if (balance <= 0.0D) {
               return;
            }

            double fundedBalance = rsSlaveData.getDouble("FundedBalance");
            String userCurrency = rsSlaveData.getString("UserCurrency");
            double exchangeRate = rsSlaveData.getDouble("ExchangeRate");
            UserData.TypeEnum userType = UserData.TypeEnum.fromValue(rsSlaveData.getInt("Type"));
            long maxSlaveAccountEntryID = rsSlaveData.getLong("MaxAccountEntryID");
            double newCredit = 0.0D;
            if (rsSlaveData.getObject("NewCredit") != null) {
               newCredit = rsSlaveData.getDouble("NewCredit");
               if (!userCurrency.equalsIgnoreCase(rsSlaveData.getString("AccountEntryCurrency"))) {
                  ExpireCredit.log.warn("Currency mismatch exception: " + username);
                  return;
               }

               if (rsSlaveData.next()) {
                  ExpireCredit.log.warn("Multiple currency exception: " + username);
                  return;
               }
            }

            rsSlaveData.close();
            connSlave.close();
            if (newCredit / exchangeRate >= 100.0D && (userType == UserData.TypeEnum.MIG33_MERCHANT || userType == UserData.TypeEnum.MIG33_TOP_MERCHANT)) {
               return;
            }

            double expiration = 0.0D;
            double fundedExpiration = 0.0D;
            if (newCredit < balance) {
               if (newCredit > fundedBalance) {
                  fundedExpiration = 0.0D;
               } else {
                  fundedExpiration = fundedBalance - newCredit;
               }

               expiration = balance - newCredit;
            }

            if ((Boolean)SystemPropertyEntities.Temp.Cache.SE433_ENABLED.getValue()) {
               if (expiration <= 0.0D) {
                  ExpireCredit.log.debug("Not expiring user [" + username + "] expiration [" + expiration + "] is less than minimal amount [0]");
                  return;
               }
            } else if (expiration / exchangeRate < ExpireCredit.minExpiryAmount) {
               ExpireCredit.log.debug("Not expiring user [" + username + "] expiration [" + expiration / exchangeRate + "] is less than minimal amount [" + ExpireCredit.minExpiryAmount + "]");
               return;
            }

            Connection connMaster = null;

            try {
               if (!ExpireCredit.simulationMode) {
                  connMaster = ExpireCredit.getMasterConnection();
                  connMaster.setAutoCommit(false);
                  ResultSet rsMaxMasterAccountEntryID = connMaster.createStatement().executeQuery("SELECT MAX(ID) FROM accountentry WHERE username='" + username + "' AND ID >= " + maxSlaveAccountEntryID + " FOR UPDATE");
                  if (!rsMaxMasterAccountEntryID.next()) {
                     throw new Exception("Unable to check max accountentry ID on master: " + username);
                  }

                  if (rsMaxMasterAccountEntryID.getLong(1) != maxSlaveAccountEntryID) {
                     ExpireCredit.log.warn("User did a transaction during expiration: " + username);
                     return;
                  }

                  PreparedStatement psInsertAccountEntry = connMaster.prepareStatement("INSERT INTO AccountEntry (Username, DateCreated, Type, Reference, Description, Amount, FundedAmount, Tax, Currency, ExchangeRate) VALUES (?,now(),24,?,'Expiration of credit older than 90 days',?,?,0,?,?)");
                  psInsertAccountEntry.setString(1, username);
                  psInsertAccountEntry.setString(2, username);
                  psInsertAccountEntry.setDouble(3, -expiration);
                  psInsertAccountEntry.setDouble(4, -fundedExpiration);
                  psInsertAccountEntry.setString(5, userCurrency);
                  psInsertAccountEntry.setDouble(6, exchangeRate);
                  if (psInsertAccountEntry.executeUpdate() != 1) {
                     throw new Exception("ERROR: Unable to insert an expiration account entry. Username: " + username);
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

               double balanceBeforeExpiration = balance / exchangeRate;
               double fundedBalanceBeforeExpiration = fundedBalance / exchangeRate;
               double balanceAfterExpiration = (balance - expiration) / exchangeRate;
               double fundedBalanceAfterExpiration = (fundedBalance - fundedExpiration) / exchangeRate;

               try {
                  StringBuilder sb = new StringBuilder();
                  sb.append("\"");
                  sb.append(username.replaceAll("\"", "\"\""));
                  sb.append("\",");
                  sb.append(ExpireCredit.df6.format(balanceBeforeExpiration));
                  sb.append(',');
                  sb.append(ExpireCredit.df6.format(fundedBalanceBeforeExpiration));
                  sb.append(',');
                  sb.append(ExpireCredit.df6.format(balanceAfterExpiration));
                  sb.append(',');
                  sb.append(ExpireCredit.df6.format(fundedBalanceAfterExpiration));
                  ExpireCredit.writer.write(sb.toString());
                  ExpireCredit.writer.newLine();
                  ExpireCredit.writer.flush();
               } catch (IOException var45) {
                  throw new Exception("Unable to log credit expiry. Username: " + username + " Exception: " + var45.getMessage());
               }

               ++ExpireCredit.numExpired;
               ExpireCredit.totalCreditExpired += expiration / exchangeRate;
               ExpireCredit.totalFundedCreditExpired += fundedExpiration / exchangeRate;
            } catch (Exception var46) {
               if (connMaster != null) {
                  connMaster.rollback();
               }

               ExpireCredit.log.error("Exception processing the user " + username + ": " + var46.getMessage());
            } finally {
               if (connMaster != null) {
                  connMaster.close();
               }

            }
         } finally {
            Calendar var35 = Calendar.getInstance();
            int currentHour = var35.get(11);
            if (currentHour >= ExpireCredit.startOfPeak && currentHour < ExpireCredit.endOfPeak) {
               Thread.sleep(1000L);
            }

         }

      }
   }
}
