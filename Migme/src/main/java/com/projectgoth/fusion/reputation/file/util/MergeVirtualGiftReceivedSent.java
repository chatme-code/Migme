package com.projectgoth.fusion.reputation.file.util;

import com.projectgoth.fusion.reputation.file.MergeSummaryFiles;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;

public class MergeVirtualGiftReceivedSent {
   public static void main(String[] args) {
      MergeSummaryFiles merger = new MergeSummaryFiles(DirectoryUtils.getDirectoryHolder());

      try {
         String runDateString = "";
         merger.mergeJoinFiles("merged.virtualgift." + runDateString, "virtualgiftsent." + runDateString + ".csv.sorted.processed", 0, 1, "virtualgiftreceived." + runDateString + ".csv.sorted.processed", 0, 1);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

   }
}
