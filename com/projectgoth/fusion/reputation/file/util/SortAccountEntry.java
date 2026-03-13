package com.projectgoth.fusion.reputation.file.util;

import com.projectgoth.fusion.reputation.file.SortBigFile;
import com.projectgoth.fusion.reputation.file.comparator.AccountEntryFileEntryComparator;
import com.projectgoth.fusion.reputation.file.comparator.AccountEntryStringListComparator;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import com.projectgoth.fusion.reputation.util.FileLocation;

public class SortAccountEntry {
   public static void main(String[] args) {
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      SortBigFile sort = new SortBigFile(directoryHolder);

      try {
         String lastLine = sort.go(new FileLocation(directoryHolder.getDataDirectory(), args[0]), new FileLocation(directoryHolder.getDataDirectory(), args[0]), new AccountEntryFileEntryComparator(), new AccountEntryStringListComparator(), 13, '|');
         System.out.println("last line = " + lastLine);
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

   }
}
