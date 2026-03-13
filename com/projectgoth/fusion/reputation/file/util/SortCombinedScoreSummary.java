package com.projectgoth.fusion.reputation.file.util;

import com.projectgoth.fusion.reputation.file.ScoreSummary;
import com.projectgoth.fusion.reputation.file.SortBigFile;
import com.projectgoth.fusion.reputation.file.comparator.ScoreSummaryFileEntryComparator;
import com.projectgoth.fusion.reputation.file.comparator.ScoreSummaryStringListComparator;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import com.projectgoth.fusion.reputation.util.FileLocation;

public class SortCombinedScoreSummary {
   public static void main(String[] args) {
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      SortBigFile sort = new SortBigFile(directoryHolder);

      try {
         sort.go(new FileLocation(directoryHolder.getDataDirectory(), args[0]), new FileLocation(directoryHolder.getDataDirectory(), args[0]), new ScoreSummaryFileEntryComparator(), new ScoreSummaryStringListComparator(), ScoreSummary.EXPECTED_FIELD_COUNT, ',');
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

   }
}
