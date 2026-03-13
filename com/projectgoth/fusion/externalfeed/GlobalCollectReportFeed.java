package com.projectgoth.fusion.externalfeed;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BankTransferReceivedData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class GlobalCollectReportFeed extends TimerTask {
   private static final String APP_NAME = "GlobalCollectReportFeed";
   private static final String PROTOCOL = "sftp";
   private static final int PORT = 22;
   private static Logger logger = Logger.getLogger("GlobalCollectReportFeed");
   private String server;
   private String username;
   private String password;
   private String[] merchantIDs;
   private String remoteDir;
   private String localDir;
   private String fileExtension;
   private String fileName;
   private Set<String> bankTransferEmailsSent = new HashSet();

   public GlobalCollectReportFeed(String server, String username, String password, String[] merchantIDs, String remoteDir, String localDir, String fileExtension, String fileName) {
      this.server = server;
      this.username = username;
      this.password = password;
      this.merchantIDs = merchantIDs;
      this.remoteDir = remoteDir;
      this.localDir = localDir;
      this.fileExtension = fileExtension;
      this.fileName = fileName;
   }

   public void run() {
      Iterator i$ = this.getFileNames().iterator();

      while(true) {
         File file;
         do {
            if (!i$.hasNext()) {
               return;
            }

            String name = (String)i$.next();
            file = new File(name);
         } while(!file.exists() && !this.downloadFile(file));

         try {
            this.processFile(file);
         } catch (Exception var5) {
            logger.error(var5.getClass().getName() + " (" + var5.getMessage() + ") occured while processing file " + file.getPath());
         }
      }
   }

   private List<String> getFileNames() {
      List<String> fileNames = new LinkedList();
      if (this.fileName != null) {
         fileNames.add(this.localDir + File.separatorChar + this.fileName);
      } else {
         Calendar cal = Calendar.getInstance();
         int day = cal.get(7);
         if (day == 7 || day == 1) {
            return fileNames;
         }

         String[] arr$ = this.merchantIDs;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String merchantID = arr$[i$];
            StringBuilder builder = new StringBuilder();
            builder.append(merchantID).append(cal.get(1) % 10).append(String.format("%1$03d", cal.get(6))).append(".").append(this.fileExtension);
            fileNames.add(this.localDir + File.separatorChar + builder.toString());
         }
      }

      return fileNames;
   }

   private boolean downloadFile(File file) {
      Session session = null;
      Channel channel = null;

      boolean var5;
      try {
         UserInfo userInfo = new GlobalCollectReportFeed.SSHUserInfo(this.password);
         session = (new JSch()).getSession(this.username, this.server, 22);
         session.setUserInfo(userInfo);
         session.connect();
         channel = session.openChannel("sftp");
         channel.connect();
         ChannelSftp sftp = (ChannelSftp)channel;
         BufferedReader reader = new BufferedReader(new InputStreamReader(sftp.get(this.remoteDir + file.getName())));
         BufferedWriter writer = new BufferedWriter(new FileWriter(file));

         String line;
         while((line = reader.readLine()) != null) {
            writer.write(line);
            writer.write("\r\n");
         }

         writer.close();
         reader.close();
         logger.info("Successfully downloaded " + file.getName());
         boolean var9 = true;
         return var9;
      } catch (Exception var14) {
         logger.warn("Failed to download report file " + file.getName() + " - " + var14.getMessage());
         var5 = false;
      } finally {
         if (channel != null) {
            channel.disconnect();
         }

         if (session != null) {
            session.disconnect();
         }

      }

      return var5;
   }

   private void processFile(File file) throws CreateException, IOException {
      String lastFileSuccessfullyProcessed = (String)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.GLOBAL_COLLECT_REPORT_FEED, "file");
      if (lastFileSuccessfullyProcessed != null && lastFileSuccessfullyProcessed.equals(file.getPath())) {
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GLOBAL_COLLECT_REPORT_FEED, "file", lastFileSuccessfullyProcessed);
         logger.info("Skipping file " + file.getPath() + " (already processed)");
      } else {
         logger.info("Processing file " + file.getPath());
         Account accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         BufferedReader reader = new BufferedReader(new FileReader(file));
         int lines = 0;
         int bankTransfers = 0;
         int westernUnions = 0;
         int chargeBacks = 0;
         boolean success = true;

         String line;
         while((line = reader.readLine()) != null) {
            ++lines;

            try {
               String paymentReference;
               if (this.isPayment(line)) {
                  paymentReference = line.substring(3, 15).trim();
                  Enums.PaymentEnum paymentType = Enums.PaymentEnum.fromValue(accountEJB.getPaymentType(paymentReference));
                  if (paymentType == null) {
                     throw new Exception("Unable to determine payment type for payment reference " + paymentReference);
                  }

                  if (paymentType != Enums.PaymentEnum.BANK_TRANSFER) {
                     throw new Exception("Unsupported payment type " + paymentType.toString());
                  }

                  BankTransferReceivedData bankTransferReceivedData = this.parseBankTransferReceived(line);
                  if (bankTransferReceivedData != null) {
                     bankTransferReceivedData.fileName = file.getName();
                     bankTransferReceivedData.row = lines;
                     if (SystemProperty.getBool("DisableAutomatedBankTransferCredit", true)) {
                        this.sendBankTransferNotificationEmail(bankTransferReceivedData);
                     } else {
                        accountEJB.updateBankTransferStatus(bankTransferReceivedData, new AccountEntrySourceData(GlobalCollectReportFeed.class));
                     }

                     ++bankTransfers;
                  }
               } else if (this.isChargeBack(line)) {
                  paymentReference = line.substring(15, 35).trim();
                  String chargeBackReasonCode;
                  Date chargeBackDate;
                  if (line.length() > 400) {
                     chargeBackReasonCode = line.substring(384, 386).trim();
                     chargeBackDate = (new SimpleDateFormat("yyyyMMdd")).parse(line.substring(261, 269));
                  } else {
                     chargeBackReasonCode = line.substring(364, 366).trim();
                     chargeBackDate = (new SimpleDateFormat("ddMMyyyy")).parse(line.substring(241, 249));
                  }

                  accountEJB.creditCardChargeBack(paymentReference, chargeBackDate, chargeBackReasonCode);
                  ++chargeBacks;
               }
            } catch (Exception var14) {
               logger.error(var14.getClass().getName() + " (" + var14.getMessage() + ") occured while parsing line: " + line, var14);
               success = false;
            }
         }

         logger.info("Finished processing. Lines: " + lines + ". Charge backs: " + chargeBacks + ". Bank transfer records: " + bankTransfers + ". Western Union records: " + westernUnions);
         reader.close();
         if (success) {
            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.GLOBAL_COLLECT_REPORT_FEED, "file", file.getPath());
         }

      }
   }

   private void sendBankTransferNotificationEmail(BankTransferReceivedData btr) {
      if (!this.bankTransferEmailsSent.contains(btr.fileName + btr.row)) {
         SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         DecimalFormat df = new DecimalFormat("0.00");
         String destEmail = SystemProperty.get("SalesEmail", "sales@mig.me");
         String subject = "GlobalCollect Bank Transfer " + btr.type + ": " + df.format(btr.invoiceAmountDeliv) + " " + btr.invoiceCurrencyDeliv;
         String body = "GlobalCollect Bank Transfer " + btr.type + "\n\n";
         body = body + "Date Created: " + dateFormatter.format(new Date()) + "\n";
         body = body + "Invoice Deliv Amount: " + df.format(btr.invoiceAmountDeliv) + " " + btr.invoiceCurrencyDeliv + "\n";
         body = body + "Invoice Local Amount: " + df.format(btr.invoiceAmountLocal) + " " + btr.invoiceCurrencyLocal + "\n";
         body = body + "Payment Amount: " + df.format(btr.paymentAmount) + " " + btr.paymentCurrency + "\n";
         body = body + "Payment Reference: " + btr.paymentReference + "\n";
         body = body + "Customer Mobile: " + btr.customerID + "\n";
         body = body + "Additional Reference: " + btr.additionalReference + "\n";
         body = body + "Effort Number: " + btr.effortNumber + "\n";
         body = body + "Payment Method: " + btr.paymentMethod + "\n";
         body = body + "Unclean Indicator: " + btr.uncleanIndicator + "\n";
         body = body + "Due Amount: " + df.format(btr.amountDue) + " " + btr.currencyDue + "\n";
         body = body + "Date Due: " + btr.dateDue + "\n";
         if (btr.type == BankTransferReceivedData.TypeEnum.REVERSAL || btr.type == BankTransferReceivedData.TypeEnum.REVERSAL_CORRECTION) {
            body = body + "\n";
            body = body + btr.type + "\n";
            body = body + "Reversal Amount: " + df.format(btr.reversalAmount) + " " + btr.reversalCurrency + "\n";
            body = body + "Reversal Reason ID: " + btr.reversalReasonID + "\n";
            body = body + "Reversal Reason: " + btr.reversalReasonDescription + "\n";
            body = body + "Date Collect: " + btr.dateCollect + "\n";
         }

         body = body + "Source File: " + btr.fileName + " Row: " + btr.row + "\n";
         logger.info("Sending bank transfer notification to " + destEmail + ": " + body);

         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            messageEJB.sendEmailFromNoReply(destEmail, subject, body);
         } catch (Exception var8) {
            logger.error("Unable to send bank transfer notification email. Subject: " + subject, var8);
            return;
         }

         this.bankTransferEmailsSent.add(btr.fileName + btr.row);
      }
   }

   private boolean isPayment(String line) {
      return line.startsWith("+IP") || line.startsWith("-IP") || line.startsWith("+RI") || line.startsWith("-RI");
   }

   private boolean isChargeBack(String line) {
      return line.startsWith("-CB");
   }

   private BankTransferReceivedData parseBankTransferReceived(String line) {
      BankTransferReceivedData.TypeEnum type = null;
      if (line.startsWith("+IP")) {
         type = BankTransferReceivedData.TypeEnum.PAYMENT;
      } else if (line.startsWith("-IP")) {
         type = BankTransferReceivedData.TypeEnum.CORRECTION;
      } else if (line.startsWith("+RI")) {
         type = BankTransferReceivedData.TypeEnum.REVERSAL;
      } else {
         if (!line.startsWith("-RI")) {
            return null;
         }

         type = BankTransferReceivedData.TypeEnum.REVERSAL_CORRECTION;
      }

      BankTransferReceivedData bankTransferReceivedData = new BankTransferReceivedData();
      bankTransferReceivedData.type = type;
      bankTransferReceivedData.paymentReference = line.substring(3, 15).trim();
      bankTransferReceivedData.invoiceNumber = line.substring(15, 35).trim();
      bankTransferReceivedData.customerID = line.substring(35, 50).trim();
      bankTransferReceivedData.additionalReference = line.substring(50, 70).trim();
      bankTransferReceivedData.effortNumber = Integer.parseInt(line.substring(70, 71));
      bankTransferReceivedData.invoiceCurrencyDeliv = line.substring(71, 81).trim();
      bankTransferReceivedData.invoiceAmountDeliv = Double.parseDouble(line.substring(81, 93)) / 100.0D;
      if (line.substring(93, 94).equals("-")) {
         bankTransferReceivedData.invoiceAmountDeliv = bankTransferReceivedData.invoiceAmountDeliv * -1.0D;
      }

      bankTransferReceivedData.invoiceCurrencyLocal = line.substring(94, 98).trim();
      bankTransferReceivedData.invoiceAmountLocal = Double.parseDouble(line.substring(98, 110)) / 100.0D;
      if (line.substring(110, 111).equals("-")) {
         bankTransferReceivedData.invoiceAmountLocal = bankTransferReceivedData.invoiceAmountLocal * -1.0D;
      }

      bankTransferReceivedData.paymentMethod = line.substring(200, 202).trim();
      bankTransferReceivedData.creditCardCompany = line.substring(202, 206).trim();
      bankTransferReceivedData.uncleanIndicator = line.substring(206, 207).trim();
      bankTransferReceivedData.paymentCurrency = line.substring(207, 211).trim();
      bankTransferReceivedData.paymentAmount = Double.parseDouble(line.substring(211, 223)) / 100.0D;
      if (line.substring(223, 224).equals("-")) {
         bankTransferReceivedData.paymentAmount = bankTransferReceivedData.paymentAmount * -1.0D;
      }

      bankTransferReceivedData.currencyDue = line.substring(224, 228).trim();
      bankTransferReceivedData.amountDue = Double.parseDouble(line.substring(228, 240)) / 100.0D;
      if (line.substring(240, 241).equals("-")) {
         bankTransferReceivedData.amountDue = bankTransferReceivedData.amountDue * -1.0D;
      }

      bankTransferReceivedData.dateDue = Integer.parseInt(line.substring(241, 249));
      if (type == BankTransferReceivedData.TypeEnum.REVERSAL || type == BankTransferReceivedData.TypeEnum.REVERSAL_CORRECTION) {
         bankTransferReceivedData.reversalCurrency = line.substring(300, 304).trim();
         bankTransferReceivedData.reversalAmount = Double.parseDouble(line.substring(304, 316)) / 100.0D;
         if (line.substring(316, 317).equals("-")) {
            bankTransferReceivedData.reversalAmount = bankTransferReceivedData.reversalAmount * -1.0D;
         }

         bankTransferReceivedData.reversalReasonID = line.substring(317, 319).trim();
         bankTransferReceivedData.reversalReasonDescription = line.substring(319, 344).trim();
         bankTransferReceivedData.dateCollect = Integer.parseInt(line.substring(344, 352));
      }

      return bankTransferReceivedData;
   }

   public static void main(String[] args) {
      logger.info("GlobalCollectReportFeed version @version@");
      logger.info("Copyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      if (args.length < 8) {
         logger.fatal("Usage: GlobalCollectReportFeed interval(minute) server username password merchantIDs remoteDir localDir fileExtension [filename]");
      } else {
         GlobalCollectReportFeed feed = new GlobalCollectReportFeed(args[1], args[2], args[3], args[4].split("[,;]"), args[5], args[6], args[7], args.length > 8 ? args[8] : null);
         if (args.length > 8) {
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.GLOBAL_COLLECT_REPORT_FEED, "file");
            feed.run();
         } else {
            (new Timer()).scheduleAtFixedRate(feed, 0L, (long)(Integer.parseInt(args[0]) * '\uea60'));
         }

      }
   }

   private class SSHUserInfo implements UserInfo {
      private String password;

      public SSHUserInfo(String password) {
         this.password = password;
      }

      public String getPassword() {
         return this.password;
      }

      public boolean promptYesNo(String str) {
         return true;
      }

      public String getPassphrase() {
         return null;
      }

      public boolean promptPassphrase(String message) {
         return true;
      }

      public boolean promptPassword(String message) {
         return true;
      }

      public void showMessage(String message) {
      }
   }
}
