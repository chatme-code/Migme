package com.projectgoth.fusion.reputation.file.util;

import com.projectgoth.fusion.reputation.file.SortBigFile;
import com.projectgoth.fusion.reputation.file.comparator.VirtualGiftReceivedByReceiverFileEntryComparator;
import com.projectgoth.fusion.reputation.file.comparator.VirtualGiftReceivedByReceiverStringListComparator;
import com.projectgoth.fusion.reputation.util.DirectoryHolder;
import com.projectgoth.fusion.reputation.util.DirectoryUtils;
import com.projectgoth.fusion.reputation.util.FileLocation;

public class SortVirtualGiftReceived {
   public static void main(String[] args) {
      DirectoryHolder directoryHolder = DirectoryUtils.getDirectoryHolder();
      SortBigFile sort = new SortBigFile(directoryHolder);

      try {
         sort.go(new FileLocation(directoryHolder.getDataDirectory(), args[0]), new FileLocation(directoryHolder.getDataDirectory(), args[0]), new VirtualGiftReceivedByReceiverFileEntryComparator(), new VirtualGiftReceivedByReceiverStringListComparator(), 8, '|', false);
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

   }
}
