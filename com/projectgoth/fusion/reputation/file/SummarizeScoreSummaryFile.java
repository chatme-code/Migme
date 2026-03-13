package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.domain.ScoreMetrics;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;

public class SummarizeScoreSummaryFile {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SummarizeScoreSummaryFile.class));
   private int totalUniqueUsers = 0;
   private DirectoryHolder directoryHolder;

   public SummarizeScoreSummaryFile(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
   }

   private void dumpUserData(BufferedWriter writer, ScoreMetrics metrics) throws IOException {
      writer.write(metrics.toLine());
      writer.newLine();
   }

   private void processFile(String filename) throws IOException {
      log.info("summarizing " + filename);
      long start = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(this.directoryHolder.getDataDirectory() + filename + ".processed"));
      ScoreMetrics metrics = new ScoreMetrics();

      String line;
      while((line = reader.readLine()) != null) {
         List<String> parts = StringUtil.split(line, ',');
         if (parts.size() != ScoreSummary.EXPECTED_FIELD_COUNT) {
            log.info("found more or less parts [" + parts.size() + "] in this line [" + line + "], skipping");
         } else {
            String username = (String)parts.get(0);
            if (metrics.getUsername() == null) {
               metrics.reset(username);
            }

            if (metrics.getUsername() != null) {
               int compareResult = username.compareTo(metrics.getUsername());
               if (compareResult > 0) {
                  ++this.totalUniqueUsers;
                  this.dumpUserData(writer, metrics);
                  metrics.reset(username);
               } else if (compareResult < 0) {
                  log.warn("found a smaller username after the previous one, is the file sorted incorrectly? line [" + line + "]");
               }
            }

            metrics.addChatRoomMessagesSent(Integer.parseInt((String)parts.get(1)));
            metrics.addPrivateMessagesSent(Integer.parseInt((String)parts.get(2)));
            metrics.addTotalTime(Integer.parseInt((String)parts.get(3)));
            metrics.addPhotosUploaded(Integer.parseInt((String)parts.get(4)));
            metrics.addKicks(Integer.parseInt((String)parts.get(5)));
            metrics.addAuthenticatedReferrals(Integer.parseInt((String)parts.get(6)));
            metrics.addRechargedAmount(Integer.parseInt((String)parts.get(7)));
            metrics.addVirtualGiftsReceived(Integer.parseInt((String)parts.get(8)));
            metrics.addVirtualGiftsSent(Integer.parseInt((String)parts.get(9)));
            metrics.addCallDuration(Integer.parseInt((String)parts.get(10)));
            metrics.addTotalScore(Integer.parseInt((String)parts.get(ScoreSummary.TOTAL_SCORE_INDEX)));
         }
      }

      writer.flush();
      writer.close();
      long end = System.currentTimeMillis();
      log.info("done summarizing" + filename + ", took " + (double)(end - start) / 1000.0D + " seconds");
      log.info("total unique users " + this.totalUniqueUsers);
   }

   public void go(String filename) throws IOException {
      this.processFile(filename);
   }

   public static void main(String[] args) {
      SummarizeScoreSummaryFile summarizeFile = new SummarizeScoreSummaryFile(DirectoryUtils.getDirectoryHolder());

      try {
         summarizeFile.go(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
