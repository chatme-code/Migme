package com.projectgoth.fusion.reputation.file.util;

import com.projectgoth.fusion.reputation.file.SortBigFile;
import com.projectgoth.fusion.reputation.file.comparator.SessionArchiveByStartTimeFileEntryComparator;
import com.projectgoth.fusion.reputation.file.comparator.SessionArchiveByStartTimeStringListComparator;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import com.projectgoth.fusion.reputation.util.FileLocation;

public class SortSessionArchiveByStartTime {
   public static void main(String[] args) {
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      SortBigFile sort = new SortBigFile(directoryHolder);

      try {
         sort.go(new FileLocation(directoryHolder.getDataDirectory(), args[0]), new FileLocation(directoryHolder.getDataDirectory(), args[0]), new SessionArchiveByStartTimeFileEntryComparator(), new SessionArchiveByStartTimeStringListComparator(), 26, '|');
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

   }
}
