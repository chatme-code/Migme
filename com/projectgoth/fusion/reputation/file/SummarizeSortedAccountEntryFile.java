package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.reputation.domain.AccountEntryMetrics;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import com.projectgoth.fusion.reputation.util.MeanUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class SummarizeSortedAccountEntryFile {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SummarizeSortedAccountEntryFile.class));
   public static SimpleDateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
   private LinkedList<Integer> topQuartileKicks = new LinkedList();
   private LinkedList<Integer> topQuartileReferrals = new LinkedList();
   private int totalKicks = 0;
   private int totalReferrals = 0;
   private double totalRecharged = 0.0D;
   private DirectoryHolder directoryHolder;

   public SummarizeSortedAccountEntryFile(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
   }

   private void dumpUserData(BufferedWriter writer, AccountEntryMetrics metrics) throws IOException {
      if (metrics.hasMetrics()) {
         this.totalKicks += metrics.getKicksInitiated();
         this.totalReferrals += metrics.getAuthenticatedReferrals();
         this.totalRecharged += metrics.getRechargedAmount();
         writer.write(metrics.toLine());
         writer.newLine();
      }

   }

   private void processFile(String filename) throws IOException {
      log.info("summarizing " + filename);
      long start = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(this.directoryHolder.getDataDirectory() + filename + ".processed"));
      int totalUniqueUsers = 0;
      AccountEntryMetrics metrics = new AccountEntryMetrics();

      while(true) {
         String line;
         while((line = reader.readLine()) != null) {
            List<String> parts = StringUtil.split(line, '|');
            if (parts.size() != 13) {
               log.info("found more or less parts [" + parts.size() + "] in this line [" + line + "], skipping");
            } else {
               String username = (String)parts.get(1);
               if (metrics.getUsername() == null) {
                  metrics.reset(username);
               }

               if (metrics.getUsername() != null && !username.equals(metrics.getUsername())) {
                  int compareResult = username.compareTo(metrics.getUsername());
                  if (compareResult > 0) {
                     this.dumpUserData(writer, metrics);
                     ++totalUniqueUsers;
                     metrics.reset(username);
                  } else if (compareResult < 0) {
                     log.info("WARNING, found a smaller username after the previous one, is the file sorted incorrectly? line [" + line + "]");
                  }
               }

               int type = Integer.parseInt((String)parts.get(3));
               if (type == AccountEntryData.TypeEnum.CHATROOM_KICK_CHARGE.value()) {
                  metrics.addKicksInitiated(1);
               } else if (type != AccountEntryData.TypeEnum.VOUCHER_RECHARGE.value() && type != AccountEntryData.TypeEnum.BANK_TRANSFER.value() && type != AccountEntryData.TypeEnum.CREDIT_CARD.value() && type != AccountEntryData.TypeEnum.BLUE_LABEL_ONE_VOUCHER.value() && type != AccountEntryData.TypeEnum.WESTERN_UNION.value() && type != AccountEntryData.TypeEnum.PREMIUM_SMS_RECHARGE.value() && type != AccountEntryData.TypeEnum.TELEGRAPHIC_TRANSFER.value() && type != AccountEntryData.TypeEnum.CREDIT_CARD_CHARGEBACK.value() && type != AccountEntryData.TypeEnum.BANK_TRANSFER_REVERSAL.value() && type != AccountEntryData.TypeEnum.WESTERN_UNION_REVERSAL.value()) {
                  if (type == AccountEntryData.TypeEnum.REFERRAL_CREDIT.value()) {
                     metrics.addAuthenticatedReferrals(1);
                  }
               } else {
                  double exchangeRate = Double.parseDouble((String)parts.get(12));
                  metrics.addRechargedAmount(Double.parseDouble((String)parts.get(6)) / exchangeRate);
               }
            }
         }

         writer.flush();
         writer.close();
         long end = System.currentTimeMillis();
         log.info("done summarizing " + filename + ", took " + (double)(end - start) / 1000.0D + " seconds");
         log.info("total unique users " + totalUniqueUsers + " total kicks " + this.totalKicks);
         log.info("mean value for top quartile kicks: " + MeanUtils.meanValue(this.topQuartileKicks));
         log.info("mean value for top quartile referrals: " + MeanUtils.meanValue(this.topQuartileReferrals));
         if (totalUniqueUsers > 0) {
            log.info("mean value for kicks " + this.totalKicks / totalUniqueUsers);
            log.info("total kicks " + this.totalKicks + " average " + (double)this.totalKicks / (double)totalUniqueUsers);
            log.info("total referrals " + this.totalReferrals + " average " + (double)this.totalReferrals / (double)totalUniqueUsers);
            log.info("total recharged " + this.totalRecharged + " average " + this.totalRecharged / (double)totalUniqueUsers);
         }

         return;
      }
   }

   public void go(String filename) throws IOException {
      this.processFile(filename);
   }

   public static void main(String[] args) {
      SummarizeSortedAccountEntryFile summarizeFile = new SummarizeSortedAccountEntryFile(DirectoryUtils.getDirectoryHolder());

      try {
         summarizeFile.go(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
