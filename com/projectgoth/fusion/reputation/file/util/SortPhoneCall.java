package com.projectgoth.fusion.reputation.file.util;

import com.projectgoth.fusion.reputation.file.SortBigFile;
import com.projectgoth.fusion.reputation.file.comparator.PhoneCallFileEntryComparator;
import com.projectgoth.fusion.reputation.file.comparator.PhoneCallStringListComparator;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import com.projectgoth.fusion.reputation.util.FileLocation;

public class SortPhoneCall {
   public static void main(String[] args) {
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      SortBigFile sort = new SortBigFile(directoryHolder);

      try {
         sort.go(new FileLocation(directoryHolder.getDataDirectory(), args[0]), new FileLocation(directoryHolder.getDataDirectory(), args[0]), new PhoneCallFileEntryComparator(), new PhoneCallStringListComparator(), 4, '|', false);
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

   }
}
