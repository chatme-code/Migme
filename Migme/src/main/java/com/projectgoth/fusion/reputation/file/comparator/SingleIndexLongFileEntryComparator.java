package com.projectgoth.fusion.reputation.file.comparator;

import com.projectgoth.fusion.reputation.file.FileEntry;
import java.util.Comparator;

public class SingleIndexLongFileEntryComparator implements Comparator<FileEntry> {
   private int index;

   public SingleIndexLongFileEntryComparator(int index) {
      this.index = index;
   }

   public int compare(FileEntry lhs, FileEntry rhs) {
      long lhsValue = Long.parseLong(lhs.getLine()[this.index]);
      long rhsValue = Long.parseLong(rhs.getLine()[this.index]);
      if (lhsValue < rhsValue) {
         return -1;
      } else {
         return lhsValue > rhsValue ? 1 : 0;
      }
   }
}
