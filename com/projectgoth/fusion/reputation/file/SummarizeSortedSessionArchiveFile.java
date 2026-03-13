package com.projectgoth.fusion.reputation.file;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.reputation.domain.SessionArchiveMetrics;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;

public class SummarizeSortedSessionArchiveFile {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(SummarizeSortedSessionArchiveFile.class));
   private int totalChatRoomMessages = 0;
   private int totalPrivateMessages = 0;
   private long totalTime = 0L;
   private int totalPhotosUploaded = 0;
   private int totalUniqueUsers = 0;
   private DirectoryHolder directoryHolder;

   public SummarizeSortedSessionArchiveFile(DirectoryHolder directoryHolder) {
      this.directoryHolder = directoryHolder;
   }

   private void dumpUserData(BufferedWriter writer, SessionArchiveMetrics metrics) throws IOException {
      this.totalChatRoomMessages += metrics.getChatRoomMessagesSent();
      this.totalPrivateMessages += metrics.getPrivateMessagesSent();
      this.totalTime += metrics.getTotalTime();
      this.totalPhotosUploaded += metrics.getPhotosUploaded();
      writer.write(metrics.toLine());
      writer.newLine();
   }

   private void processFile(String filename) throws IOException {
      log.info("summarizing " + filename);
      long start = System.currentTimeMillis();
      BufferedReader reader = new BufferedReader(new FileReader(this.directoryHolder.getDataDirectory() + filename));
      BufferedWriter writer = new BufferedWriter(new FileWriter(this.directoryHolder.getDataDirectory() + filename + ".processed"));
      long previousEndDate = 0L;
      SessionArchiveMetrics metrics = new SessionArchiveMetrics();

      String line;
      while((line = reader.readLine()) != null) {
         List<String> parts = StringUtil.split(line, '|');
         if (parts.size() != 26) {
            log.info("found more or less parts [" + parts.size() + "] in this line [" + line + "], skipping");
         } else {
            String username = (String)parts.get(1);
            if (metrics.getUsername() == null) {
               metrics.reset(username);
            }

            if (metrics.getUsername() != null) {
               int compareResult = username.compareTo(metrics.getUsername());
               if (compareResult > 0) {
                  ++this.totalUniqueUsers;
                  this.dumpUserData(writer, metrics);
                  metrics.reset(username);
                  previousEndDate = 0L;
               } else if (compareResult < 0) {
                  log.warn("found a smaller username after the previous one, is the file sorted incorrectly? line [" + line + "]");
               }
            }

            metrics.addChatRoomMessagesSent(Integer.parseInt((String)parts.get(17)));
            metrics.addPrivateMessagesSent(Integer.parseInt((String)parts.get(14)));
            metrics.addPhotosUploaded(Integer.parseInt((String)parts.get(25)));
            long startDate = Long.parseLong((String)parts.get(3));
            long endDate = Long.parseLong((String)parts.get(4));
            if (startDate > previousEndDate) {
               metrics.addTotalTime(endDate - startDate);
            }

            previousEndDate = endDate;
         }
      }

      writer.flush();
      writer.close();
      long end = System.currentTimeMillis();
      log.info("done summarizing" + filename + ", took " + (double)(end - start) / 1000.0D + " seconds");
      log.info("total unique users " + this.totalUniqueUsers);
      if (this.totalUniqueUsers > 0) {
         log.info("total chat room messages " + this.totalChatRoomMessages + " average " + (double)this.totalChatRoomMessages / (double)this.totalUniqueUsers);
         log.info("total private messages " + this.totalPrivateMessages + " average " + (double)this.totalPrivateMessages / (double)this.totalUniqueUsers);
         log.info("total time online " + this.totalTime + " average " + (double)this.totalTime / (double)this.totalUniqueUsers);
         log.info("total photos uploaded " + this.totalPhotosUploaded + " average " + (double)this.totalPhotosUploaded / (double)this.totalUniqueUsers);
      }

   }

   public void go(String filename) throws IOException {
      this.processFile(filename);
   }

   public static void main(String[] args) {
      SummarizeSortedSessionArchiveFile summarizeFile = new SummarizeSortedSessionArchiveFile(DirectoryUtils.getDirectoryHolder());

      try {
         summarizeFile.go(args[0]);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
