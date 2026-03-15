package com.projectgoth.fusion.reputation.file.comparator;

import com.projectgoth.fusion.reputation.file.FileEntry;
import java.util.Comparator;

public class SingleIndexIntegerFileEntryComparator implements Comparator<FileEntry> {
   private int index;

   public SingleIndexIntegerFileEntryComparator(int index) {
      this.index = index;
   }

   public int compare(FileEntry lhs, FileEntry rhs) {
      int lhsValue = Integer.parseInt(lhs.getLine()[this.index]);
      int rhsValue = Integer.parseInt(rhs.getLine()[this.index]);
      if (lhsValue < rhsValue) {
         return -1;
      } else {
         return lhsValue > rhsValue ? 1 : 0;
      }
   }
}
